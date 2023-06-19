cd ../../../bin
rm -rf ../reports/aaa-100

./startCmd.sh ../tutorial/demos/5-diam-load/test-load.xml -load -genReport -showReport -param:TestDurationSec+60

read