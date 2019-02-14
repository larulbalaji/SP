Dynamic Approval Frameworkd v1.0.0
11 sept 2017

Author: dana.reed@sailpoint.com

**************
* Background *
**************

Workflows require complex logic for approvals. Which are required? which are auto approved? Which are filtered out as they do not require that specific type of approval.

This framework allows users to use the UI to configure complex approval flows easily

**************
* Installation *
**************
Run setup.xml
Note: this use case does rely on some java files. They should already be included in seri.jar
Note: The entitlement/role attributes need to be created and configured by the user. 
Demo / setup  video: 
https://sailpoint.webex.com/sailpoint/ldr.php?RCID=2ebe0209ced0f43478bf8116e13ecae9

**************
* To Run *
**************
1) Open up Dynamcic Identity Request Approve in the business process editor
2) Add an "approval" step from your step library.
3) If you want approvals that are not approved by a explicit step to be approved, you also will want to add the "Approve Items With No Approvals" step. This will set the "approved" value to true on items that have not been explicity approved by a user  or by rule.

Configurations: 
1) Auto-approved and Approval Required values may be set on the approval itself or the approval may be configured to look at metadata on the requested item (role or entitlement)
2) Approval Required rules or attribute values must return true or false;
3) Auto-approved attributes or rules may return true or false, or a Rule name. When a auto approved Rule is executed, it must return a map in the following format. The auto approved rule included in the use case provides and example and possible configurations)
    a map status value of 'approved" will be auto-approved
    a map status  value of "approvalRequired" will require the approval and is not auto approved
    a map status  value of "rejected" will be auto-rejected. 
    Comments returned by an auto-approve rule will be included in the identityrequest ONLY when autoapprove = true;
 4) When Approval Required is disabled, the approval is assumed to be required, unless otherwise filtered out by metadata attributes (i.e. managerApprovalRequired = false)
 5) When Auto Approval is disabled, an approval is assumed to NOT be autoapproved and will require approval is approvalRequired is true.
 6) Risk threshold approvals evaluate the identity risk first and secondarily will evaluate the entitlement/role threshold.
 7) If an approval owner is required and not specified, spadmin typically is the hardcoded default approver.
 8) Approval descriptions may include variables in the $(identityName) type format.
 
 
*******************
* Version History *
*******************
11 Sept 2017
Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-DynamicApprovalFramework

Please report also all issues to Dana.reed@sailpoint.com
