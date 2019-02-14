Create User 
01/01/0000 - Initial version submitted by Dana Reed


Contact: dana.reed@sailpoint.com brent.hauf@sailpoint.com

**************
* Background *
**************
This set of artifacts provides a framework to present different forms that utilize the identityModel to create users.  SERI provides CreateUserForm
and CreateVendorForm implementations.  You can add new forms to this implementation by copying modifying one of the forms.  

This approach allows differing user experiences to be presented using the same test core framework.  Essentially the framework presents the form
as configured set in the formName variable Workflow-CreateUser and processes it.  Here is the high level flow.

1) Initialize workflow and create an empty identityModel.
2) Pass the identityModel into defined form.
3) Present a confirmation form after the form has been submitted to allow the requestor to verify their input.
4) Build a plan the identityModel and call "LCM Create and Update" with the defined approvalScheme.
5) If refreshRequestor=true refresh the manager status of the requestor. This is typically done if the identity created reports to the manager.
this lets a LCM OOTB functions to immediately available to the requestor as the manager of the new identity.

Due to the fact that "LCM Create and Update" creates the identity request tracking, auditing, desired approvals, birthright provisioning all functions
OOTB.

CreateUserForm
--------------
This form allows you to pick from the type of user Employee, Vendor, IBM, Partner.  This shows and hides different fields on the form
to demonstrate dynamic forms and field dependencies.  It also shows a model for field by field authorization to the form.

<SPRight  displayName="view_company" name="ViewCompany">
  <Description>view_company</Description>
</SPRight>
<SPRight  displayName="edit_company" name="EditCompany">
  <Description>edit_company</Description>
</SPRight>

Above our two SPRights that the requestor must have to view or edit the company field.  The view Right will hide or show the field appropriately.
The Edit Right will make the field read only as appropriate.

This model is implemented across many fields in the form.

CreateVendorForm
----------------
This form allows for the creation of 3rd party vendor identities and an administrator for that vendor.  All identities created with this form
will report to the requester.  Identities are created with employeeType of "Vendor" or "Vendor Administrators".  "Vendor Administrators" can be 
on-boarded IIQ system administrators.  Vendor administrators can then login and add vendors or vendor administrators.  The requester will be the manager
of the new identity.  This allows them to perform LCM actions on behalf of their reports such as Request Access, Manage Accounts, Change Passwords. etc.

****************
* To Configure *
****************
-Rule Libraries Dependencies
Approval Library
LCM Workflow Library


Create Vendor
--------------
This use case is loaded OOTB configured with the CreateVendorForm with the SERI HealthCare demonstration. 

IMPORTANT: The Workflow-CreateUser.xml is modified during the import-healthcare process.  Variables in the workflow are set as follows:
formName = CreateVendorForm
refreshRequestor = true
approvalScheme = none

To use the CreateVendorForm without using the import-healthcare option change the variables above and import the setup-CreateVendor.xml
AND your modified Workflow-CreateUser.

Utilize the Create Vendor quicklink to on-board "Vendor Administrators" and then login as the "Vendor Administrator" to manage the new identity.

-Rules dependencies
Query Identity Attribute"


Create User
-----------
Import the setup.xml provided in this resource catalog.

IMPORTANT: The CreateUserForm has many dependencies on SPRights (see setup.xml) and dynamically loaded rules (see dependencies below).
If you are having issues insure that the required Rights, Capabilities, Rules and Rule Libraries are imported.

-Rules dependencies
hasViewPermission
hasWritePermission
getLocationValue
Query Identity Attribute"

******************
* To Demonstrate *
******************
Create a new user through your HR
Execute the 'Process HR Joiners' task. This will aggregate from HR then call a refresh with just process events switched on

*******************
* Version History *
*******************
01/01/0000 - Initial version submitted by Dana Reed some time early when SERI was created.
9/17/15 Initial Version (well someone had to finally add a readme anyway! ).  Enhanced to support presentation of differing forms by 
the same core workflow.  Driven by Create Vendor use case for the healthcare vertical.  This is implemented in a generic way and is not specific
or dependent on SERI healthcare.

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-QuickLink-CreateUser