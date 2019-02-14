package sailpoint.seri.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.Provisioner;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AccountRequest.Operation;
import sailpoint.tools.GeneralException;

/**
 * @author Kev James
 *
 */

public class WorkflowUtils {

	private static Log _log = LogFactory.getLog(WorkflowUtils.class);


	/**
	 * Sets an IIQ attribute using the Provisioner - the "Correct" method
	 * 
	 * @param context a Sailpoint Context object
	 * @param identityName The identity to work on
	 * @param attribute The attribute to set
	 * @param value The value to set it to
	 * @param fireEvents Whether to do an identity refresh with process events or not
	 */
	public static void setIIQAttribute(SailPointContext context, String identityName, String attribute, String value, boolean fireEvents) throws GeneralException{
		
		_log.debug("setting "+attribute+"="+value+" for "+identityName);
		Identity ident = context.getObjectByName(Identity.class,identityName);
		if(ident==null) {
			throw new GeneralException("Cannot find identity "+identityName);
		}
		_log.debug("original attribute value: "+ident.getAttribute(attribute));
		// Build the plan
		ProvisioningPlan plan = new ProvisioningPlan();
		plan.setIdentity(ident);
		plan.setNativeIdentity(identityName);
		AccountRequest acr = new AccountRequest();
		acr.setOperation(Operation.Modify);
		acr.setNativeIdentity(ident.getName());
		acr.setApplication("IIQ");
		ProvisioningPlan.AttributeRequest attrReq7 = new ProvisioningPlan.AttributeRequest();
		attrReq7.setOperation(ProvisioningPlan.Operation.Set);
		attrReq7.setName(attribute);
		attrReq7.setValue(value);
		acr.add(attrReq7);
		plan.add(acr);

		// Provision the plan
		Provisioner p = new Provisioner(context);
		if(fireEvents) {
			_log.debug("firing events for "+identityName);
			p.setDoRefresh(true);
			Map<String,Object> opts=new HashMap<String,Object>();
			opts.put("processTriggers", Boolean.TRUE);
			p.setRefreshOptions(opts);
		} else {
			_log.debug("not firing events for "+identityName);
		}
		p.execute(plan);
		
		
	}
	
}
