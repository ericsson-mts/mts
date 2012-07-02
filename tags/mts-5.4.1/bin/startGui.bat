@echo off

REM set classpath
call setEnv.bat

REM start IMSLoader GUI module
@echo on
start "" "%NGN_JAVA_HOME%\bin\javaw" -Dfile.encoding=ISO-8859-15 -Xss128k -Xmx%MEMORY%m -XX:+UseConcMarkSweepGC com.devoteam.srit.xmlloader.gui.TesterGui %1
@echo off

REM -Xdebug -Xrunjdwp:transport=dt_socket,suspend=n,server=y,address=8888 for attach in debug
REM -noverify to not check class at run (useful for the sctp stack)

REM -XX:+UseConcMarkSweepGC option = less freezes due to garbage collector but lower performances (~20%)
REM but it can be used without problems if we have CPU time to spare

REM -Djava.net.preferIPv4Stack=true option = to use a large number of IP addresses 
REM (around 10000 ipaddresses)

REM wait for the user to press a key after IMSLoader ends
REM pause
