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

import java.util.List;
import org.dom4j.Element;


/**
 * Generic replacer to replace only the attributes and
 * the content of the XML tag
 * 
 * @author gpasquiers
 */
public class XMLElementTextMsgParser implements XMLElementReplacer
{
    private ParameterPool variables;
    
    private XMLElementDefaultParser xmlElementDefaultParser;
    private XMLElementTextOnlyParser xmlElementTextOnlyParser;
    
    public XMLElementTextMsgParser(ParameterPool aParameterPool)
    {
        variables = aParameterPool;
        xmlElementDefaultParser = new XMLElementDefaultParser(variables);
        xmlElementTextOnlyParser = new XMLElementTextOnlyParser(variables);
    }
    
    public List<Element> replace(Element element) throws Exception
    {
        List<Element> result ;
        
        result = xmlElementDefaultParser.replace(element);
        element = result.get(0);
        result = xmlElementTextOnlyParser.replace(element);
        return result;
    }
}
