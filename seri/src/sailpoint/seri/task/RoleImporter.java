/*
 *  A custom task executor for importing roles from a csv file.
 *
 *
 *  @author Sean Koontz, Terry Sigle, Dave Smith
 *
 *  Role Importer consists of a .csv file with specfic columns of role data.  Below
 *  is an example format to be used
 *
 *  Available Operaions on Import (note that these are case insinsitive)
 *  --------------------------------------------------------------------
 *    Add Role         - Creates a new role
 *    Delete Role      - Deletes an existing role
 *    Add Inheritance  - Adds a parent to an existing role
 *    Add Permitted    - Adds a permitted role to an existing role
 *    Add Required     - Adds a required role to an existing role
 *    Add Matchlist    - Adds a matching list to an existing business type role
 *    Add Profile      - Adds a profile with entitlements to an existing IT type role
 *    Add Scope        - Assigns a role to a scope
 *
 */
package sailpoint.seri.task;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sailpoint.api.Describer;
import sailpoint.api.Provisioner;
import sailpoint.api.SailPointContext;
import sailpoint.object.AccountSelectorRules;
import sailpoint.object.Application;
import sailpoint.object.ApplicationAccountSelectorRule;
import sailpoint.object.Attributes;
import sailpoint.object.Scope;
import sailpoint.object.Bundle;
import sailpoint.object.CompoundFilter;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.IdentitySelector;
import sailpoint.object.IdentitySelector.MatchExpression;
import sailpoint.object.IdentitySelector.MatchTerm;
import sailpoint.object.Permission;
import sailpoint.object.Profile;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.Rule;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.RFC4180LineIterator;
import sailpoint.tools.RFC4180LineParser;
import sailpoint.tools.Util;

public class RoleImporter extends AbstractTaskExecutor {

	//
	// Input Arguments
	//
	public static final String ARG_ROLE_FILENAME = "roleFile";
	public static final String ARG_USE_HEADER = "useHeader";
	public static final String ARG_UPDATE_EXISTING = "updateExisting";
	public static final String ARG_OR_PROFILES = "orProfiles";
	public static final String ARG_FILE_ENCODING = "fileEncoding";

	private static final String QUOTE = "\"";

	private String _roleFile = "";
	private String _fileEncoding = null;
	private boolean _useHeader = true;
	private boolean _updateExisting = true;
	private boolean _orProfiles = false;

	//
	// Return Arguments
	//
	public static final String RET_LINES = "lines";
	public static final String RET_ROLES_CREATED = "rolesCreated";
	public static final String RET_ROLES_UPDATED = "rolesUpdated";
	public static final String RET_ROLES_DELETED = "rolesDeleted";
	public static final String RET_PROFILES_CREATED = "profilesCreated";
	public static final String RET_ROLES_ASSIGNED = "rolesAssigned";
    public static final String RET_SCOPES_CREATED = "scopesCreated";
    public static final String RET_SCOPES_ASSIGNED = "scopesAssigned";

    
	// Counters used to hold return arguments
	private int _lines = 0;
	private int _rolesCreated = 0;
	private int _rolesUpdated = 0;
	private int _rolesDeleted = 0;
	private int _profilesCreated = 0;
	private int _rolesAssigned = 0;
    private int _scopesCreated = 0;
    private int _scopesAssigned = 0;
	
	private SailPointContext _ctx;

	private static Log _log = LogFactory.getLog(RoleImporter.class);

	private static RFC4180LineParser _parser = new RFC4180LineParser(',');


	/**
	 * Terminate at the next convenient point.
	 */
	public boolean terminate() {
		return false;
	}

	public RoleImporter() {
		super();
	}

	public RoleImporter(SailPointContext ctx) {
		super();
		_ctx = ctx;
	}


	/**
	 * Execute the Role Importer task.
	 */
	public void
	execute(SailPointContext ctx, TaskSchedule sched, TaskResult result, Attributes<String, Object> args)
			throws Exception
	{
		_ctx = ctx;
		_roleFile = args.getString(ARG_ROLE_FILENAME);
		_useHeader = args.getBoolean(ARG_USE_HEADER);
		_fileEncoding = args.getString(ARG_FILE_ENCODING);

		// Allow existing roles to be updated (default) or skipped over
		//
		if (false == args.getBoolean(ARG_UPDATE_EXISTING, true)) {
			_updateExisting = false;
		}

		// Flag for specifying multiple profiles in a role should be ORed
		//
		if (true == args.getBoolean(ARG_OR_PROFILES, false)) {
			_orProfiles = true;
		}

		// Open the file and get a handle on it.
		InputStream stream = getFileStream();

		RFC4180LineIterator lines = null;
		if ( _fileEncoding != null ) {
			_log.debug("...encoding is: " + _fileEncoding);
			try {
				lines = new RFC4180LineIterator(new BufferedReader(new InputStreamReader(stream, _fileEncoding)));
			} catch (java.io.UnsupportedEncodingException e) {
				throw new GeneralException(e);
			}
		} else {
			lines = new RFC4180LineIterator(new BufferedReader(new InputStreamReader(stream)));
		}

		// Process each of the lines
		if (lines != null) {
			_lines = processLines(ctx, lines);
		}

		result.setAttribute(RET_LINES, _lines);
		result.setAttribute(RET_ROLES_CREATED, Util.itoa(_rolesCreated));
		result.setAttribute(RET_ROLES_DELETED, Util.itoa(_rolesDeleted));
		result.setAttribute(RET_PROFILES_CREATED, Util.itoa(_profilesCreated));
		result.setAttribute(RET_ROLES_ASSIGNED, Util.itoa(_rolesAssigned));
		result.setAttribute(RET_SCOPES_CREATED, Util.itoa(_scopesCreated));
		result.setAttribute(RET_SCOPES_ASSIGNED, Util.itoa(_scopesAssigned));
	}

	//
	// processLines will parse each line in the input file and decide how to handle
	// the operation and corresponding operation 'details'
	//
	// Example: Let's say header looks like:
	//
	//      op, type, name, description, owner, parent
	//      Add Role,Organization,Role Name,Role Description,spadmin,Role Parent
	//
	// This should create a new role called 'Role Name' with the parent role, 'Role Parent'
	//
	private int
	processLines(SailPointContext ctx, RFC4180LineIterator lines)
			throws Exception
	{
		boolean done = false;
		int linesRead = 0;
		String line = null;

		while (!done) {
			line = lines.readLine();

			// If the current line is null or if the line starts with a # (comment) or size
			// of 0, then continue to the next line.
			if (line == null) {
				done = true;
				continue;
			} else if (line.startsWith("#") || line.length() < 1) {
				continue;
			} else {
				linesRead++;
			}

			List<String> tokens = _parser.parseLine(line);

			processLine(ctx, tokens);

		}
		ctx.commitTransaction();

		return linesRead;
	}

	public void processLine(SailPointContext ctx, List<String> tokens) throws GeneralException, JSONException {
		// The first token in each line represents the operation for that line
		String op = tokens.get(0).toUpperCase();

		switch(op) {
		case "ADD ROLE":
			addRole(tokens);
			break;
		case "DELETE ROLE":
			deleteRole(tokens);
			break;
		case "ADD INHERITANCE":
			addInheritance(tokens);
			break;
		case "ADD PERMITTED":
			addPermitted(tokens);
			break;
		case "ADD REQUIRED":
			addRequired(tokens);
			break;
		case "ADD MATCHLIST":
			addMatchlist(tokens);
			break;
		case "ADD PROFILE":
			addProfile(tokens);
			break;
		case "ADD METADATA":
			addMetadata(tokens);
			break;
		case "ADD DESCRIPTION":
			addDescription(tokens);
			break;
		case "ADD ASSIGNMENTRULE":
			addAssignmentRule(tokens);
			break;
		case "ADD ACCOUNTSELECTOR":
			addAccountSelector(tokens);
			break;
		case "ADD ROLEASSIGNMENT":
			addRoleAssignment(tokens);
	    case "ADD SCOPE":
			addScope(tokens);
	    case "ADD SCOPEASSIGNMENT":
			addScopeAssignment(tokens);

		}

	}

	private IdentitySelector generateMatchList(SailPointContext ctx, boolean andOp, IdentitySelector existingSelector, List<String> identityAttrs, List<String> identityValues, List<String> applications)
	{
		IdentitySelector assignmentRule = new IdentitySelector();

		MatchExpression matcher = new MatchExpression();

		_log.debug("RoleImporter.generateMatchList:  AND OP is " + andOp);
		// like cement, baby
		matcher.setAnd(andOp);

		// do we just have one app to set for all the terms
		Application singleApplication=null;
		if(applications!=null && applications.size()==1) {
			try {
				singleApplication=ctx.getObjectByName(Application.class, applications.get(0));
			} catch (GeneralException ge) {
				_log.debug("Application "+applications.get(0)+" not found. Ignoring..");
			}
		}

		// Build an assignment rule using the identity attributes
		for (int i = 0; i < identityAttrs.size(); i++) {
			String identityAttr = identityAttrs.get(i);
			String identityValue = identityValues.get(i);
			MatchTerm term = new MatchTerm();
			term.setName(identityAttr);
			term.setValue(identityValue);
			if(applications != null) {

				if(singleApplication!=null) {
					term.setApplication(singleApplication);
				} else {
					String appName=applications.get(i);
					try{
						Application app=ctx.getObjectByName(Application.class, appName);
						term.setApplication(app);
					} catch (GeneralException ge) {
						_log.debug("Application "+appName+" not found. Ignoring..");
					}
				}

			}
			matcher.addTerm(term);
		}

		// kmj: Now, if the existingSelector is not null, we need to do an 'or' of this matchexpression
		// and the existing matchexpressions
		//
		// we are also assuming that any existing selector is a matchlist

		if(existingSelector!=null) {
			MatchExpression previous=existingSelector.getMatchExpression();

			// Recreate the previous MatchExpression as a container MatchTerm
			MatchTerm previousAsTerm=new MatchTerm();
			previousAsTerm.setAnd(previous.isAnd());
			previousAsTerm.setContainer(true);
			previousAsTerm.setChildren(previous.getTerms());

			// Recreate the current MatchExpression as a container MatchTerm

			MatchTerm currentAsTerm=new MatchTerm();
			currentAsTerm.setAnd(andOp);
			currentAsTerm.setContainer(true);
			currentAsTerm.setChildren(matcher.getTerms());

			matcher=new MatchExpression();
			matcher.setAnd(false);
			matcher.addTerm(previousAsTerm);
			matcher.addTerm(currentAsTerm);

		}

		assignmentRule.setMatchExpression(matcher);

		return assignmentRule;
	}



	/**
	 * Create the profile filter from a string
	 *
	 * EntitlementValues will look like:
	 *   value1,value2,value3,...,valueN
	 *
	 * Creates:
	 *    groupmbr.containsAll({"ClaimsAdmin"})
	 */
	private String createProfileFilter(String attrName, List<String> entitlementValues) {
		String filter = attrName + ".containsAllIgnoreCase({";
		String comma = "";

		for (String entValue : entitlementValues) {
			filter += comma + QUOTE + entValue + QUOTE;

			comma = ",";
		}


		filter += "})";

		return filter;
	}

	/**
	 * Get the input File Stream.
	 */
	private InputStream getFileStream() throws Exception {
		InputStream stream = null;

		if (_roleFile == null) {
			throw new GeneralException("Filename cannot be null.");
		}
		try {
			File file = new File(_roleFile);
			if (!file.exists()) {
				// sniff the file see if its relative if it is
				// see if we can append sphome to find it
				if (!file.isAbsolute()) {
					String appHome = getAppHome();
					if (appHome != null) {
						file = new File(appHome + File.separator + _roleFile);
						if (!file.exists()) {
							file = new File(_roleFile);
						}
					}
				}
			}
			// This will throw an exception if the file cannot be found
			stream = new BufferedInputStream(new FileInputStream(file));
		} catch (Exception e) {
			throw new GeneralException(e);
		}

		return stream;
	}


	/**
	 * Try to get the app home to be smart about locating relative paths.
	 */
	private String getAppHome() {
		String home = null;
		try {
			home = Util.getApplicationHome();
		} catch (Exception e) {
			_log.error("Unable to find application home.");
		}
		return home;
	}

	private void
	setDescription(Bundle obj, String desc, String locale)
			throws GeneralException
	{
		Describer describer = new Describer(obj);

		//Localizer localizer = new Localizer(_ctx, obj.getId());

		// Use default language of IIQ instance when no locale specified
		if (null == locale) {
			describer.setDefaultDescription(_ctx, desc);
		} else {
			Map<String,String> descriptions = new HashMap<String,String>();
			descriptions.put(locale, desc);
			describer.addDescriptions(descriptions); 
		}

		describer.saveLocalizedAttributes(_ctx);
		_ctx.commitTransaction();

		return;
	}


	private Application
	bootstrapApp(String appName)
			throws GeneralException
	{
		Application app = new Application();
		app.setName(appName);

		// Set some minimum stuff so it looks good in UI
		//
		app.setType("Delimited File Parsing Connector");

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		app.setDescription("Auto-created by IdentityIQ Role Importer : " + dateFormat.format(date));

		Identity admin = _ctx.getObjectByName(Identity.class, "spadmin");
		if (null != admin) {
			app.setOwner(admin);
		}

		app.setConnector("sailpoint.connector.DelimitedFileConnector");

		// Need this to get proper editing in UI
		//  - Yes, this assumes app is actually file-based extract...can always change via debug pages
		//
		app.setAttribute("templateApplication", "DelimitedFile Template");

		_ctx.saveObject(app);
		_ctx.commitTransaction();
		_ctx.decache(app);

		return app;
	}

	private void addRole(List<String> tokens) throws GeneralException {

		//  Add Role - Format for Organization Roles
		//  ----------------------------------------------------
		//    Role Type           - Type of role.  Needs to match a valid Role Type
		//    Role Name           - Name of role.
		//    Role Display Name   - Display name of role (optional)
		//    Role Description    - Description of role.
		//    Role Owner          - Owner of role.  Needs to match a valid Identity
		//    Role Parent         - Parent of role.  Needs to match an existing role name
		//    Profile Application - only for 'Entitlement' roles
		//    Profile Attributes  - only for 'Entitlement' roles
		//    Profile Entitlement - only for 'Entitlement' roles
		//
		// TODO: Add Scope
		//  ----------------------------------------------------
		String roleType        = tokens.get(1);
		String roleName        = tokens.get(2);
		String roleDisplayName = tokens.get(3);
		String roleDesc        = tokens.get(4);
		String roleOwner       = tokens.get(5);
		String roleParent      = tokens.get(6);

		_log.debug("ADD ROLE,"+roleType+","+roleName+","+roleDesc+","+roleOwner+","+roleParent);

		// First check to see if the role already exists.  If so, then check flag for
		// whether we update or skip
		//
		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);

		if (role != null) {
			_log.info("Role name in import file matches existing role name: " + roleName);
			if (_updateExisting) {
				_log.debug("...updateExisting flag is on; role will be updated.");
			} else {
				_log.debug("...updateExisting flag is off; role will be skipped.");
				return;
			}
		} else {
			// Create a new role
			role = new Bundle();
		}

		// Set the role name, description and type
		role.setName(roleName);
		role.setType(roleType);

		if (null != roleDisplayName) {
			role.setDisplayName(roleDisplayName);
		}

		// Get the Identity of the owner.  If it's not found, default to spadmin
		Identity owner = _ctx.getObjectByName(Identity.class, roleOwner);
		if (owner != null) {
			role.setOwner(owner);
		} else {
			// Let the role come in with a default owner
			_log.warn("Cannot find owner [" + roleOwner + "].  Using spadmin.");
			owner = _ctx.getObjectByName(Identity.class, "spadmin");
			role.setOwner(owner);
		}

		// Get the parent role.  If it's not found, create a stub so that we can continue.
		// The parent role is optional.  If it's not set, then no parent will be set on the role.
		//
		Bundle parent = _ctx.getObjectByName(Bundle.class, roleParent);
		if (parent != null) {
			role.addInheritance(parent);
		} else {
			// Create stub...
			if (null != roleParent) {
				parent = new Bundle();
				parent.setName(roleParent);
				_ctx.saveObject(parent);
				_ctx.commitTransaction();
				_ctx.decache(parent);
				_rolesCreated++;
			}
			role.addInheritance(parent);
		}

		// Special Handling for Business role types.
		if ("BUSINESS".equalsIgnoreCase(roleType))
		{
			// Do Nothing
		}

		// Special Handling for Entitlement Roles
		// These are optional, and if set, creates a profile w/ entitlements for the
		// applicaion/attribute combination
		if ("ENTITLEMENT".equalsIgnoreCase(roleType) ||
				"APPLICATION".equalsIgnoreCase(roleType) ||
				"IT".equalsIgnoreCase(roleType)  ) {
			String profileDesc = tokens.get(3);   // Same as Role Description, for now...

			if (tokens.size() > 8)
			{
				String profileApp  = tokens.get(7);
				String profileAttr = tokens.get(8);

				String entValueStr = tokens.get(9);

				if (entValueStr != null && entValueStr.length() > 0)
				{

					List<String> entValues = _parser.parseLine(tokens.get(8));

					String profileFilter = createProfileFilter(profileAttr, entValues);

					_log.info("Building the Entitlement Role : " + roleName);
					_log.info("   Profile App    = " + profileApp);
					_log.info("   Profile Attr   = " + profileAttr);
					_log.info("   Profile Desc   = " + profileDesc);
					_log.info("   Profile Filter = " + profileFilter);

					Application app = _ctx.getObjectByName(Application.class, profileApp);

					// Handle non-existant apps by boot-strapping stub app
					//   - This lets us deal with role model extracts that cover more apps then
					//     are in scope for POC
					//
					if (null == app) {
						_log.info("   Referenced application does NOT exist.  Bootstrapping stub for: " + profileApp);                    	
						app = bootstrapApp(profileApp);
					}

					Profile profile = new Profile();
					Filter filter = Filter.compile(profileFilter);   // What does this do...?
					profile.setDescription(profileDesc);
					profile.setApplication(app);
					profile.addConstraint(filter);
					role.add(profile);

					_profilesCreated++;
				} else {
					_log.debug("Expected entitlement profile not found for: " + roleName);

				}
			}
		}

		// Check whether orProfiles flag is on
		if (_orProfiles) {
			role.setOrProfiles(_orProfiles);
		}

		// All done.  Go ahead and save this role.
		_ctx.saveObject(role);

		setDescription(role, roleDesc, null);

		_ctx.commitTransaction();
		_ctx.decache(role);


		_rolesCreated++;
	}

	private void deleteRole(List<String> tokens) throws GeneralException {
		//  Delete Role - Deletes existing roles
		//  ----------------------------------------------------
		//    Role Name           - Name of an existing role
		//  ----------------------------------------------------
		String roleName = tokens.get(1);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);

		if (role != null) {
			_ctx.removeObject(role);
			_ctx.commitTransaction();
			_ctx.decache(role);
			_rolesDeleted++;
		}
	}

	private void addInheritance(List<String> tokens) throws GeneralException {
		//  Add Inheritance - Adds a parent to an existing role
		//  ------------------------------------------------
		//    Role Name         - Role to add a parent to
		//    Parent Role Name  - Parent role name
		//  ------------------------------------------------
		String roleName = tokens.get(1);
		String parentRole = tokens.get(2);

		_log.debug("ADD INHERITANCE,"+roleName+","+parentRole);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		Bundle parent = _ctx.getObjectByName(Bundle.class, parentRole);

		// Create stub...
		if (parent == null && null != parentRole) {
			parent = new Bundle();
			parent.setName(parentRole);
			_ctx.saveObject(parent);
			_ctx.commitTransaction();
			_ctx.decache(parent);
			_rolesCreated++;
		}

		role.addInheritance(parent);

		_ctx.saveObject(role);
		_ctx.commitTransaction();
		_ctx.decache(role);
	}


	private void addPermitted(List<String> tokens) throws GeneralException {
		//  Add Permitted - Adds a permitted role to an existing role
		//  ----------------------------------------------
		//    Role Name            - Role to add permitted roles to
		//    Permitted Role Name  - Permitted role name
		//  ----------------------------------------------
		String roleName = tokens.get(1);
		String permittedRole = tokens.get(2);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		Bundle permitted = _ctx.getObjectByName(Bundle.class, permittedRole);

		if (role == null) {
			_log.error("Role not found: " + roleName);
			return;
		}
		if (permitted == null) {
			_log.error("Permitted Role not found: " + permittedRole);
			return;
		}
		role.addPermit(permitted);

		_ctx.saveObject(role);
		_ctx.commitTransaction();
		_ctx.decache(role);
	}


	private void addRequired(List<String> tokens) throws GeneralException {
		//  Add Required - Adds a required role to an existing role
		//  ----------------------------------------------
		//    Role Name            - Role to add required roles to
		//    Permitted Role Name  - Required role name
		//  ----------------------------------------------
		String roleName = tokens.get(1);
		String requiredRole = tokens.get(2);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		Bundle required = _ctx.getObjectByName(Bundle.class, requiredRole);

		if (role == null) {
			_log.error("Role not found: " + roleName);
			return;
		}
		if (required == null) {
			_log.error("Required Role not found: " + requiredRole);
			return;
		}
		role.addRequirement(required);

		_ctx.saveObject(role);
		_ctx.commitTransaction();
		_ctx.decache(role);
	}

	private void addMatchlist(List<String> tokens) throws GeneralException {
		//  Add MATCHLIST - Adds a matchlist to an existing business role
		//  --------------------------------------------
		//    Role Name            - Name of an existing role
		//    Match List Format    - Type of Matchlist.  Currently only
		//                            IdentityMatchList is supported.
		//                            TODO: Add LDAPFilter and Filter to formats
		//    Match List Options
		//         IdnetityMatchList  - Accepts 3 inputs
		//             AND_OP         - true - ANDs attribute/values together
		//                            - false - ORs attribute/values together
		//             Attributes     - ordered csv list of attributes to match on
		//             Values         - ordered csv list of values to match on
		//  --------------------------------------------

		String roleName = tokens.get(1);
		String matchlistType = tokens.get(2);

		_log.debug("ADD MATCHLIST,"+roleName+","+matchlistType);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);

		if (role == null) {
			_log.error("Role not found: " + roleName);
			return;
		}

		if ("IDENTITYMATCHLIST".equalsIgnoreCase(matchlistType)) {
			String mlAnd = tokens.get(3);
			String mlAttrs = tokens.get(4);
			String mlValues = tokens.get(5);
			String mlApplication = tokens.get(6);

			if (mlAnd == null || mlAttrs == null || mlValues == null)
			{
				_log.error("Problem with matchlist while parsing role " + roleName);
				return;
			}

			boolean andOp = ("AND".equalsIgnoreCase(mlAnd));

			List<String> mlAttrsList = _parser.parseLine(mlAttrs);
			List<String> mlValuesList = _parser.parseLine(mlValues);
			List<String> mlApplicationList = _parser.parseLine(mlApplication);

			if (mlAttrsList.size() != mlValuesList.size()
					// Check we have 0, 1 or same number of applications
					|| (mlApplicationList!=null && mlApplicationList.size()>1 && mlAttrsList.size() != mlApplicationList.size() ))
			{
				_log.error("Inconsistent sizes of matchlist attrs/values/applications while parsing role " + roleName);
				return;
			}
			Application matchApplication = null;
			if(mlApplication != null && mlApplication.length() > 0) {
				matchApplication = _ctx.getObjectByName(Application.class, mlApplication);
			}

			IdentitySelector existingSelector = role.getSelector();

			IdentitySelector selector = generateMatchList(_ctx, andOp, existingSelector, mlAttrsList, mlValuesList, mlApplicationList);

			role.setSelector(selector);
		} else if ("LDAPFILTER".equalsIgnoreCase(matchlistType)) {

		} else if ("FILTER".equalsIgnoreCase(matchlistType)) {

			String filterStr = tokens.get(3);

			if (filterStr == null)
			{
				_log.error("Problem with filter while parsing role " + roleName);
				return;
			}


			CompoundFilter f = new CompoundFilter();
			// sigh something above us isn't catching
			// RuntimeExceptions which are commonly
			// thrown by the XML parser
			try {
				f.update(_ctx, filterStr);
			}
			catch (RuntimeException e) {
				_log.debug("exception: " + e);
				_log.error("Problem parsing filter on role: " + roleName);
				return;
			}

			IdentitySelector selector = new IdentitySelector();

			selector.setFilter(f);

			role.setSelector(selector);
		}

		_ctx.saveObject(role);
		_ctx.commitTransaction();
		_ctx.decache(role);
	}

	private void addProfile(List<String> tokens) throws GeneralException {
		//  Add Profile - Adds a profile to an existing role
		//  --------------------------------------------
		//    Role Name
		//    Profile Description
		//    Profile Application
		//    Profile Filter
		//  --------------------------------------------
		String roleName = tokens.get(1);
		String profileDesc = tokens.get(2);
		String profileApp = tokens.get(3);
		String profileFilter = tokens.get(4);

		_log.debug("ADD PROFILE,"+roleName+","+profileDesc+","+profileApp+","+profileFilter);

		JSONArray permissionArray = null;

		if(tokens.size() > 5) {
			try {
				permissionArray = new JSONArray(tokens.get(5));
			} catch (JSONException je){
				throw new GeneralException("JSONException adding profile: "+je);
			}
		}

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {
			Application app = _ctx.getObjectByName(Application.class, profileApp);

			// Handle non-existant apps by boot-strapping stub app
			//   - This lets us deal with role model extracts that cover more apps then
			//     are in scope for POC
			//
			if (null == app) {
				_log.info("Profile references application that does NOT exist.  Bootstrapping stub for: " + profileApp);
				app = bootstrapApp(profileApp);
			}

			Profile profile = new Profile();
			if(profileFilter != null && profileFilter.length() > 0) {
				Filter filter = Filter.compile(profileFilter);
				profile.addConstraint(filter);
			}

			if(permissionArray != null && permissionArray.length() > 0) {
				for(int i = 0; i < permissionArray.length(); i++) {
					try {
						JSONObject permObject = permissionArray.getJSONObject(i);
						String target = permObject.getString("target");
						String rights = permObject.getString("rights");
						List<String> rightsList = Util.stringToList(rights);
						Permission newPerm = new Permission();
						newPerm.addRights(rightsList);
						newPerm.setTarget(target);
						profile.addPermission(newPerm);
					} catch (JSONException je){
						throw new GeneralException("JSONException adding profile: "+je);
					}
				}
			}
			profile.setDescription(profileDesc);
			profile.setApplication(app);


			role.add(profile);

			_ctx.saveObject(role);
			_ctx.commitTransaction();
			_ctx.decache(role);

			_profilesCreated++;

		}
	}

	private void addMetadata(List<String> tokens) throws GeneralException {
		//  Add Metadata - Adds metadata to an existing role
		//  ------------------------------------------------
		//    Role Name
		//    Meta-attribute Name
		//    Meta-attribute Value
		//    [Optional] datatype. Defaults to string, can be boolean or integer
		//  ------------------------------------------------
		String roleName = tokens.get(1);
		String metaAttr = tokens.get(2);
		String metaVal = tokens.get(3);
		String datatype = "string";
		if(tokens.size()==5) {
			datatype = tokens.get(4);
		}

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {
			Object value=metaVal;
			if("boolean".equals(datatype)) value=Boolean.parseBoolean(metaVal);
			if("integer".equals(datatype)) value=Integer.parseInt(metaVal);
			role.setAttribute(metaAttr, value);
			_ctx.saveObject(role);
			_ctx.commitTransaction();
			_ctx.decache(role);
		} else {
			_log.error("RoleImporter : ADD METADATA - Role [" + roleName + "] not found.");
		}
	}

	private void addDescription(List<String> tokens) throws GeneralException {
		//  Add Description - Adds localized desc to existing role
		//  ------------------------------------------------------
		//    Role Name
		//    Description
		//    Locale
		//  ------------------------------------------------

		String roleName = tokens.get(1);
		String desc = tokens.get(2);
		String locale = tokens.get(3);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {
			_log.debug("...ADD DESCRIPTION: using locale of: " + locale);
			setDescription(role, desc, locale);  
			_ctx.saveObject(role);
			_ctx.commitTransaction();
			_ctx.decache(role);
		} else {
			_log.error("RoleImporter : ADD DESCRIPTION - Role [" + roleName + "] not found.");
		}
	}

	private void addAssignmentRule(List<String> tokens) throws GeneralException {
		//  Add AssignmentRule - Adds a rule as the assignment logic
		//  ------------------------------------------------------
		//    Role Name
		//    Rule Name
		//  ------------------------------------------------


		String roleName = tokens.get(1);
		String ruleName = tokens.get(2);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {

			Rule rule = _ctx.getObjectByName(Rule.class, ruleName);
			if (null != rule) {

				IdentitySelector selector=new IdentitySelector();
				selector.setRule(rule);
				role.setSelector(selector);

				_ctx.saveObject(role);
				_ctx.commitTransaction();
				_ctx.decache(role);
			} else {
				_log.error("RoleImporter : ADD ASSIGNMENTRULE - Rule [" + ruleName + "] not found.");
			}
		} else {
			_log.error("RoleImporter : ADD ASSIGNMENTRULE - Role [" + roleName + "] not found.");
		}
	}

	private void addAccountSelector(List<String> tokens) throws GeneralException {
		//  Add Account Selector - adds an account selector rule
		//  ------------------------------------------------------
		//    Role Name
		//    Rule Name
		//    Optional: Application
		//  ------------------------------------------------


		String roleName = tokens.get(1);
		String ruleName = tokens.get(2);
		String applicationName = null;
		if(tokens.size()>3) {
			applicationName = tokens.get(3);
		}

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {

			Rule rule = _ctx.getObjectByName(Rule.class, ruleName);
			if (null != rule) {

				AccountSelectorRules asr=role.getAccountSelectorRules();
				if(asr==null) asr=new AccountSelectorRules();

				if(applicationName==null) {

					asr.setBundleLevelAccountSelectorRule(rule);
				} else {
					Application appl = _ctx.getObjectByName(Application.class, applicationName);
					if(null == appl) {
						_log.error("RoleImporter : ADD ACCOUNTSELECTOR - Application [" + applicationName + "] not found.");
						return;
					}
					ApplicationAccountSelectorRule aasr=new ApplicationAccountSelectorRule(appl, rule);
					List<ApplicationAccountSelectorRule> rules=asr.getApplicationAccountSelectorRules();
					if(rules==null) rules=new ArrayList<ApplicationAccountSelectorRule>();
					rules.add(aasr);
					asr.setApplicationAccountSelectorRules(rules);
				}

				role.setAccountSelectorRules(asr);
				_ctx.saveObject(role);
				_ctx.commitTransaction();
				_ctx.decache(role);
			} else {
				_log.error("RoleImporter : ADD ACCOUNTSELECTOR - Rule [" + ruleName + "] not found.");
			}
		} else {
			_log.error("RoleImporter : ADD ACCOUNTSELECTOR - Role [" + roleName + "] not found.");
		}
	}

	private void 
	addRoleAssignment(List<String> tokens) throws GeneralException 
	{
		//  Add Role Assignment - adds a role assignment to identity
		//  --------------------------------------------------------
		//    Role Name
		//    Identity
		//    Optional: The entity responsible for the assignment
		// 
		//    Example:
		//
		//    Add RoleAssignment,data analyst,Amanda.Ross
		//    Add RoleAssignment,Benefits Clerk,Amanda.Ross,Bulk Import
		//  --------------------------------------------------------

		String roleName = tokens.get(1);
		String identity = tokens.get(2);
		String requester = null;

		try {
			requester = tokens.get(3);
		} catch (IndexOutOfBoundsException e) {
			// no optional argument
		}

		_log.debug("ADD ROLEASSIGNMENT,"+roleName+","+identity);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null != role) {
			Identity cube = _ctx.getObjectByName(Identity.class, identity);
			if (null != cube) {
				String cubeName = cube.getName();
				ProvisioningPlan plan = new ProvisioningPlan();
				plan.setIdentity(cube);
				plan.add(ProvisioningPlan.APP_IIQ, cubeName, ProvisioningPlan.ATT_IIQ_ASSIGNED_ROLES, ProvisioningPlan.Operation.Add, roleName);
				Provisioner p = new Provisioner(_ctx);
				p.setNoRoleExpansion(true);
				p.setSource("Task");
				// allow import file to have custom requester string
				if (null != requester) {
					p.setRequester(requester);
				} else {
					p.setRequester(_ctx.getUserName());
				}
				p.execute(plan);
				_rolesAssigned++;
			} else {
				_log.error("RoleImporter : ADD ROLEASSIGNMENT - Identity [" + identity + "] not found.");
			}
		} else {
			_log.error("RoleImporter : ADD ROLEASSIGNMENT - Role [" + roleName + "] not found.");
		}
	}

	private void addScopeAssignment(List<String> tokens) throws GeneralException
	{
		//  Add ScopeAssignment - Assigns a role to a scope
		//  ------------------------------------------------------
		//    Role Name
		//    Scope Name
		//  ------------------------------------------------


		String roleName = tokens.get(1);
		String scopeName = tokens.get(2);

		Bundle role = _ctx.getObjectByName(Bundle.class, roleName);
		if (null == role) {
			_log.error("RoleImporter : ADD SCOPEASSIGNMENT - Role [" + roleName + "] not found.");
		} else {
			Scope scope = _ctx.getObjectByName(Scope.class, scopeName);
			if (null == scope) {
				_log.error("RoleImporter : ADD SCOPEASSIGNMENT - Scope for Role [" + roleName + "] not found.");
			} else {
				role.setAssignedScope(scope) ;

				_ctx.saveObject(role);
				_ctx.commitTransaction();
				_ctx.decache(role);
				_log.debug("RoleImporter : ADD SCOPEASSIGNMENT - Scope [" + scopeName + "] assigned to role [" + roleName + "].");
				_scopesAssigned++;
	        }
		}
}
	
	private void addScope(List<String> tokens) throws GeneralException
	{
		//  Add Scope - Creates a scope and move it under the parent if given
		//  ------------------------------------------------------
		//    Scope Name
		//    parentScope Name
		//  ------------------------------------------------


		String scopeName = tokens.get(1);
		String parentScopeName = tokens.get(2);

		Scope scope = _ctx.getObjectByName(Scope.class, scopeName);
		if( null != scope ) {
			_log.error("RoleImporter : ADD SCOPE - Scope [" + scopeName +"] already exists");
		} else {
			scope = new Scope(scopeName);
			_ctx.saveObject(scope) ;
			_ctx.commitTransaction();
			_ctx.decache(scope);
			_log.debug("RoleImporter : ADD SCOPE - Scope [" + scopeName +"] created");
			_scopesCreated++;
		}

		Scope parentScope = _ctx.getObjectByName(Scope.class, parentScopeName);
		
		if (null == parentScope) {
			_log.warn("RoleImporter : ADD SCOPE - No Parent scope given");			
		} else {
            parentScope.addScope(scope);
			_ctx.saveObject(parentScope);
			_ctx.commitTransaction();
			_ctx.decache(parentScope);
			_log.debug("RoleImporter : ADD SCOPE - Scope [" + scopeName + "] attached to parent [" + parentScopeName + "]");
		}	  
	}
}
