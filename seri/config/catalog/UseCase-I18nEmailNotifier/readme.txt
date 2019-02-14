I18n Email Notifier v1.0.0
20 August 2015

Contact: kevin.james@sailpoint.com
*************
* Execution *
*************
Replacement for standard product functionality. No execution required. 

************
* Background *
**************
IdentityIQ currently has no way to send different emails out based on the user's language. This use case attempts
to bridge that gap until the product supports it natively. 

**************************
* Potential Enhancements *
**************************
Other language translations

***************
* Limitations *
***************
It will not translate any of the strings that come from IdentityIQ, for example the name of a certification. It will also not translate
the workitem name, since this is done as part of the UI through sensing the browser language. 

****************
* To Configure *
****************
Import the setup.xml for the use case. This will add the 'locale' Identity Attribute, add it to the
View Identity page, change the class used for email notifications and import all the new email templates.

You will need to manually set the value for 'locale' on the identities you want to use in your demo. 

Right now, this use case supports French and German. You can set the value for "locale" by 
going to Define --> Select the identity --> Edit --> Type "fr" for French and "de" for German. --> Save. 


******************
* To Demonstrate *
******************
Once you have configured it, you just need to run through your normal demo. Emails will now appear in the language of the actors in
the use case.
One thing to be aware of is that you want to make sure that the emails your demo will be firing off DO, in fact, come in
either French or German versions. 

*******************
* Version History *
*******************
20 August 2015
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-I18nEmailNotifier
