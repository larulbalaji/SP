TinyMFA - Multifactor Authentication for IdentityIQ 1.5.1
October, 2017

Contact: mario.ragucci@sailpoint.com, linda.wang@sailpoint.com

**************
* Background *
**************
https://github.com/ghmer/tiny-mfa-plugin/releases

Multifactor-Authentication as a plugin in IdentityIQ. 
This was created as part of the new MFA capabilities of SailPoint IdentityIQ 7.2. 
It will provide you with a basic interface for enrolling MFA with IdentityIQ.
Generates QRCodes and timebased otps. Also adds a new MFA workflow and a DynamicScope. 
To be used in pocs. Supports TOPT apps (Google-Authenticator, DUO Mobile, FreeOTP, ...) 
on the major Mobile Operating Systems (Android, IOS).


**************************
* Potential Enhancements *
**************************
- None


***************
* Limitations *
***************

Therefore, it has neither the ambitions nor the functionality to compete with other mfa-services out there. 
Please keep that in mind when you deploy this.
Still, it is a full totp service that could, in principle, also be used by other applications.

****************
* To Configure *
****************

1.Install the plugin. 
After you installed the plugin, a bunch of new objects are imported into IdentityIQ.
There are two new Capabilities
 - TinyMFA Activated Identity
 - View TinyMFA Plugin Activated Identity
 
2. Assign the "View TinyMFA Plugin Activated Identity" capabilities assigned, a new icon will appear on your main menubar. It looks like a tiny cell phone on the right hand corner.
From there, you have the option to have a look at "**Your QRCode**". You will find a QRCode that can be scanned with google-authenticator.

Then, assign the "TinyMFA Activated Identity" capability to that identity. But before you do that, you have to have scanned the QR code
with the authenticator, so that your authenticator has an entry for that identity. 

The "**TinyMFA Activated Identity**" is meant to be assigned to all identities that shall authenticate via google authenticator. 
Once assigned, the identity will be assigned a DynamicScope "**TinyMFA Authenticated**"

3. Last, you may review the login configuration of IdentityIQ. 

Go to **Global Settings** -> **Login Configuration** -> **MFA Configuration**. 
You will see a checkbox next to the label "**MFA TinyMFA**". 
If it is not checked, check it, then select the population "**TinyMFA Authenticated**" and add it to the list of "**MFA TinyMFA Populations**". 

Anyone belonging to that DynamicScope is now required to enter a totp token the next time they logon to the system.

For example, if you wanted to give Amanda Ross MFA, you'd first assign "View TinyMFA Plugin Activated Identity" capability to her. 
Then, you'd login as Amdanda and click on the little phone icon on the menubar, scan the QRCode, which creates an entry in your authenticator app for Amanda.
After that, you will need to assign "TinyMFA Activated Identity" capability to Amanda. 
Now, when you log in as Amanda Ross, you will be prompted to enter an authentication token. Enter the token generated from your authenticator app on your phone and 
you will be able to login. 
******************
* To Demonstrate *
******************
Sign in as you normally would and you will be prompted to input your authentication token.
Submit it and you are logged in. 


*******************
* Version History *
*******************
October 2017
v 1.4

December 2017 
v. 1.5.1
********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Plugin-TinyMFA
