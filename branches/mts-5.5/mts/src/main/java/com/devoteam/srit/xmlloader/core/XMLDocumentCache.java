/*
 * XMLDocumentCache.java
 *
 * Created on 30 octobre 2007, 12:09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import java.net.URI;
import java.util.HashMap;

/**
 *
 * @author gpasquiers
 */
public class XMLDocumentCache
{
    static private HashMap<String, XMLDocument> cache = new HashMap<String, XMLDocument>();
    static private boolean _enabled = true;

    static public void enable(){
        _enabled = true;
    }

    static public void disable(){
        _enabled = false;
    }

    static public XMLDocument get(URI pathXML, URI pathXSD) throws Exception
    {
        XMLDocument xmlDocument = null;

        if(_enabled){
            xmlDocument = cache.get(pathXML.toString());
        }

        if(null == xmlDocument)
        {
            xmlDocument = new XMLDocument();
            xmlDocument.setXMLSchema(pathXSD);
            xmlDocument.setXMLFile(pathXML);
            xmlDocument.parse();
            if(_enabled){
                cache.put(pathXML.toString(), xmlDocument);
            }
        }

        
        return xmlDocument.duplicate();
    }
    
    static public void reset()
    {
        cache.clear();
    }
}
