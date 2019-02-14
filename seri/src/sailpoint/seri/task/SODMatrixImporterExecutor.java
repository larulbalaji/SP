package sailpoint.seri.task;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import sailpoint.seri.sodimporter.SODMatrixImporter;
import sailpoint.seri.sodimporter.SODPair;
import sailpoint.tools.GeneralException;

public class SODMatrixImporterExecutor extends SODImporterExecutor {

	private static Log log = LogFactory.getLog(SODMatrixImporterExecutor.class);
	
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
		SODMatrixImporter.Filetype type=SODMatrixImporter.Filetype.INVALID;
		if("csv".equalsIgnoreCase(sType)) type=SODMatrixImporter.Filetype.CSV;
		if("xls".equalsIgnoreCase(sType)) {
			type=SODMatrixImporter.Filetype.XLS;
		}

		SODMatrixImporter imp=new SODMatrixImporter(filename,type);
		if(type==SODMatrixImporter.Filetype.XLS) {
			String sName=args.getString("sheetName");
			imp.setSheetName(sName);
		}

		boolean genRoles=args.getBoolean("generateRoles");
		String sRoletype=args.getString("roleType");
		if(genRoles) {
			SODMatrixImporter.Roletype rType=SODMatrixImporter.Roletype.INVALID;
			if("it".equalsIgnoreCase(sRoletype)) rType=SODMatrixImporter.Roletype.IT;
			if("business".equalsIgnoreCase(sRoletype)) rType=SODMatrixImporter.Roletype.BUSINESS;
			imp.setRoletype(rType);
		}
		if("entitlement".equalsIgnoreCase(sRoletype)) {
			log.debug("SODImporterExecutor.execute: Entitlement");
			useEntitlements=true;
			entitlementApp=args.getString("entitlementApp");
			entitlementAttr=args.getString("entitlementAttr");
		} else {
			log.debug("SODImporterExecutor.execute: "+sRoletype);
		}
		String lblSource=args.getString("labelSource");
		if(lblSource!=null) {
			SODMatrixImporter.Source src=SODMatrixImporter.Source.INVALID;
			if("row".equalsIgnoreCase(lblSource)) src=SODMatrixImporter.Source.ROW;
			if("column".equalsIgnoreCase(lblSource)) src=SODMatrixImporter.Source.COLUMN;
			int idx=args.getInt("labelIndex");
			imp.setLabel(src, idx);
		}

		imp.setNameColumn(args.getInt("nameColIndex"));
		imp.setNameRow(args.getInt("nameRowIndex"));
		imp.setData(args.getInt("dataRowIndex"), args.getInt("dataColIndex"), args.getInt("dataSize"));
		String active=(String)args.get("activeSODcsvString");
		imp.setActiveSODcsvString(active);

		List<SODPair> pairList=imp.getSODMap();
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
		
		for(SODPair pair: pairList) {
			boolean isUpdate=true; 
			pol=context.getObjectByName(Policy.class, "SOD Policy "+pair.left+"-"+pair.right);
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

				MatchExpression matchLeft=new MatchExpression();
				MatchTerm mtLeft=new MatchTerm();
				mtLeft.setApplication(appl);
				mtLeft.setName(entitlementAttr);
				mtLeft.setValue(pair.left);
				
				matchLeft.addTerm(mtLeft);
				
				IdentitySelector left=new IdentitySelector();
				left.setMatchExpression(matchLeft);
				
				MatchExpression matchRight=new MatchExpression();
				MatchTerm mtRight=new MatchTerm();
				mtRight.setApplication(appl);
				mtRight.setName(entitlementAttr);
				mtRight.setValue(pair.right);
				
				matchRight.addTerm(mtRight);
				
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
			context.saveObject(pol);
			context.commitTransaction();
			if(isUpdate) {
				sodsUpdated++;
			} else {
				sodsCreated++;
			}
		}

		result.setAttribute(ROLES_CREATED, new Integer(rolesCreated));
		result.setAttribute(ROLES_SKIPPED, new Integer(rolesSkipped));
		result.setAttribute(SODS_CREATED, new Integer(sodsCreated));
		result.setAttribute(SODS_UPDATED, new Integer(sodsUpdated));

	}

	@Override
	public boolean terminate() {
		// TODO Auto-generated method stub
		return false;
	}
}
