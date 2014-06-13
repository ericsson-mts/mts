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

Compatibilité avec MTS : 
MTS.5.8.4 et suivantes.

Routage interne dans MTS :
Le routage par défault des réponses est utilisé : ce qui veut dire que les 
réponses sont dispatchées au scenario qui a émis la requête correspondante.
(route.DEFAULT_RESPONSE = true)
Le routage par défault des requêtes subséquente est utilisé : ce qui veut dire que les 
requètes subséquentes sont dispatchées au scenario qui a traité la requête initiale 
(route.DEFAULT_SUBSEQUENT = true)
correspondante (nouvelle fonctionnalité de la 5.8.4).
Le routage des requêtes initiales est effectué en utilisant le mécasnisme du scenarioRouting
cad lorsqu'un message arrive, on évalue les différentes formules configuées par le paramètre : 
route.SCENARIO_NAME=avp.8.value,header.command|avp.8.value,message.protocol
et on recherche le scenario qui a le même nom. Ceci nous permet de discriminer 
les côtés originationing et terminating en se basant sur l'AVP 8=Framed-IP-Address.

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
