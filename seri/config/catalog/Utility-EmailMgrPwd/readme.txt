Email Manager Passwords v1.0.0
10 Jul 2013

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************

This rule emails the manager a users new passwords for each new account created in the provisioning project.  

*************
* Execution *
*************

This rule must be called after the plan as been compiled in a workflow which produces the project.  It should be called after provisioning has been successful.  The value of passwordAttrName application attribute must be set to the attribute in the account provisioning policy that will contain the password.  For example for active directory this would be *password*.
 
I have tested this in the LCM Provisioning Workflow and Identity Refresh workflow in 6.1.  Here is an example of how to call a rule from a workflow.  
 
  <Step icon="Task" action='script:emailManagerAccountPasswords(project, passwordNotifyTemplate)' name="EMail Manager Passwords">
    <Transition to="end"/>
  </Step>

This utility also includes a test harness that allows you to test your email template and processing via iiq console.  For example you can test using the following command "workflow EmailMgrPwdTest args.xml". Do do this you need to capture the project and create your own args.xml file which provides the parameters to the rule.


<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE Attributes PUBLIC "sailpoint.dtd" "sailpoint.dtd">
<Attributes>
  <Map>
    <entry key="project">
      <value>
		<ProvisioningProject identity="Alice.Ford">
		  <Attributes>
		    <Map>
		      <entry key="disableRetryRequest">
		        <value>
		          <Boolean>true</Boolean>
		        </value>
		      </entry>
		      <entry key="identityRequestId" value="0000000007"/>
		      <entry key="requester" value="spadmin"/>
		      <entry key="source" value="LCM"/>
		    </Map>
		  </Attributes>
		  <ExpansionItems>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="ObjectType" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="User"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="*password*" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="P@ssw0rd"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="pwdLastSet" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="true"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="IIQDisabled" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="false"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="sAMAccountName" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="Alice.Ford2"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="givenName" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="Alice"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="sn" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="Ford"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="description" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="Created by IdentityIQ on 07/10/2013 17:21:09"/>
		    <ExpansionItem application="Active_Directory" cause="ProvisioningPolicy" name="msNPAllowDialin" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" operation="Add" sourceInfo="Active_Directory" value="Not Set"/>
		  </ExpansionItems>
		  <MasterPlan>
		    <ProvisioningPlan>
		      <AccountRequest application="Active_Directory" op="Create">
		        <Attributes>
		          <Map>
		            <entry key="forceNewAccount" value="true"/>
		            <entry key="interface" value="LCM"/>
		            <entry key="operation" value="Create"/>
		          </Map>
		        </Attributes>
		      </AccountRequest>
		      <Attributes>
		        <Map>
		          <entry key="identityRequestId" value="0000000007"/>
		          <entry key="requester" value="spadmin"/>
		          <entry key="source" value="LCM"/>
		        </Map>
		      </Attributes>
		      <Requesters>
		        <Reference class="sailpoint.object.Identity" id="2c9090b33fca1d49013fca1d807400ce" name="spadmin"/>
		      </Requesters>
		    </ProvisioningPlan>
		  </MasterPlan>
		  <ProvisioningPlan nativeIdentity="Alice.Ford" targetIntegration="Active_Directory">
		    <AccountRequest application="Active_Directory" nativeIdentity="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com" op="Create" targetIntegration="Active_Directory">
		      <Attributes>
		        <Map>
		          <entry key="forceNewAccount" value="true"/>
		          <entry key="interface" value="LCM"/>
		          <entry key="operation" value="Create"/>
		        </Map>
		      </Attributes>
		      <AttributeRequest name="ObjectType" op="Add" value="User"/>
		      <AttributeRequest name="givenName" op="Add" value="Alice"/>
		      <AttributeRequest name="sn" op="Add" value="Ford"/>
		      <AttributeRequest name="sAMAccountName" op="Add" value="Alice.Ford2"/>
		      <AttributeRequest name="*password*" op="Add" value="1:gF98hCU32oRbJsImB//fnQ==">
		        <Attributes>
		          <Map>
		            <entry key="secret" value="true"/>
		          </Map>
		        </Attributes>
		      </AttributeRequest>
		      <AttributeRequest name="description" op="Add" value="Created by IdentityIQ on 07/10/2013 17:21:09"/>
		      <AttributeRequest name="pwdLastSet" op="Add">
		        <Value>
		          <Boolean>true</Boolean>
		        </Value>
		      </AttributeRequest>
		      <AttributeRequest name="IIQDisabled" op="Add">
		        <Value>
		          <Boolean></Boolean>
		        </Value>
		      </AttributeRequest>
		      <AttributeRequest name="msNPAllowDialin" op="Add" value="Not Set"/>
		      <ProvisioningResult status="committed"/>
		    </AccountRequest>
		    <Attributes>
		      <Map>
		        <entry key="identityRequestId" value="0000000007"/>
		        <entry key="requester" value="spadmin"/>
		        <entry key="source" value="LCM"/>
		      </Map>
		    </Attributes>
		    <Requesters>
		      <Reference class="sailpoint.object.Identity" id="2c9090b33fca1d49013fca1d807400ce" name="spadmin"/>
		    </Requesters>
		  </ProvisioningPlan>
		  <QuestionHistory>
		    <Question shown="true">
		      <Field application="Active_Directory" displayName="Distinguished Name" name="Active_Directory:distinguishedName" priority="10" required="true" reviewRequired="true" template="Account" type="string" value="CN=Alice Ford2,OU=Brussels,OU=Europe,OU=Demo,DC=seri,DC=sailpointdemo,DC=com"/>
		    </Question>
		    <Question shown="true">
		      <Field application="Active_Directory" displayName="sAMAccountName" name="Active_Directory:sAMAccountName" priority="10" required="true" reviewRequired="true" template="Account" type="string" value="Alice.Ford2"/>
		    </Question>
		  </QuestionHistory>
		</ProvisioningProject>
      </value>
    </entry>
   </Map>
</Attributes>

*******************
* Version History *
*******************

10 Jul 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-EmailMgrPwd

