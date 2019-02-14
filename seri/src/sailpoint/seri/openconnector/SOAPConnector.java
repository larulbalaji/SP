package sailpoint.seri.openconnector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import openconnector.AbstractConnector;
import openconnector.Connector;
import openconnector.ConnectorConfig;
import openconnector.ConnectorException;
import openconnector.Filter;
import openconnector.Item;
import openconnector.Log;
import openconnector.ObjectAlreadyExistsException;
import openconnector.ObjectNotFoundException;
import openconnector.Result;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Application;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningResult;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class SOAPConnector extends AbstractConnector {

	protected String endPoint;
	protected String stylesheet;

	private final static String CONNECTOR_TYPE="SOAPOut";

	private Iterator<Map<String, Object>> it = null;
	private Filter _filter;

	DocumentBuilder _dBuilder = null;

	public SOAPConnector() {
		super();
	}

	public SOAPConnector(ConnectorConfig config, Log log) {
		super(config, log);
	}

	public String getConnectorType() {
		return CONNECTOR_TYPE;
	}

	public void testConnection() {
		endPoint = config.getString("endpoint");
		stylesheet = config.getString("stylesheet");

		// TODO: Test endpoint is reachable
		log.debug("Test Connection");
		log.debug("endPoint="+endPoint);
		try {
			URL u=new URL(endPoint);
			URLConnection conn=u.openConnection();
		} catch (IOException ioe) {
			throw new ConnectorException("Cannot connect to Endpoint");
		}
		log.debug("stylesheet="+stylesheet);
		// open and load in stylesheet
		try {			
			DocumentBuilder db=getDocumentBuilder();
			InputStream is=getFileStream(stylesheet);
			if(is==null) {
				throw new ConnectorException("Can't find stylesheet in filesystem or classpath");
			}
			Document sSheet = db.parse(is);
        	OutputFormat format = new OutputFormat(sSheet);
        	if(log.isTraceEnabled()) {
        		ByteArrayOutputStream baos=new ByteArrayOutputStream(); 
	        	try {
		            format.setIndenting(true);
		            XMLSerializer serializer = new XMLSerializer(baos, format);
	            	serializer.serialize(sSheet);
	            	String trace=baos.toString();
	            	log.trace(trace);
	            } catch (IOException ioe){
	            }
        	}
		} catch (GeneralException e) {
			throw new ConnectorException("Unable to configure XML Parser");
		} catch (SAXException e) {
			throw new ConnectorException("SAX Exception parsing Stylesheet "+e);
		}
		catch (IOException e) {
			throw new ConnectorException("IOException parsing Stylesheet "+e);
		}

	}


	/**
	 * Support all of the features for all supports object types.
	 */
	@Override
	public List<Feature> getSupportedFeatures(String objectType) {
		log.debug("SOAPConnector.getSupportedFeatures("+objectType+"): ");
		List<Feature> features = super.getSupportedFeatures(objectType);

		// Check if the SOAP App supports create
		if (config.getBoolean("supportsCreate")) {
			features.add(Connector.Feature.CREATE);
		}
		// Check if the SOAP App supports delete
		if (config.getBoolean("supportsDelete")) {
			features.add(Connector.Feature.DELETE);
		}
		//        features.add(Connector.Feature.DISCOVER_SCHEMA);
		// features.add(Connector.Feature.ENABLE);
		return Arrays.asList(Feature.values());
	}

	@Override
	public Iterator<Map<String,Object>> iterate(Filter filter) {

		// Return the iterator on a copy of the list to avoid concurrent mod
		// exceptions if entries are added/removed while iterating.
		//  Iterator<Map<String, Object>> it = null;
		try {
			_filter = filter;
			if(log.isDebugEnabled())
				log.debug("Before getObjectsMap().values()");
			it = new ArrayList<Map<String,Object>>(getObjectsMap().values()).iterator();
			if(log.isDebugEnabled())
				log.debug("After getObjectsMap().values()");
		} catch (Exception e) {
			if(log.isDebugEnabled())
				log.debug("Exception occured ", e);
		}      
		// Note: FilteredIterator should not be used for most connectors.
		// Instead, the filter should be converted to something that can be
		// used to filter results natively (eg - an LDAP search filter, etc...)
		// Wrap this in a PagingIterator so the cache won't get corrupted.
		// return new PagingIterator(new FilteredIterator(it, filter));
		return it;
	}

	/**
	 * Return the Map that has the objects for the currently configured object
	 * type.  This maps native identifier to the resource object with that
	 * identifier.
	 * @throws Exception 
	 */
	private Map<String,Map<String,Object>> getObjectsMap()
			throws Exception {   	

		log.debug("SOAPConnector.getObjectsMap: ");
		Filter filter = _filter;

		StringBuilder bldr=new StringBuilder();
		bldr.append("<aggregate>");
		if(filter!=null) {
			bldr.append(filterToXml(filter));
		}
		bldr.append("</aggregate>");

		String ret=sendSOAP(bldr.toString());

		// expect back
		// <results>
		// <object id="userid">
		//   <attribute name="name">value</attribute>
		// </object>
		// </results>

		Map<String,Map<String,Object>> results=SOAPConnectorXMLHandler.parseResultString(ret);

		return results;

	}

	private String filterToXml(Filter fltr) {

		StringBuilder bldr=new StringBuilder();
		bldr.append("<filter ");
		List<Filter> subfilters=fltr.getFilters();
		if(subfilters!=null) {
			bldr.append("conjunct=\""+((fltr.getConjunct()==Filter.Conjunct.AND)?"AND":"OR"));
			bldr.append("\">\n");
			for(Filter sub:subfilters) {
				bldr.append(filterToXml(sub));
			}
		} else {
			bldr.append("property=\"");
			bldr.append(fltr.getProperty());
			bldr.append("\" ");
			bldr.append("operation=");
			switch(fltr.getOp()) {
			case EQ: bldr.append("\"EQ\""); break;
			case GT: bldr.append("\"GT\""); break;
			case GE: bldr.append("\"GE\""); break;
			case LT: bldr.append("\"LT\""); break;
			case LE: bldr.append("\"LE\""); break;
			case STARTS_WITH: bldr.append("\"STARTS_WITH\""); break;
			case ENDS_WITH: bldr.append("\"ENDS_WITH\""); break;
			case SUBSTRING: bldr.append("\"SUBSTRING\""); break;
			case NULL: bldr.append("\"\""); break;
			}
			bldr.append(" value=\"");
			bldr.append(fltr.getValue().toString());
			bldr.append("\">\n");

		}
		bldr.append("</filter>\n");
		return bldr.toString();
	}

	@Override
	public Map<String, Object> read(String arg0) throws ConnectorException,
	ObjectNotFoundException, UnsupportedOperationException {
		// Send the read message through the soap channel
		// <read id="xyz123"/>
		// the return should be
		//
		// <object id="xyz123"/>
		//   <attribute name="aaaa">singleValue</attribute>
		//   <attribute name="bbbb">
		//     <value>multivalue1</value>
		//     <value>multivalue2</value>
		//   </attribute>
		// </object>
		return null;
	}



	@Override
	public Result create(String id, List<Item> items)
			throws ConnectorException, ObjectAlreadyExistsException,
			UnsupportedOperationException {
		log.debug("SOAPConnector.create: ");
		if (!config.getBoolean("supportsCreate")) {
			log.info("SOAPConnector.create: Unsupported!");
			throw new UnsupportedOperationException();
		}
		// Send the create message through the soap channel
		// <create id="xyz123">
		//   <attribute name="xxx">yyy</attribute>
		//   <attribtue name="111">
		//     <value>xxx</value>
		//     <value>222</value>
		//   </attribute>
		// </create>
		// return should be
		// <ok/>
		// <fail/>
		StringBuilder bldr=new StringBuilder();
		bldr.append("<create id=\""+id+"\">");
		for(Item item: items) {
			bldr.append("<attribute name=\""+item.getName()+"\">");
			Object value=item.getValue();
			if(value instanceof List) {
				List lst=(List)value;
				for(Object obj:lst) {
					bldr.append("<value>"+obj.toString()+"</value>");
				}
			} else {
				bldr.append(value.toString());
			}
			bldr.append("</attribute>");
		}
		bldr.append("</create>");

		String ret=sendSOAP(bldr.toString());

		log.trace("SOAPConnector.create: ret="+ret);

		Result result = getResult(ret);
		if(result.getStatus()==Result.Status.Committed) {
			HashMap<String,Object> obj=new HashMap<String,Object>();
			obj.put("id", id);
			for(Item item: items) {
				obj.put(item.getName(), item.getValue());
			}
			result.setObject(obj);
		}
		return result;
	}

	@Override
	public Result delete(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {

		log.debug("SOAPConnector.delete ");
		if (!config.getBoolean("supportsDelete")) {
			log.info("SOAPConnector.delete: Unsupported!");
			throw new UnsupportedOperationException();
		}
		// Send the delete message through the soap channel
		// <delete id="xyz123">
		// return should be
		// <ok/>
		// <fail/>	
		StringBuilder bldr=new StringBuilder();
		bldr.append("<delete id=\""+id+"\">");
		if(options!=null) {
  		for(Entry<String, Object> e:options.entrySet()) {
  			bldr.append("<attribute name=\""+e.getKey()+"\">"+e.getValue().toString()+"</attribute>");
  		}
		}
		bldr.append("</delete>");
		String ret=sendSOAP(bldr.toString());

		log.debug("SOAPConnector.delete: ret="+ret);

		Result result = getResult(ret);
		return result;
	}

	private Result getResult(String xml) {

	  Result result = new Result(Result.Status.Failed);
	  List<String> messages=new ArrayList<String>();

	  try {
			Document doc=buildDocument(xml);
			String docEl=doc.getDocumentElement().getTagName();
			log.debug("SOAPConnector.getResult: docEl="+docEl);
			if ("ok".equals(docEl)) {
				result.setStatus(Result.Status.Committed);
			} else {
			  messages.add("Failed");
				
				// TODO: add failur message
				// TODO: Handle queued
				// TODO: handle retry
			}
		} catch (GeneralException ioe) {
			log.error("SOAPConnector.getResult: GeneralException "+ioe);
			messages.add("GeneralException "+ioe);
		}
	  result.setMessages(messages);
	  return result;

	}


	@Override
	public Result enable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return super.enable(id, options);
	}

	@Override
	public Result disable(String id, Map<String, Object> options)
			throws ConnectorException, ObjectNotFoundException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return super.disable(id, options);
	}

	private String sendSOAP(String xml) throws ConnectorException, ObjectNotFoundException {

		log.debug("SOAPConnector.sendSOAP: \n"+xml);

		endPoint = config.getString("endpoint");
		stylesheet = config.getString("stylesheet");

		// Take the input XML
		// Create a DOM
		try {
			Document srcDoc = buildDocument(xml);
			// Run it through the Stylesheet
			String xmldata = transformDocument(srcDoc);

			log.debug("SOAPConnector.sendSOAP: output is:\n"+xmldata);

			// Send it to the endpoint
			log.debug("SOAPConnector.sendSOAP: posting to "+endPoint);
			URLConnection conn=null;
			URL url=new URL(endPoint);
			if(endPoint!=null && endPoint.startsWith("https://")) {
				// Set up an all-trusting trust manager
				// and host name verifier
				try {
					SSLContext ctx= SSLContext.getInstance("SSL");
					ctx.init(null, new TrustManager[] { new LocalSSLTrustManager() }, null);
					HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
					// Create all-trusting host name verifier
					HostnameVerifier allHostsValid = new HostnameVerifier() {
						public boolean verify(String hostname, SSLSession session) {
							return true;
						}
					};

					// Install the all-trusting host verifier
					HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException("Unable to initialise SSL context", e);
				} catch (KeyManagementException e) {
					throw new RuntimeException("Unable to initialise SSL context", e);
				}
			}
			conn=url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestProperty("Content-Type", "text/xml;charset=\"UTF-8\"");

			BufferedWriter  wr = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),"UTF-8"));
			// You can use "UTF8" for compatibility with the Microsoft virtual machine.
			//				log.debug("SOAPConnector.sendSOAP: sending header..");
			//				wr.write("POST " + url.getPath() + " HTTP/1.0\r\n");
			//				wr.write("Host: "+url.getHost()+" \r\n");
			//				wr.write("Content-Length: " + xmldata.length() + "\r\n");
			//				wr.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
			//				wr.write("\r\n");
			log.debug("SOAPConnector.sendSOAP: sending data..");
			//Send data
			wr.write(xmldata);
			wr.flush();
			log.trace("SOAPConnector.sendSOAP: flushed");

			StringBuilder response=new StringBuilder();

			// Response
			try {
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				log.trace("SOAPConnector.sendSOAP: got reader");
				log.trace("SOAPConnector.sendSOAP: --Response-----------------------------");
				String line;
				while((line = rd.readLine()) != null) {
					log.trace(line);
					response.append(line);
				}
				log.trace("SOAPConnector.sendSOAP: --Returning----------------------------");
				// Run the return value through the stylesheet
				Document doc=buildDocument(response.toString());
				String translated=transformDocument(doc);
				log.trace(translated);
				log.trace("SOAPConnector.sendSOAP: ---------------------------------------");
				// return a Result
				return translated;

			} catch (IOException ioe) {
				// WEB-specific IOException
				// Handle HTTP Error code here..
				log.error("SOAPConnector.sendSOAP: IOException getting SOAP Response"+ ioe);
				log.error("IOException getting SOAP Response", ioe);
				throw new ConnectorException(ioe);
			}
		} catch (ParserConfigurationException e) {
			log.error("SOAPConnector.sendSOAP: PCEException sending SOAP message"+ e);
			log.error("ParserConfigurationException sending SOAP message", e);
			throw new ConnectorException(e);
		} catch (GeneralException e) {
			log.error("SOAPConnector.sendSOAP: GeneralException sending SOAP message"+ e);
			log.error("GeneralException sending SOAP message", e);
			throw new ConnectorException(e);
		} catch (SAXException e) {
			log.error("SOAPConnector.sendSOAP: SAXException sending SOAP message"+ e);
			log.error("SAXException sending SOAP message", e);
			throw new ConnectorException(e);
		} catch (IOException e) {
			log.error("SOAPConnector.sendSOAP: IOException sending SOAP message"+ e);
			log.error("IOException sending SOAP message", e);
			throw new ConnectorException(e);
		} catch (TransformerConfigurationException e) {
			log.error("SOAPConnector.sendSOAP: TransformerConfigurationException sending SOAP message"+ e);
			log.error("TransformerConfigurationException sending SOAP message", e);
			throw new ConnectorException(e);
		} catch (TransformerException e) {
			log.error("SOAPConnector.sendSOAP: TransformerException sending SOAP message"+ e);
			log.error("TransformerException sending SOAP message", e);
			throw new ConnectorException(e);
		}
	}

	public String transformDocument(Document srcDoc) throws ParserConfigurationException,
	SAXException, IOException, TransformerConfigurationException,
	TransformerException, GeneralException {
		Templates templates;

		TransformerFactory tfactory = TransformerFactory.newInstance();
		if (!tfactory.getFeature(DOMSource.FEATURE)) {
			log.info("DOM node processing not supported!");
			throw new ConnectorException("DOM node processing not supported!");
		}


		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		log.debug("Using DocumentBuilderFactory " + dfactory.getClass());

		dfactory.setNamespaceAware(true);

		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
		log.debug("Using DocumentBuilder " + docBuilder.getClass());

		Node doc = null;

		DOMSource dsource = null;
		
		doc=docBuilder.parse(getFileStream(stylesheet));
		dsource = new DOMSource(doc);
		
		// If we don't do this, the transformer won't know how to
		// resolve relative URLs in the stylesheet.
		dsource.setSystemId("");
		
		log.debug("SOAPConnector.transformDocument: ---------------------- SrcDoc");
    	OutputFormat format = new OutputFormat(srcDoc);
    	if(log.isTraceEnabled()) {
    		ByteArrayOutputStream baos=new ByteArrayOutputStream(); 
        	try {
	            format.setIndenting(true);
	            XMLSerializer serializer = new XMLSerializer(baos, format);
            	serializer.serialize(srcDoc);
            	String trace=baos.toString();
            	log.trace(trace);
            } catch (IOException ioe){
            }
    	}

    	log.trace("SOAPConnector.transformDocument: ---------------------- Stylesheet");
    	
    	format = new OutputFormat((Document)doc);
    	if(log.isTraceEnabled()) {
    		ByteArrayOutputStream baos=new ByteArrayOutputStream(); 
        	try {
	            format.setIndenting(true);
	            XMLSerializer serializer = new XMLSerializer(baos, format);
            	serializer.serialize(doc);
            	String trace=baos.toString();
            	log.trace(trace);
            } catch (IOException ioe){
            }
    	}		
		
		log.debug("Stylesheet document built OK");

		templates = tfactory.newTemplates(dsource);

		Transformer transformer = templates.newTransformer();

		log.debug("Source document built OK");

		DOMSource ds = new DOMSource(srcDoc.getDocumentElement());
		//ds.setSystemId(new File(sourceID).toURI().toString());

		// create a skeleton output document, to which
		// the transformation results will be added

		Document out = docBuilder.newDocument();
		//	            Element extra = out.createElement("extra");
		//	            out.appendChild(extra);


		ByteArrayOutputStream baos=new ByteArrayOutputStream();

		//transformer.transform(ds, new DOMResult(out));
		transformer.transform(ds, new StreamResult(baos));
		log.debug("Transformation done OK");

		String xmldata=baos.toString();
		return xmldata;
	}

	public Document buildDocument(String xml) throws GeneralException {
		try {
			DocumentBuilder db=getDocumentBuilder();
			InputStream is=new ByteArrayInputStream(xml.getBytes());
			Document srcDoc = db.parse(is);
			return srcDoc;
		} catch (SAXException e) {
			log.error("SOAPConnector.buildDocument: SAXException "+e);
			throw new GeneralException(e);
		} catch (IOException e) {
			log.error("SOAPConnector.buildDocument: IOException "+e);
			throw new GeneralException(e);
		}
	}

	public ProvisioningResult provision(ProvisioningPlan plan)
			throws ConnectorException, ObjectAlreadyExistsException, 
			ObjectNotFoundException, UnsupportedOperationException {


		ProvisioningResult result = new ProvisioningResult();
		Connection connection = null; 

		if (plan != null)
		{

			// Each Plan has one or more requesters
			// put Attributes on each AccountRequest to show the native Identity of the requester(0)
			// TODO: loop through all requesters.
			// TODO: possibly loop through all requesters and set value in Attributes for all id/app combos


			List<AccountRequest> accReqs = plan.getAccountRequests();
			List<AccountRequest> newAccReqs = new ArrayList<AccountRequest>();
			for (AccountRequest req: accReqs) {
				Identity identity = plan.getRequesters().get(0);
				if (identity==null) continue;

				String name = identity.getName();

				try {
					SailPointContext context=SailPointFactory.getCurrentContext();

					Application appl = context.getObjectByName(Application.class, req.getApplication());
					if (appl==null) continue;

					// Get the identity from this context
					Identity id2=context.getObjectById(Identity.class, identity.getId());
					//	        		context.attach(identity);

					Link lnk = id2.getLink(appl);
					if (lnk==null) continue;
					log.debug("SOAPOut.provision: Native ID for "+req.getApplication()+" = "+lnk.getNativeIdentity());
					req.put("requesterNativeID", lnk.getNativeIdentity());
				} catch (GeneralException ge) {
					log.error("SOAPOut.provision: GeneralException "+ge);
				}
				newAccReqs.add(req);
			}
			plan.setAccountRequests(newAccReqs);
			try {
				String response=sendSOAP(plan.toXml());
				ProvisioningResult r=new ProvisioningResult();
				r.setStatus(ProvisioningResult.STATUS_COMMITTED);
				return r;
			} catch (GeneralException e) {
				log.error("plan.toXml() throws "+e);
				throw new ConnectorException("can't provision plan");
			}

		}

		return null;
	}

	private void throwUnsupported(String method) {
		throw new UnsupportedOperationException(method + " not supported by " + getClass());
	}

	private DocumentBuilder getDocumentBuilder() throws GeneralException{
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
						log.trace("Ignoring " + publicId + ", " + systemId);
						return new InputSource(new StringReader(""));
					}
				});
			} catch (ParserConfigurationException e) {
				log.error("SOAPConnector.buildDocument: PCE "+e);
				throw new GeneralException(e);

			}
		}
		return _dBuilder;

	}
	
	   /**
	    * Get the input File Stream.
	    */
	   private InputStream getFileStream(String fileName) throws GeneralException {
	      InputStream stream = null;

	      if (fileName == null) {
	         throw new GeneralException("Filename cannot be null.");
	      }
	      try {
	         File file = new File(fileName);
	         if (!file.exists()) {
	            // sniff the file see if its relative if it is
	            // see if we can append sphome to find it
	            if (!file.isAbsolute()) {
	               String appHome = getAppHome();
	               if (appHome != null) {
	                  file = new File(appHome + File.separator + fileName);
	                  if (!file.exists()) {
	                     file = new File(fileName);
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
