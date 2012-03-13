/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.genscript;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton for catch information from genscript config file
 * @author bthou
 */
public class ConfigParam {

    private static ConfigParam instance = null;
    private Map<String, List<Param>> parametresMap;
    private String repertoireConfigParam = "../conf/genscript/";

    // Constructeur
    private ConfigParam() {
    	File filePathConf = null;
    	try {
            parametresMap = new HashMap<String, List<Param>>();
            //URI pathConf = URIFactory.newURI(repertoireConfigParam);
            filePathConf = new File(repertoireConfigParam);
        }
        catch (Exception ex) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Error configuration file for generating script");
        }

    	String[] listeFichiersConf = filePathConf.list();
        for (String fichier : listeFichiersConf) {
            // Si le nom de fichier de configuration de type "paramSendClient"
            if (fichier.startsWith(Param.TARGET_SENDCLIENT) && fichier.endsWith(".csv")) {

                // On recupere le nom du protocole
                String protocol = fichier.replace(Param.TARGET_SENDCLIENT, "").replace(".csv", "");
                readParamSend(protocol, fichier, Param.TARGET_SENDCLIENT);
            }
            // Si le nom de fichier de configuration de type "paramSendServer"
            if (fichier.startsWith(Param.TARGET_SENDSERVER) && fichier.endsWith(".csv")) {

                // On recupere le nom du protocole
                String protocol = fichier.replace(Param.TARGET_SENDSERVER, "").replace(".csv", "");
                readParamSend(protocol, fichier, Param.TARGET_SENDSERVER);
            }
            // Si c'est un fichier de configuration de message de type "paramRecClient"
            else if (fichier.contains(Param.TARGET_RECCLIENT) && fichier.endsWith(".csv")) {
                // On recupere le nom du protocole
                String protocol = fichier.replace(Param.TARGET_RECCLIENT, "").replace(".csv", "");
                readParamRec(protocol, fichier, Param.TARGET_RECCLIENT);
            }
            // Si c'est un fichier de configuration de message de type "paramRecServer"
            else if (fichier.contains(Param.TARGET_RECSERVER) && fichier.endsWith(".csv")) {
                // On recupere le nom du protocole
                String protocol = fichier.replace(Param.TARGET_RECSERVER, "").replace(".csv", "");
                readParamRec(protocol, fichier, Param.TARGET_RECSERVER);
            }

        }
    }

    // Lecture des parametres de config de type ParamSendPPP
    private void readParamSend(String protocol, String filename, String target) {    
	    try {
		    List<Param> liste = parametresMap.get(protocol);
		
		    if (liste == null) {
		        // On l'enregistre dans la Map
		        parametresMap.put(protocol, new ArrayList());
		    }
		
		    // On lit le fichier
		    InputStream ips = new FileInputStream(repertoireConfigParam + filename);
		    InputStreamReader ipsr = new InputStreamReader(ips);
		    BufferedReader br = new BufferedReader(ipsr);
		    String ligne;
		    while ((ligne = br.readLine()) != null) {
		        String[] elements = ligne.split(";");
		
		        if (elements.length > 2) {
		            parametresMap.get(protocol).add(new Param(elements[0], elements[1], elements[2], target));
		        }
		
		    }
		    br.close();
		}
		catch (Exception ex) {
		    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Error configuration file for generating script");
            ex.printStackTrace();
            System.exit(-20);
		}
    }

    // Lecture des parametres de config de type ParamRecPPP
    private void readParamRec(String protocol, String filename, String target) {
	    try {
		    List<Param> liste = parametresMap.get(protocol);
		
		    if (liste == null) {
		        liste = parametresMap.put(protocol, new ArrayList());
		    }
		
		    // On lit le fichier
		    InputStream ips = new FileInputStream(repertoireConfigParam + filename);
		    InputStreamReader ipsr = new InputStreamReader(ips);
		    BufferedReader br = new BufferedReader(ipsr);
		    String ligne;
		    while ((ligne = br.readLine()) != null) {
		        String[] elements = ligne.split(";");
		
		        if (elements.length > 1) {
		            Param p = new Param(elements[0], elements[1], null, target);
		            p.setRemplacedValue("");
		            parametresMap.get(protocol).add(p);
		            ParamGenerator.getInstance().recordParam(p);
		        }
		    }
		    br.close();
		}
		catch (Exception ex) {
		    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Error configuration file for generating script");
		}
    }
    
    // Methode d'acces au singleton
    public static ConfigParam getInstance() {
        if (instance == null) {
            instance = new ConfigParam();
        }
        return instance;
    }

    public List<Param> getParam(String protocole) {
        return parametresMap.get(protocole);
    }
}
