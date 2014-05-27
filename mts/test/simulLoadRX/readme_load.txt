Mode d'emploi pour le lancement des tests fonctionnels :


Ouvrir l'un des fichiers de test suivants :
=> simulLoadRX/testLoad.xml (interface RX entre AF et PRCF) ou 
=> simulLoadGQ/testLoad.xml (interface GQ entre AF et SPDF) TODO


Paramètres importants (editables):
hostAF => adresse IP de la fonction AF pour le protocole DIAMETER 
          (si simulée par MTS alors adresse locale)
hostAF => port de la fonction AF pour le protocole DIAMETER
realmAF => nom de domaine (=royaume" du côté originating) pour le AF function

hostPCRF => adresse IP de la fonction PRCF pour le protocole DIAMETER 
          (si simulée par MTS alors adresse locale)
hostPCRF => port de la fonction PCRF pour le protocole DIAMETER
realmPCRF => nom de domaine (=royaume" du côté originating) pour le PCRF function

originIPAddress => adresse IP du terminal du côté originating
termIPAddress => adresse IP du terminal du côté terminating

responseCode => response code to reply to requests
responseTime => response time (in seconds) for the transactions when sending a response
sessionTime => duration (in seconds) of the sessions

testDurationSec => duration (in seconds) of the test
speedAAR => Speed (in message per seconds) to send the RAR messages (for AF side)
speedRAR => Speed (in message per seconds) to send the RAR messages (for PCRF side) used in parallel mode ("Run parallel" menu item)
numberRAR => Number of the RAR messages to send used in manual mode ("Start" button)
speedASR => Speed (in message per seconds) to send the ASR messages (for PCRF side) used in parallel mode ("Run parallel" menu item)
numberASR => Number of the ASR messages to send used ion manual mode ("Start" button)

Procédure d'utilisation :
Lancer en mode manuel ('Start' button) ou en mode parallel le testcase (désélectionner les autres testcases) : Responder_recAAR_STR_001
Lancer les registers + appels
Consulter les compteurs de statistiques utilisateur : nombre de sessions actives et durée des session des 2 cotés : AF et PRCF

Vous avez les possibilités suivantes en cours de test :

1) Envoyer des messages ASR en BURST en lancant manuellement le testcase : Loader_SendASR 
vous pouvez spécifier le nombre de message à envoyer dans la case 'N'; 
la valeur par default est donnée par le paramètre [numberASR]

2) Envoyer des messages ASR en CONTINU en lancant en mode parallel le testcase : Loader_SendASR 
vous pouvez spécifier le nombre de message à envoyer dans la case 'N'; 
la valeur par default est donnée par le paramètre [numberASR]

3) Envoyer des messages RAR en BURST en lancant manuellement le testcase : Loader_SendRAR 
vous pouvez spécifier le nombre de message à envoyer dans la case 'N'; 
la valeur par default est donnée par le paramètre [numberRAR]

4) Envoyer des messages ASR en CONTINU en lancant en mode parallel le testcase : Loader_SendASR 
vous pouvez spécifier le flux de message RAR à envoyer par le paramètre [speedRAR]
