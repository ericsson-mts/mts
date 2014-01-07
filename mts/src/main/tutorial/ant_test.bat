echo OFF

set OPTIONS=-level:DEBUG -stor:file -gen:false -show:false

rem run the unit tests
cd ..\bin

REM ******************************************* protocol modules
call startCmd.bat ..\tutorial\core\test.xml -sequential %OPTIONS% -param:param_1+one -param:[param_2]+two -param:[param_3]+three
call startCmd.bat ..\tutorial\diameter\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sip\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sip\test.xml -sequential -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\sip\test.xml -sequential -config:protocol.STACK_CLASS_NAME_SIP+com.devoteam.srit.xmlloader.sip.jain.StackSipJain %OPTIONS%
call startCmd.bat ..\tutorial\rtp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\rtpflow\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\rtpflow\test.xml -sequential -config:message.KEEP_SENT_MESSAGES+true %OPTIONS%
call startCmd.bat ..\tutorial\http\test.xml -sequential %OPTIONS% 
call startCmd.bat ..\tutorial\http\test.xml -sequential -config:USE_NIO+true %OPTIONS%
call startCmd.bat ..\tutorial\tcp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\udp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\radius\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\smtp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\rtsp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\imap\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\pop\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\smpp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\ucp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\m3ua\SCCP\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\iua\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\sigtran\v5ua\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\h323\h225CS\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\tls\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\h248\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\msrp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\snmp\test.xml -sequential %OPTIONS%
REM call startCmd.bat ..\tutorial\pstn\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\mgcp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\stun\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\gtp\test.xml -sequential %OPTIONS%
call startCmd.bat ..\tutorial\ethernet\test.xml -sequential %OPTIONS%

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

cd ..\..

pause
