Ce fichier décrit les différents fichiers suceptibles d'être modifiés, qui
composent une "configuration".

Pour sélectionner une "configuration" il faut venir éditer l'attribut "home" de
la balise <test> dans le fichier scripts/test.xml et y indiquer le chemin vers
le répertoire contenant la configuration.

* flux.properties

    Ce fichier est à la base. Il contient les informations réseaux (hosts,ports)
    ainsi que différents paramètres communs à tous les flux.

    Il contient également les chemins vers la liste des IP sources et la liste
    des profils.

* config_source_ip_list.csv (dans l'exemple)

    Ce fichier contient la liste des addresses IP source qui seront partagées
    entre les différents profils. Ce fichier est un CSV d'une seule colonne.

* config_profils.csv (dans l'exemple)

    Ce fichier contient la liste des profils qui se partageront les addresses
    IP du fichier config_source_ip_list.csv. Chaque ligne correspond à un profil
    qui est caractérisé par différents paramètres (informations de ratio, de
    résultat attendu...).
    
    Chaque profil (donc chaque ligne) contient un chemin vers un fichier qui
    représentera une liste de MSISDN
    
* profils/profil_dev.csv (dans l'exemple)
    
    Ce fichier contient une liste des abonnés qui appartiennent à un profil.
    
    Chaque ligne de cette liste représente un abonné caractérisé par son MSISDN
    et quelques autres paramètres.
    
* tester.properties

    Ce fichier sert à paramétrer la durée du test soit part une durée, soit par
    un nombre d'exécutions.
    
    * runner.TEST_DURATION 
        
        Ce paramètre correspond à la durée (en secondes) d'exécution du test.
        Lorsque, lors de l'exécution, la totalité du profil a été jouée, alors
        on recommence à zero (le prochain MSISDN qui sera joué sera le premier
        du profil).
        
    * runner.TEST_EXECUTIONS

        Ce paramètre correspond au nombre d'exécutions à réaliser. Une exécution
        d'un test correspond à "jouer" un MSISDN de chaque profil. Ce paramètre
        sert à controler plus finement le nombre de MSISDN qui sera joué. Si par
        exemple chaque profil contient 1000 abonnés. En donnant la valeur 1000 à
        ce paramètre nous jouerons chaque profil, en entier, une fois.                 