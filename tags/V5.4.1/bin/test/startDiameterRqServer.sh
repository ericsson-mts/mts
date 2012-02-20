#!/bin/sh

echo "Run the unit tests"
cd ..
sh ./startCmd.sh ../tutorial/diameter/943_rq/Rq.xml -sequential -reportdir:../reports/diameter/943_rq
