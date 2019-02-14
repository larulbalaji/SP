Joiner Process. V1
02/05/2014

Contact: norman.aroesty@sailpoint.com, dana.reed@sailpoint.com, kevin.james@sailpoint.com, brent.hauf@sailpoint.com

************************
* Library dependencies *
************************
LCM Workflow Library

*************
* Execution *
*************
This use case is a combination of work by Norm, Dana and Brent. Kev just pulled it all together and tidied it up a bit.
It provides a standard LCE joiner event for the SERI Demo environment.
This use case delivers emails to the user and their manager, with the mail to the manager providing the username and
a description of how to allow the user to set their initial password using the SMS password reset functionality. All this
is deferred until after birthright provisioning (according to standard role assignment rules) has completed
Notifications are also scheduled to notify the manager of a pending leave event.

************
* Background *
**************
Needed a good joiner event for demonstration purposes, showing the new 6.3 functionality that allows SMS password reset
but also making sure all accounts have been provisioned before we send the notifications


**************************
* Potential Enhancements *
**************************
Move the Twilio account details to build.properties


***************
* Limitations *
***************

****************
* To Configure *
****************
This use case is loaded OOTB as of SERI 1.5.  However, there are configuration steps that are required before executing the use case.

1) Update SMS Twilio Account Settings.  Navigate to System Setup->Login Configuration->User Reset.  
You will need to provide Twilio AccountID/AccountSID, Authentication Token and phone number assigned by Twilio.
2) Update the smsphone value in "SERI Configuration".  This is the value of the cell phone you want to receive the SMS texts for you demonstrations.

  <entry key="smsphone" value="5125551212"/>
  
Currently you need to do this through the debug page.

The phone identity attribute is sourced from the value in the configuration via the Identity attribute rule "Set Phone Identity Attribute".


******************
* To Demonstrate *
******************
Create a new user through your HR
Execute the 'Process HR Joiners' task. This will aggregate from HR then call a refresh with just process events switched on

*******************
* Version History *
*******************

7/4/2014
updated for deeplinks

7/11/2014
cleaned up workflow

7/13/2014
Added Brent's sms password reset stuff


********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-Joiner
