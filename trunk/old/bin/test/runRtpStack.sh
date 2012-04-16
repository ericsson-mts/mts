#!/bin/sh

pwd
cd ..
pwd
. ./setEnv.sh

echo **** Start Benchmark ***************************************
/usr/bin/java com.devoteam.srit.xmlloader.rtp.test.RtpManagerTest

pause

