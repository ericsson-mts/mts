@echo off

rmdir out /s/q

cd ..\..\bin


echo ---------------
echo -- TUTO SIP  --
echo ---------------

set ARGS0="SIP:172.16.21.32:7070" 
set ARGS1="../tutorial/genscript/tutorial_SIP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="SIP:172.16.21.32:5070" 
set ARGS1="../tutorial/genscript/tutorial_SIP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="SIP:172.16.21.32:5080" 
set ARGS1="SIP:172.16.21.32:4626" 
set ARGS2="../tutorial/genscript/tutorial_SIP.pcap"
set ARGS3="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2% %ARGS3%


echo ---------------
echo -- TUTO RTP  --
echo ---------------

set ARGS0="RTP:172.16.21.32:10000"
set ARGS1="../tutorial/genscript/tutorial_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="RTP:172.16.21.32:10002"
set ARGS1="../tutorial/genscript/tutorial_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="RTP:172.16.21.32:10007"
set ARGS1="../tutorial/genscript/tutorial_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="RTP:172.16.21.32:11000"
set ARGS1="../tutorial/genscript/tutorial_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="RTP:172.16.21.32:1066"
set ARGS1="../tutorial/genscript/tutorial_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%


echo --------------------
echo -- TUTO DIAMETER  --
echo --------------------

set ARGS0="DIAMETER:172.16.21.32:4279" 
set ARGS1="../tutorial/genscript/tutorial_DIAMETER.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:172.16.21.32:3871" 
set ARGS1="../tutorial/genscript/tutorial_DIAMETER.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

echo ------------------------------
echo -- TEST DES SCRIPTS GENERES --
echo ------------------------------

cd ..\bin

set OPTIONS=-level:WARN -stor:file -gen:false -show:false
set ARGS0="../tutorial/genscript/out/testout.xml"
call startCmd.bat %ARGS0% -seq %OPTIONS%

pause
