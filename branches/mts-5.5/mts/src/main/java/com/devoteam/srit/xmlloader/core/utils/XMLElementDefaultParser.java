/*
 * XMLElementDuplicator.java
 *
 * Created on 30 mai 2007, 22:58:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.util.LinkedList;
import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Generic replacer to replace only the attributes of the XML tag
 * 
 * @author Gege
 */
public class XMLElementDefaultParser implements XMLElementReplacer
{
    private ParameterPool parameterPool;
    
    public XMLElementDefaultParser(ParameterPool aParameterPool)
    {
        parameterPool = aParameterPool;

    }
    
    public List<Element> replace(Element element) throws Exception
    {
        List<Element> list = new LinkedList<Element>();
        
        Element newElement = element.createCopy();
        list.add(newElement);
        List<Attribute> attributes = newElement.attributes();
        
 
        for(Attribute attribute:attributes)
        {
            String value = attribute.getValue();
            
            LinkedList<String> parsedValue = parameterPool.parse(value);
            
            if(parsedValue.size() != 1)
            {
                throw new ExecutionException("Invalid size of variables in attribute " + value);
            }
            
            attribute.setValue(parsedValue.getFirst());
        }
        return list;
    }
}
