#!/bin/sh



echo "generate Test Plan from Tutorial"
cd ../bin
sh ./startCmd.sh ../tutorial/core/test.xml -testplan
sh ./startCmd.sh ../tutorial/diameter/test.xml -testplan
sh ./startCmd.sh ../tutorial/sip/test.xml -testplan
sh ./startCmd.sh ../tutorial/rtp/test.xml -testplan
sh ./startCmd.sh ../tutorial/rtpflow/test.xml -testplan
sh ./startCmd.sh ../tutorial/http/test.xml -testplan
sh ./startCmd.sh ../tutorial/tcp/test.xml -testplan
sh ./startCmd.sh ../tutorial/udp/test.xml -testplan
sh ./startCmd.sh ../tutorial/sctp/test.xml -testplan
sh ./startCmd.sh ../tutorial/radius/test.xml -testplan
sh ./startCmd.sh ../tutorial/smtp/test.xml -testplan
sh ./startCmd.sh ../tutorial/rtsp/test.xml -testplan
sh ./startCmd.sh ../tutorial/imap/test.xml -testplan
sh ./startCmd.sh ../tutorial/pop/test.xml -testplan
sh ./startCmd.sh ../tutorial/smpp/test.xml -testplan
sh ./startCmd.sh ../tutorial/ucp/test.xml -testplan
sh ./startCmd.sh ../tutorial/sigtran/test.xml -testplan
sh ./startCmd.sh ../tutorial/tls/test.xml -testplan
sh ./startCmd.sh ../tutorial/h248/test.xml -testplan
sh ./startCmd.sh ../tutorial/msrp/test.xml -testplan
sh ./startCmd.sh ../tutorial/snmp/test.xml -testplan
cd ../..

read


