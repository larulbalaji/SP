    Access Request with Scopes - 1.0
	17 February 2017
    Contact: sebastien.lelarge@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    None

    *************
    * Execution *
    *************
    This use case follows the standard access request process

    ************
    * Background *
    **************
	We see more and more requests from prospects like "Do you support assignment scopes ?", "I want to request role X on perimeter Y", etc...
	In some cases, we can meet the requirement with our role model by leveraging optional roles (for perimeters) but most of the time it becomes
	complex.
	IIQ doesn't support it yet. We cannot assign an entitlement or role to a user along with a scope that applies to this relation
	
	This Use case is an example implementation of how we can support "role + scopes" in IIQ while keeping other things working (certifications,
	detection of it roles, etc...) 
	It is based on :
	   - a schema extension for business roles and entitlements. 2 new attributes (scopeRequired and scopeMulti) are added to flag entitlements/roles
	   - 2 roles / entitlements that support scopes and that have a template which will be used to provision the right entitlements (with the instanciated value for the scope)
	       the '%scope%' placeholder must be used wherever a value substitution is needed
	       ex: the name for a template entitlement could be : Privilege_%scope%. instances of it will be Privilege_East, Privilege_North, etc...
	       ex: scoped roles will be automatically duplicated as "<original role name> - <scope name>"
	   - a custom workflow that is called at the end of the request process.
	       This workflow analyses the content of the request. If scoped entitlements or roles are requested, it displays a form that will collect scopes
	       It then patches the provisioning plan so that the actual role / entitlements are put in the request (instead of the template values)
	       If these roles or entitlements do not exist, the workflow creates them as non requestable objects. They are just copies of the template, where
	       %scope% is replaced by the appropriate scope
	       At the end, the custom workflow calls LCM Provisioning with original args and the updated plan.
	       Substitutions for roles work with our SERI role model (BR -> IT Role -> Entitlements)
	   - Scopes proposed by the dynamic form are native Scope objects in IIQ. From a demo perspective, this is good as the prospect actually sees "Scopes" in the config page
	       Adding a new scope will be reflected immediately in the form.
       - In case the user already has been assigned the same template in the past, the form retrieves the scope values that are currently assigned
            - if the access right only expects 1 scope, the old access is removed, and the new one is assigned
            - if the access right expects multiple scopes, the form manages changes : it removes scopes that were unselected, grants scopes that were added

    **************************
    * Potential Enhancements *
    **************************
    - Integrate with the Role Change Propagation use case to make sure changes made on a template role are reflected on users who
        own the role on a specific perimeter
    - could be extend to other applications using the same %scope% mechanism
    - owner for the new role and entitlement is spadmin. Might be changed to someone else if you want to chain with the approval process demo

    ***************
    * Limitations *
    ***************
    None I can see for demo purposes
    POCs might require some rework if the role model is different

    ****************
    * To Configure *
    ****************
    1. Import all the artifacts via setup.xml
    Be aware that it will change the configuration to use the 'LCM Provisioning - Scopes' workflow 
    for access requests
    It also changes your Bundle and ManagedAttribute extended attributes
    
    2. Copy groups_def.csv and create_AD_groups.ps1 files on your Resource image
    3. Execute the PS script in order to create groups (RegionDirector_xxxxx) found in the CSV file
    4. Aggregate AD groups
    
    ******************
    * To Demonstrate *
    ******************
The use case can be demoed with 2 roles
Roles are: Regional Director (expects 1 scope), Sales Engineer (supports multi scopes)
Roles give entitlements with the same name via an IT role

    - Start an access request for a user
    - Pick whatever business role or entitlement that is NOT scoped
    - Pick a role that expects a scope
    - Submit the request
    - A form is popped up and asks for a scope for the Region Director or Sales Engineer access (note that the other item in the request is not displayed)
    - Select your scope(s)
    - Submit the request
    - You can show:
        - track my request and see that the correct item + scope was requested
        - the provisining occurs with the right values
    - This way of doing things guarantees that certifications (and especially remediation) will work correctly (prospects generally want to certifiy the association between a role and a scope)

    *******************
    * Version History *
    *******************
	1.0 release on 17 February 2017
	
    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************

    SERI.Workflow.LcmWithScopes
    
********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LCMProvisioning-RequestWithScopes
