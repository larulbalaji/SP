SecurityIQ V1.0.0
10 Oct 2017
 
Contact: linda.wang@sailpoint.com, jeff.bounds@sailpoint.com

************
* Background *
**************
With IIQ 7.2, there is now a SecurityIQ application definition.   This replaces the ActiveDirectory unstructured
targets configuration and tasks.
In addition, A RuleLibrary was added to facilitate a single location where SIQ configuration can be set.
The RuleLibrary queries for the application named "SecurityIQ" and gets the connection information.  
There is no longer a need to change any Rules/Workflows to point to a different location if you use different
SIQ endpoints.  (e.g. Rule-GroupRefresh-CreateUpdateManagedAttribute or Worflow-LifecycleEvent-TransferProcessor-IdentityInc)

***************
* To Configure *
****************
This resource is a part of the SERI Standard Demo. 
Therefore, the necessary tasks to configure this resource are included in the "Setup Demo" Task. 
If you wish to only import SecurityIQ related objects for a POC.  Import the Resource-SecurityIQ/setup.xml 

*******************
* Version History *
*******************
 
10 Oct 2017
First release

********************************
* Testing Status/ Known Issues *
********************************

