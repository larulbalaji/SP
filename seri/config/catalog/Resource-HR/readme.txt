HR Datasource v 2.0.0
5 December 2013

Contact: kevin.james@sailpoint.com


**************
* Background *
**************
This catalog entry is the HR datasource for SERI. It comprises the HR Application and supporting artifacts. It relies on the Orange HRM system installed in the
SERI Resource VM. The supporting artifacts create some base workgroups, define some identity mappings and provide
password setting during aggregation.

****************
* To Configure *
****************

The system running the application server containing IIQ must have a "hosts" entry for seri.sailpointdemo.com that points to the SERI Resource VM.
The VM must be running at the time the "Setup Demo" task is run, in order to allow IIQ to collect employee/contractor data from the HR system.

*******************
* Version History *
*******************
5 March 2014
Added getObjectSQL statement to allow individual identity refreshes to work

5 December 2013
Updated to use the Orange HRM system as the source for HR data

1 July 2013
Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Resource-HR
