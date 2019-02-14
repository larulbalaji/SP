Transfer Processor with extra's
20140508

Contact: hrv@sailpoint.com
************************
* Library dependencies *
************************
Yes. /seri/xml/Rule/RuleLibrary-Demo-Workflow.xml

*************
* Execution *
*************

************
* Background *
**************
The transfer processor from the "old" demo with extra's.

On Manager change it will ask the old manager if access should be retained (and for how long) or revoked.
On retain it will reschedule
On Revoke, it will issue an Access Certification.

I added the functionality that on a retain, both the old manager and the new manager can still see the user in their "list" to request or revoke access.

**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************

****************
* To Configure *
****************
Run Setup.xml

Configure LCM / Manager and enable "Share attributes with the requester". Add the "transferManager" attribute to the list.

Run an Identity Refresh with the option "Refresh Identity Attributes"
This will populate the "transferManager" attribute for all managers with their own displayable name. (to prevent any manager with a blank attribute to "share" that with all users with a blank attribute)

******************
* To Demonstrate *
******************
Change a manager on an Identity.
See the workitem for the old manager (Retain / Revoke decision)
On Retain, the workflow will be started again on the selected date.
On Retain, the user will now show up on both the old manager and new manager's list of people they can request access for. Show how the old manager still has the user in his list, even though the manager is now different.


*******************
* Version History *
*******************
16-9-2013
Integrated into SERI
Added the "transferManager" functionality.


8-5-2014
Automated the set as much as possible by adding IdentityTrigger and ObjectConfig


********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-TransferProcessor2