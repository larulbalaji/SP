RemoveCreateProcessTrigger
Date: June 2, 2015
Author: jeff.bounds@sailpoint.com

************************
* Library Dependencies *
************************
None

************************
*      Execution       *
************************
This will create an Identity Refresh task called "Identity Refresh - Remove Joiner Event".
Execute this as a normal refresh task.

************************
*      Background      *
************************
When you first aggregate a user and their identity cube is created, a trigger is added called needsCreateProcessing.
This trigger is used when Lifecycle Events are processed.   Specifically in this case the "Joiner event".
Sometimes during a POC we don't want the Joiner Event to execute for a set of users.   This artifact will clear that trigger.

************************
*   To Configure       *
************************
Import setup.xml.   Execute "Identity Refresh - Remove Joiner Event".
The comments can be removed to include removing any Native Change Detections

************************
*  Version History     *
************************
09 Aug 2013 - First Release
02 Jun 2015 - Added NativeChangeDetection and context.saveObject

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Utility-RemoveJoinerEvent
