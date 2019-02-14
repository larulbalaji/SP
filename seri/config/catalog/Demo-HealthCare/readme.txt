Demo-HealthCare Version v1.0.0
11 Sept 2015

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************

These artifacts are utilized to add heatlhcare vertical specific demonstration capabilities on base SERI.

**************
* Setup      *
**************
The setup steps documented here assume that you have a fully functioning base SERI configuration with the setup-demo task executed successfully.

There are 2 portions of the setup that are necessary.  The first is to add data to the resource image for the EPIC application and identities into Orange HRM.
The second is to import additional artifacts into your IIQ instance

1) EPIC Data:  This step creates a database for the EPIC resource located in config/catalog/Resource-EPIC
- Follow the steps outlined in the readme for the EPIC resource (config/catalog/Resource-EPIC/readme.txt)


2) Orange HRM Data: This steps adds additional identities for the healthcare vertical to the Orange HRM system on the resource image.
-Follow the steps outlined in /seri/config/demo/data/orangehrm/HealthCare/readme.txt.

3) Import IIQ Artifacts: This step imports that artifacts needed to utilize the HealthCare data imported in 1 and 2 above.  This will import artifacts located in /seri/config/catalog/Demo-HealthCare
and /seri/config/catalog/Resource-EPIC artifacts.  The artifacts include the EPIC application, health care specific roles, and tasks setup the health care demo.
Execute the command "ant import-healthcare"

4) Setup Health Care Demo:  This executes the "Setup Demo - Health Care" task imported as part of the steps above.   Below are the high level steps executed by this task.

    -Aggregate HR data
    -Imports Health Care Roles
    -Provisions Health Care entitlements to AD
    -Refresh entitlements
    -Generate Manager and Application Owner certifications

Execute the command "ant setup-demo-healthcare"


***************
* Demo Script *
***************

This demonstration script is provided as a high level outline of a potential demonstration flow.  Demonstration presenters will need to create and thoroughly practice their own script.

Reporting Structure
-Jordan Sullivan: All Nurses report to Jordan
-Bob Kelso: All doctors report to Bob.  Jordan Sullivan also reports to Bob.

1) Onboard New Employee: When adding a new user you will see additions to Orange HRM in the following areas:
-Job Title: Doctor, Doctor Chief, Nurse Charge, Nurse Staff, Nurse Manager
-Sub Unit: A sub unit of Health Care and under Health Care Nursing, Doctors, Students

Identities will provisioned birthright access based on department.  Refer to "Role Structure" below for details.

2) Role Structure: All roles are contained within the "Healthcare Provider Organization"
BirthRight Provisioning occurs as follows
Department = Doctors
AD Campus Access
AD User Basic
AD VPN

Department = Nursing
AD Campus Access
AD User basic

Department = Students
AD Campus Access


3) EPIC access request: You can request access to EPIC entitlements

4) Manger Certification: Manager certification is auto generated from Bob Kelso down in the organization. This create's manager certifications for Bob Kelso and Jordan Sullivan.

5) EPIC Application owner certification


*******************
* Version History *
*******************
11 Sept 2015
Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Healthcare-Demo
