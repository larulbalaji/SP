package sailpoint.seri.task;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.GenericConstraint;
import sailpoint.object.Identity;
import sailpoint.object.IdentitySelector;
import sailpoint.object.IdentitySelector.MatchExpression;
import sailpoint.object.IdentitySelector.MatchTerm;
import sailpoint.object.Policy;
import sailpoint.object.Policy.State;
import sailpoint.object.Policy.ViolationOwnerType;
import sailpoint.object.SODConstraint;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.seri.sodimporter.Importer;
import sailpoint.seri.sodimporter.SODImporterException;
import sailpoint.seri.sodimporter.SODListImporter;
import sailpoint.seri.sodimporter.SODPair;
import sailpoint.tools.GeneralException;

public class SODListImporterExecutor extends SODImporterExecutor {

	private static Log log = LogFactory.getLog(SODListImporterExecutor.class);
	
	@Override
	public void execute(SailPointContext context, TaskSchedule schedule,
			TaskResult result, Attributes<String, Object> args)
					throws Exception {

		ClassLoader classloader =
				org.apache.poi.poifs.filesystem.POIFSFileSystem.class.getClassLoader();
		URL res = classloader.getResource(
				"org/apache/poi/poifs/filesystem/POIFSFileSystem.class");
		String path = res.getPath();
		log.debug("SODImporterExecutor: --------------------------------------");
		log.debug("SODImporterExecutor: Core POI came from " + path);
		log.debug("SODImporterExecutor: --------------------------------------");

		boolean useEntitlements=false;
		String entitlementApp=null;
		String entitlementAttr=null;


		// Parse the parameters
		String filename=args.getString("filename");
		String sType=args.getString("fileType");
		SODListImporter.Filetype type=SODListImporter.Filetype.INVALID;
		if("csv".equalsIgnoreCase(sType)) type=SODListImporter.Filetype.CSV;
		if("xls".equalsIgnoreCase(sType)) {
			type=SODListImporter.Filetype.XLS;
		}

		SODListImporter imp=new SODListImporter(filename,type);
		if(type==SODListImporter.Filetype.XLS) {
			String sName=args.getString("sheetName");
			imp.setSheetName(sName);
		}
		
		int leftCol=args.getInt("leftCol");
		if(leftCol==0) {
			throw new SODImporterException("Left Column must be specified and greater than zero");
		}
		imp.setLeftColumn(leftCol);

		int rightCol=args.getInt("rightCol");
		if(rightCol==0) {
			throw new SODImporterException("Right Column must be specified and greater than zero");
		}
		imp.setRightColumn(rightCol);

		int startRow=args.getInt("startRow");
		if(startRow==0) {
			throw new SODImporterException("Start Row must be specified and greater than zero");
		}
		imp.setStartRow(startRow);

		boolean genRoles=args.getBoolean("generateRoles");
		String sRoletype=args.getString("roleType");
		if(genRoles) {
			Importer.Roletype rType=Importer.Roletype.INVALID;
			if("it".equalsIgnoreCase(sRoletype)) rType=Importer.Roletype.IT;
			if("business".equalsIgnoreCase(sRoletype)) rType=Importer.Roletype.BUSINESS;
			imp.setRoletype(rType);
		}
		boolean singlePolicy=args.getBoolean("singlePolicy");
		String singlePolicyName=null;
		if(singlePolicy) {
			singlePolicyName=args.getString("singlePolicyName");
		}
		
		boolean entitlementsSeparate=args.getBoolean("entitlementsSeparate");
		
		if("entitlement".equalsIgnoreCase(sRoletype) || entitlementsSeparate) {
			log.debug("SODImporterExecutor.execute: Entitlement");
			useEntitlements=true;
			entitlementApp=args.getString("entitlementApp");
			entitlementAttr=args.getString("entitlementAttr");
		} else {
			log.debug("SODImporterExecutor.execute: "+sRoletype);
		}
		if(entitlementsSeparate) {
			imp.setEntitlementFilename(args.getString("entFilename"));
			String sEType=args.getString("entFileType");
			Importer.Filetype eType=Importer.Filetype.INVALID;
			if("csv".equalsIgnoreCase(sEType)) eType=Importer.Filetype.CSV;
			if("xls".equalsIgnoreCase(sEType)) {
				eType=Importer.Filetype.XLS;
			}
			imp.setEntitlementType(eType);
			if(eType==Importer.Filetype.XLS) {
				imp.setEntitlementSheetName(args.getString("entSheetName"));
			}
			imp.setEntRefCol(args.getInt("entRefCol"));
			imp.setEntValueCol(args.getInt("entValueCol"));
			imp.setEntStartRow(args.getInt("entStartRow"));
		}
		
		List<SODPair> pairList=imp.getSODMap();
		Map<String, List<String>> entitlements=null;
		
		if (entitlementsSeparate) {
			entitlements=imp.getEntitlementsMap();
		}
		
		int rolesCreated=0;
		int rolesSkipped=0;
		if(genRoles) {
			String[] roleNames=imp.getNames();
			Identity owner=context.getObjectByName(Identity.class, ownerName);
			for(String roleName: roleNames) {
				Bundle bndl=context.getObjectByName(Bundle.class, roleName);
				if(bndl==null) {
					// create role
					bndl=new Bundle();
					bndl.setName(roleName);
					bndl.setOwner(owner);
					bndl.setType(imp.getRoleTypeString());
					context.saveObject(bndl);
					context.commitTransaction();
					rolesCreated++;
				} else {
					// role already exists
					rolesSkipped++;
				}
			}
		}
		int sodsCreated=0;
		int sodsUpdated=0;
		Policy pol=null;
		
		if (singlePolicy) {
			// try to get the existing policy object - otherwise running the SOD Policy Importer
			// more than once results in JDBC Batch update errors
			pol=context.getObjectByName(Policy.class, singlePolicyName);
			if(pol==null) {
				pol=new Policy();
				pol.setName(singlePolicyName);
			}
			pol.setState(State.Active);
			pol.setType((useEntitlements?"Entitlement":"")+"SOD");
			pol.setViolationOwnerType(ViolationOwnerType.Manager);
			pol.setDescription("Separation of Duty policy between "
					+ ((useEntitlements)?"entitlements":"roles")
					+ ((useEntitlements)?" on Application "+entitlementApp:"")		
					);
			pol.setCertificationActions("Remediated,Mitigated,Delegated");
		}
		
		for(SODPair pair: pairList) {
			boolean isUpdate=true; 
			if(!singlePolicy) {
				// try to get the existing policy object - otherwise running the SOD Policy Importer
				// more than once results in JDBC Batch update errors
				pol=context.getObjectByName(Policy.class, singlePolicyName);
				if(pol==null) {
					pol=new Policy();
					pol.setName("SOD Policy "+pair.left+"-"+pair.right);
					isUpdate=false;
				} else {
					// clear out current constraints
					pol.setGenericConstraints(null);
					pol.setActivityConstraints(null);
					pol.setSODConstraints(null);
				}
				pol.setState(State.Active);
				pol.setType((useEntitlements?"Entitlement":"")+"SOD");
				pol.setViolationOwnerType(ViolationOwnerType.Manager);
				pol.setDescription("Separation of Duty policy between "
						+ ((useEntitlements)?"":"roles ")+pair.left+" and "+pair.right+
						((useEntitlements)?" on Application "+entitlementApp:"")		
						);
				pol.setCertificationActions("Remediated,Mitigated,Delegated");
			}

			if(useEntitlements) {
				pol.setExecutor(sailpoint.policy.EntitlementSODPolicyExecutor.class);
				pol.setConfigPage("entitlementPolicy.xhtml");
				// Set up a new GenericConstraint
				// With two IdentitySelectors for left and right
				// each IdentitySelector has a MatchExpression
				// this is from the XML export of a Policy object
				
				// set up policy using entitlements
				GenericConstraint gc=new GenericConstraint();

				gc.setName(pair.left+" - "+pair.right+" constraint");
				gc.setViolationOwnerType(ViolationOwnerType.Manager);
				
				Application appl=context.getObjectByName(Application.class, entitlementApp);
				if(appl==null) {
					throw new GeneralException ("cannot find application '"+entitlementApp+"' for SOD Importer");
				}

				MatchExpression matchLeft = getMatchExpression(entitlementAttr,
						entitlementsSeparate, entitlements, pair.left, appl);
				
				
				IdentitySelector left=new IdentitySelector();
				left.setMatchExpression(matchLeft);
				
				MatchExpression matchRight=getMatchExpression(entitlementAttr,
						entitlementsSeparate, entitlements, pair.right, appl);
				
				IdentitySelector right=new IdentitySelector();
				right.setMatchExpression(matchRight);
				
				gc.setLeftSelector(left);
				gc.setRightSelector(right);
				
				pol.addConstraint(gc);
			} else {
				pol.setConfigPage("sodpolicy.xhtml");
				pol.setExecutor(sailpoint.policy.SODPolicyExecutor.class);
				SODConstraint cons=new SODConstraint();
				cons.setName(pair.left+" - "+pair.right+" constraint");
				cons.setViolationOwnerType(ViolationOwnerType.Manager);
				
				List<Bundle> left=new ArrayList<Bundle>();
				Bundle bLeft=context.getObjectByName(Bundle.class, pair.left);
				if(bLeft!=null) {
					left.add(bLeft);
				}
				cons.setLeftBundles(left);

				List<Bundle> right=new ArrayList<Bundle>();
				Bundle bRight=context.getObjectByName(Bundle.class, pair.right);
				if(bRight!=null) {
					right.add(bRight);
				}
				cons.setRightBundles(right);
				pol.addConstraint(cons);
			}

			//			context.saveObject(cons);
			if(!singlePolicy) {
				context.saveObject(pol);
				context.commitTransaction();
			}
			if(isUpdate) {
				sodsUpdated++;
			} else {
				sodsCreated++;
			}
		}
		if(singlePolicy) {
			context.saveObject(pol);
			context.commitTransaction();
		}

		result.setAttribute(ROLES_CREATED, new Integer(rolesCreated));
		result.setAttribute(ROLES_SKIPPED, new Integer(rolesSkipped));
		result.setAttribute(SODS_CREATED, new Integer(sodsCreated));
		result.setAttribute(SODS_UPDATED, new Integer(sodsUpdated));

	}

	private MatchExpression getMatchExpression(String entitlementAttr,
			boolean entitlementsSeparate,
			Map<String, List<String>> entitlements, String sodValue,
			Application appl) {
		MatchExpression matchExpr=new MatchExpression();
		if(entitlementsSeparate) {
			List<String> entValues=entitlements.get(sodValue);
			matchExpr.setAnd(false);
			if(entValues!=null) {
				for(String val: entValues) {
					MatchTerm term=new MatchTerm();
					term.setApplication(appl);
					term.setName(entitlementAttr);
					term.setValue(val);
					matchExpr.addTerm(term);
				}
			}
		} else {
			MatchTerm mtLeft=new MatchTerm();
			mtLeft.setApplication(appl);
			mtLeft.setName(entitlementAttr);
			mtLeft.setValue(sodValue);
			matchExpr.addTerm(mtLeft);
		}
		return matchExpr;
	}

	@Override
	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}
}
