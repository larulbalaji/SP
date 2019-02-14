    Role Changes Propagation - 1.0
	30 December 2016
    Contact: sebastien.lelarge@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    The custom task 'Evaluate Role Change Impacts' found in the use case artifacts needs the sailpoint.seri.task.RoleChangeEvaluator Java class

    *************
    * Execution *
    *************
    This use case contains Tasks and Workflows.
    They can be triggered via the UI

    ************
    * Background *
    **************
	Idea is to have a reference implementation of the 'Propagate Role Changes' feature that came with 6.4 so that we can demo it
	easily in front of prospects. The focus has been put on the impacts of a role modification in order to inform about the consequences
	of such changes in terms of impacted roles and users.
	The use case includes:
	   - Update of system configuration to enable the feature, set a custom workflow (see below) for the propagate changes task
	   - A custom task that returns the number of impacted users for RoleChangeEvent objects that are already in the system queue.
	       - Task template 'Propagate Role Changes - Evaluate Impacts' that uses the custom executor
	       - Task instance named 'SERI - Evaluate Role Changes Impacts' using this template
	   - An instance of the 'Propagate Role Changes' task named 'Process Role Changes'. This task is used to do the provisioning of changes
	   - A custom workflow 'SERI Role Changes Propagation' that is launched by the Propagate Role Changes task.
	       This workflow is started for each identity that is impacted. It executes the provisioning plan found in the change event and sends
	       an email notification to the end user
       - EmailTemplate 'SERI - Notify Role Changes Propagation'
       - A custom workflow 'Role Modeler - Impact Analysis - Changes Propagation' that is derived from the stock workflow. In case of changes
            that need to be propagated, it adds an approval step for the role owner so that he can validate the impacts of the changes (mainly
            number of impacted users for each impacted role)
	        

    **************************
    * Potential Enhancements *
    **************************
    - Make the approval step more generic in the new Role Modeler workflow
    - start the propagation of changes when the impacts are approved. This would require to patch the existing Executor because it does not support
        execution for a given role (all events in the queue are processed)

    ***************
    * Limitations *
    ***************
    None

    ****************
    * To Configure *
    ****************
    Import all the artifacts via setup.xml
    Be aware that it will change the configuration to use the 'Role Modeler - Impact Analysis - Changes Propagation' workflow 
    for role creation, update and delete operations
    
    ******************
    * To Demonstrate *
    ******************
    - Change a role so that entitlements will be changed for end users
        - Change requirements on a Business Role
        - Change profiles on an IT Role
        - Change inheritance, etc...
    - Use the 'Submit' button rather than the 'Submit with impact analysis' button. The latter will still work and the new approval step will be executed after
        the OOTB Impact Analysis. That just creates 2 approvals and does not demo very well. The submit button will only triggers the new approval
    - You will see that the role has a pending workItem (appears in red)
    - Option 1: you can execute the 'Propagate Role Changes - Evaluate impacts' task and show the results.
    - Option 2: you can open the workitem that was created (for the role owner) and see more details about the change
    - You can either Approve or Reject the changes
        - Approve -> RoleChangeEvent objects are created, you can then execute the 'Propagate Role Changes' task and show the provisioning results.
            An email has also been sent to all impacted end users
        - Reject: nothing else happens
    - In both case, the action is tracked in the Audit (Advanced Analytics search will show the approval result)


    *******************
    * Version History *
    *******************
	1.0 release on December 30, 2016
	
    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************

    SERI.Workflow.RoleChangesNotification
    SERI.Workflow.RoleModeler
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-RoleChangesPropagation