/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.genscript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;

/**
 * Represent a generated Test
 * @author bthou
 */
public class Test {
    
    String name;
    String description;
    
    List<TestCase> listTestCase;
    
    Element test;
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    
    public String getDescription() { return description; }
    public void setDescription(String d) { description = d; }
    
    public Element getTest() { 
        if(test == null){            
            test = new DOMElement("test");
        
            if(getName() != null){
                test.addAttribute("name", getName());
            }
            if(getDescription() != null){
                test.addAttribute("description", getDescription());
            }            
        }
        return test; 
    }
    public void setTest(Element t) { test = t; }
    // ------------------
    
    // Constructeur pour un test
    public Test(String n, String d){
        name = n;
        description = d;
        
        listTestCase = new ArrayList();      
    }
    
    // Methode pour ajouter un testcase au test
    public void addTestCase(TestCase tc){
        listTestCase.add(tc);
    }
    
    // Methode qui retourne l'élément xml dom4j correspond au test
    public Element toXmlElement(){        
        test = getTest();

        Element newTest = new DOMElement("test");
        if(getName() != null){
            newTest.addAttribute("name", getName());
        }
        if(getDescription() != null){
            newTest.addAttribute("description", getDescription());
        }
        
        // Pour chaque testcase
        for(TestCase tc : listTestCase){
           // On génére l'élément xml
           tc.toXmlElement();
        }
                
        // Ajout des paramètres du test
        List<Element> params = ParamGenerator.getInstance().paramTestToXml();
        for(Element p : params){            
            newTest.add(p);
        }
        
        
        for(Iterator i = test.elements("testcase").iterator(); i.hasNext();){
            Element elem = (Element)i.next();
            newTest.add(elem.createCopy());
        }      
        
        return newTest;
    }  
    
}
