UseCase-ScopingSERI 0.1
08/03/2017 Initial Release

Contact: achim.reckeweg@sailpoint.com
************************
* Library dependencies *
************************


*************
* Execution *
*************
Import the setup.xml or add
UseCase-ScopingSERI to myDemo

************
* Background *
**************
Prospect want to see multi tenancy.
As we do not support real multi tenancy, we do support scoping.
This allows to demo users/managers that are assigned to a department and can only work on
people from that same department and can only request access to Roles assigned to 
their department.

Setup for an environment like this was cumbersome and error prone.
After importing this artifacts and execution of the supplied Tasks the Roles are assigned 
to a particular Scope and the Identities  are scoped based on the department attribute.
The assignment of the scopes to the roles is done by importing a csv file with the role 
importer
seri/data/DemoRoles/StandardDemo-PlaceBundleInScopesImport.csv

department                 scope
"Regional Operations"      "All"
"Executive Management"     "All"

"Inventory"                "Finance & Accounting"
"Accounting"               "Finance & Accounting"
"Finance"                  "Finance & Accounting"  

"Engineering"              "IT"
"Information Technology"   "IT"

"Human Resources"          "HR"

Manager can request entitlements and roles defined for users in their scope
Note: This are more people then their direct reports and differs from default SERI 
Users can request roles defined for their scope
A role admin is defined (Role Admin Capability assigned)

Note: Scoping is configured to allow access to unscoped objects - this is necessary to allow managers to request entitlements also
      Unfortunately there are identities that are created because the correlation is not configured correctly.
      This would pop up in the target population also
      Hence a filter is defined on the DynamicScope for Managers 
      
**************************
* Potential Enhancements *
**************************
--

***************
* Limitations *
***************
If you add a Role Manager (capability) to an admin and want him to manage roles within 
his scope, make sure to stick to scopes with Level1 (located directly below root) otherwise 
the role manager won't be able to manage it's role


****************
* To Configure *
****************
After setting up standard SERI
Import the setup.xml or add UseCase-ScopingSERI to myDemo
Run the Task "Setup Scoping Demo"



******************
* To Demonstrate *
******************
use a manager from within any Scope and, logon and show that you see only a limited number of identities
Keep in mind that the Scope All includes all identities because of Inheritance
                       Manager   non-Manager  Total
Total:                                       48          217       265
All (Amanda.Ross):                           12            3        15  --> 238 (wg. Unscoped)
Finance & Accounting (Catherine.Simmons):    18           93       111 
IT (Howard.Rose):                            12           64        76
HR (Andrea.Hudson):                           6           30        36

For showing Scoped Role Admin
Login as anyone of:
Identity          Department             Scope
Annie.Chavez      Human Resources        HR
Alice.Ford        Information Technology IT
Alan.Bradley      Engineering            IT
Allen.Burton      Finance                Finance & Accounting
Adam.Kennedy      Accounting             Finance & Accounting
Antonio.Fanklin   Inventory              Finance & Accounting
Amanda.Ross       Regional Operations    All 
 
Or Request the Business Role "Role Admin" for any Identity in your scope
There is no approval required
Login as the identity and show the additional Role Tab in the menu and the scoped Roles
       

*******************
* Version History *
*******************
07/03/2017
Initial Release

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/UseCase-ScopingSERI

Instructions for how to make a new tag for your contribution and 
create a "Testing Status" issue on Github: https://harbor.sailpoint.com/docs/DOC-21317

