Report Executor and Exporter v1.0
29/08/2017

Contact: sebastien.lelarge@sailpoint.com
************************
* Library dependencies *
************************
Requires the sailpoint.seri.task.ReportExecutorAndExporter class that is found in seri.jar

*************
* Execution *
*************
Navigate to Tasks, make a new task with the "Report Executor and Exporter" template. 
On the task configuration screen:
- select your report (only instantiated reports can be selected, not the templates themselves)
- select which output format is needed (CSV and/or PDF)
- enter the location where the report must be copied after execution. The location should be an absolute path. 
For example (on a mac): /Users/linda.wang/apache-tomcat-7.0.47/webapps/identityiq

************
* Background *
**************
This custom task has been created as an answer to recent POC requirements where a report must be generated and then pushed to a shared location

**************************
* Potential Enhancements *
**************************
- support multiple file transfer protocol
- allow the execution of an "After script"
- allow to choose the output filename naming convention (currently static)

***************
* Limitations *
***************
- The executed report must exist as an instance of an existing template since no interaction is possible
- As of today, the task generates a report named yyyyMMddHHmm_<report name>.<pdf|csv>
- The target location must be mounted on the IIQ host and write access must be authorized

****************
* To Configure *
****************
import via setup.xml

******************
* To Demonstrate *
******************
See execution
Once configured:
- run the task you created
- you can open the Task Result: will display the full path to the generated files
- go to the specified location and open the reports

*******************
* Version History *
*******************
29 Aug 2017 - S. Lelarge
First Release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-Report-ExecutorAndExporter
