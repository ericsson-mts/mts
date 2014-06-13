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

Compatibilité avec MTS : 
MTS.5.8.4 et suivantes.

Routage interne dans MTS :
Le routage par défault des réponses a été débrayé : ce qui veut dire que les 
réponses ne sont pas dispatchées au scenario qui a émis la requête correspondante.
(route.DEFAULT_RESPONSE = false)
Le routage par défault des requêtes subséquente a été débrayé : ce qui veut dire que les 
requètes subséquentes ne sont pas dispatchées au scenario qui a traité la requête initiale 
correspondante (nouvelle fonctionnalité de la 5.8.4).
(route.DEFAULT_SUBSEQUENT = false)
Le routage des requêtes initiales est effectué en utilisant le mécasnisme du scenarioRouting
cad lorsqu'un message arrive, on évalue les différentes formules configuées par le paramètre : 
route.SCENARIO_NAME=message.request,header.command
et on recherche le scenario qui a le même nom. Ceci nous permet de discriminer 
les requêtes et les reponses de chaque type de message.

Restriction de la pile DIAMETER DK :
* Les messages CER/CEA sont gérés intrinsèquement par la pile en se basant sur les 
paramètres de configuration capability.XXXX. Lorsque l'on créé un listenpoint DIAMETER 
on a la possibilité de spécifier certains AVPS des messages CER/CEA.
Mais seuls un certain nombre d'AVP peuvent être envoyés. 
* Idem pour les messages DPR/DPA avant la rupture de la connection TCP.
* Idem pour les messages DWR/DWA pour lesquels on peut configurer le temps entre 
2 requêtes (node.WATCHDOG_INTERVAL).
* Il est possible de débrayer donc d'assouplir les contrôles faits par la pile 
par rapport au CER/CEA aussi bien en émission qu'en réception par le paramètre
capability.CONTROL_VALIDITY.
* Il est possible de me pas envoyer les messages CER/CEA mais dans ce cas il me semble 
qu'il n'est pas possible de les envoyer dans le scenario 
(paramètre capability.AUTO_CER_CEA_ENABLE).
* Il est possible de me pas envoyer les messages CER/CEA mais dans ce cas il me semble 
qu'il n'est pas possible de les envoyer dans le scenario.
(paramètre capability.AUTO_DPR_DPA_ENABLE)

Bug de la pile DIAMETER DK :
Lorsque 2 connections TCP sont ouvertes depuis la même machine vers 2 listenpoints
dynamiques (<createLIstenpointDIAMETER>) alors il semblerait qu'il y ait des mélanges 
des confusions dans les structures internes de la pile qui empêchent l'émission 
d'un message. Dans ce cas de figure le problème peut être contourner en changeant 
l'ordre de creation des listenpoints.


Fabien HENRY => Ericsson Lannion
