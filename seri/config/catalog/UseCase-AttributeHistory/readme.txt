Attribute History reporting v1.0.0

Contact: kevin.james@sailpoint.com

**************
* Background *
**************
A common customer request is for us to show when certain attributes were modified, for example, when did Steve's Security Level change?
He has top secret access now, I don't know when that happened..

**************************
* Potential Enhancements *
**************************
Investigate whether the standard audit event
<AuditEvent action="change">
is sufficient for our needs. This event, however, doesn't record the previous value, just the new value.

***************
* Limitations *
***************
This is more of a product limitation - in advanced analytics, you can't search on the "String" contents of an AuditEvent.

****************
* To Configure *
****************
Import the use case. In System Setup->Identity Mappings, for the attributes you want to monitor, set the 
Value Change Rule to be 'Update attribute history'. Now any changes to this cube attribute will be audited

******************
* To Demonstrate *
******************
Set some cube attributes to be monitored as in the 'To Configure' section. make some changes to users' attributes.
Configure the report 'Attribute History Report' as required.


*******************
* Version History *
*******************

25 April 2014
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-AttributeHistory
