UseCase-LCMAttachments v 1.0.0
28 Aug 2017 Initial release stolen from ssdv4

Contact: achim.reckeweg@sailpoint.com, paul.wheeler@sailpoint.com

************************
* Library dependencies *
************************
sailpoint.services.standard.lcmattachments.FileUploadServlet 


*************
* Execution *
*************
--

**************
* Background *
**************
Out of the box, IdentityIQ does not provide the ability to attach documents to access requests. 
This is a commonly-requested feature, as customers sometimes want requesters to justify their 
need for a requested role or entitlement or to provide additional information. 
The Access Request Attachments solution addresses this requirement by permitting the requester 
to upload files as attachments associated with the request so that they can be viewed by the 
approver to aid in making an approval decision or as background information.
The solution requires a shared storage area for attachments, and a web server pointing to 
this location which allows viewing of the files. 
In some cases this could use storage on one of the IdentityIQ application servers, with the 
application server itself providing the web server functionality, or it could be a dedicated 
shared storage area and web server, which is the preferred option.



*****************
* Prerequisites *
*****************
- A shared filesystem must be available that can be accessed by all the IdentityIQ servers, 
  so the service account used by IdentityIQ must have read/write/delete access to this area, 
  but end users should not have any filesystem access to it.
- A web server must be available for viewing/downloading the file attachments, and this 
  must point to the shared file storage so that files stored there can be accessed using a URL. 
  If no other web server is available, an application server used by IdentityIQ may be used, 
  with the shared storage located inside the folder structure served by the application server.

For the SERI Resource Image the webserver is reused. A new directory has been created in the SERI Amsterdam SERI Resource Image: 
C:/xampp/htdocs/attachments
The webserver base url in this case is http://<servername or ipadress/attachments
**NOTE***If you are using /attachments, then you must run this use case within the Resource Image with the version of SERI
running inside of the Resource Image. If you have deployed SERI with this use case outside of the Resource Image, 
then your SERI server may not be able to find the /attachments directory. 
***************
* Limitations *
***************
- Due to the fact that the solution relies on the use of the old non-responsive forms, it must force the use of the old
  technology enabling the support for custom renderers.
  Unfortunately with this approach the approval items are NOT visible on the bell menu and the dashboards.
  You have to open your work items from the menu to see the waiting approval.
- This solution should not be thought of as a secure Content Management solution. 
  Anyone who knows the correct URL will be able to access files via the web server, 
  although URLs will be very difficult to guess, as the original filename will be prefixed 
  with a 13-character numeric string (a Java timestamp representation). 
  The web server should therefore not allow a root view of all the files available in the 
  configured attachment folder.
- Periodic file housekeeping, such as removal of files pertaining to old requests, will 
  be a manual process. While the solution provides the ability to limit the maximum size 
  of an attachment and the maximum number of attachments per request, the monitoring of 
  available disk space will need external tools. 
  Deletion of files will prevent those attachments from being viewable in IdentityIQ.
- The solution requires replacement of the default LCM Provisioning and Provisioning 
  Approval Subprocess workflows with customized versions. 
  A Versions that will work with 7.x is supplied, but older versions will require modifications 
  to the default workflows. 
- The solution uses old-style Ext-JS forms, not IdentityIQ 7.x responsive forms. 
  It is possible that future versions of IdentityIQ may remove support for these, in which 
  case the solution will no longer work in its current form.
  
****************
* Enhancements *
****************
It should be relatively easy to add an attribute to the roles that enables the enhancement for 
only some roles and fade it out for the rest. 

****************
* To Configure *
****************
Set with withLCMAttachments to true in build.properties
This switch enables the modifications of web.xml, adding the servlet for uploads.
additionally it adds the necessary config properties to default.target.properties

The client side of the configuration is enabled by import the setup.xml or adding
the usecase to the myDemo property of build.properties

******************
* To Demonstrate *
******************
Access Requests
Once the solution is deployed and the “LCM Provisioning with Attachments” workflow is configured 
as the access request workflow, when an access request is submitted a screen will be presented to upload attachments.
A file can be selected using the “Browse” button and a description must be entered. 
On clicking the “Add File” button, the file will be uploaded to the shared location and 
will appear in the list of uploaded files.
The uploaded files can be viewed by clicking in the “View” link or removed from the list 
(and the shared storage) by clicking the red ‘no entry’ icon.
If the requester attempts to upload a file larger than the configured maximum they will be 
presented with a message and the file will not be uploaded: "File is larger then the permitted size"
Similarly, if the file extension of an attachment is not in the configured list of permitted 
extensions an error message will be presented.
The request must be completed by clicking the “Complete” button. 
It is not mandatory to add any attachments, but the Complete button must still be clicked 
to move the request to the approval stage.

Approvals
When an access request has been submitted and approvals are enabled in the workflow, the 
configured approver(s) will receive a work item which includes a list of attachments and 
the ability to view each one.
The approver may use the information in the attachments to help with making an approval 
decision or for background information.

*******************
* Version History *
*******************
28 Aug 2017 
Initial release stolen from ssdv4

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/UseCase-LCMAttachments

