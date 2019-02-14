New Identity - use I18N rules - Filter managers
2014-06-03

Contact: hrv@sailpoint.com

************************
* Library dependencies *
************************
i18N libraries, part of import

*************
* Execution *
*************


************
* Background *
**************
We need a way to guarantee uniqueness on cube names, email addresses, etc.
Part of this was built for a POC and I modified it into a SERI format, offering the following capabilities:
- Create Unique ID's / Cubenames / Email address on aggregation from OrangeHRM
- Create Unique ID's / Cubenames / Email address on "Create Identity" via UI. Great visibility into how it works (triggers on lastname field being changed)
- The Identity Creation form will only display managers in the manager selection field (Filtered)
- Optional: The Identity creation form is pre-configured to dynamically generate a password based on a password policy in IIQ. Uncomment the relevant sections in the form XML.
- Optional: Can use the unique ID (now cube name) moving forward for SAMaccountname, DN, CN, etc. (but you will need to modify the provisioning policies for each app to do so)

Will also import a new AD-DN namer rule because the old one generated AD names on first.last and would break in a scenario where uniqueness is required.


**************************
* Potential Enhancements *
**************************
Make Termination date work....


***************
* Limitations *
***************
Termination date does not work in the user creation form.... there is a bug in IIQ (Workflow does not handle date field), workaround is complicated.

****************
* To Configure *
****************
Import setup.xml

Because we are going to create the cube name based on the generation of a unique id, 
standard correlation will not work and potentially, a new Identity will be created on each aggregation.
We therefore need to add a correlation rule to the Authoritative Application in order to match on 
whatever unique identifier the authoritative application provides. In most cases, that will be the Employee ID  

- Set the "Identity Attribute" on the Human Resources Application to "empid" 
- Do NOT set a "Display Attribute" value
- Set the correlation rule "Correlation - EmployeeID" on the Human Resources Application
- Set the creation rule "Set Unique Username (I18n) with email on Identity Creation"

Optionally, uncomment values in the "Form-NewIdentityCreation-FilterManagers.xml" to use a password generated based on a (non-application specific) password policy.

**Note:**
When you apply these settings to a already aggregated SERI instance, all Human Resource accounts will be visible twice on the cube. 
Once with the account name of "first.last" and ocne with the account name of "empid"
Not an issue when you import and make changes before running Demo Setup.

******************
* To Demonstrate *
******************
Use your imagination, but I suggest playing with the Identity Creation Form. 

*******************
* Version History *
*******************
2014-02-20 First release for SERI
2014-04-15 Added auto config for HR app (overwrites) for partner build process (well, me that is...)
2014-06-03 Made sure selections for Job Title, Department and location pull back from pre-existing values in IIQ Identities. 

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-i18nUsernamesAndUniqueID