DB Performance Utils
Dec 02, 2016

Contact: jeff.bounds@sailpoint.com
************************
* Library dependencies *
************************
None

*************
* Execution *
*************
IIQDB Performance Test
	Run rule "IIQDB Performance Test"

Periodic Oracle Maintenance
	Run task "Periodic Oracle Maintenance"

************
* Background *
**************
These utilities are sometimes necessary during POCs.   
The IIQDB Performance Test is used to determine latency or potential performance issues between the IIQ App Server and Database.   
	DB-Performance-Notes.pdf and README-DB-Performance-Test provide more detail on how to read the output
Periodic Oracle Maintenance is a commonly used ProServ util to "fix" some of the Oracle and MSSQL issues around indexing.   
	During a POC if you create and delete a large number of user or links it is recommended to run this task.

**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************

****************
* To Configure *
****************
Run setup.xml and you should be done.

******************
* To Demonstrate *
******************
This is NOT for demo purposes!
These utilities are primarily to be used during a POC when there are database issues

*******************
* Version History *
*******************
Dec 02, 2016 - Creation of UseCase

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-DBPerformanceUtils

