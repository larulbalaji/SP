Location Change
December 1, 2015

Contact: jeff.bounds@sailpoint.com
************************
* Library dependencies *
************************
Yes, Assumes SERI install (SERIConfiguration)

*************
* Execution *
*************
Lifecycle Event that is executed when a user's location is changed.

**************
* Background *
**************
This is a simple workflow to demonstrate the ability to move users around in OU containers.
Deprovisioning workflows also show this by moving to a DisabledOU.
This is for simple location changes (ie Austin to London).
The Workflow now bypasses saveObject(link) and targetedAgg.   We use the AfterProvisioningRule to fix the link.
The code has been left in for the other methods.
This workflow can use a targeted aggregation to update the link.   Norm had issues with it not working.  I'm unsure why.
I added the option to do a context.saveObject(link) as well.   If the targeted aggregation does not work during testing:
  1.  Aggregate AD to fix the link first
  2.  Change the Workflow variable fixLink to be "Link" rather than "Agg".
  3.  Change the Users location and rerun Identity Refresh.  


**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************
DO NOT USE with identities that have multiple AD accounts (ie Catherine Simmons).
Should not be used in conjuction with TransferProcessor Lifecycle Events


****************
* To Configure *
****************
Run Setup.xml


******************
* To Demonstrate *
******************
In OrangeHRM, change a users location
Run an Aggregation and Identity Refresh with "Process Events"
The user will have moved from one OU to the correct OU.

*******************
* Version History *
*******************
June 25, 2015 - Creation
Dec 1, 2015 - Added AfterProvisioningRule.


*******************
* Logging         *
*******************
log4j.logger.SERI.Workflow.LocationChange.Start
log4j.logger.SERI.Workflow.LocationChange.CreatePlan
log4j.logger.SERI.Workflow.LocationChange.Provision
log4j.logger.SERI.Workflow.LocationChange.TargetAggregation
log4j.logger.SERI.Workflow.LocationChange.saveLink
log4j.logger.SERI.Rule.AfterProvisioning

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-LocationChange

