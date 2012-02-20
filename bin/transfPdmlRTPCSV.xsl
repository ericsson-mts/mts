<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="text" indent="no" encoding="UTF-8"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>

<xsl:template match="/pdml">
	<xsl:text>Payload;PayloadType;SequenceNumber;RTPTimestamp;Ssrc;RelativeSendTimestamp(msec);AbsoluteSendTimestamp(sec)&#xA;</xsl:text>
		<xsl:apply-templates select="packet">
		<xsl:with-param name="firstRTPPacketAbsoluteTimestamp" select="packet[proto[@name='rtp']][1]/proto[@name='geninfo']/field[@name='timestamp']/@value"/>
	</xsl:apply-templates>
</xsl:template>
<xsl:template match="packet[proto[@name='rtp']]">
	<xsl:param name="firstRTPPacketAbsoluteTimestamp"/>
	<xsl:apply-templates select="proto[@name='rtp']/field[@name='rtp.payload']"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="proto[@name='rtp']/field[@name='rtp.p_type']/@show"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="proto[@name='rtp']/field[@name='rtp.seq']/@show"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="proto[@name='rtp']/field[@name='rtp.timestamp']/@show"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="proto[@name='rtp']/field[@name='rtp.ssrc']/@show"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="round((proto[@name='geninfo']/field[@name='timestamp']/@value - $firstRTPPacketAbsoluteTimestamp) * 1000)"/>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="proto[@name='geninfo']/field[@name='timestamp']/@value"/>
	<xsl:text>&#xA;</xsl:text>
</xsl:template>

<xsl:template match ="field[@name='rtp.payload']">
	<xsl:element name="payload"><xsl:attribute name="format">binary</xsl:attribute>
		<xsl:text>h</xsl:text><xsl:value-of select="replace(@show,':',' h')"/>
	</xsl:element>
</xsl:template>
</xsl:stylesheet>