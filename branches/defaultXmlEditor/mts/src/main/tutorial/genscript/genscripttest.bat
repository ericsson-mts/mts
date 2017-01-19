@echo off

rmdir out /s/q

cd ..\..\bin


echo --------------------------------------------
echo -- TEST SIP SUR UDP AVEC PORT PAR DEFAULT --
echo --------------------------------------------

set ARGS0="SIP:172.16.21.194:7070" 
set ARGS1="../tutorial/genscript/trace_SIP_UDP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

echo -----------------------------------------
echo -- TEST SIP SUR UDP AVEC UN AUTRE PORT --
echo -----------------------------------------

set ARGS0="SIP:172.16.21.194:5070" 
set ARGS1="../tutorial/genscript/trace_SIP_UDP_port_non_default.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

echo ------------------------------------------------
echo -- TEST SIP SUR UDP AVEC DES PORTS DIFFERENTS --
echo ------------------------------------------------

set ARGS0="SIP:172.16.21.194:5070" 
set ARGS1="../tutorial/genscript/trace_SIP_UDP_ports_differents.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="SIP:172.16.21.194:5080"
set ARGS1="../tutorial/genscript/trace_SIP_UDP_ports_differents.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

echo --------------------------------------------
echo -- TEST SIP SUR TCP AVEC PORT PAR DEFAULT --
echo --------------------------------------------

set ARGS1="SIP:172.16.21.194:2710" 
set ARGS1="../tutorial/genscript/trace_SIP_TCP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2% %ARGS3%

set ARGS0="SIP:172.16.21.194:7070" 
set ARGS1="../tutorial/genscript/trace_SIP_TCP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%


echo -----------------------------------------
echo -- TEST SIP SUR TCP AVEC UN AUTRE PORT --
echo -----------------------------------------

set ARGS0="SIP:172.16.21.194:3062" 
set ARGS1="../tutorial/genscript/trace_SIP_TCP_port_non_default.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%


set ARGS0="SIP:172.16.21.194:5070" 
set ARGS1="../tutorial/genscript/trace_SIP_TCP_port_non_default.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

echo ------------------------------------------------
echo -- TEST SIP SUR TCP AVEC DES PORTS DIFFERENTS --
echo ------------------------------------------------

set ARGS0="SIP:172.16.21.194:5070" 
set ARGS1="../tutorial/genscript/trace_SIP_TCP_ports_differents.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="SIP:172.16.21.194:5080"
set ARGS1="../tutorial/genscript/trace_SIP_TCP_ports_differents.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="SIP:172.16.21.194:3537"
set ARGS1="../tutorial/genscript/trace_SIP_TCP_ports_differents.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%


echo --------------
echo -- TEST RTP --
echo --------------

set ARGS0="RTP:172.16.21.194:10002" 
set ARGS1="../tutorial/genscript/trace_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="RTP:172.16.21.194:11000" 
set ARGS1="../tutorial/genscript/trace_RTP.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%


set ARGS0="SIP:172.16.21.194:7070" 
set ARGS1="RTP:172.16.21.194:10000"
set ARGS2="../tutorial/genscript/trace_RTP_event.pcap"
set ARGS3="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2% %ARGS3%

set ARGS0="RTP:172.16.21.194:11000" 
set ARGS1="../tutorial/genscript/trace_RTP_event.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%



echo --------------------
echo -- TEST DIAMETER  --
echo --------------------

set ARGS0="DIAMETER:172.16.21.194:3488" 
set ARGS1="../tutorial/genscript/trace_DIAMETER.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:172.16.21.194:3871" 
set ARGS1="../tutorial/genscript/trace_DIAMETER.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%


echo -------------------------
echo -- TEST DIAMETER REAL  --
echo -------------------------


set ARGS0="DIAMETER:192.168.200.228:51827" 
set ARGS1="../tutorial/genscript/trace_DIAM_telefonica.cap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:192.168.50.173:10000" 
set ARGS1="../tutorial/genscript/trace_DIAM_telefonica.cap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:192.168.200.228:51867" 
set ARGS1="../tutorial/genscript/trace_DIAM_telefonica.cap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%


set ARGS0="DIAMETER:10.194.126.75:29383" 
set ARGS1="../tutorial/genscript/trace_DIAM_Cisco_CSG.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:10.100.50.120:27700" 
set ARGS1="../tutorial/genscript/trace_DIAM_Cisco_CSG.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
rem call genscript.bat %ARGS0% %ARGS1% %ARGS2%

rem does not work because of message checking why ?
set ARGS0="DIAMETER:164.48.219.44:50708" 
set ARGS1="../tutorial/genscript/trace_DIAM_swm_ok.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:164.48.219.44:13868" 
set ARGS1="../tutorial/genscript/trace_DIAM_swm_ok.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%


set ARGS0="DIAMETER:164.48.219.55:3871" 
set ARGS1="../tutorial/genscript/EX24_DIAMETER.pcap"
set ARGS2="../tutorial/genscript/out/testout.xml"
call genscript.bat %ARGS0% %ARGS1% %ARGS2%

set ARGS0="DIAMETER:164.48.219.55:65293" 
set ARGS1="../tutorial/genscript/EX24_DIAMETER.pcap"
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
