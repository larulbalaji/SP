package sailpoint.seri.tools.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class FilterExport extends Task {


	/* Filters an export from IdentityIQ
	 * Just takes objects modified or created after the "since" date
	 */

	private String exportFile=null;
	private String outputDir=null;
	private String since=null;
	private String sincePattern="yyyyMMddHHmmssSSS";
	
	private boolean includeSysLog=false;
	private boolean includeTaskResult=false;
	private boolean includeRequest=false;
	
	// for crlf and tabs..
	XMLEventFactory eventFactory;
	XMLEvent evtCRLF;
	XMLEvent evtTab;


	public static void main(String[] args) {
		FilterExport fe=new FilterExport();
		fe.setExportFile("C:\\Users\\kev\\AppData\\Local\\Temp\\export9724251\\null1343030311.xml");
		fe.setOutputDir("C:\\Users\\kev\\AppData\\Local\\Temp\\export9724251\\out");
		fe.setSince("20130729150444660");
		fe.setSincePattern("yyyyMMddHHmmssSSS");
		fe.execute();
	}

	public void execute() throws BuildException {

		if (exportFile==null) {
			throw new BuildException("exportFile must be specified");
		}

		if (outputDir==null) {
			throw new BuildException("outputDir must be specified");
		}

		if (since==null) {
			throw new BuildException("since must be specified");
		}

		SimpleDateFormat sdf=new SimpleDateFormat(sincePattern);

		try {
			Date from=sdf.parse(since);

			File f=new File(exportFile);
			if(!f.exists()) {
				throw new BuildException("Can't find exportFile "+exportFile);
			}
			File out=new File(outputDir);
			if(!out.exists()) {
				throw new BuildException("Can't find outputDir "+outputDir);
			}
			if(!out.isDirectory()) {
				throw new BuildException("outputDir "+outputDir+" is not a directory");
			}

			eventFactory = XMLEventFactory.newInstance();
			evtCRLF = eventFactory.createCharacters("\n");
			evtTab = eventFactory.createCharacters("\t");

			doFilter(f, out, from.getTime());


		} catch (ParseException e) {
			throw new BuildException("Can't parse date '"+since+"' with pattern '"+sincePattern+"'");
		}
	}

	private void doFilter(File f, File out, long time) {

		int exported=0;
		int skipped=0;

		try {
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty("javax.xml.stream.supportDTD", false);
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

			// Setup a new eventReader
			InputStream in = new FileInputStream(f);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

			boolean writing=false;
			int depth=0;
			int elDepth=0;
			int tabs=0;
			String objName;
			XMLEventWriter eventWriter=null;

			XMLEvent lastEvent=null;

			StringBuilder chars=new StringBuilder();
			

			while (eventReader.hasNext()) {

				XMLEvent event = eventReader.nextEvent();

				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					if(writing) {
						// transfer this to the output file
						if(lastEvent.isStartElement()) {
							eventWriter.add(evtCRLF);
						}
						writeEl(eventWriter, depth-elDepth, startElement);
					} else if (depth==1) {
						long mod=getMod(startElement);
//						System.out.println("FilterExport.doFilter: time="+time+" mod="+mod);
						if( mod>time && shouldExport(startElement.getName().getLocalPart())) {
							eventWriter=outputFactory.createXMLEventWriter(new FileOutputStream(getFileName(out, startElement)));
							eventWriter.add(eventFactory.createDTD("<?xml version='1.0' encoding='UTF-8'?>\n<!DOCTYPE "+
									startElement.getName().getLocalPart()+" PUBLIC \"sailpoint.dtd\" \"sailpoint.dtd\">\n"));
							writeEl(eventWriter, 0, startElement);
							writing=true;
							elDepth=depth;
							exported++;
						} else {
							skipped++;
						}
					}
					chars=new StringBuilder();
					depth++;
					lastEvent=event;
				} else if(event.isEndElement()) {
					EndElement endEl = event.asEndElement();
					depth--;
					if(writing) {
						String content = chars.toString().trim();
						if(content.length()>0) {
							eventWriter.add(eventFactory.createCharacters(content));
							eventWriter.add(endEl);
						} else if (lastEvent.isStartElement()) {
							eventWriter.add(endEl);
						} else {
							writeEl(eventWriter, depth-elDepth, endEl);
						}
						eventWriter.add(evtCRLF);
						if(depth==elDepth) {
							writing=false;
							eventWriter.close();
						}
					}
					chars=new StringBuilder();
					lastEvent=event;
				} else if(event.isCharacters()) {
					chars.append(event.asCharacters().getData().trim());
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log("Exported "+exported+" artifacts, skipped "+skipped+" before snapshot date or excluded");
	}

	private boolean shouldExport(String localPart) {
		
		if("SyslogEvent".equals(localPart)) {
			return includeSysLog;
		}
		
		if("TaskResult".equals(localPart)) {
			return includeTaskResult;
		}
		
		if("Request".equals(localPart)) {
			return includeRequest;
		}
		return true;
	}

	private String getFileName(File out, StartElement startElement) {

		String objName=startElement.getName().getLocalPart();

		
		StringBuilder sb=new StringBuilder(out.getAbsolutePath());
		sb.append(File.separator);
		sb.append(startElement.getName().getLocalPart());
		sb.append("-");
		String namePart=null;
		if ("SyslogEvent".equals(objName)) {
			namePart=startElement.getAttributeByName(new QName("quickKey")).getValue();
		} else {
			namePart=startElement.getAttributeByName(new QName("name")).getValue();
		}
		sb.append(camelize(namePart));
		sb.append(".xml");

		return sb.toString();
	}

	private Object camelize(String namePart) {

		/* convert name to camelcase
		 * change underscores, hyphens and slashes to spaces
		 * then remove all the spaces, capitalizing the letter after the space
		 */

		namePart=namePart.replace('_', ' ');
		namePart=namePart.replace('/', ' ');
		namePart=namePart.replace('\\', ' ');
		namePart=namePart.replace(':', ' ');

		String[] parts=namePart.split(" ");
		StringBuilder sb=new StringBuilder();
		boolean first=true;
		for(String part: parts) {

			if(part.length()>0) {
				sb.append(part.substring(0,1).toUpperCase());
				sb.append(part.substring(1));
			}
		}

		return sb.toString();
	}

	private void writeEl(XMLEventWriter eventWriter, int j, XMLEvent ele) throws BuildException{

		if(ele.isStartElement()) {
			ArrayList<Attribute> attrs=new ArrayList<Attribute>();
			StartElement se=(StartElement)ele;
			Iterator<Attribute> oldAttrs=se.getAttributes();
			while(oldAttrs.hasNext()) {
				Attribute att=oldAttrs.next();
				String attName=att.getName().getLocalPart();
				if(!"id".equals(attName)&&
				   !"created".equals(attName)&&
				   !"modified".equals(attName)) {
					attrs.add(att);
				}
				ele=eventFactory.createStartElement(se.getName(), attrs.iterator(), se.getNamespaces());
			}
		}
		
		try {
			for(int i=0;i<j;i++) {
				eventWriter.add(evtTab);
			}
			eventWriter.add(ele);
		} catch (XMLStreamException e) {
			throw new BuildException (e);
		}
	}

	private long getMod(StartElement el) {

		/* Get the millisecond time that this was last
		 * modified
		 */

		Attribute since=el.getAttributeByName(new QName("modified"));
		if(since==null) {
			since=el.getAttributeByName(new QName("created"));
		}

		if(since==null) {		
			return 0;
		}
		return Long.parseLong(since.getValue());
	}

	public String getExportFile() {
		return exportFile;
	}
	public void setExportFile(String exportFile) {
		this.exportFile = exportFile;
	}
	public String getOutputDir() {
		return outputDir;
	}
	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}
	public String getSince() {
		return since;
	}
	public void setSince(String since) {
		this.since = since;
	}
	public String getSincePattern() {
		return sincePattern;
	}
	public void setSincePattern(String sincePattern) {
		this.sincePattern = sincePattern;
	}



}
