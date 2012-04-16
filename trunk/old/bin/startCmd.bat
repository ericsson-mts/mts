@echo off

REM set classpath
call setEnv.bat

set ARGS=
:LOOP
  if "%1"=="" goto :DONE
  set ARGS=%ARGS% %1
  shift
  goto :LOOP
:DONE

REM start IMSLoader CMD module
"%NGN_JAVA_HOME%\bin\java" -Dfile.encoding=ISO-8859-15 -Xss128k -Xmx%MEMORY%m -XX:+UseConcMarkSweepGC com.devoteam.srit.xmlloader.cmd.TextImplementation %ARGS%
@echo off

REM -noverify to not check class at run (useful for the sctp stack)
REM -XX:+UseConcMarkSweepGC option = less freezes due to garbage collector but lower performances (~20%)
REM but it can be used without problems if we have CPU time to spare

REM -Djava.net.preferIPv4Stack=true option = to use a large number of IP addresses 
REM (around 10000 ipaddresses)