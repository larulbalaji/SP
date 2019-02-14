Transfer Process. V3
27 Feb 2017

Contact: norman.aroestyd@sailpoint.com (for cert / provisioning functionality)
         kevin.james@sailpoint.com (for SIQ functionality)

************************
* Library dependencies *
************************
LCM Workflow Library
SP Util Rule Library (RuleLibrary-Util.xml)
Approval Library


*************
* Execution *
*************
These artifacts are meant to be executed using a standard LCE transfer event.  In this case
the workflow does the provisioning not the refresh event.  

To execute, I run my refresh task with:
	Refresh identity attributes 		ON
	Refresh manager status				ON
	Refresh assign and detected roles	ON
	process events 						ON
	provision assignments 				OFF

These settings are already implemented in the "Process Events" task as a part of the Standard Demo. 

************
* Background *
**************
Yet another transfer process.  Assumes both jobTitle and manager have changed.  
Which in my mind is a transfer, since both manager and job responsibilities have changed.
     - jobTitle is used to drive role assignments 
     - Manager change drives notification and work item assignments

What I like about this process is that it creates and acts upon three provisioning plans 
all of which are calculated in the getTransferAccountRequest routine (see RuleLibrary-Util.xml)

	/*
	 * We return a hashmap with the following three values
	 * planAdd - PP for the roles to add based on the new transfer matching criteria
	 * planRemove - PP of the items to delete - used in a deffered task  
	 * planChgSrc - PP need to modify source of the roles to Task.  
	 * 				this way, they are not deleted if a refresh rule runs.  They will
	 *		 		be deleted by the defered task in N days
	*/
		
In this transfer routine we do the following:
1) Notify previous manager, new manager, and user of the transfer.  In the previous    
   manager notification we tell him that an access request has been generated to take
   action if he so chooses, otherwise access will be automatically taken away in 28 days.  
   Email shown below

2) Adds new roles via a call to LCM Provisioning with planAdd

3) Generate previous Manager Certification.  In my demo, I never act upon this. IN fact,   
   at the end of the demo, I always clean it up with the console command of 
   delete certificationGroup Prev*
   
3) Sunsets the previously assigned roles and entitlements in 28 days (using role and
   entitlement sunrise).  Call LCM Provisoning with planRemove
   
4) sets up a task to delete the previously assigned roles and entitlements in 2 minutes.  
   After all we can not wait 28 days to complete the demo/poc. Use planRemove in this 
   task.  
   
5) modify source of the roles to Task.  this way, they are not deleted if a refresh rule 
   runs.  They will be deleted by the defered task in N days

6) If turned on with the 'SIQEnabled' variable, go query SIQ for any resources owned by the moving user
   (email address is used as the value to look up in SIQ). The sponsor (default: old manager) will be
   sent a workitem to decide whether each owned resource will remain owned by the moving user. If not,
   ownership is transferred to the sponsor.


**************************
* Potential Enhancements *
**************************
Loads - depends on how the business process of a transfer in a POC/demo
Fire a resource owner election for each resource that should no longer be owned by the moving user 

***************
* Limitations *
***************
This is designed for the manager transfer of a newly created identity. 
This use case currently does not accommodate the manager transfer of existing identities that have the follow accounts: 
- ERP
- Mainframe

If you are using this on an identity that has the above accounts, all you have to do is to 
change the optimisticProvisioning from false to true in IntegrationConfig: 
      <entry key="optimisticProvisioning" value="true"/>
      
For the SIQ resource owner reassignment part, please only use Gladys Vasquez. 
In the latest Resource Image VM, she is currently the owner of 3 resources: 
project_Valhalla
project_Vertex
project_Vermillion

When Gladys is transferred, her manager Kathryn Gardner will receive the work item and decide whether Gladys will continue to own those resources. 
Any 'declined' resources revert to ownership by Kathryn Gardner. 


****************
* To Configure *
****************
This does role assignments via a matcher routine in RuleLibrary-Util.xml.  
The matcher routine looks for a business role assignments  
This demo assumes both jobTitle and manager change.  
This is now a part of the standard demo.

OOTB these are configured to point at the resource VM (seri.sailpointdemo.com) so you'll need to have your DNS or
hosts file configured correctly. To configure the SIQ side, you'll need to change the following variables to
connect to the SIQ Database.
SIQEnabled (set to true to use SIQ functionality)
SIQDBServer
SIQDBName
SIQDBUser
SIQDBPassword 

If you wish to change who is the sponsor (approver) for the SIQ part, change what sponsor is set to in the 'Start' step
of the workflow


******************
* To Demonstrate *
******************
Configure as above.
1) Set up a new user with a jobTitle. He should be provisioned a set of 
   roles and entitlements.  
2) Change jobTitle and manager.  We change both just like a real transfer. 
You can do this either in OrangeHRM or directly on the cube. 
3) Run the "Process HR Changes" task. 
4) Look at identity and see role changes in 28 days.  See task to take it away in 2 
minutes.  Explain that the task is for demo purposes only
5) Look at entitlements.  See both sets. Look at resources, see both sets.
6) After two minutes, re-look at identity.  Old entitlements have been taken away.

The following currently can only be done with Gladys Vasquez (see the Limitations section above). 
7) Login to SIQ. Look for resources owner by the moving user
8) Login to IIQ as sponsor. See the new workitem. Indicate that some resources should
   not still be owned by the mover
9) Go back to SIQ. See that the resources have been reassigned to the sponsor  


*******************
* Version History *
*******************
xx xxx 2013
Integration into SERI

xx xxx 2013
First release

05/09/2016
Fixed setup.xml
Modified RuleLibrary-Util.xml to look for All Business roles rather than the Demo Hierarchy.
Changes to EmailTemplates.
	Modified Colors to match SailPoint.com.  Not perfect, but I hated that green color.
	Removed "Dear", seemed too informal.
	Fixed bug in New Manager email that addressed email rather than name.
Incorporated Norms changes to simplify the workflow

08/08/2016
Talked to Jeff and implemented back some of the changes I made.  
Also, included latest RuleLibrary-Util.xml in this branch 

08/26/2016
Added Process HR Changes task with new Process Events Transfer Processor 3.

27 Feb 2017
Added functionality to change resource owner in SIQ

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LifecycleEvent-TransferProcessor3