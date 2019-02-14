ERP Portal Application v 1.0.0
1 July 2013

Contact: kevin.james@sailpoint.com

**************
* Background *
**************
This resource was primarily designed to showcase the UserStore SOAP connector/application. Some data has been migrated from the ERP
system in the SE Demo, but the group names have been modified to be less finance-focused, so we have another set of data.

**************************
* Potential Enhancements *
**************************
Figure out how to make boolean columns for inactive,service and privileged. setting it to boolean doesn't seem to read "TRUE" and "FALSE" properly
Write the provisioning policy

***************
* Limitations *
***************
Currently the provisioning policy isn't done

****************
* To Configure *
****************
You will need the ERP Portal application installed. If you are running on tomcat and the tomcat location is configured in build.properties,
"ant UserStore-ERPPortal" from the SERI directory will install it.
If not, you will need to deploy the UserStore.war file from $SERI\resources\UserStore, and then:
- copy $SERI\config\catalog\data\ERPPortal\attributes.csv to the UserStore's WEB-INF\classes
- copy $SERI\config\catalog\data\ERPPortal\users.csv to the UserStore's appserver
- modify the UserStore's WEB-INF\web.xml to change the storeLocation parameter

in IIQ console or the System Setup page, import seri/catalog/Resource-ERPPortal/setup.xml
This will give you the Correlation rule, the Application definition and a Task Definition to aggregate
Once you have aggregated, you will need to run an Identity Refresh with "Refresh assigned, detected roles and promote additional entitlements"
and "Promoted Managed Attributes" enabled


*******************
* Version History *
*******************

1 July 2013
First ERP version of UserStore

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Resource-ERPPortal
