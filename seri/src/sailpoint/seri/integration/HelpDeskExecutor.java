/* (c) Copyright 2008 SailPoint Technologies, Inc., All Rights Reserved. */

/**
 * Implementation of IntegrationExecutor interface used for ACE POC.
 * 
 * Author: Jeff
 *
 * This is similar to TestExecutor but doesn't have the simulated errors.
 * The purpose is to print stuff to the console so you can try things in
 * the UI and see that the integration is being called.
 *
 * The file test/integration.xml has an IntegrationConfig that uses this
 * class.  Tweak that as necessary to control which roles are sync'd.
 *
 * I'm putting this in the core product rather than in the test
 * branch so we have it accessible for debugging in POCs and deployments.
 *
 * ...and on behalf of the SailPoint SE's, let me say to Jeff:
 *                T H A N K    Y O U   ! ! !
 */

package sailpoint.seri.integration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List; 
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.integration.AbstractIntegrationExecutor;
import sailpoint.integration.JsonUtil;
import sailpoint.integration.ProvisioningPlan;
import sailpoint.integration.RequestResult;
import sailpoint.integration.RoleDefinition;
import sailpoint.object.Attributes;
import sailpoint.object.AuditEvent;
import sailpoint.object.Bundle;
import sailpoint.object.Configuration;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.object.Identity;
import sailpoint.object.IntegrationConfig;
import sailpoint.object.ProvisioningProject;
import sailpoint.object.ProvisioningResult;
import sailpoint.object.ResourceObject;
import sailpoint.object.Rule;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.PermissionRequest;
import sailpoint.server.Auditor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import sailpoint.api.Workflower;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowLaunch;


public class HelpDeskExecutor extends AbstractIntegrationExecutor 
{
    private static Log log = LogFactory.getLog(HelpDeskExecutor.class);

    public static final String ARG_CONFIG_OBJECT_NAME = "configObjectName";
    public static final String ARG_PLAN_RULE_NAME     = "planRuleName";
    public static final String ARG_OPTIMISTIC_PROVISIONING = "optimisticProvisioning";
    public static final String ARG_MANUAL_WORK_ITEM = "manualWorkItem";
    public static final String ARG_EMAIL_WORK_ITEM = "emailWorkItem";
    
    
    
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
    String _planRuleName;
    Boolean _isOptimisticProv;
    Boolean _isManualWorkItem;
    Boolean _isEmailWorkItem;

    //////////////////////////////////////////////////////////////////////
    //
    // Constructor / Properties
    //
    //////////////////////////////////////////////////////////////////////

    public HelpDeskExecutor() {
    }

    public String jsonify(Map map) {
        String json = null;
        // this can throw, eat it
        try {
            json = JsonUtil.render(map);
        }
        catch (Throwable t) {
            log.error("HelpDeskExecutor: Unable to jsonify Map!");
        }
        return json;
    }

    public String jsonify(RoleDefinition def) {
        return jsonify(def.toMap());
    }

    public String jsonify(ProvisioningPlan plan) {
        return jsonify(plan.toMap());
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
    	
    	// Sean K - Allowing name of configuration object to be specified in IntegrationConfig.
    	//          Still not bullet-proof, but better than before...
    	//
    	_configObjectName = config.getString(ARG_CONFIG_OBJECT_NAME);
    	if (null == _configObjectName) _configObjectName = "SERI Configuration";
  
    	_planRuleName = config.getString(ARG_PLAN_RULE_NAME);
    	_isOptimisticProv = Util.otob(config.getString(ARG_OPTIMISTIC_PROVISIONING));
    	log.debug("_isOptimisticProv:" + _isOptimisticProv);
    	
    	_isManualWorkItem = Util.otob(config.getString(ARG_MANUAL_WORK_ITEM));
    	log.debug("_isManualWorkItem:" + _isManualWorkItem);

    	_isEmailWorkItem = Util.otob(config.getString(ARG_EMAIL_WORK_ITEM));
    	log.debug("_isEmailWorkItem:" + _isEmailWorkItem);
    	
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

        RequestResult result = null;

        log.debug("HelpDeskExecutor: addRole");
        log.debug(jsonify(def));

        String name = def.getName();

        if (name == null) {
            result = new RequestResult();
            result.addError("Missing role name");
        }
        else {
            if (_roles == null)
                _roles = new ArrayList<RoleDefinition>();

            RoleDefinition existing = findRole(name);
            if (existing != null)
                _roles.remove(existing);
            _roles.add(def);
        }
        
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

        RequestResult result = null;

        log.debug("HelpDeskExecutor: deleteRole " + name);

        if (name == null) {
            result = new RequestResult();
            result.addError("Missing role name");
        }
        else if (_roles == null || !_roles.contains(name)) {
            result = new RequestResult();
            result.addError("Role does not exist");
        }
        else {
            _roles.remove(name);
        }
        
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
        }
        else {
            if (_plans == null)
                _plans = new ArrayList<ProvisioningPlan>();
            
            if (_planRuleName != null && _planRuleName.length() > 0 && !_planRuleName.equalsIgnoreCase("none")) {
	            	
	    		    if (plan != null)
	    		    	log.debug("plan before calling : " + _planRuleName + ": " + plan.toMap());
	    		    else
	       		    	log.debug("plan is null before calling :" + _planRuleName);
	    		    
	    		    Map args = new HashMap();
	    		    args.put("plan",plan);
	    		    SailPointContext context = this.getContext();
	    		    Rule rule = context.getObjectByName(Rule.class, _planRuleName);
	    		    if (rule != null) {
		    		    log.debug("calling Rule:" + _planRuleName);
		    		    plan = (sailpoint.object.ProvisioningPlan) context.runRule(rule, args);
		
		    		    Object obj =  context.runRule(rule, args);
		    		    log.debug("After calling :" + _planRuleName);
		    		    sailpoint.object.ProvisioningPlan retPlan = (sailpoint.object.ProvisioningPlan) obj;
		    		    log.debug("After cast to plan:" + retPlan); 
		    		    if (retPlan != null) {
		    		    	log.debug("retPlan:" + retPlan.toXml());
		    		    	plan = retPlan;
		    		    }
		
		    		    
		    		    if (plan != null)
		    		    	log.debug("plan after calling " + _planRuleName + ": " + plan.toMap());
		    		    else
		       		    	log.debug("plan is null after calling " + _planRuleName);
	    		    }
    		    
    		}

            //convert from a object.ProvisioningPlan to a integration.ProvisioningPlan
            //this executor was originally coded as integration.ProvisioningPlan
            varIntegrationPlan = new ProvisioningPlan(plan.toMap());
           
            _plans.add(varIntegrationPlan);
        }

        // mock up a Request ID for demo purposes
        // --------------------------------------
        String requestId = generateRequestId();
        result.setRequestID(requestId);
        
        // Email to the help desk
        // ---------------------------------------
        if (_isEmailWorkItem)
        	emailify(identity,varIntegrationPlan,requestId);
        
        if (_isManualWorkItem)
        	doManualActions(plan);
        
        result.setStatus(ProvisioningResult.STATUS_QUEUED);
        result.setRequestID(requestId);

        // A little sunshine for the Tomcat console
        // ----------------------------------------
        log.debug(" ");
        log.debug("********************************************************************************");
        log.debug("HelpDeskExecutor: provision  " + identity);
        log.debug("HelpDeskExecutor: Request ID " + requestId);
        log.debug(plan.toXml());
        log.debug("********************************************************************************");
        log.debug(" ");

        auditFulfillmentRequest(varIntegrationPlan, requestId);

        plan.setResult(result);
        

       
        //set the status on the plan. If optimistic provisioning set to commmitted.
        //This will cause the cube links to be updated
        
        if (_isOptimisticProv) {
            log.debug("Optimistic Provision");
        	result.setStatus(ProvisioningResult.STATUS_COMMITTED);
        } else {
        	result.setStatus(ProvisioningResult.STATUS_QUEUED);
        } 

        // override status if requested
        List<AccountRequest> accounts = plan.getAccountRequests();
        if (accounts != null) {
            for (AccountRequest account : accounts) {

            	//Change to eliminate need to insert the following in PlanInitializerScript for the connector
                if (_isOptimisticProv) {
	                account.put(ARG_RESULT_STATUS, ProvisioningResult.STATUS_COMMITTED);
                } else {
	                account.put(ARG_RESULT_STATUS, ProvisioningResult.STATUS_QUEUED);
                }

                List<AttributeRequest> atts = account.getAttributeRequests();
                if (atts != null) {
                    for (AttributeRequest att : atts) {
                        log.trace("Attribute Request " + att.toXml());
                        result = new ProvisioningResult();
                        if (_isOptimisticProv) {
                        	result.setStatus(ProvisioningResult.STATUS_COMMITTED);
                        } else {
                        	result.setStatus(ProvisioningResult.STATUS_QUEUED);
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

                        result = new ProvisioningResult();
                        if (_isOptimisticProv) {
                        	result.setStatus(ProvisioningResult.STATUS_COMMITTED);
                        } else {
                        	result.setStatus(ProvisioningResult.STATUS_QUEUED);
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
     * Generate a Request ID 
     * ----------------------------------------------------------
     *
     *    This for Tracking Purposes of the Remediation request.
     *    It is also done because "the story is in the telling"
     *    and having a Request ID helps to tie the story of a 
     *    simulated Help Desk integration to "real" Service
     *    Request / Ticketing system integration.
     *
     *    This could be improved to include a sequence # in the
     *    Request ID and to include the Request ID in Audit Logs
     *    and any work item that is created.
     * ----------------------------------------------------------
     *
     */
    public String generateRequestId() {

        DateFormat dateFormatter = new SimpleDateFormat("MMddHHmmss");

        Date theTimeIsNow = new Date();

        String requestId  = dateFormatter.format(theTimeIsNow);

        return requestId;
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
    public String emailify(String identity, ProvisioningPlan plan, String requestId) {

        if(null == plan) {
           log.error("Null Provisioning Plan...!!!");
           return "Null ProvisionPlan";
        }

  
     // Mapify, then Parsify the Provisioning Plan 
     // ------------------------------------------ 
        Map map = plan.toMap();

        log.debug("emailify plan:" + plan.toMap());
        List acctList = (List)(map.get("accounts"));

        Map el = (Map) acctList.get(0);                              // HARD-CODED VALUE

        String _application = Util.getString(el,"application");
        String _instance    = Util.getString(el,"instance");
        String _nativeId    = Util.getString(el,"nativeIdentity");

        String _operation   = null;
        Object _object      = el.get("op");
        if (_object != null) {
           _operation = (String)(_object.toString());
        }
//=
//= Probably can remove all 'json' code once ProvisioningPlan is parsed correctly...
//=
        String json = null;
        try {
            json = JsonUtil.render(map);
        }
        catch (Throwable t) {
            log.error("HelpDeskExecutor: Unable to jsonify Map!");
        }
//=

     // Assemblify and Sendify the email
     // --------------------------------
        try {
           SailPointContext context = SailPointFactory.getCurrentContext();
           Map<String,Object> mailargs  = new HashMap<String,Object>();

           Identity id = null;
           String _firstname = null;
           String _lastname = null;
           
           if (null != identity && identity.length() > 0) {
              id = context.getObjectByName(Identity.class,identity);
              _targetDisplayName  = id.getDisplayName();
              _firstname = id.getFirstname();
              _lastname = id.getLastname();
           }

      	   String toAddress    = getHelpDesk_ToAddress();     //"helpdesk@acegroup.com";
           String templateName = getHelpDesk_EmailTemplate(); //"Notify Help Desk";

           mailargs.put("userId"          , identity);
           mailargs.put("nativeIdentity"  , _nativeId);
           mailargs.put("application"     , _application);
           mailargs.put("operation"       , _operation);
           mailargs.put("firstName"       , _firstname);
           mailargs.put("lastName"        , _lastname);
           mailargs.put("ProvisioningPlan", plan);
           mailargs.put("json"            , json);
           mailargs.put("requestId"       , requestId);

           EmailTemplate template = context.getObjectByName(EmailTemplate.class, templateName);
           if (null == template) log.error("HelpDeskExecutor: Email template, " + templateName + (", not found in repo!"));

           log.debug("templateName:" + templateName);
           log.debug("email args:" + mailargs);
           EmailOptions mailopts = new EmailOptions(toAddress, mailargs);

           context.sendEmailNotification(template, mailopts);

        } catch (GeneralException ge) {
           log.error("General Exception with sending email...");
           return "Error";
        }
        return "Success";
    }


 // ----------------------------------------------------------------------------
    public String getHelpDesk_ToAddress() {
       return getHelpDeskConfiguration("helpDesk-ToAddress");
    }
 // ----------------------------------------------------------------------------
    public String getHelpDesk_EmailTemplate() {
       return getHelpDeskConfiguration("helpDesk-EmailTemplate");
    }
 // ----------------------------------------------------------------------------
    public String getHelpDeskConfiguration(String value) {

       Configuration helpdeskConfig = null;
       try {
          helpdeskConfig = (SailPointFactory.getCurrentContext()).getObject(Configuration.class, _configObjectName);
       } catch (GeneralException ex) {
          log.error("Problem Getting System Configuration...");
          if(value.equals("helpDesk-ToAddress"))
             return("helpdesk@example.com");
          else if(value.equals("helpDesk-EmailTemplate"))
             return("Notify Help Desk");
          else
             return("No Config Object");
       }

       if (helpdeskConfig != null) {
          Attributes<String,Object> attrs = helpdeskConfig.getAttributes();
          if (attrs != null) {
             for (Object key : attrs.keySet()) {
                String keyValue = key.toString();
                String attrValue = attrs.getString(keyValue);
                if(keyValue.equals(value)) {
                   return attrValue; 
                }
             }
          }
       }
        return ("No Config Key Match");
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