/* (c) Copyright 2014 SailPoint Technologies, Inc., All Rights Reserved. */

/**
 * A task that runs an existing report (not a template) and exports the results in the specified location.
 *
 * author: Sebastien Lelarge
 */

package sailpoint.seri.task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.api.TaskManager;
import sailpoint.object.Attributes;
import sailpoint.object.Filter;
import sailpoint.object.JasperResult;
import sailpoint.object.PersistedFile;
import sailpoint.object.QueryOptions;
import sailpoint.object.TaskDefinition;
import sailpoint.object.TaskItemDefinition;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.persistence.PersistedFileInputStream;
import sailpoint.task.AbstractTaskExecutor;
import sailpoint.task.TaskMonitor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class ReportExecutorAndExporter extends AbstractTaskExecutor {

    public static final String TASK_TEMPLATE_NAME = "Role Changes Impact Evaluator";
    
    //
    // Input Arguments
    //
    public static final String ARG_REPORT_ID = "report";
    public static final String ARG_REPORT_LOCATION = "location";
    public static final String ARG_REPORT_FORMAT = "nameFormat";
    public static final String ARG_REPORT_EXPORT_CSV = "csvExport";
    public static final String ARG_REPORT_EXPORT_PDF = "pdfExport";
    
    // Internal variables
    private String _reportId;
    private String _reportName;
    private String _reportLocation = "";
    private String _reportNameFormat = "";
    private boolean _exportCSV = false;
    private boolean _exportPDF = false;
    
    //
    // Return Arguments
    //
    public static final String RET_EXECUTION_SUMMARY = "result";
    private String _returnedStatus = "";

    private SailPointContext _ctx;
    
    private static Log log = LogFactory.getLog(ReportExecutorAndExporter.class);

    public ReportExecutorAndExporter() {
    }

    public void execute(SailPointContext context, TaskSchedule sched,
                        TaskResult result, Attributes<String,Object> args)
        throws Exception {

        _ctx = context;
        
        // check if a task is already running
        boolean isTaskRunning = isTaskAlreadyRunning(context, result);

        if (isTaskRunning) {
            log.info("Another instance of the Task is already running. Terminating Task: " + result.getName());
            throw new GeneralException("Another instance of the Task is already running. Terminating this Task...");
        }

        // Task monitor to update task progress.
        TaskMonitor monitor = new TaskMonitor(context, result);

        // START HERE
        // check arguments
        // report name is mandatory
        _reportId = args.getString(ARG_REPORT_ID);
        log.debug("report ID: " + _reportId);
        if (null == _reportId || "".equals(_reportId)) {
            _returnedStatus = "The report name is mandatory";
            setResultStatus(result, _returnedStatus);
            throw new GeneralException(_returnedStatus);
        }
        
        // Check existence of the Report
        TaskDefinition taskDefReport = _ctx.getObjectById(TaskDefinition.class, _reportId);
        if (taskDefReport == null) {
            _returnedStatus = "The specified report does't exist";
            setResultStatus(result, _returnedStatus);
            throw new GeneralException(_returnedStatus);
        }
        _reportName = taskDefReport.getName();
        log.debug("report Name: " + _reportName);
                
        // Export location is mandatory, location must exist
        _reportLocation = args.getString(ARG_REPORT_LOCATION);
        log.debug("report location: " + _reportLocation);
        if (null == _reportLocation || "".equals(_reportLocation)) {
            _returnedStatus = "The export location is mandatory";
            setResultStatus(result, _returnedStatus);
            throw new GeneralException(_returnedStatus);
        }
        
        File fExportDirectory = new File(_reportLocation);
        if (!(fExportDirectory.exists() && fExportDirectory.isDirectory() && fExportDirectory.canWrite())) {
            _returnedStatus = "The export location does not exist or is not writable";
            setResultStatus(result, _returnedStatus);
            throw new GeneralException(_returnedStatus);
        }
        
        _exportCSV = args.getBoolean(ARG_REPORT_EXPORT_CSV);
        _exportPDF = args.getBoolean(ARG_REPORT_EXPORT_PDF);
        log.debug("exportCSV = " + _exportCSV);
        log.debug("exportPDF = " + _exportPDF);
        
        // Now run the report
        TaskManager tm = new TaskManager(_ctx);
        monitor.updateProgress("Executing report: " + taskDefReport.getName());
        tm.setLauncher(_ctx.getUserName());
        TaskResult reportResult = tm.runSync(taskDefReport, null);
        
        if (reportResult == null || reportResult.hasErrors()) {
            _returnedStatus = "Error were found while running the report";
            if (reportResult != null) {
                _returnedStatus += ": " + reportResult.getErrors();
            }
            setResultStatus(result, _returnedStatus);
            throw new GeneralException(_returnedStatus);
        }
        
        // Export results in desired format
        if (_exportCSV) {
            monitor.updateProgress("Exporting CSV");
            _returnedStatus += "CSV Export: " + generateAndExportReport(reportResult, _reportName, PersistedFile.CONTENT_TYPE_CSV) + "\n";
        }
        
        if (_exportPDF) {
            monitor.updateProgress("Exporting PDF");
            _returnedStatus += "PDF Export: " + generateAndExportReport(reportResult, _reportName, PersistedFile.CONTENT_TYPE_PDF) + "\n";
        }
        
        setResultStatus(result, _returnedStatus);
        
    }

    private String generateAndExportReport(TaskResult tr, String reportName, String fileType) {
        
        JasperResult jResult = tr.getReport();
        
        PersistedFile file = jResult.getFileByType(fileType);

        if (file == null) {
            return "Unable to get the jasper result";
        }

        String fileName = file.getName();
        log.debug("Jasper FileName " + fileName);

        PersistedFileInputStream is = new PersistedFileInputStream(_ctx, file);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        int nRead;
        byte[] data = new byte[16384];

        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            is.close();
        } catch (IOException e) {
          log.error("Error converting report file of type '"+fileType+"' to byte array", e);
          return "Error converting report file of type '"+fileType+"' to byte array";
        }

        // Generate file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String dateJour = sdf.format(new Date());
        String fileExt = fileType.substring(fileType.indexOf("/") + 1).toLowerCase();
        String outputFileName = _reportLocation + "/" + dateJour + "_" + reportName + "." + fileExt;
        log.debug("output file name: " + outputFileName);
        
        // Write to the output file
        byte[] fileBytes = buffer.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(outputFileName);
            fos.write(fileBytes);
            fos.close();
        } catch (FileNotFoundException fnfe) {
            log.error("Unable to find file " + outputFileName);
            return "Unable to find file " + outputFileName;
        }
        catch (IOException ioe) {
            log.error("Unable to write file " + outputFileName);
            return "Unable to write file " + outputFileName;
        }
        
        return "OK - " + outputFileName;
    }
    
    
    /**
     * Return true if any other role propagation task is already running.
     * @param context
     * @param result
     * @return
     * @throws GeneralException
     */
    private boolean isTaskAlreadyRunning(SailPointContext context, TaskResult result)
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
    
    
    private void setResultStatus(TaskResult result, String status) {
        // store results in the TaskResult
        result.setAttribute(RET_EXECUTION_SUMMARY, status);
    }

    @Override
    public boolean terminate() {
       
        return false;
    }
    
    
}
