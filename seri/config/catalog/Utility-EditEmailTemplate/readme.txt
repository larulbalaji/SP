Utility-EditEmailTemplate v 1.1.0
08/23/2017 Update to ssdv4 supplied version

Contact: achim.reckeweg@sailpoint.com, kevin.james@sailpoint.com
************************
* Library dependencies *
************************


*************
* Execution *
*************
Use the QuickLink Admin Tools/Edit Email Template

************
* Background *
**************
The Email Template Editor provides an administrative UI for editing IdentityIQ email 
templates. Plain text and simple HTML templates are supported. 
A WYSIWYG interface is provided for HTML templates.

In order to use the Email Template Editor a user must be a member of the “IIQ Admins” workgroup, 
or have the SystemAdministrator capability. This provides a QuickLink category labelled 
“Admin Tools” in the UI, and a QuickLink under this called “Edit Email Template”.
On clicking the QuickLink the user will be presented with a “Select Email Template” form. 
A dropdown labelled “Template” allows the user to create a new email template or select 
an existing one for editing. Clicking “Next” brings up the form that is used to enter or 
edit the template details.
The form has a “Template Type” selector to switch between editing the template as plain text or HTML.
 
When editing an existing template, the type of template will be detected and the Template 
Type selector will be set appropriately. For an existing template the Template Type can be 
toggled between Plain Text and HTML to change the type of template; however, if changing 
from HTML to Plain Text the HTML tags will appear in the Message Body field and will need 
to be removed. If changing the Template Type after editing the Message Body, the changes 
made will not be reflected in the Message Body for the new type.
When you have completed editing the fields, save the email template and it will be ready for use.

By default the Email Template Editor does not display controls for editing colors for 
fonts and text highlighting. These options are available and can be enabled by setting 
the workflow variable “enableColorControls” to “true” in the “Edit Email Templates” workflow. 
However, there is currently an issue that causes the color palette grid displayed when 
selecting these options to have selectable blank squares instead of colored squares. 
These do work and can be used to modify colors but there may be an element of trial 
and error when selecting the colors.
 
      
**************************
* Potential Enhancements *
**************************
--

***************
* Limitations *
***************
When editing existing HTML templates it is advisable to make a copy of the template before 
editing as the editor may remove more complex HTML or modify other elements.
- Known product issues in IdentityIQ 7.0 and 7.1 cause problems with line endings 
  (to be fixed in 7.0p6 and 7.1p1). This causes line feeds to be removed when displaying an 
  existing template, and if saved the line feeds will be removed from the template. 
  A warning message will appear in the form with affected IdentityIQ patch levels.
- The tool uses old-style Ext-JS forms, not IdentityIQ 7.x responsive forms. 
  It is possible that future versions of IdentityIQ may remove support for these, 
  in which case the tool will no longer work.


****************
* To Configure *
****************
After setting up standard SERI
Import the setup.xml or add Utility-EditEmailTemplate to myDemo


******************
* To Demonstrate *
******************
As member of the "IIQ Admins" workgroup or with an identity that holds the 
"SystemAdministrator" capability. Select the Quicklink "Admin Tools/Edit Email Template"
       

*******************
* Version History *
*******************
08/23/2017 
Update to ssdv4 supplied version
11/20/2013
Initial Release

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Utility-EditEmailTemplate


