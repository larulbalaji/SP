Risk Based Approvals. V1.1
28/03/2014

Contact: rishi.garrod@sailpoint.com

************************
* Library dependencies *
************************
LCM Workflow Library

*************
* Execution *
*************
None

************
* Background *
**************
This workflow has been designed to replace "LCM Provisioning" as the default workflow called
on an access request. The ProvisioningPlan will be split into 2 ProvisioningPlans one for high risk and one for low risk. "LCM Provisioning" or equivalent will be called for both plans.
Each entitlement/role in the request will be examined for the risk score. If the score is higher than the pre-defined limit then the request will follow the normal request procedure.
If however it is below the limit then no approvals will be required.

**************************
* Potential Enhancements *
**************************
We could easily use another attribute to drive the decision such as extended attributes.

***************
* Limitations *
***************
Any user interactions will become work items. This process takes a single interactive request and turns it into 2 non-interactive processes.
This split will not do an interactive policy check across the whole request before it is split.

****************
* To Configure *
****************
There are 2 artifacts to import, the workflow and the rule it uses.

Use this workflow instead of LCM provisioning as the default workflow in "Lifecycle manager Configuration/Business Processes/Request Access".
The logger is setup for "SERI.Workflow.RiskBasedApprovals". Add this to log.properties file to display debug info.

There are 3 workflow attributes which should be setup for you specific needs:

"entitlementRiskApprovalLevel" - The risk level boundary for requiring an approval of specific entitlement.
"roleRiskApprovalLevel" - The risk level boundary for requiring an approval of specific role.
"provisioningWorkflow" The workflow that will be called to process the generated provisioning plans. The default value is "LCM Provisioning".

******************
* To Demonstrate *
******************
Configure as above.
Set up the risk levels on a number of roles/entitlements
Do an access request.
Sit back and enjoy.


*******************
* Version History *
*******************


xx xxx 2013
Integration into SERI

xx xxx 2013
First release

28 Mar 2014
Replaced "&lt;" with "<" since the rule is in a CDATA section


********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LCMProvisioning-RiskBasedApprovals
