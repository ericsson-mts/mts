del /F /Q ..\java\com\devoteam\srit\xmlloader\sigtran\ap\generated\tcap\*
call binaryNotes/bin/bncompiler.cmd -m java -o ../java/com/devoteam/srit/xmlloader/sigtran/ap/generated/tcap -ns com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap -mp binaryNotes/bin/modules -f ../conf/sigtran/tcap/tcap.asn
call binaryNotes/bin/bncompiler.cmd -m java -o ../java/com/devoteam/srit/xmlloader/sigtran/ap/generated/tcap -ns com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap -mp binaryNotes/bin/modules -f ../conf/sigtran/tcap/DialoguePDUs.asn
call binaryNotes/bin/bncompiler.cmd -m java -o ../java/com/devoteam/srit/xmlloader/sigtran/ap/generated/tcap -ns com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap -mp binaryNotes/bin./modules -f ../conf/sigtran/tcap/UnidialoguePDUs.asn
rem call binaryNotes/bin/bncompiler.cmd -m java -o ../java/com/devoteam/srit/xmlloader/sigtran/ap/generated/tcap -ns com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap -mp binaryNotes/bin/modules -f ../conf/sigtran/tcap/TCAPMessages.asn
rem call binaryNotes/bin/bncompiler.cmd -m java -o ../java/com/devoteam/srit/xmlloader/sigtran/ap/generated/tcap -ns com.devoteam.srit.xmlloader.sigtran.ap.generated.tcap -mp binaryNotes/bin/modules -f ../conf/sigtran/tcap/TC-Notation-Extensions.asn

javac -cp output/;../../lib/java/binarynotes.jar ./output/ldapv3/*.java



