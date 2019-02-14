Org Importer 1.0
25 April 2016

Contact: kevin.james@sailpoint.com
************************
* Library dependencies *
************************
This depends on the IPF being installed as well as the Org Importer plugin

*************
* Execution *
*************
This is built into SERI so it will automatically run as part of the 'Setup Demo' task

************
* Background *
**************
We are often asked (especially by SAP customers) about our ability to support an approval structure
outside our standard IIQ-centric manager, owner, security officer structure, and have something more
org-specific (i.e. the approver is specified on an organizational object that the recipient is a member
of). This use case provides that functionality.

**************************
* Potential Enhancements *
**************************
Anything you might want to add in the future (or someone else might want to add)

***************
* Limitations *
***************
Anything that someone using it might need to be aware of, for example “doesn’t work with manual workitems” or “attribute XYZ is hardcoded”

****************
* To Configure *
****************
IPF, Org Structure plugin, and import of org structure are all part of the standard SERI.
To show different approvers you will need to change the approval scheme in the workflow
'LCM Provisioning - Org Structure Edition'

******************
* To Demonstrate *
******************
See videos on Learncore:  https://app.learncore.com/dashboard/108211

*******************
* Version History *
*******************
26 Apr 2016
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-OrgImporter
