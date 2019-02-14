package sailpoint.seri.integration;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.api.Workflower;
import sailpoint.integration.AbstractIntegrationExecutor;
import sailpoint.integration.JsonUtil;
import sailpoint.integration.ProvisioningPlan;
import sailpoint.integration.RequestResult;
import sailpoint.integration.RoleDefinition;
import sailpoint.object.AuditEvent;
import sailpoint.object.Bundle;
import sailpoint.object.Configuration;
import sailpoint.object.Identity;
import sailpoint.object.IntegrationConfig;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.PermissionRequest;
import sailpoint.object.ProvisioningProject;
import sailpoint.object.ProvisioningResult;
import sailpoint.object.Rule;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowLaunch;
import sailpoint.server.Auditor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;


public class RuleBasedExecutor extends AbstractIntegrationExecutor 
{
	private static Log log = LogFactory.getLog(RuleBasedExecutor.class);

	public static final String ARG_OPTIMISTIC_PROVISIONING = "optimisticProvisioning";
	public static final String ARG_MANUAL_WORK_ITEM = "manualWorkItem";
	public static final String ARG_EXECUTION_TYPE= "executionType";
	public static final String ARG_PAYLOAD_RULE= "payloadRule";



	//////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	//////////////////////////////////////////////////////////////////////

	/**
	 * Argument that may be left in the AccountRequest or AttributeRequest
	 * to control which ProvisioningResult status to return.    
	 */
	public static final String ARG_RESULT_STATUS = "resultStatus";

	/**
	 * Argument that may be left in the AccountRequest or AttributeRequest
	 * to control the request id to return in the ProvisioningResult.
	 */
	public static final String ARG_RESULT_REQUEST_ID = "resultRequestId";

	/**
	 * Argument that may be left in the AccountRequest that
	 * is the ResourceObject to return in the ProvisioningResult.
	 */
	public static final String ARG_RESULT_OBJECT = "resultObject";

	//////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	//////////////////////////////////////////////////////////////////////

	/**
	 * "database" of roles that are being managed.
	 */
	static List<RoleDefinition> _roles = null;

	/**
	 * History of provisioning requests that were processed.
	 */
	static List<ProvisioningPlan> _plans = null;

	String _integrationName;
	String _targetDisplayName;
	String _configObjectName;
	String _executionType;
	String _payloadRule;
	Boolean _isOptimisticProv;
	Boolean _isManualWorkItem;

	//////////////////////////////////////////////////////////////////////
	//
	// Constructor / Properties
	//
	//////////////////////////////////////////////////////////////////////

	public RuleBasedExecutor() {
	}

	static public void reset() {
		_roles = null;
		_plans = null;
	}

	static public List<RoleDefinition> getRoles() {
		return _roles;
	}

	static public List<ProvisioningPlan> getProvisions() {
		return _plans;
	}


	//////////////////////////////////////////////////////////////////////
	//
	// IntegrationInterface
	//
	//////////////////////////////////////////////////////////////////////

	public void configure(SailPointContext context, IntegrationConfig config)
			throws Exception 
	{
		_integrationName = config.getName();

		_isOptimisticProv = Util.otob(config.getString(ARG_OPTIMISTIC_PROVISIONING));
		log.debug("_isOptimisticProv:" + _isOptimisticProv);

		_isManualWorkItem = Util.otob(config.getString(ARG_MANUAL_WORK_ITEM));
		log.debug("_isManualWorkItem:" + _isManualWorkItem);

		_executionType = Util.otos(config.getString(ARG_EXECUTION_TYPE));
		log.debug("_executionType:" + _executionType);

		_payloadRule = Util.otos(config.getString(ARG_PAYLOAD_RULE));
		log.debug("_payloadRule:" + _payloadRule);


		// save the args for later calls to provision()
		super.configure(context, config);

	}

	public String ping() throws Exception {

		log.info("HelpDeskExecutor: ping");

		return "Good morning starshine, the earth says hello!";
	}

	/**
	 * Return the current list of "manageable" roles.
	 */
	public List<String> listRoles() throws Exception {

		List<String> names = new ArrayList<String>();
		if (_roles != null) {
			for (RoleDefinition role : _roles)
				names.add(role.getName());
		}

		log.debug("HelpDeskExecutor: listRoles");
		log.debug(names);

		return names;
	}

	/**
	 * Create or update the definition of a role.
	 */
	public RequestResult addRole(RoleDefinition def) throws Exception {

		RequestResult result = new RequestResult();
		result.addError("Unsupported action: addRole('"+def.getName()+"')");

		//        RequestResult result = null;
		//
		//        log.debug("HelpDeskExecutor: addRole");
		//        log.debug(jsonify(def));
		//
		//        String name = def.getName();
		//
		//        if (name == null) {
		//            result = new RequestResult();
		//            result.addError("Missing role name");
		//        }
		//        else {
		//            if (_roles == null)
		//                _roles = new ArrayList<RoleDefinition>();
		//
		//            RoleDefinition existing = findRole(name);
		//            if (existing != null)
		//                _roles.remove(existing);
		//            _roles.add(def);
		//        }

		return result;
	}

	private RoleDefinition findRole(String name) {
		RoleDefinition found = null;
		if (name != null && _roles != null) {
			for (RoleDefinition role : _roles) {
				if (name.equals(role.getName())) {
					found = role;
					break;
				}
			}
		}
		return found;
	}

	/**
	 * Delete a role.
	 */
	public RequestResult deleteRole(String name) throws Exception {

		RequestResult result = new RequestResult();
		result.addError("Unsupported action: deleteRole('"+name+"')");

		//        RequestResult result = null;
		//
		//        log.debug("HelpDeskExecutor: deleteRole " + name);
		//
		//        if (name == null) {
		//            result = new RequestResult();
		//            result.addError("Missing role name");
		//        }
		//        else if (_roles == null || !_roles.contains(name)) {
		//            result = new RequestResult();
		//            result.addError("Role does not exist");
		//        }
		//        else {
		//            _roles.remove(name);
		//        }

		return result;
	}





	/**
	 * ----------------------------------------------------------
	 * Make changes to an identity defined by a ProvisioningPlan.
	 * ----------------------------------------------------------
	 *
	 *    This is the method which is invoked when it's time to
	 *    provision.  This is where for ACE Insurance, we need to
	 *    insert logic and handling to send an email message, which
	 *    is used to simulate the opening of a Help Desk ticket.
	 * ----------------------------------------------------------
	 */
	public ProvisioningResult provision(sailpoint.object.ProvisioningPlan plan)
			throws Exception {

		ProvisioningResult result = new ProvisioningResult();

		Identity identityObj = plan.getIdentity();
		if (identityObj == null) {
			result.addError("Identity object is missing from plan");
		}

		ProvisioningPlan varIntegrationPlan = null; 


		String identity = identityObj.getName();
		if (identity == null) {
			result.addError("Missing identity");
			result.setStatus(ProvisioningResult.STATUS_FAILED);
		}
		else {
			if (_plans == null)
				_plans = new ArrayList<ProvisioningPlan>();
			//convert from a object.ProvisioningPlan to a integration.ProvisioningPlan
			//this executor was originally coded as integration.ProvisioningPlan
			varIntegrationPlan = new ProvisioningPlan(plan.toMap());

			_plans.add(varIntegrationPlan);


			switch (_executionType.toLowerCase()) {
			case "email":
				// Email to the help desk
				result=emailify(identity,varIntegrationPlan, result);
				break;
			case "rest":
				// Send to a rule that will send via REST
				result=restify(identity,varIntegrationPlan, result);
				break;
			case "soap":
				// Send to a rule that will send via REST
				result=soapify(identity,varIntegrationPlan, result);
				break;
			case "rule":
				return runPayloadRule(null, identity, varIntegrationPlan, result);
			default:
				result.addError("Unrecognised execution type '"+_executionType+"'");
				result.setStatus(ProvisioningResult.STATUS_FAILED);
			}

			if (_isManualWorkItem)
				doManualActions(plan);

		}
		auditFulfillmentRequest(varIntegrationPlan, result.getRequestID());

		plan.setResult(result);



		//set the status on the plan. If optimistic provisioning set to commmitted.
		//This will cause the cube links to be updated

		if (_isOptimisticProv) {
			log.debug("Optimistic Provision");
			result.setStatus(ProvisioningResult.STATUS_COMMITTED);
		} else {
			// Shouldn't assume this goes to queued - what if there was an error, or a RETRY?
			// result.setStatus(ProvisioningResult.STATUS_QUEUED);
		} 

		// override status if requested
		List<AccountRequest> accounts = plan.getAccountRequests();
		if (accounts != null) {
			for (AccountRequest account : accounts) {

				//Change to eliminate need to insert the following in PlanInitializerScript for the connector
				if (_isOptimisticProv) {
					account.put(ARG_RESULT_STATUS, ProvisioningResult.STATUS_COMMITTED);
				} else {
					account.put(ARG_RESULT_STATUS, result.getStatus());
				}

				List<AttributeRequest> atts = account.getAttributeRequests();
				if (atts != null) {
					for (AttributeRequest att : atts) {
						log.trace("Attribute Request " + att.toXml());
						ProvisioningResult oldResult=result;
						result = new ProvisioningResult();
						if (_isOptimisticProv) {
							result.setStatus(ProvisioningResult.STATUS_COMMITTED);
						} else {
							result.setStatus(oldResult.getStatus());
						}
						att.setResult(result);

						// also allow this, status better match!
						result.setRequestID(att.getString(ARG_RESULT_REQUEST_ID));
					}
				}

				List<PermissionRequest> perms = account.getPermissionRequests();
				if (perms != null) {
					for (PermissionRequest perm : perms) {
						log.trace("Permission Request: " + perm.toXml());

						ProvisioningResult oldResult=result;
						result = new ProvisioningResult();
						if (_isOptimisticProv) {
							result.setStatus(ProvisioningResult.STATUS_COMMITTED);
						} else {
							result.setStatus(oldResult.getStatus());
						}
						result.setRequestID(perm.getString(ARG_RESULT_REQUEST_ID));
						perm.setResult(result);

					}
				}

			}
		}


		// use the new convention of all results in the plan
		return null;

	}


	private Boolean doManualActions(sailpoint.object.ProvisioningPlan plan){

		HashMap launchArgsMap = new HashMap();
		log.debug("doManualActions");
		Identity identity = plan.getIdentity();
		if (identity == null) {
			log.error("plan identity object is null");
			return false;
		}

		String identityName = identity.getName();
		ProvisioningProject project = new ProvisioningProject();
		project.add(plan);

		launchArgsMap.put("identityName", identityName);
		launchArgsMap.put("project",      project);
		launchArgsMap.put("plan",         plan);


		WorkflowLaunch wflaunch = new WorkflowLaunch();

		try {
			SailPointContext context = SailPointFactory.getCurrentContext();

			Workflow wf = (Workflow) context.getObjectByName(Workflow.class,"Do Manual Actions Help Desk Executor");
			if (wf == null)
				log.debug("Do Manual Actions workflow is null");
			else
				log.debug("Do manual actions workflow found");

			String wfCaseName = "Manual Actions " + identityName;
			wflaunch.setWorkflowName(wf.getName());
			wflaunch.setWorkflowRef(wf.getName());
			wflaunch.setCaseName("Do Manual Actions " + identityName );
			log.debug("Do Manual Actions args:" + launchArgsMap);
			wflaunch.setVariables(launchArgsMap);
			log.debug("Workflow Launch" + wflaunch.toXml());


			//Create Workflower and launch workflow from WorkflowLaunch
			Workflower workflower = new Workflower(context);
			//WorkflowLaunch launch = workflower.launchSafely ( wf,wfCaseName,launchArgsMap );
			WorkflowLaunch launch = workflower.launch( wf,wfCaseName,launchArgsMap );

			// WorkflowLaunch launch = workflower.launch(wflaunch);

			// print workflowcase ID (example only; might not want to do this in the task)
			String workFlowId = launch.getWorkflowCase().getId();
			log.debug("Manual Actions Workflow case id:" + workFlowId);

		} catch (GeneralException ge) {
			log.error("General Exception calling Do Manual Work Items");
			log.error(ge);
			return false;
		}

		return true;

	}
	private void
	auditFulfillmentRequest(ProvisioningPlan plan, String requestId)
	{
		String source;
		String requesterDisplayName = null;

		Map args = plan.getArguments();
		if (null != args) {
			source = (String) args.get("source");
			String requester = (String) args.get("requester");
			if (null != requester) {
				try {
					Identity cube = getContext().getObjectByName(Identity.class, requester);
					if (cube != null){
						requesterDisplayName = cube.getName();
					} else {
						requesterDisplayName = "IIQ Scheduler";
					}             
				} catch (GeneralException ge) {
					log.error("Exception getting requester's cube : " + ge.toString());
				}
			}
		} else {
			// Role model reconciliation; set source to LCM 
			source = "LCM";
		}

		// Put the ticket # on the cube as an identity event
		//
		Auditor.logAs(source + (requesterDisplayName != null ? ":" + requesterDisplayName : "") , 
				AuditEvent.ActionIdentityTriggerEvent, plan.getIdentity(), 
				"Provisioning Request for " + _targetDisplayName, "Fulfillment process initiated in " + _integrationName, " : Ticket #" + requestId, "Approved LCM request");

		return;     

	}

	/**
	 * ----------------------------------------------------------
	 * Send an Email with the details of the ProvisioningPlan
	 * ----------------------------------------------------------
	 *
	 *    This is the method which is invoked to send the email.
	 *    In the POC, we will send an email, in production we
	 *    will open a Help Desk ticket (ref. IIQ v3.2)
	 * ----------------------------------------------------------
	 */
	public ProvisioningResult emailify(String identity, ProvisioningPlan plan, ProvisioningResult result) throws Exception {

		Map<String,Object> args=new HashMap<String,Object>();

		Configuration helpdeskConfig = null;
		try {
			helpdeskConfig = (SailPointFactory.getCurrentContext().getObject(Configuration.class, _configObjectName) );
			args.put("configObject", helpdeskConfig);
		} catch (GeneralException ex) {
			result.addError("Problem Getting System Configuration...");
			result.setStatus(ProvisioningResult.STATUS_RETRY); // retry cos we could fix this before next try
			return result;
		}

		return runPayloadRule(args, identity, plan, result);
	}


	/**
	 * ----------------------------------------------------------
	 * Send a REST payload with the details of the ProvisioningPlan
	 * ----------------------------------------------------------
	 * We will execute a rule which will make a REST call to send a payload
	 * ----------------------------------------------------------
	 */
	public ProvisioningResult restify(String identity, ProvisioningPlan plan, ProvisioningResult result) throws Exception {      
		return http(identity, plan, result);
	}

	/**
	 * ----------------------------------------------------------
	 * Send a SOAP payload with the details of the ProvisioningPlan
	 * ----------------------------------------------------------
	 * We will execute a rule which will make a REST call to send a payload
	 * ----------------------------------------------------------
	 */
	public ProvisioningResult soapify(String identity, ProvisioningPlan plan, ProvisioningResult result) throws Exception {
		return http(identity, plan, result);      
	}

	private ProvisioningResult http(String identity, ProvisioningPlan plan,
			ProvisioningResult result) throws Exception {
		Map<String,Object> args=new HashMap<String,Object>();
		String getUrl=_config.getString("url");
		String sUsername=_config.getString("username");
		String sPassword=_config.getString("password");
		getUrl=_config.getString("url");

		URI hostUri = new URI(getUrl);
    CloseableHttpClient client=getPreEmptiveClient(hostUri, sUsername, sPassword);
		args.put("client", client);
		args.put("uri", hostUri);
		// Rule should use:
		//      HttpGet get=new HttpGet(getUrl); (or HttpPost)
		//      CloseableHttpResponse response=client.execute(get);
		return runPayloadRule(args, identity, plan, result);
	}

	private CloseableHttpClient getPreEmptiveClient(URI hostUri, String sUsername, String sPassword) {

		HttpHost target=new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());    

		CredentialsProvider credsProvider = null;

		if (sUsername!=null && sPassword!=null) {
			credsProvider=new BasicCredentialsProvider();
			credsProvider.setCredentials(
					new AuthScope(target.getHostName(), target.getPort()),
					new UsernamePasswordCredentials(sUsername, sPassword));
		}

		int timeout=3000; // default to 3 seconds
		try {
			timeout=Util.otoi(_config.getString("timeout"));
		} catch (Exception e) {
			// some error calculating timeout; use default
		}

		//    X509HostnameVerifier hostnameVerifier = new AllowAllHostnameVerifier();
		RequestConfig config = RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.build();

		HttpClientBuilder bldr = HttpClients.custom()
				.setDefaultRequestConfig(config);
		//      .setHostnameVerifier(hostnameVerifier)
		if (credsProvider!=null) {
			bldr.setDefaultCredentialsProvider(credsProvider);
		}

		if(hostUri.getScheme().equals("https")) {
			SSLContextBuilder builder = new SSLContextBuilder();
			try {
				builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
						builder.build());
				bldr.setSSLSocketFactory(sslsf);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CloseableHttpClient httpclient = bldr.build();

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local
		// auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(target, basicAuth);

		// Add AuthCache to the execution context
		HttpClientContext localContext = HttpClientContext.create();
		localContext.setAuthCache(authCache);

		return httpclient;
	}

	public ProvisioningResult runPayloadRule(Map<String,Object> args, String identity, ProvisioningPlan plan, ProvisioningResult result) throws Exception {

		if(args==null) args=new HashMap<String,Object>();

		SailPointContext context=SailPointFactory.getCurrentContext();

		args.put( "identityName", identity );
		args.put( "plan", plan );
		args.put( "result", result);

		Rule rule=context.getObjectByName(Rule.class, _payloadRule);
		if(rule==null) {
			result.addError("Couldn't find payload Rule '"+_payloadRule+"'");
			result.setStatus(ProvisioningResult.STATUS_RETRY);
			return null;
		}
		
		log.debug("Executing payload rule: "+_payloadRule);
		Object obj=context.runRule(rule, args);

		if(!(obj instanceof ProvisioningResult)) {
			result.addError("payload Rule '"+_payloadRule+"' must return a value of type ProvisioningResult");
			result.setStatus(ProvisioningResult.STATUS_RETRY);
		}

		return (ProvisioningResult)obj;

	}

	public RequestResult getRequestStatus(String requestID) throws Exception {
		throw new UnsupportedOperationException("Dummy requestID passed: " + requestID);
	}


	//////////////////////////////////////////////////////////////////////
	//
	// IntegrationExecutor
	//
	//////////////////////////////////////////////////////////////////////

	public void finishRoleDefinition(IntegrationConfig config,
			Bundle src, 
			RoleDefinition dest)
					throws Exception {

		log.debug("HelpDeskExecutor: finishRoleDefinition " + src.getName());
	}


}