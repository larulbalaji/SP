LogTail Plugin
January 2018

Contact: patrick.sena@sailpoint.com 

**************
* Background *
**************
Used to tail a log file from the identityiq web UI.
Also adding a link to the debug page from the main IdentityIQ configuration menu.
*************
* Execution *
*************

**************************
* Potential Enhancements *
**************************
define the log path file in the plugin configuration
allow to tail files from outside of the war file.

***************
* Limitations *
***************


****************
* To Configure *
****************
1 Install the plugin (simple, no?)




******************
* To Demonstrate *
******************
You can access it via the Debug Page --> Settings (Gear Icon) and click on Log Tail. 
Or, you can access it here: 
[IIQHome]/plugins/pluginPage.jsf?pn=logTail 
e.g. (http://localhost:8080/identityiq/plugins/pluginPage.jsf?pn=logTail)

Set the relative path of the log file in the text field and click load.

*******************
* Version History *
*******************
January 2018
Initial Version

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Plugin-LogTail
