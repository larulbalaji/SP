End User Service Management v1.0.0
10 Aug 2016

Contact: achim.reckeweg@sailpoint.com

************
* Background *
**************
A prospect required a business-facing "Service Creation" capability. 
A Service to them is an application facing entitlement, plus everything that is necessary to assign this entitlement to a user.
The assignment (rule) should also be possible while creating the service.
This should be done in a business  friendly form and be available to a particular user group. In their case all project managers.
The applications that they want to act on are Active Directory and a company LDAP server.
For the POC I decided to stick with AD and use Sean's End User Group Management as a starting point.

This usecase introduces a new QuickLink in the category IT Services and defines a workflow "Create Service"
This use case only manages AD groups and requires the "Active Directory" application defined in SERI.
The DynamicScope capability was used to control who can access these IT Services from their dashboard.  
I stick to the Workgroup-based identity selector for the dynamic scoping.  
If the logged-in user is in the "BU Service Management Team" workgroup, they get access.
 

**************************
* Potential Enhancements *
**************************
 

***************
* Limitations *
***************
 - This use case requires "Active Directory" app defined in SERI and does not support group management for other apps
 - The naming scheme is a bit straightforward: AD Group is named literally. IT Roles are prefixed with "IT-", Business Roles are prefixed with "SR-"
 - All roles are created two container roles: technicalServiceRoles, serviceRoles


****************
* To Configure *
****************
Import the setup.xml and place your target identity that should be allowed to use this QuickLink into the "BU Service Managemnet Team"- 


******************
* To Demonstrate *
******************
The messaging for this demo is to convey that we provide a business-friendly group management capability via our extensible QuickLink functionality,
configurable forms and workflow.  This is underpinned by our Group Object Management APIs and built-in connector support, allowing for an end-to-end, 
automated solution.

Show that the AD Group that you are about to create does not exist yet.
Login as Amanda Ross or anyone who has been added to the "BU Service Management Team" workgroup.  You will see a new QuickLink category called "IT Services".

Click into the Create Service quick link.  Demonstrate the business-friendly form for specifying a new group.  The owner defaults to the logged-in user, 
but it can be changed.  There is client-side validation on the Service Name field (checks against Entitlement Catalog).  
The CreateService Usecase ONLY creates Security Groups.

Most importantly, the initial identity membership can be defined by creating a MatchList/Filter for the business role.
I implemented the methods "Pick Identity", "AttributeMatch", "Predefined Population" and "FilterString".
If you select either of them an apropriate MatchList is created and stored at the business role.
If you check the "Assign Service Now" checkbox, a refresh task is launched. Caution: if a filter is set on the task it is not reset.
And it is a full Identity Refresh and is time consuming. Additionally a Full Text Index is launched to make sure that the created service is searchable.


*******************
* Version History *
*******************
10 Aug 2016
First version 

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-EndUserServiceCreation

