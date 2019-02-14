Enterprise Directory v1.0.0
10 July 2013

Contact: sean.koontz@sailpoint.com

**************
* Background *
**************

This SERI catalog entry is for an LDAP resource target called "Enterprise Directory".  Upon import of the setup.xml, you will have an IIQ
application that uses the Sun One - Direct connector to manage an OpenDJ LDAP server running in the SERI AD Resource Image.

The OpenDJ server is seeded with a very small data set of accounts and groups, but it is enough to support LCM Request Access demo scenarios.

The provisioning policy for the application has been configured to auto-derive mandatory account attributes necessary to create an LDAP account.
So, assuming you select an identity that has a full set of identity attributes (i.e., not an orphan), you won't see a form and the LDAP account
creation will just happen behind the scenes.  Use the OpenDJ admin tool in the resource image to prove to your audience the account was created.

This resource is part of the SERI Standard Demo.

*******************
* Version History *
*******************
10 July 2013
Initial revision

October 2017
Migrated to IIQ 7.2 

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Resource-EnterpriseDirectory
