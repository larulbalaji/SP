package sailpoint.seri.openconnector;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.seri.integration.HelpDeskExecutor;

public class SOAPConnectorXMLHandler {
	
    private static Log log = LogFactory.getLog(SOAPConnectorXMLHandler.class);

	public static void main(String[] args) {
    	 String src="<results>"+
    	 "<object id=\"01\">"+
    	 "<attribute name=\"firstname\">Kev</attribute>"+
    	 "<attribute name=\"lastname\">James</attribute>"+
    	 "</object>"+
    	 "<object id=\"02\">"+
    	 "<attribute name=\"firstname\">Brian</attribute>"+
    	 "<attribute name=\"lastname\">Blessed</attribute>"+
    	 "</object>"+
    	 "</results>";

    	 Map<String, Map<String, Object>> x= parseResultString(src);
    	 /*main*/System.out.println(x.toString());
	}
	

	public static Map<String, Map<String, Object>> parseResultString(String ret) {

		// expect back
    	// <results>
    	// <object id="userid">
    	//   <attribute name="name">value</attribute>
    	// </object>
    	// </results>
    	
		Map<String, Map<String, Object>> retVal=new HashMap<String, Map<String, Object>>();
		
		Map<String, Object> object=null; 
		
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        
        XMLStreamReader xmlr;
        try {
                xmlr = xmlif.createXMLStreamReader("results", new StringReader(ret));
                int eventType = xmlr.getEventType(); // should be 7 for start document
                StringBuilder chars=new StringBuilder();
                String id=null;
                String property=null;
                String element=null;
                List<String> values=null;
                
                while (xmlr.hasNext()) {
                        
                        eventType=xmlr.next();
                        
                        switch(eventType) {
                       
                                case XMLEvent.START_ELEMENT:
                                	element=xmlr.getLocalName();

                                	if(("object").equals(element)) {
                                		object=new HashMap<String, Object>();
                                		id=xmlr.getAttributeValue("", "id");
                                	} else if(("attribute").equals(element)) {
                                		property=xmlr.getAttributeValue("", "name");
                                		values=new ArrayList<String>();
                                	} else if(("value").equals(element)) {
                                		// do nothing; maybe do something in future
                                		// value means there are a number of values
                                	}
                                    break;
                                case XMLEvent.END_ELEMENT:
                                	element=xmlr.getLocalName();
                                    String content = chars.toString().trim();
                                    chars=new StringBuilder();
                                        
                                    if ("attribute".equals(element)) {
                                    	if(values.size()>0) {
                                    		object.put(property, values);
                                    	} else {
                                    		object.put(property, content);
                                    	}
                                    } else if ("object".equals(element)) {
                                      retVal.put(id,  object);
                                    } else if ("value".equals(element)) {
                                    	values.add(content);
                                    }
                                    break;
                                case XMLEvent.CHARACTERS:
                                        chars.append(xmlr.getText());
                                default:
                                        //log.warn("testSTAX.buildFiltersLookup: eventType="+eventType);
                        }
                        
                }
        } catch (XMLStreamException e) {
                log.error(e);
        }
		
		return retVal;
	}

}
