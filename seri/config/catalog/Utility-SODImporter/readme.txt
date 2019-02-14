SODImporter v1.0.0
27 Feb 2013

Contact: Kevin.James@sailpoint.com

**************
* Background *
**************
This task definition allows the importing of SOD definitions from an Excel spreadsheet or CSV file, either in Matrix or list format.


************************
* Library dependencies *
************************
poi-3.8-20120326.jar
poi-ooxml-3.8-20120326.jar
poi-ooxml-schemas-3.8-20120326.jar
xmlbeans-2.3.0.jar

REMOVE poi-2.5.1-final-20040804.jar if it is there.

*************
* Execution *
*************

There are two ways of running the SOD Matrix Importer - the SOD List Importer is currently only exposed through IdentityIQ.

From the command line:
----------------------

java -cp <classpath> sailpoint.seri.sodimporter.XMLPolicyBuilder <options>

Mandatory options:
-i <input file>
-active <active flags>
-o <output file>
-nr <name row>
-nc <name column>
-dr <data start row>
-dc <data start column>
-ds <data size>

Mandatory for type XLS:
-s <Excel sheet name>

Optional:
-ls <column|row>
-l <label column/row number>
-cr <IT|Business>

 type defaults to CSV
 
The command line version will generate an XML file that is importable with IIQ Console/System Setup.

From IdentityIQ
---------------
with IIQ Console or System Setup, import TaskDef-SODMatrixImporter.xml and/or TaskDef-SODListImporter.xml
under Monitor->tasks,create a new Import SOD Definitions task
enter the parameters, then save and execute


*******************
* Version History *
*******************
21 Jun 2013
Updated to work with relative paths (files deployed inside IdentityIQ e.g under WEB-INF/config)

24 Apr 2013
Matrix/List version imported to SERI

27 Feb 2013
Integration into SERI

08 Feb 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-SODImporter
