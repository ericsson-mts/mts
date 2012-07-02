
::Based on Wireshark pdml capture file this script generates  XML Scenarios that correspond to those traces. 

::Pour exporter dans Wireshark 
:: * Choisir dans le menu <file/export/file>
:: * Mettre un nom de fichier
:: * Dans la combo <Save file type>, choisir <PDML (XML packet detail)>
:: * Valider par le bouton <Save>

:: La commande à lancer est transfRTP.bat en se placant dans le répertoire bin.
:: Le nom du fichier à transformer est défini dans le fichier .bat dans la variable INPUTFILE.
:: Les fichiers générés sont dans le répertoire courant 

::Input file
SET INPUTFILE=./sounds/bonjourXMLLoader.pdml

:: You might want to adapt the output to your needs by setting the following parameters:

::set LCOALHOST to the local IP address(example SET LOCALHOST=localHOST=192.168.0.2)
SET LOCALHOST=
::set LOACALPORT for custom local port(example SET LOCALPORT=localPort=9898)
SET LOCALPORT=
::set REMOTEHOST for custom receiver's address (example: SET REMOTEHOST=remoteHost=192.168.0.3)
SET REMOTEHOST=
::set REMOTEPORT for custom receiver's port(example SET REMOTEPORT=remotePort=8989
SET REMOTEPORT=
::set SESSIONNAME for custom RTP session name(example: SET SESSIONNAME=sessionName=MyRTPSession, leave blank otherwise
SET SESSIONNAME=
:: set DESTSCENARIO  to perfom local tests (example: SET DESTSCENARIO=destScenario=MyScenario)
SET DESTSCENARIO=
::set RTPPAUSETIME  to fix the pause time between 2 RTP packets (example: SET RTPPAUSETIME=RTPPauseTime=20)
SET RTPPAUSETIME=

java -jar saxon8.jar %INPUTFILE% transfPdmlRTPSendPart.xsl %DESTSCENARIO% %RTPPAUSETIME% > RTPSendPart.xml
java -jar saxon8.jar %INPUTFILE% transfPdmlRTPScenario.xsl %DESTSCENARIO% > RTPScenario.xml
java -jar saxon8.jar %INPUTFILE% transfPdmlRTPScenarioReceiver.xsl  > RTPScenarioReceiver.xml
java -jar saxon8.jar %INPUTFILE% transfPdmlRTPTest.xsl %LOCALHOST% %LOCALPORT% %REMOTEHOST% %REMOTEPORT% %SESSIONNAME% %DESTSCENARIO% %RTPPAUSETIME% > test.xml
java -jar saxon8.jar %INPUTFILE% transfPdmlRTPCSV.xsl  > RTPSendPart.csv
java -jar saxon8.jar %INPUTFILE% transfPdmlRTPScenarioLoop.xsl %DESTSCENARIO% %RTPPAUSETIME% > RTPScenarioLoop.xml
pause