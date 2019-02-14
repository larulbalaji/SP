enableTimeMachine
Date: Aug 9, 2013
Author: jeff.bounds@sailpoint.com

************************
* Library Dependencies *
************************
None

************************
*      Execution       *
************************
Now there is a handy utility called "time machine" at the location http://host:port/identityiq/debug/timeMachine.jsf

You can move time forward and run the 'Check Expired WorkItems' task to see that escalations / reminders are happening as expected. So to check the reminder 1 is being sent 7 days after start... move time 7 days forward and run the task. Then to check that Escalation is happening 7 days after that... move the time forward 7 more days and the run the 'Check Expired WorkItems' task again.

Note:- You will need system administrator capability *and* will need to set system configuration value 'timeMachineEnabled' to true.
Note:- This only impacts the date calculations. This *DOES NOT* reset the system clock or even the JVM clock.


************************
*      Background      *
************************
When dealing with Future events such as a future termination date, we sometime create scheduledWorkflows
or Requests to terminate the user at a later date.   We can set the scheduledWorkflow delay to a 
few seconds or minutes.   The alternative is to have actualy dates and then use the timeMachine to move
the application clock forward or reset.

************************
*   To Configure       *
************************
During SERI setup, run "ant enableTimeMachine"

************************
*  Version History     *
************************
09 Aug 2013 - First Release
