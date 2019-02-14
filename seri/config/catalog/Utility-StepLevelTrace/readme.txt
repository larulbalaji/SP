StepLevelTrace v 1.0.0

6/25/14

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************
The standard tracing for workflows allows you trace what steps are executed and view the variable values at the start of a workflow. All tracing is sent to stdout.  

This utility extends the standard tracing.  It provides finer grained control of what steps and variables are traced.
Variables can also be traced at the step level for changes to the variable.  It enables tracing of variables as each step executes and the values at the start and end of each step.
The variables that are traced can also be specified.

Output can be sent to log4j.  This allows for control of the what is traced, output to stdout or file, and what specific output gets routed to which file.
This can allow multiple individuals to debug workflows on the same server using different files to view there trace output.

The implementation is an extension to the sailpoint.workflow.StandardWorkflowHandler class.  It overrides the startStep and endStep methods of that class.
This based on step arguments or workflow variables, tracing behavior is performed.

************************
* Library dependencies *
************************
Requires the sailpoint.workflow.SERIWorkflowHandler class installed into your IIQ instance. 

****************
* To Configure *
****************
In order to utilize this capability you must do the following:

1) Change the handler that your workflow is using.  To accomplish that the handler attribute for for the workflow must be set to sailpoint.workflow.SERIWorkflowHandler.

<Workflow explicitTransitions="true" handler="sailpoint.workflow.SERIWorkflowHandler" libraries="Identity" name="TraceTest">

2) Workflow level enhanced tracing can be utilized by adding the following variables to the workflow.

<!-- Use these variables to provide workflow wide settings -->
<!-- Each variable can be overridden by setting the same arg name at the step level -->

<!-- set the traceStep variable value="string:true" to turn on step tracing workflow wide.-->
<Variable name="traceStep" initializer="string:true"/>

<!-- set the traceStepVariablesStart variable value="string:true" to output variable values at the start of each step-->
<Variable name="traceStepVariablesStart" initializer="string:true"/>

<!-- set the traceStepVariablesEnd variable value="string:true" to output variable values at the end of each step-->
<Variable name="traceStepVariablesEnd" initializer="string:true"/>

<!-- set the traceStepVariableNames variable value="string:varA,varB,..." to limit which variables are output-->
<!-- when set only the variable names in this list will be output-->
<Variable name="traceStepVariableNames" initializer="string:account launcher"/>

<!-- set the traceStepLog4j variable value="string:true" select a log4j output.  If not present or false output goes to stdout-->
<!-- output will be routed to log4j.logger.SERI.Workflow.<your workflow name> -->
<!-- enter a value of log4j.logger.SERI.Workflow.WorkflowX=trace in the log4j.properties -->
<Variable name="traceStepLog4j" initializer="string:false"/>

3) Step level enhanced tracing can be utilized by adding the following arguments to the step.  Step level arguments override the workflow level variables defined above.

<!--set traceStep true/false to output step start and end -->
<Arg name="traceStep" value="true"/>

<!-- set the traceStepVariablesStart variable value="string:true" to output variable values at the start of the step-->
<Arg name="traceStepVariablesStart" value="true"/>

<!-- set the traceStepVariablesEnd variable value="string:true" to output variable values at the end of the step-->
<Arg name="traceStepVariablesEnd" value="true"/>

<!-- set the traceStepVariableNames variable value="string:varA,varB,..." to limit which variables are output-->
<!-- when set only the variable names in this list will be output-->
<Arg name="traceStepVariableNames" value="varA, varB, varC"/>


**************************
* Potential Enhancements *
**************************


***************
* Limitations *
***************

*******************
* Version History *
*******************
25 Jun 2014
Initial Version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-StepLevelTrace