package sailpoint.seri.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.Aggregator;
import sailpoint.api.CertificationScheduler;
import sailpoint.api.Certificationer;
import sailpoint.api.EntitlementCorrelator;
import sailpoint.api.ObjectUtil;
import sailpoint.api.SailPointContext;
import sailpoint.api.ScoreKeeper;
import sailpoint.api.TaskManager;
import sailpoint.api.Workflower;
import sailpoint.api.certification.CertCreationHelper;
import sailpoint.object.Application;
import sailpoint.object.Attributes;
import sailpoint.object.Certification;
import sailpoint.object.CertificationAction;
import sailpoint.object.CertificationDefinition;
import sailpoint.object.CertificationDelegation;
import sailpoint.object.CertificationEntity;
import sailpoint.object.CertificationItem;
import sailpoint.object.CertificationItemSelector;
import sailpoint.object.CertificationSchedule;
import sailpoint.object.EntitlementSnapshot;
import sailpoint.object.Filter;
import sailpoint.object.GroupDefinition;
import sailpoint.object.GroupFactory;
import sailpoint.object.GroupIndex;
import sailpoint.object.Identity;
import sailpoint.object.Link;
import sailpoint.object.ProvisioningPlan;
import sailpoint.object.ProvisioningPlan.AccountRequest;
import sailpoint.object.ProvisioningPlan.AttributeRequest;
import sailpoint.object.QueryOptions;
import sailpoint.object.QuickLink;
import sailpoint.object.SailPointObject;
import sailpoint.object.ScoreConfig;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.object.UIPreferences;
import sailpoint.object.Widget;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowLaunch;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.task.IdentityRefreshExecutor;
import sailpoint.task.ResourceIdentityScan;
import sailpoint.tools.GeneralException;
import sailpoint.tools.RFC4180LineIterator;
import sailpoint.tools.RFC4180LineParser;
import sailpoint.tools.Util;
import sailpoint.tools.xml.XMLObjectFactory;
import sailpoint.web.certification.BulkCertificationHelper;

/**
 * A task that causes background changes to help walk through a demo scenario.
 *
 * @author Nicholas Crown (adapted from work by Kelly Grizzle, Jeff Larson, and
 *         others...)
 *         
 *         ...and bastardized/bludgeoned by Sean Koontz
 */
public class SEDemoSetupExecutor extends AbstractTaskExecutor {
  private static final Log log = LogFactory.getLog(SEDemoSetupExecutor.class);

  public class NotActedUponCertificationItemSelector implements CertificationItemSelector
  {
    public boolean matches(CertificationItem item) throws GeneralException
    {
      return(!item.isActedUpon());
    }
  }

  /**
   * Simple class to store the scripted certActions we want to perform on a certification.
   */
  public class ScriptedCertificationAction
  {
    public String certifierName;
    public String identityName;
    public String type;
    public String applicationName;
    public String key;
    public String action;
    public String comment;
  }

  public class AccessRequestAction
  {
    public List<String> recipients;
    public List<String> roles;
    public String requester;
  }

  /***
   * DashboardAction encapsulates the setting of some dashboard configuration
   * recipient is the cube name
   * action is one of: Set (Sets the list to the objects specified)
   *                   SetNot (Sets the list to all objects except those specified)
   *                   SetNone (Sets the list to 'none')
   *                   Add (Adds the objects specified to what is already there)
   *                   Remove (Removes the specified objects; if none currently exist, it's the same as SetNot)
   * @author kevin.james
   *
   */
  public class DashboardAction
  {
    public String recipient;
    public String action;
    public String objectType;
    public String objects;
  }


  // ////////////////////////////////////////////////////////////////////
  //
  // Arguments
  //
  // ////////////////////////////////////////////////////////////////////

  /**
   * Constants for the modes in which this task can be run.
   */
  public static enum Mode {

    /**
     * Mode to run to seed the demo with data. This will perform Account
     * Aggregation, Entitlement Correlation, and other setup steps.
     */
    CUBE_SETUP,

    /**
     * Mode to run to setup the certification demo. This will include:
     * GENERATE_INITIAL_CERTIFICATIONS, ACT_ON_CERTIFICATIONS, and
     * GENERATE_CERTIFICATION_DIFFS.
     */
    CERT_SETUP,

    /**
     * Mode to run to generate some differences that will show up in a
     * certification.
     */
    GENERATE_CERT_DIFFS,

    /**
     * Mode to run to randomly act upon some certifications so that you get
     * nice Christmas tree colors in the dash board graphs.
     */
    ACT_ON_CERTS,

    /**
     * Mode to run to generate identity and group history data to feed the
     * trending charts and reports.
     */
    TRENDING_SETUP,

    /**
     * Mode to run to generate access requests to populate the new 7.0
     * dashboard widget 'Latest Approvals'.
     */
    REQUEST_SETUP,

    /**
     * Mode to run a configuration of a user's dashboard content
     */
    DASHBOARD_SETUP,
    /**
     * Mode that resets back to a base state.
     */
    RESET;

    public static Mode fromString(String modeArg) {
      if(modeArg==null) return Mode.CUBE_SETUP;
      return Mode.valueOf(Mode.class, modeArg);
    }

  }

  public enum ActionType {
    QUICKLINK ("quicklink", "userQuickLinkCards"),
    WIDGET ("widget", "userHomeWidgets");

    private final String type;
    private final String uiPrefKey;

    ActionType(String type, String prefKey) {
      this.type=type;
      this.uiPrefKey=prefKey;
    }

    public static ActionType getType(String type) {
      if(type==null) return null;
      for (ActionType possibleType: ActionType.values()) {
        if (possibleType.type.equalsIgnoreCase(type.toLowerCase())) return possibleType;
      }
      return null;
    }
  }

  /**
   * The name of the argument that holds the mode in which this task is being
   * run. This can be run in different modes to set up different parts of the
   * demo.
   */
  public static final String ARG_MODE = "mode";

  /**
   * Names of applications to scan. Either this or ARG_APPLICATION is
   * required, the former is provided for temporary backward compatibility.
   */
  public static final String ARG_APPLICATIONS = "applications";

  /**
   * Specifies the name of the identity who should be the cert group
   * owner.
   */
  public static final String ARG_CERT_GROUP_OWNER = "certGroupOwner";

  /**
   * The name of the argument that holds the name of the manager for which to
   * re-generate the certification.
   */
  public static final String ARG_CERT_MANAGER_NAME = "certManagerName";

  /**
   * The name of the argument that holds the name of the application for which
   * to generate the certification.
   */
  public static final String ARG_CERT_APP_NAME = "certAppName";

  /**
   * The name of the argument that holds the name of the erp application.
   *
   */
  public static final String ARG_CERT_TWEAK_APP_NAME = "certTweakAppName";

  /**
   * The name of the argument that holds the name of the user whose
   * entitlements should be tweaked.
   */
  public static final String ARG_CERT_FOCUS_USER = "certFocusUser";

  /**
   * The name of the argument that holds the name of the user who should
   * receive the delegation and remediation requests.
   */
  public static final String ARG_CERT_REMEDIATION_USER = "certRemediationUser";

  /**
   * The name of the argument that holds the list of users to tweak for
   * generating differences.
   */
  public static final String ARG_CERT_TWEAK_USERS = "certTweakUsers";

  /**
   * The name of the argument that holds the name of the entitlement to use
   * when tweaking users.
   */
  public static final String ARG_CERT_TWEAK_ENT_NAME = "certTweakEntName";

  /**
   * The name of the argument that holds the map of entitlements to use while
   * tweaking users.
   */
  public static final String ARG_CERT_TWEAK_ENTS = "certTweakEnts";

  public static final String ARG_CERT_FOCUS_APP_NAME = "certFocusApp";
  public static final String ARG_CERT_FOCUS_ENTS = "certFocusEnts";
  public static final String ARG_CERT_FOCUS_ENT_NAME = "certFocusEntName";

  /**
   * The name of the argument that holds the map of identities to managers for
   * use in generating an ad-hoc Identity Certification.
   */
  public static final String ARG_IDENTITY_MANAGER_MAP = "identityManagers";

  /**
   * The name of the argument that holds the Score Config.
   */
  public static final String ARG_SCORE_CONFIG = "scoreConfig";

  /**
   * The name of the argument that holds the GroupFactory for trending.
   */
  public static final String ARG_GROUP_FACTORY_NAME = "groupFactoryName";

  /**
   * The name of the argument that holds the group to highlight in trending.
   */
  public static final String ARG_TRENDING_GROUP = "trendingGroup";

  /**
   * The name of the argument that holds the number of months to generate
   * history.
   */
  public static final String ARG_TRENDING_MONTHS = "trendingMonths";

  /**
   * The name of the argument that holds the max amount for the composite
   * score.
   */
  public static final String ARG_COMPOSITE_MAX = "maxComposite";

  /**
   * The name of the argument that holds the max amount for the business role
   * score.
   */
  public static final String ARG_BUSINESS_ROLE_MAX = "maxBusinessRole";

  /**
   * The name of the argument that holds the max amount for the entitlement
   * score.
   */
  public static final String ARG_ENTITLEMENT_MAX = "maxEntitlement";

  /**
   * The name of the argument that holds the max amount for the policy score.
   */
  public static final String ARG_POLICY_MAX = "maxPolicy";

  /**
   * The name of the argument that holds the max amount for the certification
   * score.
   */
  public static final String ARG_CERT_MAX = "maxCert";

  /**
   * The name of the argument that holds the member count for the trending
   * group.
   */
  public static final String ARG_DEFAULT_GROUP_MEMBER_COUNT = "defaultGroupMemberCount";

  public static final String ARG_ACTIONS_FILE_PATH = "actionsFile";

  private static final String ARG_CERTS_TO_COMPLETE = "certsToComplete";

  /**
   * The name of the argument that holds the max percentage of certs 
   * to be completed. 
   */
  
  public static final String ARGS_MAX_PERCENT_CERTS_TO_COMPLETE = "maxPercentCertsToComplete";

  // ////////////////////////////////////////////////////////////////////
  //
  // Fields
  //
  // ////////////////////////////////////////////////////////////////////

  /**
   * Context given to us by the scheduler. We can commit transactions.
   */
  private SailPointContext context;

  /**
   * TaskResult given to us by the scheduler.
   */
  private TaskResult result;

  /**
   * Arguments given to us by the scheduler.
   */
  private Attributes<String, Object> args;

  /**
   * Set by the terminate method to indiciate that we should stop when
   * convenient.
   */
  private boolean terminate;

  /**
   * Internal score keeper for calculating random scores for trending.
   */
  ScoreKeeper _scoreKeeper;

  /**
   * Cached score configuration.
   */
  ScoreConfig _scoreConfig;

  /**
   * Random number generator for generateHistory().
   */
  Random _random;

  private List<ScriptedCertificationAction> certActions;
  private List<AccessRequestAction> reqActions;
  private List<DashboardAction> dashActions;

  // ////////////////////////////////////////////////////////////////////
  //
  // TaskExecutor Interface
  //
  // ////////////////////////////////////////////////////////////////////

  public SEDemoSetupExecutor() {
  }

  /**
   * Terminate at the next convenient point.
   */
  public boolean terminate() {
    this.terminate = true;
    return true;
  }


  /**
   * Run the demo setup.
   */
  public void execute(SailPointContext context, TaskSchedule sched,
      TaskResult result, Attributes<String, Object> args)
          throws Exception {

    this.context = context;
    this.result = result;
    this.args = args;
    this.terminate = false;

    for (String key : args.keySet()) {
      log.debug("task arg: " + key + " = '" + args.getString(key) + "'");
    }

    // Load the scripted certActions.
    String actionsFilePath = args.getString(ARG_ACTIONS_FILE_PATH);
    // Decide what kind of input file we are dealing with
    Mode actionType = Mode.fromString(args.getString(ARG_MODE));

    certActions = new ArrayList<ScriptedCertificationAction>();
    reqActions = new ArrayList<AccessRequestAction>();
    dashActions = new ArrayList<DashboardAction>();

    if(actionsFilePath != null) {
      RFC4180LineParser parser = new RFC4180LineParser(',');

      File file = new File(Util.findFile(actionsFilePath));

      if(file.canRead()) {
        FileReader fileReader = new FileReader(file);

        BufferedReader reader = new BufferedReader(fileReader);

        RFC4180LineIterator iterator = new RFC4180LineIterator(reader);

        String line = null;

        while((line = iterator.readLine()) != null) {
          ArrayList<String> fields = parser.parseLine(line);
          if (actionType==Mode.CERT_SETUP) {
            ScriptedCertificationAction action = new ScriptedCertificationAction();

            action.certifierName = fields.get(0);
            action.identityName = fields.get(1);
            action.type = fields.get(2);
            action.applicationName = fields.get(3);
            action.key = fields.get(4);
            action.action = fields.get(5);
            action.comment = fields.get(6);

            certActions.add(action);
          } else if (actionType==Mode.REQUEST_SETUP) {
            AccessRequestAction action = new AccessRequestAction();

            action.recipients = stringToList(fields.get(0));
            action.roles = stringToList(fields.get(1));
            action.requester = fields.get(2);

            reqActions.add(action);
          } else if (actionType==Mode.DASHBOARD_SETUP) {
            DashboardAction action=new DashboardAction();
            action.recipient=fields.get(0);
            action.action=fields.get(1);
            action.objectType=fields.get(2);
            action.objects=fields.get(3);
            dashActions.add(action);
          }
        }
      }

      if (actionType==Mode.CERT_SETUP) {
        log.info("Loaded " + certActions.size() + " action(s)");
      } else if (actionType==Mode.REQUEST_SETUP) {
        log.info("Loaded " + reqActions.size() + " action(s)");
      }
    }

    /*
     * The certifications that are being imported have the app name stored
     * in the application id field. We need to update that with the current
     * application id
     */

    try {
      // Get the mode of operation. Default to DEMO_CUBE_SETUP.
      String modeArg = args.getString(ARG_MODE);
      Mode mode = Mode.fromString(modeArg);

      switch (mode) {
        case CUBE_SETUP:
          cubeSetup();
          break;
        case CERT_SETUP:
          fixImportedCerts();
          certSetup();
          break;
        case GENERATE_CERT_DIFFS:
          generateCertDiffs();
          break;
        case ACT_ON_CERTS:
          actOnCerts();
          break;
        case TRENDING_SETUP:
          trendingSetup();
          break;
        case REQUEST_SETUP:
          accessRequestSetup();
          break;
        case DASHBOARD_SETUP:
          dashboardSetup();
          break;
        case RESET:
          reset();
          break;
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }

  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setup the demo data from a clean install
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Executes the intial Account Aggregation, Entitlement Correlation, etc...
   * tasks It expects that the appropriate configuration files have already
   * been imported.
   */
  private void cubeSetup() throws GeneralException {
    log.debug("Running cubeSetup");

    try {
      // 1. Aggregate Accounts.
      ResourceIdentityScan aaTask = new ResourceIdentityScan();
      Attributes<String, Object> newArgs = new Attributes<String, Object>();
      newArgs.put(Aggregator.ARG_APPLICATIONS, args
          .get(Aggregator.ARG_APPLICATIONS));
      newArgs.put(Aggregator.ARG_NO_MANAGER_CORRELATION, args
          .get(Aggregator.ARG_NO_MANAGER_CORRELATION));
      aaTask.execute(context, null, result, newArgs);

      if (terminate)
        return;

      // 2. Correleate Entitlements, Refresh Risk Scores, and Check
      // Policies.
      IdentityRefreshExecutor refreshTask = new IdentityRefreshExecutor();
      refreshTask.execute(context, null, result, args);

      if (terminate)
        return;
    } catch (Exception e) {
      throw new GeneralException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setup the cert demo
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  private void certSetup() throws Exception {
    log.debug("Running certSetup");

    // 1. Generate some initial certifications
    generateInitialCertifications();

    // 2. Generate some differences for the certifications
    generateCertDiffs();

    // 3. Act upon some certifications
    //actOnCerts();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setup the trending demo
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  private void trendingSetup() throws GeneralException {
    log.debug("Running trendingSetup");

    prepareScoreKeeper();

    if (terminate)
      return;

    // 1. Execute an IdentityRefresh task to generate current group indexes.
    try {
      Attributes<String, Object> newArgs = new Attributes<String, Object>();
      newArgs.put(IdentityRefreshExecutor.ARG_REFRESH_GROUPS, "true");

      TaskManager taskMan = new TaskManager(context);
      taskMan.runSync("Identity Refresh", newArgs);
    } catch (Exception e) {
      throw new GeneralException(e);
    }

    if (terminate)
      return;

    // 2. Generate history for a particular GroupFactory
    generateGroupHistory();
    // GroupDefinition group =
    // context.getObjectByName(GroupDefinition.class, "Accounting");
    // generateHistory(group);

    if (terminate)
      return;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Setup a bunch of access requests
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  private void accessRequestSetup() throws GeneralException {
    log.debug("Running access request Setup");

    String workflowName="LCM Provisioning"; // might parameterize this later

    for (AccessRequestAction action: reqActions) {

      // generate args. They'll be the same for each recipient        

      // Separate request for each recipient..
      for (String recipient: action.recipients) {

        Map<String, Object> args=new HashMap<String,Object>();
        ProvisioningPlan plan=new ProvisioningPlan();
        plan.setNativeIdentity(recipient);
        AccountRequest accReq=new AccountRequest(AccountRequest.Operation.Modify, "IIQ", null, recipient);
        for (String role: action.roles) {
          accReq.add(new AttributeRequest("assignedRoles", ProvisioningPlan.Operation.Add, role));
        }
        plan.add(accReq);
        args.put("identityName", recipient);
        args.put("flow", "AccessRequest");
        args.put("notificationScheme", "none"); // don't want a bunch of emails every time we run this
        args.put("plan", plan);

        if (workflowName!=null) {
          Workflow wf = (Workflow) context.getObjectByName(Workflow.class,workflowName);
          WorkflowLaunch wflaunch = new WorkflowLaunch();
          wflaunch.setWorkflowName(wf.getName());
          wflaunch.setWorkflowRef(wf.getName());
          wflaunch.setLauncher(action.requester);
          wflaunch.setVariables(args);

          //Create Workflower and launch workflow from WorkflowLaunch
          Workflower workflower = new Workflower(context);
          WorkflowLaunch launch = workflower.launch(wflaunch);
          // print workflowcase ID (example only; might not want to do this in the task)
          String workFlowId = launch.getWorkflowCase().getId();
          log.debug("\twf="+workflowName);
          log.debug("\tworkFlowId: "+workFlowId);
        }

      }

    }

  }

  private void dashboardSetup() throws GeneralException {
    log.debug("Running dashboard Setup");

    for (DashboardAction action: dashActions) {

      Identity iden=context.getObjectByName(Identity.class, action.recipient);
      if (iden==null) {
        log.warn("dashboardSetup: Identity "+action.recipient+" not found");
        continue;
      }

      // Find the QueryOptions object; a reference to it is stored on the Identity
      UIPreferences prefObject=iden.getUIPreferences();
      if(prefObject==null) {
        prefObject=new UIPreferences();
        prefObject.setOwner(iden);
        prefObject.put("userHomeContentOrder", "QuickLink, Widget");
      }

      // Get the Action Type; this is (currently) either widget or quicklink
      // This way we can code the key to put() the value to in the enum 
      ActionType aType=ActionType.getType(action.objectType);

      /* action is one of: Set (Sets the list to the objects specified)
       *   SetNot (Sets the list to all objects except those specified)
       *   SetNone (Sets the list to 'none')
       *   Add (Adds the objects specified to what is already there)
       *   Remove (Removes the specified objects; if none currently exist, it's the same as SetNot)
       */
      if (action.action.equalsIgnoreCase("SetNone")) {
        // Just put 'None' into the UIPreferences
        prefObject.put(aType.uiPrefKey, "None");

      } else if (action.action.equalsIgnoreCase("Set")) {
        // Set the value in UIPreferences to a specific list. Convert the separator from | to ','
        String[] items=action.objects.split("\\|");
        String value=concatList(Arrays.asList(items));
        prefObject.put(aType.uiPrefKey, value);

      } else if (action.action.equalsIgnoreCase("SetNot")) {
        // Get a list of all the possible values. Remove the specified ones. Store the resulting list
        List allElements=null;
        if (aType==ActionType.QUICKLINK) {
          allElements=getAllQuickLinks();
        } else if (aType==ActionType.WIDGET) {
          allElements=getAllWidgets();
        }
        if (allElements!=null) {
          String[] items=action.objects.split("\\|");
          for(String item: items) {
            allElements.remove(item);
          }
          prefObject.put(aType.uiPrefKey, concatList(allElements));
        }

      } else if (action.action.equalsIgnoreCase("Add")) {
        // Get the current list. Add the specified values. put it back
        List<String> lCurrent=new ArrayList<String>();
        String currentList=(String)prefObject.get(aType.uiPrefKey);
        if(currentList!=null) {
          String[] sCurrent=currentList.split(",");
          for(String sc: sCurrent) {
            lCurrent.add(sc.trim());
          }
        }
        lCurrent.addAll(Arrays.asList(action.objects.split("\\|")));
        prefObject.put(aType.uiPrefKey, concatList(lCurrent));

      } else if (action.action.equalsIgnoreCase("remove")) {
        // Get the current list. Remove the specified values. Put it back
        // If the current list is empty, make it 'all things' then remove the
        // specified values
        List<String> lCurrent=new ArrayList<String>();
        String currentList=(String)prefObject.get(aType.uiPrefKey);

        if (currentList==null) {
          if (aType==ActionType.QUICKLINK) {
            lCurrent=getAllQuickLinks();
          } else if (aType==ActionType.WIDGET) {
            lCurrent=getAllWidgets();
          }
        } else {
          String[] sCurrent=currentList.split(",");
          for(String sc: sCurrent) {
            lCurrent.add(sc.trim());
          }
        }

        for (String item: action.objects.split("\\|")) {
          lCurrent.remove(item);
        }

        prefObject.put(aType.uiPrefKey, concatList(lCurrent));

      }
      context.saveObject(prefObject);
      // Now we've saved the preferences object, we need to make sure the Identity has a reference to it
      iden.setUIPreferences(prefObject);
      context.saveObject(iden);

      context.commitTransaction();
    }

  }

  private String concatList(Iterable<String> items) {
    StringBuilder setValue=new StringBuilder();
    boolean first=true;
    for(String item: items) {
      if(first) first=false;
      else setValue.append(", ");
      setValue.append(item);
    }
    return setValue.toString();
  }

  private List<String> getAllQuickLinks() throws GeneralException {
    return getAllObjects(QuickLink.class);
  }

  private List<String> getAllWidgets() throws GeneralException {
    return getAllObjects(Widget.class);
  }

  private List<String> getAllObjects(Class clazz) throws GeneralException {
    List<SailPointObject> objects=context.getObjects(clazz);
    List<String> names=new ArrayList<String>();
    for(SailPointObject obj: objects) {
      names.add(obj.getName());
    }
    return names;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generate the initial certifications
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Get the certifier user.
   */
  private Identity getCertifier() throws GeneralException {

    return context.getObjectByName(Identity.class, "spadmin");
  }

  private Identity getCertifier(boolean appOwner) throws GeneralException
  {
    String name = null;

    if(appOwner)
      name = null;
    else
      name = args.getString(ARG_CERT_MANAGER_NAME);

    return(context.getObjectByName(Identity.class, name));
  }

  private Identity getCertifier(String identity) throws GeneralException
  {
    return(context.getObjectByName(Identity.class, identity));
  }

  /**
   * Generate some certifications.
   *
   */
  private void generateInitialCertifications() throws GeneralException {

    try {

      // 1. Generate a manager certification.
      Identity launcher =
          this.context.getObjectByName(Identity.class, "spadmin");
      String managerName = (String) args.get(ARG_CERT_MANAGER_NAME);

      // Create the CertificationDefinition : Modified due to refactoring in 5.2
      CertificationDefinition definition = CertCreationHelper.createCertDefinition(this.context, 
          new CertCreationHelper.SimpleCertDef(launcher, managerName));

      definition.setProcessRevokesImmediately(true);
      definition.setCertificationNameTemplate(generateCertName("Manager Certification Campaign : ", true));

      String certGroupOwner = (String) args.get(ARG_CERT_GROUP_OWNER);
      if (null == certGroupOwner) certGroupOwner = "spadmin";
      Identity certOwner = this.context.getObjectByName(Identity.class, certGroupOwner);
      definition.setCertificationOwner(certOwner);
      definition.setCertPageListItems(Util.atob("false"));

      // Make the decisions in the cert authoritative for desired state
      //
      definition.setUpdateAttributeAssignments(true);

      // exclude 'All Users' and 'User Basic'
      definition.setExclusionRuleName("Exclude Base Roles and HR App for All Users");

      // Launch the global manager cert.
      CertificationScheduler scheduler =
          new CertificationScheduler(this.context);
      CertificationSchedule schedule = new CertificationSchedule(this.context, launcher, definition);
      schedule.setRunNow(true);

      TaskSchedule ts = scheduler.saveSchedule(schedule, false);

      // Wait on task to finish (allow waiting up to five minutes).
      TaskManager tm = new TaskManager(this.context);
      tm.awaitTask(ts, 300);

      if (terminate)
        return;

      // 2. Generate an application owner certifications. Unfortunately,
      // it takes too long for a global application owner. Therefore,
      // we are generating one real certification and faking the rest.

      // Create the Certification Definition
      definition = CertCreationHelper.createCertDefinition(this.context,
          new CertCreationHelper.SimpleCertDef(launcher, Certification.Type.ApplicationOwner, false));

      // Add applications to be certified
      String appNames = args.get(ARG_CERT_APP_NAME).toString();
      if (appNames != null) {
        List<String> appIds = new ArrayList<String>();
        for (String appName: appNames.split("\\|")) {
          Application app = context.getObject(Application.class, appName);
          appIds.add(app.getId());
        }

        definition.setApplicationIds(appIds);

        // Set definition properties
        definition.setProcessRevokesImmediately(true);
        definition.setCertificationOwner(certOwner);
        definition.setCertificationNameTemplate(generateCertName("App Owner Certification Campaign : ", false));

        // Make the decisions in the cert authoritative for desired state
        //
        definition.setUpdateAttributeAssignments(true);

        // Launch the targeted application owner cert.
        scheduler = new CertificationScheduler(this.context);
        schedule = new CertificationSchedule(this.context, launcher, definition);
        schedule.setRunNow(true);

        ts = scheduler.saveSchedule(schedule, false);

        // Wait on task to finish (allow waiting up to five minutes).
        tm.awaitTask(ts, 300);

        if (terminate)
          return;
      }

      /*
       * // 3. Generate an advanced (Identity based) certification.
       * certArgs.put(CertificationExecutor.ARG_CERTIFICATION_TYPE,
       * CertificationExecutor.Type.Individual); // Kludge! We need to
       * replace the manager name with an id String identsManager =
       * args.getString(ARG_IDENTITY_MANAGER_MAP); Map<String, String>
       * identMgrMap =
       * Util.stringToMap(args.getString(ARG_IDENTITY_MANAGER_MAP));
       *
       * Collection<String> entries = identMgrMap.values();
       * for(Iterator<String> entryIter = entries.iterator();
       * entryIter.hasNext();) { String mgrStr = entryIter.next();
       * if(identsManager.indexOf(mgrStr) >= 0) { Identity mgr =
       * context.getObjectByName(Identity.class, mgrStr); identsManager =
       * identsManager.replaceAll(mgrStr, mgr.getId()); } }
       * certArgs.put(CertificationExecutor.ARG_IDENTITY_MANAGER_MAP,
       * identsManager); certTask.execute(context, null, result,
       * certArgs);
       *
       * if (terminate) return;
       */

    } catch (Exception e) {
      throw new GeneralException(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generate certification differences
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Generate certification diffs for the requested application or manager
   * certification. Note that this will only display diffs when run for the
   * first time for each app owner/manager because the same differences are
   * generated each time.
   *
   */
  @SuppressWarnings("unchecked")
  private void generateCertDiffs() throws Exception 
  {
    // Step 1 - Sign off on previous certification.
    String managerName = args.getString(ARG_CERT_MANAGER_NAME);
    String appName = args.getString(ARG_CERT_APP_NAME);

    // TODO: Parameterize this value and support tweaking an appOwner cert.
    boolean appOwner = false;
    String tweakAppName = args.getString(ARG_CERT_TWEAK_APP_NAME);
    String certFocusUser = args.getString(ARG_CERT_FOCUS_USER);
    List<String> certTweakUsers = Util.stringToList(args.getString(ARG_CERT_TWEAK_USERS));
    String certTweakEnt = args.getString(ARG_CERT_TWEAK_ENT_NAME);
    String certTweakEnts = args.getString(ARG_CERT_TWEAK_ENTS);
    String[] ents = certTweakEnts.split(";");

    List<String> usersToCorrelate = new ArrayList<String>();

    if (appOwner) {
      signOffOnPrevious(appName, appOwner);
    } else {
      // 5.1 Port - Look for subordinate manager certs as well
      //
      List<String> subordinateManagers = getSubordinateManagers(managerName);
      for (String subordinateManager : subordinateManagers) {
        //log.info("...signing off previous cert for: " + subordinateManager);
        signOffOnPrevious(subordinateManager, false);
      }

      signOffOnPrevious(managerName, appOwner);
    }

    // Run the perform maintenance task to progress the completed certification.
    performMaintenance();

    // Steps 2-4 - Modify the identity to add/remove business roles and
    // entitlements.
    swizzleEntitlements(certFocusUser);
    usersToCorrelate.add(certFocusUser);

    // Step 5 - Add simple additional entitlements to other users.
    for (String userName : certTweakUsers) {
      // TODO: Parameterize the number of entitlements that are added.
      for (int i = 0; i < ents.length; i++) {
        addAttributeValue(userName, tweakAppName, certTweakEnt, ents[i]);
      }
      usersToCorrelate.add(userName);
    }

    // Step 6 - Correlate entitlements.
    EntitlementCorrelator ec = new EntitlementCorrelator(context);
    for (String userName : usersToCorrelate) {
      ec.processIdentity(context.getObjectByName(Identity.class, userName));
    }

    // Step 7 - Generate new certification.

    /* Certificationer certificationer = new Certificationer(context);

        CertificationBuilder builder = null;
        if (appOwner) {
            Application app = context.getObjectByName(Application.class,
                    appName);
            List<String> apps = new ArrayList();
            apps.add(app.getId());
            builder = new AppOwnerCertificationBuilder(context, null, apps,
                    false);
        } else {
            Identity manager = context.getObjectByName(Identity.class,
                    managerName);
            builder = new ManagerCertificationBuilder(context, manager, false);

        }
        builder.setAllowProvisioningRequirements(true);
        builder.setProcessRevokesImmediately(true);

        Certification cert = certificationer.generateCertification(
                getCertifier(), builder.getContext());
        certificationer.start(cert); */

    /*
        Identity launcher = this.context.getObjectByName(Identity.class, "spadmin");
        Identity manager = context.getObjectByName(Identity.class, managerName);

        CertificationScheduler scheduler = new CertificationScheduler(this.context);
        CertificationScheduleDTO dto = new CertificationScheduleDTO(this.context);
        scheduler.initializeScheduleBean(dto, launcher);

        dto.setType(Certification.Type.Manager.toString());
        dto.setCertifier(manager);
        dto.setAllowProvisioningRequirements(true);
        dto.setProcessRevokesImmediately(true);

        // 5.1 Port - Deal with the new CertificationGroup object
        //
        String certGroupOwner = (String) args.get(ARG_CERT_GROUP_OWNER);
        if (null == certGroupOwner) certGroupOwner = "spadmin";
        Identity certOwner = this.context.getObjectByName(Identity.class, certGroupOwner);

        dto.setCertificationOwner(certOwner);
        dto.setCertificationNameTemplate(generateCertName("Manager Certification Campaign : ", false));

        CertificationDefinition def =
            scheduler.buildManagerDefinition(launcher, "Manager Certification: " + managerName, dto);
        TaskSchedule ts = scheduler.saveSchedule(def, dto, true, false);

        // Wait on task to finish (allow waiting up to five minutes).
        TaskManager tm = new TaskManager(this.context);
        tm.awaitTask(ts, 300);
     */

    Identity launcher =
        this.context.getObjectByName(Identity.class, "spadmin");
    managerName = (String) args.get(ARG_CERT_MANAGER_NAME);

    // Create the CertificationDefinition : Modified due to refactoring in 5.2
    CertificationDefinition definition = CertCreationHelper.createCertDefinition(this.context, 
        new CertCreationHelper.SimpleCertDef(launcher, managerName));

    definition.setProcessRevokesImmediately(true);
    definition.setAllowProvisioningRequirements(true);
    definition.setAllowExceptionPopup(true);
    definition.setCertificationNameTemplate(generateCertName("Manager Certification Campaign : ", false));

    // Make the decisions in the cert authoritative for desired state
    //
    definition.setUpdateAttributeAssignments(true);

    // exclude 'All Users' and 'User Basic'
    definition.setExclusionRuleName("Exclude Base Roles and HR App for All Users");

    // Include accounts with no entitlements; this drives demo around privileged admin accounts (seank-adm vs. seank)
    definition.setCertifyEmptyAccounts(true);

    String certGroupOwner = (String) args.get(ARG_CERT_GROUP_OWNER);
    if (null == certGroupOwner) certGroupOwner = "spadmin";
    Identity certOwner = this.context.getObjectByName(Identity.class, certGroupOwner);
    definition.setCertificationOwner(certOwner);
    definition.setCertPageListItems(Util.atob("false"));


    // Launch the global manager cert.
    CertificationScheduler scheduler =
        new CertificationScheduler(this.context);
    CertificationSchedule schedule = new CertificationSchedule(this.context, launcher, definition);
    schedule.setRunNow(true);

    TaskSchedule ts = scheduler.saveSchedule(schedule, false);

    // Wait on task to finish (allow waiting up to five minutes).
    TaskManager tm = new TaskManager(this.context);
    tm.awaitTask(ts, 300);

    if (terminate)
      return;

  }

  /**
   * Bulk approve all items that have not been acted upon in a certification entity.
   * 
   * @param entity the certification entity
   * @param certifier the certifier
   * @throws GeneralException
   */
  private void bulkApprove(CertificationEntity entity, Identity certifier) throws GeneralException
  {
    CertificationAction action = new CertificationAction();

    action.approve(certifier, null);
    action.setCreated(new Date());
    action.setActor(certifier);

    CertificationItemSelector itemSelector = new NotActedUponCertificationItemSelector();

    entity.bulkCertify(certifier, null, action, itemSelector, false);
  }

  /**
   * Bulk mitigate all items that have not been acted upon in a certification entity.
   * 
   * Mitigation is not currently supported using the bulkCertify method of the
   * certification entity instead we simply iterate over all policy violations that
   * have not been acted upon and perform the mitigation.  All policy violations are
   * allowed for two months. 
   * 
   * @param entity the certification entity
   * @param certifier the certifier
   * @throws GeneralException
   */
  private void bulkMitigate(CertificationEntity entity, Identity certifier) throws GeneralException
  {
    Calendar calendar = Calendar.getInstance();

    calendar.add(Calendar.MONTH, 2);

    String comment = "Allowing this violation for two months during job transition";

    for(CertificationItem item : getAllLeafs(entity)) {
      if(item.getType() == CertificationItem.Type.PolicyViolation && !item.isActedUpon())
        item.mitigate(context, certifier, null, calendar.getTime(), comment);
    }
  }

  private List<ScriptedCertificationAction> findAction(CertificationItem item, Identity certifier) throws GeneralException
  {
    String identityName = item.getIdentity();
    CertificationItem.Type type = item.getType();
    String applicationName = null;
    String key = null;

    if (type == CertificationItem.Type.Bundle) {
      key = item.getBundle();
    } else if (type == CertificationItem.Type.Exception) {
      EntitlementSnapshot entitlement = item.getExceptionEntitlements();

      applicationName = entitlement.getApplicationName();

      if (entitlement.getAttributes() != null && entitlement.getAttributes().size() > 0)
        key = entitlement.getAttributeName() + "=" + entitlement.getAttributeValue().toString();
      else if (entitlement.getPermissions() != null && entitlement.getPermissions().size() > 0)
        key = entitlement.getPermissionRights() + " on '" + entitlement.getPermissionTarget() + "'";
    } else if (type == CertificationItem.Type.PolicyViolation) {
      key = item.getPolicyViolation().getConstraintName();
    }

    // Very naive implementation.
    List<ScriptedCertificationAction> matchingActions = new ArrayList<ScriptedCertificationAction>();

    for (ScriptedCertificationAction action : certActions) {
      //log.info("action certifier name = " + action.certifierName + "; action identity name = " + action.identityName + "; action type = " + action.type + "; action key = " + action.key);
      //log.info("....certifier name = " + certifier.getName() + "; identityName = " + identityName + "; type = " + type + "; key = " + key);
      if (action.certifierName.equals(certifier.getName()) && action.identityName.equals(identityName) && action.type.equals(type.toString())) {
        if (type == CertificationItem.Type.Bundle) {
          if (action.key.equals(key)) {
            matchingActions.add(action);
          }
        } else {
          if (Util.nullSafeEq(action.applicationName, applicationName) && action.key.equals(key))
            matchingActions.add(action);
        }
      }
    }

    if (matchingActions.isEmpty())
      return(null);

    return(matchingActions);
  }

  private void leaveComments(CertificationEntity entity, Identity certifier) throws GeneralException
  {        
    for (CertificationItem item : getAllLeafs(entity)) {
      List<ScriptedCertificationAction> actions = findAction(item, certifier);

      if (actions != null) {
        for (ScriptedCertificationAction action : actions) {
          // 5.1 Port : Support standalone comments as well as comments on
          //            cert decision
          //
          if (action.action.equals("DecisionComment")) {
            log.info("Setting DecisionComment on item to " + action.comment);
            item.getAction().setComments(action.comment);
          } else if (action.action.equals("Comment")) {
            log.info("Setting Comment on item to " + action.comment);
            Identity identity = item.getIdentity(context);
            if (identity != null){
              identity.addEntitlementComment(context, item, certifier.getDisplayName(), action.comment);
              context.saveObject(identity);
            }   
          }
        }
      }
    }
  }

  /**
   * Sign off on the previous certification. Either appName or managerName
   * should be specified (but not both). If a previous certification is not
   * found, this throws an exception.
   *
   * @param name
   *            The name of the application or manager.
   * @param appOwner
   *            A boolean indicating this is an appOwner certification.
   */
  private void signOffOnPrevious(String name, boolean appOwner)
      throws GeneralException 
  {
    if (null == name)
      throw new GeneralException("Application or manager must be specified.");

    // Only need to work on this if it hasn't been signed yet.
    Certification prev = getPreviousCertification(name, appOwner);
    if (null != prev) {
      if (!prev.hasBeenSigned()) {
        Identity certifier = getCertifier(name);

        // Bulk approve any items that aren't yet complete.
        if (!prev.isComplete()) {
          if (null != prev.getEntities()) {
            for (CertificationEntity identity : prev.getEntities()) {


              if (!identity.isComplete()) {
                bulkApprove(identity, certifier);

                // Leave a comment.
                leaveComments(identity, certifier);

                bulkMitigate(identity, certifier);
              }
            }
          }
        }

        // Save and sign off on the certification, there may be unnecessary saves
        // here but this does appear to work correctly.  The transition to end
        // will be handled by the maintenance task.
        context.saveObject(prev);
        context.commitTransaction();

        Certificationer certificationer = new Certificationer(context);
        certificationer.refresh(prev);
        certificationer.sign(prev, certifier);

        context.saveObject(prev);
        context.commitTransaction();
      }
    }
  }

  /**
   * Get the previous certification for the given application or manager.
   * Either appName or managerName is assumed to be non-null. Throw an
   * exception if we cannot find a previous certification of the specified
   * type.
   *
   * @param name
   *            The name of the application or manager.
   * @param appOwner
   *            A boolean indicating this is an appOwner certification.
   *
   * @return The previous certification for the given application or manager.
   */
  private Certification getPreviousCertification(String name, boolean appOwner)
      throws GeneralException {

    Filter filter = null;
    if (appOwner) {
      Application app = context.getObjectByName(Application.class, name);
      if (null == app)
        throw new GeneralException(
            "Need to import demo data - could not find: " + name);
      filter = Filter.and(Filter.eq("applicationId", app.getId()), Filter
          .eq("type", Certification.Type.ApplicationOwner));
    } else {
      filter = Filter.and(Filter.eq("manager", name), Filter.eq("type",
          Certification.Type.Manager));
    }

    QueryOptions ops = new QueryOptions();
    ops.add(filter);
    ops.setOrderBy("created");
    ops.setOrderAscending(false);
    List<Certification> prevCerts = this.context.getObjects(
        Certification.class, ops);
    if ((null == prevCerts) || prevCerts.isEmpty()) {
      log.info("No previous certification to sign off on for certifier: " + name);
      return null;
    }

    return prevCerts.get(0);
  }

  /**
   * Add and remove entitlements and business roles from the given identity.
   *
   * @param identityName
   *            The identity to act upon.
   */
  @SuppressWarnings("unchecked")
  private void swizzleEntitlements(String identityName)
      throws GeneralException
  {
    String focusAppName = args.getString(ARG_CERT_FOCUS_APP_NAME);
    String certTweakEnt = args.getString(ARG_CERT_FOCUS_ENT_NAME);

    String certTweakEnts = args.getString(ARG_CERT_FOCUS_ENTS);
    String[] ents = certTweakEnts.split(";");

    Identity identity = context.getObjectByName(Identity.class, identityName);
    if (null == identity)
      throw new GeneralException("Could not find user " + identityName);

    // 1. Add some attributes
    Link appLink = getOrCreateLink(identity, focusAppName);
    for (String ent : ents) {
      addAttributeValue(appLink, certTweakEnt, ent);
    }

    // 2. Add some permissions
    // TODO: Add some permissions
  }

  /**
   * Add a value to a multi-valued attribute on the given user's requested
   * link.
   *
   * @param userName
   *            The name of the user to which to add the value.
   * @param appName
   *            The name of the app on which to add the value.
   * @param attrName
   *            The name of the attribute to which to add the value.
   * @param val
   *            The value to add.
   */
  private void addAttributeValue(String userName, String appName, String attrName, String val) 
      throws GeneralException 
  {
    Identity identity = context.getObjectByName(Identity.class, userName);
    if (null == identity)
      throw new GeneralException("Could not find user " + userName);
    Link link = getOrCreateLink(identity, appName);
    addAttributeValue(link, attrName, val);
  }

  /**
   * Get or create the Link for the given application name on the requested
   * Identity.
   *
   * @param identity
   *            The Identity from which to retrieve (or on which to add) the
   *            Link.
   * @param appName
   *            The name of the application of the link to get/create.
   *
   * @return The Link for the given application name on the requested
   *         Identity.
   */
  private Link getOrCreateLink(Identity identity, String appName)
      throws GeneralException 
  {
    Application app = context.getObjectByName(Application.class, appName);
    if (null == app)
      throw new GeneralException("Could not find application " + appName);

    Link link = identity.getLink(app);

    // Link should be there, but we'll create it if it is not.
    if (null == link) {
      link = new Link();
      link.setDisplayName(identity.getName());
      link.setNativeIdentity(identity.getName());
      link.setApplication(app);
      Attributes<String, Object> attrs = new Attributes<String, Object>();
      attrs.put("firstname", identity.getName());
      attrs.put("lastname", identity.getName());
      link.setAttributes(attrs);
      identity.add(link);
    }

    return link;
  }

  /**
   * Add the value to the multi-valued attribute on the given Link.
   *
   * @param link
   *            The Link on which to add the value.
   * @param attrName
   *            The name of the multi-valued attribute to which to add the
   *            value.
   * @param val
   *            The value to add (if not already present).
   */
  @SuppressWarnings("unchecked")
  private void addAttributeValue(Link link, String attrName, String val) 
  {
    List<String> vals = (List<String>) link.getAttribute(attrName);
    if (null == vals)
      vals = new ArrayList<String>();
    if (!vals.contains(val))
      vals.add(val);
    link.setAttribute(attrName, vals);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Act on the certifications
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Randomly approve, mitigate, delegate, remediate pieces of all active
   * certifications.
   */
  private void actOnCerts() 
      throws GeneralException 
  {
    List<Certification> certs = context.getObjects(Certification.class);
    if (null != certs) {
      for (Certification cert : certs) {
        if (doWeTweakThisThang(cert)) {
          actOnCertification(cert);
        }
      }
    }
  }

  /**
   * Return whether or not to approve, mitigate, etc... this certification. We
   * will ignore the certifications on which we are generating differences.
   */
  private boolean doWeTweakThisThang(Certification cert)
      throws GeneralException 
  {
    String managerList = args.getString(ARG_CERT_MANAGER_NAME);
    String appNames = args.getString(ARG_CERT_APP_NAME);

    // Only tweak the special app owner certification. The others have
    // hard-coded values.
    Application certApp = cert.getApplication(context);
    if (null != certApp && appNames!=null) {
      for (String appName: appNames.split("\\|")) {
        if (appName.equals(certApp.getName())) {
          return true;
        }
      }
      // Kludge! We need to patch the id's for the hard-coded
      // appOwner certs.
      Application app = context.getObjectByName(Application.class,
          cert.getApplicationId());
      if (null != app)
        cert.setApplicationId(app.getId());
      context.saveObject(cert);
      context.commitTransaction();
      return false;
    }

    if (managerList!=null) {
      Identity certifier = cert.getManager(context);
      String[] namedCertifiers=managerList.split("\\|");
      if ((null != certifier)) {
        for (String mgr: namedCertifiers) {
          if ( mgr.equals(certifier.getName()) ) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Randomly approve, mitigate, remediate, and delegate the identities and
   * items in the given certification.
   *
   * @param cert
   *            The Certification to act upon.
   */
  private void actOnCertification(Certification cert) throws GeneralException {

    // If the cert is named (Application or Manager) in the argument
    // ARG_CERTS_TO_COMPLETE then set percentToComplete to 100 and ToDelegate to 0
    // <TODO: Sign OFF>
    String certsToComplete = args.getString(ARG_CERTS_TO_COMPLETE);
    boolean shouldComplete=false;
    if(certsToComplete!=null) {
      for (String certName: certsToComplete.split("\\|")) {
        if(cert.getApplication(context)!=null && cert.getApplication(context).getName().equals(certName) ||
            cert.getCertifiers().contains(certName) ) {
          shouldComplete=true;
          break;
        }
      }
    }

    if (shouldComplete) {
      
      // Just pick the first certifier
      String sSigner=cert.getCertifiers().get(0);
      Identity iSigner=context.getObjectByName(Identity.class, sSigner);

      // Do a bulk approve of all items  - currently just the App owner cert (Mgr cert not implemented)
      for (CertificationEntity entity: cert.getEntities()) {
        bulkApprove(entity, iSigner );
      }
      
      context.saveObject(cert);
      context.commitTransaction();
      Certificationer certificationer = new Certificationer(context);
      certificationer.refresh(cert);
      
      // Do a signoff
      certificationer.sign(cert, iSigner);
      context.saveObject(cert);
      context.commitTransaction();
    } else {

      // Complete any random percentage or a max percentage.
      // Delegate a smaller percentage, any
      // amount of the remaining up to a max of 10% (any more than that and
      // the certification owner can be considered a slacker).
      double percentToComplete = Math.random();
      String argMaxPercToComplete = args.getString(ARGS_MAX_PERCENT_CERTS_TO_COMPLETE);
      if (argMaxPercToComplete != null){
    	  percentToComplete = percentToComplete * Double.parseDouble(argMaxPercToComplete)/100;    	  
      }

      // We want the special app to show up without many changes so that it
      // will be in the red. Give it a max of 20% complete.
      String tweakAppName = args.getString(ARG_CERT_TWEAK_APP_NAME);
      Application app = cert.getApplication(context);
      if ((null != app) && tweakAppName.equals(app.getName())) {
        percentToComplete = Math.random() * 0.20;
      }

      double percentToDelegate = Math.random()
          * Math.min(0.1, Math.random() * (1 - percentToComplete));

      if (null != cert.getEntities()) {
        for (CertificationEntity identity : cert.getEntities()) {
          double myNumber = Math.random();

          if (shouldIDoIt(myNumber, 0, percentToComplete)
              && !identity.isComplete()) {
            randomlyComplete(identity);
          } else if (shouldIDoIt(myNumber, percentToComplete,
              percentToComplete + percentToDelegate)) {
            delegate(identity);
          }
        }
      }
      context.saveObject(cert);
      Certificationer certificationer = new Certificationer(context);
      certificationer.refresh(cert);
    }
  }
  /**
   * Given a random number and upper/lower bounds, tell me whether I should do
   * it or not.
   */
  private static boolean shouldIDoIt(double myNumber, double bottomBand,
      double topBand) {
    return (bottomBand <= myNumber) && (myNumber <= topBand);
  }

  /**
   * Randomly approve, mitigate, and/or remediate the items in the given
   * identity.
   *
   * @param identity
   *            The CertificationIdentity to act upon.
   */
  private void randomlyComplete(CertificationEntity identity)
      throws GeneralException {

    String certRemediationUser = args.getString(ARG_CERT_REMEDIATION_USER);

    List<CertificationItem> items = getAllLeafs(identity);
    for (CertificationItem item : items) {

      // Select between approve, mitigate, and remediate. 68% approve,
      // 30% remediate, 2% mitigate.
    	
      double myNumber = Math.random();
      
      boolean isProvisioning = false;      
      
      // There's only one app in appList. 
      Set<Application> appList= item.getApplications(context);
      if (appList != null){
    	  for (Application application : appList) {
    		  if (application.getFeaturesString().contains("PROVISIONING")){
    			  isProvisioning = true; 
    		  }
    	  }
      }
      
      if (shouldIDoIt(myNumber, 0, 0.68)) {
        // Approve.
        item.approve(context, getCertifier(), null);
      } else if (shouldIDoIt(myNumber, 0.68, 0.98) && (isProvisioning == false)) {
        // Remediate, unless the app is a direct-connect (contains feature string "PROVISIONING").  
        item.remediate(context, getCertifier(), null,
            CertificationAction.RemediationAction.OpenWorkItem,
            certRemediationUser,
            "Please remove this entitlement from "
                + identity.getName(),
                "This employee does not need this access.", null,null);
      } else {
        // Mitigate.
        item.mitigate(context, getCertifier(), null, new Date(System
            .currentTimeMillis()
            + 1000 * 60 * 60 * 24 * 14),
            "Allow this for two more weeks.");
      }
    }
  }

  /**
   * Delegate either the given CertificationIdentity or one of the items in
   * the certification (it is randomly chosen which gets delegated).
   *
   * @param identity
   *            The identity to delegate.
   */
  private void delegate(CertificationEntity identity) {

    String certRemediationUser = args.getString(ARG_CERT_REMEDIATION_USER);

    CertificationDelegation delegation = new CertificationDelegation();
    String fullname = Util.getFullname(identity.getFirstname(), identity
        .getLastname());
    delegation.setOwnerName(certRemediationUser);
    delegation.setDescription("Please certify access on " + fullname);
    delegation
    .setComments("Would you mind taking care of this for me?  I don't know this person.");

    // Flip a coin ... heads delegate the identity, tails delegate one of
    // the items.
    double myNumber = Math.random();
    if (shouldIDoIt(myNumber, 0, 0.5)) {
      identity.setDelegation(delegation);
    } else {
      CertificationItem item = getFirstLeaf(identity);
      if (null != item)
        item.setDelegation(delegation);
    }
  }

  /**
   * Get the first leaf item in the given identity.
   */
  private CertificationItem getFirstLeaf(CertificationEntity identity) {

    CertificationItem item = null;
    List<CertificationItem> items = identity.getItems();
    while ((null == item) && (null != items)) {
      if (!items.isEmpty()) {
        CertificationItem current = items.get(0);
        if (null == current.getItems())
          item = current;
        else
          items = current.getItems();
      }
    }
    return item;
  }

  /**
   * Get all leaf items from the given identity.
   */
  private List<CertificationItem> getAllLeafs(CertificationEntity identity) {

    List<CertificationItem> items = new ArrayList<CertificationItem>();
    items.addAll(getAllLeafs(identity.getItems()));
    return items;
  }

  private List<CertificationItem> getAllLeafs(List<CertificationItem> items) {

    List<CertificationItem> leafs = new ArrayList<CertificationItem>();

    if (null != items) {
      for (CertificationItem item : items) {
        if ((null == item.getItems()) || item.getItems().isEmpty())
          leafs.add(item);
        else
          leafs.addAll(getAllLeafs(item.getItems()));
      }
    }

    return leafs;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reset back to initial state
  //
  ////////////////////////////////////////////////////////////////////////////

  /**
   * Reset all identities in the demo data base to their original state. TODO:
   * Consider resetting the environment from here. For now, nuking the dB and
   * executing DEMO_DATA_SETUP is the better option.
   */
  private void reset() throws GeneralException {
  }

  // ////////////////////////////////////////////////////////////////////
  //
  // Identity History Simulation
  //
  // ////////////////////////////////////////////////////////////////////

  /**
   * Preload persistent state to the extent possible and cache it.
   */
  private void prepareScoreKeeper() throws GeneralException {
    _scoreKeeper = new ScoreKeeper(context, args);
    _scoreKeeper.prepare();
  }

  // ////////////////////////////////////////////////////////////////////
  //
  // Group History Simulation
  //
  // ////////////////////////////////////////////////////////////////////

  private GroupIndex newGroupIndex(GroupDefinition group) {

    GroupIndex index = new GroupIndex();

    // Let's take the name of the definition just so we have
    // something to show. Could also toString the filter,
    // but this is nicer for the UI
    index.setName(group.getName());
    index.setDescription(group.getDescription());

    // always point back to the group
    index.setDefinition(group);

    return index;
  }

  private ScoreConfig getScoreConfig() throws GeneralException {

    if (_scoreConfig == null) {

      // this is the standard score config
      String configName = ScoreConfig.OBJ_NAME;
      if (args != null) {
        // but it may be overridden with a task arg for
        // special situations
        String s = args.getString(ARG_SCORE_CONFIG);
        if (s != null)
          configName = s;
      }

      _scoreConfig = context.getObject(ScoreConfig.class, configName);
      // Some of the Scorers need to create lookup caches
      // for performance. Some of them use static members
      // which is probably bad, others will store them in the
      // ScoreDefinition object for reuse on successive calls
      // to the Scorer. Because the ScoreConfig may be modified,
      // we need to clone it to make sure these mods don't
      // make it back into the database.
      XMLObjectFactory f = XMLObjectFactory.getInstance();
      _scoreConfig = (ScoreConfig) f.clone(_scoreConfig, context);
    }

    return _scoreConfig;
  }

  /**
   * Generate interesting group index histories for the demo.
   *
   */
  private void generateGroupHistory() throws GeneralException {

    // 1. Get the "Department" GroupFactory
    GroupFactory groupFactory = null;
    groupFactory = context.getObjectByName(GroupFactory.class, getArgument(
        ARG_GROUP_FACTORY_NAME, true));
    QueryOptions qo = new QueryOptions();

    // 2. Fetch all the GroupDefinitions for the given factory(ies)
    if (groupFactory != null) {
      qo.add(Filter.eq("factory", groupFactory));
      // qo.add(Filter.eq("indexed", new Boolean(true)));
      List<GroupDefinition> groupDefs = context.getObjects(
          GroupDefinition.class, qo);
      if (groupDefs != null) {

        for (GroupDefinition group : groupDefs) {
          // 3. Generate history for each group
          if (group.isIndexed())
            generateHistory(group);

          context.decache(group);
        }
      }
    }
  }

  /**
   * Generate fake index history for one group.
   */
  private void generateHistory(GroupDefinition group) throws GeneralException {

    // Assume granularity is monthly, should make this flexible
    // enough to look at the SystemConfiguration and generate other
    // granules.

    // 1. Create the Calendar for use in generating the history and
    // determine the number of granules
    Date now = new Date();
    Calendar c = Calendar.getInstance();
    c.setTime(now);

    // 2. Delete all the indexes except for the current one
    QueryOptions ops = new QueryOptions();
    if (null != group.getIndex()) {
      ops.add(Filter.lt("created", group.getIndex().getCreated()));
    }
    deleteGroupIndexes(group, ops);

    // We want 6 months worth of history (including the current values)
    int units = Integer.valueOf(getArgument(ARG_TRENDING_MONTHS, true));
    c.add(Calendar.MONTH, -(units - 1));
    // Setting the clock back a bit to fix a display ordering issue
    c.add(Calendar.HOUR, -1);

    GroupIndex index = null;

    // 3. Iterate over the number of units and generate a GroupIndex
    for (int i = 0; i < (units - 1); i++) {

      // Hibernate layer is supposed to preserve creation dates
      // if we set them before saving.
      index = randomizeGroupIndex(group, (i + 1));
      index.setCreated(c.getTime());
      c.add(Calendar.MONTH, 1);
      context.saveObject(index);
      context.commitTransaction();
    }
  }

  /**
   * Create a new GroupIndex and fill it with randomness.
   */
  private GroupIndex randomizeGroupIndex(GroupDefinition group, int trendUnit)
      throws GeneralException {

    // 1. Create a new GroupIndex linked to the Group
    GroupIndex index = newGroupIndex(group);

    // 2. Randomize the base scores and set them on the index.
    // We want to show a downward sloping trend line so we are leveraging
    // the trending unit passed in.
    int compositeMax = Integer
        .valueOf(getArgument(ARG_COMPOSITE_MAX, true));
    int adjComposite = compositeMax - trendUnit * genRandomInRange(75, 100);

    // Fetch the member count from the most recent index
    int memberCount = 0;
    if (null != group.getIndex()) {
      memberCount = group.getIndex().getMemberCount();
    } else {
      memberCount = Integer.valueOf(getArgument(
          ARG_DEFAULT_GROUP_MEMBER_COUNT, true));
    }
    int adjViolations = memberCount - trendUnit * genRandomInRange(5, 15);

    // For now we are reusing the composite score for the subscores. If
    // necessary, we could randomize.
    index.setCompositeScore(adjComposite);
    index.setBusinessRoleScore(adjComposite);
    index.setEntitlementScore(adjComposite);
    index.setPolicyScore(adjComposite);
    index.setCertificationScore(adjComposite);
    index.setTotalViolations(Math.abs(adjViolations));

    // 3. Set the member count.
    // For now, we will maintain the value. We may want to show some
    // variation here to represent typical turnover.
    index.setMemberCount(memberCount);

    // 4. Set the Score Bands (low, medium, high).
    // Assume that the number is the default of three.
    ScoreConfig config = getScoreConfig();
    int bands = config.getNumberOfBands();
    // int bands = 3;

    index.setBandCount(bands);
    int lowBand = (memberCount * genRandomInRange((15 + trendUnit), 20)) / 100;
    int medBand = (memberCount * genRandomInRange((60 + trendUnit), 90)) / 100;
    int highBand = Math.abs(memberCount - (lowBand + medBand));
    index.setBand(0, lowBand);
    index.setBand(1, medBand);
    index.setBand(2, highBand);

    return index;
  }

  /**
   * Delete indexes associated with a group, with possible filtering for
   * creation date.
   */
  private void deleteGroupIndexes(GroupDefinition group, QueryOptions ops)
      throws GeneralException {

    // Sigh, we may be deleting the index that is currently
    // referenced by the group. Have to null this out
    // to avoid a foreign key constraint violation.

    /*
     * if (group.getIndex() != null) { group.setIndex(null);
     * context.saveObject(group); context.commitTransaction(); }
     */

    if (ops == null)
      ops = new QueryOptions();
    ops.add(Filter.eq("definition", group));

    ObjectUtil.removeObjects(context, GroupIndex.class, ops);
  }

  /**
   * Generate a random number in the specified range.
   */
  private int genRandomInRange(int min, int max) {
    return (int) (Math.random() * (max - min + 1)) + min;
  }

  /**
   * Generate a random number in the specified range leveraging the
   * {@java.util.Random} class.
   */
  private int genRandomInRange(long seedValue, int min, int max) {
    Random randomizer = new Random(seedValue);
    int randomInt = randomizer.nextInt();
    randomInt = Math.abs(randomInt);
    randomInt = (randomInt % max + 1) + min;

    return randomInt;
  }

  /**
   * Grab an argument. Throw an exception if it is missing and you want puke.
   *
   */
  private String getArgument(String key, boolean puke)
      throws GeneralException {

    if (args.containsKey(key)) {
      return args.getString(key);
    } else if (puke) {
      throw new GeneralException("Missing Argument: " + key);
    }
    return null;
  }

  /**
   * Cancel all certifications.
   */
  private void cancelCertifications() throws GeneralException {
    Certificationer certificationer = new Certificationer(context);

    List<Certification> objs = context.getObjects(Certification.class);
    if (objs != null) {
      for (Certification a : objs) {
        certificationer.delete(a);
      }
    }

    context.commitTransaction();
  }

  private void fixImportedCerts() throws GeneralException {
    QueryOptions qo = new QueryOptions().add(Filter
        .notnull("applicationId"));
    List<Certification> certifications = this.context.getObjects(
        Certification.class, qo);
    if (certifications != null) {
      for (Certification cert : certifications) {
        String appId = cert.getApplicationId();
        Application app = this.context.getObjectByName(
            Application.class, appId);
        if (app != null)
          cert.setApplicationId(app.getId());

        context.saveObject(cert);
        context.commitTransaction();

      }
    }

  }

  /**
   * Execute the perform maintenance task.
   */
  private void performMaintenance() throws GeneralException
  {
    TaskManager taskManager = new TaskManager(context);

    taskManager.runSync("Perform Maintenance", null);
  }

  /**
   *  Create a dynamic name for the auto-generated certifications
   */
  private String generateCertName(String prefix, boolean simulatePast)
  {
    String quarter = "";

    Calendar rightNow = Calendar.getInstance();
    int month = rightNow.get(Calendar.MONTH);
    int yy = rightNow.get(Calendar.YEAR);

    if (month >= Calendar.JANUARY && month <= Calendar.MARCH) {
      quarter = "Q1";
    } else if (month >= Calendar.APRIL && month <= Calendar.JUNE) {
      quarter = "Q2";
    } else if (month >= Calendar.JULY && month <= Calendar.SEPTEMBER) {
      quarter = "Q3";
    } else {
      quarter = "Q4";
    }

    if (simulatePast) {
      yy--;
    }

    return (prefix + quarter + " FY" + yy);
  }

  private List getSubordinateManagers(String managerName)
  {
    List<String> subordinateManagers = new ArrayList<String>();

    QueryOptions qo = new QueryOptions();

    Filter [] filters = new Filter[1];
    filters[0] = Filter.eq("manager.name", managerName);
    qo.add(filters);

    List<String> props = new ArrayList<String>();
    props.add("name");

    try {
      java.util.Iterator<Object []> result = this.context.search(Identity.class, qo, props);
      while (result.hasNext()) {
        Object [] record = result.next();
        String subManager = (String) record[0];
        //log.info("Found subordinate manager : " + subManager);
        subordinateManagers.add(subManager);
      }
    } catch (GeneralException ge) {
      log.error(ge.getMessage());
    }

    return subordinateManagers;
  }

  private List<String> stringToList(String string) {
    // Split a string by bar separator
    List ret=new ArrayList<String>();
    if(string!=null) {
      String[] parts=string.split("\\|");
      for(String part: parts) {
        ret.add(part);
      }
    }
    return ret;
  }


}
