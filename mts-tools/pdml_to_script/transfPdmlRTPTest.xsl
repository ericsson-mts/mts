<xsl:stylesheet version = '2.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:character-map name="mycharmap">
		<xsl:output-character character="&#xBB;" string="&amp;"/>
	</xsl:character-map>
<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="no" use-character-maps="mycharmap"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>

<xsl:param name='localHost'></xsl:param>
<xsl:param name='localPort'></xsl:param>
<xsl:param name='remoteHost'></xsl:param>
<xsl:param name='remotePort'></xsl:param>
<xsl:param name='sessionName'></xsl:param>
<xsl:param name='destScenario'></xsl:param>
<xsl:param name='RTPPauseTime'></xsl:param>


	
<xsl:template match="/pdml/packet[proto[@name='rtp']][1]">
<xsl:text>&#xA;</xsl:text>	
<xsl:comment>
This is a XMLLoader Test generated automatically from Wireshark pdml capture file.
The script included in this test calls RTPSendFile script file to send a flow of RTP packets.

The following parameters can be customized.
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
<test name="RTP">
<xsl:text>&#xA;</xsl:text>

	<xsl:choose>
		<xsl:when test="$destScenario!=''">
			<parameter name="[localHost]" operation="setFromSystem" value="IPAddress" value2="eth0"/>
		</xsl:when>
		<xsl:when test="$localHost=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[localHost]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="proto[@name='ip']/field[@name='ip.src_host']/@show"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
		<xsl:when test="$localHost!=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[localHost]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="$localHost"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
	</xsl:choose>
		<!--   -->

<xsl:text>&#xA;</xsl:text>	
	<xsl:choose>
		<xsl:when test="$destScenario!=''">
			<parameter name="[localPort]" operation="set" value="8001"/>
		</xsl:when>
		<xsl:when test="$localPort=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[localPort]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="proto[@name='udp']/field[@name='udp.srcport']/@show"/></xsl:attribute>	
			</xsl:element>		
		</xsl:when>
		<xsl:when test="$localPort!=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[localPort]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="$localPort"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
		<!--   -->
	</xsl:choose>
<xsl:text>&#xA;</xsl:text>	
	<xsl:choose>
		<xsl:when test="$destScenario!=''">
			<parameter name="[remoteHost]" operation="setFromSystem" value="IPAddress" value2="eth0"/>
		</xsl:when>
		<xsl:when test="$remoteHost=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[remoteHost]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="proto[@name='ip']/field[@name='ip.dst_host']/@show"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
		<xsl:when test="$remoteHost!=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[remoteHost]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="$remoteHost"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
	</xsl:choose>
		<!--   -->

<xsl:text>&#xA;</xsl:text>	
	<xsl:choose>
		<xsl:when test="$destScenario!=''">
			<parameter name="[remotePort]" operation="set" value="8010"/>
		</xsl:when>
		<xsl:when test="$remotePort=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[remotePort]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="proto[@name='udp']/field[@name='udp.dstport']/@show"/></xsl:attribute>	
			</xsl:element>		
		</xsl:when>
		<xsl:when test="$remotePort!=''">
			<xsl:element name="parameter">
				<xsl:attribute name="name">[remotePort]</xsl:attribute>
				<xsl:attribute name="operation">set</xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="$remotePort"/></xsl:attribute>
			</xsl:element>
		</xsl:when>
		<!--   -->
	</xsl:choose>
<xsl:text>&#xA;</xsl:text>
<xsl:element name="parameter">
  <xsl:attribute name="name">[sessionName]</xsl:attribute>
  <xsl:attribute name="operation">set</xsl:attribute>
		<xsl:if test="$sessionName=''">
			<xsl:attribute name="value">RTPSession</xsl:attribute>
		</xsl:if>
		<xsl:if test="$sessionName!=''">
			<xsl:attribute name="value"><xsl:value-of select="$sessionName"/></xsl:attribute>
		</xsl:if>
		<!--   -->
</xsl:element>
<xsl:text>&#xA;</xsl:text>
<xsl:if test="$RTPPauseTime!=''">
	<xsl:element name="parameter">
		<xsl:attribute name="name">[RTPPauseTime]</xsl:attribute>
		<xsl:attribute name="operation">set</xsl:attribute>
		<xsl:attribute name="value"></xsl:attribute>
		<xsl:attribute name="value"><xsl:value-of select="$RTPPauseTime"/></xsl:attribute>
	</xsl:element>
	<xsl:text>&#xA;</xsl:text>
</xsl:if>

<xsl:if test="$destScenario!=''">
	<xsl:element name="parameter">
		<xsl:attribute name="name">[destScenario]</xsl:attribute>
		<xsl:attribute name="operation">set</xsl:attribute>
		<xsl:attribute name="value"></xsl:attribute>
		<xsl:attribute name="value"><xsl:value-of select="$destScenario"/></xsl:attribute>
	</xsl:element>
</xsl:if>
<xsl:text>&#xA;</xsl:text>
<testcase name="RTPScenario" description="RTPScenario" state="true"><xsl:text>&#xA;</xsl:text>
        <xsl:text>&#x9;</xsl:text><scenario name="RTPScenario">RTPScenario.xml</scenario><xsl:text>&#xA;</xsl:text>
		<xsl:if test="$destScenario!=''">
		<xsl:text>&#x9;</xsl:text><xsl:element name="scenario">
			<xsl:attribute name="name">[destScenario]</xsl:attribute>
			<xsl:text>RTPScenarioReceiver.xml</xsl:text>
		</xsl:element><xsl:text>&#xA;</xsl:text>
		</xsl:if>
</testcase><xsl:text>&#xA;</xsl:text>

</test>
</xsl:template>




</xsl:stylesheet> 