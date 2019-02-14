Use Case SIQ Sensitive Data SOD
06/13/2016

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************
This use case provides the ability to demonstrate detective and preventative SoD checks for entitlements marked as containing sensitive data.  In theory the entitlements would be marked as providing access to unstructured data classified as having financially sensitive data.  When a entitlements marked as having financially sensitive data are requested for a  non-employee a policy will be tripped in the UI.

*************
* Execution *
*************
1) Import setup.xml
2) Make sure you have an entitlement with the dataClassifications meta-data that includes “Financially Sensitive” in the CSV list of that value.
3) Execute a request for that entitlement for a user with an employeeType=Contractor

**************************
* Potential Enhancements *
**************************



***************
* Limitations *
***************

****************
* To Configure *
****************
See Execution section

******************
* To Demonstrate *
******************
See Execution section

*******************
* Version History *
*******************

5/13/2016
Initial Version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-SIQSensitiveDataSOD
