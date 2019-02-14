Microsoft Azure Active Directory Integration
03/27/2017

Contact: mike.kiser@sailpoint.com, geoff.schwartz@sailpoint.com

**************
* Background *
**************
Recently SailPoint announced a partnership with Microsoft.  The value proposition for this joint partnership
    - Comprehensive approach to managing access, underpinned by a strong governance foundation, improves security and reduces risk
    - Synchronized delivery of access throughout a user's lifecycle improves productivity


*************
* Execution *
*************

**************************
* Potential Enhancements *
**************************
- None


***************
* Limitations *
***************
- SAML configuration assumes that IdentityIQ is running on the following URL:  https://iiq-server.sailpointdemo.com:8443/identityiq
- The demo requires network access.
- Azure Premium is a paid SAAS Application.  Verify the subscription is still active.  Contact Mike Kiser if the subscription is no longer valid.
- This is a shared environment,so there could be issues with multiple people using the environment at 1 time.  Please make sure you check out the calendar resources.
- Microsoft SSO is enabled, so creating multiple Chrome profiles is required to effective demo this integration.
- Since this is a shared environment, there could be multiple people using the environment at the same time. Make sure that you reserve the environment before using it by sending a calendar invitation to #IdentityIQAzureEnv-1. 
If the environment is being used by someone else, your invitation will be rejected.


****************
* To Configure *
****************
1 Install the plugin (simple, no?)
2 Edit the Synchronize Azure Integration Task to set "application" to the Azure Active Directory - IIQ Integration application in IIQ




******************
* To Demonstrate *
******************
To demo SSO, use the following URL: myapps.microsoft.com to sign into Azure. 
When you are demonstrating the SSO, the username's domain name is Identityiqpoc.onmicrosoft.com. 
For example, Amanda's Ross's credentials are amanda.ross@identityiqpoc.onmicrosoft.com. 
The default password is : ADpass1$.
1. SSO from Azure to IIQ expects SSL and that IIQ is running on iiq-server.sailpointdemo.com.
  Follow instructions from this page: https://harbor.sailpoint.com/docs/DOC-19809
2. Install MS Azure AD Integration plugin 2.0.0
3. Because SAML is configured between Azure and IIQ, I would recommend creating 4 Chrome People (Admin, Jerry Bennett, Amanda Ross and Guest).
4. Run the following tasks:
    a. AA – Azure Active Directory
    b. AGA – Azure Active Directory Groups
    c. Synchronize Azure Integration
    d. Full Text Index Refresh
    e. Detect Policy Violations
    f. Refresh Identity Cube
5. Make sure SSO is enable for Microsoft Azure (Global Settings -> Login Configuration ->SSO Configuation)
6. Rename Roles appropriately for your demo (defaults to SSO in role name)
7. Create a Manager Cert


*******************
* Version History *
*******************
3/24/2017
Initial Version

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/Plugin-MSAzureIntegration
