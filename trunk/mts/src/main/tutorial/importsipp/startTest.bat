cd ..\..\bin

del ..\conf\importsipp\test.xml

call importsipp.bat -sippfile ..\conf\importsipp\uac.xml -testcase test_ua
call importsipp.bat -sippfile ..\conf\importsipp\uas.xml -testcase test_ua

call importsipp.bat -sippfile ..\conf\importsipp\branchc.xml -testcase test_branch
call importsipp.bat -sippfile ..\conf\importsipp\branchs.xml -testcase test_branch

call importsipp.bat -sippfile ..\conf\importsipp\3pcc-C-A.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\conf\importsipp\3pcc-A.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\conf\importsipp\3pcc-C-B.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\conf\importsipp\3pcc-B.xml -testcase test_3pcc

call importsipp.bat -sippfile ..\conf\importsipp\uac_pcap.xml -testcase uapcap
call importsipp.bat -sippfile ..\conf\importsipp\uas.xml -testcase uapcap

call importsipp.bat -sippfile ..\conf\importsipp\regexp.xml -testcase regexp
call importsipp.bat -sippfile ..\conf\importsipp\uas.xml -testcase regexp

call importsipp.bat -sippfile ..\conf\importsipp\uac.xml -testcase ooc
call importsipp.bat -sippfile ..\conf\importsipp\ooc_default.xml -testcase ooc

call startcmd.bat ..\conf\importsipp\test.xml -seq
pause