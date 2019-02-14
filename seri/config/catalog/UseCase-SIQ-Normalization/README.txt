Use Case: SIQ-Normalization 17/11/2017
Contact: sebastien.lelarge@sailpoint.com

**************
* Background *
**************
Normalization of access rights on folders / access requests / automated provisioning
This use case covers 2 situations:
- only the results of normalization have to be showed
- the normalization configuration and execution must be done live in front of the customer

*************
* Execution *
*************
The SERI Image already contains a Normalized folder and nothing has to be executed if a demo of the normalization results is enough
The normalized folder is X:\Data\Departments\Storage

However, if the customer wants to see the normalization process running live, there are several steps that you need to follow (see the next sections)
In this case, the SERI image contains another folder preconfigured for normalization. Use X:\Data\Departments\Inventory in this case

Please note that the Inventory and Storage folders were configured with the exact same permissions
They can be used (e.g in the Permissions tree of the Web UI) to tell the before/after story around normalization

**************************
* Potential Enhancements *
**************************
- Create a script that will clean the AD groups after the normalization process has been executed, so that they don't accumulate over time. 
As of today, this operation needs to be done manually if you do the normalization live multiple times

***************
* Limitations *
***************

****************
* To Configure *
****************
Nothing to do 

******************
* To Demonstrate *
******************
1) Situation: show already normalized permissions
    - open the SIQ Web UI and navigate to Data/Departments/Inventory to explain the BEFORE situation (show the permissions)
    - Then, navigate to Data/Departments/Storage to show the permissions after normalization: AFTER situation -> access migrated to wbx-XXXXXXX groups
    - Optionally, you can create an access request to Storage to demo the automated provisioning. This request must be approved by Catherine.Simmons (owner of the folder)
    - Once approved, the grantee is added to the normalized AD groups

2) Situation: demo how to normalize (it is a good idea to make a snapshot of the VM. Another option is to rollback the normalization afterwards)
The Inventory folder is already configured for normalization. A fulfillment request is already existing in SIQ
Before starting the process, make sure that previously created (if you ran this scenario before) AD groups in the SecurityIQ,Groups,Demo,seri,sailpoitdemo,com OU are removed
To normalize the access to Inventory, do that:
    - connect to the Web UI to show the permissions BEFORE normalization
    - from the UI Client, go to System -> Access Fulfillment -> right click the fulfillment request for the Inventory folder and 'Fulfill now'
    - from the UI Client, synchronize Identities
    - from the UI client, analyze permissions for the Windows file Server application
    - once this is done, go back to the Web UI and show the permissions on the Inventory folder again

*******************
* Version History *
*******************
17/11/2017 Initial Version

********************************
* Testing Status/ Known Issues *
********************************