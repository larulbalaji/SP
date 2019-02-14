Role SunRise SunSet Report v 1.0.0
May 16, 2016

Contact: jeff.bounds@sailpoint.com
************************
* Library dependencies *
************************
None

*************
* Execution *
*************
Run from Analyze->Reports

************
* Background *
**************
When Roles are schedule to be assigned or removed via SunRise/SunSet dates.  There is no really good place to see all upcoming actions just for Roles.
The hidden URL (montitor/requests/requests.jsf) can show this, but has other Requests as well.   Plus during POCs I've had requests for a report that shows the information.

There are two ways this could have be achieved.   Request objects have the information (nextLaunch, roleName, identityName, etc..) but I decided to achieve this use case via the Identity.
The Identity objects have the information stored as well in RoleAssignments.   The issue is that RoleAssignments are stored in the Preferences CLOB which is not hibernate indexed.
The DataSource class does a basic SQL query to find ALL identities that have either a %endDate% or %startDate%.   Then that list of identities is used to query for the Identity Object.
Then using the normal APIs we find the RoleAssignments.

Doing it this way allowed me to figure out how to get around some limitations when dealing with IdentitySnapshots.   These reports should be added to SERI soon.

**************************
* Potential Enhancements *
**************************
Add in Date fields to limit the search.

***************
* Limitations *
***************
None I can thing of

****************
* To Configure *
****************
import via setup.xml

******************
* To Demonstrate *
******************
Have a request for a Role with either a SunRise or Sunset date (or Both).
Select report from Analyze->Reports


*******************
* Version History *
*******************
May 16, 2016 - Version 1.0

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-Report-RoleSunRiseSunset
