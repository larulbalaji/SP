Approvals with Metadata Scenarios v1.0.0
13 Jun 2014

Contact: marc.gamache@sailpoint.com

************
* Background *
**************
Marc built this LCM Workflow and artifacts to demonstrate IIQ's metadata driven workflow capabilities.

**************************
* Potential Enhancements *
**************************
Build out a Quicklink and Workflow that will allow a user to select an Application/Entitlement/Role and be able to change the Approval flow only.

***************
* Limitations *
***************
?

****************
* To Configure *
****************
Import setup.xml. 
Configure an Application/Entitlement/Role to require Manager, Owner, or Addl Approvals.

******************
* To Demonstrate *
******************
Select an Application and check the 'Manager' Approval.
Select an Entitlement from the same Application and check the 'Owner' Approval.
Request the Entitlement for a user.
The Request should require the user's manager's approval as well as the Entitlement (or Application Owner if no Entitlement Owner has been configured) Owner's approval.

*******************
* Version History *
*******************
13 Jun 2014
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ApprovalsWithMetadata
