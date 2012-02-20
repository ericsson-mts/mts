#!/bin/sh

echo "Run the unit tests"
cd ..
sh ./startCmd.sh ../tutorial/diameter/941_e4/e4.xml -sequential -reportdir:../reports/diameter/941_e4
