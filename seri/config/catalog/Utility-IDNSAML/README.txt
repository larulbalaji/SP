IDN SAML Utility 
December 2016
Contact:kevin.james@sailpoint.com
************************
* Library dependencies *
************************
Rule - IdentityNowSAML 
*************
* Execution *
*************
1. Update your build.properties to include the setting: 
withIDNSAML=true
2. Make sure that iiq-server.sailpointdemo.com resolves to the host of your IIQ server (127.0.0.1). In your hosts file,
it should look like this: 
127.0.0.1   iiq-server.sailpointdemo.com


************
* Background *
**************
This utility automatically configures IIQ to do SAML auth from the IDN org demo.identitynow.com. 

**************************
* Potential Enhancements *
**************************
None right now. 

***************
* Limitations *
***************
None right now. 


******************
* To Demonstrate *
******************
1. Navigate to demo.identitynow.com. 
2. Login as amanda.ross/ADpass1$
3. Click the 'Sailpoint IdentityIQ SAML' button. You should now be logged into your local IIQ as Amanda Ross. 

*******************
* Version History *
*******************
December 2016
First Version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-IDNSAML

