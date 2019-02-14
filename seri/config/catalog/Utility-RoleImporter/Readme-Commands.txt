Role Importer:

Note: 
Blank Lines and Lines beginning with a "#" will be ignored
Make sure to add roles in the right sequence - parent roles must exist

"ADD ROLE":
  // Adds a role of a given type
  //    Role Type           - Type of role.  Needs to match a valid Role Type
  //    Role Name           - Name of role.
  //    Role Display Name   - Display name of role (optional)
  //    Role Description    - Description of role.
  //    Role Owner          - Owner of role.  Needs to match a valid Identity
  //    Role Parent         - Parent of role.  Needs to match an existing role name
  //    Profile Application - only for 'Entitlement' roles
  //    Profile Attributes  - only for 'Entitlement' roles
  //    Profile Entitlement - only for 'Entitlement' roles  
  // -------------------------------------------------  
  //      op, type, name, description, owner, parent
  //      Add Role,Organization,Role Name,Role Description,spadmin,Role Parent
  // -------------------------------------------------  
Add Role,organizational,Finance Technical Roles,,Bundles of technical access assigned to Finance and Accounting business roles,Lori.Ferguson,Finance & Accounting,
Add Role,it,User Basic,,Role encompassing the most basic access needed within the company.,Dennis.Barnes,,
Add Role,business,All Users,,Role that grants access all users should have,spadmin,,


"DELETE ROLE":
  // Deletes a role
  // -----------------------------
  //  op, name
  //  Delete Role,Role Name
  // -----------------------------

"ADD INHERITANCE":
  //  Add Inheritance - Adds a parent to an existing role
  //  ------------------------------------------------
  //    Role Name         - Role to add a parent to
  //    Parent Role Name  - Parent role name
  //  ------------------------------------------------
  // op,Role Name,Parent Role Name
Add Inheritance,Financial Planning & Analysis Manager,Data Analyst,,,,
    
"ADD PERMITTED":
    //  Add Permitted - Adds a permitted role to an existing role
    //  ----------------------------------------------
    //    Role Name            - Role to add permitted roles to
    //    Permitted Role Name  - Permitted role name
    //  ----------------------------------------------
Add Permitted,Procurement Analyst,Inventory Analyst Access,,,,
    
"ADD REQUIRED":
    //  Add Required - Adds a required role to an existing role
    //  ----------------------------------------------
    //    Role Name            - Role to add required roles to
    //    Permitted Role Name  - Required role name
    //  ----------------------------------------------
Add Required,All Users,User Basic,,,,
    
    
"ADD MATCHLIST":
    //  Add MATCHLIST - Adds a matchlist to an existing business role
    //  --------------------------------------------
    //    Role Name            - Name of an existing role
    //    Match List Format    - Type of Matchlist.  Currently only
    //                            IdentityMatchList is supported.
    //                            TODO: Add LDAPFilter and Filter to formats
    //    Match List Options
    //         IdnetityMatchList  - Accepts 3 inputs
    //             AND_OP         - true - ANDs attribute/values together
    //                            - false - ORs attribute/values together
    //             Attributes     - ordered csv list of attributes to match on
    //             Values         - ordered csv list of values to match on
    //  --------------------------------------------
Add MatchList,Benefits Clerk,IDENTITYMATCHLIST,or,"jobTitle,jobTitle,jobTitle","""Benefits Clerk"",""Benefits Manager"",""Compensation & Benefits Manager""",
    
"ADD PROFILE":
    //  Add Profile - Adds a profile to an existing role
    //  --------------------------------------------
    //    Role Name
    //    Profile Description
    //    Profile Application
    //    Profile Filter
    //  --------------------------------------------
Add Profile,User Basic,,Active Directory,"memberOf.containsAll({""CN=All_Users,OU=Groups,OU=Demo,DC=seri,DC=sailpointdemo,DC=com""})"

    
"ADD METADATA":
  //  Add Metadata - Adds metadata to an existing role
  //  Make sure that the attribute is defined and made available for the role.
  //  ------------------------------------------------
  //    Role Name
  //    Meta-attribute Name
  //    Meta-attribute Value
  //    [Optional] datatype. Defaults to string, can be boolean or integer
  //  ------------------------------------------------
Add Metadata,DNS Administrator Access,adminRole,true

"ADD DESCRIPTION":
  //  Add Description - Adds localized desc to existing role
  //  ------------------------------------------------------
  //    Role Name
  //    Description
  //    Locale
  //  ------------------------------------------------
    
"ADD ASSIGNMENTRULE":
  //  Add AssignmentRule - Adds a rule as the assignment logic
  //  ------------------------------------------------------
  //    Role Name
  //    Rule Name
  //  ------------------------------------------------
Add AssignmentRule,All Users,Active and non-orphaned Users
    

"ADD ACCOUNTSELECTOR":
  //  Add Account Selector - adds an account selector rule
  //  ------------------------------------------------------
  //    Role Name
  //    Rule Name
  //    Optional: Application
  //  ------------------------------------------------
  // op,Role Name,Rule Name,Application
  // Add Account Selector,....
Add AccountSelector,DNS Administrator Access,Leverage Provisioning Plan,Active Directory  

"ADD ROLEASSIGNMENT":
  //  Add Role Assignment - adds a role assignment to identity
  //  --------------------------------------------------------
  //    Role Name
  //    Identity
  //    Optional: The entity responsible for the assignment
  // 
  //    Example:
  //
  //    Add RoleAssignment,data analyst,Amanda.Ross
  //    Add RoleAssignment,Benefits Clerk,Amanda.Ross,Bulk Import
  //  --------------------------------------------------------

"ADD SCOPE":
  //  Add Scope - Creates a scope and moves it under a parent
  //  ------------------------------------------------------
  //    Scope Name
  //    parentScope Name
  //  ------------------------------------------------
  // op,Scope Name,parentScope Name
  // Add Scope,Benefits Clerk,Benefits

"ADD SCOPEi ASSIGNMENT":
  //  Add ScopeAssignment - Assigns a role to a scope
  //  ------------------------------------------------------
  //    Role Name
  //    Scope Name
  //  ------------------------------------------------
  // op,Role Name,Scope Name
  // Add Scope,Benefits Clerk,Benefits
  
