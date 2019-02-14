Contractor to Employee v1.0.0
5 February 2018

Author: marc.gamache@sailpoint.com
		michael.stuyt@sailpoint.com

**************
* Background *
**************

This use case is for linking an existing Contractor identity to a new Employee identity as part of an on-boarding exercise from a HR system.  A security admin will be prompted with a form to confirm the match. If a match is confirmed, the old cube is linked to the new cube and all accounts with entitlements are moved to the new cube.


*************
* Limitations *
*************
The use case relies on making a match based on Identity Attributes.  Currently only the First Name and Last Name attributes are used.  The use case could be extended to include other forms of PII.
 

*************
* Execution *
*************
- Import artifacts via setup.xml. 
- Add "Jerry Bennett" to the SecurityIT workgroup: Setup --> Groups --> Workgroups Tab --> SecurityIT --> Add Jerry Bennett to the "Members"
- Onboard a contractor through the "Create Identity" quicklink.
- Request an AD entitlement for the contractor.  
- Ensure the AD account was created for the new contractor.
- Onboard a new employee through OrangeHRM with the same first and last names.
- Run the "Process HR Joiners" workflow.
- A form will be created and routed to Jerry Bennett to confirm the contractor/employee match.
- Confirm the match and the old identity cube will be deactivated and the new cube will have the existing entitlments transferred.
- Show the new identity cube with the "Former Identity" field containing a link to the old Contractor cube.
 
*******************
* Version History *
*******************
5 February 2018: Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ContractorToEmployee