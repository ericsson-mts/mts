cd ..\..\bin

del ..\conf\importsipp\out\test.xml

call importsipp.bat -sippfile ..\conf\importsipp\uas.xml -testfileName ..\conf\importsipp\test_uas.xml -testcase test_uas

call importsipp.bat -sippfile ..\conf\importsipp\uac.xml -testfileName ..\conf\importsipp\test_uac.xml -testcase test_uac

call startSipP.bat -sf ..\conf\importsipp\uas.xml -cp 7060

pause