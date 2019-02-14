Disable Passthrough v1.0.0
21 Jun 2013

Contact: Kevin.James@sailpoint.com

**************
* Background *
**************
This is a simple rule that blanks the passthrough authentication list, and turns off forgotten password
and answer questions on login

This is for the times that you want a SERI demo environment set up, but don't have the AD VM running.

*************
* Execution *
*************

This rule is really designed to be run from the ant target in SERI:
ant disable-passthrough

However, you could also run it from iiq console:
> rule "SERI - Disable Passthrough"

or the debug screen.

*******************
* Version History *
*******************

21 Jun 2013
First release

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-DisablePassThrough
