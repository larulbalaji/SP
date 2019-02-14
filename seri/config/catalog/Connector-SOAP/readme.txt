SOAP Connector v1.0.0
10 July 2013

Contact: kevin.james@sailpoint.com

**************
* Background *
**************

The SOAPConnector is a sample connector build to show bidirectional connectivitiy with a SOAP service.

The interaction with the service is enabled through the use of an XSL stylesheet. The stylesheet must exist on the filesystem. There is
currently a bug where the stylesheet cannot be referenced relative to the classpath of the appserver. a sample stylesheet is included.

****************
* To Configure *
****************

The SOAPConnector itself generates a number of XML documents for the various actions:

Aggregation
-----------
<aggregate/>
This initiates an aggregation of the accounts on the target system. This should be translated into a SOAP envelope to collect
account data.
The returning SOAP message should be translated into the following format:
<results>
  <object id="targetId">
    <attribute name="attribute1">value1</attribute>
    .
    .
    for N attributes
  </object>
  .
  .
  for N objects
</results>

Creation
--------
<create id="targetId">
  <attribute name="attribute1">value1</attribute>
  <attribute name="attribute2">value2</attribute>
</create>
This initiates creation of an account on the target system. This should be translated into a SOAP envelope to create an account.

The returning SOAP message should be translated into one of the following:
<ok/>
<fail/>

Deletion
--------
<delete id="targetId"/>
  <attribute name="attribute1">value1</attribute>
  <attribute name="attribute2">value2</attribute>
</delete>
This initiates creation of an account on the target system. This should be translated into a SOAP envelope to create an account.

The returning SOAP message should be translated into one of the following:
<ok/>
<fail/>


*******************
* Version History *
*******************

10 July 2013
Initial version

********************************
* Testing Status/ Known Issues *
********************************

https://github.com/sailpoint/seri/labels/Connector-SOAP
