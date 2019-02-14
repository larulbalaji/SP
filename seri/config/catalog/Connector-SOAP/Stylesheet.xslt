<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<!-- kmj -->
	<!-- You will need to generate templates for the following elements: create 
		disable enable delete Each of these input elements will have a number of 
		child elements matching the format <operation name='name'>value</operation> 
		where operation is Set, Add, Remove name is the name of the attribute, value 
		is the value Transform this into a soap envelope for your target application -->

  <!-- The easiest way to get the envelopes to do the translation for is probably to user
        something like SOAPUI (http://www.soapui.org/) and load the WSDL in then generate
        sample requests -->


	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

	<xsl:template xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" match="soapenv:Envelope|soapenv:Header|soapenv:Body">
		<xsl:apply-templates select="@*|node()" />
	</xsl:template>

	<!-- Template for aggregating accounts (outbound) -->
	<!-- Potentially you will get passed a <filter> object under aggregate -->
	<xsl:template match="aggregate">
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soapserver.userstore.kjames.sailpoint.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getUsers/>
   </soapenv:Body>
</soapenv:Envelope>
	</xsl:template>
	
	<xsl:template xmlns:ns2="http://soapserver.userstore.kjames.sailpoint.com/"
		match="ns2:getUsersResponse">
		<results>
			<xsl:for-each select="return">
				<object>
					<xsl:attribute name="id"><xsl:value-of select="username"/></xsl:attribute>
					<xsl:element name="attribute">
						<xsl:attribute name="name">id</xsl:attribute>
						<xsl:value-of select="username" />
					</xsl:element>
					<xsl:element name="attribute">
						<xsl:attribute name="name">firstname</xsl:attribute>
						<xsl:value-of select="firstname" />
					</xsl:element>
					<xsl:element name="attribute">
						<xsl:attribute name="name">lastname</xsl:attribute>
						<xsl:value-of select="lastname" />
					</xsl:element>
				<xsl:for-each select="attributes/entry">
					<xsl:element name="attribute">
						<xsl:attribute name="name"><xsl:value-of
							select="key" /></xsl:attribute>
						<xsl:value-of select="value" />
					</xsl:element>
				</xsl:for-each>
				</object>
			</xsl:for-each>
		</results>
	</xsl:template>

	<xsl:template match="create">
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soapserver.userstore.kjames.sailpoint.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:addUser>
         <username><xsl:value-of select="@id"/></username>
         <firstname><xsl:value-of select="attribute[@name='firstname']"/></firstname>
         <lastname><xsl:value-of select="attribute[@name='lastname']"/></lastname>
         <attributes>
           <xsl:for-each select="attribute[@name!='firstname' and @name!='lastname']">
            <entry>
               <key><xsl:value-of select="@name"/></key>
               <value><xsl:value-of select="."/></value>
            </entry>
           </xsl:for-each>
         </attributes>
      </soap:addUser>
   </soapenv:Body>
</soapenv:Envelope>
  </xsl:template>

	
	<xsl:template match="delete">
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:soap="http://soapserver.userstore.kjames.sailpoint.com/">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:deleteUser>
         <username><xsl:value-of select="@id"/></username>
      </soap:deleteUser>
   </soapenv:Body>
</soapenv:Envelope>
	</xsl:template>

	<xsl:template xmlns:ns2="http://soapserver.userstore.kjames.sailpoint.com/" match="//ns2:addUserResponse|//ns2:deleteUserResponse">
		<xsl:choose>
			<xsl:when test="return/statusCode='0'">
				<ok/>
			</xsl:when>
			<xsl:otherwise><fail/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
