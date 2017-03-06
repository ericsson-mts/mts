@echo off

rem keep mts/bin as the running dir

set CLASS=%1
set MODE=%2

rem : remove two first arguments (will not be passed to java)
shift
shift

rem : copy all arguments to ARG variable (done to pass correctly all arguments to java even if number of arguments is greater than 9) (problem with wildcard %* that ignores shift command)
set ARGS=
:LOOP
  if "%1"=="" goto :DONE
  set ARGS=%ARGS% %1
  shift
  goto :LOOP
:DONE

set /p JAVA_HOME=< java_home
set /p JAVA_MEMORY=< java_memory
set /p JAVA_ARGUMENTS=< java_arguments

if %MODE% == fork (
start "" "%JAVA_HOME%\javaw" -Xmx%JAVA_MEMORY%m %JAVA_ARGUMENTS% %CLASS% %ARGS% > ..\logs\start.log 2>&1
) else (
"%JAVA_HOME%\java" -Xmx%JAVA_MEMORY%m %JAVA_ARGUMENTS% %CLASS% %ARGS% > ..\logs\start.log 2>&1
)