package sailpoint.seri.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.AuthenticationAnswer;
import sailpoint.object.AuthenticationQuestion;
import sailpoint.object.Identity;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Internationalizer;
import sailpoint.tools.RFC4180LineIterator;
import sailpoint.tools.RFC4180LineParser;
import sailpoint.tools.Util;

public class ChallengeQuestionAnswerImporterExecutor extends AbstractTaskExecutor {

	public static final String NUM_LINES = "numLines";
	public static final String ANSWERS_CREATED = "answersCreated";
	public static final String IDENTITIES_NOT_FOUND = "identitiesNotFound";
	public static final String QUESTIONS_NOT_FOUND = "questionsNotFound";
	public static final String ERROR_LINES = "errorLines";
	public static final String SEPARATOR_CHAR = ",";

	private static final String ownerName="spadmin";

	/* This task takes a csv file of the following format:
	 * identity,question,answer
	 * 
	 * And adds the answer for the question to the identity
	 */
	private static Log log = LogFactory.getLog(ChallengeQuestionAnswerImporterExecutor.class);


	private String answerFile = "";
	private String sLocale;

	private int numlines=0;
	private int answersCreated=0;
	private int identitiesNotFound=0;
	private int questionsNotFound=0;
	private int errorLine=0;

	private Locale locale=Locale.ENGLISH;

	// take copies of execute parameters, to avoid passing with every method signature
	private SailPointContext ctx;
	
	private boolean terminated=false;

	@Override
	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
					throws Exception {

		this.ctx=context;

		// Parse the parameters
		sLocale=args.getString("locale");
		if(sLocale!=null) {
			Locale loc=new Locale(sLocale);
			if(loc!=null) this.locale=loc;
		}
		answerFile=args.getString("filename");



		// Open the file and get a handle on it.
		InputStream stream = getFileStream();

		RFC4180LineIterator lines = null;
		lines = new RFC4180LineIterator(new BufferedReader(new InputStreamReader(stream)));

		// Process each of the lines
		if (lines != null) {
			String line=null;
			while( (line=lines.readLine()) !=null && !terminated) {
				setAnswer(line);
			}
		}

		result.setAttribute(NUM_LINES, new Integer(numlines));
		result.setAttribute(ANSWERS_CREATED, new Integer(answersCreated));
		result.setAttribute(IDENTITIES_NOT_FOUND, new Integer(identitiesNotFound));
		result.setAttribute(QUESTIONS_NOT_FOUND, new Integer(questionsNotFound));
		result.setAttribute(ERROR_LINES, new Integer(errorLine));

	}

	@Override
	public boolean terminate() {
		terminated=true;
		return true;
	}

	private void setAnswer(String line) {

		RFC4180LineParser parser=new RFC4180LineParser(SEPARATOR_CHAR);

		try {
			numlines++;
			List<String> tokens=parser.parseLine(line);
			if(tokens.size()!=3) {
				log.debug("Incorrect number of tokens("+tokens.size()+"): "+line);
				errorLine++;
				return;
			}
			String identity=tokens.get(0);
			String question=tokens.get(1);
			if(question==null) {
				log.debug("question was null: "+line);
				errorLine++;
				return;
			}
			String answer=tokens.get(2);
			if(answer==null) {
				log.debug("answer was null: "+line);
				errorLine++;
				return;
			}

			Identity targetIdentity=ctx.getObjectByName(Identity.class, identity);
			if(targetIdentity==null) {
				log.debug("Identity not found: "+identity);
				identitiesNotFound++;
				return;
			}

			// ok, so here we need to iterate through the questions, and find the right one
			// the ootb questions are i18n'ed, so they are stored as e.g. auth_question_mothers_maiden_name
			// I guess we need to check for both, so use the Internationalizer
			List<AuthenticationQuestion> questions=ctx.getObjects(AuthenticationQuestion.class);
			boolean found=false;
			for(AuthenticationQuestion q: questions) {
				if( question.equals(q.getQuestion()) ) {
					found=true;
				} else {
					String intlQuestion =Internationalizer.getMessage(q.getQuestion(), locale);
					if( question.equals(intlQuestion) ) {
						found=true;
					}
				}
				if(found) {
					List<AuthenticationAnswer> answers=targetIdentity.getAuthenticationAnswers();
					if(answers==null) answers=new ArrayList<AuthenticationAnswer>();
					for(AuthenticationAnswer aAnswer: answers) {
						// if we find it, take it out and exit the for loop
						if( aAnswer.getQuestion().getId().equals(q.getId()) ) {
							answers.remove(aAnswer);
							break; // out of the for loop
						}
					}
					// make a new entry with this answer
					AuthenticationAnswer ans=new AuthenticationAnswer();
					ans.setQuestion(q);
					ans.setAnswer(answer);
					answers.add(ans);
					targetIdentity.assignAuthenticationAnswers(answers);
					ctx.saveObject(targetIdentity);
					ctx.commitTransaction();

					break; // out of the for loop for questions
				}				
			}
			if(found) answersCreated++; 
			else questionsNotFound++;

		} catch (GeneralException ge) {
			log.debug("Error line: "+line);
			errorLine++;
		}
	}

	/**
	 * Get the input File Stream.
	 */
	private InputStream getFileStream() throws Exception {
		InputStream stream = null;

		if (answerFile == null) {
			throw new GeneralException("Filename cannot be null.");
		}
		try {
			File file = new File(answerFile);
			if (!file.exists()) {
				// sniff the file see if its relative if it is
				// see if we can append sphome to find it
				if (!file.isAbsolute()) {
					String appHome = getAppHome();
					if (appHome != null) {
						file = new File(appHome + File.separator + answerFile);
						if (!file.exists()) {
							file = new File(answerFile);
						}
					}
				}
			}
			// This will throw an exception if the file cannot be found
			stream = new BufferedInputStream(new FileInputStream(file));
		} catch (Exception e) {
			throw new GeneralException(e);
		}

		return stream;
	}

	/**
	 * Try to get the app home to be smart about locating relative paths.
	 */
	private String getAppHome() {
		String home = null;
		try {
			home = Util.getApplicationHome();
		} catch (Exception e) {
			log.error("Unable to find application home.");
		}
		return home;
	}

}
