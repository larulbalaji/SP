
	package sailpoint.workflow;

	import org.apache.commons.logging.Log;
	import org.apache.commons.logging.LogFactory;
	import sailpoint.workflow.IdentityApplicationOwnerApprovalGenerator;

	import sailpoint.tools.GeneralException;

	public class IdentityApplicationAttributeOwnerLibrary  extends IdentityLibrary {

	    private static Log log = LogFactory.getLog(IdentityLibrary.class);

	    public IdentityApplicationAttributeOwnerLibrary() {
	    }
	    
	    public Object buildApplicationOwnerApprovals(WorkflowContext wfc) 
	            throws GeneralException {
	            
	            return new IdentityApplicationAttributeOwnerApprovalGenerator(wfc).buildApplicationRuleOwnerApprovals();
	        }

	}

