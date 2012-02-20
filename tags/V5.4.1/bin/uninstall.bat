@echo off

REM set classpath
call setEnv.bat

@echo on
REM start the uninstaller
"%NGN_JAVA_HOME%\bin\java" -jar ../Uninstaller/uninstaller.jar
@echo off
