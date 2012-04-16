<xsl:stylesheet version = '2.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:output method="xml" indent="no" encoding="UTF-8" omit-xml-declaration="no"/>
<xsl:strip-space elements="*"/>
<xsl:preserve-space elements="xsl:text"/>

<xsl:param name='destScenario'></xsl:param>
<xsl:param name='RTPPauseTime'></xsl:param>

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
<parameter name="payload" operation="setFromCsv" value="./RTPSendPart.csv" value2="Payload" /><xsl:text>&#xA;</xsl:text>	
<parameter name="payloadType" operation="setFromCsv" value="./RTPSendPart.csv" value2="PayloadType" /><xsl:text>&#xA;</xsl:text>	
<parameter name="sequenceNumber" operation="setFromCsv" value="./RTPSendPart.csv" value2="SequenceNumber" /><xsl:text>&#xA;</xsl:text>	
<parameter name="RTPTimestamp" operation="setFromCsv" value="./RTPSendPart.csv" value2="RTPTimestamp" /><xsl:text>&#xA;</xsl:text>	
<parameter name="ssrc" operation="setFromCsv" value="./RTPSendPart.csv" value2="Ssrc" /><xsl:text>&#xA;</xsl:text>	
<parameter name="relativeSendTimestampFromCSV" operation="setFromCsv" value="./RTPSendPart.csv" value2="RelativeSendTimestamp(msec)" /><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#xA;</xsl:text>	
<parameter name="numberOfPackets" operation="size" value="[payload]"/><xsl:text>&#xA;</xsl:text>	
<parameter name="counter" operation="set" value="0"/><xsl:text>&#xA;</xsl:text>	

<parameter name="startTimestamp" operation="setFromSystem" value="Timestamp" value2="1970"/><xsl:text>&#xA;</xsl:text>	
<parameter name="nextSendTimestamp" operation="set" value="[startTimestamp]"/><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#xA;</xsl:text>	
<while><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#x9;</xsl:text><condition>
<test parameter="[counter]" condition="lowerThan" value="[numberOfPackets]"/>
</condition><xsl:text>&#xA;</xsl:text><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#x9;</xsl:text><do><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#x9;</xsl:text><xsl:element name="sendPacketRTP">
		<xsl:attribute name="name">sendPacketRTP</xsl:attribute>
		<xsl:attribute name="state">true</xsl:attribute>
		<xsl:attribute name="sessionName">[sessionName]</xsl:attribute>
		<xsl:if test="$destScenario!=''">
			<xsl:attribute name="destScenario">[destScenario]</xsl:attribute>
		</xsl:if><xsl:text>&#xA;</xsl:text>	
<xsl:text>&#x9;&#x9;</xsl:text><packet><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;&#x9;&#x9;</xsl:text><header ssrc="[ssrc([counter])]" payloadType="[payloadType([counter])]" seqnum="[sequenceNumber([counter])]" timestamp="[RTPTimestamp([counter])]"/><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;&#x9;&#x9;</xsl:text><payload format="binary">[payload([counter])]</payload><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;&#x9;</xsl:text></packet><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text></xsl:element><xsl:text>&#xA;</xsl:text>	

		<xsl:if test="$RTPPauseTime!=''">
			<xsl:text>&#x9;&#x9;</xsl:text><parameter name="nextSendTimestamp" operation="add" value="[nextSendTimestamp]" value2="[RTPPauseTime]"/><xsl:text>&#xA;</xsl:text>
			<xsl:text>&#x9;&#x9;</xsl:text><xsl:comment>Use the following line if you want to use pause time from csv file</xsl:comment><xsl:text>&#xA;</xsl:text>
			<xsl:text>&#x9;&#x9;</xsl:text><xsl:comment>&#60;parameter name="nextSendTimestamp" operation="add" value="[startTimestamp]" value2="[relativeSendTimestampFromCSV([counter])]"/&#61;</xsl:comment><xsl:text>&#xA;</xsl:text>
		</xsl:if>
		<xsl:if test="$RTPPauseTime=''">
			<xsl:text>&#x9;&#x9;</xsl:text><parameter name="nextSendTimestamp" operation="add" value="[startTimestamp]" value2="[relativeSendTimestampFromCSV([counter])]"/><xsl:text>&#xA;</xsl:text>
			<xsl:text>&#x9;&#x9;</xsl:text><xsl:comment>Use the following line if you want to impose your own packet tempo ( you need to set RTPPauseTime variable)</xsl:comment><xsl:text>&#xA;</xsl:text>
			<xsl:text>&#x9;&#x9;</xsl:text><xsl:comment>&#60;parameter name="nextSendTimestamp" operation="add" value="[nextSendTimestamp]" value2="[RTPPauseTime]"/&#61;</xsl:comment><xsl:text>&#xA;</xsl:text>
		</xsl:if><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><parameter name="currentTimestamp" operation="setFromSystem" value="Timestamp" value2="1970"/><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><parameter name="diffTime" operation="substract" value="[nextSendTimestamp]" value2="[currentTimestamp]"/><xsl:text>&#xA;&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><if>
			<condition>
				<test parameter="[diffTime]" condition="greaterThan" value="0"/>
			</condition><xsl:text>&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><then>
				<pause name="between 2 rtp packets" milliseconds="[diffTime]"/>
			</then>
			</if><xsl:text>&#xA;&#xA;</xsl:text>
<xsl:text>&#x9;</xsl:text><parameter name="counter" operation="add" value="[counter]" value2="1"/><xsl:text>&#xA;</xsl:text>
	</do><xsl:text>&#xA;</xsl:text>
</while><xsl:text>&#xA;</xsl:text>

</scenario>
</xsl:template>
</xsl:stylesheet>