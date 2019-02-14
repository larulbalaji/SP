Remove Review Required Rule
Date: October 16, 2017
Author: jeff.bounds@sailpoint.com

************************
* Library Dependencies *
************************
None

************************
*      Execution       *
************************
This is a simple rule that can be run from iiq console or a rule runner. 



************************
*      Background      *
************************
For some application the default ProvisoiningPolicy will have ReviewRequired set.
This can be annoying when testing provisioning and it stops to present a form

************************
*   To Configure       *
************************
Import setup.xml.   Execute the rule
You can choose to run this rule on all apps or just one of the apps.
If you want to choose all apps, set 
boolean doAllApps = true; 
It's set to false as default. 

Otherwise, if you want to only run it on one app, leave that line unchanged and fill in the app of your choice here: 
String appName = "Active Directory";

The default is set to "Active Directory". 


********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-RemoveReviewRequired
