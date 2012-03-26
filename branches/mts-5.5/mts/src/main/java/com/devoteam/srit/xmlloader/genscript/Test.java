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
