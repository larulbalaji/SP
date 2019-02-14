Use Case: SIQ-GDPR-DataClassification-Packs 01/11/2017
Contact: steve.kendall@sailpoint.com

**************
* Background *
**************
GDPR Policy/Rule packs for SecurityIQ Data Classification purposes.

*************
* Execution *
*************
1) Got to MS SQL Server with the Microsoft SQL Server Management Tool.
2) Change to the SecurityIQDB_SERI Database.
3) Import the SQL file in this folder called - 54_SIQMETZITZ-1559-GDPR.SQL


**************************
* Potential Enhancements *
**************************
1) Other GDPR relevant packs. Pack 2 is already under development. (Nov 1st 2017)

***************
* Limitations *
***************

****************
* To Configure *
****************
See Execution section

******************
* To Demonstrate *
******************
1) Using the SecurityIQ Business User Interface, click "Compliance"
2) You can use the "Filter" mechanism to find a Policy called - "General Data Protection Regulation (GDPR)"
3) You can drill into the policy and see the associated rules.
4) Through the Administrative User Interface for SecurityIQ you can run the Data Classification Index task.
5) An Automated task will subsequently run and apply the GDPR policy against the indexed data.
6) View the results in the Business User Interface by clicking the "Forensics" button and "Data Classifications"
7) Again, use the filter to show just files containing sensitive data based on the GDPR Policy.

*******************
* Version History *
*******************
01/11/2017 Initial Version

********************************
* Testing Status/ Known Issues *
********************************
