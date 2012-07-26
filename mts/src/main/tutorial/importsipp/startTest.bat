cd ..\..\bin
call importsipp.bat ..\tutorial\importsipp\uac.xml ..\tutorial\importsipp\out\uac_mts.xml ..\tutorial\importsipp\out\test.xml test_ua
call importsipp.bat ..\tutorial\importsipp\uas.xml ..\tutorial\importsipp\out\uas_mts.xml ..\tutorial\importsipp\out\test.xml test_ua
call importsipp.bat ..\tutorial\importsipp\branchc.xml ..\tutorial\importsipp\out\branchc_mts.xml ..\tutorial\importsipp\out\test.xml test_branch
call importsipp.bat ..\tutorial\importsipp\branchs.xml ..\tutorial\importsipp\out\branchs_mts.xml ..\tutorial\importsipp\out\test.xml test_branch

call importsipp.bat ..\tutorial\importsipp\3pcc-C-A.xml ..\tutorial\importsipp\out\3pcc-C-A_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-A.xml ..\tutorial\importsipp\out\3pcc-A_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-C-B.xml ..\tutorial\importsipp\out\3pcc-C-B_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-B.xml ..\tutorial\importsipp\out\3pcc-B_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc

call startcmd.bat ..\tutorial\importsipp\out\test.xml -seq
pause