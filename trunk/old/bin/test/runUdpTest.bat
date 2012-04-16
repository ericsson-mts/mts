echo off

cd ..

call setEnv.bat
set CLASSPATH=%CLASSPATH%;../build/classes/

echo **** Start UDP stack ***************************************
"%NGN_JAVA_HOME%\bin\java" -Xmx1000m com.devoteam.srit.xmlloader.udp.test.UdpTest

pause
