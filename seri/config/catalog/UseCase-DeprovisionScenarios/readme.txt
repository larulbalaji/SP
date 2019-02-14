Deprovision Scenarios v1.0.0
7 Nov 2013

Contact: dana.reed@sailpoint.com , kevin.james@sailpoint.com, brent.hauf@sailpoint.com

************
* Background *
**************
Dana built this functionality to automate the deactivation/reactivation process for application accounts. Kev did some tidying.
You can set a different deprovisioning process for each application. The process can be modified as to what happens when the account is disabled.

**************************
* Potential Enhancements *
**************************
When the product supports it, use ordering on the application extended attributes to make it not look stupid.
Fix the rehire scenario:
  When we set the inactive attribute on the cube to "false" through the quicklink, the LCE is not fired, so the accounts are not reenabled.
  Setting inactive=false through "Edit Identity" does fire though..


***************
* Limitations *
***************
Anything that someone using it might need to be aware of, for example �doesn�t work with manual workitems� or �attribute XYZ is hardcoded�

****************
* To Configure *
****************

By importing this use case, lifecycle events for terminate and rehire are generated. These events trigger off the "inactive" cube attribute.
Changing to true fires the terminate, changing to false triggers rehire.

Each application can now have configurable functionality. There are a number of actions that can be performed:
- Disable Account Immediately
- Remove Entitlements and Disable Account Immediately // Not yet implemented
- Disable Account Immediately, Wait, then Delete
- Delete Account Immediately
- Do Nothing

There are also hooks to be able to modify the data on the account or what actions are performed, before and after the action.

Account Deprovisioning
----------------------
- Update Link Rule:  This is a rule that will modify the data in the account, attributes and such (e.g. set description to "disabled on xx.yy.zz")
                     This rule is called *before* the plan is compiled. A Map of attribute name/value pairs is passed in, and a Map of same should
                     be returned 
- Update Plan Rule:  This is a rule that can modify the plan, for example to change the disable to a delete or something
           		       This rule is called *after* the plan is compiled, but before it gets sent to the provisioner
- Post Provisioning: This rule is called after the provisioner has been invoked on the plan. This rule is responsible for persisting its own
                     changes, for example through context.saveObject

Account Reprovisioning
----------------------
- Update Link Rule:  These rules follow the same pattern as the Deprovisioning rules, but are obviously performing functions during the
- Update Plan Rule:  reactivation process.
- Post Provisioning:


There is also a lifecycle event to take the termination date from a feed and set a future event to deprovision accounts. If the date changes in an
aggregation, the event will move any existing termination event. Notifications will be sent to the manager on set numbers of days before the user 
is due to leave. Configure by updating the leaverNotification value in "SERI Configuration". This is a CSV list of numbers of days before the leave event that the notifications will occur
  <entry key="leaverNotification" value="30,15"/>
  
******************
* To Demonstrate *
******************
To demonstrate delayed termination with prior notifications: 

1. Import setup.xml. 
2. Click on "Edit Identity" Quicklink for others. 
3. Choose an identity you want to terminate. Make sure the identity has a manager. 
The notification emails will go to the Identity's manager. 
4. Select "Inactive" and fill in the termination date in this format: dd/mm/yyyy. 
Remember: the notifications will go out depending on the number of days you've indicated in 
Configuration-SERIConfiguration.xml (<entry key="leaverNotifications" value="30,15"/>). 
The default is 30, 15. If you want it to be something else, change it accordingly. 
5. Sign in as the Identity's manager and approve the request. 
6. At this point, three future events should have been set up: 
two pending leaver notification workflows (each one sends an email) and one deprovision accounts workflow. 
So, if you have set the termination date at 30 days from now, the Identity's manager should be getting 
1 email. If you have set the termination date at 15 days from now, the manager would be getting 2 emails
immediately. 

TODO! Write out instructions for other scenarios. 

*******************
* Version History *
*******************
23 July 2015
Included the pre-notification of a leaver. 

27 May 2014
Updated the terminate user quick link form.  If a single user is selected you can view limited identity information along with the roles and applications they have.  This was in response to a customer requirement in a POC.

5 March 2014
Added call to Identity Request Finalize to TerminateDisableAccount workflow

24 Apr 2013
Integration into SERI

08 Feb 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-DeprovisionScenarios
