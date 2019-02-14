Excel File Parsing
March 2016

Contact: jeff.bounds@sailpoint.com
************************
* Library dependencies *
************************
Yes, Assumes SERI install (SERIConfiguration) which includes the Apache POI libraries.

*************
* Execution *
*************
During Aggregation or connectorDebug Iterate

**************
* Background *
**************
On occasion customers will ask us to import data from an Excel (XLSX) file not just a CSV.   The best course is to push for them to convert their spreadsheets
to simple parseable formats such as CSV.   If they are unable, then this example can be used as a framework (or to demo).   This is a simple XLS file 
that is run through a preiterate rule to read.


**************************
* Potential Enhancements *
**************************
None

***************
* Limitations *
***************
This only works with XLSX (Office Open XML Spreadsheet) file formats which is used by Excel 2007 and newer.   
The older Excel XLS are Microsoft proprietary and difficult to parse.   Draw the line here.   While Apache does have a POI library for older
Excel formats, it is a mess.

Additionally make sure that the Excel file is not using images for checkmarks (or anything strange like that).   
Images in Excel are not stored in the cell, instead they are overlays on top of the cell.   
This makes it very difficult to determine which cell the image is actually located in.

DISCOVER SCHEMA on the Configuration->Schema page will NOT work.  You will have to input the column names manually as shown in the example AppDef.


****************
* To Configure *
Import Setup.xml


******************
* To Demonstrate *
******************
Import setup.xml.
Run the aggregation task.
(Optional) show the file in Excel.

*******************
* Version History *
*******************
March 28, 2016 - Creation


*******************
* Logging         *
*******************
SERI.Rule.PreIterate-XLSXToCSV

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ExcelFileParsing