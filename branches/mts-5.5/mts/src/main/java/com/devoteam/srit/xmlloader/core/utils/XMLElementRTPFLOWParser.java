/*
 * XMLElementAVPParser.java
 *
 * Created on 15 juin 2007, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.Attribute;

/**
 * specific replacer for <sendMessageRTPFLOW> operation : 
 * don't replace the specific "timestamp", "seqnum", "deltatime" 
 * and "mark" attributes because they can be multiple values.  
 * 
 * @author gpasquiers
 */
public class XMLElementRTPFLOWParser implements XMLElementReplacer
{
    private ParameterPool parameterPool;
    
    public XMLElementRTPFLOWParser(ParameterPool aParameterPool)
    {
        this.parameterPool = aParameterPool;
    }
    
    public List<Element> replace(Element element) throws Exception
    {
        List<Element> result = new LinkedList();

        //do classic replacement of attribute and save it in result
        Element newElement = element.createCopy();
        result.add(newElement);
        List<Attribute> attributes = newElement.attributes();

        for(Attribute attribute:attributes)
        {
            if(!attribute.getName().equalsIgnoreCase("timestamp")
               && !attribute.getName().equalsIgnoreCase("seqnum")
               && !attribute.getName().equalsIgnoreCase("deltaTime")
               && !attribute.getName().equalsIgnoreCase("mark"))
            {
                String value = attribute.getValue();

                LinkedList<String> parsedValue = parameterPool.parse(value);

                if(parsedValue.size() != 1)
                {
                    throw new ExecutionException("Invalid size of variables in attribute " + value);
                }

                attribute.setValue(parsedValue.getFirst());
            }
        }
        
        return result;
    }
}
