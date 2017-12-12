echo off

REM run the unit tests
cd ..
call startCmd.bat ..\tutorial\tcp\901_eserv_server\test.xml -sequential -reportdir:..\reports\mdplugin_server
