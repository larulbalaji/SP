/* (c) Copyright 2008 SailPoint Technologies, Inc., All Rights Reserved. */

/**
 * 
 * Author: Brent Hauf
 * 
 *
 */

package sailpoint.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.MessageAccumulator;
import sailpoint.api.ObjectUtil;
import sailpoint.api.RequestManager;
import sailpoint.api.RoleLifecycler;
import sailpoint.api.SailPointContext;
import sailpoint.api.TaskManager;
import sailpoint.api.Terminator;
import sailpoint.monitoring.ProcessLogGenerator;
import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.EmailOptions;
import sailpoint.object.EmailTemplate;
import sailpoint.object.Identity;
import sailpoint.object.Request;
import sailpoint.object.RequestDefinition;
import sailpoint.object.Resolver;
import sailpoint.object.SailPointObject;
import sailpoint.object.TaskDefinition;
import sailpoint.object.TaskSchedule;
import sailpoint.object.Workflow;
import sailpoint.object.WorkflowCase;
import sailpoint.object.WorkflowLaunch;
import sailpoint.request.WorkflowRequestExecutor;
import sailpoint.server.Auditor;
import sailpoint.tools.DateUtil;
import sailpoint.tools.EmailException;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;
import sailpoint.tools.Util;
import sailpoint.tools.xml.XMLClass;
import sailpoint.web.messages.MessageKeys;



import java.util.Iterator;
import sailpoint.tools.MapUtil;
import java.lang.reflect.Method;
import java.lang.Class;
import sailpoint.tools.xml.AbstractXmlObject;

/**
 * Replacement WorkflowHandler for most IIQ workflows.
 * See config/catalog/Utility-StepLevelTrace/readme.txt for details
 */
public class  SERIWorkflowHandler extends StandardWorkflowHandler {

  private static Log log = LogFactory.getLog(SERIWorkflowHandler.class);

  Log _traceLog4J = null;


  public static String ARG_TRACE           = "traceStep";
  public static String ARG_TRACEVAR_START  = "traceStepVariablesStart";
  public static String ARG_TRACEVAR_END    = "traceStepVariablesEnd";
  public static String ARG_TRACEVAR_NAMES  = "traceStepVariableNames";
  public static String ARG_TRACE_LOG4J     = "traceStepLog4j";


  private Boolean _workflowLevelSet = false;
  private String  _trace       = null;
  private String  _traceStart  = null;
  private String  _traceEnd    = null;
  private String  _traceNames  = null;

  private String getTraceVariableNames(WorkflowContext wfc) {

    Workflow.Step currStep = wfc.getStep();
    List<Workflow.Arg> args = currStep.getArgs();

    if (args == null)
      return null;

    for (Workflow.Arg arg : args) {
      if (arg.getName().equalsIgnoreCase(ARG_TRACEVAR_NAMES) ) {
        return (String) arg.getValue();
      }
    }

    if (_traceNames != null) {
      return _traceNames;
    }

    return null;
  }

  private Boolean isTraceStep(WorkflowContext wfc) {

    Map<java.lang.String,java.lang.Object> vars = wfc.getWorkflow().getVariables();

    if (vars != null) {
      for (Map.Entry<String, Object> var : vars.entrySet()) {

        Object value = var.getValue();
        String varName = var.getKey();

        if (varName.equalsIgnoreCase(ARG_TRACE)) {
          //found a workflow level variable so return it
              return Util.otob(var.getValue());
        }
      }
    }

    //we did not find a workflow level variable.  Look at the step args
    Workflow.Step currStep = wfc.getStep();    
    List<Workflow.Arg> args = currStep.getArgs();

    if (args == null)
      return false;

    for (Workflow.Arg arg : args) {
      if (arg.getName().equalsIgnoreCase(ARG_TRACE) ) {
        return Util.otob(arg.getValue());
      }
    }

    if (_trace != null) {
      return Util.otob(_trace);
    }

    return false;
  }

  private Boolean isTraceVarStart(WorkflowContext wfc) {

    Workflow.Step currStep = wfc.getStep();
    List<Workflow.Arg> args = currStep.getArgs();

    if (args == null)
      return false;


    for (Workflow.Arg arg : args) {
      if (arg.getName().equalsIgnoreCase(ARG_TRACEVAR_START) ) {
        return Util.otob(arg.getValue());
      }
    }

    if (_traceStart != null) {
      return Util.otob(_traceStart);
    }

    return false;
  }

  private Boolean isTraceVarEnd(WorkflowContext wfc) {

    Workflow.Step currStep = wfc.getStep();
    List<Workflow.Arg> args = currStep.getArgs();

    if (args == null)
      return false;

    for (Workflow.Arg arg : args) {
      if (arg.getName().equalsIgnoreCase(ARG_TRACEVAR_END) ) {
        return Util.otob(arg.getValue());
      }
    }

    if (_traceEnd != null) {
      return Util.otob(_traceEnd);
    }

    return false;
  }

  private Boolean isTraceVariableName(String varName) {

    if (varName.equalsIgnoreCase(ARG_TRACE))
      return true;

    if (varName.equalsIgnoreCase(ARG_TRACEVAR_START))
      return true;

    if (varName.equalsIgnoreCase(ARG_TRACEVAR_END))
      return true;

    if (varName.equalsIgnoreCase(ARG_TRACEVAR_NAMES))
      return true;

    return false;
  }

  private void logMessage(String msg) {
    if (_traceLog4J != null)
      _traceLog4J.debug(msg);
    else
      System.out.println(msg);    
  }

  private void processStep(WorkflowContext wfc, boolean start) {

    log.debug("Starting processStep");
    log.debug("start:" + start);

    Workflow.Step currStep = wfc.getStep();
    //List<Workflow.Arg> args = currStep.getArgs();

    String traceVariableNames = getTraceVariableNames(wfc);

    Boolean traceVars = false;
    if (start)
      traceVars = isTraceVarStart(wfc);
    else
      traceVars = isTraceVarEnd(wfc);

    if (isTraceStep(wfc)) {

      if (start) {
        logMessage("Starting step: " + currStep.getName());
      }
      else {
        logMessage("Ending step: "   + currStep.getName());
      }
    }

    if (traceVars) {

      if (start)
        logMessage("Variables at start of step " + currStep.getName() + ":");
      else
        logMessage("Variables at end of step " + currStep.getName() + ":");

      Map<java.lang.String,java.lang.Object> vars = wfc.getWorkflow().getVariables();

      if (vars != null) {

        for (Map.Entry<String, Object> var : vars.entrySet()) {

          Object value = var.getValue();
          String varName = var.getKey();

          //don't output the values that are being used by this process
          if (isTraceVariableName(varName))
            continue;

          //if they have entered specific names skip the rest
          Boolean skipVariable = false;
          if (traceVariableNames != null && traceVariableNames.indexOf(varName) == -1) {
            skipVariable = true;
            log.debug("Skipping Variable:" + varName);
          }

          if ( value != null && !skipVariable ) {
            boolean hasMethod=false;
            if (value instanceof AbstractXmlObject ) {
              AbstractXmlObject xml = (AbstractXmlObject) value;
              logMessage(var.getKey() + " = ");
              try {
                logMessage(xml.toXml());
                hasMethod = true;
              } catch (Exception e) {
                log.debug("Caught exception:" + e);
              }

            }

            if (!hasMethod) {
              log.debug("Variable does not have toXml method");
              logMessage(var.getKey() + " = " + var.getValue());                            
            }    

          }
        }
      }

    }

  }



  @Override
  public void startWorkflow(WorkflowContext wfc) throws GeneralException {

    super.startWorkflow(wfc);

  }

  @Override
  public void endWorkflow(WorkflowContext wfc) throws GeneralException {

    if (Util.otob(wfc.getWorkflow().get(ARG_TRACE))) {
      logMessage("Ending Workflow: " +  wfc.getWorkflow().getName());
    }

    super.endWorkflow(wfc);
  }

  @Override
  public void startStep(WorkflowContext wfc) throws GeneralException {
    log.debug("startStep");

    if ( Util.otob(wfc.getWorkflow().get(ARG_TRACE_LOG4J)) ) {
      String logName = "SERI.Workflow." + wfc.getWorkflow().getName() + "." + wfc.getStep().getName();
      log.debug("log4j logging as:" + logName);
      _traceLog4J = LogFactory.getLog(logName);
    } else {
      log.debug("message sent to stdout");
    }

    if (!_workflowLevelSet) {
      _trace      = (String) wfc.getWorkflow().get(ARG_TRACE);
      _traceStart = (String) wfc.getWorkflow().get(ARG_TRACEVAR_START);
      _traceEnd   = (String) wfc.getWorkflow().get(ARG_TRACEVAR_END);
      _traceNames = (String) wfc.getWorkflow().get(ARG_TRACEVAR_NAMES);
      log.debug("log4j:" + wfc.getWorkflow().get(ARG_TRACE_LOG4J));

      if (Util.otob(wfc.getWorkflow().get(ARG_TRACE))) {
        logMessage("Starting Workflow: " +  wfc.getWorkflow().getName());
      }

      _workflowLevelSet = true;

      log.debug("Workflow Level Settings");
      log.debug(ARG_TRACE + " = " + _trace);
      log.debug(ARG_TRACEVAR_START + " = " + _traceStart);
      log.debug(ARG_TRACEVAR_END + " = " + _traceEnd);
      log.debug(ARG_TRACEVAR_NAMES + " = " + _traceNames);

    }


    processStep(wfc, true);

    super.startStep(wfc);
  }

  @Override
  public void endStep(WorkflowContext wfc)
      throws GeneralException
  {

    log.debug("endStep");

    processStep(wfc, false);

    super.endStep(wfc);
  }

  @Override
  public void startApproval(WorkflowContext wfc) throws GeneralException {
    //logMessage("startApproval SERI");

    super.startApproval(wfc);
  }

  @Override
  public void endApproval(WorkflowContext wfc) throws GeneralException {
    //logMessage("endApproval SERI");

    super.endApproval(wfc);
  }

}
