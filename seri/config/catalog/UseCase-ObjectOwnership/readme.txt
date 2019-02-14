    ObjectOwnership 1.0.0
	  3 June 2015
    Contact: jeff.bounds@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependencies. 

    *************
    * Execution *
    *************
    This is a workflow subprocess that can be included in any termination workflow.

    ************
    * Background *
    **************
	  During a termination event, it is possible that the user terminated is defined as an Owner for an IdentityIQ object
	  such as application, bundle, or managedattribute.   The user could also have been designated as a "sponsor" for a service account.
	  Current termination workflows leave the owner or sponsor value set to the terminated user.   Customers often ask how SailPoint
	  would handle such an event.   This workflow subprocess will present a form to an administrator that allows them to see the current ownership
	  and then set the new owner. 
	
    **************************
    * Potential Enhancements *
    **************************
    Allow setting of individual ownership rather than blocks

    ***************
    * Limitations *
    ***************
    Currently the form only allows setting of one new owner for all objects of the same type.  For example, if the user owns 3 applications
    one identity is defined as the new owner for all 3 applications.   This doesn't allow one app to be set to identity1 and the other 2 set to identity2.
    I believed the form to be too cluttered to allow that.

    ****************
    * To Configure *
    ****************
    Import all the artifacts via setup.xml
    Modify an existing termination workflow to include Workflow-ObjectOwship as a subprocess.   The workflow assumes identityName is passed in.
    FormOwner can be set to a different identity.   Default value is spadmin.


    ******************
    * To Demonstrate *
    ******************
    Terminate a user, a form will be presented to spadmin if the terminated user has object ownership 

    *******************
    * Version History *
    *******************
    3 June 2015
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

https://github.com/sailpoint/seri/labels/UseCase-ObjectOwnership