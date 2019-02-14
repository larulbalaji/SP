Use Case Claim Account
04/02/2015

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************
Customers, particularly in education health care verticals, require the ability for an identity to claim the Active Directory accounts. This process
occurs post identity creation and birth right provisioning.  Essentially the requirement is to have the identity provide PII information such 
as employeeId/student ID, Month and Day of birth, and last name.  This is deemed sufficient to authenticate the identity.  Then authentication questions,
phone, number and possibly email are updated/gathered along with setting the password.

This process can also be utilized to force authentication questions and/or phone number to be updated.  This insures that SMS reset can be used
regardless if the phone number has already been captured.

*************
* Execution *
*************
Once configured the self registration link on the login page will show the text "Claim Accounts".  User clicks on the "Claim Accounts" link
and is presented with a form to enter ID, Month and Day of birth, and last name.  Then they are required to provide 2 authentication questions.
The last step provides a user with a corporate policy they must accept and the ability to enter their phone number and possibly update
their email address.  On this last form they also provide they password which must comply the the IIQ password policy.

When complete the authentication questions are saved, Active Directory password is set, and phone and email attributes are updated.

By default identity accounts can only be claimed once.  See the configure below on how to remove this restriction.

Use advanced analytics audit search with values of "action=PasswordChange" and you will see audit events for both password policy acceptance and 
claimed account.

**************************
* Potential Enhancements *
**************************
-Enhance the Authentication question dynamically render the correct number of configured authentication questions.  Currently it must be 2 questions.
-Ability to handle identities with multiple Active Directory accounts.
-Insures that the onboarding emails provide the employeeId to the manager  


***************
* Limitations *
***************
-if the identity has multiple Active Directory accounts the AD password could be set for the wrong account.  However, the phone and 
authentication questions would then be set.  The user can reset their password via the OOTB mechanism.

****************
* To Configure *
****************
1) Copy the files provided iiqCustom.properties and iiqMessages_en.properties to WEB-INF/classes/sailpoint/web/messages and restart your server.
2) Edit the values in the properties from step one to change the verbage if desired.  The entries in iiqCustom.properties are self explanatory.
The entries to change in iiqMessages_en.properties are below.

# SSR
ui_ssr_login=Login
ui_ssr_success=Your account claiming process is complete.

For different non-english languages the entries will have to be updated for the desired language and placed in the location defined in step 1.
3) Import the setup.xml class.  Insure that the employeeId and birthDate fields are searchable.

4) If your need to use different initial inputs the "Claim Account Identify" form will need to be modified.  The only requirement for the flow
to function is that the "Claim Account Identify" provides a valid "name" field.  This value is updated in the model that is past back to the
workflow.  Which drives the remainder of the process.

5) The default behavior is for accounts to allow accounts to be claimed once.  To allow unlimited number of times
an identity can claim accounts make the following change in the workflow.  Insure the initializer is a script returning
the java keyword true or false.  This insures the variable will be initialized as a boolean.

<Variable initializer="script: return false;" name="claimOnce"/>




******************
* To Demonstrate *
******************
See Execution section above

*******************
* Version History *
*******************

4/2/2016
Initial Version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ClaimAccount

