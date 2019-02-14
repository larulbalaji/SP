Link Status Account Report v.1.0.0
September 1, 2017

Contact: norman.aroesty@sailpoint.com
************************
* Library dependencies *
************************
None

*************
* Execution *
*************
Create a report from the template "Link Status Account Report" from: 
+
+Intelligence --> Reports --> Identity and User Reports
+
+Fill out the from the report, then run. 
************
* Background *
**************
Norm developed this report to show that when a user is terminated, this report serves as a way to prove to the auditors that all his accounts have been disabled.
It has a custom form that allows user to select a user and the application (although most times the report is run on a user). 
The report shows the link status for each user. 

**************************
* Potential Enhancements *
**************************
None

***************
* Limitations *
***************
Non
****************
* To Configure *
****************
Import the setup.xml. 
******************
* To Demonstrate *
******************
Optional: terminate a user, then run the report to show that the user's accounts are gone. 

1. Navigate to Intelligence --> Reports --> Identity and User Reports and find the "Link Status Account Report" report template. 
2. Follow the form and create a new report with a name you've chosen. 
3. Select the identity of the user you just terminated. You can choose to view whether the link to a specific application is still there by adding the name of the application on the right. 
Leaving it blank = selecting all applications. 
+4. Configure the report layout in the next screen and you can click either "Save and Preview" for a preview or the report or "Save and Execute" 
+for the report to run. 
*******************
* Version History *
*******************
September 2017
First release

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Usecase-Report-LinkStatus

Instructions for how to make a new tag for your contribution and create a "Testing Status" issue on Github: https://harbor.sailpoint.com/docs/DOC-21317

