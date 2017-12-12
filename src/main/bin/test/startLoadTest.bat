
cd ..

rmdir /S/Q ..\reports

call startCmd.bat ..\tutorial\sip\load\invite\test-load.xml -load

call startCmd.bat ..\tutorial\diameter\load\test-load.xml -load

pause
