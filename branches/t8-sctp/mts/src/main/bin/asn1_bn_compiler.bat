echo off
echo Process the ASN files in the directory ../conf/asn1/%1 : please confirm ?
pause

del /F /Q .\binaryNotes\java\com\devoteam\srit\xmlloader\sigtran\ap\%1
mkdir .\binaryNotes\java\com\devoteam\srit\xmlloader\sigtran\ap\%1

FOR %%i IN (../conf/asn1/%1/*.asn) do call binaryNotes/bin/bncompiler.cmd -m java -o ./binaryNotes/java/com/devoteam/srit/xmlloader/sigtran/ap/%1 -ns com.devoteam.srit.xmlloader.sigtran.ap.%1 -mp binaryNotes/bin/modules -f ../conf/asn1/%1/%%i

rem javac -cp output/;../../lib/java/binarynotes.jar ./output/ldapv3/*.java



