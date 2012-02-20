#!/bin/sh

cd ..

rm -rf ../reports

./startCmd.sh load_100 ../tutorial/sip/charge/testcases-100.xml -Rrampload

./startCmd.sh aaa_100 ../tutorial/aaa/charge/testcases-100.xml -Rrampload

./startCmd.sh sema_100 ../tutorial/core/charge/semaphore/testcases-100.xml -Rrampload

./startCmd.sh empty_100 ../tutorial/core/charge/empty/testcases-100.xml -Rrampload


read
