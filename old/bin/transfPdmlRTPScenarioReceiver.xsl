<xsl:stylesheet version = '2.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="no"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>
<xsl:template match="/pdml">

<xsl:text>&#xA;</xsl:text>
<scenario><xsl:text>&#xA;</xsl:text>
<openConnectionRTP sessionName="RTPReceiverConnection" localHost="[remoteHost]" localPort="[remotePort]" remoteHost="[localHost]" remotePort="[localPort]"/><xsl:text>&#xA;</xsl:text>
<parameter name="[count]" operation="set" value="1"/><xsl:text>&#xA;</xsl:text>
<semaphore name="SEM1" action="wait" timeout="0" /><xsl:text>&#x9;</xsl:text>
<semaphore name="SEM2" action="notify" /><xsl:text>&#x9;</xsl:text>
<while><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text>	<condition>
	<xsl:element name="test">
		<xsl:attribute name="parameter">[count]</xsl:attribute>
		<xsl:attribute name="condition">lowerEqualThan</xsl:attribute>
		<xsl:attribute name="value"><xsl:value-of select="count(/pdml/packet[proto[@name='rtp']])"/></xsl:attribute>
	</xsl:element>
	</condition><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><do>
<receivePacketRTP timeout="0"/><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><parameter name="[count]" operation="add" value="[count]" value2="1"/></do><xsl:text>&#xA;</xsl:text>
</while><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><finally><closeConnectionRTP sessionName="RTPReceiverConnection" /></finally><xsl:text>&#xA;</xsl:text>

</scenario>
</xsl:template>
</xsl:stylesheet>