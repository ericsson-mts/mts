<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="yes"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>

<xsl:param name='destScenario'></xsl:param>
<xsl:param name='RTPPauseTime'></xsl:param>


<xsl:template match="/pdml">
	
<xsl:comment>
This is a part of a XMLLoader scenario. It was generated automatically from Wireshark pdml capture file.
It opens a RTP connection and sends RTP packets exactly as they were captured.

To use this script in your own script must initialize the following parameters:
- [localHost] : Sender's IP address;
- [localPort]  : Sender's port;
- [remoteHost] : Receiver's IP address;
- [remotePort]  : Receivers's port;
- [sessionName]  : RTP session name (whatever name you like;
<xsl:if test="$destScenario!=''">
	<xsl:text>- [destScenario]  : the destination scenario of the RTP packets&#xA;</xsl:text>	
</xsl:if>
<xsl:if test="$RTPPauseTime!=''">
	<xsl:text>- [RTPPauseTime]  : pause between two RTP packet&#xA;</xsl:text>	
</xsl:if>
</xsl:comment>
	
	<xsl:text>&#xA;</xsl:text>	
	<xsl:element name = "openConnectionRTP">
		<xsl:attribute name="name">OpenConnectionRTP</xsl:attribute>
		<xsl:attribute name="state">true</xsl:attribute>
		<xsl:attribute name="sessionName">[sessionName]</xsl:attribute>
		<xsl:attribute name="localHost">[localHost]</xsl:attribute>
		<xsl:attribute name="localPort">[localPort]</xsl:attribute>
		<xsl:attribute name="remoteHost">[remoteHost]</xsl:attribute>
		<xsl:attribute name="remotePort">[remotePort]</xsl:attribute>
	</xsl:element>
	<xsl:text>&#xA;</xsl:text>		
	<xsl:text>&#xA;</xsl:text>	
	<parameter name="startTimestamp" operation="setFromSystem" value="Timestamp" value2="1970"/><xsl:text>&#xA;</xsl:text>
	<parameter name="nextsendTimestamp" operation="set" value="[startTimestamp]"/><xsl:text>&#xA;</xsl:text>
	<xsl:text>&#xA;</xsl:text>
	<xsl:apply-templates select="packet"/>
	<xsl:text>&#xA;</xsl:text>	
	
	<closeConnectionRTP name="closeConnectionRTP" state="true" sessionName="[sessionName]" />	

</xsl:template>

<xsl:template match="packet[proto[@name='rtp']]">
	<xsl:text>&#xA;</xsl:text>
	<xsl:element name="sendPacketRTP">
		<xsl:attribute name="name">sendPacketRTP</xsl:attribute>
		<xsl:attribute name="state">true</xsl:attribute>
		<xsl:attribute name="sessionName">[sessionName]</xsl:attribute>
		<xsl:if test="$destScenario!=''">
			<xsl:attribute name="destScenario">[destScenario]</xsl:attribute>
		</xsl:if>
		<xsl:text>&#xA;</xsl:text>
		<xsl:element name="packet">
			<xsl:text>&#xA;</xsl:text>
			<xsl:text>    </xsl:text>
			<xsl:element name="header">
				<xsl:attribute name="ssrc"><xsl:value-of select="proto[@name='rtp']/field[@name='rtp.ssrc']/@show"/></xsl:attribute>
				<!--<xsl:attribute name="ssrc">0</xsl:attribute>-->
				<xsl:attribute name="payloadType"><xsl:value-of select="proto[@name='rtp']/field[@name='rtp.p_type']/@show"/></xsl:attribute>
				<xsl:attribute name="seqnum"><xsl:value-of select="proto[@name='rtp']/field[@name='rtp.seq']/@show"/></xsl:attribute>
				<xsl:attribute name="timestamp"><xsl:value-of select="proto[@name='rtp']/field[@name='rtp.timestamp']/@show"/></xsl:attribute>
			</xsl:element>
			<xsl:text>&#xA;</xsl:text>
			<xsl:apply-templates select="proto[@name='rtp']/field[@name='rtp.payload']"/>
			<xsl:text>&#xA;</xsl:text>
		</xsl:element>
		<xsl:text>&#xA;</xsl:text>
	</xsl:element><xsl:text>&#xA;</xsl:text>

	<xsl:if test="$RTPPauseTime!=''">
		<parameter name="nextsendTimestamp" operation="add" value="[nextsendTimestamp]" value2="[RTPPauseTime]"/><xsl:text>&#xA;</xsl:text>
	</xsl:if>
	<xsl:if test="$RTPPauseTime=''">
		<xsl:element name="parameter">
			<xsl:attribute name="name">nextsendTimestamp</xsl:attribute>
			<xsl:attribute name="operation">add</xsl:attribute>
			<xsl:attribute name="value">[startTimestamp]</xsl:attribute>
			<xsl:attribute name="value2"><xsl:value-of select="round((proto[@name='geninfo']/field[@name='timestamp']/@value - /pdml/packet[proto[@name='rtp']][1]/proto[@name='geninfo']/field[@name='timestamp']/@value)* 1000)"/></xsl:attribute>
		</xsl:element><xsl:text>&#xA;</xsl:text>
	</xsl:if>

	<parameter name="currentTimestamp" operation="setFromSystem" value="Timestamp" value2="1970"/><xsl:text>&#xA;</xsl:text>
	<parameter name="diffTime" operation="substract" value="[nextsendTimestamp]" value2="[currentTimestamp]"/><xsl:text>&#xA;</xsl:text>
	
	<if><condition><test parameter="[diffTime]" condition="greaterThan" value="0"/></condition><xsl:text>&#xA;</xsl:text>
		<xsl:text>&#x9;</xsl:text>
		<then><pause name="between 2 rtp packets" milliseconds="[diffTime]"/></then><xsl:text>&#xA;</xsl:text>
	</if>
	<xsl:text>&#xA;</xsl:text>
</xsl:template>

<xsl:template match ="proto[@name='rtp']/field[@name='rtp.payload']">
	<xsl:element name="payload"><xsl:attribute name="format">binary</xsl:attribute>
		<xsl:text>h</xsl:text><xsl:value-of select="replace(@show,':',' h')"/>
	</xsl:element>
</xsl:template>

</xsl:stylesheet>