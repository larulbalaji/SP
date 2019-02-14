package sailpoint.seri.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.IdentityHistoryService;
import sailpoint.api.ManagedAttributer;
import sailpoint.api.SailPointContext;
import sailpoint.object.AbstractCertifiableEntity;
import sailpoint.object.Application;
import sailpoint.object.Bundle;
import sailpoint.object.Certifiable;
import sailpoint.object.Certification;
import sailpoint.object.CertificationEntity;
import sailpoint.object.CertificationItem;
import sailpoint.object.EntitlementGroup;
import sailpoint.object.Entitlements;
import sailpoint.object.Filter;
import sailpoint.object.GroupDefinition;
import sailpoint.object.Identity;
import sailpoint.object.IdentityHistoryItem;
import sailpoint.object.Link;
import sailpoint.object.ManagedAttribute;
import sailpoint.object.PolicyViolation;
import sailpoint.object.QueryOptions;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class ExclusionRuleUtility {
	
	private static Log log = LogFactory.getLog(ExclusionRuleUtility.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void populateEntitlementsMap(String app, String name,
			String value, Map entitlementsMap, Map state) {
		Map appMap;
		if (entitlementsMap.get(app) != null) {
			appMap = (Map<String,List<String>>) entitlementsMap.get(app);
		} else {
			appMap = new HashMap<String,List<String>>();
		}
		List<String> aAttrTypeList;
		if (appMap.get(name) != null) {

			aAttrTypeList = (List<String>) appMap.get(name);
		} else {
			aAttrTypeList = new ArrayList<String>();
		}

		if (aAttrTypeList.contains(value) == false) {
			aAttrTypeList.add(value);
		}
		appMap.put(name, aAttrTypeList);
		entitlementsMap.put(app, appMap);
		log.debug("EXISITING LIST:  " + state.get("populations"));
		state.put("populations", entitlementsMap);

	}

	@SuppressWarnings("rawtypes")
	public static void parseCompositeFilter(
			Filter.CompositeFilter aCompositeFilter, Map vals,
			Map entitlementsMap, Map state) {
		List aCompositeFilterChildrenList = aCompositeFilter.getChildren();
		for (int z = 0; z < aCompositeFilterChildrenList.size(); z++) {
			if (aCompositeFilterChildrenList.get(z) instanceof Filter.CompositeFilter) {
				parseCompositeFilter(
						(Filter.CompositeFilter) aCompositeFilterChildrenList
								.get(z),
						vals, entitlementsMap, state);
			} else if (aCompositeFilterChildrenList.get(z) instanceof Filter.LeafFilter) {
				parseLeafFilter(
						(Filter.LeafFilter) aCompositeFilterChildrenList.get(z),
						vals, entitlementsMap, state);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void parseLeafFilter(Filter.LeafFilter aLeafFilter, Map vals,
			Map entitlementsMap, Map state) {

		if (aLeafFilter.getCollectionCondition() != null) {
			parseCompositeFilter(aLeafFilter.getCollectionCondition(), vals,
					entitlementsMap, state);
		} else {
			log.debug("vals:  " + vals);
			if (vals == null) {
				vals = new HashMap<String,String>();
			}
			if (aLeafFilter.getProperty().equalsIgnoreCase("application.name")) {
				vals.put("app", (String) aLeafFilter.getValue());
			} else if (aLeafFilter.getProperty().equalsIgnoreCase("name")) {
				vals.put("name", (String) aLeafFilter.getValue());
			} else if (aLeafFilter.getProperty().equalsIgnoreCase("value")) {
				vals.put("value", (String) aLeafFilter.getValue());
			}

			/**
			 * log.debug("True Expression:  "+
			 * aTrueFilter.getExpression());
			 * log.debug("True Expression:  "+
			 * aTrueFilter.getOperation());
			 * log.debug("True property:  "+
			 * aTrueFilter.getProperty()); log.debug("True value:  "+
			 * aTrueFilter.getValue());
			 **/

			if (vals.get("value") != null && vals.get("name") != null
					&& vals.get("app") != null) {
				populateEntitlementsMap((String) vals.get("app"),
						(String) vals.get("name"), (String) vals.get("value"),
						entitlementsMap, state);
				vals.put("app", null);
				vals.put("name", null);
				vals.put("value", null);
				log.debug("vals IS NULL AAGAIN:  " + vals);
				log.debug("                 ");
			}

		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean filterAccountTypes(SailPointContext context,
			List itemsToExclude, Certifiable item, String attributeName,
			Object attributeValue, boolean excludeOnMatch)
			throws GeneralException {

//		CertificationItem tmp = null;

		QueryOptions ops = new QueryOptions();
		Filter[] filters = new Filter[2];
		filters[0] = Filter.eq("nativeIdentity",
				((Entitlements) item).getNativeIdentity());
		filters[1] = Filter.eq("application.name",
				((Entitlements) item).getApplicationName());
		ops.add(filters);

		Iterator acLinks = context.search(Link.class, ops);

		if (acLinks != null) {
			while (acLinks.hasNext()) {
				Link acLink = (Link) acLinks.next();

				Map extendedMap = acLink.getExtendedAttributes();
				/*log.debug("Extended Map:  " + extendedMap);
				log.debug("Attribute Name:  " + attributeName);
				log.debug("Extended Map Attribute Name:  "
						+ extendedMap.get(attributeName));
				log.debug("       "); */
				if (extendedMap != null) {
					boolean match = false; // use the match = true variable to
											// exclude if excludeOnMatch =
											// false. In that case we actually
											// INCLUDE all non matches.

					if (extendedMap.get(attributeName) != null) {
						Object value = (Object) extendedMap.get(attributeName);
						//log.debug(" VALUE IS:  " + value);
						if (value instanceof Boolean) {
							if (((Boolean) value) == Util.otob(attributeValue)) {
								match = true;
							}
						} else if (value instanceof String) {
							if (value != null
									&& ((String) value)
											.equalsIgnoreCase((String) attributeValue)) {
								match = true;
							}
						} else if (value instanceof Identity) {
						//	log.debug("VALUE IS AN IDENTITY");
							if (value != null
									&& value == (Identity) context.getObject(
											Identity.class,
											(String) attributeValue)) {
									match = true;
							}

						}

					}
					if (excludeOnMatch && match == true) {
						itemsToExclude.add(item);
						match = false;
						return true;
					}

					if (excludeOnMatch == false && match == false) {
						match = false;
						itemsToExclude.add(item);
						return true;
					}

				}
			}
		}
		return false;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean filterApplicationTypes(SailPointContext context,
			List itemsToExclude, Certifiable item, String attributeName,
			Object attributeValue, boolean excludeOnMatch)
			throws GeneralException {

		Application application = context.getObjectByName(Application.class,
				((Entitlements) item).getApplicationName());

		if (application != null) {

			if (application != null
					&& ((Map) application.getExtendedAttributes()) != null) {

				boolean match = false; // use the match = true variable to
										// exclude if excludeOnMatch = false. In
										// that case we actually INCLUDE all non
										// matches.
				if (((Map) application.getExtendedAttributes())
						.get(attributeName) != null) {
					Object value = (Object) ((Map) application
							.getExtendedAttributes()).get(attributeName);

					if (value instanceof Boolean) {
						if (((Boolean)value) == Util.otob(attributeValue)) {
							match = true;

						}
					} else if (value instanceof String) {

						if (value != null
								&& ((String) value)
										.equalsIgnoreCase((String) attributeValue)) {
							match = true;
						}
					} else if (value instanceof Identity) {
						if (value != null
								&& value == (Identity) context
										.getObject(Identity.class,
												(String) attributeValue)) {
							match = true;
						}

					}
				}

				if (excludeOnMatch && match == true) {
					//log.debug("removing item");
					itemsToExclude.add(item);
					match = false;
					return true;
				}

				if (excludeOnMatch == false && match == false) {
					match = false;
					itemsToExclude.add(item);
					return true;
				}

			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean excludePreviouslyCertifiedItems(
			SailPointContext context, List itemsToExclude, Certifiable item,
			Identity id, Certification certification) throws GeneralException {
		CertificationItem tmp = null;
		IdentityHistoryService historyService = new IdentityHistoryService(
				context);

		if (item instanceof Entitlements) {
			tmp = new CertificationItem((Entitlements) item,
					Certification.EntitlementGranularity.Value);
			tmp.setParent(new CertificationEntity(
					(AbstractCertifiableEntity) id));
			tmp.getParent().setCertification(certification);

			if (historyService.getLastDecision(id.getId(), tmp) != null) {
				itemsToExclude.add(item);
				return true;
			}
		} else if (item instanceof Bundle) {
			if (historyService.getLastRoleDecision(id.getId(),
					((Bundle) item).getName()) != null) {
				itemsToExclude.add(item);
				return true;
			}
		} else if (item instanceof PolicyViolation) {
			if (historyService.getLastViolationDecision(id.getId(),
					(PolicyViolation) item) != null) {
				itemsToExclude.add(item);
				return true;
			}
		}

		return false;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean includeOnlyEntitlmentsDefinedInPopulation(
			SailPointContext context, List itemsToExclude,
			String populationName, Certifiable item, boolean excludeOnMatch,
			Map state) throws GeneralException {

		Map entitlementsMap = null;
		if (state.get("populations") != null) {
			entitlementsMap = (Map) state.get("populations");
		} else {
			entitlementsMap = new HashMap();
			Map vals = new HashMap();

			GroupDefinition groupObject = context.getObjectByName(
					GroupDefinition.class, populationName);

			// first we get the filter.
			Filter.CompositeFilter aCompositeFilter = (Filter.CompositeFilter) groupObject
					.getFilter();

			/**
			 * now we iterate the filter to get the groups we wish to either
			 * include or exclude. We do not care about and's and or's at this
			 * point, so we just get a big bad list of the groups and
			 * applications upon which we wish to filter.
			 **/
			parseCompositeFilter(aCompositeFilter, vals, entitlementsMap, state);
		}

		if (item instanceof Entitlements) {

			//log.debug("Its an ENTITLEMENT: " + excludeOnMatch);

			EntitlementGroup myEnt = (EntitlementGroup) item;
			String appName = myEnt.getApplicationName();

			if (excludeOnMatch == false) {

				if ((Map) state.get("populations") == null
						|| ((Map) state.get("populations")).get(appName) == null
						|| myEnt.getAttributes() == null
						|| ((Map) ((Map) state.get("populations")).get(appName))
								.get(myEnt.getAttributeNames().get(0)) == null
						|| ((List) ((Map) ((Map) state.get("populations"))
								.get(appName)).get((String) myEnt
								.getAttributeNames().get(0))).contains(myEnt
								.getAttributes().getString(
										myEnt.getAttributeNames().get(0))) == false) {

					log.debug("You not in the list. Excluding");
					itemsToExclude.add(item);
					return true;

				}

			} else {
				if (((Map) state.get("populations")).get(appName) != null
						&& ((Map) state.get("populations")).get(appName) != null
						&& myEnt.getAttributes() != null
						&& ((Map) ((Map) state.get("populations")).get(appName))
								.get(myEnt.getAttributeNames().get(0)) != null
						&& ((List) ((Map) ((Map) state.get("populations"))
								.get(appName)).get(myEnt.getAttributeNames()
								.get(0))).contains(myEnt.getAttributes()
								.getString(myEnt.getAttributeNames().get(0))) == true) {

					log.debug("You ARE in the list. Excluding");
					itemsToExclude.add(item);
					return true;

				}

			}
		}
		return false;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean matchByManagedAttributeExtendedAttribute(
			SailPointContext context, List itemsToExclude, Certifiable item,
			String attributeName, Object attributeValue, boolean excludeOnMatch)
			throws GeneralException {

		/*
		 * We want to exclude all entitlements that do not have a specific
		 * ManagedAttribute marking. To do this, we query the ManagedAttribute
		 * Class for the supplied attribute and look for the supplied value.
		 * This can be a boolean, string or identity. The excludeOnMatch
		 * variable tells us if we should include the certifiable item in the
		 * cert on a match or exclude it. for instance, if we have an attribute
		 * called 'soxRelated' and we wish to only include sox related
		 * entitlements, we will set this excludeOnMatch to false.
		 */

		if (item instanceof Entitlements) {
			Entitlements ents = (Entitlements) item;
			List attributeNames = ents.getAttributeNames();
			// Currently I only handle attributes (not permissions)
			if (attributeNames != null) {
				String maName = ents.getAttributes().getString(
						ents.getAttributeNames().get(0));
				Application applicationObject = context.getObjectByName(
						Application.class, ents.getApplicationName());

				// Tracing
				// log.debug ("\tattributeName: " + attributeNames);
				// log.debug ("\tmaName: " + maName);
				// log.debug ("\tapplicationS " +
				// ents.getApplicationName());

				// Now that I have the entitlementGroup, I get the managed
				// attribute name and value. This is a list of one ('memberOf')
				// with one group name 'cn=Mygroup...').
				// Once I do that, I then get the managed attribute from the
				// ManagedAttributer and see if the extended attribute is set.
				// If it equals, I exclude. I will build the opposite of this
				// whereby i INCLUDE only the matches
				if (ents.getAttributeNames() != null) {
					ManagedAttribute manAtt = ManagedAttributer.get(context,
							applicationObject, (String) ents
									.getAttributeNames().get(0), maName);
				//	log.debug("Man Attributer is: " + manAtt);
					if (manAtt != null) {
						if (manAtt.getAttribute("identity") != null) {
					
						}

						boolean match = false; // use the match = true variable
												// to exclude if excludeOnMatch
												// = false. In this case we
												// actually EXCLUDE all non
												// matches.
						if (manAtt.getAttribute(attributeName) != null) {
							if (manAtt.getAttribute(attributeName) instanceof Boolean) {
								boolean val = (Boolean) manAtt
										.getAttribute(attributeName);
								if (val == Util.otob(attributeValue)) {
									match = true;

								}
							} else if (manAtt.getAttribute(attributeName) instanceof String) {
								String val = (String) manAtt
										.getAttribute(attributeName);
								if (val != null
										&& val.equalsIgnoreCase((String) attributeValue)) {
									match = true;
								}
							} else if (manAtt.getAttribute(attributeName) instanceof Identity) {
								Identity val = (Identity) manAtt
										.getAttribute(attributeName);
								if (val != null
										&& val == (Identity) context.getObject(
												Identity.class,
												(String) attributeValue)) {
									match = true;

								}

							}
						}
						if (excludeOnMatch && match == true) {
							itemsToExclude.add(item);
							match = false;
							return true;
						}

						if (excludeOnMatch == false && match == false) {
							match = false;
							itemsToExclude.add(item);
							return true;
						}
					}
				} else if (ents.getPermissions() != null) {
					itemsToExclude.add(item);
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean filterPermissions(SailPointContext context,
			List itemsToExclude, Certifiable item, boolean excludeOnMatch)
			throws GeneralException {

		/*
		 * We want to exclude all permissions. we loop through each entry. if it
		 * has permissions, we exlude or include based on the 'exclude on match'
		 * flag
		 */

		if (item instanceof Entitlements) {
			Entitlements ents = (Entitlements) item;

			if (ents.getAttributeNames() != null && excludeOnMatch == false) {
				itemsToExclude.add(item);
				return true;
			} else if (ents.getPermissions() != null && excludeOnMatch) {
				itemsToExclude.add(item);
				return true;

			}
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean excludeEntitlementsCertifiedAfterDayDuration(
			SailPointContext context, List itemsToExclude, Certifiable item,
			int days, Identity id, Certification certification)
			throws GeneralException {
		CertificationItem tmp = null;
		IdentityHistoryService historyService = new IdentityHistoryService(
				context);

		// long NUM_DELAY_UNITS = 2;
		// long DEFINE_ONE_HOUR = 60 * 60 * 1000;
		long DEFINE_ONE_DAY = 1 * 24 * 60 * 60 * 1000;

		// Rule Variables
		// --------------
		// Boolean _debug = true; // Set to false to suppress output
		long deltaTime = days * DEFINE_ONE_DAY;

		Date nowDate = new Date();
		long now = nowDate.getTime();

		long when = now - deltaTime;
		Date myDate = new Date(when);

		if (item instanceof Entitlements) {
			tmp = new CertificationItem((Entitlements) item,
					Certification.EntitlementGranularity.Value);
			tmp.setParent(new CertificationEntity(
					(AbstractCertifiableEntity) id));
			tmp.getParent().setCertification(certification);

			IdentityHistoryItem aIdentityHistoryItem = historyService
					.getLastDecision(id.getId(), tmp);
			if (aIdentityHistoryItem != null
					&& aIdentityHistoryItem.getEntryDate().before(myDate)) {
				itemsToExclude.add(item);
				return true;

			} 
		} else if (item instanceof Bundle) {

			IdentityHistoryItem aIdentityHistoryItem = historyService
					.getLastRoleDecision(id.getId(), ((Bundle) item).getName());
			if (aIdentityHistoryItem != null
					&& aIdentityHistoryItem.getEntryDate().before(myDate)) {
				itemsToExclude.add(item);
				return true;

			} 
		} else if (item instanceof PolicyViolation) {

			IdentityHistoryItem aIdentityHistoryItem = historyService
					.getLastViolationDecision(id.getId(),
							(PolicyViolation) item);
			if (aIdentityHistoryItem != null
					&& aIdentityHistoryItem.getEntryDate().before(myDate)) {
				itemsToExclude.add(item);
				return true;
			} 
		}
		return false;
	}

}
