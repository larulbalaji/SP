package sailpoint.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.AccountGroupService;
import sailpoint.api.ManagedAttributer;
import sailpoint.api.SailPointContext;
import sailpoint.connector.Connector;
import sailpoint.connector.ConnectorException;
import sailpoint.connector.ConnectorFactory;
import sailpoint.connector.ObjectNotFoundException;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Difference;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentityEntitlement;
import sailpoint.object.Link;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AbstractRequest;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.ObjectOperation;
import sailpoint.object.ProvisioningPlan.ObjectRequest;
import sailpoint.object.ProvisioningPlan.Operation;
import sailpoint.object.QueryOptions;
import sailpoint.object.ResourceObject;
import sailpoint.tools.GeneralException;
import sailpoint.tools.MapUtil;
import sailpoint.tools.Util;

/**
 */
public class ManagedAttributeTransformer extends AbstractTransformer<ManagedAttribute> {
    
    private static Log log = LogFactory.getLog(ManagedAttributeTransformer.class);
    
    SailPointContext context;
    
    public ManagedAttributeTransformer(SailPointContext ctx, Map<String,Object> optMap) {
        context = ctx;
        //setOptions(optMap);
    }
    
    // TODO
    private final String MEMBER_ATTRIBUTE = "memberOf";
        
    @SuppressWarnings("rawtypes")
	public Map<String, Object> toMap(ManagedAttribute group) throws GeneralException {
        Map<String, Object> retMap = new HashMap<String, Object>();
        if (group == null) {
            return retMap;
        }        
        
        Map<String,Object> sys = new HashMap<String,Object>();
        appendBaseSailPointObjectInfo(group, sys);
        
        retMap.put("sys", sys);
        Attributes<String,Object> attrs = group.getAttributes();
        if ( attrs != null ) {
            retMap.putAll(attrs);
            // Sean K - Allow parent groups to be in model
            //
            //attrs.remove(MEMBER_ATTRIBUTE);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("returning group map model: " + retMap);
        }
        MapUtil.put(retMap, "identityMembership", getIdentityMembership(group) );        
        MapUtil.put(retMap, "sys.nativeIdentity", group.getValue());
        MapUtil.put(retMap, "sys.attribute", group.getAttribute());
        
        Application app = group.getApplication();
        if ( app != null ) {
            MapUtil.put(retMap, "sys.appName", app.getName());
            MapUtil.put(retMap, "sys.appId", app.getId());
        }
        
        Identity owner = group.getOwner();
        if ( owner != null ) {
            MapUtil.put(retMap, "sys.owner", owner.getId());
        }
        
        String type = group.getType();
        if ( type != null ) {
            MapUtil.put(retMap, "sys.type", type);
        }
        
        // Sean K - Get parent groups (i.e. inheritance)
        List<ManagedAttribute> parents = group.getInheritance();
        if (null != parents && !parents.isEmpty()) {
        	List memberOf = new ArrayList();
        	for (ManagedAttribute parent : parents) {
        		memberOf.add(parent.getId());
        	}
        	MapUtil.put(retMap, MEMBER_ATTRIBUTE, memberOf);
        }
        
        // achimr - add transformer class is not automatically added by the AbstractTransformer.
        //          7.0p3 bail out on this
        MapUtil.put(retMap, ATTR_TRANSFORMER_CLASS, this.getClass().getName());
        MapUtil.put(retMap, ATTR_TRANSFORMER_OPTIONS, "" );
        
        return retMap;
    }
    
    private List<String> getIdentityMembership(ManagedAttribute managedAttribute) 
            throws GeneralException {

        if ( managedAttribute == null || managedAttribute.getId() == null ) 
            return null;

        AccountGroupService svc = new AccountGroupService(context);
        QueryOptions qo = svc.getMembersQueryOptions(managedAttribute);
        
        return getMembers(qo);
    }
       
    private List<String> getMembers(QueryOptions qo) throws GeneralException {
        List<String> list = new ArrayList<String>();
        
        Iterator<Object[]> rows = context.search(IdentityEntitlement.class, qo, "identity.id");
        if ( rows != null ) {
            while ( rows.hasNext() ) {
                Object[] row = rows.next();
                if ( row != null ) {
                    if ( row.length > 0 ) {
                        String id = (String)row[0];
                        if ( id != null )
                            list.add(id);
                    }
                }
            }
        }
        return Util.size(list) > 0 ? list : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    // Map Model to ProvisioningPlan 
    //
    ///////////////////////////////////////////////////////////////////////////
    
    ManagedAttribute cachedCurrent = null;
    
    private ManagedAttribute getCurrent(Map<String,Object> groupModel) 
        throws GeneralException {
        
        if ( cachedCurrent != null ) {
            return cachedCurrent;            
        }
        
        String id = (String)MapUtil.get(groupModel, "sys.id");
        if ( id != null ) {
             cachedCurrent = context.getObject(ManagedAttribute.class, id);    
        }
        return cachedCurrent;
    }
        
    public List<ProvisioningPlan> mapToPlans(Map<String,Object> groupModel, HashMap<String,Object> ops) 
        throws GeneralException {
        
        List<ProvisioningPlan> plans = new ArrayList<ProvisioningPlan>();
        
        ProvisioningPlan groupPlan = getGroupPlan(groupModel);
        if ( groupPlan != null ) {
            plans.add(groupPlan);
        }
        
        List<ProvisioningPlan> identityPlans = getIdentityPlans(groupModel, ops);
        if ( identityPlans != null ) {
            plans.addAll(identityPlans);
        }
        return Util.size(plans) > 0 ? plans : null;
    }
        
    private ProvisioningPlan getGroupPlan(Map<String,Object> groupModel) 
        throws GeneralException {
        
        ProvisioningPlan plan = null;
        if ( groupModel == null )
            return plan;
        
        ManagedAttribute current = getCurrent(groupModel);
        
        Attributes<String,Object> currentAttrs = (current != null ) ? current.getAttributes() : new Attributes<String,Object>();
        
        String appId = (String)MapUtil.get(groupModel, "sys.appId");
        String appName = (String)MapUtil.get(groupModel, "sys.appName");

        Application app = null;
        if ( appId != null ) 
            context.getObject(Application.class, appId);
        if ( app == null ) {
            if ( appName != null )
                app = context.getObject(Application.class, appName);
        }
                
        String nativeIdentity = (String)MapUtil.get(groupModel, "sys.nativeIdentity");

        plan = new ProvisioningPlan();
        
        ObjectRequest req = new ObjectRequest();
        req.setType("group");
        req.setApplication(appName);
        req.setNativeIdentity(nativeIdentity);
        
        String maId = (String)MapUtil.get(groupModel, "sys.id");
        
        Connector connector = ConnectorFactory.getConnector(app, null);
        
        ResourceObject ro = null;
        try {
            ro = connector.getObject(Connector.TYPE_GROUP, nativeIdentity, null);
        } catch ( ObjectNotFoundException nf ) {
          // no problem... 
        } catch(ConnectorException c) {
            throw new GeneralException("Problem trying to fetch object from connector!" + nativeIdentity);
        } 
        
        if ( maId == null || ro == null ) {
            req.setOp(ObjectOperation.Create);
        } else {
            req.setOp(ObjectOperation.Modify);
        }   
        
        Set<String> keys = groupModel.keySet();
        if ( keys != null ) {
        	for ( String key : keys ) {
        		if ( key != null ) {
        			if ( key.equals("identityMembership") || key.equals("sys") )
        				continue;                    
        			Object value = groupModel.get(key);
        			Object currentValue = currentAttrs.get(key);
        			Difference diff = Difference.diff(currentValue, value);                    
        			if ( diff != null ) {
        				AttributeRequest attr = new AttributeRequest();
        				attr.setName(key);
        				attr.setValue(value);
        				attr.setOperation(Operation.Set);
        				req.add(attr);
        				// Sean K - [Workaround] Clean up inheritance to reflect what we are provisioning
        				if (key.equals(MEMBER_ATTRIBUTE)) {
        					List<String> parents = (List<String>) value;
        					List<ManagedAttribute> newParents = new ArrayList<ManagedAttribute>();
        					if (null != parents) {
        						for (String p : parents) {
        							ManagedAttribute ma = ManagedAttributer.get(context, appId, MEMBER_ATTRIBUTE, p);
        							newParents.add(ma);
        						}
        					}
        					if (req.getOp().equals(ObjectOperation.Modify)) {
        						current.setInheritance(newParents);
        					} else {
        						// What do we do here; MA will be created as a result of plan submission as well
        					}
        				}
        			}
        		}
        	}
        }
        
        //
        // check owner
        //
        String owner = (String) MapUtil.get(groupModel, "sys.owner");
        String ownerId = null;
        Identity currentOwner = ( current != null ) ? current.getOwner() : null;
        if ( currentOwner != null ) {
            ownerId = currentOwner.getId();            
        }
        doInternal(req, owner, ownerId,"sysOwner" );
        
        //
        // For creates the attribute will be set
        //
        String attribute = (String) MapUtil.get(groupModel, "sys.attribute");
        String currentAttribute = ( current != null ) ? current.getAttribute() : null; 
        doInternal(req, attribute, currentAttribute,"sysAttribute" );
        
        //sysManagedAttributeType
        String maType = (String) MapUtil.get(groupModel, "sys.type");
        String currentMaType = ( current != null && current.getType() != null ) ? current.getType().toString() : null;
        doInternal(req, maType, currentMaType, "sysManagedAttributeType" );
        
        if ( Util.size(req.getAttributeRequests()) == 0 ) {
            plan = null;
        } else {
            plan.addRequest(req);
        }
        return plan;
    }
    
    private void doInternal(AbstractRequest req, Object newValue, Object currentValue,  String wfAttr) {
        Difference attrDiff = Difference.diff(newValue, currentValue);
        if ( attrDiff != null ) {
            AttributeRequest attr = new AttributeRequest();
            attr.setName(wfAttr);
            attr.setValue(newValue);
            attr.setOperation(Operation.Set);
            req.add(attr);
        }
    }
    
    private List<ProvisioningPlan> getIdentityPlans(Map<String,Object> groupModel, HashMap<String,Object> ops)
        throws GeneralException {
        
        List<ProvisioningPlan> plans = new ArrayList<ProvisioningPlan>();
        List<String> currentMemberShip = null;
        
        List<String> modelMembership = (List<String>)MapUtil.get(groupModel, "identityMembership");
        ManagedAttribute current = getCurrent(groupModel);
        if ( current != null ) {
            currentMemberShip = getIdentityMembership(current);
        }
        
        Difference diff = Difference.diff(currentMemberShip, modelMembership);
        if ( diff != null ) {
            List<String> added = diff.getAddedValues();
            if ( Util.size(added) > 0  ) {
                List<ProvisioningPlan> addPlans = getPlans(groupModel, added, true);
                if ( Util.size(addPlans) > 0 )
                    plans.addAll(addPlans);
            }
            List<String> removed = diff.getRemovedValues();
            if ( Util.size(removed) > 0  ) {
                List<ProvisioningPlan> removePlans = getPlans(groupModel, removed, false);
                if ( Util.size(removePlans) > 0 )
                    plans.addAll(removePlans);
            }
        }
        return plans;
    }
    
    private List<ProvisioningPlan> getPlans(Map<String,Object> groupModel, List<String> values, boolean add) 
        throws GeneralException {
        
        if ( values == null) 
            return null;

        String appName = (String)MapUtil.get(groupModel, "sys.appName");
        List<ProvisioningPlan> plans = new ArrayList<ProvisioningPlan>();
        for ( String value : values ) {

            String name = getIdentityName(value);
            if ( name == null ) continue;
            
            ProvisioningPlan plan = new ProvisioningPlan();
            plan.setNativeIdentity(name);
            
            AccountRequest acct = new AccountRequest();
            acct.setNativeIdentity(getNativeIdentity(value, appName));
            acct.setApplication(appName);
            acct.setOperation(AccountRequest.Operation.Modify);
            
            AttributeRequest attr = new AttributeRequest();
            if ( add ) {
                attr.setOp(Operation.Add);
            } else {
                attr.setOp(Operation.Remove);
            }
            attr.setValue(MapUtil.get(groupModel, "sys.nativeIdentity"));
            attr.setName((String)MapUtil.get(groupModel, "sys.attribute"));
            
            acct.add(attr);
            plan.add(acct);
            
            plans.add(plan);
        }
        return Util.size(plans) > 0 ? plans : null;
    }
    
    private String getNativeIdentity(String identityId, String appName) throws GeneralException {
        QueryOptions ops = new QueryOptions();
        ops.add(Filter.eq("identity.id",identityId ));
        ops.add(Filter.eq("application.name", appName));
        
        String ni = null;
        Iterator<Object[]> objs = context.search(Link.class, ops, "nativeIdentity");
        if ( objs != null ) {
            while ( objs.hasNext() ) {
                Object[] row = objs.next();
                if ( row != null ) {
                    if ( row.length > 0 ) {
                        ni = (String)row[0];
                    }
                }
            }
        }
        return ni;        
    }
    
    private String getIdentityName(String id) throws GeneralException {
        String name = null;
        
        QueryOptions ops = new QueryOptions();
        ops.add(Filter.eq("id",id ));
        
        Iterator<Object[]> objs = context.search(Identity.class, ops, "name");
        if ( objs != null ) {
            while ( objs.hasNext() ) {
                Object[] row = objs.next();
                if ( row != null ) {
                    if ( row.length > 0 ) {
                        name = (String)row[0];
                    }
                }
            }
        }
        return name;
    }
}
