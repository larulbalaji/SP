    ProbableManagedAttributeOwnerRecertification 1.0.0
	25 March 2015
    Contact: norman.aroesty@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependecies.

    *************
    * Execution *
    *************
    This creates a quicklink called Owner Validation Ent..   Click the quicklink, select a manager
    The manager then is presented with an approval to revalidate the Managed Attributes he owns.
    If he changes ownership, then ownership in IIQ and AD change.   


    ************
    * Background *
    **************
	Idea is to have a stable of owner recertifications:
	   - Is this my subordinate
	   - Do I own this managed attribute
	   - etc.
	This is the first attempt at a process that can be demo'ed to our prospects.  

    **************************
    * Potential Enhancements *
    **************************
    Currently the ownership is not updated until the perform maintenance task is run.
    Ensure it works with workgroups :).  I have not done workgroup testing

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
    Click the Owner Validation Ent. quicklink on the dashboard.   
    Have the manager revalidate that he still owns the managed entitlements.  


    *******************
    * Version History *
    *******************
	1.0 release on March 25, 2015
	
    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************

    SERI.Workflow.ProbableManagedAttributeOwner
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ProbableOwnerManagedAttribute
