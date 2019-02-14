AD Nested Groups Report v1.0
17/03/2016

Contact: sebastien.lelarge@sailpoint.com
************************
* Library dependencies *
************************
This report uses the following Java datasource class : sailpoint.seri.reporting.datasource.ADNestedGroupsDataSource

*************
* Execution *
*************
Run from Intelligence->Reports->Identity and User Reports
Click the 'AD Nested Groups Report'

************
* Background *
**************
This is more and more asked by our prospects or during POCs
Although IIQ stores inheritance information about group entitlements, this is not rendered user access reports.

**************************
* Potential Enhancements *
**************************
1. The report in its current version provides information about how a user has access to an AD group:
- direct access : the user is a direct member of this group
- indirect access : the user is a member of another group which inherits from the requested group
In case of indirect access, the report gives the name of the AD group via which the user has access to the targeted group. This could be enhanced by displaying the full path of groups

2. The report doesn't show information about roles in the sense that AD groups can be granted via roles.
This information could be added as a new column to give more information about how the user has access to the AD group

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
Select the report from Intelligence->Reports

The report has a configuration step where you can filter on identities and/or AD groups
- only correlated identities are available in the dropdown list
- only ManagedAttributes of type 'Active Directory' are available

- if you don't specify AD groups, all existing AD groups will be used in the reports
- if you don't specify identities, all ad groups assigned to identities will be present in the output
- if you only specify identities : the report outputs all AD groups accessed direcly or indirectly by these persons
- if you only specify AD groups : the report will give the list of persons having access directly or indirectly to these groups
- if you specify both AD groups and identities : the report will filter on both criteras

The report outputs these columns:
- Identity Name
- Application Name 
- Account
- Group : the name of a group selected in the filter
- direct : true if the user is a member of this group / false if user has access to this group through another group
- via group : relevant when direct is set to false. Gives the name of the group which grants access to the group referenced in the 'Group' column


*******************
* Version History *
*******************
17 Mar 2016 - S. Lelarge
Integration into SERI

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-Report-ADNestedGroups

