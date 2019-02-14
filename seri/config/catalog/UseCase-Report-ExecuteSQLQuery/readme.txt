SQL Query Executor Report v1.1
28/09/2016

Contact: sebastien.lelarge@sailpoint.com
************************
* Library dependencies *
************************
This report uses the following Java datasource class : sailpoint.seri.reporting.datasource.SqlQueryDataSource

*************
* Execution *
*************
Run from Intelligence->Reports->Generic reports
Click the 'SQL Query Executor'

************
* Background *
**************
This report allows us to return the results from an SQL Query that is specified in the report form


**************************
* Potential Enhancements *
**************************
In a production usage, mind to add some protection mechanism to prevent execution of potentially dangerous SQL statements (UPDATE, INSERT, DROP, etc...)

***************
* Limitations *
***************
Regarding the SQL Query, the following limitations apply:
- column aliases MUST be enclosed within quotes, e.g SELECT count(*) AS 'Total' FROM xxxx

****************
* To Configure *
****************
import via setup.xml

******************
* To Demonstrate *
******************
Select the report from Intelligence->Reports

The report has a configuration step where you can specify the SQL Query to execute
The SQL statement is executed to determine which columns are found in the output and those are made available in the report layout config.
The column names are derived from this SQL Query but are not included in the output by default. You need to select the columns in the Layout configuration step.
By default, a 'Line number' column is included, you can remove it from the output.  

*******************
* Version History *
*******************
28 Sep 2016 - S. Lelarge
Bug fixes, added cleaner error handling and label support for column names
Added support for 'Select *' queries
Added a few examples of queries created by Patrick Sena

16 Jun 2016 - S. Lelarge
First Release


********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-Report-ExecuteSQLQuery
