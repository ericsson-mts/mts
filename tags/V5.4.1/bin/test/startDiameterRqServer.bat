echo off

REM run the unit tests
cd ..
call startCmd.bat ..\tutorial\diameter\943_rq\Rq.xml -sequential -reportdir:..\reports\diameter\943_Rq
