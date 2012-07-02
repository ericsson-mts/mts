echo off

REM run the unit tests
cd ..
call startCmd.bat ..\tutorial\diameter\942_e2\e2.xml -sequential -reportdir:..\reports\diameter\942_e2
