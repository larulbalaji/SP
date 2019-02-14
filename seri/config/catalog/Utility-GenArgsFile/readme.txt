Args File Utility v1.0.0
12 Jul 2013

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************
In many cases driving your workflow and provisioning test is time consuming.  A much more efficient way of testing workflow is to execute that test from "iiq console" passing an args.xml file (e.g. "workflow 'LCM Provisoning' args.xml").  This has the exact same effect as walking thru the GUI to execute the workflow, provisioning, interactive policy checks, approvals.  You can hook this into the your flavor of LCM provisioning workflow and it will write a file for every request.  If that request fails you can simple look at the request id in the GUI (i.e. manage-request) run your LCM Provisioning from iiq console by locating the corresponding variables file. 

This utility is provided to automate the process of creating an args.xml file.  The generated file (i.e. Demo - LCM Provisioning-args-0000000005.xml) is written  as "<workflow name>-args-<request id>.xml". The utility can be hooked into any workflow.  If the workflow does not have an identityRequestId value then the file will be written as "<workflow name>-args.xml".   The identityRequestId variable is created when calling the Initialize subworkflow.


*************
* Execution *
*************
"workflow myworkflow /tmp/myworkflow-args-0000000005.xml"


***************
* Limitations *
***************
-When wiring in after the initialize step there could be issues because the initialize step is creating additional variables etc.  This may not be a pure representation of what was past to the workflow.

****************
* To Configure *
****************
There are two ways to use this utility.  I think the most powerful way would be to wire it into your major LCM workflows.

Add the following entries to your workflow.  You can choose any file location you desire.

1) Define your location <Variable initializer="/tmp/" name="argsFileLocation"/>
2) Include the rule library

<RuleLibraries>
  <Reference class="sailpoint.object.Rule" name="genArgs"/>
</RuleLibraries>

3) Call it from a step.
<Step name="genArgs">
  <Script>
    <Source>
	<![CDATA[

       genArgs(wfcontext, argsFileLocation);
      

	]]>
    </Source>
  </Script>
</Step>

When wiring into an LCM workflow or any time you want the identity request number to be included in the file name.  Here is an example from the 6.1 "LCM Provisioning flow".  Basically you want to have the initialize step transition to the genArgs call and move the original Initialize transitions to the genArgsFile step.  Unfortunately this must be done after the Initialize step in the workflow in order to capture the identityRequestId that is generated in the initialize step.

 <Step icon="Task" name="Initialize">
   <Arg name="flow" value="ref:flow"/>
   <Arg name="formTemplate" value="Identity Update"/>
   <Arg name="identityName" value="ref:identityName"/>
   <Arg name="identityDisplayName" value="ref:identityDisplayName"/>
   <Arg name="launcher" value="ref:launcher"/>
   <Arg name="optimisticProvisioning" value="ref:optimisticProvisioning"/>
   <Arg name="plan" value="ref:plan"/>
   <Arg name="priority" value="ref:workItemPriority"/>
   <Arg name="policiesToCheck" value="ref:policiesToCheck"/>
   <Arg name="policyScheme" value="ref:policyScheme"/>
   <Arg name="source" value="ref:source"/>
   <Arg name="trace" value="ref:trace"/>
   <Arg name="requireViolationReviewComments" value="ref:requireViolationReviewComments"/>
   <Arg name="allowRequestsWithViolations" value="ref:allowRequestsWithViolations"/>
   <Arg name="enableRetryRequest" value="ref:enableRetryRequest"/>
   <Arg name="batchRequestItemId" value="ref:batchRequestItemId"/>
   <Arg name="endOnProvisioningForms" value="ref:endOnProvisioningForms"/>
   <Arg name="endOnManualWorkItems" value="ref:endOnManualWorkItems"/>
   <Description>
     Call the standard subprocess to initialize the request, this includes
     auditing, building the approvalset, compiling the plan into 
      project and checking policy violations.
   </Description>
   <Return name="project" to="project"/>
   <Return name="approvalSet" to="approvalSet"/>
   <Return name="policyViolations" to="policyViolations"/>
   <Return name="identityRequestId" to="identityRequestId"/>
   <Return name="violationReviewDecision" to="violationReviewDecision"/>
   <Return merge="true" name="workItemComments" to="workItemComments"/>
   <WorkflowRef>
     <Reference class="sailpoint.object.Workflow"  name="Identity Request Initialize"/>
   </WorkflowRef>
   <Transition to="genArgsFile"/>
 </Step>
<Step name="genArgsFile">
   <Script>
     <Source>
	<![CDATA[

        genArgs(wfcontext, argsFileLocation);


	]]>
     </Source>
   </Script>
   <Transition to="Exit On Manual Work Items" when="script:(isTrue(endOnManualWorkItems) &amp;&amp; (project.getUnmanagedPlan() != null))"/>
   <Transition to="Exit On Provisioning Form" when="script:(isTrue(endOnProvisioningForms) &amp;&amp; (project.hasQuestions()))"/>
   <Transition to="Exit On Policy Violation" when="script:(&quot;cancel&quot;.equals(violationReviewDecision) || ((size(policyViolations) > 0 ) &amp;&amp; (policyScheme.equals(&quot;fail&quot;))))"/>
   <Transition to="Create Ticket" when="script:(ticketManagementApplication != null)"/>
   <Transition to="Approve"/>
 </Step>

*******************
* Version History *
*******************
12 Jul 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-GenArgsFile
