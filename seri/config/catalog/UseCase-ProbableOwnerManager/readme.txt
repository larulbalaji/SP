    ProbableOwnerManager ReCertification 1.0.0
	25 March 2015
    Contact: norman.aroesty@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependencies.

    *************
    * Execution *
    *************
    This creates a quicklink called Owner Validation Mgr..   Click on the quicklink, select a manager. 
    The manager then is presented with an approval to revalidate his subordinates.
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
    None

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
    Have the manager revalidate his subordinate relationship.  


    *******************
    * Version History *
    *******************
	1.0 release on March 25, 2015
	
    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************

    SERI.Workflow.ProbableManagerWorkflow

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ProbableOwnerManager