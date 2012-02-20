
cd ..

rmdir /S/Q ..\reports

call startCmd_env.bat regression ../tutorial/test.xml -Rsequential

pause