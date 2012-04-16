#!/bin/sh

cd ..

rm -rf ../reports

./startCmd.sh regression ../tutorial/test.xml -Rsequential
