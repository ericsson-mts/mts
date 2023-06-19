

@echo  off
REM generate Test Plan from Tutorial
cd ..\bin
call startCmd.bat ..\tutorial\core\test.xml -testplan
call startCmd.bat ..\tutorial\diameter\test.xml -testplan
call startCmd.bat ..\tutorial\sip\test.xml -testplan
call startCmd.bat ..\tutorial\rtp\test.xml -testplan
call startCmd.bat ..\tutorial\rtpflow\test.xml -testplan
call startCmd.bat ..\tutorial\rtpflow\test.xml -testplan
call startCmd.bat ..\tutorial\http\test.xml -testplan
call startCmd.bat ..\tutorial\tcp\test.xml -testplan
call startCmd.bat ..\tutorial\udp\test.xml -testplan
call startCmd.bat ..\tutorial\radius\test.xml -testplan
call startCmd.bat ..\tutorial\smtp\test.xml -testplan
call startCmd.bat ..\tutorial\rtsp\test.xml -testplan
call startCmd.bat ..\tutorial\imap\test.xml -testplan
call startCmd.bat ..\tutorial\pop\test.xml -testplan
call startCmd.bat ..\tutorial\smpp\test.xml -testplan
call startCmd.bat ..\tutorial\ucp\test.xml -testplan
call startCmd.bat ..\tutorial\sigtran\test.xml -testplan
call startCmd.bat ..\tutorial\tls\test.xml -testplan
call startCmd.bat ..\tutorial\h248\test.xml -testplan
call startCmd.bat ..\tutorial\msrp\test.xml -testplan
call startCmd.bat ..\tutorial\snmp\test.xml -testplan
cd ../..
pause