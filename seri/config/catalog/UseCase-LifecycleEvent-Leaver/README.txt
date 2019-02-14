Leaver Process. V1
02/05/2014

Contact: norman.aroestyd@sailpoint.com

************************
* Library dependencies *
************************
LCM Workflow Library
SP Util Rule Library (RuleLibrary-Util.xml)
Demo - Workflow RuleLibrary (RuleLibrary-Demo-Workflow.xml)


*************
* Execution *
*************
These artifacts are meant to be executed using a standard LCE leaver event. 

************
* Background *
**************
This is yet another leaver event.  What I like about this instance versus the standard leaver 
routine is the following:

1) If the user owns any apps or is part of a workgroup, an administrator alert goes out. 
2) Any resource that the user is part off (application), the application owner is alerted 
   to the departure.  
3) The workflow skips, any authoritative apps - does not try to disable HR when HR was the 
   source of the event)
4) For this demo, the disable does the following (see below).  
   This is in RuleLibrary-Util.xml getDisablePlan method.  I realize the other leaver event 
   uses meta data on the application.  That is cool.  I wanted to get this out in the seri wild because 
   step 1, 2, and 3.  
5) Schedule Account Delete Workflow for N days after disable.  Just to show capability.  



**************************
* Potential Enhancements *
**************************
Use meta data on applications as the other leaver event does.  I realize this may be confusing 
haveing two leave events, so we may want to merge the two together to form a better workflow

***************
* Limitations *
***************
You must be joking

****************
* To Configure *
****************
import all artifacts
configure leaver LCE
configure getDisablePlan method in RuleLibrary-Util.xml.  
getDisablePlan shoudl be easy enough to modify based on POC requirements

	if (appType.compareTo("Active Directory - Direct") == 0) {
		System.out.println("Build account modifications and disable request for: " + appName);
			
		AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, appName, null, identityName);
		acctReq.setNativeIdentity(link.getNativeIdentity());
		String description = "Terminated on " + Calendar.getInstance().getTime();
		acctReq.add(new AttributeRequest("description", ProvisioningPlan.Operation.Set, description));
		acctReq.add(new AttributeRequest("memberOf", ProvisioningPlan.Operation.Set, new ArrayList()));
		acctReq.add(new AttributeRequest("AC_NewParent", "OU=Disabled,OU=Demo,DC=seri,DC=sailpointdemo,DC=com"));
		acctReq.add(new AttributeRequest("manager", ""));

		plan.add(acctReq);				
				
		AccountRequest disableAcctReq = new AccountRequest(AccountRequest.Operation.Disable, appName, null, identityName);
		disableAcctReq.setNativeIdentity(link.getNativeIdentity());
		plan.add(disableAcctReq);

	} else if (appType.compareTo("SunOne - Direct") == 0) {
		System.out.println("Build account modifications and disable request for: " + appName);

				
		AccountRequest acctReq = new AccountRequest(AccountRequest.Operation.Modify, appName, null, identityName);
		acctReq.setNativeIdentity(link.getNativeIdentity());
		String description = "Terminated on " + Calendar.getInstance().getTime();
		acctReq.add(new AttributeRequest("description", ProvisioningPlan.Operation.Set, description));
		acctReq.add(new AttributeRequest("badgeStatus", ProvisioningPlan.Operation.Set, "InActive"));
		// Null out the group asginments.  Should be as simple as the following line but it is not.  But has been submitted
		// acctReq.add(new AttributeRequest("memberOf", ProvisioningPlan.Operation.Set, new ArrayList()));
		Object groupObject = link.getAttribute("groups");
		System.out.println ("***********************");
		System.out.println ("\t" + groupObject);
		if (groupObject != null) {
			List groupList = sailpoint.tools.Util.asList(groupObject);
			System.out.println ("\t" + groupList);
			for (int i = 0;  i< groupList.size(); i++) {
     			acctReq.add(new AttributeRequest("groups", ProvisioningPlan.Operation.Remove, groupList.get(i)));

 			}
		}
		System.out.println ("***********************");

		plan.add(acctReq);				
				
		// Disable on this resource, is setting badgeStatus to InActive - see above
		// AccountRequest disableAcctReq = new AccountRequest(AccountRequest.Operation.Disable, appName, null, identityName);
		// disableAcctReq.setNativeIdentity(link.getNativeIdentity());
		// plan.add(disableAcctReq);

	} else {
		if (app.isAuthoritative()) {
              System.out.println("Application is authoritative (i.e., SAP)...skipping disable");
        } else if (appType.compareTo("Logical") == 0) {
            System.out.println("Application is logical...skipping disable");
        } else {
			System.out.println("Build disable request for: " + appName);
			AccountRequest disableAcctReq = new AccountRequest(AccountRequest.Operation.Disable, appName, null, identityName);
			disableAcctReq.setNativeIdentity(link.getNativeIdentity());
			plan.add(disableAcctReq);
		}
	}
			


******************
* To Demonstrate *
******************
Have LCE triggered
			

*******************
* Version History *
*******************
Date
What I changed since the previous one

xx xxx 2013
Integration into SERI

xx xxx 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-Leaver
