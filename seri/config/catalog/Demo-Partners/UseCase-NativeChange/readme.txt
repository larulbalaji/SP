Native Change Demo
20131028

Contact: hrv@sailpoint.com
************************
* Library dependencies *
************************


*************
* Execution *
*************

************
* Background *
**************
Limit Native Change to a single user (Catherine Simmons, London branch in AD) in the standard demo

**************************
* Potential Enhancements *
**************************
DataArchive needs to be set to "Assigned" in order for it not to disappear from the cube.... It demos nicer when the warning sign appears.
Sean Koontz will make changes to the standard cert that is performed as part of the demo setup to accommodate this in near future.

***************
* Limitations *
***************
Has been deliberately tied into a small scope of users (Population for just Catherine) and thus runs fast enough with the involved tasks.

****************
* To Configure *
****************
Run setup.xml and you should be done.
This is configured on the "partner" version of the resource image, which includes configured shortcuts for running the tasks and hiding that from the demo itself.

******************
* To Demonstrate *
******************

Show Catherine's access (Cube or in AD) and revoke on of her groups in AD.
Run <CTRL><ALT><6> on the partner demo (or launch the 6-spdemo-NativeChange task to aggregate and refresh.

Show workitem for her Manager (Amanda Ross) to approve or reject the native change

Talk about:
Desired versus Actual state.
IIQ can enforce the desired state (revoke the requests) or, when this is a valid change, make it part of the desired state (approve).

Using Native change is not only a great way to keep the desired state, but it also validates any ad-hoc changes directly to applications, that could be valid for business reasons.
For example regarding emergency actions. 

*******************
* Version History *
*******************
28 October 2013
Integration into SERI

