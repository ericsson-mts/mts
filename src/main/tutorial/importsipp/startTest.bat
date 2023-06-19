cd ..\..\bin

del ..\tutorial\importsipp\test.xml

call importsipp.bat -sippfile ..\tutorial\importsipp\uac.xml -testcase test_ua
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -testcase test_ua

call importsipp.bat -sippfile ..\tutorial\importsipp\branchc.xml -testcase test_branch
call importsipp.bat -sippfile ..\tutorial\importsipp\branchs.xml -testcase test_branch

call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-C-A.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-A.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-C-B.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-B.xml -testcase test_3pcc

call importsipp.bat -sippfile ..\tutorial\importsipp\uac_pcap.xml -testcase uapcap
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -testcase uapcap

call importsipp.bat -sippfile ..\tutorial\importsipp\regexp.xml -testcase regexp
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -testcase regexp

call importsipp.bat -sippfile ..\tutorial\importsipp\uac.xml -testcase ooc
call importsipp.bat -sippfile ..\tutorial\importsipp\ooc_default.xml -testcase ooc

call startcmd.bat ..\tutorial\importsipp\test.xml -seq
pause