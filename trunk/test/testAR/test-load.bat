echo OFF

REM run the unit tests
cd ..\..\bin

call startCmd.bat ..\tutorial\core\test.xml -load -param:CAPS+60 -config:logs.MAXIMUM_LEVEL+ERROR -config:logs.STORAGE_LOCATION+1 -reportDir:D:\\temp\\report -genReport -showRep

cd ../..
pause