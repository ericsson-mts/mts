@echo off

call setEnv.bat

set ARGS=
:LOOP
  if "%1"=="" goto :DONE
  set ARGS=%ARGS% %1
  shift
  goto :LOOP
:DONE

"%NGN_JAVA_HOME%\bin\java" -Dfile.encoding=ISO-8859-15 -Xss128k -Xmx%MEMORY%m -XX:+UseConcMarkSweepGC com.devoteam.srit.xmlloader.genscript.genscriptCmd %ARGS%
@echo off