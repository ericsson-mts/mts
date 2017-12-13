echo off
echo Compile the ASN classes and archive them in the file ../lib/BN_classes_%1.jar : please confirm ?
pause

del /F/Q binaryNotes\classes
mkdir binaryNotes\classes
del /F/Q ..\lib\BN_classes_%1.jar

set /p JDK_HOME=< jdk_home
rem echo %JDK_HOME%
  
rem "Compile classes"
"%JDK_HOME%\javac"  -cp binaryNotes\lib\java\binarynotes.jar -d binaryNotes\classes binaryNotes\java\com\devoteam\srit\xmlloader\sigtran\ap\%1\*.java

rem "Archive classes"
cd binaryNotes\classes
"%JDK_HOME%\jar" cvf BN_classes_%1.jar com\devoteam\srit\xmlloader\sigtran\ap\%1\*.class
cd ../.. 
copy binaryNotes\classes\BN_classes_%1.jar ..\lib
