<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="/">
<xsl:output method="xml" doctype-system="sailpoint.dtd" doctype-public="sailpoint.dtd" indent="yes" />
<!--DOCTYPE sailpoint PUBLIC "sailpoint.dtd" "sailpoint.dtd"-->
<sailpoint>
<xsl:apply-templates select="//TaskDefinition[@name='Aggregate HR Authoritative']"/>
</sailpoint>
</xsl:template>

</xsl:stylesheet>
