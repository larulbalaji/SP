Args File Utility v1.0.0
12 Aug 2013

Contact: brent.hauf@sailpoint.com


**************
* Background *
**************
This use case provides Leave of Absence (LOA) and return from LOA life cycle event processing.  This is achieved by using application attributes to drive account provisioning changes upon LOA and return from LOA.  When an identity is deemed to be in LOA, via a life cycle event, then each application is processed for LOA based on the attributes.

The options are:

LOA
1) Disable immediately
2) Do Nothing
3) Leave of absence rule

Return from LOA
1) Enable immediately
2) Do nothing
3) Return from LOA rule

The rules have the same signature for LOA and return from LOA.  They are handed the identityModel and the link from the model that corresponds to that application.  The process relies on the buildPlanFromIdentityModel workflow action. The rules can then manipulate the link and or model which will result in a provisioning action.  Typical uses for these rules are to set description attributes "This account is on LOA per corporate policy do not enable this account - 12 Aug 2013 11:00:15am".  Other parts of the link and identityModel can be manipulated.  However not all changes will generate the desired provisioning plan (e.g. changing the DN for an AD account will not generate a move to a new OU).  For each account a new workflow will be launched "LOA: <identity name>" or "Return from LOA: <identity name>".  In addition, you will also see a request from the scheduler in "manage requests" for each account.  This will detail the provisioning that was attempted.


*************
* Execution *
*************
You will need to create two life cycle events for LOA and return from LOA.  The LOA lifecycle event must point  "Lifecycle Event - LOA Accounts". The return from LOA life cycle event will need to point "Lifecycle Event - LOA Return Accounts".

You will then need to update each application to select the LOA and return from LOA actions.  You may also need to create rules for each action.  Here is an example of an LOA rule for an Active Directory account.

<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Rule PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Rule   language="beanshell"  name="Rule_ADLOA" type="Workflow">
  <Description>A rule used in the LOA processing to set the account description for AD.</Description>
  <Signature returnType="Object">
    <Inputs>
      <Argument name="link">
        <Description>
        </Description>
      </Argument>
      <Argument name="identityModel">
        <Description>
        </Description>
      </Argument>
    </Inputs>
    <Returns>
      <Argument name="link">
        <Description>
        </Description>
      </Argument>
    </Returns>
  </Signature>
  <Source>
	
	String description = "Do NOT enable this account (IdM " + Calendar.getInstance().getTime() + "): User access to this account has been removed per corporate rules.";
  	link.put("description", description);
    return link;
  </Source>
</Rule>


Also note that you can test your rules independent of the life cycle event by using the Workflow-Test-LOARule.xml provided.  Note that you will need to modify the workflow.  See the description in the workflow for details.


DISABLING/ENABLING Identities
-The workflows currently disable the identity on LOA and enable the identity in return from LOA.  To change this behavior you need to set the disableIdentity variable "Lifecycle Event - LOA Accounts" and the enableIdentity variable in the "Lifecycle Event - LOA Return Accounts" workflow.


**************************
* Potential Enhancements *
**************************
-Handle moving an AD account to a new OU or LDAP accounts.  This would likely have to be handled by either enhancements to the buildPlanFromIdentityModel method or by generating specific plans based on a change in the DN in conjunction with the application type.

-The design could also be enhanced to reuse rules etc between LOA and Return from LOA the current implementation was derived from the deprovisioning use case and a large amount of cut and paste was used between LOA and Return from LOA.  So for example if there is a bug found in LOA it would likely have to be fixed in return from LOA and vice versa.

***************
* Limitations *
***************
-Currently due to bugs around disabling and setting the description, there is a separate provisioning request request sent for the disable and calling the rule.
-The process relies on the buildPlanFromIdentityModel workflow action.  Therefore, things like moving an AD account to a different OU while on LOA is not supported.

****************
* To Configure *
****************
import setup.xml and then configure your life cycle events for LOA and return from LOA.

******************
* To Demonstrate *
******************
Trigger you lifecycle event and then view the updated links after the requests have been completed.


*******************
* Version History *
*******************
12 Aug 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LOAScenarios
