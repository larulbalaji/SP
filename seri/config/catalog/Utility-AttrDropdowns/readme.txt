Attribute Dropdowns v1.0.0
26 March 2015

Author: marc.gamache@sailpoint.com

**************
* Background *
**************

This is a simple workaround that builds FieldValue rules from the Identity Mappings (ObjectConfig -> Identity)
Import the 'setup.xml' file and a Task named 'AttrDropdowns' will be available.
Fix the TaskDefinition so that it points to the 'AttrDropdowns' rule.
The 'FixImportedObjects' utility will fix the TaskDefinition that runs the rule for you!
(The 'clean' option for tasks that run rules has a known bug in IIQ (bug#: 16227 et al).
Alternatively, you can simply run the 'AttrDropdown' rule in the CLI or the GUI.

NOTE:  This utility will overwrite any rule that has a name that matches the name of an Identity Attribute!
This allows you to run the rule after an object has been imported that references a FieldValue rule that had not been created yet.
[ie: If you import an application that references 'firstname' in its Provisioning Policy before you run the AttrDropdowns rule,
there will already be a FieldValue rule named 'firstname'.  The AttrDropdowns rule must overwrite it otherwise the rule will simply
be an empty shell...]

Once you have run the rule, you will have FieldValue rules that you can select in the Provisioning Policy UI.

*******************
* Version History *
*******************
26 March 2015: Removed the Quicklink reference
24 March 2015: Initial revision

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-AttrDropdowns