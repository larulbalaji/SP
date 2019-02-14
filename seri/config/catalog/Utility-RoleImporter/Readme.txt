RoleImporter v 2.1.0
Date of this revision/version: 02/02/2016

Contact: 
Sean.Koontz@sailpoint.com, 
Terry.Sigle@sailpoint.com, 
Dave.Smith@sailpoint.com, 
Achim.Reckeweg@sailpoint.com

************************
* Library dependencies *
************************
sailpoint.seri.task.RoleImporter

*************
* Execution *
*************
Come up with csv files according to the documentation: Readme-Commands.txt.
Configure and Start the Task from within IdentityIQ.
Start Setup/Task/New Task/Role Importer.
Supply the path to the csv file and execute.

************
* Background *
**************
Very often it is necessary to set up a role hierarchy for a POC.
The Role Importer enables you to create arbitrary Entitlements and Roles.
Different Role Types are supported but must exist prior to use them.
As IdentityIQ does not support a seperate Org Hierarchy the organizational structure is most often
modelled using organizational roles. If customers asking for multi tenancy, we support this (at least
partially), with the creation and assignment of scopes.
Even if scopes are not directly related to Roles, the role importer was extended with two commands 
"Add Scope" and "Add Scope Assignment" to support this usecase. 

**************************
* Potential Enhancements *
**************************
Currently nothing planned

***************
* Limitations *
***************
Make sure to create the hierarchy in the correct sequence
Objects further up the tree must exist first!

****************
* To Configure *
****************
Nothing special needed.
After SERI is setup correctly the Role Importer is available

******************
* To Demonstrate *
******************
Start Setup/Task/New Task/Role Importer.
Supply the path to the csv file and execute.

*******************
* Version History *
*******************
Date
2.1.0 02/02/2016

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-RoleImporter