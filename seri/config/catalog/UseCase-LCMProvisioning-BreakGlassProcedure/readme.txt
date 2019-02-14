Break-Glass access requests
04-06-2014

Contact: hrv@sailpoint.com

************************
* Library dependencies *
************************
None

*************
* Execution *
*************
None

************
* Background *
**************
This workflow has been designed to replace "LCM Provisioning" as the default workflow.
Based on the membership of the "Break Glass Users" workgroup, members will be presented with a simple form at the start of the workflow.
(After the submit button is hit on the access request)

The form and selection allows the requester to bypass approvals
A notification will be sent to the security officer (WF Variable for security officer set to Jerry.Bennett) 

**************************
* Potential Enhancements *
**************************
Email now dumps the Plan in XML format. This could be made a bit nicer.

***************
* Limitations *
***************


****************
* To Configure *
****************
Import setup.XML 
Add users that are allowed to use this procedure to the "Break Glass Users" workgroup. 
Set the "Request Access" workflow in System Setup / Lifecycle Management / Business Processes to "LCM Provisioning - Break Glass"  

******************
* To Demonstrate *
******************
Configure as above.
Request access
Make a decision when the form pops up.

Show results as either approval required, or provisioned without approval.

*******************
* Version History *
*******************

04-06-2014
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LCMProvisioning-BreakGlassProcedure
