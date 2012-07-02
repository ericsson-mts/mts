@echo off

REM reset the classpath
set CLASSPATH=

REM add to the classpath the contents of the file classpath 
for /f "delims=" %%a in (classpath) do call setEnvAppend.bat %%a

REM set the variable NGN_JAVA_HOME from the file java_home 
for /f "delims=" %%a in (java_home) do set NGN_JAVA_HOME=%%a
REM set the variable MEMORY from the file memory 
for /f "delims=" %%a in (memory) do set MEMORY=%%a

REM add to the classpath the jars of the folder modules
for %%i in (..\modules\*.jar) do call setEnvAppend.bat %%i

REM add to the classpath the jars of the folder lib
for %%i in (..\lib\*.jar) do call setEnvAppend.bat %%i


REM
REM Set JRE options (not used ATM)
REM
REM set GC_OPTIONS=-verbose:gc -Xloggc:../logs/gclog.log -XX:+PrintGCDetails
REM set DEBUG_OPTIONS=-Xdebug -Xrunjdwp:transport=dt_socket,address=3999,server=y,suspend=n

REM -XX:+UseConcMarkSweepGC option = less freezes due to garbage collector but lower performances (~20%)
REM it can be used without problems if we have CPU time to spare

REM -Djava.net.preferIPv4Stack=true option = to use a large number of IP addresses 
REM (around 10000 ipaddresses)

REM for wireshark network capture, there is a bug with some CPU (Athlon 64 X2) concerning the clock signal 
REM (specially with multiples processors) on Windows XP SP1-2. To go around this problem, please change the 
REM following parameter into the boot.ini file: 
REM ******\WINDOWS="Microsoft Windows XP Professionnel" /usepmtimer 
REM (to go there, go to the "Control Panel" panel; then choose the "System" windows; then click on the "Advanced button"
REM and finally click on the "Startup and Restore" button).
REM This trouble is solved normally in XP SP3, Windows 7 or new versions.
