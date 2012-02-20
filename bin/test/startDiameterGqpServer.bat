echo off

REM run the unit tests
cd ..
call startCmd.bat ..\tutorial\diameter\944_gqp\Gqp.xml -sequential -reportdir:..\reports\diameter\944_Gqp
