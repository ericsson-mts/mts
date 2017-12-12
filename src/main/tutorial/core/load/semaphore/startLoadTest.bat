cd ..\..\..\bin

rmdir /S/Q ..\reports

call startCmd.bat sema_10 -L ..\tutorial\charge\semaphore\testcases-10.xml

call startCmd.bat sema_100 -L ..\tutorial\charge\semaphore\testcases-100.xml

call startCmd.bat sema_1000 -L ..\tutorial\charge\semaphore\testcases-1000.xml

pause