####
#### dans une ubuntu / debian
####

http://www.kamailio.org/w/2010/06/debian-and-ubuntu-apt-repositories/

apt-get install kamailio kamailio-mysql-modules mysql-server


####
#### dans kamailio.cfg, ajouter / modifier (éventuellement écraser par celui présent dans le dossier : attention ne marche pas toujours) :
####

#!define WITH_AUTH
#!define WITH_MYSQL

...

/* add local domain aliases */
alias="sip.france.fr"

...


####
#### éditer / remplacer kamctl (cf celui présent dans le dossier)
####



####
#### initialiser la base
####
kamdbctl create

####
#### ajouter les users (cf script.sh)
####
kamctl add username password