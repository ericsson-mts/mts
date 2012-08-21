cd ..\..\bin
call importsipp.bat ..\tutorial\importsipp\uac.xml ..\tutorial\importsipp\out\uac_mts.xml ..\tutorial\importsipp\out\test.xml test_ua
call importsipp.bat ..\tutorial\importsipp\uas.xml ..\tutorial\importsipp\out\uas_mts.xml ..\tutorial\importsipp\out\test.xml test_ua

call importsipp.bat ..\tutorial\importsipp\branchc.xml ..\tutorial\importsipp\out\branchc_mts.xml ..\tutorial\importsipp\out\test.xml test_branch
call importsipp.bat ..\tutorial\importsipp\branchs.xml ..\tutorial\importsipp\out\branchs_mts.xml ..\tutorial\importsipp\out\test.xml test_branch

call importsipp.bat ..\tutorial\importsipp\3pcc-C-A.xml ..\tutorial\importsipp\out\3pcc-C-A_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-A.xml ..\tutorial\importsipp\out\3pcc-A_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-C-B.xml ..\tutorial\importsipp\out\3pcc-C-B_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc
call importsipp.bat ..\tutorial\importsipp\3pcc-B.xml ..\tutorial\importsipp\out\3pcc-B_mts.xml ..\tutorial\importsipp\out\test.xml test_3pcc

call importsipp.bat ..\tutorial\importsipp\uac_pcap.xml ..\tutorial\importsipp\out\uac_pcap_mts.xml ..\tutorial\importsipp\out\test.xml uapcap
call importsipp.bat ..\tutorial\importsipp\uas.xml ..\tutorial\importsipp\out\uas_mts.xml ..\tutorial\importsipp\out\test.xml uapcap

call importsipp.bat ..\tutorial\importsipp\regexp.xml ..\tutorial\importsipp\out\regexp_mts.xml ..\tutorial\importsipp\out\test.xml regexp
call importsipp.bat ..\tutorial\importsipp\uas.xml ..\tutorial\importsipp\out\uas_mts.xml ..\tutorial\importsipp\out\test.xml regexp


call importsipp.bat ..\tutorial\importsipp\ooc_default.xml ..\tutorial\importsipp\out\ooc_default_mts.xml ..\tutorial\importsipp\out\test.xml ooc
call importsipp.bat ..\tutorial\importsipp\uas.xml ..\tutorial\importsipp\out\uas_mts.xml ..\tutorial\importsipp\out\test.xml ooc

call startcmd.bat ..\tutorial\importsipp\out\test.xml -seq
pause