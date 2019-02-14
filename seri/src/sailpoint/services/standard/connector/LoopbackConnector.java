package sailpoint.services.standard.connector;

import sailpoint.api.Provisioner;
import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.connector.AbstractConnector;
import sailpoint.connector.AbstractObjectIterator;
import sailpoint.connector.Connector;
import sailpoint.connector.ConnectorException;
import sailpoint.object.Application;
import sailpoint.object.Application.Feature;
import sailpoint.object.AttributeDefinition;
import sailpoint.object.AttributeMetaData;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AbstractRequest;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.ProvisioningPlan.ObjectOperation;
import sailpoint.object.ProvisioningResult;
import sailpoint.object.QueryOptions;
import sailpoint.object.ResourceObject;
import sailpoint.object.Schema;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/** @author christian.cairney@sailpoint.com
 * 
 * IdentityIQ Loopback connector.
 *  
 *    o  Any object type to be modelled as entitlments
 *    o  Use provisioning plans instead of the API to avoid locking issues
 *    o  Schema dictated by Application schema and not hard coded 
 *    o  Delta aggregation support
 *    o  Performance improvements
 *  
 *  Class connects to IdentityIQ and aggregates Identities as accounts,
 *  and any other attribute from the identity.
 *  
 *  Default configuration has Workgroups are encapsulated as workgroups and
 *  Capabilities are brought in as is capabilities.  This can be changed
 *  by the implementer
 * 
 * */
public class LoopbackConnector extends AbstractConnector {
	
	static final Logger log = Logger.getLogger(LoopbackConnector.class);

	public final static String ATTR_NAME = "name";
	public final static String ATTR_FIRSTNAME = "firstname";
	public final static String ATTR_LASTNAME = "lastname";
	public final static String ATTR_DISPLAYNAME = "displayName";
	public final static String ATTR_WORKGROUPS = "workgroups.name";
	public final static String ATTR_CAPABILITIES = "capabilities.name";
	public final static String ATTR_INACTIVE_FLAG = "inactive";
	
	public final static String CONNECTOR_TYPE = "IdentityIQ Connector";

	public final static String APP_ATTR_COMMON_DELTA_POSTFIX = "DeltaTimeStamp";
	public final static String APP_ATTR_COMMON_ATTRIBUTE_MAP = "attributeTransformMap";
	public final static String AGGREGATION_DELTA = "deltaAggregation";
	
	public final static String TYPE_WORKGROUP = "workgroup";

	private static final Object	IIQ_OBJECT_NAME_WORKGROUP	= "Workgroup";
	private static final Object	IIQ_OBJECT_NAME_IDENTITY	= "Identity";
	
	private Map<String,String> attributeMap = null;
	
	// Application context
	private SailPointContext context = null;

	/**
	 * Constructor
	 * @param application
	 * @throws GeneralException
	 */
	public LoopbackConnector(final Application application)
			throws GeneralException {

		super(application);
		
		log.debug(String.format("Constructor: IdentityIQConnector(%s)",
				application));

		init();

	}

	/**
	 * Constructor
	 * @param application
	 * @param instance
	 * @throws GeneralException
	 */
	public LoopbackConnector(final Application application,
			final String instance) throws GeneralException {
		
		super(application, instance);
		
		log.debug(String.format("Constructor: IdentityIQConnector(%s, %s)",
				application, instance));

		init();

	}

	/**
	 * Initialise, used on either constructors
	 * 
	 * @throws GeneralException
	 */
	private void init() throws GeneralException {
		
		log.debug("Init");
		context = SailPointFactory.getCurrentContext();
		Application app = getApplication();
		attributeMap = (Map<String,String>) app.getAttributeValue(APP_ATTR_COMMON_ATTRIBUTE_MAP);
		if (attributeMap == null) {
			log.debug("Attribute map is null");
		} else {
			log.debug("Initialised attribute map: " + attributeMap.toString());
		}
		
	}

	/**
	 * Test connection. Will always return OK in this case.
	 *
	 * @throws ConnectorException
	 */

	@Override
	public void testConfiguration() throws ConnectorException {
		return;
	}

	@Override
	public String getConnectorType() {
		log.debug("Entering getConnectorType()");
		
		//
		//
		
		if (log.isDebugEnabled()) log.debug("Exiting with value " + CONNECTOR_TYPE);
		
		return CONNECTOR_TYPE;
	}

	@Override
	// TODO:  Need to fix this up so it can handle all the object types
	public List<Schema> getDefaultSchemas() {
		log.debug("Enter: getDefaultSchemas()");
		final List<Schema> schemas = new ArrayList<Schema>();

		try {
			// Fill schemas
			schemas.add(discoverSchema(TYPE_ACCOUNT,
					new HashMap<String, Object>()));
			schemas.add(discoverSchema(TYPE_GROUP,
					new HashMap<String, Object>()));
		} catch (final ConnectorException ex) {
			log.error("Error discovering schemas", ex);
		}
		return schemas;

	}

	@Override
	// TODO:  Need to fix this up so it can handle all object types.
	public Schema discoverSchema(final String objectType,	final Map<String, Object> options) throws ConnectorException {
		if (log.isDebugEnabled()) log.debug(String.format("Enter: discoverSchema(%s, %s)", objectType,
				options));
		final Schema schema = new Schema();
		schema.setObjectType(objectType);
		// Account schema
		if (TYPE_ACCOUNT.equalsIgnoreCase(objectType)) {
			log.debug("Getting account schema");
			schema.setNativeObjectType(TYPE_ACCOUNT);
			schema.setIdentityAttribute(ATTR_NAME);
			schema.setDisplayAttribute(ATTR_NAME);
			schema.setGroupAttribute(ATTR_WORKGROUPS, "true");
			schema.addAttributeDefinition(ATTR_NAME, "string", "Account name",	true);
			schema.addAttributeDefinition(Identity.ATT_FIRSTNAME, "string",	"First name", false);
			schema.addAttributeDefinition(Identity.ATT_LASTNAME, "string", "Last name",	false);
			schema.addAttributeDefinition(ATTR_DISPLAYNAME, "string","Display name", false);
			
			final AttributeDefinition capabilities = new AttributeDefinition(ATTR_CAPABILITIES, "string","IdentityIQ Capabilities", false);
			capabilities.setManaged(true);
			capabilities.setEntitlement(true);
			capabilities.setMulti(true);
			capabilities.setRequired(false);
			schema.addAttributeDefinition(capabilities);
			
			final AttributeDefinition workgroups = new AttributeDefinition(	ATTR_WORKGROUPS, "group","IdentityIQ Workgroups", false);
			workgroups.setManaged(true);
			workgroups.setEntitlement(true);
			workgroups.setMulti(true);
			workgroups.setRequired(false);
			schema.addAttributeDefinition(workgroups);
			
			try {
				log.debug(schema.toXml());
			} catch (final GeneralException ex) {
				log.error(ex);
			}
			return schema;
		}
		// Group schema
		if (TYPE_GROUP.equalsIgnoreCase(objectType)) {
			log.debug("Getting group schema");
			schema.setNativeObjectType(TYPE_GROUP);
			schema.setIdentityAttribute(ATTR_NAME);
			schema.setDisplayAttribute(ATTR_NAME);
			schema.addAttributeDefinition(ATTR_NAME, "string",
					"Workgroup name", true);
			schema.addAttributeDefinition(ATTR_DISPLAYNAME, "string",
					"Display name", false);
			try {
				log.debug(schema.toXml());
			} catch (final GeneralException ex) {
				log.error(ex);
			}
			return schema;
		}
		log.error("Unsupported schema type");
		
		return schema;
	}
	
	/**
	 * Get a single object by it's name.
	 *
	 * @param objectType
	 * @param identityName
	 * @param options
	 * @return
	 * @throws ConnectorException
	 */
	@Override
	public ResourceObject getObject(final String objectType,final String identityName, final Map<String, Object> options) throws ConnectorException {
		
		if (log.isDebugEnabled()) log.debug(String.format("Enter: getObject(%s, %s, %s)", objectType, identityName, options));

		Application app = getApplication();
		Schema schema = app.getSchema(objectType);
		
		if (schema ==  null) throw new ConnectorException("Application object " + app.getName() + " does not support object type " + objectType);
		
		ResourceObject returnObject = null;
		try {
			returnObject = createResourceObject(null, identityName, schema);
		} catch (GeneralException e) {
			log.error("Could not get resource object in getObject method",e);
		}
		
		return returnObject;
	}

	/**
	 * Search for matching objects and return an iterator.
	 *
	 * @param objectType
	 * @param filter
	 * @param options
	 * @return
	 * @throws ConnectorException
	 */
	@Override
	public AbstractObjectIterator<ResourceObject> iterateObjects(final String objectType, final Filter filter, final Map<String, Object> options) throws ConnectorException {
		
		if (log.isDebugEnabled()) log.debug(String.format("Enter: iterateObjects(%s, %s, %s)",objectType, filter, options));
		
		List<Filter> localFilters = new ArrayList<Filter>();
		if (filter != null) localFilters.add(filter);
		
		Boolean delta = false;
		Date deltaDate = null;
		Date newDeltaDate = new Date();
		String appAttrDeltaAttribute = null;

		AbstractObjectIterator<ResourceObject> it = null;

		Application app = getApplication();
		if (app == null) throw new ConnectorException("Could not get application to iterate on.");
		Schema schema = app.getSchema(objectType);
		if (schema == null) throw new ConnectorException("Could not get schema for object type " + objectType);
		String nativeObjectType = schema.getNativeObjectType();
		if (nativeObjectType == null) throw new ConnectorException("Could not find native object type from connector object type " + objectType);

		if (options != null && options.containsKey(AGGREGATION_DELTA)) {

			delta = (options.get(AGGREGATION_DELTA).toString().toLowerCase().equals("true")) ? true : false;
			if (log.isDebugEnabled()) log.debug("Delta flag has been set to " + String.valueOf(delta));
		} else {
			log.debug("Delta flag is not being used.");
		}

		if (schema != null) {
			
			appAttrDeltaAttribute = schema.getObjectType() + APP_ATTR_COMMON_DELTA_POSTFIX;
			if (log.isDebugEnabled()) log.debug("Delta attribute name for object type " + schema.getObjectType() + "is " + appAttrDeltaAttribute);
					
			if (objectType.equalsIgnoreCase(TYPE_ACCOUNT)) {

				// TODO: Filter out accounts which no longer have any entitlements

			}

			// Add object type filer if required
			Filter f = getIdentityIQObjectFilter(schema);
			if (f != null) localFilters.add(f);

			// If delta is needed then attempt to add it here
			if (delta) {
				deltaDate = app.getAttributes().getDate(appAttrDeltaAttribute);
				if (deltaDate != null) {
					// Add delta filter if need be
					localFilters.add(Filter.or(Filter.gt("modified", deltaDate), Filter.gt("created", deltaDate)));
				}
			}

			Filter localFilter = null;
			
			if (localFilters.size() > 1) {
				localFilter = Filter.and(localFilters);
			} else if (localFilters.size() == 1) {
				localFilter = localFilters.get(0);
			} else {
				localFilter = null;
			}
			try {
				if (log.isDebugEnabled()) log.debug("Filter: " + (localFilter != null ? localFilter.toXml() : "NO LOCAL FILTER"));
				
				it = new LoopbackIterator(schema, localFilter);

			}

			catch (final GeneralException ex) {
				log.error("Error getting iterator", ex);
				throw new ConnectorException(ex);
			}
		} else {
			throw new ConnectorException("Schema for object type " + objectType + " was not found in application " + app.getName());
		}
		
		// Update the app object with the new delta date
		try {
			app.setAttribute(appAttrDeltaAttribute, newDeltaDate);
			
			context.saveObject(app);
			context.commitTransaction();
		} catch (GeneralException e) {
			log.warn("Could not save application delta time stamp",
					e);
		}
		
		log.debug("Returning iterator");
		return it;
	}


	/**
	 * Provision method uses the IdentityIQ internal provisioning 
	 * methods instead of direct API calls.
	 * 
	 */
	@Override
	public ProvisioningResult provision(final ProvisioningPlan plan)
			throws ConnectorException, GeneralException {
		
		log.debug("Entering provision");

		ProvisioningResult result = new ProvisioningResult();

		if (plan != null) {

			ProvisioningPlan iiqPlan = (ProvisioningPlan) plan.deepCopy(context);
			Identity identity = context.getObjectByName(Identity.class,iiqPlan.getNativeIdentity());
			if (identity == null) throw new GeneralException("Could not find identity: " + iiqPlan.getNativeIdentity());
			
			iiqPlan.setIdentity(identity);
			
			if (log.isDebugEnabled())
				log.debug("IIQPlan:" + iiqPlan.toXml());

			List<AccountRequest> requests = iiqPlan.getAccountRequests();
			List<AccountRequest> removeRequests = new ArrayList<AccountRequest>();
			
			if (requests != null) {
				// Transform the account requests to IIQ Application
				
				Iterator<AccountRequest> itRequests = requests.iterator();
				
				while (itRequests.hasNext()) {
					
					AccountRequest ar  = itRequests.next();
					
					if (log.isDebugEnabled()) log.debug(String.format("Account request operation: %s.", ar.getOp()));

					// Change the application target to IIQ
					ar.setApplication(ProvisioningPlan.APP_IIQ);
					
					//We may need to change the provisioning plan's
					// attribute request name...
					List<AttributeRequest> attributeRequests = ar.getAttributeRequests();
					
					if (attributeRequests != null) {
						for (AttributeRequest attributeRequest : ar.getAttributeRequests()) {
							
							if (attributeMap.containsKey(attributeRequest.getName())) {
								if (log.isDebugEnabled()) log.debug("Transformed Attribute request from: " + attributeRequest.toXml() );
								attributeRequest.setName(attributeMap.get(attributeRequest.getName()));
								if (log.isDebugEnabled()) log.debug("To: " + attributeRequest.toXml() );
							}
						}
					} else {
						log.debug("No attribute requests to process");
					}
					
					// Further transforms may be necessary depending on the
					// operation
					ProvisioningPlan.ObjectOperation op = ar.getOp();

					switch (op) {

						case Disable:
							
							ar.setOp(ProvisioningPlan.ObjectOperation.Modify);
							ar.add(new AttributeRequest(ATTR_INACTIVE_FLAG,ProvisioningPlan.Operation.Set,"true"));
							
							break;
					
						case Enable:
							
							ar.setOp(ProvisioningPlan.ObjectOperation.Modify);
							ar.add(new AttributeRequest("inactive",ProvisioningPlan.Operation.Set,""));
							break;
						
						case Create:
							// Create provisioning plan should be transformed to 
							// a modify and the native identity set to the identities
							// name().
							
							log.debug("Transforming a Create provisioning plan to Modify");
							
							ar.setOp(ProvisioningPlan.ObjectOperation.Modify);
							ar.setNativeIdentity(iiqPlan.getNativeIdentity());
							result.setStatus(ProvisioningResult.STATUS_COMMITTED);
							
							setAccountRequestStatus(ar, ProvisioningResult.STATUS_COMMITTED);

							
							break;
						case Modify:
	
							// Send modify as is to the provisioner
							result.setStatus(ProvisioningResult.STATUS_COMMITTED);
							setAccountRequestStatus(ar, ProvisioningResult.STATUS_COMMITTED);
							
							break;
	
						case Set:
							
							// Send set as is to the provisioner
							result.setStatus(ProvisioningResult.STATUS_COMMITTED);
							setAccountRequestStatus(ar, ProvisioningResult.STATUS_COMMITTED);
							
						case Delete:
	
							// Deletes are not supported, so we should remove this plan							
							removeRequests.add(ar);
							
							break;
						default:
	
							String unsupportedOp = String.format("Unsupported operation: %s", op);
							log.debug(unsupportedOp);
							result.addError(unsupportedOp);

							// We do not know what this request is, so remove it!
							removeRequests.add(ar);
							
							break;
					}
					
					
				}
			} else {
				throw new ConnectorException(
						"No account request found for operation");
			}

			// Ok, if this isn't something we are managing ourselves, then we need
			// to send it off to the provisioner as it should be now transformed
			// to target IdentityIQ internal application.
			
			
			if (iiqPlan.getAccountRequests()!= null && iiqPlan.getAccountRequests().size() > 0) {

				
				// Remove any surplus request objects that we do not need.
				for (AccountRequest ar : removeRequests) {
					if (log.isDebugEnabled()) log.debug("Removing account request type from plan:" + ar.getType());
					iiqPlan.remove(ar);
				}
				
				if (log.isDebugEnabled())
					log.debug(String.format("Transformed Plan: %s", iiqPlan.toXml()));
				
				Provisioner provisioner = new Provisioner(context);
				
				log.trace("Compiling plan");
				
				Attributes<String,Object> options = new Attributes<String, Object>();
				
				provisioner.setNoLocking(true);		
				provisioner.setSource(iiqPlan.getSource());
				//provisioner.setOptimisticProvisioning(true);
				
				// Compile the plan and off we go
				provisioner.compile(iiqPlan, options);
				

				log.trace("Executing provisioner");
				provisioner.execute();
				log.trace("Provisioner executed");

				
				result.setObject(getObject(this.TYPE_ACCOUNT, plan.getNativeIdentity(), null));
				result.setStatus(ProvisioningResult.STATUS_COMMITTED);
				
				if (log.isDebugEnabled()) log.debug("Setting result object: " + result.toXml());
				
			} else {

				if (result.getErrors() == null|| result.getErrors().size() == 0) {
					result.setStatus(ProvisioningResult.STATUS_COMMITTED);
				} else {
					result.setStatus(ProvisioningResult.STATUS_FAILED);
				}
			}

		} else {
			throw new ConnectorException(
					"No plan found for requested operation");
		}


		plan.setResult(result);

		log.debug("Exiting provision");
		
		return result;
	}
	
	
	/**
	 * Set the account status based on the operation status
	 * 
	 * @param ar
	 * @param status
	 */
	private void setAccountRequestStatus(AbstractRequest ar, String status) {
		
		ProvisioningResult result = new ProvisioningResult();
		result.setStatus(status);
		setAccountRequestStatus(ar, result);
		
	}
	
	/**
	 * Set the account status based on a ProvisioningResult
	 * @param ar
	 * @param result
	 */
	private void setAccountRequestStatus(AbstractRequest ar, ProvisioningResult result) {
		
		ar.setResult(result);
		if (ar.getAttributeRequests() != null) {
			for (AttributeRequest attr : ar.getAttributeRequests()) {
				attr.setResult(result);
			}
		}
	}
	

	/**
	 * Create a resource object from an object request
	 *
	 * @param identity
	 * @param objectType
	 * @return
	 * @throws GeneralException 
	 */
	ResourceObject createResourceObject(String id, String name, Schema objectSchema) throws GeneralException {
		
		if (log.isDebugEnabled()) log.debug(String.format("Entering createResourceObject ID=%s, Name=%s and type=%s", id, name, objectSchema.toXml()));
		
		ResourceObject resourceObject = new ResourceObject();
		
		// Work out the default filtering options
		List<Filter> filterList = new ArrayList<Filter>();
		Class objectClass = null;
	
		// See if the object type needs an additional filter then
		// if required, add it to the filter list
		Filter f = getIdentityIQObjectFilter(objectSchema);
		if (f != null) filterList.add(f);
	
		if (id != null) filterList.add(Filter.eq("id", id));
		if (name != null) filterList.add(Filter.eq("name", name));
		
		Filter filter = Filter.and(filterList);
		QueryOptions qo = new QueryOptions(filter);
		
		//Application app = this.getApplication();

		Class nativeObjectClass = getIdentityIQObjectType(objectSchema);
		resourceObject.setObjectType(objectSchema.getObjectType());

		// Default flags
		resourceObject.setAttribute(Connector.ATT_IIQ_DISABLED, "false");
		
		List<String> attributeNames = objectSchema.getAttributeNames();
		Iterator<Object[]> it = context.search(nativeObjectClass, qo, attributeNames);
		
		int entitlementCount = 0;
		
		if (it.hasNext()) {
			
			while (it.hasNext()) {
				
				Object[] values = it.next();
				for (int i=0; i < values.length; i++) {
					
					String attributeName = attributeNames.get(i);
		
					Object value = values[i];
					AttributeDefinition attrDef = objectSchema.getAttributeDefinition(attributeName);
					
					if (log.isDebugEnabled()) log.debug("Transforming field " + attributeName + "=" + value);
					
					if (attributeName.equals(objectSchema.getIdentityAttribute())) {
						resourceObject.setIdentity((String) value);
					} else if (attributeName.equals(objectSchema.getDisplayAttribute())) {
						resourceObject.setDisplayName((String) value);
					} else {
						
						if (attrDef.isMulti()) {
							
							List fieldValues = (List) resourceObject.get(attributeName);
							
							if (fieldValues == null) {
								fieldValues = new ArrayList();
								resourceObject.setAttribute(attributeName, fieldValues);
							}
							if (value != null && !fieldValues.contains(value)) fieldValues.add(value);
							
						} else {
							if (value != null) resourceObject.setAttribute(attributeName, value);
						}
						
						// Determine special attributes for account types
						if (objectSchema.getObjectType().equalsIgnoreCase(TYPE_ACCOUNT)) {
							
							if (attributeName.equals(ATTR_INACTIVE_FLAG)) {
								resourceObject.setAttribute(Connector.ATT_IIQ_DISABLED, value);
							}
						}
					}
					
					
					if (attrDef.isEntitlement() && value != null) entitlementCount++;
				}
			}
			
			
		} else {
			log.error("Ambiguous filter did not return any records: " + filter.toXml());
			throw new GeneralException("Ambiguous filter did not return any identity in createResourceObjectFromIdentity");
		}
	
		if (log.isTraceEnabled()) log.trace(String.format("Resource object: %s", resourceObject.toXml()));	
		
		return resourceObject;
	}

	/**
	 * 
	 */
	@Override
	protected void finalize() throws Throwable {
		log.debug("finalize");
		super.finalize();
	}

	/**
	 * 
	 * @author christian.cairney
	 *
	 */
	private class LoopbackIterator extends AbstractObjectIterator<ResourceObject>  {

		private final Iterator<Object[]> iter;
		private final Class nativeObjectType;
		private final Schema objectSchema;

		// private SailPointContext context;
		// private boolean cleanContext = false;

		public LoopbackIterator(Schema schema,  Filter filt) throws GeneralException {
			
			super(schema);
			log.debug("Entering IiqWorkgroupConnectorIterator");
			
			objectSchema = schema;
			
			final QueryOptions qo = new QueryOptions();
			qo.setDistinct(true);
			if (filt != null) qo.add(filt);
			qo.setResultLimit(0);

			nativeObjectType = getIdentityIQObjectType(schema);
			Iterator<Object[]> it = context.search(nativeObjectType, qo, "id");
			
			List<Object[]>  iterator = new ArrayList<Object[]> ();
			
			// We have to copy the iterator otherwise the calling method
			// we iterates through this has a nasty habit of closing the
			// iterator at 100 objects... don't know why yet
			//
			// TODO:  	find a way of removing this code, luckily it does
			//			not perform to badly for 1000's of objects.
			//
			
			while (it.hasNext()) {
				iterator.add(it.next());
			}
			
			iter = iterator.iterator();
			
			log.debug("Exiting IiqConnectorIterator");

		}

		@Override
		public boolean hasNext() {
			
			log.debug("Entering hasNext()");
			boolean hasNext = iter.hasNext();
			
			if (log.isDebugEnabled() ) log.debug("Exiting hasNext() = " + String.valueOf(hasNext));
			
			return hasNext;
		}

		@Override
		public sailpoint.object.ResourceObject next() {
			
			
			log.debug("Entering Iterator next()");
			
			sailpoint.object.ResourceObject resourceObj = null;
			
			if (iter.hasNext()) {
				
				log.debug("Iterator has next()");
				
				final Object[] item = iter.next();				
				final String id = (String) item[0];
				
				if (log.isDebugEnabled()) log.debug("Iterating on ID=" + id); 

				try {
					resourceObj = createResourceObject( id, null, objectSchema);
				} catch (GeneralException e) {
					log.error("Could not crate resource object, returning null", e);
				}
				
				return resourceObj;
			}
			/*
			 * } catch (final GeneralException ex) {
			 * log.error("Error getting next item", ex); }
			 */
			
			if (log.isTraceEnabled()) {

			}
			
			log.debug("Exiting Iterator next()");
			return resourceObj;
		}

		@Override
		public void close() {
			// Avoid memory leaks by flushing the iterator
			Util.flushIterator(iter);
			log.debug("iterator closed");
		}

		@Override
		protected void finalize() throws Throwable {
			log.debug("finalize");
			super.finalize();

		}
	}
	
	/**
	 * From the application schema, get the object types name and return the
	 * IdentityIQ sailpoint object class to query against.
	 * 
	 * @param schema
	 * @return
	 * @throws GeneralException
	 */
	private Class getIdentityIQObjectType(Schema schema) throws GeneralException {
		
		Class nativeObjectType;
		
		try {
			
			String nativeObjectTypeName = schema.getNativeObjectType();
			if (nativeObjectTypeName.equals(IIQ_OBJECT_NAME_WORKGROUP)) {
				nativeObjectTypeName = "sailpoint.object.Identity";
			} 

			nativeObjectType = Class.forName( (nativeObjectTypeName.startsWith("sailpoint.object") ? nativeObjectTypeName : "sailpoint.object." + nativeObjectTypeName ));
			
		} catch (ClassNotFoundException e) {
			throw new GeneralException("Could not find object class " + schema.getNativeObjectType());
		}
		return nativeObjectType;
		
	}
	
	/**
	 * If an object filter is needed for the given object type
	 * then this function will return it, otherwise expect
	 * null.
	 * 
	 * @param schema
	 * @return
	 */
	private Filter getIdentityIQObjectFilter(Schema schema) {
		
		Filter filter = null;
		if (schema.getNativeObjectType().equals(IIQ_OBJECT_NAME_IDENTITY)) {
			filter = Filter.ne("workgroup", true);
		} else if (schema.getNativeObjectType().equals(IIQ_OBJECT_NAME_WORKGROUP)) {
			filter = Filter.eq("workgroup", true);
		}
		return filter;
		
	}
	
	public List<Feature> getSupportedFeatures() {
		
		List<Feature> features = new ArrayList<Feature>();

		features.add(Application.Feature.SYNC_PROVISIONING);
		features.add(Application.Feature.PROVISIONING);
		features.add(Application.Feature.SEARCH);

		return features;
		
	}

}
