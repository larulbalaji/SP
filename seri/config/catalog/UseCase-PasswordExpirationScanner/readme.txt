    PasswordExpirationScanner 2.0.0
	  5 June 2015
    Contact: jeff.bounds@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    Dependent upon the PasswordExpirationScanner.class.   If a normal SERI build, then this file is included in seri.jar.
    Otherwise you will need to copy the class file to WEB-INF/classes/sailpoint/seri/task

    *************
    * Execution *
    *************
    1. Create a new Task of type PasswordExpirationScanner.
    2. Set the Expiring Email Template value to "Password Expiration Notification-HTML"
    3. Set Application to Scan for Expiring Passwords to "Active Directory"
    4. Specify a day range.  (e.g. 15,10,5,1).   41 is a good value if you just set a users password
    5. Specify the rootSearchBase for AD (e.g. dc=seri,dc=sailpointdemo,dc=com)
    6. Optional - Enable test mode.  This will not send any emails and allows for checking of proper execution.
    7  Execute the task



    ************
    * Background *
    **************
	Customers ask for the ability to send reminder emails about password expiration.   Password and their expiration is most commonly stored in Active Directory/LDAP Directory.
	Rather than process each user in an IdentityRefresh task, the PasswordExpirationScanner will query Active Directory for a list of users whose passwords will expire within a specified range.
	Most LDAPs provide a virtual attribute for when passwords expire.   Active Directory does not.   Instead it has a Global MaxPwdAge attribute and a PwdLastSet attribute for each user.
    With a little math, we can determine whose password is about to expire.
	An email is then sent to those users in the day range with an IdentityIQ deeplink to change their password.
	Additonally, the PasswordScanner supports notifying all users whose passwords have expired.
  

    **************************
    * Potential Enhancements *
    **************************
    1.  Add support for SunONE/OpenDJ
    2.  Figure out how to filter an attribute in a TaskDefinition
    3.  Clean up the code to be more readable
    4.  Improve the Notification Email
    5.  Add AuditEvents for notifications.

    ***************
    * Limitations *
    ***************
    1.  Added limited support for multiple searchDNs.  Just grab the first one
    2.  Only supports Active Directory
    3.  Not tested against multiple domains or with a limited serviceaccount user
    

    ****************
    * To Configure *
    ****************
    1. Import all the artifacts via setup.xml
    2. Create a new Task of type PasswordExpirationScanner.
    3. Set the Expiring Email Template value to "Password Expiration Notification-HTML"
    4. Set Application to Scan for Expiring Passwords to "Active Directory"
    5. Specify a day range.  (e.g. 15,10,5,1).   41 is a good value if you just set a users password
    6. Specify the rootSearchBase for AD (e.g. dc=seri,dc=sailpointdemo,dc=com)
    7. Optional - Enable test mode.  This will not send any emails and allows for checking of proper execution.
    8. Execute the task


    ******************
    * To Demonstrate *
    ******************
    1. Change a user's password in AD
    2. Run the ScannerTask
    3. Show email and task results.


    *******************
    * Version History *
    *******************
    25 Sept 2014
    Version 1.5.0 contributed to SERI
    
    5 June 2015
    Version 2.0.0 Added ability to handle searchDNs



    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    *                                                *
    **************************************************

    sailpoint.seri.task.PasswordExpirationScanner
    
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-PasswordExpirationScanner