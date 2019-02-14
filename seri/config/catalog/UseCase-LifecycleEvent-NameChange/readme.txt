Name Change
December, 2015

Contact: jeff.bounds@sailpoint.com
************************
* Library dependencies *
************************
Yes, Assumes SERI install (SERIConfiguration)

*************
* Execution *
*************
When executed 

**************
* Background *
**************
This is a simple Lifecycle Event to demonstrate a last name change for a user.   
The identity cube name will be updated as well as Active Directory. 


**************************
* Potential Enhancements *
**************************
Add Name conflict resolution.
Handle multiple AD accounts

***************
* Limitations *
***************
DO NOT USE a user with multiple Active Directory accounts.   This would require more work to figure out what the account name would change to.
We are not interested in the complex use cases in this scenario.
Only changes DN, sAMAccountName, userPrincipalName and DN in AD.  Other attributes can be changed with Identity Attribute Sync Targets.
This does not check for name conflicts!

****************
* To Configure *
****************
Run Setup.xml


******************
* To Demonstrate *
******************
In Orange HRM, change a user's Last Name.
Execute an Account Aggregation and Identity Refresh with "Process Events"
The user's cube name will be changed to match the new last name (Ie.  Amanda.Ross becomes Amanda.Smith)
The Active Directory account changes as well.   This workflow only changes DN, sAMAccountName, userPrinciplaName and CN.

*******************
* Version History *
*******************
June 25, 2015 - Creation
September 14, 2015 - Finally fixed errors.
March 2016 - Added userPrincipalName. 


*******************
* Logging         *
*******************
log4j.logger.SERI.Workflow.NameChange.Start
log4j.logger.SERI.Workflow.NameChange.ChangeCubeName
log4j.logger.SERI.Workflow.NameChange.CreatePlan
log4j.logger.SERI.Workflow.NameChange.Provision
log4j.logger.SERI.Workflow.NameChange.saveLink

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-NameChange
