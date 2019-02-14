Requires: JDK 1.8

First, create the database using the SQL in WEB-INF/database/idnConfig.sql

Then, in the war you’ll need to update META-INF/context.xml to point at your database if you’re not running MySQL on localhost.
This file needs to be updated *in* the war.

Then zip it up into a war file again and deploy it using Tomcat.

Next, create a source in IDN to be leveraged by the API. Typically this will be of type Generic.
Make sure the "Accounts" checkbox is selected.

Next, create an Identity Profile in IDN and leverage the Generic source you just created for the Identity Mappings.

Next, go to localhost:8080/registration on your tomcat and database server and click on "Configure".
Enter the org name (short name, not full URL) and API details and hit "Get Sources"

Select the source you created and click Save


In this version you need to edit the source to change the attributes.
You need to modify idnRegCtrl.js in two places and index.html to add / remove attributes.


  