End User Group Management v1.0.0
15 Dec 2013

Contact: sean.koontz@sailpoint.com

************
* Background *
**************
A prospect required a business-facing "AD distribution list management" capability.  While IIQ supports Group Object Management OOTB, it is an 
administrative interface (not business friendly).  We decided to prototype their requirement using IIQ's extensible QuickLink functionality.

This use case introduces a new QuickLink category, IT Services, and two QuickLink workflows:  Request Group and Manage My Groups

This use case only manages AD groups and requires the "Active Directory" application defined in SERI.

The 6.2 DynamicScope capability was used to control who can access these IT Services from their dashboard.  For now, we use a simple Workgroup-based 
identity selector for the dynamic scoping.  If the logged-in user is in the "BU Service Management Team" workgroup, they get access.

This use case includes an example of using form references.  That is, the forms are independent objects and referenced by the workflow vs. 
being embedded in the workflow.

To help automate the demo setup, this use case introduces an "accountGroupRefreshRule" on the AD group agg task.  The rule takes the 'managedBy' field 
on the incoming group (a DN), resolves that to an identity and sets it as the entitlement owner.  This allows the "Manage My Groups" workflow to be demo'ed
without having to first provision a group via "Request Group".
 

**************************
* Potential Enhancements *
**************************
There are many potential enhancements...

 - Fully implement the "Delete Group" functionality that is part of the Manage My Groups workflow
 - Add more group attributes to be specified when creating and managing the AD groups
 - Enrich the email notifications
 - Add approval to the Manage My Groups
 - Add ability for other users who are not the group owner to search for and edit existing groups
 - Add confirmation page to the Request Group upon submission
 

***************
* Limitations *
***************
 - This use case requires "Active Directory" app defined in SERI and does not support group management for other apps
 - The "Delete" functionality in the Manage My Groups is not fully implemented.  Clicking the Delete button just ends the workflow.


****************
* To Configure *
****************
This catalog entry is included in the SERI Standard Demo, so it will be configured automatically as part of the SERI demo setup.

Some additional background...

The SERI AD RI has three groups whose 'managedBy' attribute is set.  During AD group agg, these owners get set in the entitlement catalog.  The 
owners are Jerry Bennett and Amanda Ross.

The Identity Creation rule that runs as part of HR load, puts Amanda Ross in the "BU Service Management Team" workgroup.  This allows you to 
demo this use case by logging in as Amanda.  Jerry will have access to the quick links because he has SystemAdministrator capability.


******************
* To Demonstrate *
******************
The messaging for this demo is to convey that we provide a business-friendly group management capability via our extensible QuickLink functionality,
configurable forms and workflow.  This is underpinned by our Group Object Management APIs and built-in connector support, allowing for an end-to-end, 
automated solution.

Login as Amanda Ross or anyone who has been added to the "BU Service Management Team" workgroup.  You will see a new QuickLink category called "IT Services".

Click into the Request Group quick link.  Demonstrate the business-friendly form for specifying a new group.  The owner defaults to the logged-in user, 
but it can be changed.  There is client-side validation on the Group Name field (checks against Entitlement Catalog).  Both Security and Distribution Lists
can be created.  Most importantly, the initial identity membership can be specified such that the group is created and then the identities are added to
the group.  Upon clicking submit, the workflow proceeds directly to approval.  The default approver is the AD app owner, AD Admins workgroup.
 
When clicking on "Manage My Groups", the drop-down will pre-populate with groups that the logged-in-user is the owner of (based on entitlement catalog).



*******************
* Version History *
*******************
Date
What I changed since the previous one

13 Jan 2014
Integration into SERI

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-EndUserGroupManagement
