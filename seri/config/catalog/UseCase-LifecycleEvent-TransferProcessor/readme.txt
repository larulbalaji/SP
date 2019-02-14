Original Transfer Processor
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
The transfer processor from the "old" demo.

On Manager change it will ask the old manager if access should be retained (and for how long) or revoked.
On retain it will reschedule
On Revoke, it will issue an Access Certification.

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


******************
* To Demonstrate *
******************
Change a manager on an Identity.
See the workitem for the old manager (Retain / Revoke decision)
On Retain, the workflow will be started again on the selected date.
On Revoke, a cert will be generated

*******************
* Version History *
*******************
16-9-2013
Integrated into SERI

8-5-2014
Automated the set as much as possible by adding IdentityTrigger 

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-TransferProcessor
