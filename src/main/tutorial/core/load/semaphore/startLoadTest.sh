cd ../../../bin

rm -rf ../reports

./startCmd.sh sema_10 -L ../tutorial/charge/semaphore/testcases-10.xml

./startCmd.sh sema_100 -L ../tutorial/charge/semaphore/testcases-100.xml

./startCmd.sh sema_1000 -L ../tutorial/charge/semaphore/testcases-1000.xml

read