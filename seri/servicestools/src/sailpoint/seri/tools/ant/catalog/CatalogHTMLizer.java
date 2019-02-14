package sailpoint.seri.tools.ant.catalog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class CatalogHTMLizer extends Task {

	private String catalogLocation;
	private String docLocation;
	private String templateLocation;

	public static void main(String[] args) throws BuildException {
		CatalogHTMLizer izer=new CatalogHTMLizer();
		izer.setCatalogLocation("/home/kjames/workspace/seri/config/catalog");
		izer.setDocLocation("/home/kjames/seri-doc");
		izer.setTemplateLocation("/home/kjames/workspace/seri/servicestools/doc/templates");
		izer.execute();
	}

	public String getCatalogLocation() {
		return catalogLocation;
	}

	public void setCatalogLocation(String arg1) {
		this.catalogLocation = arg1;
	}

	public String getDocLocation() {
		return docLocation;
	}

	public void setDocLocation(String arg2) {
		this.docLocation= arg2;
	}

	public String getTemplateLocation() {
		return templateLocation;
	}

	public void setTemplateLocation(String templateLocation) {
		this.templateLocation = templateLocation;
	}

	public void execute() throws BuildException {

		Map<String,CatalogSection> sections=new HashMap<String,CatalogSection>();

		if(catalogLocation==null) {
			throw new BuildException("must have a catalog location");
		}

		if(docLocation==null) {
			throw new BuildException("must have a doc location");
		}

		// Check catalogLocation exists
		File cat=new File(catalogLocation);
		if(!cat.exists()) {
			throw new BuildException("unable to find catalog location");
		}
		File doc=new File(docLocation);
		if(!doc.exists()) {
			doc.mkdirs();
		}
		// Enumerate entries
		String[] catEntries=cat.list();
		// foreach entry
		for(String catEntry: catEntries) {
			// add entry to section
			File f=new File(catalogLocation+File.separator+catEntry);
			if(f.isDirectory()) {
				int sep=catEntry.indexOf("-");
				if(sep==-1) {
					log("Skipping invalid entry "+catEntry);
				} else {
					String section=catEntry.substring(0,sep).trim();
					String entry=catEntry.substring(sep+1).trim();
					File test=new File(catalogLocation+File.separator+section+"-"+entry+File.separator+"readme.txt");
					CatalogSection l=sections.get(section);
					if(l==null) l=new CatalogSection(section);
					CatalogEntry ce=new CatalogEntry(entry);				
					if(!test.exists()) {
						ce.setValid(false);
					}
					l.addEntry(ce);
					sections.put(section, l);

				}
			}
		}

		try {
			Velocity.setProperty("resource.loader", "file");
			Velocity.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			Velocity.setProperty("file.resource.loader.path", templateLocation);
			//templateLocation);
			Velocity.setProperty("file.resource.loader.cache", "false");
			Velocity.init();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		VelocityContext context = new VelocityContext();
		context.put( "catalog", sections );

		String templateName = "Catalog.template";

		useTemplate(context, templateName, docLocation+File.separator+"index.html");

		// translate the readme
		for(CatalogSection cs: sections.values()) {
			// create individual readme
			String name=cs.getSectionName();
			List<CatalogEntry> entries=cs.getEntries();
			for(CatalogEntry entry: entries) {
				if (entry.isValid()) {
					translateReadme(name, entry.getName());
				}
			}
		}

	}

	private void translateReadme(String section, String entry) throws BuildException {
		File dir=new File(docLocation+File.separator+section+File.separator+entry);
		if(!dir.exists()) dir.mkdirs();

		// parse the readme.
		// It should look like this:
		/*
		 * 		NameOfThisThing v x.y.z
		 * 		Date of this revision/version
		 * 		Contact: person.who.made.it@sailpoint.com
		 *
		 *		************************        ignore lines like this
		 *		* Library dependencies *        make lines like this headers
		 */


		try {
			BufferedReader br=new BufferedReader(new FileReader(catalogLocation+File.separator+section+"-"+entry+File.separator+"readme.txt"));
			Readme r=new Readme();
			String line=br.readLine();

			int v=line.lastIndexOf(" v");
			// get the name and the version
			if(v==-1) {
				r.setName(line);
				r.setVersion("v0.0.0");
			} else {
				r.setName(line.substring(0,v+1).trim());
				r.setVersion(line.substring(v+1).trim());
			}
			ArrayList<String> title=new ArrayList<String>();
			List<String> content=new ArrayList<String>();
			boolean inTitle=false;
			while( (line=br.readLine()) !=null ) {
				if(line.startsWith("**")) {
					if (!inTitle) {
						r.addContent(title, content);
						title=new ArrayList<String>();
						content=new ArrayList<String>();
						inTitle=true;
					}
					inTitle=!inTitle;
					// ignore
				} else if(line.startsWith("* ")) {
					String x=line.substring(2).trim();
					if(x.endsWith(" *")) x=x.substring(0,x.length()-2);
					title.add(x);
				} else {
					content.add(line.trim());
				}			
			}
			r.addContent(title, content);


			br.close();
			r.addContent(title, content);
			VelocityContext context = new VelocityContext();

			context.put( "readme", r );
			String templateName = "Readme.template";

			useTemplate(context, templateName, docLocation+File.separator+section+File.separator+entry+"-"+"readme.html");


		} catch (FileNotFoundException e) {
			log("CatalogHTMLizer.translateReadme: No readme for "+section+"-"+entry, Project.MSG_ERR);
			return;
		} catch (IOException ioe) {
			throw new BuildException("IOException "+ioe);
		}

	}

	private void useTemplate(VelocityContext context,
			String templateName, String outputName) throws  BuildException {
		Template template = null;

		try
		{
			template = Velocity.getTemplate(templateName);
		}
		catch( ResourceNotFoundException rnfe )
		{
			throw new BuildException("CatalogHTMLizer.execute: no such template "+templateName);
		}
		catch( ParseErrorException pee )
		{
			System.out.println("CatalogHTMLizer.useTemplate: PEE "+pee);
			throw new BuildException("Problem parsing the template");
			// syntax error: problem parsing the template
		}
		catch( MethodInvocationException mie )
		{
			throw new BuildException("Something in the template threw an exception");

			// something invoked in the template
			// threw an exception
		}
		catch( Exception e )
		{
			throw new BuildException("Exception "+e);
		}

		try {
			FileWriter fw=new FileWriter(outputName);

			template.merge( context, fw );
			fw.close();
		} catch (ResourceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MethodInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
