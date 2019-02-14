LogTree Plugin
January 2018

Contact: patrick.sena@sailpoint.com 

**************
* Background *
**************
logTree is parsing a log file and looking to a workflow trace. 
Then for each workflow it will create a tree with all the workflow nodes. 
You can expand the nodes and get access to the node logs. 
It make the workflow logs much easier to read. 
 

*************
* Execution *
*************

**************************
* Potential Enhancements *
**************************
Set up the log file path from the plugin configuration.
Allow parsing log files from outside the identityiq war file.


***************
* Limitations *
***************


****************
* To Configure *
****************
Install the plugin. 



******************
* To Demonstrate *
******************
You can access it via the Debug Page --> Settings (Gear Icon) and click on Log Tree. 
Or, you can access it here: 
[IIQHome]/plugins/pluginPage.jsf?pn=logTree 
e.g. (http://localhost:8080/identityiq/plugins/pluginPage.jsf?pn=logTree)

Set the relative path of the log file in the text field and click load.

*******************
* Version History *
*******************
January 2018
Initial Version

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Plugin-LogTree
