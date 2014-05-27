Mode d'emploi pour le lancement des tests fonctionnels :


Ouvrir l'un des fichiers de test suivants :
=> simulFuncRX/testFunc.xml (interface RX entre AF et PRCF) ou 
=> simulFuncGQ/testFunc.xml (interface GQ entre AF et SPDF)


Paramètres importants (editables):
hostAF => adresse IP de la fonction AF pour le protocole DIAMETER 
          (si simulée par MTS alors adresse locale)
hostAF => port de la fonction AF pour le protocole DIAMETER
simulAF => flag pour simuler (valeur true)ou non (valeur false) le côté AF
realmAF => nom de domaine (=royaume" du côté originating) pour le AF function

hostPCRF => adresse IP de la fonction PRCF pour le protocole DIAMETER 
          (si simulée par MTS alors adresse locale)
hostPCRF => port de la fonction PCRF pour le protocole DIAMETER
simulPCRF => flag pour simuler (valeur true)ou non (valeur false) le côté PCRF
realmPCRF => nom de domaine (=royaume" du côté originating) pour le PCRF function

originIPAddress => adresse IP du terminal du côté originating
termIPAddress => adresse IP du terminal du côté terminating

responseCode => response code to reply to requests
responseTime => response time (in seconds) for the transactions when sending a response
sessionTime => duration (in seconds) of the sessions

Procédure d'utilisation :
Lancer les testcases : RecAAR SendAAA + RecSTR SendSTA côté ORIGNINATING et TERMINATING
Lancer les registers 
Déclencher un appel
Envoyer un ASR par le test case : SendASR RecASA
Envoyer un RAR par le test case : SendRAR RecRAA
Terminer l'appel
