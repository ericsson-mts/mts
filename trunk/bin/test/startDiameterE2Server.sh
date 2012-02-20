#!/bin/sh

echo "Run the unit tests"
cd ..
sh ./startCmd.sh ../tutorial/diameter/942_e2/e2.xml -sequential -reportdir:../reports/diameter/942_e2
