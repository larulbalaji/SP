New Identity - Auto certification 
20140415

Contact: hrv@sailpoint.com & mark.Oldroyd@sailpoint.com

************************
* Library dependencies *
************************
Not that I know of, but could be the library referenced in the risk demo.

*************
* Execution *
*************


************
* Background *
**************
Simple demo to show how a LCM event can be triggered from a new user creation and cause the manager to receive an access certification for the new user
Allows to show Birthright provisioning, as well as explain that Compliance from day one is important because access is based on Job title and people do make mistakes...


**************************
* Potential Enhancements *
**************************
May want to restrict the LCM event to the demo population. 


***************
* Limitations *
***************
You need to import a proper LCM user creation scenario, such as "UseCase-i18nUsernamesAndUniqueID" to demo from within LCM. 
The tasks use the "Monitored Users" population to limit the amount of users required to aggregate and refresh.

****************
* To Configure *
****************
Import the setup.xml file
(Re)Create the shortcut on your desktop to hide running the aggregation task.
Optionally, import a user creation scenario, such as "UseCase-i18nUsernamesAndUniqueID"

******************
* To Demonstrate *
******************
Create a new user, either through LCM or through OrangeHRM.
When through OrangeHRM, make sure you set the employeeID to strat with "SP" or with "00". Next, trigger the aggregation (shortcut 3)

Go to the manager's browser and see the new user certification is available.
It will take a (new) manager less than 5 minutes to complete, but we ensure that any mistakes resulting in wrong access is addressed as soon as possible.

Optionally, run the report defined for Jerry Bennett.

*******************
* Version History *
*******************
Date
What I changed since the previous one

31 July 2013
Integration into SERI

1 August 2013
Included auto Tag generation, tags in the certification and a new Identity based report

15 April 2014
Moved to main SERI catalog and referred to i18n use case for importing a user creation form to keep overlap limited.

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-JoinerWithCertification

