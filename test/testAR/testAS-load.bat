echo off

REM run the unit tests
cd ..\bin
echo NE PAS ARRETER SVP
echo TEST EN ATTENTE DE LANCEMENT
ping 0.0.0.0 -n 16200 > null
echo LANCEMENT EN COURS
call startCmd.bat ..\testAS\test-orig.xml -load -reportdir:../testAS/reports/test-orig
echo FIN DU TEST

pause
