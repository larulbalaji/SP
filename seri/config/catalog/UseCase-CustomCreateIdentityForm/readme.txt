Create Identity Provisioning Policy Form v1.1.0

Contact: norman.aroesty@sailpoint.com

**************
* Background *
**************
A common customer request is for us to show a tricked out create identity form.  
This form and the attached rule is intended to do that for the SERI demo


**************************
* Potential Enhancements *
**************************
Trick out the form to support contractors.  I would have done that and it is easy to do
except in a provisioning policy the hidden attribute map does not work, see bug 20465.  
Until that is fixed we can not have a fancy form with hidden sections for employee and contractor.


***************
* Limitations *
***************
does not support hidden fields see bug 20465

****************
* To Configure *
****************
This use case is part of the standard demo.

******************
* To Demonstrate *
******************
Click on the Create Identity Link and see the custom form

*******************
* Version History *
*******************

25 April 2014
First release

13 February 2018 - v1.1.0 - S. Lelarge
Added a wrapper workflow for the LCM C&U in order to support 'correlated' and 'correlatedOverridden' flags as before 7.0
Added the 'includeHiddenFields' flag set to true in the creation form

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/UseCase-CustomCreateIdentityForm
