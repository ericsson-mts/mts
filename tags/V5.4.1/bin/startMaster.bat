@echo off

REM set classpath
call setEnv.bat

@echo on
REM IMSLoader master module GUI
start "" "%NGN_JAVA_HOME%\bin\javaw" -Dfile.encoding=ISO-8859-15 -Xss128k -Xmx200m com.devoteam.srit.xmlloader.master.MasterImplementation
@echo off

REM Wait for the user to press a key after XML Loader ended
REM pause
