cd ..\..\..\bin

rmdir /S/Q ..\reports

call startCmd.bat ..\tutorial\diameter\charge\test-client-10.xml -load -reportdir:..\reports\diameter-10 -duration:15

call startCmd.bat ..\tutorial\diameter\charge\test-client-100.xml -load -reportdir:..\reports\diameter-100 -duration:15

call startCmd.bat ..\tutorial\diameter\charge\test-client-1000.xml -load -reportdir:..\reports\diameter-1000 -duration:15

pause