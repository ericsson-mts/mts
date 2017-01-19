<xsl:stylesheet version = '2.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:character-map name="mycharmap">
		<xsl:output-character character="&#xBB;" string="&amp;"/>
	</xsl:character-map>
<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="no" use-character-maps="mycharmap"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>

<xsl:param name='destScenario'></xsl:param>

<xsl:template match="/">
<xsl:text disable-output-escaping="yes"><![CDATA[
<!DOCTYPE RTPimport [
  <!ENTITY RTPSendFile SYSTEM "RTPSendPart.xml">
]>]]></xsl:text>
<xsl:text>&#xA;</xsl:text>	
<scenario>
<xsl:if test="$destScenario!=''">
<xsl:text>&#xA;</xsl:text>	
	<semaphore name="SEM1" action="notify" /><xsl:text>&#xA;</xsl:text>
	<semaphore name="SEM2" action="wait" timeout="10" /><xsl:text>&#xA;</xsl:text>
	<pause name="small pause" seconds="0.05" /><xsl:text>&#xA;</xsl:text>
</xsl:if>

<xsl:text>&#xA;&#xBB;RTPSendFile;&#xA;</xsl:text>

</scenario>
</xsl:template>
</xsl:stylesheet>