/* (c) Copyright 2008 SailPoint Technologies, Inc., All Rights Reserved. */

/**
 * 
 */

package sailpoint.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.ManagedAttributer;
import sailpoint.api.SailPointContext;
import sailpoint.api.Workflower;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.ObjectRequest;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowCase;
import sailpoint.object.WorkflowLaunch;
import sailpoint.tools.GeneralException;
import sailpoint.tools.MapUtil;
import sailpoint.tools.Util;
import sailpoint.transformer.AbstractTransformer;
import sailpoint.transformer.ManagedAttributeTransformer;

/**
 * Workflow library containing utilities for identity management.
 *
 */
public class ManagedAttributeLibrary extends WorkflowLibrary {

    private static Log log = LogFactory.getLog(ManagedAttributeLibrary.class);

    public ManagedAttributeLibrary() {
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Map Model
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public Map<String,Object> getManagedAttributeModel(WorkflowContext wfc) 
        throws GeneralException {
        
        SailPointContext ctx = wfc.getSailPointContext();        
        Attributes<String,Object> args = wfc.getArguments();
               
        String managedAttributeId = Util.getString(args, "maId");

        String appId = Util.getString(args, "appId");
        if ( appId == null ) {
            throw new GeneralException("appId was null and is required.");
        }        
        Application app = ctx.getObject(Application.class, appId);
        if ( app == null ) {
            throw new GeneralException("Unable to resolve appllication for '"+appId+"'");
        }
        
        String value = Util.getString(args, "value");
        if ( value == null ) {
            throw new GeneralException("value was null and is required.");
        }
        String name = Util.getString(args, "name");
        if ( name == null ) {
            throw new GeneralException("name was null and is required.");
        }
        
        ManagedAttribute attr = null;
        if ( managedAttributeId != null ) {
            attr = ctx.getObject(ManagedAttribute.class, managedAttributeId);
        } else {
            if ( appId != null && value != null && name != null ) {
                attr = ManagedAttributer.get(ctx, appId, name, value);
                if ( attr == null ) {
                    attr = new ManagedAttribute();
                    attr.setApplication(app);
                    attr.setName(name);
                    attr.setValue(value);
                }
            } 
        }
        
        Map<String,Object> stepArgs = wfc.getStepArguments();        
        HashMap<String,Object> ops = null;
        if ( stepArgs != null ) {
            ops = new HashMap<String,Object>(stepArgs);
        } else {
            ops = new HashMap<String,Object>();
        }
        
        ManagedAttributeTransformer transformer = new ManagedAttributeTransformer(ctx, ops);
        Map<String,Object> mapModel = transformer.toMap(attr);
                
        return mapModel;
    }
    
    /**
     * Build up a provisioningfrom from the map model that has been updated.
     * 
     * The returned ProvisioningPlan will be null if nothing has changed.
     * 
     * @param wfc
     * @return
     * @throws GeneralException
     */
    @SuppressWarnings("unchecked")
    public List<ProvisioningPlan> buildPlansFromManagedAttributeModel(WorkflowContext wfc) 
        throws GeneralException {
        
        Attributes<String,Object> args = wfc.getArguments();
        Map<String,Object> identityModel = (Map<String,Object>)Util.get(args, "maModel");
        if ( identityModel == null ) {
            throw new GeneralException("Identity map model was null.");
        }
        
        Map<String,Object> stepArgs = wfc.getStepArguments();        
        HashMap<String,Object> ops = null;
        if ( stepArgs != null ) {
            ops = new HashMap<String,Object>(stepArgs);
        } else {
            ops = new HashMap<String,Object>();
        }
        
        ManagedAttributeTransformer transfomer = new ManagedAttributeTransformer(wfc.getSailPointContext(), ops);
        return transfomer.mapToPlans(identityModel, ops);        
    }    
    
    @SuppressWarnings("unchecked")
    public void executeManageAttributePlans(WorkflowContext wfc) 
        throws GeneralException {
        
        Attributes<String,Object> args = wfc.getArguments();
        
        List<ProvisioningPlan> plans = (List<ProvisioningPlan>)Util.get(args, "plans");
        if ( plans == null ) {
            throw new GeneralException("Plans to execute were null.");
        }
        // Group plans first
        plans = orderPlans(plans);
        for ( ProvisioningPlan plan : plans ) {
            WorkflowCase wfcase = launchWorkflow(wfc.getSailPointContext(), plan, args);    
            if ( wfcase.isError() ) {
                // TODO: fail if the group workflows fail? or always
                if ( plan.getAccountRequests() == null ) {
                    throw new GeneralException("Failure launch workflow!" + wfcase.getErrors());
                }
            }
        }
    }   
    
    private List<ProvisioningPlan> orderPlans(List<ProvisioningPlan> plans) {
        List<ProvisioningPlan> ordered = new ArrayList<ProvisioningPlan>();
        if ( plans == null ) 
            return null;
        
        for ( ProvisioningPlan plan : plans ) {
            if ( plan.getAccountRequests() == null ) {
                ordered.add(0, plan);
            } else 
                ordered.add(plan);            
        }
        return ordered;
    }
    
    private WorkflowCase launchWorkflow(SailPointContext context, ProvisioningPlan plan, Map<String,Object> stepArgs) 
        throws GeneralException {

        WorkflowCase wfcase = null;
        
        if ( plan != null ) {
            //log.debug( "Plan before removing: " + plan.toXml() );
            // 2016.10.19 ar There MUST be one, otherwise a group can't not created ;-)
        	// Remove the trnasformerClass and transformerOptions needed by the transformerclass since 7.0p3
        	// added in ManagedAttributeTransformer
            if( plan.getObjectRequests() != null ) {
            	for( ProvisioningPlan.ObjectRequest objReq : plan.getObjectRequests() ) {
            		ProvisioningPlan.AttributeRequest attrReq = objReq.getAttributeRequest(AbstractTransformer.ATTR_TRANSFORMER_CLASS);
            		if( null != attrReq ) {
            		   objReq.remove(attrReq);
            		}
            		attrReq = objReq.getAttributeRequest(AbstractTransformer.ATTR_TRANSFORMER_OPTIONS);
            		if( null != attrReq ) {
            		  objReq.remove(attrReq);
            		}
            	}
            }
            //log.debug( "Plan after Removing: " + plan.toXml() );
            
            Map<String,Object> args = new HashMap<String,Object>();
            if ( stepArgs != null ) 
                args.putAll(stepArgs);
            
            if ( plan.getAccountRequests() != null ) {
                // Identity change
                String identityWorkflow = Util.getString(stepArgs, "identityWorkflow");
                if ( identityWorkflow == null ) {
                    identityWorkflow = "LCM Provisioning";
                }
                args.put("identityName", plan.getNativeIdentity());
                wfcase = runWorkflow(identityWorkflow, context, plan, args);
            } else {
                // group change
                String groupWorkflow = Util.getString(stepArgs, "groupWorkflow");
                if ( groupWorkflow == null ) {
                    groupWorkflow = "Entitlement Update";
                }
                wfcase = runWorkflow(groupWorkflow, context, plan, args);
            }
        }
        return wfcase;
    }

    private WorkflowCase runWorkflow(String wfName, SailPointContext context, ProvisioningPlan plan, Map<String,Object> args) 
            throws GeneralException {
        
        Workflower workflower = new Workflower(context);
        Workflow workflow = context.getObject(Workflow.class, wfName);
        if ( workflow == null ) {
            throw new GeneralException("Unable to find workflow named '" + wfName + "'");
        }
        Map<String,Object> vars = new HashMap<String,Object>();
        if ( args != null )
            vars.putAll(args);

        vars.put("plan", plan);
        scrub(vars);

        WorkflowLaunch launch = workflower.launch(workflow, null, vars);
        WorkflowCase wfcase = launch.getWorkflowCase();
        
        return wfcase;
    }
    
    //RETHINK THIS
    private void scrub(Map<String,Object> vars) {
        vars.remove("handler");
        vars.remove("plans");
        vars.remove("workflow");
        vars.remove("wfcase");
        vars.remove("wfcontext");
        vars.remove("step");
        vars.remove("sessionOwner");
    }
}
