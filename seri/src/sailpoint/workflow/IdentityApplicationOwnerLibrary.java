package sailpoint.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.workflow.IdentityApplicationOwnerApprovalGenerator;

import sailpoint.tools.GeneralException;

public class IdentityApplicationOwnerLibrary extends IdentityLibrary {

    private static Log log = LogFactory.getLog(IdentityLibrary.class);

    public IdentityApplicationOwnerLibrary() {
    }
    
    public Object buildApplicationOwnerApprovals(WorkflowContext wfc) 
            throws GeneralException {
            
            return new IdentityApplicationOwnerApprovalGenerator(wfc).buildApplicationOwnerApprovals();
        }

}
