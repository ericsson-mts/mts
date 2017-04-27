echo OFF

set OPTIONS=-level:WARN -stor:file -gen:false -show:false -param:[iterations]+1
rem set MODE1=1
set MODE1=-seq

rem run the unit tests
cd ..\bin
del /Q/F/S ..\logs

echo *************** functional testing
call startCmd.bat ..\tutorial\core\test.xml %MODE1% -param:param_1+one -param:[param_2]+two -param:[param_3]+three %OPTIONS%
call startCmd.bat ..\tutorial\diameter\test.xml %MODE1% -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.dk.StackDiameter %OPTIONS%
call startCmd.bat ..\tutorial\diameter\test_light.xml %MODE1% -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.light.StackDiameter -config:USE_NIO+false %OPTIONS%
call startCmd.bat ..\tutorial\diameter\test_light.xml %MODE1% -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.light.StackDiameter -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\sip\test.xml %MODE1% -config:USE_NIO+false %OPTIONS%
call startCmd.bat ..\tutorial\sip\test.xml %MODE1% -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\sip\test_jain.xml %MODE1% -config:protocol.STACK_CLASS_NAME_SIP+com.devoteam.srit.xmlloader.sip.jain.StackSip %OPTIONS%
call startCmd.bat ..\tutorial\rtp\test.xml %MODE1% -config:USE_NIO+false %OPTIONS%
call startCmd.bat ..\tutorial\rtp\test.xml %MODE1% -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\rtp\test.xml 001_jmf_noreg -config:protocol.STACK_CLASS_NAME_RTP+com.devoteam.srit.xmlloader.rtp.jmf.StackRtp %OPTIONS%
call startCmd.bat ..\tutorial\rtpflow\test.xml %MODE1% -config:USE_NIO+false %OPTIONS%
call startCmd.bat ..\tutorial\rtpflow\test.xml %MODE1% -config:USE_NIO+true -config:message.KEEP_SENT_MESSAGES+true %OPTIONS%
call startCmd.bat ..\tutorial\http\test.xml %MODE1% -config:USE_NIO+false %OPTIONS% 
call startCmd.bat ..\tutorial\http\test.xml %MODE1% -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\tcp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\udp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\radius\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\smtp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\rtsp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\imap\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\pop\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\smpp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\ucp\test.xml %MODE1% %OPTIONS%

call startCmd.bat ..\tutorial\sigtran\m3ua\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\BICC\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\SCCP\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\iua\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\v5ua\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\TCAP\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\MAP\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\CAP\test.xml %MODE1% %OPTIONS%

call startCmd.bat ..\tutorial\h323\h225CS\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\tls\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\h248\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\msrp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\snmp\test.xml %MODE1% %OPTIONS%

REM call startCmd.bat ..\tutorial\pstn\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\mgcp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\stun\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\gtp\test.xml %MODE1% %OPTIONS%
call startCmd.bat ..\tutorial\ethernet\test.xml %MODE1% %OPTIONS%

pause

REM ******************************************* genscript module
cd ..\tutorial\genscript
call genscripttest.bat

REM ******************************************* importsipp module
cd ..\tutorial\importsipp
call startTest.bat

REM ******************************************* gui module
call startGui.bat

REM ******************************************* master module
call startMaster.bat

REM ******************************************* master module
call startSlave.bat

cd ..\..

pause
