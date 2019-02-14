EPIC Version v1.0.0
10 July 2013

Contact: brent.hauf@sailpoint.com

**************
* Background *
**************

Provides the simulated EPIC application for the healthcare demonstration.

**************
* Setup      *
**************
The EPIC database needs to created on the SERI resource image.  To do so utilize the EPIC-createMySQLTables.sql to create the tables.

Copy the file EPIC-createMySQLTables.sql to the resource image to a directory.  For example: C:\SailPoint\SERI\data\EPIC. Open a command prompt and change to the directory.
Run the following command from that directory.

mysql -uroot -psailpoint < EPIC-createMySQLTables.sql

*******************
* Version History *
*******************
10 Sept 2015
Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Resource-EPIC

