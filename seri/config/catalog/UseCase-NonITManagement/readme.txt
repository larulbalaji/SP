UseCase-NonITManagement 0.1
08/16/2017 Initial Release

Contact: achim.reckeweg@sailpoint.com
************************
* Library dependencies *
************************


*************
* Execution *
*************


************
* Background *
**************
Sometimes prospects are asking how to manage Non IT Assets like smartphones or tablets.
Still the best answer to this is to integrate with an Asset Management System like
e.g. GLPI http://glpi-project.org/ or Snipe-IT https://snipeitapp.com/

But for simple usecases / demoes the approach taken here might be sufficient.
I use a simple database that holds the assets as well as the users. 
The accounts and assets are managed in simple tables and linked by a cross table.
Therefore an account can have as many assets as necessary.
      
**************************
* Potential Enhancements *
**************************
port the db creation scripts to more databases
might be a good idea for a plugin ;-)

***************
* Limitations *
***************
mysql only
No management and/or user interface supplied.
An Asset can be requested several times (which does not make sense at all, but hey we are demoing)

****************
* To Configure *
****************
1) Create the databases on the SERI AD Resource Image (default): source the AssetsDB_Create.mysql and AssetsDB_Data.mysql
2) Import the setup.xml or add
   UseCase-NonIT-Management to myDemo
3) Run the Aggregate AssetsDB-Accounts 
4) Run the Aggregate AssetsDB-Assests Tasks   

Afterwards everybody can request a non it assets by using the default Request Access Page

******************
* To Demonstrate *
******************
Have a look at Amanda.Ross, Catherine.Simmons, Jerry.Bennett, Adam.Kennedy they already own some devices. 
Use the Request Access page and search for iPhone or tablet and request it.

       

*******************
* Version History *
*******************
08/16/2017
Initial Release

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/UseCase-NonIT-Management

Instructions for how to make a new tag for your contribution and 
create a "Testing Status" issue on Github: https://harbor.sailpoint.com/docs/DOC-21317

