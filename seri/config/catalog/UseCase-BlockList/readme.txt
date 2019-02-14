Block List Preferences v1.0.0
4 October 2016

Author: marc.gamache@sailpoint.com

**************
* Background *
**************

This workflow sets the preferences of a user in the BlockList workgroup so that any work items are sent to the users in the AdminTeam workgroup.
Users in the BlockList are generally executives that should not receive approvals, certifications, or any other work items.

*************
* Limitations *
*************
A future enhancement might use an Identity attribute, such as ExecutiveAdmin, as the forwarding identity rather than the AdminTeam workgroup.
Individuals who are removed from the BlockList must have their preferences reset.

*************
* Execution *
*************
Import artifacts via setup.xml. 
Make sure that you added identities to both Blocklist and AdminTeam workgroups. 
The 'Block List Refresh' task can then be scheduled or run as needed.
The task will iterate through the individuals who are in the BlockList workgroup and set their preferences so that any work items are forwarded to the AdminTeam. 

*******************
* Version History *
*******************
4 October 2016: Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-BlockList