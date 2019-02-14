Usecase-LifecycleEvent-CertificationOnNativeChange
20180101

Contact: achim.reckeweg@sailpoint.com

************************
* Library dependencies *
************************
No dependency

*************
* Execution *
*************


************
* Background *
**************
Prospects wants to see how to monitor/handle native changes on applications.
In theory this one is pretty easy as the bits and pieces are already supplied by the ootb product.
Unfortunately there is a bug that duplicates every line item in the generated approval.
Issue: "IIQETN-6265 Native Change Event has duplicate entries" created for it. 
Had to hack the original Workflow.
This usecase kicks off a "Native Change Approval" (cert campaign) for an individual identity if a change is detected
I use AD and AD Groups for showcasing 

**************************
* Potential Enhancements *
**************************


***************
* Limitations *
***************


****************
* To Configure *
****************
Import the setup.xml file


******************
* To Demonstrate *
******************
Show the assignments of an arbitrary user.
Add/Remove an ADGroup using Active Directory User and Computers.
Run an Aggregation of AD
Run "Process Events" Task
Login as the manager of the above chosen identity and show the approval.


*******************
* Version History *
*******************

01 January 2018
Initial version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Usecase-LifecycleEvent-CertificationOnNativeChange

