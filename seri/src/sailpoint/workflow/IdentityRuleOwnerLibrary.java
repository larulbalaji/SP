	package sailpoint.workflow;

	import org.apache.commons.logging.Log;
	import org.apache.commons.logging.LogFactory;
	import sailpoint.workflow.IdentityRuleOwnerApprovalGenerator;

	import sailpoint.tools.GeneralException;


	public class IdentityRuleOwnerLibrary extends IdentityLibrary {	

			    private static Log log = LogFactory.getLog(IdentityLibrary.class);

			    public IdentityRuleOwnerLibrary() {
			    }
			    
			    public Object buildAttributeRuleOwnerApprovals(WorkflowContext wfc) 
			            throws GeneralException {
			            
			            return new IdentityRuleOwnerApprovalGenerator(wfc).buildRuleOwnerApprovals();
			     }
	}