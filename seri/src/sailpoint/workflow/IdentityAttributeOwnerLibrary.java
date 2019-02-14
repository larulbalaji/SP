package sailpoint.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.workflow.IdentityRuleOwnerApprovalGenerator;

import sailpoint.tools.GeneralException;


public class IdentityAttributeOwnerLibrary extends IdentityLibrary {	

		    private static Log log = LogFactory.getLog(IdentityLibrary.class);

		    public IdentityAttributeOwnerLibrary() {
		    }
		    
		    public Object buildAttributeOwnerApprovals(WorkflowContext wfc) 
		            throws GeneralException {
		            
		            return new IdentityAttributeOwnerApprovalGenerator(wfc).buildAttributeOwnerApprovals();
		     }
}