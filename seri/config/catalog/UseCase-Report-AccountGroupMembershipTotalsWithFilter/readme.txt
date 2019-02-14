    Account Group Membership Totals Report with Filters 1.0.0
    10 August 2017
    Contact: kumfei.poon@sailpoint.com

    ************************
    * Library dependencies *
    ************************
    No dependencies.

    *************
    * Execution *
    *************
    This replaces the OOTB "Account Group Membership Totals Report" report, and creates a new form "Account Group Membership Totals Report Form" that adds radio buttons for All/NonEmptyOnly/EmptyOnly selection.

    **************
    * Background *
    **************
    OOTB, there is no easy way for IdentityIQ users to generate a report on all Account Groups that do not have any members (empty groups), a common requirement in demos and POCs. (This functionality was available in IdentityIQ 5.x)
    This enables user to generate account group membership totals report for all account groups, only non-empty account groups, or only empty account groups.

    **************************
    * Potential Enhancements *
    **************************
    None at this time.

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
    Generate the Account Group Membership Totals Report, with appropriate selection of "All groups", "Non-empty groups only" or "Empty groups only". 


    *******************
    * Version History *
    *******************
    1.0 release on August 10, 2017
	
    **************************************************
    * SERILOG                                        *
    * List of all log4j settings that can be enabled *
    **************************************************
    None
    
    ********************************
    * Testing Status/ Known Issues *
    ********************************
    https://github.com/sailpoint/seri/labels/UseCase-Report-AccountGroupMembershipTotalsWithFilter
