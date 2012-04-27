/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.genscript;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

/**
 * Represent a generated scenario
 * @author bthou
 */
public class Scenario {
    
    String name;
    String path;
    
    Long lastTimestamp;
    
    List<Message> listeMessages;
    Map<String, ListenPoint> mapListenPoint;
    List<FiltreGenerator> listeFiltres;    
        
    Element scenario;
    String scenarioStr;        
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String n) { name = n; }        
    
    public String getPath() { return path; }
    public void setPath(String p) { path = p; }   
    
    public Element getScenario() { return scenario; }
    public void setScenario(Element s) { scenario = s; }
    // ------------------
    
    // Constructeur
    public Scenario(String n, String p, List<FiltreGenerator> f){
        name = n;
        path = p;
        listeMessages = new ArrayList();
        mapListenPoint = new HashMap();
        listeFiltres = f;
        generateListenPoint();        
    }
    
    // Methode pour générer les listenpoints
    public void generateListenPoint(){
        // Pour chaque filtre utilisé dans ce scénario
        for(FiltreGenerator filtre : listeFiltres){
            // On récupère le port par défault dans le fichier de configuration du protocole
            String defaultPort = Config.getConfigByName(filtre.getProtocole().toLowerCase()+".properties").getString("listenpoint.LOCAL_PORT", "");
            
            // Si le port utilisé n'est pas celui par défault
            if(!defaultPort.equals(filtre.getHostPort().toString())){
                // On creait un listenpoint
                if(!filtre.getProtocole().equals("DIAMETER")){
                    mapListenPoint.put(filtre.getHostPort().toString(), new ListenPoint(filtre.getProtocole(), "listenPoint"+filtre.getHostPort().toString(), filtre.getHostName(), filtre.getHostPort().toString(), null, null, null));
                }
            }
        }
    }
    
     // Methode de génération d'un message
    public void generateMsg(Message message) throws Exception {        
        // On récupère le listenpoint eventuel
        ListenPoint lp = mapListenPoint.get(message.getSrcPort());
        // On génére le message à envoyer
        String lpName = null;
        if(lp != null){ 
            lpName = lp.getName(); 
        }

        message.setListenPoint(lpName);

        listeMessages.add(message);
    }
    
    // Methodes de comparaison avec des filtres
    public FiltreGenerator support(FiltreGenerator fg){
        FiltreGenerator retour = null;
        for(FiltreGenerator f : listeFiltres){
            if(f.getProtocole().contains(fg.getProtocole()) && f.getHostName().equals(fg.getHostName()) && f.getHostPort().toString().equals(fg.getHostPort().toString())){
                retour = f;
            }
        }
        return retour;
    }
    
    
    // Methode qui retourne l'élément String correspond au scenario
    public String toXml() throws Exception{
        if(scenarioStr == null){
            scenarioStr = "";
            scenarioStr += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+System.getProperty("line.separator");
            scenarioStr += "<scenario>"+System.getProperty("line.separator");
            
            // Insertion des listenpoints
            String allListenPoint = "";
            
            Msg firstMsg = null;
            if (listeMessages.size() > 0)
            {
            	firstMsg = listeMessages.get(0).getMsgSrc();
            }
            
            // Mise en place des listenpoints
            Set<String> mapListenPointKey = mapListenPoint.keySet();
            Iterator i = mapListenPointKey.iterator();
            while(i.hasNext()){
                ListenPoint lp = (ListenPoint) mapListenPoint.get(i.next());
                System.out.println("Generate <createListenpointPPP> " + lp);
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate <createListenpointPPP> " + lp);
                allListenPoint += ParamGenerator.getInstance().applyParam(lp.getPPP(), lp.createToXml(), firstMsg);
            }

            // Remise dans l'ordre des messages du scenario
            Collections.sort(listeMessages);

            String allMsg = "";
            // Insertion des messages dans le scenario
            for(Message message : listeMessages){

                // GESTION DES PAUSES
                if(lastTimestamp == null){
                    lastTimestamp = message.getTimestamp();
                }
                // Calcul de la différence
                Long difference = message.getTimestamp() - lastTimestamp;
                Float differenceInSeconds = difference.floatValue()/1000000;

                // Seuil à partir duquel on génère une pause
                if(differenceInSeconds >= 0.001){
                    Pause pause = new Pause("pause", differenceInSeconds);
                    // System.out.println("Generate : <pause> " + pause);
                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate : <pause> " + pause);
                    allMsg += pause.toXml();
                }

                lastTimestamp = message.getTimestamp();            

                // GESTION DES MESSAGES XML TAG wiuthout <parameters>
                String xml = message.toXml();
                // GESTION DES PARAMETRES         
                String xmlWithParam = ParamGenerator.getInstance().applyParam(message.getPPP(), xml, message.getMsgSrc());
                                
                String msgSummary = message.toShortString();
                int pos = msgSummary.indexOf("\n");
                msgSummary = msgSummary.substring(0, pos);
                if (message.getEnvoi())
                {
                	System.out.println("Generate <sendMessagePPP> " + msgSummary);
                	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate <sendMessagePPP>\n", xmlWithParam);
                }
                else
                {
                	System.out.println("Generate <receiveMessagePPP> " + msgSummary);
                	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate <receiveMessagePPP\n> ", xmlWithParam);
                }
                
                allMsg += xmlWithParam; 
            }


            scenarioStr += ParamGenerator.getInstance().paramScenarioToXml();
            scenarioStr += System.getProperty("line.separator");

            scenarioStr += allListenPoint;
            scenarioStr += System.getProperty("line.separator");
            scenarioStr += allMsg;


            // Fermeture des listenpoints ouverts
            Set<String> mapListenPointKeyClose = mapListenPoint.keySet();
            Iterator ic = mapListenPointKeyClose.iterator();        
            while(ic.hasNext()){
                ListenPoint lp = (ListenPoint) mapListenPoint.get(ic.next());
                System.out.println("Generate <removeListenpointPPP> ");
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Generate <removeListenpointPPP> ");
                scenarioStr += ParamGenerator.getInstance().applyParam(lp.getPPP(),lp.closeToXml(), firstMsg);
            }
            
            scenarioStr += "</scenario>"+System.getProperty("line.separator");
        }
        return scenarioStr;
    }
    
    // Methode qui retourne l'élément xml dom4j correspond au scenario
    public Element toXmlElement(){
        if(getScenario() == null){            
            scenario = new DOMElement("scenario");
        
            if(getName() != null){
                scenario.addAttribute("name", getName());
            }
            if(getPath() != null){
                scenario.setText(getPath());
            }
        }        
        return getScenario();
    }  
    
}
