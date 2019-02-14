Create Identity REST v1.0.0

Contact: brent.hauf@sailpoint.com 

**************
* Background *
**************
A common customer request is for us to show how REST web services can be invoked externally.  Many cases this is with regard to pushing new identity
creates into IIQ via REST.


****************
* To Configure *
****************
Import the workflow Workflow-CreateIdentityREST.xml.

Invoke the REST service sending the following JSON request.  The JSON payload below is an example.  You can add any identity attribute name and value
to the workflowArgs.  The workflow will create a plan and pass them to LCM Create and Update:

{"workflowArgs":
    {
        "firstname":"Jason",
        "lastname":"Day",
        "identityName":"Jason.Day",
        "email":"jason@sailpointdemo.com",
        "jobTitle":"Accounts Payable Analyst",
        "department": "Finance",
        "location": "Austin"
    }
}

A successful result will respond with:

<Response xmlns="http://localhost/identityiq/rest/workflows/CreateIdentity/launch">
   <attributes>
      <result>success</result>
   </attributes>
   <complete>false</complete>
   <errors null="true"/>
   <failure>false</failure>
   <metaData null="true"/>
   <requestID>402893814ff6d070014ff71d28f10072</requestID>
   <retry>false</retry>
   <retryWait>0</retryWait>
   <status null="true"/>
   <success>false</success>
   <warnings null="true"/>
</Response>


******************
* Limitations    *
******************
Multi-valued attributes have not been tested.

******************
* To Demonstrate *
******************
Utilize the SoapUI project included in here CreateIdentityREST-SOAPUI.txt.

1) Open the project in SOAPUI
2) Expand all nodes in the CreateIdentityREST project until you the "CreateIdentity" node under "POST - runWorkflow".
3) Open the request editor.
4) Update the JSON to provide your desired attribute and values
5) If needed change the user from spadmin/admin.
6) Execute the request.
7) Show the first item in the response document
   <attributes>
      <result>success</result>
   </attributes>
8) Login into IIQ and view the identity
9) View the Identity Request as well.

*******************
* Version History *
*******************

22 Sept 2015
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-CreateIdentityREST
