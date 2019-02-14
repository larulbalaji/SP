Email Template Changer
1 November 2013

Contact: hrv@sailpoint.com

************************
* Library dependencies *
************************
None 

*************
* Execution *
*************
It's a separate shell script to quickly change a bunch of email templates.
Usage: templateChanger.sh <config file>

Copies email templates from the Templates folder, substitutes the variables according to the used configuration file 
and puts them in the target folder (TEST, configurable) 
If test="TRUE" has been specified in the config file, it will then open firefox and display the raw files for a preview. 
HTML will be translated, but obviously, you also see the raw stuff surrounding the CDATA tags.....

**************
* Background *
**************
I hated to modify email template HTML code on a POC.... So here you go
 

**************************
* Potential Enhancements *
**************************
More templates, more substitution inside the templates, like the small tables inside the main content of some of them.
Adding this into SERI itself, so we always "ship" with better templates.


***************
* Limitations *
***************
Shell script has been tested on a Mac. Sed command seems a bit different than on Linux, so your mileage may vary.


****************
* To Configure *
****************
Copy the Example.conf file and change the color coding and fonts to your liking.
Use Example.jpg as a guide as to what config item changes what section of the template.

Set test, tetsDir and testURL parameters to quickly view the results in you environment (local apache or tomcat folder)


******************
* To Demonstrate *
******************
Not a real demo this....


*******************
* Version History *
*******************
1 November 2013
First release (0.3)

5 November 2013 (0.4)
More templates, 6.2 color schemes (IdentityIQ6.2 A B and C configurations)

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-EmailTemplateChanger

