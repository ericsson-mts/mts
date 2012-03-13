/*
 * XMLElementAVPParser.java
 *
 * Created on 15 juin 2007, 15:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.diameter.MsgDiameterParser;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * specific replacer for <sendMessageDIAMETER> operation : 
 * duplicate the XML tags if there are multiple value in all 
 * parameters on the XML tag (like avp)
 * if the parameter is empty, then the XML tag is removed 
 * also used for <stats> operation 
 * 
 * @author gpasquiers
 */
public class XMLElementAVPParser implements XMLElementReplacer
{
    private ParameterPool parameterPool;
    
    private XMLElementDefaultParser xmlElementDefaultParser;
    private XMLElementTextOnlyParser xmlElementTextOnlyParser;
    
    public XMLElementAVPParser(ParameterPool parameterPool)
    {
        this.parameterPool = parameterPool;
        xmlElementDefaultParser = new XMLElementDefaultParser(parameterPool);
        xmlElementTextOnlyParser = new XMLElementTextOnlyParser(parameterPool);
    }
    
    public List<Element> replace(Element element) throws Exception
    {
        List<Element> result ;
        
        if(element.getName().equalsIgnoreCase("header"))
        {
            result = xmlElementDefaultParser.replace(element);
        }
        else // <avp .../>
        {
            LinkedList list = new LinkedList<Element>();
            
            List<Attribute> attributes ;
            attributes = element.attributes();
            
            int allowedParameterLength = -1;
            boolean hasParameter = false;
            
            for(Attribute attribute:attributes)
            {
                String value = attribute.getValue();	                
                Matcher matcher = Parameter.pattern.matcher(value);
                
                while(matcher.find())
                {
                    String variableStr = matcher.group();
                    
                    if(false == parameterPool.isConstant(variableStr))
                    {
                        Parameter variable = parameterPool.get(variableStr);
                        hasParameter = true;
                        if (variable != null)
                        {
                            if(allowedParameterLength == -1)
                            {
                                allowedParameterLength = variable.length();
                            }
                            else if(allowedParameterLength != variable.length())
                            {
                                throw new ExecutionException("Invalid length of variables : a variable of length " + allowedParameterLength + " has been found but " + variableStr + " has a length of " + variable.length());
                            }
                        }
                    }
                }
            }
            
            if (!hasParameter)
            {
                allowedParameterLength = 1;
            }
            
            for(int i=0; i<allowedParameterLength; i++)
            {
                Element newElement = element.createCopy();
                
                List<Attribute> newElementAttributes ;
                newElementAttributes = newElement.attributes();
                
                for(Attribute newAttribute:newElementAttributes)
                {
                    String value = newAttribute.getValue();
                    
                    Pattern pattern = Pattern.compile(Parameter.EXPRESSION);
                    Matcher matcher = pattern.matcher(value);
                    int offset = 0;
                    while(matcher.find())
                    {
                        String before = value.substring(0, matcher.end()+offset-1);
                        String after  = value.substring(matcher.end()+offset-1);

                        if (parameterPool.exists(matcher.group()))
                        {
                            value = before + "(" + i + ")" + after;
                            offset += ((String) "(" + i + ")").length();
                        }
                        
                    }
                    newAttribute.setValue(value);
                }
                
                List<Element> tempList = xmlElementDefaultParser.replace(newElement);
                try
                {
                    for(Element e:tempList)
                    {
                        MsgDiameterParser.getInstance().doDictionnary(e, "base", false);

                        list.addAll(xmlElementTextOnlyParser.replace(e));
                    }
                }
                catch(ParsingException e)
                {
                    throw new ExecutionException("Error while checking parsed variables against dictionary");
                }




            }
            result = list;
        }
        
        return result;
    }
}
