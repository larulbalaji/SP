TRAKK-WS 1.0.0
October 2017
 
Contact: norman.aroesty@sailpoint.com, linda.wang@sailpoint.com

************
* Background *
**************
TRAKK-WS is a web service application. It's based on the TRAKK time tracking application used in the training classes. 
While TRAKK is used for employees, TRAKK-WS is used for only contractors. 
This web service assumes the trakkws database (trakk-ws.ddl) can be connected via the following: 
String URL = "jdbc:mysql://seri.sailpointdemo.com/trakkws?useServerPrepStmts=true&tinyInt1isBit=true&useUnicode=true&characterEncoding=utf8";
String USER = "root";
String PASS = "sailpoint”;


The TRAKK-WS web application uses the TRAKKWS database on the AD Resource Image and is deployed there as well, at: 
http://ad-resource.seri.sailpointdemo.com:9080/trakkws


****************
* To Configure *
****************
This resource is a part of the SERI Standard Demo. 
Therefore, the necessary tasks to configure this resource are included in the "Setup Demo" Task. 

The web application is deployed and running in the AD Resource Image, so there should be no further configuration.
However, if you would like to modify the application and/or deploy it elsewhere, you can do so by modifying the source code included in /trakkws. 
After modifying it, you will have to rebuild it with maven. The instructions can be found here: 
http://tutorial-academy.com/restful-webservice-jersey-maven/
After rebuilding it, you will need to move web.xml into WEB-INF/ manually. 

If you choose to deploy the application elsewhere,  you can simply copy the trakkws directory from C:\xampp\tomcat\webapps in the AD Resource Image
and deploy it wherever you'd like. After doing that, you have to change the Base Url under the Configuration --> Settings tab. 

You can test to see if the web app and the rest service are running by opening up the app in the browser by going to the test page
(http://ad-resource.seri.sailpointdemo.com:9080/trakkws/rest/test) and you will see the following message: 
Hello there! Testing success!
Simple RESTful Get Service is running ... 
Ping @ [Current Date and Time]

The code for the TRAKK-WS is included in TRAKKWS-Source.zip, if you want to modify and rebuild it. 

  
*************
* Execution *
*************
The following are implemented: 
-Test
-Create Account
-Account Aggregation and
-Get Object

You will be able to aggregate the accounts and create new accounts either by creating a new TRAKK-WS account directly or
Manage User Access and selecting one of the three TRAKK-WS entitlements (approve/input/reject). 


*******************
* Version History *
*******************
 
October 2017
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Resource-TRAKK-WS
