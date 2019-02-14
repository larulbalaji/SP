Risk Demo
20130730

Contact: hrv@sailpoint.com
************************
* Library dependencies *
************************
Creating the Certification depends on the Workflow rule library out of the old demo system. It is part of the artifacts included.

*************
* Execution *
*************

************
* Background *
**************
Show how the Risk model can be utilized and put to good use by using only two of the sliders in the entire model.


The output is less technical than native change and may appeal more to risk focussed persons.

**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************
Has been deliberately tied into a small scope of users (Population) and thus runs fast enough with the involved tasks.

Will overwrite the risk model

****************
* To Configure *
****************
Run setup.xml and you should be done.
This is configured on the "partner" version of the resource image, which includes configured shortcuts for running the tasks and hiding that from the demo itself.

******************
* To Demonstrate *
******************
Make sure demo user is in the spDemo demo population. We have scoped things off so the refresh happens quickly and not all demo users are affected (We use Henry Butler)

Show one user's risk score through advanced analytics, or show high risk users in manage /  Identity risk.
Add user to the TreasuryMgmt group in AD.

Run <CTRL><ALT><1> on the partner demo (or launch the 1-spdemo-risk task to aggregate and refresh.

Refresh advanced analytics / high risk users to see demo user is now a high-risk user.

Explain why
Explain you can model and tune without any impact
Once satisfied, to risk levels to a policy and let it fire off a workflow
Switch to separate browser 
window with manager (we use Amanda Ross) and show new certification item for Henry Butler (demo user)
Certify Identity and sign off
Run <CTRL><ALT><2> on the partner 
demo (or launch the 2-spdemo-risk task to aggregate and refresh.

Show how Risk has been mitigated.


Optionally you can also show reports before and after of Risky users.


*******************
* Version History *
*******************
29 July 2013
Integration of this partner scenario into SERI

21 Mar 2014
Moved to Demo-Standard
Renamed 1-spDemo-Risk to Reaggregate Monitored Users
Renamed 2-spDemo-Risk to Refresh Monitored Users

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-RiskDemo


