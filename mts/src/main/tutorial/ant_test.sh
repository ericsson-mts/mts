#!/bin/sh

OPTIONS="-level:WARN -stor:file -gen:false -show:false -param:[iterations]+1"
MODE1=-seq

echo "Run the unit tests"
cd ../bin
rm -rf ../logs/*

echo ************** functional testing
sh ./startCmd.sh ../tutorial/core/test.xml $MODE1 -param:param_1+one -param:[param_2]+two -param:[param_3]+three $OPTIONS
sh ./startCmd.sh ../tutorial/diameter/test.xml $MODE1 -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.dk.StackDiameter $OPTIONS
sh ./startCmd.sh ../tutorial/diameter/test_light.xml $MODE1 -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.light.StackDiameter -config:USE_NIO+false $OPTIONS
sh ./startCmd.sh ../tutorial/diameter/test_light.xml $MODE1 -config:protocol.STACK_CLASS_NAME_DIAMETER+com.devoteam.srit.xmlloader.diameter.light.StackDiameter -config:USE_NIO+true $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test.xml $MODE1 -config:USE_NIO+false $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test.xml $MODE1 -config:USE_NIO+true $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test_jain.xml $MODE1 -config:protocol.STACK_CLASS_NAME_SIP+com.devoteam.srit.xmlloader.sip.light.StackJainSip $OPTIONS
sh ./startCmd.sh ../tutorial/rtp/test.xml $MODE1 -config:USE_NIO+false $OPTIONS
sh ./startCmd.sh ../tutorial/rtp/test.xml $MODE1 -config:USE_NIO+true $OPTIONS
sh ./startCmd.sh ../tutorial/rtp/test.xml 001_jmf_noreg -config:protocol.STACK_CLASS_NAME_RTP+com.devoteam.srit.xmlloader.rtp.jmf.StackRtp $OPTIONS
sh ./startCmd.sh ../tutorial/rtpflow/test.xml $MODE1 -config:USE_NIO+false $OPTIONS
sh ./startCmd.sh ../tutorial/rtpflow/test.xml $MODE1 -config:message.KEEP_SENT_MESSAGES+true $OPTIONS
sh ./startCmd.sh ../tutorial/http/test.xml $MODE1 -config:USE_NIO+false $OPTIONS
sh ./startCmd.sh ../tutorial/http/test.xml $MODE1 -config:USE_NIO+true $OPTIONS
sh ./startCmd.sh ../tutorial/tcp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/udp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sctp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/radius/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/smtp/test.xml -sequentia $OPTIONS
sh ./startCmd.sh ../tutorial/rtsp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/imap/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/pop/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/smpp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/ucp/test.xml $MODE1 $OPTIONS

sh ./startCmd.sh ../tutorial/sigtran/m3ua/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/BICC/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/SCCP/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/iua/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/v5ua/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/TCAP/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/MAP/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/CAP/test.xml $MODE1 $OPTIONS

sh ./startCmd.sh ../tutorial/h323/h225CS/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/tls/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/h248/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/msrp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/snmp/test.xml $MODE1 $OPTIONS
# sh ./startCmd.sh ../tutorial/pstn/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/mgcp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/stun/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/gtp/test.xml $MODE1 $OPTIONS
sh ./startCmd.sh ../tutorial/ethernet/test.xml $MODE1 $OPTIONS

read

echo ******************************************* genscript module
cd ../tutorial/genscript
REM call genscripttest.bat

REM ******************************************* importsipp module
cd ..\tutorial\importsipp
call startTest.sh

REM ******************************************* gui module
sh ./startGui.sh &

REM ******************************************* master module
sh ./startMaster.sh &

REM ******************************************* master module
sh ./startSlave.sh &

cd ../..

read


