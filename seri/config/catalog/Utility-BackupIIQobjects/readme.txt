BackupIIQObjects.py V1
Save all IIQ objects that have been created/modified since a certain date
02/12/2014

Contact: geoffroy.hy@sailpoint.com

************************
* Library dependencies *
************************
Python utility (it has been tested on Python 2.7.8), which should be installed by default on your MAC

*************
* Execution *
*************
External Python utility (it has been tested on Python 2.7.8)

**************
* Background *
**************
Utility that will save all IIQ objects that have been created/modified since a certain date.
- Useful at the end of a POC prep to save only the objects that have been created/modified during the preparation
- Useful in a POC to save every day your progress, and ease the roll back, in case... just in case...

**************************
* Potential Enhancements *
**************************
None

***************
* Limitations *
***************
None

****************
* To Configure *
****************
1/ To use it, you need Python (it has been tested on 2.7.8)
2/ Copy the Python utility (and rename) to a directory
3/ Export latest IIQ configuration (WARNING: NOT clean... for example "export ALL.xml", but not "export ALLclean.xml -clean"... the utility needs the attributs modified and/or created)
4/ Copy the IIQ export file to the same directory as the Python utility
5/ Run the Python utility, with the following arguments:

python BackupIIQObjects.py File Date Time Directory
(ex. python BackupIIQObjects.py ALL.xml 20141123 000000 Customer)

  File        IIQ export file: the result of the IIQ Export command in console mode, for example "export all.xml" (without -clean).
  Date        Date: save objects created or modified since this date (YYYYmmDD)
  Time        Time: save objects created or modified since this time (HHmmSS)
  Directory   Directory: directory where to store objects (if it doesn't exist it will be created).

To reload those objects, copy the directory (ex. Customer) to WEB-INF/config and System Setup > Import from File
- You can reload per class of objects. All objects are stored in a directory, named as the object name (ex. Customer/ObjectName/Date-Time.xml)
- You can reload all objects, using the file in the root directory (ex. Customer/Date-Time.xml)

******************
* To Demonstrate *
******************
Nothing to demonstrate

*******************
* Version History *
*******************
02/12/2014 First Release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-BackupIIQobjects