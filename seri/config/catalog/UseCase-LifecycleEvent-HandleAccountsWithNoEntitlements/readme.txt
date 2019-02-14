UseCase-LifecycleEvent-HandleAccountsWithNoEntitlements
July, 2017

Contact: dana.reed@sailpoint.com, achim.reckeweg@sailpoint.com

************************
* Library dependencies *
************************
Relies on and extends UseCase-DeprovisionScenarios
automatically added by setup.xml

*************
* Execution *
*************
A Lifecycle Event is defined that if triggered, calls a rule that in turn calls a workflow for the configured
action. The Workflow will look for all applications that don't have entitlements and will undertake the proper
deprovisioning scenario on those accounts based on what is set on the application extended attribute.

**************
* Background *
**************
Some customers want to have an account on an application removed when the last entitlement of an account is removed.
This usecase extends the Deprovision Scenarios and adds an additional section on the application details page
The actions that can be taken are in accordance to the policies defined for the Deprovision Scenarios
- Disable Account Immediately
- Disable Account Immediately, Wait, Then Delete
- Delete Account Immediately
- Do Nothing


**************************
* Potential Enhancements *
**************************
The following actions passed testing against IIQ 7.1: 
- Disable Account Immediately
- Delete Account Immediately
- Do Nothing 

Disable Account Immediately, Wait, Then Delete needs further testing. 
***************
* Limitations *
***************


****************
* To Configure *
****************
import UseCase-LifecycleEvent-HandleAccountsWithNoEntitlements/setup.xml

or add it to the myDemo= property in build.properties like
myDemo=UseCase-LifecycleEvent-HandleAccountsWithNoEntitlements


******************
* To Demonstrate *
******************
Make sure that the artifacts are imported as this is NOT part of the standard SERI.
Assign and create entitlements on an appropriate application.
Remove the entitlements and run an Identity Refresh.
make sure that the option "Process events" is checked.
Have a look at the cube and the application account tab
Optionally, use any application specific data browser and show that the account has gone.


*******************
* Version History *
*******************
?? Created
June 2017 - Fixed several bugs and ported it to SERI 7.1.


*******************
* Logging         *
*******************
log4j.logger.SERI.Rule.IdentityTrigger-HasNonAuthoritativeAppsWithNoEntitlements
log4j.logger.SERI.Rule.InitAccountsNoEntitlementsDeprovWF

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-HandleAccountsWithNoEntitlements

