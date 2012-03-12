@echo off

REM set classpath
set /p JAVA_HOME=< java_home

@echo on
REM start the uninstaller
"%JAVA_HOME%\bin\java" -jar ../Uninstaller/uninstaller.jar
@echo off
