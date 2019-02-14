    EditAccountAttribute 2.0.0
	10 June 2014
    Contact: jeff.bounds@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependecies.

    *************
    * Execution *
    *************
    This creates a quicklink called EditAccountAttributes.   Click the quicklink, select the user, then select their account you wish to edit.


    ************
    * Background *
    **************
	With SunIDM migrations, we've been getting more requests to do Application edits.  For example, a user just wants to change an attribute in Active Directory
	The workflow makes use of the identityModel to display user attributes and provision any changes.   The identityModel claims to have the ability to distinguish between a users accounts
	ie. identityModel.links[name=AD] but I couldn't get this to work with something like identityModel.links[id=blah] to distinguish which account I wanted.
	Instead I was able to create my own identityModel.    Using sailpoint.api.IdentityService we can get the link that we want to edit.   Then using sailpoint.transformer.LinkTransformer
	we can convert the Link object to a linkModel hashmap.   The linkModel is passed to the form for display.   Any changes made on the form to the linkModel are then converted to a ProvisioningPlan.
	LinkTransformer.mapToPlan(linkModel,null)

    **************************
    * Potential Enhancements *
    **************************
    None at this time

    ***************
    * Limitations *
    ***************
    None known right now

    ****************
    * To Configure *
    ****************
    Import all the artifacts via setup.xml


    ******************
    * To Demonstrate *
    ******************
    Click the Edit Account Attributes quicklink on the dashboard.   A form with a list of the user's applications is displayed.   Select the application you wish to edit.   Then select the application account you wish to edit.   The next form displays the currently set account attributes.     You can then select "Show Empty Attributes" to display all attributes in the schema, even if the user does not have a value set.    Once a change has been made, the normal workflow process for account changes is processed.


    *******************
    * Version History *
    *******************
    17 Feb 2014
    Version 1.0.0 contributed to SERI

    25 Feb 2014
    Version 1.0.1 Changed serilogs to include step names.   Fixed typo in Readme.txt

    10 June 2014
    Version 2.0.0
    Fixed setup.xml
    Modified workflow to handle users with multiple accounts
    Removed ability to edit the Identity Attribute
    Removed "Edit Attributes".  Every item except Identity Attribute is editable now
    Added some help keys

    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************

    SERI.Workflow.EditAccountAttributes.SelectApplication
    SERI.Workflow.EditAccountAttributes.SelectApplicationLink
    SERI.Workflow.EditAccountAttributes.GetUserId
    SERI.Workflow.EditAccountAttributes.GetIdentityLinkModel
    SERI.Workflow.EditAccountAttributes.BuildAttributeForm
    SERI.Workflow.EditAccountAttributes.EditAttributes
    SERI.Workflow.EditAccountAttributes.BuildProvisioningPlan
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-EditAccountAttributes
