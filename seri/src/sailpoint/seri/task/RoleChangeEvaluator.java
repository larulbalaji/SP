/* (c) Copyright 2014 SailPoint Technologies, Inc., All Rights Reserved. */

/**
 * A task that iterates over the all events in 'spt_role_change_event' table
 * and applies the provisioning plan in the event to all identities having assigned
 * role as role in the event. After applying the plan successfully, task will delete the event from queue.
 * In case of exception, task will stop and it will not delete event from the queue.
 *
 * author: ikram momin
 */

package sailpoint.seri.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.IdIterator;
import sailpoint.api.Identitizer;
import sailpoint.api.Provisioner;
import sailpoint.api.RolePropagator;
import sailpoint.api.SailPointContext;
import sailpoint.api.Terminator;
import sailpoint.object.Attributes;
import sailpoint.object.Bundle;
import sailpoint.object.Filter;
import sailpoint.object.QueryOptions;
import sailpoint.object.RoleChangeEvent;
import sailpoint.object.TaskDefinition;
import sailpoint.object.TaskItemDefinition;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.task.TaskMonitor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class RoleChangeEvaluator extends AbstractTaskExecutor {

    public static final String TASK_TEMPLATE_NAME = "Role Changes Impact Evaluator";
    
    public static final String RET_TOTAL = "total";
    public static final String RET_PROCESSED = "eventsProcessed";
    public static final String RET_IMPACTS = "impacts";

    private boolean terminate;
    /** RolePropagator API to perform actual propagation of events. */
    private RolePropagator propagator;

    private static Log log = LogFactory.getLog(RoleChangeEvaluator.class);

    public RoleChangeEvaluator() {
    }

    public void execute(SailPointContext context, TaskSchedule sched,
                        TaskResult result, Attributes<String,Object> args)
        throws Exception {

        // check if a task is already running
        boolean isTaskRunning = isRoleChangeEvaluationAlreadyRunning(context, result);

        if (isTaskRunning) {
            log.info("Another Role Changes Evaluation Task already running. Terminating Task: " + result.getName());
            throw new GeneralException("Another Role Changes Evaluation Task already running. Terminating this Task...");
        }
        // Check for any events.
        if (0 == context.countObjects(RoleChangeEvent.class, null)) {
            result.setAttribute(RET_TOTAL, 0);
            result.setAttribute(RET_PROCESSED, 0);
            return;
        }

        int totalEventProcessed = 0;

        // Task monitor to update task progress.
        TaskMonitor monitor = new TaskMonitor(context, result);
        int eventsToProcess = context.countObjects(RoleChangeEvent.class, null);
        // Total number of events to process. This count is shown in final result.
        int totalEvents = eventsToProcess;

        // Initialize the RolePropagator
        // requires provisioner, identitizer, terminator
        Attributes<String, Object> attrs = new Attributes<String, Object>();
        Provisioner provisioner = new Provisioner(context, attrs);
        Identitizer idzer = new Identitizer(context, args);
        Terminator terminator = new Terminator(context);
        propagator = new RolePropagator(context, provisioner, idzer, terminator);
                
        // Get the events from the queue table
        QueryOptions ops = new QueryOptions();
        ops.setOrderBy("created");
        ops.setOrderAscending(true);
        
        List<RoleChangeEvent> eventList = context.getObjects(RoleChangeEvent.class, ops);

        if (!Util.isEmpty(eventList)) {
            // Initialize the structure to store impacts
            // This will be stored in the TaskResult at the end of the process
            Map<Date, Map<String, String>> impacts = new TreeMap<Date, Map<String, String>>();
            for (RoleChangeEvent event : eventList) {

                if (null != event) {
                    Map<String, String> eventProperties = new HashMap<String, String>();
                    
                    Bundle roleObj = context.getObjectById(Bundle.class, event.getBundleId());
                    
                    if (roleObj != null) {
                        // Update task monitor
                        monitor.updateProgress("Process event for Bundle - " + roleObj.getName());
                        
                        eventProperties.put("rolename", roleObj.getDisplayableName());
                        
                        // evaluate the number of impacted identities
                        IdIterator idit = propagator.getIdIterator(event);
                        // If no identity is connected to the role then assume success.
                        if (Util.isEmpty(idit)) {
                            log.debug("Identity id iterator is empty for bundle - " +
                                    event.getBundleName());
                            eventsToProcess--;
                            eventProperties.put("impactedUsers", "0");
                        } else {
                            // Converting IdIterator to list of ids
                            List<String> ids = idIteratorToList(idit);
                            eventProperties.put("impactedUsers", Integer.toString(ids.size()));
                            
                        }
    
                        if (terminate) {
                            result.setTerminated(true);
                        }
                        
                        totalEventProcessed++;
                        
                    } else {
                        // Role was deleted since the event was created in the queue, nothing to do
                        eventsToProcess--;
                        eventProperties.put("impactedUsers", "Role does not exist");
                    }
                    
                    impacts.put(event.getCreated(), eventProperties);
                }
            }
            
            // store results in the TaskResult
            StringBuilder impactDetails = new StringBuilder();
            for (Date eventDate : impacts.keySet()) {
                Map<String, String> eventProp = impacts.get(eventDate); 
                impactDetails.append("Role name: " + eventProp.get("rolename") + ",\t\t");
                impactDetails.append("Impacted identities: " + eventProp.get("impactedUsers"));
                impactDetails.append("\n");
            }
            result.setAttribute(RET_IMPACTS, impactDetails.toString());
            
        } else {
            // No event to process
            log.info("No Role Change Events to process");
        }

        result.setAttribute(RET_TOTAL, propagator.getTotalIdentityUpdates());
        result.setAttribute(RET_PROCESSED, totalEventProcessed + " / " + totalEvents);
        
    }

    /**
     * Return true if any other role propagation task is already running.
     * @param context
     * @param result
     * @return
     * @throws GeneralException
     */
    private boolean isRoleChangeEvaluationAlreadyRunning(SailPointContext context, TaskResult result)
        throws GeneralException {

        boolean isTaskRunning = false;
        List<TaskDefinition> listDefs = new ArrayList<TaskDefinition>();
        TaskItemDefinition parentDef = result.getDefinition().getParent();
        QueryOptions qo = new QueryOptions(Filter.eq("parent", parentDef));

        // Get all the tasks that share same task template name
        Iterator<TaskDefinition> itDefs = context.search(TaskDefinition.class, qo);
        while (itDefs.hasNext()) {
            TaskDefinition def = itDefs.next();
            // Skip current task ID from the query
            if (!def.getId().equals(result.getDefinition().getId())) {
                listDefs.add(def);
            }
        }
        if (Util.isEmpty(listDefs)) {
            return isTaskRunning;
        }
        qo = new QueryOptions();
        // Get the tasks which are still running
        qo.add(Filter.and(Filter.in("definition", listDefs), Filter.isnull("completed")));
        Iterator<Object[]> taskResults = context.search(TaskResult.class, qo, "id");

        if (taskResults.hasNext()) {
            isTaskRunning = true;
        }
        return isTaskRunning;
    }
    

    public boolean terminate() {
        terminate = true;
        if (null != propagator) {
            propagator.setTerminate(true);
        }
        return true;
    }

    /**
     * Converting IdIterator to list of ids.
     */
    private List<String> idIteratorToList(IdIterator idit) {
        List<String> ids = new ArrayList<String>();
        if (!Util.isEmpty(idit)) {
            while (idit.hasNext()) {
                String id = idit.next();
                ids.add(id);
            }
        }
        return ids;
    }
}
