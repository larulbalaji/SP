LimitedRoleRequests
2 March 2017

Contact: edwin.grimminck@sailpoint.com
************************
* Library dependencies *
************************
none

*************
* Execution *
*************
Deploy all artifacts and make sure your demo managers 'Department' attribute matches an organizational role level in your role model

************
* Background *
**************
Goal of this UseCase is to demonstrate a way of limiting the number of roles that are available to request for different
types of Identities depending on their Function and Department.
Employees (non-managers) are only allowed to request Business Roles that are tagged as 'Employee' (using a custom
role attribute called 'roleTag'), this UseCase contains 3 of these Roles underneath a 'Corporate Roles' organizational role.

Managers are only allowed to request Roles that are part of the Role hierarchy within their Department. This
depends on a match between the name of an Organizational role within the Role model and the Department value. This will for instance
work for Peter Powell (Human Resources).
If no match between a managers Department and the role model is found all Business Roles are available, for example for Amanda Ross.
Jerry Bennett still has an overview of all roles and entitlements.

**************************
* Potential Enhancements *
**************************
-

***************
* Limitations *
***************
-

****************
* To Configure *
****************
Make sure your demonstration managers have a department name corresponding to a toplevel organizational role name.
See setup.xml for the import order of artifacts.

******************
* To Demonstrate *
******************
Login as a typical end user (non-manager) -> only 3 roles are requestable
Login as a manager with corresponding role model -> only Roles within the Role Models hierarchy are requestable
Login as manager without corresponding role model -> All Roles (but no entitlements) are requestable
Login as Jerry.Bennett -> All Roles and Entitlements are requestable

*******************
* Version History *
*******************
2 Mar. 2017
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LimitRequestableRoles
