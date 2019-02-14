PAM Demo 1.1
15/7/2017

Contact: mike.kiser@sailpoint.com
************************
* Library dependencies *
************************
Requires IIQ 7.1p2+

*************
* Execution *
*************
(see configuration section)


************
* Background *
**************
Allows for the demonstration of the new Privileged Account Management Module


**************************
* Potential Enhancements *
**************************
Auto installation in default SERI

***************
* Limitations *
***************


****************
* To Configure *
****************
1) import 'init-pam.xml' from the console (identityiq/WEB-INF/config)
2) install plugin: scim-pam-bridge-plugin.zip
3) install plugin: pamDemo.1.0.zip
4) Configure SCIM Pam Bridge Plugin: Set 'JDBC Application Name' attribute to 'JDBC PAM Application'
5) Run 'setupPAM' task
6) Shock and Awe

******************
* To Demonstrate *
******************
- Show PAM GUI either via via "Manage Access" --> Privileged Account Management, 
you can also add a Quicklink to the Home Screen. You may need to login/out for the Quicklink to show up.  

- Add a user to a PAM related group via normal access request
    - Basic PAM Access/ PAM Admins
    
- Add a user directly to a container via the PAM GUI interface
    - Once you do that, you have to rerun the "setupPAM" task in order to re-aggregate the perms on the containers.

- Create a cert to show revocation of container membership
    - Then rerun "setupPAM" task. 


*******************
* Version History *
*******************
15 Jul 2017 - Mike Kiser
First Release

November 2017
Version 1.1 - Updated to work with IIQ 7.2 

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-PrivilegedAccountManagement

