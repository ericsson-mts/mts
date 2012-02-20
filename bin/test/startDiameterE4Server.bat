echo off

REM run the unit tests
cd ..
call startCmd.bat ..\tutorial\diameter\941_e4\e4.xml -sequential -reportdir:..\reports\diameter\941_e4
