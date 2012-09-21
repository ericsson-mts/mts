#!/bin/sh

cd ..

rm -rf ../reports

./startCmd.sh ../tutorial/core/test.xml -sequential
