#!/bin/sh

OPTIONS= "-level:DEBUG -stor:file -gen:false -show:false"

echo "Run the unit tests"
cd ../bin

echo ******************************************* protocol modules
sh ./startCmd.sh ../tutorial/core/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/diameter/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test.xml -sequential -config:USE_NIO+true $OPTIONS
sh ./startCmd.sh ../tutorial/sip/test_jain.xml -sequential -config:protocol.STACK_CLASS_NAME_SIP+com.devoteam.srit.xmlloader.sip.light.StackJainSip %OPTIONS%
sh ./startCmd.sh ../tutorial/rtp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/rtpflow/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/rtpflow/test.xml -sequential -config:message.KEEP_SENT_MESSAGES+true $OPTIONS
sh ./startCmd.sh ../tutorial/http/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/http/test.xml -sequential -config:USE_NIO+true %OPTIONS%
sh ./startCmd.sh ../tutorial/tcp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/udp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sctp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/radius/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/smtp/test.xml -sequentia $OPTIONS
sh ./startCmd.sh ../tutorial/rtsp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/imap/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/pop/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/smpp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/ucp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/m3ua/SCCP/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/iua/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/sigtran/v5ua/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/h323/h225CS/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/tls/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/h248/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/msrp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/snmp/test.xml -sequential $OPTIONS
# sh ./startCmd.sh ../tutorial/pstn/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/mgcp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/stun/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/gtp/test.xml -sequential $OPTIONS
sh ./startCmd.sh ../tutorial/ethernet/test.xml -sequential $OPTIONS

read

echo ******************************************* genscript module
cd ../tutorial/genscript
REM call genscripttest.bat

REM ******************************************* genscript module
cd ../tutorial/importsipp
REM call startTest.bat

REM ******************************************* gui module
sh ./startGui.sh

REM ******************************************* master module
sh ./startMaster.sh

cd ../..

read


