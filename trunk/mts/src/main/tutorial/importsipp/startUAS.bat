cd ..\..\bin

del ..\conf\importsipp\out\test_uas.xml
call importsipp.bat -sippfile ..\conf\importsipp\uas.xml -testfile ..\conf\importsipp\out\test_uas.xml -testcase test_uas
del ..\conf\importsipp\out\test_uac.xml
call importsipp.bat -sippfile ..\conf\importsipp\uac.xml -testfile ..\conf\importsipp\out\test_uac.xml -testcase test_uac

call startSipP.bat -sf ..\conf\importsipp\out\uas_mts.xml -cp 7060

pause
