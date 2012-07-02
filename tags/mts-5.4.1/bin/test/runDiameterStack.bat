echo off

cd ..

call setEnv.bat
set CLASSPATH=%CLASSPATH%;../build/classes/
set CLASSPATH=%CLASSPATH%;../3pp/Diameter-0.9.6.4/Diameter-0.9.6.4.jar
set CLASSPATH=%CLASSPATH%;../3pp/jain-sip1.2/JainSipApi1.2.jar
set CLASSPATH=%CLASSPATH%;../3pp/jain-sip1.2/JainSipRi1.2.jar
set CLASSPATH=%CLASSPATH%;../3pp/httpdcore-4.0/jakarta-httpcore-4.0-alpha4.jar

echo **** Start Diameter stack ***************************************
"%NGN_JAVA_HOME%\bin\java" -classpath %CLASSPATH%;../lib/junit.jar junit.textui.TestRunner com.devoteam.srit.xmlloader.diameter.test.DiameterManagerTest

pause
