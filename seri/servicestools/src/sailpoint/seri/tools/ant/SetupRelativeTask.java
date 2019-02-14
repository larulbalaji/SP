package sailpoint.seri.tools.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class SetupRelativeTask extends Task{

	
/*
 * 	 takes an xml importer file and makes all include references relative to a directory in the path
 * 	 for example
 * 
 *   <setupRelative baseDir="WEB-INF">
 *   <fileset dir="catalog">
 *      <include name="** /setup.xml"/>
 *	 </fileset>
 *	 <setupRelative>
 *   could take
 *   <?xml version='1.0' encoding='UTF-8'?>
 *   <!DOCTYPE sailpoint PUBLIC 'sailpoint.dtd' 'sailpoint.dtd'>
 *   <sailpoint>
 *     <ImportAction name='include' value='Correlation-ADCorrelation.xml'/>
 *     <ImportAction name='include' value='Rule-ProvisioningPolicyField-ADDistinguishedName.xml'/>
 *     <ImportAction name='include' value='Configuration-LocationOUConfiguration.xml'/>
 *     <ImportAction name='include' value='Application-ActiveDirectory.xml'/>
 *   </sailpoint>
 *   
 *   and make it 
 *    *   <?xml version='1.0' encoding='UTF-8'?>
 *   <!DOCTYPE sailpoint PUBLIC 'sailpoint.dtd' 'sailpoint.dtd'>
 *   <sailpoint>
 *     <ImportAction name='include' value='config/seri/catalog/Resource-ActiveDirectory/Correlation-ADCorrelation.xml'/>
 *     <ImportAction name='include' value='config/seri/catalog/Resource-ActiveDirectory/Rule-ProvisioningPolicyField-ADDistinguishedName.xml'/>
 *     <ImportAction name='include' value='config/seri/catalog/Resource-ActiveDirectory/Configuration-LocationOUConfiguration.xml'/>
 *     <ImportAction name='include' value='config/seri/catalog/Resource-ActiveDirectory/Application-ActiveDirectory.xml'/>
 *   </sailpoint>
 */	
	
	
	
	private DocumentBuilder _dBuilder = null;

	private Vector<FileSet> filesets = new Vector<FileSet>();
    private String setupFile = null;
    private String baseDir = null;
	
	public void execute() throws BuildException {
		if (baseDir==null) {
			throw new BuildException("baseDir must be specified");
		}
		if (setupFile==null && filesets.size() == 0 )
			throw new BuildException("You must include a setup file or set of setup files");
		else { // lets create an init file 

			int numFiles=0;
			if(setupFile!=null) {
				numFiles=1;
			} else {
				for (FileSet fs: filesets) {
					numFiles+=fs.size();
				}
			}
			
			log("Making "+numFiles+" setup files relative to config directory");
			
			if(setupFile!=null) {
				makeRelative(setupFile);
			} else {
				for (FileSet fs: filesets) {
					makeRelative(fs);
				}
			}
		
		}
	}
	
	public void addFileSet(FileSet fileset) {
    	if (!filesets.contains(fileset)) {
    		filesets.add(fileset);
    	}
    }
	
	public void setSetupFile(String file) {
		this.setupFile=file;
	}

	public void setBaseDir(String dir) {
		this.baseDir=dir;
	}
	private void makeRelative(String setupFile) {
		throw new BuildException("Not yet implemented");
	}
	
	private void makeRelative(FileSet fs) {
		DirectoryScanner ds = fs.getDirectoryScanner(getProject());
		String[] files = ds.getIncludedFiles();
        // Loop through files
        for (String file: files) {
        	
        	String newFile=ds.getBasedir().getAbsolutePath()+File.separatorChar+file;
            File theFile=new File(newFile);
            String parent=theFile.getParent();
        	        		
        	log("SetupRelativeTask.makeRelative: " + newFile, Project.MSG_DEBUG);

        	Document setupDoc=buildDocument(newFile);
        	Element e=setupDoc.getDocumentElement();
        	replaceImportActionValues(e, parent, baseDir);
        	
        	OutputFormat format = new OutputFormat(setupDoc);
        	try {
	        	FileOutputStream fos=new FileOutputStream(newFile);
	            format.setIndenting(true);
	            XMLSerializer serializer = new XMLSerializer(fos, format);
            	serializer.serialize(setupDoc);
            	fos.close();
            } catch (IOException ioe){
            	throw new BuildException("IOEXception writing "+newFile+": "+ioe);
            }
        }
	}
	
	private void replaceImportActionValues(Node e, String parentDir, String baseDir) {
		
		// find the bit of the parentDirectory that is above baseDir
		int base=parentDir.lastIndexOf(baseDir);
		String prefix=".";
		
		if(base!=-1) {
			
			base++; // for the separator
			base+=baseDir.length();
			
			prefix=parentDir.substring(base);
			prefix=prefix.replace(File.separatorChar, '/'); // for java path separator
		}
		
		NodeList nl=e.getChildNodes();
		
		for(int i=0;i<nl.getLength();i++) {
			Node node = nl.item(i);
			if(node.hasChildNodes()) {
				replaceImportActionValues(node, parentDir, baseDir);
			}			
			if("ImportAction".equals(node.getNodeName()) ) {
			  Node name=node.getAttributes().getNamedItem("name");
			  if (name!=null && "include".equals(name.getNodeValue())) {
				Node value=node.getAttributes().getNamedItem("value");
  				if(value!=null) {
  					String nodeValue = value.getNodeValue();
  					boolean changed=false;
  					if(nodeValue.startsWith("/")) {
  						nodeValue=nodeValue.substring(1);
  						changed=true;
  					} else {
  						String newValue=prefix+"/"+nodeValue;
  						URI uri;
  						try {
  							uri = new URI(newValue);
  							nodeValue = uri.normalize().getPath();
  							changed=true;
  						} catch (URISyntaxException e1) {
  							log("SetupRelativeTask.replaceImportActionValues: URI Generation failed for "+newValue);
  						}
  					}
  					if(changed) {
  						value.setNodeValue(nodeValue);
  					}
  				}
			  }
			}
		}
		
	}
	
	private Document buildDocument(String xml){
		try {
			DocumentBuilder db=getDocumentBuilder();
			InputStream is=new FileInputStream(xml);
			Document srcDoc = db.parse(is);
			is.close();
			return srcDoc;
		} catch (SAXException e) {
			System.out.println("SOAPConnector.buildDocument: SAXException "+e);
			throw new BuildException(e);
		} catch (IOException e) {
			System.out.println("SOAPConnector.buildDocument: IOException "+e);
			throw new BuildException(e);
		}
	}
	
	private DocumentBuilder getDocumentBuilder(){
		if(_dBuilder==null) {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setValidating(false);
				dbFactory.setNamespaceAware(true);
				_dBuilder = dbFactory.newDocumentBuilder();
				_dBuilder.setEntityResolver(new EntityResolver() {

					@Override
					public InputSource resolveEntity(String publicId, String systemId)
							throws SAXException, IOException {
						log("Ignoring " + publicId + ", " + systemId, Project.MSG_DEBUG);
						return new InputSource(new StringReader(""));
					}
				});
			} catch (ParserConfigurationException e) {
				log("SOAPConnector.buildDocument: PCE "+e);
				throw new BuildException(e);

			}
		}
		return _dBuilder;

	}

}
