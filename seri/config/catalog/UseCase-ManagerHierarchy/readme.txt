Manager Hierarchy Use Cases
27 Jan 2017

Contact: patrick.sena@sailpoint.com

************
* Background *
**************
Provide the managers hierarchies in a custom identity attribute.
The manager hierarchy will look like : Top Manager / Middle Manager / Manager.
If there is any inconsistency in the management chain then the attribute will be:
	Warning, user is his own manager
	Warning, there is a loop in the hierarchy. <ManagerName> was already in the hierarchy: <Hierarchy>.


**************************
* Potential Enhancements *
**************************

***************
* Limitations *
***************
N/A

****************
* To Configure *
****************
Import setup.xml. 

******************
* To Demonstrate *
******************
Run an identity refresh task with Refresh identity attributes option checked.

*******************
* Version History *
*******************
27 Jan 2016
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-ManagerHierarchy
