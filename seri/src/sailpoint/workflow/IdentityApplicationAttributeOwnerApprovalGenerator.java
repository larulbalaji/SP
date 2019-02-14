package sailpoint.workflow;

	import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
	import java.util.LinkedHashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.Set;

	import org.apache.commons.logging.Log;
	import org.apache.commons.logging.LogFactory;

	import sailpoint.api.ObjectUtil;
	import sailpoint.api.SailPointContext;
	import sailpoint.object.Application;
	import sailpoint.object.ApprovalItem;
	import sailpoint.object.ApprovalSet;
	import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.Identity;
import sailpoint.object.Rule;
import sailpoint.object.WorkItem;
	import sailpoint.object.Workflow;
	import sailpoint.object.Workflow.Approval;
	import sailpoint.tools.GeneralException;
	import sailpoint.tools.Util;
	import sailpoint.tools.xml.XMLObjectFactory;

	public class IdentityApplicationAttributeOwnerApprovalGenerator {  

		private static Log log = LogFactory.getLog(IdentityApplicationRuleOwnerApprovalGenerator.class);
		  
	    public static final String APPROVAL_TYPE_MANAGER = "manager";
	    public static final String APPROVAL_TYPE_NEW_MANAGER = "newManager";
	    public static final String APPROVAL_TYPE_OWNER = "owner";
	    public static final String APPROVAL_TYPE_SECURITY_OFFICER = "securityOfficer";
	    
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
	    
	    String _approvalOwnerRule;
			String _approvalOwnerRuleAttribute;
			String _approvalOwnerAttribute;
			String _approvalOwnerType;
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
	        
	    /**
	     * Always called from a a workflow, initialize it with a 
	     * workflow context.
	     * 
	     * @param wfc
	     */
	    public IdentityApplicationAttributeOwnerApprovalGenerator(WorkflowContext wfc) {
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
	        
	        _approvalOwnerRule = _args.getString("approvalOwnerRule");
			_approvalOwnerRuleAttribute = _args.getString("approvalOwnerRuleAttribute");
			_approvalOwnerType =  _args.getString("approvalOwnerType");
			_approvalOwnerAttribute =  _args.getString("approvalOwnerAttribute"); 
			
			
	        if ( Util.isNullOrEmpty(_fallBackApprover) ) {
	            _fallBackApprover = "spadmin";
	        }
	        _initialized = true;
	    }

	    private boolean autoApproveAllowed() {
	        if ( _disableAutoApproval || IdentityLibrary.isElectronicSignatureEnabled(_wfc) ) {
	            return false;
	        }
	        return true;
	    }

	    protected boolean isApprovalSetRequired() {
	        return true;
	    }


	    
	    public List<Approval> buildApplicationRuleOwnerApprovals() throws GeneralException {
	        init();
	        return getApplicationOwnerApprovalsInternal();
	    }
	    
	    private List<Approval> getApplicationOwnerApprovalsInternal() throws GeneralException {
	        List<Approval> approvals = null;
	        Map<String,ApprovalSet> ownerMap = buildApplicationOwnerMap();
	        if ( ownerMap != null ) {
	            approvals = buildApprovalsFromMap(ownerMap, "Application Owner");
	        }
	        return approvals;
	    }
	    
	    private Map<String,ApprovalSet> buildApplicationOwnerMap() throws GeneralException {

	        // djs: use a LinkedHashMap here to preserve the order of the
	        // approvers in the list.  This is important for manager transfer
	        // approvals.
	        Map<String,ApprovalSet> ownerToSet = new LinkedHashMap<String,ApprovalSet>();

	        List<ApprovalItem> items = _approvalSet.getItems();
	        if ( items == null ) {
	            log.debug("No items in approval set, no owners to resolve.");
	        }
	        
	        for ( ApprovalItem item : items ) {
	            List<String> approvers = getApplicationOwners(item);
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
	    
	    
	    private String resolveAppAttributeOwner( ApprovalItem item, String applicationName) 
		        throws GeneralException {
		        
		        String approverName = null;
		        List<String> values = item.getValueList();
		        if ( Util.size(values) > 0 ) {
		            if ( Util.size(values) != 1 ) 
		                throw new GeneralException("More then one value found in an approval item");

    		      	Application aApp = _wfc.getSailPointContext().getObjectByName(Application.class, applicationName);
    		      	System.out.println("VALUE IS:  "+_approvalOwnerAttribute);
    		      	approverName = (String)aApp.getStringAttributeValue(_approvalOwnerAttribute);
    		      	System.out.println("VALUE IS:  "+approverName);
		    		
		        }
		        return approverName;
		    }
		    
	    
	    private List<String> getApplicationOwners(ApprovalItem item)
	            throws GeneralException { 
	            
	            if ( item == null )
	                return null;

	            List<String> owners = new ArrayList<String>();
	            String app = item.getApplication(); 
	            
	            String approverName = resolveAppAttributeOwner(item, app);
	             if ( approverName != null ) {
	                //approverName = owner.getName();
	                owners.add(approverName);
	            } else {
	                // unable to find owner for application foo...
	                log.debug("Unable to find owner for application ["+app+"]");
	            }
	            
	            return owners;
	        }
	    
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
	    protected boolean isAutoApprove(String approverName) throws GeneralException {
	        List<String> workGroupMembers = getWorkGroupMemberNames(approverName);
	        return (Util.nullSafeEq(approverName, _launcher) || Util.nullSafeContains(workGroupMembers, _launcher)) && autoApproveAllowed();
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
	    
	    private void setApprovalDescription(String type, Approval approval) {
	        if ( approval != null )  {
	            String description = _args.getString(Workflow.ARG_WORK_ITEM_DESCRIPTION);
	            if ( Util.isNullOrEmpty(description) ) {
	                description = createApprovalDescription(type, approval);
	            }
	            approval.setDescription(description );
	        }
	    }
	    protected String createApprovalDescription(String type, Approval approval) {
	        return Util.splitCamelCase(type) + " Approval - Account Changes for User: " + _identityDisplayName;
	    }



	}
