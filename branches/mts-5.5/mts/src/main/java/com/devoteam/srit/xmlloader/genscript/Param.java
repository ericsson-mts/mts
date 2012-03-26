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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;

/**
 * Reprensent a generated Param
 * @author bthou
 */
public class Param {
        
    // name of the <parameter> operation
    String name;
    
    // operation and its values of the <parameter> operation 
    String operation;
    String value;
    String value2;
    String value3;
    String editable;
    
    // rule to apply (REGEX, XPATH, PATHKEY) to replace the parameter into XML block
    String regle;
    
    // level of the parameter
    String remplacedValue;
    
    // flag to generate or not the XML line for the <parameter> operation
    boolean displayed = false;
    
    // level of the parameter to create
    static public String LEVEL_TEST     =  "test";
    static public String LEVEL_TESTCASE =  "testcase";
    static public String LEVEL_SCENARIO =  "scenario";
    
    String level = LEVEL_SCENARIO;       
    
    // XML tree of the <parameter> operation
    Element param;
    
    // target or scope of the parameter : "paramSendClient", "paramSendServer", "paramReceive"
    static public String TARGET_SENDCLIENT =  "paramSendClient";
    static public String TARGET_SENDSERVER =  "paramSendServer";
    static public String TARGET_RECCLIENT =  "paramRecClient";    
    static public String TARGET_RECSERVER =  "paramRecServer";
    
    String target;

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String n){ name = n; }
    
    public String getOperation(){ return operation; }
    public void setOperation(String o){ operation = o; }    
    
    public String getRegle(){ return regle; }               
    public void setRegle(String r){ regle = r; }
    
    public void setValue(String v){ value = v; }
    public String getValue(){ return value; }
    
    public void setValue2(String v2){ value2 = v2; }
    public String getValue2(){ return value2; }
    
    public void setValue3(String v3){ value3 = v3; }
    public String getValue3(){ return value3; }
    
    public void setEditable(String e){ editable = e; }
    public String getEditable(){ return editable; }

    public String getRemplacedValue() { return remplacedValue; }

    public void setRemplacedValue(String remplacedValue) {
        if(getOperation().equals("set")){
            setValue(remplacedValue);
        }
        this.remplacedValue = remplacedValue;
    }
    
    public boolean getDisplayed(){ return displayed; }    
    public void setDisplayed(boolean d){ displayed = d; }   
    
    public String getLevel(){ return  level; }
    public void setLevel(String l){ level = l; }
    
    public Element getParam(){ return  param; }
    public void setParam(Element p){ param = p; }
    // --------------------------------
    
    public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	
	// Constructeur
	public Param(String n, String l, String o, String r, String t) throws Exception{
		init(n, l, o, r, t);
    }

	// Constructeur
	private void init(String n, String l, String o, String r, String t) throws Exception{
        name = n;
        level = l;
        if ((!LEVEL_TEST.equalsIgnoreCase(level)) &&
        	(!LEVEL_TESTCASE.equalsIgnoreCase(level)) &&
        	(!LEVEL_SCENARIO.equalsIgnoreCase(level)))
        {        	
        	throw new Exception("Bad config parameter level : " + n);
        }
                   
        String[] operationTab = o.split(",");
        if(operationTab.length >= 1){
            operation = operationTab[0];
        }
        if(operationTab.length >= 2){
            value = operationTab[1];
        }
        if(operationTab.length >= 3){
            value2 = operationTab[2];
        }
        if(operationTab.length >= 4){
            value3 = operationTab[3];
        }
        
        regle = r;
        target = t;
    }

    public Param(String n, String o, String r, String t) throws Exception {
    	// cut the level and the name
    	int pos = n.indexOf(":");
        name = n;
        level = LEVEL_SCENARIO;
        if (pos >= 0)
        {
        	name= "[" + n.substring(pos + 1);
        	level = n.substring(1, pos);
        } 

        init(name, level, o, r, t);
    }
    
    // Methode pour cloner des parametres
    public Param clone() {
    	Param clone = null;
    	try 
    	{ 	    	
	        clone = new Param(getName(), getLevel(), getOperation(), getRegle(), getTarget());
	        clone.setValue(value);
	        clone.setValue2(value2);
	        clone.setValue3(value3);
	        clone.setEditable(editable);
	        clone.setDisplayed(displayed);
    	} 
    	catch (Exception e)
    	{
    		// does not happen
    	}
        return clone;
    }
    
    // Methode a appeler pour effectuer les substitution dans le message passé en paramètre
    public String applySubstitution(String text, Msg msg) throws Exception{
        
        String msgAvecParametres = "";
        
        if(getRegle() != null){
            String[] regexTab = getRegle().split("#");            

            // Si la regle est sous forme d'expression régulière
            if(regexTab[0].toUpperCase().contains("REGEX")){
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Replace parameter " + getRegle() + " => " + name);
                String[] regexRule = Arrays.copyOfRange(regexTab, 1, regexTab.length);
                msgAvecParametres = regexRemplace(regexRule, 0, text);               
            }
            // Si la regle est sous forme de xpath
            else if(regexTab[0].toUpperCase().contains("XPATH")){
                GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Replace parameter " + getRegle() + " => " + name);
                // Récupération des paramètres
                String xpathRule = regexTab[1];
                String attribut = regexTab[2];           

                attribut = attribut.replace("@", "");

                // Création de l'arbre DOM correspondant au message
                SAXReader reader = new SAXReader();
                try{
                    Document document = reader.read(new ByteArrayInputStream(text.getBytes("UTF-8")));
                    // Création de l'objet XPATH ) selectionner 
                    XPath xpath = new DefaultXPath(xpathRule);

                    // Récupération des noeuds correspondants
                    List<Node> nodes = xpath.selectNodes(document.getRootElement(), xpath);

                    // Pour chaque noeuds à modifier
                    Element aRemplacer = null;
                    for(Node n : nodes){
                        setRemplacedValue(n.getText());
                        if(n instanceof Element){
                            aRemplacer = (Element)n;
                        }
                        else{
                            aRemplacer = n.getParent();
                        }                    
                        String newValue = getName();
                        String oldValue = aRemplacer.attribute(attribut).getValue();
                        // On regarde si on est dans le cas de paramètres mixtes
                        if(regexTab.length > 3){
                            if(regexTab[3].equals("REGEX")){
                                setRemplacedValue(null);
                                String[] regexRule = Arrays.copyOfRange(regexTab, 4, regexTab.length);
                                newValue = regexRemplace(regexRule, 0, oldValue); 
                            }
                        }
                        aRemplacer.setAttributeValue(attribut, newValue);
                    }

                    // Convertion en chaîne de caractère de l'arbre DOM du message
                    msgAvecParametres = document.getRootElement().asXML();
                }
                catch(Exception e){

                }            
            }
            // si la règle est sous forme de pathkey
            else if(regexTab[0].toUpperCase().contains("PATHKEY")){
                
                String valeurARemplacer = null;
                // On récupère la valeur à remplacer
                String pathKeyWord = regexTab[1];
                if(msg != null){
                    Parameter valeurParamARemplacer = msg.getParameter(pathKeyWord);
                    if(valeurParamARemplacer.length() > 0){
                        valeurARemplacer = valeurParamARemplacer.get(0).toString();
                    }

                    // On remplace dans tout le message par le parametre
                    if(valeurARemplacer != null){
                        msgAvecParametres = text.replace(valeurARemplacer, getName());
                        if(!msgAvecParametres.equals(text)){
                            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Replace parameter " + valeurARemplacer + " => " + name);
                            setRemplacedValue(valeurARemplacer);
                        }
                    }
                }    
            }
        }
        // Si le message n'a pas subit de modification, on retourne null
        if(!isUsed()){
            return null;
        }
        
        // Sinon on retourne le message modifié avec les paramètres
        return msgAvecParametres;
    }
    
    // Methode effectuant les remplacements
    public String regexRemplace(String[] regexTab, Integer i, String str){
        // Si on arrive à la fin de la cascade d'expression régulière
        if(i == regexTab.length){
            // On sauvegarde la valeur que l'on remplace
            if(remplacedValue == null){
                setRemplacedValue(str);
            }
            // Si la valeur que l'on souhaite remplacer est bien la même
            if(remplacedValue.equals(str)){
                // On retourne le nom du paramètre
                return getName();
            }
            // Sinon
            else{
                // On ne remplace pas
                return str;
            }
        }
        
        Pattern pattern0 = Pattern.compile(regexTab[i]);
        Matcher m0 = pattern0.matcher(str);
        StringBuffer sb0 = new StringBuffer();
        while(m0.find()){
            MatchResult mr = m0.toMatchResult();
            String aRemplacer= mr.group();
            String remplacerPar = regexRemplace(regexTab, i+1, aRemplacer);
            m0.appendReplacement(sb0, remplacerPar);
        }
        m0.appendTail(sb0);
        return sb0.toString();
    }
    
    // Methode permettant de savoir si un paramètre est effectivement utilisé ou pas
    public boolean isUsed(){
        return (getRemplacedValue() != null && !getRemplacedValue().contains("[") && !getRemplacedValue().contains("]"));
    }
    
    // Methodes pour connaitre le niveau d'application du paramètre
    public boolean isLevelTest(){
        return getLevel().equals(LEVEL_TEST);
    }    
    
    public boolean isLevelTestcase(){
        return getLevel().equals(LEVEL_TESTCASE);
    }
    
    public boolean isLevelScenario(){
        return getLevel().equals(LEVEL_SCENARIO);
    }
    
    public boolean isTargetRec(){
        return getTarget().equals(TARGET_RECCLIENT) || getTarget().equals(TARGET_RECSERVER);
    }

    public boolean isTargetSend(){
        return getTarget().equals(TARGET_SENDCLIENT) || getTarget().equals(TARGET_SENDSERVER);
    }

    // Methodes pour définir le niveau d'application du paramètre
    public void setLevelTest(){
        setLevel(LEVEL_TEST);
    }

    public void setLevelTestcase(){
        setLevel(LEVEL_TESTCASE);
    }
    
    public void setLevelScenario(){
        setLevel(LEVEL_SCENARIO);
    }
        
    // Methode retournant la famille du parametre
    public String getFamily(){
        return getName().replaceAll("_[0-9]+]", "]");
    }
    
    
    // Methode retournant la balise parameter
    public String toXml(){    	
        String parametre = "";
        if ((getOperation() != null) && (getOperation().length() > 0) &&
        	((getName().startsWith("[")) && (getName().endsWith("]")))) {       		
	        parametre += "  <parameter ";
	        if(getName() != null){
	            parametre += "name=\""+getName()+"\" ";            
	        }
	        if(getOperation() != null){
	            parametre += "operation=\""+getOperation()+"\" ";            
	        }
	        if(getValue() != null){
	            parametre += "value=\""+getValue()+"\" ";            
	        }
	        if(getValue2() != null){
	            parametre += "value2=\""+getValue2()+"\" ";            
	        }
	        if(getValue3() != null){
	            parametre += "value3=\""+getValue3()+"\" ";            
	        }
	        if(getEditable() != null){
	            parametre += "editable=\""+getEditable()+"\" ";            
	        }
	        parametre += "/>\n";
        }
        return parametre;
    }
    
    
    // Methode retournant le parametre comme element dom4j
    public Element toXmlElement(){
        if (getParam() == null) {
            param = new DOMElement("parameter");
            if(getName() != null){
                param.addAttribute("name", getName());
            }
            if(getOperation() != null){
                param.addAttribute("operation", getOperation());
            }
            if(getValue() != null){
                param.addAttribute("value", getValue());
            }
            if(getValue2() != null){
                param.addAttribute("value2", getValue2());
            }
            if(getValue3() != null){
                param.addAttribute("value3", getValue3());
            }
            if(getEditable() != null){
                param.addAttribute("editable", getEditable());
            }
        }
        
        return getParam();
    }
    
    public String toString(){
        return name + ";" + operation + ";" + regle; 
    }
    
}
