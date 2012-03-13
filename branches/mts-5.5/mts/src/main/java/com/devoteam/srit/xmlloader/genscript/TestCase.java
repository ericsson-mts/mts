/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.genscript;

import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

/**
 * Represent a generated testcase
 * @author bthou
 */
public class TestCase {
    
    String name;
    String description;
    String state = "false";
    
    List<Scenario> listScenario;
    
    Element testcase;
    
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    
    public String  getState() { return state; }
    public void setState(String  s) { state = s; }
    
    public Element getTestCase() {
        if(testcase == null){
            testcase = new DOMElement("testcase");
            if(getName() != null){
                testcase.addAttribute("name", getName());
            }
            if(getDescription() != null){
                testcase.addAttribute("description", getDescription());
            }
            if(getState() != null){
                testcase.addAttribute("state", getState());
            }
            
            for(Scenario sc : listScenario){
                testcase.add(sc.toXmlElement());
            }
        }
        return testcase; 
    }
    public void setTestCase(Element tc) { testcase = tc; }   
    // ------------------
    
    // Constructeur
    public TestCase(String n, String d, String s){
        name = n;
        description = d;
        state = s;
        
        listScenario = new ArrayList();
    }
    
    // Methode pour ajouter un testcase au test
    public void addScenario(Scenario sc){
        listScenario.add(sc);
    }
    
    // Methode pour connaitre le nombre de scenario de ce testcase
    public int nbScenario(){
        return listScenario.size();
    }    
    
    // Retourne l'élément testcase
    public Element toXmlElement(){
        
        testcase = getTestCase();
        
        testcase.clearContent();
        
        // Ajout des paramètres du testcase
        List<Element> params = ParamGenerator.getInstance().paramTestcaseToXml();
        for(Element p : params){            
            testcase.add(p);
        }
        
        // Ajout des scenario
        for(Scenario sc : listScenario){
            testcase.add(sc.toXmlElement());
            
        }

        return getTestCase();
    }
    
}
    
    
