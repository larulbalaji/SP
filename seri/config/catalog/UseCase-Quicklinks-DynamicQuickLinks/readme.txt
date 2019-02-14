Truely Dynamic Scopes
2014-06-03

Contact: hrv@sailpoint.com

************************
* Library dependencies *
************************
None

*************
* Execution *
*************


************
* Background *
**************
Quicklinks show up regardless of the amount of work to do. 
For example: Policy Violations (0)

To make things appear just a bit more dynamic, we can (at login time only) make a decision 
to hide all quicklinks that have 0 workitems associated with them 

This results in a much more dynamic demo when different actors have different content.    

**************************
* Potential Enhancements *
**************************
Product enhancement required to re-evaluate the dynamic scopes themselves on a dashboard refresh.


***************
* Limitations *
***************
Only re-evaluates the underlying dynamic scopes on login.

****************
* To Configure *
****************
Import setup.xml

Logout and login again to see the difference.

******************
* To Demonstrate *
******************
Use your imagination....

All normal users will NOT have the (0) quicklinks visible
All admins will alwasy show all quicklinks (like Jerry.Bennett)

*******************
* Version History *
*******************
2014-06-03 First release for SERI

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-Quicklinks-DynamicQuickLinks
