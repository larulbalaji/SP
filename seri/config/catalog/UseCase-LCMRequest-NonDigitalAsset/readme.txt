Non-Digital Asset Request. V1
2/17/2014

Contact: nick.wellinghoff@sailpoint.com

****************
MANIFEST
****************

sailpoint.seri.catalog.nondigitalasset.NonDigitalAssetJDBC.java

web\css\sailpoint\shared-icons.css
web\images\icons\desk.png
web\images\icons\key.png
web\images\icons\laptop.png
web\images\icons\pencil.png
web\images\icons\phone.png
web\images\icons\Snow16.png
web\images\icons\software.png

************************
* Library dependencies *
************************
None

*************
* Execution *
*************
None

************
* Background *
**************
A use case the shows how one could setup SailPoint to request non digital things like phones, computers etc.  Provide basic service desk functionality. 
**************************
* Potential Enhancements *
**************************
None

***************
* Limitations *
***************
This solution is really to placate any questions on IF our product can do this. 

****************
* To Configure *
****************
import setup.xml
setup a table on the "test" database using the provided table sql in nondigital.sql
copy the css and image content to get custom icon types


******************
* To Demonstrate *
******************
Run the Full-Text index task to rebuild role request descriptions
Request a non-digital assset role from Access Request
Notice provisiong rule write request data to table
Reaggregate nondigital assett and the asset should appear on the cube


*******************
* Version History *
*******************

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-LCMRequest-NonDigitalAsset
