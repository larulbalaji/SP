Use Case: SIQ-ServiceNowIncidentAlerts 
Date:     Nov 09, 2017
Contact: jeff.bounds@sailpoint.com

**************
* Background *
**************
This use case provides the ability to create a ServiceNow Incident for an Activity.  This specific example is used for a File System activity, but can be modified for any type.
When SIQ detects any file in  //ad-resource/Data/Departments/Audit/ being read, it will create a ServiceNow Incident on http://ven01309.service-now.com/


*************
* Execution *
*************
SERI:
	1.  Open any file in //ad-resource/Data/Departments/Audit
	2.  Wait for activity monitor to detect and react
	3.  Login to http://ven01309.service-now.com/  spadmin/Sailp0!nt.   Look at Incidents.
	
NON-SERI/POC/CUSTOM-DEMO
	1.  Copy/Edit siqServiceNowIncident ps1 and bat files from seri/resources/SIQ/WindowsFileServer.   siqServiceNowIncident.ps1 has the servicenow url, user, & password.
	2.  Configure Activity Monitoring->Responses->Manage Response Configurations.
		a.  Create a UserExit with Name of "ServiceNow Incident" and a Filename that points to the siqServiceNowIncident.bat
	3.  Configure Activity Monitoring->Responses->Manage Responses
		a.  Create a UserExit with Name of "ServiceNow".
		b.  Message should be "%File Server|User Name%" "%File Server|Action Type%" "%File Server|Path%" "%File Server|Object Name%"    Note: Each param is enclosed in a double quote, and a space between.
		c.  Configuration should be the Response Configuration created in step 2.
	4.  Create a Policy Rule of type Alert and add the appropriate requirements.   On the last page of configuration, select the ServiceNow Incident as the Associate Response
	

**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************

****************
* To Configure *
****************
See Execution section

******************
* To Demonstrate *
******************
See Execution section

*******************
* Version History *
*******************
Nov 09, 2017 Initial Version

********************************