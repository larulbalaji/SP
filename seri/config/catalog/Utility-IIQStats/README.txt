IIQ Stats v1.0.2
31st October 2017

Contact: steve.kendall@sailpoint.com
(Original rule supplied by - Jason Slavick)

This is a simple rule that shows statistics about configured items 
within IdentityIQ


*************
* Execution *
*************

Run it from iiq console:
> rule "IIQStats"

or use the debug screen.

******************
* Example Output *
******************
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE String PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<String>IIQ Installation Statistics
***************************
Version Information
  IIQ Version: 7.2  Patch: 
  Build Date: 10/05/2017 09:56 AM -0500
  LCM Enabled: true

Identity Statistics:
  Total Identities: 258
  Active Identities: 247
  Inactive Identities: 11
  Uncorrelated Identities: 29
  Identity Snapshots: 102

Application statistics
  Total Applications: 258
  Application number by connector type:
    1	Active Directory - Direct	
    1	Delimited File Parsing Connector	
    1	JDBC	
    1	Logical	
    1	RACF	
    1	SOAPConnector	
    1	SunOne - Direct	

Link statistics:
  Links: 626

Certification statistics
  CertificationGroups: 6
  CertificationItems: 796

TaskSchedule stats:

Role statistics
  Role number: 152
  Role number by role type:
    77	business	
    2	container	
    67	it	
    6	organizational	
  Role types and if they are requestable:
    organizational			false
    it			false
    container			false
    entitlement			false
    business			true

LCM statistics
  IdentityRequests: 23

Entitlement Catalog statistics
  Entitlement Catalog entries: 1772

System Configuration statistics
  identitySnapshotInterval: 86400
  identitySnapshotMaxAge: 0
  taskResultMaxAge: 0
  identityIndexGranule: month
  groupIndexGranule: month
  certificationArchiveMaxAge: 0
  certificationMaxAge: 0
  lcmEnabled: true

Extended attribute statistics  Extended Identity Attributes: 0
  Extended Bundle Attributes: 0
  Extended Link Attributes: 4
  Extended Application Attributes: 0
  Extended ManagedAttribute: 0
</String>


*******************
* Version History *
*******************
31st Oct 2017
Enhanced version Information.

10 July 2013
First release

11 July 2014 v1.0.1
Readme updated.

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-IIQStats