package sailpoint.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.RequestManager;
import sailpoint.api.SailPointContext;
import sailpoint.object.Configuration;
import sailpoint.object.Filter;
import sailpoint.object.Identity;
import sailpoint.object.QueryOptions;
import sailpoint.object.Request;
import sailpoint.object.Workflow;
import sailpoint.tools.GeneralException;

public class EndForwardingService extends Service {

  private static final Log log = LogFactory.getLog(EndForwardingService.class);

  private static final String CONFIG_OBJECT_NAME = "EndForwardingService";
  private static final String LAST_RUN_DATE = "lastRunDate";
  private static final String CONFIG_END_EVENT = "endForwardingWorkflow";
  private static final Object DEFAULT_END_WORKFLOW = "End Forwarding Period";

  private SailPointContext _ctx;

  @Override
  public void execute(SailPointContext context) throws GeneralException {

    // find users that have a forwarding end between the last time we ran,
    // and now

    this._ctx=context;

    Date lastRun=getLastEnd();
    log.trace("Last Run: "+lastRun);

    QueryOptions qo=new QueryOptions();
    Filter start=Filter.gt("modified", lastRun);
    qo.add(start);

    Iterator iter=context.search(Identity.class, qo);

    log.trace("Searching since "+lastRun+" : hasNext="+iter.hasNext());

    boolean changed=false;
    while (iter.hasNext()) {

      // Does this user have a forwarding user?
      Identity iden=(Identity)iter.next();
      if (!("spadmin".equals(iden.getName()))) {
        Date forwardEndDate=(Date)iden.getPreference("forwardEndDate");

        log.debug("Checking: "+iden.getName());
        // First case - forwarding is enabled (we are between start date and end date) and an end date is set
        // Result: Make sure the future event is set to the right date (in case end date has changed)
        if (hasForwardingEnabled(iden) && forwardEndDate!=null) { // no point looking if there's no end date for the forwarding
          log.debug("searching for requests");
          // Forwarding is in place; check that we have an event to turn it off later
          Request req=findScheduledEvent(iden, getEndWorkflowName());
          if (req!=null) { 
            // check that the event is correct. So the nextLaunch date should match the Identity's forwardEndDate value
            Date nextLaunch=req.getNextLaunch();
            if (!(nextLaunch.equals(forwardEndDate))) {
              // Update the event
              req.setNextLaunch(forwardEndDate);
              _ctx.saveObject(req);
              changed=true;
            }
          } else {
            scheduleEvent(iden, getEndWorkflowName(), forwardEndDate);
          }
        }
        if (!changed) {
          // Second case - There is no forwarding user (check for an empty 'forward' preference (the user to forward to))
          //              and there is an existing future event
          // Result: This would mean that forwarding has been turned off. We should probably set the workitems back
          // to the existing user now - so we'll just reset the date and let the workflow do its business
          Map<String, Object> preferences = iden.getPreferences();
          if(preferences!=null) {
            if (preferences.get("forward")==null) {
              Request req=findScheduledEvent(iden, getEndWorkflowName());
              if (req!=null) {
                req.setNextLaunch(new Date());
                context.saveObject(req);
                changed=true;
              }
            }
          }
        }
        if (!changed && forwardEndDate==null) {
          // Third case: There is no end date.
          // Result: Remove any existing future event, since there is now no date for it to run on
          log.debug("Forwarding has no end - checking for requests to remove");
          Request req=findScheduledEvent(iden, getEndWorkflowName());
          if (req!=null) {
            log.debug("found an event. Removing it");
            context.removeObject(req);
            changed=true;
          } else {
            // forwarding with no bounds has ended
            scheduleEvent(iden, getEndWorkflowName(), new Date());
          }
        }
      }

    }

    if (changed) _ctx.commitTransaction();
    log.debug("Execute: finished");
  }

  private String getEndWorkflowName() throws GeneralException {
    return getConfigStringOption(CONFIG_END_EVENT);
  }

  private void scheduleEvent(Identity iden, String workflowName,
      Date forwardEndDate) throws GeneralException {

    String identityName = iden.getName();

    Workflow wf=_ctx.getObjectByName(Workflow.class, workflowName);
    if(wf==null) {
      log.error("Cannot schedule: No such workflow '"+workflowName+"'");
      return;
    }
    String caseName=( "End Forwarding: "+workflowName+" for "+identityName );

    Map args=new HashMap();
    // workaround for Bug #27980
    args.put("catchExceptions", true);

    log.debug("putting identityName="+identityName);
    args.put("identityName", identityName);
    //    if(wfArgs!=null) {
    //      args.putAll(wfArgs);
    //    }

    RequestManager.scheduleWorkflow(_ctx, wf, caseName,
        args, forwardEndDate, iden);

  }

  private Request findScheduledEvent(Identity iden, String workflowName) throws GeneralException {

    // TODO: Right now we're assuming there is only maximum one event scheduled
    // This may not be the case. We should either throw an exception, notify someone
    // or just clear up. Think about this..

    List<Request> requests=_ctx.getObjects(Request.class);

    String identityName=iden.getName();

    for(Request req: requests) {

      String reqWorkflowName=(String)req.getAttribute("workflow");
      String reqIdentityName=(String)req.getAttribute("identityName");
      if (reqWorkflowName!=null && reqIdentityName!=null) {
        log.debug("Got Request: "+reqWorkflowName+" for "+reqIdentityName);
      }

      if( workflowName.equals(reqWorkflowName) && identityName.equals(reqIdentityName) ) {
        //        String caseName=(String)req.getAttribute("caseName");
        //        serilog.debug("wfCase="+wfCase+" : caseName="+caseName);
        //        if( wfCase==null || (caseName!=null && caseName.equals(wfCase)) ) {
        //          serilog.debug("Found a match");
        //            req.setNextLaunch(theDate);
        //            context.saveObject(req);
        //          numResults++;
        log.debug("Found an event");
        return req;
      }
    }
    return null;
  }

  private boolean hasForwardingEnabled(Identity user) {

    // The user is deemed to have forwarding enabled if a forwarding user is set
    // and 'now' is in between the forwardStartDate and the forwardEndDate
    Map<String, Object> preferences = user.getPreferences();
    if(preferences==null) return false;

    if (preferences.get("forward")==null) return false;

    Date now=new Date();

    Date start=(Date)preferences.get("forwardStartDate");
    if (start==null || now.before(start)) return false;
    Date end=(Date)preferences.get("forwardEndDate");
    if (end==null) return true; // forwarding is on; but there's no end date
    if (end.after(now)) return true; // we're in the forwarding period
    return false;
  }


  private Date getLastRun() throws GeneralException {

    // NOTE
    //return getLastExecute();
    // I'm not using the above, because the Last Execute time is set when the Servicer executes
    // the service, so it'll always be pretty much 'now'
    // I don't want to use getLastEnd() either, as there's a very slim possibility that
    // something will have changed during the last run (i.e. after the search but before getLastEnd()


    // go look at our configuration object, to see the last time we were run
    Date lastRun=getConfigDateOption(LAST_RUN_DATE);
    setConfigOption(LAST_RUN_DATE, new Date()); // now

    return lastRun;
  }

  private Configuration createBaseConfig() throws GeneralException {
    Configuration config=new Configuration();
    config.setName(CONFIG_OBJECT_NAME);

    config.put(CONFIG_END_EVENT, DEFAULT_END_WORKFLOW);
    _ctx.saveObject(config);
    _ctx.commitTransaction();

    return config;
  }

  private Date getConfigDateOption(String optionName) throws GeneralException {

    Configuration config=_ctx.getObjectByName(Configuration.class, CONFIG_OBJECT_NAME);
    if (config==null) config=createBaseConfig();

    Date theDate=config.getDate(optionName);
    if (theDate==null) {
      theDate=new Date(0L); // never been run
    }

    return theDate;
  }

  private String getConfigStringOption(String optionName) throws GeneralException {
    Configuration config=_ctx.getObjectByName(Configuration.class, CONFIG_OBJECT_NAME);
    if (config==null) config=createBaseConfig();

    String theString=config.getString(optionName);
    if (theString==null) {
      theString=""; // never been run
    }

    return theString;
  }

  private void setConfigOption(String optionName, Object value) throws GeneralException {
    Configuration config=_ctx.getObjectByName(Configuration.class, CONFIG_OBJECT_NAME);
    if (config==null) config=createBaseConfig();

    config.put(optionName, value);
    _ctx.saveObject(config);
    _ctx.commitTransaction();
  }

}
