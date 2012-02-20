@echo off

title imsloader.slave

REM set classpath
call setEnv.bat


@echo on
REM activate RMI logs: -Djava.rmi.server.logCalls=true
REM IMSLoader master module GUI
"%NGN_JAVA_HOME%\bin\java" -Dfile.encoding=ISO-8859-15 -Xss128k -Xmx%MEMORY%m -XX:+UseConcMarkSweepGC com.devoteam.srit.xmlloader.master.SlaveImplementation %1 %2 %3 %4 %5 %6 %7 %8 %9
@echo off

REM Wait for the user to press a key after XML Loader ended
pause
