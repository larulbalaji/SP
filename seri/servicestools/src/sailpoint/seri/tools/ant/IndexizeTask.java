package sailpoint.seri.tools.ant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class IndexizeTask extends Task{


  /*
   * 	 takes a hbm.xml file (currently identity only) and adds indices to <property>s that don't have an index
   */	

  private String inputFile  = null;
  private String outputFile = null;

  private static final String tempElementName="aVeryLongNameThatWontBeUsed";

  public void execute() throws BuildException {
    
    if (inputFile==null) {
      throw new BuildException("HBM input filename must be specified");
    }
    if (outputFile==null) {
      throw new BuildException("HBM output filename must be specified");
    }

    XMLInputFactory xmlif = XMLInputFactory.newFactory();
    XMLStreamReader xmlr  = null;

    StringBuilder sb=new StringBuilder();
        
    try {
      xmlr=xmlif.createXMLStreamReader(inputFile, new WrappingFileInputStream(inputFile));


      while(xmlr.hasNext()) {
        int eType=xmlr.next();
        switch(eType) {
          case XMLStreamConstants.START_ELEMENT:
            sb.append(printElement(xmlr));
            break;
          case XMLStreamConstants.CHARACTERS:
          case XMLStreamConstants.COMMENT:
            if (eType==XMLStreamConstants.COMMENT) {
              sb.append("<!--");
            }
            sb.append(xmlr.getText());
            if (eType==XMLStreamConstants.COMMENT) {
              sb.append("-->");
            }
            break;
        }
      }
    } catch (FileNotFoundException e) {
      throw new BuildException("can't find HBM file "+inputFile);
    } catch (XMLStreamException e) {
      throw new BuildException(e);
    }
    try {
      FileWriter fw=new FileWriter(outputFile);
      fw.write(sb.toString());
      fw.close();
    } catch (IOException ioe) {
      throw new BuildException("Error writing output HBM output file : "+ioe);
    }

  }

  private String printElement(XMLStreamReader xmlr) {
    // TODO Auto-generated method stub
    String elName = xmlr.getLocalName();
    if (elName.equals(tempElementName)) return "";
    StringBuilder sb=new StringBuilder();
    sb.append("<");
    sb.append(elName);
    boolean indexed=false;
    String idAttrName=null;
    for (int i=0; i<xmlr.getAttributeCount(); i++) {
      String attrName = xmlr.getAttributeLocalName(i);
      String attrValue = xmlr.getAttributeValue(i);
      if(attrName.equals("index")) {
        indexed=true;
      }
      if(attrName.equals("name")) {
        idAttrName=attrValue;
      }
      sb.append(" ");
      sb.append(attrName);
      sb.append("=\"");
      sb.append(attrValue);
      sb.append("\"");

    }
    if (!indexed && elName.equals("property")) { // only index properties)
      sb.append(" index=\"spt_identity_");
      sb.append(idAttrName);
      sb.append("_ci\"");
    }
    sb.append("/>");
    return sb.toString();
  }

  public void setInputFile(String name) {
    this.inputFile=name;
  }

  public void setOutputFile(String name) {
    this.outputFile=name;
  }
  
  private class WrappingFileInputStream extends InputStream {

    private boolean inStart=true;
    private boolean inEnd=false;

    private int startIndex=0;
    private int endIndex=0;

    private String start="<"+tempElementName+">";
    private String end="</"+tempElementName+">";

    private FileInputStream fs=null;

    public WrappingFileInputStream(String name) throws FileNotFoundException {

      fs=new FileInputStream(name);
      // TODO Auto-generated constructor stub
    }

    @Override
    public int read() throws IOException {

      if (inStart) {
        if (startIndex<start.length()) {
          int ch = start.charAt(startIndex++);
          return ch;
        } else {
          inStart=false;
        }
      }
      int i=fs.read();
      if(i==-1) {
        if (endIndex<end.length()) {
          return end.charAt(endIndex++);
        }
      } else {
        return i;
      }
      return -1;
    }

  }


}
