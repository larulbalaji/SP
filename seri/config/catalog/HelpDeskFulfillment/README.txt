Helpdesk Integration v1.0.0
10 July 2013

Contact: seteam@sailpoint.com

**************
* Background *
**************

Before actual productized Remedy integration, there was the Help Desk integration.

This package provides a sample integration executor to collect provisioning
plan details from IdentityIQ and prepare a well-formatted email message.  This
can be useful for applications that are manually administered or for a Help Desk
system that can take a well-formatted email message as input for creating a
Help Desk ticket.

At a minimum, this is useful as a way to demonstrate the details that IIQ can 
send to the provisioning layer, regardless of the manner in which it is sent.

****************
* To Configure *
****************

   * Import the configuration objects that are placed into the $SPHOME/WEB-INF/config/SEDemo-Addons/HelpDesk
     directory.  An importAll.xml is available to import the other objects.

   * Determine which application(s) are desired for Help Desk integration and add these 
     Application / Entitlement attribute pairs to the IntegConfig object.

   * Deploy the HelpDeskExecutor.class to $SPHOME/WEB-INF/classes/sailpoint/integration

   * NOTE: Be careful with email template configuration, so that the domain in the from-address (stored
           in the POC Configuration object) and the image paths and embedded links in the Email Templates
           are correct for your environment.  They currently assume: http://localhost:8080/identityiq

   * NOTE: Be careful with the 'universalManager' value in the Integration Configuration object.  If you have
           multiple Integration Executors, then you will want to remove this, so the Help Desk integrator
           doesn't attempt to (or isn't assigned inadvertently) to handle provisioning for some other application.

   *  <entry key="manualWorkItem" value="false"/> set this value to true in order for a manual work item to be created for fulfillment.

   *  <entry key="optimisticProvisioning" value="false"/> set this value to true for optimistic provisioning to be performed.  If this value is true the 
      provisioning will be assumed successful and immediately reflected on the cube.

   *  <entry key="planRuleName" value="none"/>  This provides a rule that allows manipulation of the plan prior to being emailed or sent in a manual work item.
      This also provides provides a hook to execute other logic without manipulating the plan.  For example, a workflow could be executed as part of remediation.

******************
* To Demonstrate *
******************

   * Create a provisioning event.  The most common of these is to revoke some entitlement (e.g. an AD Group)
     on an application that is configured to integrate via HelpDeskExecutor.

*******************
* Version History *
*******************

10 July 2013
Initial revision

06 April 2015
Brent Hauf added optimistic provisioning and manual work item creation.

********************************
* Testing Status/ Known Issues *
********************************
https://github.com/sailpoint/seri/labels/HelpDeskFulfillment