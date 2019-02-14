IdentityIQ Loopback Connector
14 March 2017

Contact: achim.reckeweg@sailpoint.com

**************
* Background *
**************
It was not possible to add identities to IdentityIQ internal Workgroups. Also Capabilities assigned by Roles were visible only on the user rights tab. 
With this Loopback Connector developed by Christian Cairney (Professional Services) the IdentityIQ internal entitlements can be transparently integrated. 
The Capabilities and Workgroups are requestable like any other entitlement utilizing the Request Access page. And after an Account Aggregation the assigned capabilities and workgroups are listed on the entitlements page. 
The connector is also part of the SSDv3 package supplied by Professional Services. 

See SSF_Tools_LoopbackConnector_UserGuide.pdf supplied by Christian for further details
****************
* To Configure *
****************
Nothing to be configured

On Account Aggregation the Identities know to IdentityIQ will be created as accounts.
Capabilities are shown as entitlements
Workgroups the Identity belongs to are shown as entitlements

AccountGroup Aggregation will create the Workgroups and Capabilities as Entitlements in the Entitlement Catalog making them Requestable like any other entitlement also.

On assigning a workgroup entitlement the Identity will be placed into the workgroup.
Note that indirect assigned capabilities (via workgroup) are not shown as entitlement right now.

On assigning a capability the Identity will be assigned the capability


*******************
* Version History *
*******************

14 March 2017
Initial version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Connector-IIQConnector
