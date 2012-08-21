cd ..\..\bin
call importsipp.bat -sippfile ..\tutorial\importsipp\uac.xml -result ..\tutorial\importsipp\out\uac_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_ua
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -result ..\tutorial\importsipp\out\uas_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_ua

call importsipp.bat -sippfile ..\tutorial\importsipp\branchc.xml -result ..\tutorial\importsipp\out\branchc_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_branch
call importsipp.bat -sippfile ..\tutorial\importsipp\branchs.xml -result ..\tutorial\importsipp\out\branchs_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_branch

call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-C-A.xml -result ..\tutorial\importsipp\out\3pcc-C-A_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-A.xml -result ..\tutorial\importsipp\out\3pcc-A_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-C-B.xml -result ..\tutorial\importsipp\out\3pcc-C-B_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_3pcc
call importsipp.bat -sippfile ..\tutorial\importsipp\3pcc-B.xml -result ..\tutorial\importsipp\out\3pcc-B_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase test_3pcc

call importsipp.bat -sippfile ..\tutorial\importsipp\uac_pcap.xml -result ..\tutorial\importsipp\out\uac_pcap_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase uapcap
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -result ..\tutorial\importsipp\out\uas_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase uapcap

call importsipp.bat -sippfile ..\tutorial\importsipp\regexp.xml -result ..\tutorial\importsipp\out\regexp_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase regexp
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -result ..\tutorial\importsipp\out\uas_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase regexp


call importsipp.bat -sippfile ..\tutorial\importsipp\ooc_default.xml -result ..\tutorial\importsipp\out\ooc_default_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase ooc
call importsipp.bat -sippfile ..\tutorial\importsipp\uas.xml -result ..\tutorial\importsipp\out\uas_mts.xml -testfile ..\tutorial\importsipp\out\test.xml -testcase ooc

call startcmd.bat ..\tutorial\importsipp\out\test.xml -seq
pause