package sailpoint.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.ManagedAttributer;
import sailpoint.api.ObjectUtil;
import sailpoint.api.SailPointContext;
import sailpoint.object.Application;
import sailpoint.object.ApprovalItem;
import sailpoint.object.ApprovalSet;
import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.QueryOptions;
import sailpoint.object.Rule;
import sailpoint.object.WorkItem;
import sailpoint.object.Workflow;
import sailpoint.object.Workflow.Approval;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import sailpoint.tools.xml.XMLObjectFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IdentityRuleOwnerApprovalGenerator {
	/* (c) Copyright 2008 SailPoint Technologies, Inc., All Rights Reserved. */


	/**
	 * A class encapsulating various maintenance of the IdentityEntitlement
	 * objects added in 6.0.
	 *
	 */


	/**
	 * 
	 * Class that handles the various Approvals that happen 
	 * when we are dealing with approvals for LCM and IIQ
	 * requests for Identities.
	 * 
	 * This logic used to be split out across multiple rules
	 * and rule libraries but was consolidated in 6.2 
	 * to make it easier to manange this logic from a deveopment
	 * perspective.
	 * 
	 * The rules will continue to exist as "code-free" ways
	 * to implement this functionality in rules but those
	 * mechanisms will no longer be maintained and replaced
	 * with this functionality.
	 * 
	 * 1. For LCM Approvals the rule started in the rule 
	 *    'LCM Build Owner Approvals' which is found
	 *    in lcmrules.xml. 
	 *    
	 * 2. For LCM Create and Update approvals were generated
	 *    in the rule 'LCM Build Identity Approvers' which 
	 *    is found in lcmrules.xml.
	 * 
	 * Both rule implementations rely heavily on methods found
	 * in the "Approval Rule Library" which is found in 
	 * workflowRules.xml.
	 * 
	 * @author <a href="mailto:dan.smith@sailpoint.com">Dan Smith</a>
	 * 
	 * @since 6.2
	 * 
	 */

	    
	    private static Log log = LogFactory.getLog(IdentityRuleOwnerApprovalGenerator.class);
	 
	    public static final String APPROVAL_TYPE_OWNER = "owner";
	 
	    
	    /**
	     * Flag to disable the auto approval that occurs when there the launcher
	     * of the workflow is also the approver.  We typically will want to disable
	     * this for electronic signatures are in play OR if there is specific
	     * customer policy to disable it. 
	     */
	    public static final String ARG_DISABLE_AUTO_APPROVAL = "disableLauncherAutoApproval";

	    /**
	     * Variable in a workflow that holds the fallbackApprover when another
	     * approver cannot be resolved.   This default to spadmin.
	     */
	    public static final String ARG_FALLBACK_APPROVER = "fallbackApprover";

	    /**
	     * Arg that holds the approvalScheme.
	     */
	    public static final String ARG_APPROVAL_SCHEME = "approvalScheme";

	    /**
	     * Rule that will get one last chance to build the list of approvals.
	     */
	    private static final String ARG_APPROVAL_ASSIGNMENT_RULE = "approvalAssignmentRule";
	    
	    /**
	     * Arguments to the approval step.
	     */
	    Attributes<String,Object> _args;
	    
	    /**
	     * The approval set that we are approving
	     */
	    ApprovalSet _approvalSet;
	    
	    /**
	     * Name of the identity that has changed being approved
	     */
	    String _identityName;
	    
	    /**
	     * Display name of the identity thats being approved.
	     */
	    String _identityDisplayName;
	    
	    /**
	     * Who gets the workflow if we can't locate an Identity.
	     */
	    String _fallBackApprover;
	    
	    /**
	     * The launcher of the workflow.
	     */    
	    String _launcher;
	    
	    /**
	     * Context needed for various things, like fetching the 
	     * existing Triggers.
	     */
	    SailPointContext _context;
	    
	    /**
	     * This is always called from a workflow, so we require a 
	     * WorkflowContext. 
	     */
	    WorkflowContext _wfc;
	        
	    /**
	     * Flag to indicate if the "auto" approval behavior should
	     * be disabled.  
	     */
	    boolean _disableAutoApproval;
	    
	    /**
	     * Flag to indicate if we've been initialized.
	     */
	    boolean _initialized;
	        
	    String _approvalOwnerRule;
	    
		String _approvalOwnerRuleAttribute;
		
		String _approvalOwnerAttribute;
		
		String _approvalOwnerType;
		
		
	
		
	    /**
	     * Always called from a a workflow, initialize it with a 
	     * workflow context.
	     * 
	     * @param wfc
	     */
	    public IdentityRuleOwnerApprovalGenerator(WorkflowContext wfc) {
	        _wfc = wfc;
	    }
	    
	    /**
	     * Gather the required variables necessary to build up 
	     * Approval objects.
	     * 
	     * @throws GeneralException
	     */
	    protected void init() throws GeneralException {
	        _context = _wfc.getSailPointContext();
	        
	        if ( _initialized ) return;
	        
	        _args = _wfc.getArguments();
	        if ( _args == null)
	            throw new GeneralException("Args were null, unable to build approvals...");
	        
	        
	        _approvalOwnerRule = _args.getString("approvalOwnerRule");
			_approvalOwnerRuleAttribute = _args.getString("approvalOwnerRuleAttribute");
			_approvalOwnerType =  _args.getString("approvalOwnerType");
			_approvalOwnerAttribute =  _args.getString("approvalOwnerAttribute"); 
			
	    
	               
	        _approvalSet = (ApprovalSet) _args.get("approvalSet");
	        if ( isApprovalSetRequired() && (_approvalSet == null) ) {
	            throw new GeneralException("Required variable approvalSet");            
	        }
	        
	        _identityName = Util.getString(_args, IdentityLibrary.VAR_IDENTITY_NAME);
	        if ( _identityName == null ) {
	            throw new GeneralException("Required variable identityName");            
	        }
	        
	        _identityDisplayName = Util.getString(_args, "identityDisplayName");
	        if ( _identityDisplayName == null ) {
	            _identityDisplayName = _identityName;
	        }
	        _launcher = Util.getString(_args, Workflow.VAR_LAUNCHER);
	        
	        _disableAutoApproval = Util.getBoolean(_args, ARG_DISABLE_AUTO_APPROVAL);
	        _fallBackApprover = Util.getString(_args, ARG_FALLBACK_APPROVER);
	        if ( Util.isNullOrEmpty(_fallBackApprover) ) {
	            _fallBackApprover = "spadmin";
	        }
	        _initialized = true;
	    }

	    /**
	     * Return whether this generator requires and ApprovalSet.  Defaults to true, subclasses can override.
	     *
	     * @return True.
	     */
	    protected boolean isApprovalSetRequired() {
	        return true;
	    }


	    /**
	     * Build a list of Approvals for the owners of the objects
	     * being requested. 
	     * 
	     * This includes resolving Role Owners, Entitlement Owners and 
	     * Application Owners.
	     * 
	     * @return A list of approval objects, each targeted at the things they own.
	     * 
	     * @throws GeneralException
	     */
	    public List<Approval> buildRuleOwnerApprovals() throws GeneralException {
	        init();
	        return getRuleOwnerApprovalsInternal();
	    }
	            
	    private List<Approval> getRuleOwnerApprovalsInternal() throws GeneralException {
	        List<Approval> approvals = null;
	        Map<String,ApprovalSet> ownerMap = buildRuleOwnerMap();
	        if ( ownerMap != null ) {
	            approvals = buildApprovalsFromMap(ownerMap, "Owner");
	        }
	        return approvals;
	    }

	    /**
	     * Build a list of Approvals that will run in parallel for the owners of the objects being requested
	     *
	     * This will create a parent Approval with mode set to Parallel containing children approvals for each owner
	     * @return A parallel poll approval object with a child approval for each owner
	     * @throws GeneralException
	     */
	    private Approval buildRuleParallelOwnerApproval() throws GeneralException {
	        Approval parentApp = null;
	        List<Approval> approvals = buildRuleOwnerApprovals();
	        if (!Util.isEmpty(approvals)) {
	            parentApp = new Approval();
	            parentApp.setMode(Workflow.ApprovalModeParallelPoll);
	            parentApp.setChildren(approvals);
	        }
	        return parentApp;

	    }
	    
	    /**
	     * Based on the "approvalScheme" Get the list of approver names for a given 
	     * IIQ Identity Create/Edit.
	     * 
	     * @return A list of approver names
	     * 
	     * @throws GeneralException
	     */
	    public List<Approval> getIdentityApprovals() throws GeneralException {
	        init();
	        
	        List<Approval> approvals = null;
	        String approvalScheme = _args.getString(IdentityLibrary.VAR_APPROVAL_SCHEME);
	        if ( approvalScheme != null ) {            
	            if ( _approvalSet != null ) {
	                List<String> approvers = new ArrayList<String>();
	                List<ApprovalItem> items = _approvalSet.getItems();
	                if ( Util.size(items) == 1 ) {
	                    ProvisioningPlan plan = (ProvisioningPlan)Util.get(_args, IdentityLibrary.ARG_PLAN);
	                    if ( plan == null && approvalScheme.contains("newManager") ) {
	                        throw new GeneralException("For newManager schema you must define the plan so it can be used to find the new manager value.");
	                    }
	                    // See if we have workflow variable named "approverElectronicSignature", disable auto 
	                    // approve
	                    String esig = Util.getString(_args, "approverElectronicSignature");
	                    if ( !Util.isNullOrEmpty(esig) ) {
	                        _disableAutoApproval = true;
	                    }
	                    //  There is normally just one approval in an IIQ request
	                    approvers = getIdentityApprovers(approvalScheme, items.get(0), plan);
	                }
	                
	                if ( Util.size(approvers) > 0 ) {
	                    approvals = new ArrayList<Approval>();
	                    for ( String approver : approvers ) {
	                        Approval approval = buildApprovalInternal(_approvalSet, approver);
	                        if ( approval != null ) {
	                            approvals.add(approval);
	                        }
	                        setApprovalDescription("Identity", approval);
	                    }
	                }
	            }
	        }
	        return approvals;
	    }
	    
	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Simple Approvals
	    //
	    ///////////////////////////////////////////////////////////////////////////
	    
	    /**
	     * Build the approvals for the specified type, which will correlate
	     * to one of the approval schemes.
	     * 
	     * Always use 'fallbackApprover' if an owner for the approval
	     * cannot be found.
	     * 
	     * @ignore Returning a list of approval objects because approval sets
	     * except a list and it will make it simpler change the behavior
	     * if we desire later. Right now returns a list 
	     * 
	     * @param wfc
	     * @param type
	     * @return list of Approval objects for the given type
	     * 
	     * @throws GeneralException
	     */
	    private Approval buildApproval(String type) 
	            throws GeneralException {

	        if ( type == null ) {
	            type = "manager";
	        }
	        
	        String approverName = null;
	       
	        // Always fallback to the default approval
	        if ( Util.isNullOrEmpty(approverName) ) {
	            approverName = _fallBackApprover;
	        }    
	        
	        Approval approval = buildApprovalInternal(_approvalSet, approverName);
	        if ( approval != null ) {
	            setApprovalDescription(type, approval);
	        }
	        return approval;
	    }

	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Owner Approval
	    //
	    ///////////////////////////////////////////////////////////////////////////
	    
	    /**
	     * Build a Map<String,ApprovalSet> each key representing a unique owner.
	     * Use a Map here to allow us to easily merge any items for the same owner.
	     *
	     */
	    private Map<String,ApprovalSet> buildRuleOwnerMap() 
	        throws GeneralException {

	        // djs: use a LinkedHashMap here to preserve the order of the
	        // approvers in the list.  This is important for manager transfer
	        // approvals.
	        Map<String,ApprovalSet> ownerToSet = new LinkedHashMap<String,ApprovalSet>();

	        List<ApprovalItem> items = _approvalSet.getItems();
	        if ( items == null ) {
	            log.debug("No items in approval set, no owners to resolve.");
	        }
	        
	        for ( ApprovalItem item : items ) {
	            List<String> approvers = getRuleOwners(item);
	            if ( Util.size(approvers) == 0 && _fallBackApprover != null ) {
	                if ( log.isDebugEnabled() ) {
	                    log.debug("Approver could not be resolved using fallbackApprover '"+_fallBackApprover+"'.");
	                }
	                approvers.add(_fallBackApprover);
	            }
	            
	            //
	            // Build an approval set or add an ApprovalItem 
	            // to an existing set
	            //
	            if ( Util.size(approvers) > 0 ) {
	                for ( String approver : approvers ) {
	                    ApprovalSet set = (ApprovalSet)ownerToSet.get(approver);
	                    if ( set == null ) {
	                        set = new ApprovalSet();
	                    }
	                    
	                    // Make a copy of the item here so they are independent of the the cart's item.  
	                    ApprovalItem itemCopy = (ApprovalItem) XMLObjectFactory.getInstance().clone(item, _context);
	                    set.add(itemCopy);
	                    
	                    ownerToSet.put(approver, set);
	                    if ( autoApproveAllowed()  ) {
	                        // djs: when we come across the launcher who is also the approver
	                        // or member of the approver workgroup auto approve the item, 
	                        // this will allow us to audit/report on
	                        // the request but not force an approval.  We won't create an 
	                        // Approval object if all of the items are acccepted
	                        List<String> workGroupMembers = getWorkGroupMemberNames(approver);
	                        if ( approver.equals(_launcher) || Util.nullSafeContains(workGroupMembers, _launcher)) {
	                            itemCopy.setState(WorkItem.State.Finished);
	                            if ( log.isDebugEnabled() ) {
	                                log.debug("Launcher was also approver and was removed.");
	                            }
	                            // If there is just one approver AND we are marking this 
	                            // Auto-Approved also mark the master approvalSets item 
	                            // finished
	                            if ( approvers.size() == 1 ) {
	                                _approvalSet.findAndMergeItem(itemCopy, approvers.get(0), null, true);
	                            }
	                        }
	                    }
	                }
	                // Update the "cart" representation
	                //
	                // set the item's owner so we have an update
	                // version in the "registry"
	                // Should we store a csv Multiple approvers ?
	                item.setOwner(approvers.get(0));
	            }
	        }
	        if ( log.isDebugEnabled() ) {
	            if ( !Util.isEmpty(ownerToSet) )
	                log.debug("OwnerSetMap: " + XMLObjectFactory.getInstance().toXml(ownerToSet));
	            else
	                log.debug("OwnerSetMap EMPTY.");
	        }
	        return ownerToSet;
	    }

	    /**
	     * For a single approval item figure out the owner of the
	     * thing referenced in the owner.
	     * 
	     * @param item
	     * @return a List of owner Identity names
	     * 
	     * @throws GeneralException
	     */
	    private List<String> getRuleOwners(ApprovalItem item)
	        throws GeneralException { 
	        
	        if ( item == null )
	            return null;

	        List<String> owners = new ArrayList<String>();
	        String app = item.getApplication(); 
	        if ( ProvisioningPlan.APP_IIQ.compareTo(app) == 0 ) { 
	            if ( ( ProvisioningPlan.ATT_IIQ_DETECTED_ROLES.equals(item.getName()) ) ||
	                 ( ProvisioningPlan.ATT_IIQ_ASSIGNED_ROLES.equals(item.getName()) ) ) {
	                String owner = resolveRuleRoleOwner(item);
	                if ( owner != null ) 
	                    owners.add(owner);
	            } 
	        } else {
	            String owner = resolveRuleEntitlementOwner(item);
	            if ( owner != null ) 
	                owners.add(owner);
	        }
	        return owners;
	    }
	    
	    

	  
	    /**
	     * 
	     * Resolve the owners of the item's value, which will 
	     * reference a role.  Currently just find the owner of the role
	     * and that's the approver.
	     * 
	     * Returns the name of the Identity that owners the role (role.owner)
	     * 
	     */
	    private String resolveRuleRoleOwner(ApprovalItem item) 
	        throws GeneralException {
	        
	        String approverName = null;
	        List<String> values = item.getValueList();
	        if ( Util.size(values) > 0 ) {
	            if ( Util.size(values) != 1 ) 
	                throw new GeneralException("More then one value found in an approval item");

	            String roleName = (String)values.get(0);
	           // approverName = resolveProperty(Bundle.class, roleName, "owner.name");
	            
	            if(_approvalOwnerType.equals("Rule"))
	    		{
	    		
	    			Map args = new HashMap();
	    			args.put("context_",_context);
	    			args.put("wfcontext",_wfc);
	    			args.put("item",item);
	    			args.put("identityName",_identityName);
	    			
	    			Rule rule = null;
	    			
	    			if(_approvalOwnerRule!= null)
	    			{
	    			 rule = _context.getObjectByName(Rule.class, _approvalOwnerRule);
	    			}
	    			
	    			if (rule != null) 
	    				approverName = (String)_context.runRule(rule, args);
	    			
	    		
	    		}
	    		else if(_approvalOwnerType.equals("Rule Attribute"))
	    		{
	    		    String ruleName = null;
	    		    
	    		        System.out.println("VALUE IS:  "+item);
	    		        System.out.println("VALUE IS:  "+item.getValue());
	    		      	Bundle aBund = _wfc.getSailPointContext().getObjectByName(Bundle.class, (String)item.getValue());
	    		      	System.out.println("VALUE IS:  "+_approvalOwnerRuleAttribute);
	    		      	ruleName = (String)aBund.getAttribute(_approvalOwnerRuleAttribute);
	    		      	System.out.println("VALUE IS:  "+ruleName);
	    		   
	    		    
	    		    
	    		    if(ruleName!= null)
	    		    {
	    				Map args = new HashMap();
	    				args.put("context_",_context);
		    			args.put("wfcontext",_wfc);
		    			
		    			args.put("itemName",item.getName());
		    			args.put("itemValue",item.getValue());
		    			args.put("item",item);
		    			args.put("identityName",_identityName);
	    				Rule rule = null;
	    				
	    				if(ruleName!= null)
	    				{
	    				 rule =  _context.getObjectById(Rule.class, ruleName);
	    				}
	    					System.out.println("VALUE IS:  "+rule);
	    				
	    				if (rule != null) 
	    				return (String)_context.runRule(rule, args);
	    			}
	    			else return "spadmin";
	    			
	    		}
	        }
	        return approverName;
	    }
	    
	  
	      private String getRuleManagedAttributeOwner(Application app, String name, String value ) 
	        throws GeneralException {
	        
	        String owner = null;
	        ManagedAttribute ma = ManagedAttributer.get(_context, app.getId(), name, value);
	        if ( ma != null ) {
	            Identity maOwner = null;// ma.getOwner();
	           // if ( maOwner != null ) {
	           //     owner = maOwner.getName();
	           // }
	            
	            if(_approvalOwnerType.equals("Rule"))
	    		{
	    		
	    			Map args = new HashMap();
	    			args.put("context_",_context);
	    			args.put("wfcontext",_wfc);
	    			
	    			args.put("application",app);
	    			args.put("itemName",name);
	    			args.put("itemValue",value);
	    			args.put("identityName",_identityName);
	    			Rule rule = null;
	    			
	    			if(_approvalOwnerRule!= null)
	    			{
	    			 rule = _context.getObjectByName(Rule.class, _approvalOwnerRule);
	    			}
	    			
	    			if (rule != null) 
	    			owner = (String)_context.runRule(rule, args);
	    			
	    		
	    		}
	    		else if(_approvalOwnerType.equals("Rule Attribute"))
	    		{
	    		    String ruleName = null;
	    		 
	    		     // applicationObject = ma.getApplication();
	    			//ManagedAttribute attributeObject = ManagedAttributer.get(_wfc.getSailPointContext(), applicationObject, false, item.getName(),  ((List)item.getValue()).get(0));
	    			ruleName = (String)ma.getAttribute(_approvalOwnerRuleAttribute);
	    		    
	    		    if(ruleName!= null)
	    		    {
	    				Map args = new HashMap();
	    				args.put("context_",_context);
		    			args.put("wfcontext",_wfc);
		    			args.put("application",app);
		    			args.put("itemName",name);
		    			args.put("itemValue",value);
		    			args.put("identityName",_identityName);
		    			
	    				Rule rule = null;
	    				
	    				if(ruleName!= null)
	    				{
	    				 rule =  _wfc.getSailPointContext().getObjectById(Rule.class, ruleName);
	    				}
	    					System.out.println("VALUE IS:  "+rule);
	    				
	    				if (rule != null) 
	    					owner = (String)_context.runRule(rule, args);
	    			}
	    			else owner = "spadmin";
	    			
	    		}
	                
	        }
	        return owner;
	    }
	    private String resolveRuleEntitlementOwner(ApprovalItem item) 
	        throws GeneralException {
	        
	        String approverName = null;
	        
	        if ( item == null )
	            throw new GeneralException("ApprovalItem was null, could not compute owner.");

	        String appName = item.getApplication();
	        if ( appName == null ) {
	            return null;
	        }
	        
	        Application application = _context.getObjectByName(Application.class, appName);
	        if ( application == null ) 
	            throw new GeneralException("Couldn't find application ["+appName+"]");

	        Identity owner = null;//application.getOwner();
	      /*  
	        if ( owner != null ) {
	            approverName = owner.getName();
	        } else {
	            // unable to find owner for application foo...
	            log.debug("Unable to find owner for application ["+appName+"]");
	        }  
	      */

	        // Check the attributeName and value and check to see if we are dealing with 
	        // a ManagedAttribute if so see if can use the owner from the owner set ther
	        String attrName = item.getName();
	        List<String> vals = item.getValueList();
	        if ( ( attrName != null ) && ( Util.size(vals) > 0 ) ) {
	            // there should just be one value in each record... 
	            // log something
	            String val = (String)vals.get(0);
	            String maOwner = getRuleManagedAttributeOwner(application, attrName, val);
	            if ( maOwner == null) {
	                log.debug("Managed Attribute owner not found.. falling back to app owner.");
	            } else {
	                approverName = maOwner;
	            }
	        }
	        return approverName;
	    }
	    
	    /**
	     * Use the ManagedAttributer to find the managed attribute and get the
	     * owner from the ManagedAttribute.
	     */

	    
	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Manager Approval
	    //
	    ///////////////////////////////////////////////////////////////////////////
	        
	   
	    
	    
	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Identity Create and Update Approvals  
	    //
	    ///////////////////////////////////////////////////////////////////////////
	        
	    /**
	     * Used during Identity Create and Update approvals, which are a bit differnt
	     * then then "normal" approvals because all of the approvers see the exact
	     * same approval.
	     * 
	     * Return a list of names that represent the names of the Identities that should
	     * approve the item.  
	     * 
	     * We need the Plan during manager transfers when we need to figure
	     * out what the new value is for the manager attribute.
	     *
	     * Only time the returned list is null is when we have "none" approval scheme.
	     */
	    private List<String> getIdentityApprovers(String scheme, ApprovalItem item, ProvisioningPlan plan)
	        throws GeneralException { 

	        List<String> approvers = new ArrayList<String>();
	        
	        if ( scheme == null ) {
	            throw new GeneralException ("Approval scheme was null unable to get Identity approvers.");
	        }

	        List<String> schemes = Util.csvToList(scheme);
	        
	        // "none" will short-circuit and cause no approvals
	        if ( schemes.contains("none") ) {
	            return null;
	        } 
	        
	        if ( scheme.contains("owner") ) {
	            List<String> ownerNames = getRuleOwners(item);
	            if ( Util.size(ownerNames) > 0 ) {
	                approvers.addAll(ownerNames);
	            }
	        }

	            
	        if ( approvers.size() == 0 ) {
	            if ( Util.isNotNullOrEmpty(_fallBackApprover) ) {
	                log.debug("Approver could not be resolved using fallbackApprover '"+_fallBackApprover+"'.");
	                approvers.add(_fallBackApprover);
	            } else
	                throw new GeneralException("Approver could not be generated, null or fallback approver.");
	        }
	        return approvers;
	    }       
	    
	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Generic Approvals - Used by 6.2 Approval Subprocess  
	    //
	    ///////////////////////////////////////////////////////////////////////////
	    
	    /**
	     * 
	     * Build a list of approvals based on the approval scheme.
	     * 
	     * @Since 6.2
	     * 
	     * @return List of Approval objects
	     * 
	     * @throws GeneralException
	     */
	    @SuppressWarnings("unchecked")
	    public List<Approval> buildCommonApprovals(List<String> schemes)
	        throws GeneralException {

	        List<Approval> approvals = new ArrayList<Approval>();
	        init();
	        if ( !Util.isEmpty(schemes) ) {
	            if ( Util.nullSafeContains(schemes, "none") ) {
	                // none overrides others
	                return null;
	            }

	            Boolean originalValue = Util.otob(_disableAutoApproval);
	            for (String approvalScheme : schemes ) {
	                try {
	                    if ( approvalScheme == null )
	                        continue;

	                    List<Approval> schemeApprovals = new ArrayList<Approval>();

	                    // allow scheme specific overrides to the electronic signature that might
	                    // be set on the ApprovalStep
	                    String esig = _args.getString(approvalScheme+"ElectronicSignature");
	                    if ( Util.isNullOrEmpty(esig) && Util.nullSafeEq(approvalScheme,"newManager") ){
	                        // fall back here, they'll likely be the same
	                        esig = _args.getString("managerElectronicSignature");
	                    }

	                    //
	                    // If we have an esig at any level explicitlydisableAutoApproval
	                    //
	                    if ( Util.isNotNullOrEmpty(esig) ) {
	                        _disableAutoApproval = true;
	                    }

	                    //TODO: think about overlap between modes
	                    if ( Util.nullSafeEq("owner", approvalScheme) ) {
	                        Approval ownerApp = buildRuleParallelOwnerApproval();
	                        if (ownerApp != null) {
	                            schemeApprovals.add(ownerApp);
	                        }
	                   } else
	                   if ( Util.nullSafeEq("identity", approvalScheme) ) {
	                        List identities = _args.getList("approvingIdentities") ;
	                        if ( identities != null ) {
	                            for ( Object owner: identities ) {
	                                String nameOrId = Util.otoa(owner);
	                                if ( nameOrId != null ) {
	                                    Approval approval = buildApprovalInternal(_approvalSet, nameOrId);
	                                    if ( approval != null ) {
	                                       setApprovalDescription(approvalScheme, approval);
	                                       schemeApprovals.add(approval);
	                                    }
	                                }
	                            }
	                        }
	                    } else {
	                        Approval schemeApproval  = buildApproval(approvalScheme);
	                        if ( schemeApproval != null ) {
	                            schemeApprovals.add(schemeApproval);
	                        }
	                    }

	                    // Adorn esigs and email templates to the approval if configured
	                    if ( schemeApprovals != null ) {
	                        for (Approval app : schemeApprovals) {
	                            addCommonArgs(app, approvalScheme);
	                        }
	                        approvals.addAll(schemeApprovals);
	                    }

	                } finally {
	                    // Always put this back when we complete
	                    _disableAutoApproval = originalValue;
	                }
	            }
	        }

	        String ruleName = _args.getString(ARG_APPROVAL_ASSIGNMENT_RULE);
	        if ( Util.isNotNullOrEmpty(ruleName) ) {
	            Rule rule = _context.getObject(Rule.class, ruleName);
	            if ( rule == null ) {
	                throw new GeneralException("Unable to find the approval assignment " + ruleName);
	            }
	            HashMap<String,Object> ruleContext = new HashMap<String,Object>();
	            ruleContext.putAll(_args);
	            ruleContext.put("approvals", approvals );
	            ruleContext.put("approvalSet", _approvalSet);
	            approvals = (List<Approval>) _context.runRule(rule, ruleContext);
	        }
	        return ( approvals != null ) ? approvals : null;
	    }        

	    public List<Approval> buildCommonApprovals() throws GeneralException {
	        init();
	        String scheme = _args.getString(ARG_APPROVAL_SCHEME);
	        if ( scheme != null ) {
	            List<String> schemes = Util.csvToList(scheme);
	            return buildCommonApprovals(schemes);
	        } else {
	            return null;
	        }
	    }

	    private void addCommonArgs(Approval a, String approvalScheme) {
	        if (a != null) {
	            if (!Util.isEmpty(a.getChildren())) {
	                for (Approval ca : a.getChildren()) {
	                    addCommonArgs(ca, approvalScheme);
	                }
	            }

	            if ((a.getApprovalSet() != null) || !this.isApprovalSetRequired()) {
	                String emailTemplate = _args.getString(approvalScheme + "EmailTemplate");
	                if (Util.isNullOrEmpty(emailTemplate) && Util.nullSafeEq(approvalScheme, "newManager")) {
	                    // fall back here, they'll likely be the same
	                    emailTemplate = _args.getString("managerEmailTemplate");
	                }

	                String esig = _args.getString(approvalScheme + "ElectronicSignature");
	                if (Util.isNullOrEmpty(esig) && Util.nullSafeEq(approvalScheme, "newManager")) {
	                    // fall back here, they'll likely be the same
	                    esig = _args.getString("managerElectronicSignature");
	                }
	                if (esig != null) {
	                    a.addArg(WorkItem.ATT_ELECTRONIC_SIGNATURE, esig);
	                }
	                if (emailTemplate != null) {
	                    a.addArg(Workflow.ARG_WORK_ITEM_NOTIFICATION_TEMPLATE, emailTemplate);
	                }
	                setApprovalDescription(approvalScheme, a);
	            }
	        }
	    }
	    ///////////////////////////////////////////////////////////////////////////
	    //
	    // Utility  
	    //
	    ///////////////////////////////////////////////////////////////////////////

	  
	    /**
	     * Using the approval set and the approval name build a new sailpoint.object.Workflow.Approval
	     * object.
	     * 
	     * @param set
	     * @param approverName
	     * 
	     * @return null if it's auto approved, an Approval otherwise
	     */
	    protected Approval buildApprovalInternal(ApprovalSet set, String approverName) throws GeneralException{
	        Approval approval = null;
	        if ( isAutoApprove(approverName) ) {
	            for (ApprovalItem item : Util.safeIterable(set.getItems())) {
	                if (item == null)
	                    continue;

	                //Watch out in case we are generating a split approval set
	                //where a launcher-approver made the original request so we don't override
	                //an up-the-chain rejection.
	                if (!item.isRejected()) {
	                    item.setState(WorkItem.State.Finished);
	                }

	                item.setOwner(_launcher);

	                //Audit the auto approval. Should we spawn off a private context to do this? -rap
	                try {
	                    IdentityLibrary.auditDecision(_wfc, item);
	                } catch(GeneralException ge) {
	                    log.error("Failed to audit approval auto approve");
	                }
	            }
	        } else {                    
	            approval = new Approval();
	            if ( approverName != null )
	                approval.setOwner("\"" + approverName + "\"");  // quote the owner in case the user name has a comma
	            //Create a clone of the approval set so we won't update the masterSet
	            approval.setApprovalSet(set.clone());
	            approval.addArg(Workflow.ARG_WORK_ITEM_TARGET_CLASS, "sailpoint.object.Identity");
	            approval.addArg(Workflow.ARG_WORK_ITEM_TARGET_NAME, _identityName);    
	        }
	        return approval;        
	    }

	    /**
	     * Return true if the launcher is the requester and auto-approval is allowed (ie - there is not an electronic
	     * signature required).
	     *
	     * @param approverName  The name of the approver.
	     *
	     * @return True if auto-approval is allowed, false otherwise.
	     */
	    protected boolean isAutoApprove(String approverName) throws GeneralException {
	        List<String> workGroupMembers = getWorkGroupMemberNames(approverName);
	        return (Util.nullSafeEq(approverName, _launcher) || Util.nullSafeContains(workGroupMembers, _launcher)) && autoApproveAllowed();
	    }

	    /**
	     * Convert a Map<String,ApprovalSet> into Approval objects. 
	     * One Approval object for each of the keys in the map.
	     * 
	     * This method is public so that consumers of the old Rule library can
	     * switch to using this method as the rule methods are now
	     * deprecated.
	     * 
	     * @param approverNameToApprovalSet
	     * @param approvalScheme
	     * @return List of ApprovalObjects one per key in the Map
	     * 
	     * @throws GeneralException
	     */
	    public List<Approval> buildApprovalsFromMap(Map<String,ApprovalSet> approverNameToApprovalSet, String approvalScheme) 
	        throws GeneralException {
	       
	        // this only init's if required
	        init();
	        List<Approval> approvals = new ArrayList<Approval>();
	        if ( approverNameToApprovalSet == null ) 
	            return null;

	        Iterator<String> keys = null;
	        Set<String> keySet = approverNameToApprovalSet.keySet();
	        if ( keySet != null )  
	            keys = keySet.iterator();

	        if ( keys != null ) {
	            while ( keys.hasNext() ) {
	                String name = (String)keys.next();
	                if ( name == null ) continue;
	                ApprovalSet set = approverNameToApprovalSet.get(name);
	                if ( set != null ) {
	                    if ( log.isDebugEnabled() ) {
	                        log.debug("Approver["+name+"] " + set.toXml());
	                    }
	                    Approval approval = buildApprovalInternal(set, name);
	                    if ( approval != null ) {
	                        setApprovalDescription(approvalScheme, approval);
	                        approvals.add(approval);
	                    }
	                }
	            }
	        }
	        if ( log.isDebugEnabled() ) {
	            if ( Util.size(approvals) > 0 )
	                log.debug("Approvals: " + XMLObjectFactory.getInstance().toXml(approvals));
	            else
	                log.debug("Approvals EMPTY.");
	        }
	        return approvals;
	    }
	    
	    /**
	     * Check with the current step's approval and see if it has an Electronic Signature
	     * which will disable auto approvals.
	     * 
	     * Also check with the explicit flag that allows disabling the auto
	     * approvals.
	     * 
	     * @return true if auto approvals are ok
	     */
	    private boolean autoApproveAllowed() {
	        if ( _disableAutoApproval || IdentityLibrary.isElectronicSignatureEnabled(_wfc) ) {
	            return false;
	        }
	        return true;
	    }

	    /**
	     * Set the default built workitem description based on the type if not explicitly configured
	     * as an argument.
	     * 
	     * @param type
	     * @param approval
	     */
	    private void setApprovalDescription(String type, Approval approval) {
	        if ( approval != null )  {
	            String description = _args.getString(Workflow.ARG_WORK_ITEM_DESCRIPTION);
	            if ( Util.isNullOrEmpty(description) ) {
	                description = createApprovalDescription(type, approval);
	            }
	            approval.setDescription(description );
	        }
	    }

	    /**
	     * Create the description for the approval object.
	     *
	     * @param type  The approval scheme.
	     * @param approval  The Approval for which to create the description.
	     *
	     * @return  The description for the approval.
	     */
	    protected String createApprovalDescription(String type, Approval approval) {
	        return Util.splitCamelCase(type) + " Approval - Account Changes for User: " + _identityDisplayName;
	    }

	    /**
	     * Method to return workgroup member names given an workgroup Identity
	     * @param workGroup
	     * @return
	     * @throws GeneralException
	     */
	    private List<String> getWorkgroupMemberNames(Identity workGroup) throws GeneralException{
	        List<String> memberNames = new ArrayList<String>();
	        Iterator<Object[]> memberItr = ObjectUtil.getWorkgroupMembers(_context, workGroup, Util.csvToList("name"));
	        if(memberItr != null) {
	            while(memberItr.hasNext()) {
	                memberNames.add((String)memberItr.next()[0]);
	            }
	        }
	        
	        return memberNames;
	    }
	    
	    /**
	     * Method to return workgroup member names given a workgroup string name
	     * @param workGroupName
	     * @return
	     * @throws GeneralException
	     */
	    private List<String> getWorkGroupMemberNames(String workGroupName) throws GeneralException{
	        Identity workGroup = _context.getObjectByName(Identity.class, workGroupName);
	        if(workGroup != null && workGroup.isWorkgroup()) {
	            return getWorkgroupMemberNames(workGroup);
	        } else {
	            return null;
	        }
	    }
	}
