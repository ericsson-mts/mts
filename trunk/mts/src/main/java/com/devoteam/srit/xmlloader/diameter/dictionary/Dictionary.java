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

package com.devoteam.srit.xmlloader.diameter.dictionary;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;


/**
 *
 * @author gpasquiers
 */
public class Dictionary
{
    
    // current instance of dictionary
    static private Dictionary _dictionary ;
    
    // harcoded path of dictionary.xml
    static private String DICTIONARY_PATH="../conf/diameter/dictionary.xml" ;
    
    // hashmaps
    private HashMap<String, Application>  applicationByName ;
    private HashMap<String, Application>  applicationById ;
    
    /**
     * Returns the current instance of Dictionary.
     * Creates one if it does not already exists.
     */
    static public synchronized Dictionary getInstance() throws ParsingException
    {
        // creates an instance if needed
        {
            if(_dictionary==null)
            {
                _dictionary = new Dictionary();
                try
                {
                	String dictionaryPath = Config.getConfigByName("diameter.properties").getString("dictionary.PATH", DICTIONARY_PATH);
                    _dictionary.parseFromFile(dictionaryPath);
                }
                catch(Exception e)
                {
                    throw new ParsingException(e);
                }
            }
        }
        
        // return the instance of dictionary
        return _dictionary ;
    }
    
    /**
     * Parse the dictionary from a file.
     */
    private void parseFromFile(String path) throws Exception
    {
        XMLDocument dictionaryDocument = new XMLDocument();
        dictionaryDocument.setXMLSchema(URIFactory.newURI("../conf/schemas/diameter-dictionary.xsd"));
        dictionaryDocument.setXMLFile(URIFactory.newURI(path));
        dictionaryDocument.parse();
        
        Document document = dictionaryDocument.getDocument();
        
        // parse vendors
        
        applicationByName = new HashMap();
        applicationById = new HashMap();
        
        traceDebug("try to parsing application base");

        //
        // base first, important for references from other applications
        //
        parseApplication(document.getRootElement().element("base"));
        
        traceDebug("try to parsing all application");

        //
        // all applications
        //
        List<Element> elements = document.getRootElement().elements("application");
        for(Element element:elements)
        {
            parseApplication(element);
        }
    }
    
    private void parseApplication(Element root) throws ParsingException
    {
        boolean isBase = false ;
        if(root.getName().equals("base"))
        {
            isBase = true ;
        }
        
        int id = -1 ;
        try
        {
            if(null != root.attribute("id"))
            {
                id = Integer.parseInt(root.attributeValue("id"));
            }
            else if(false == isBase)
            {
                traceWarning("No application.id, skipping");
                return ;
            }
        }
        catch(Exception e)
        {
            traceWarning("Invalid application.code, skipping");
            return ;
        }
        
        String name = null ;
        if(null != root.attribute("name"))
        {
            name = root.attributeValue("name");
        }
        else if(false == isBase)
        {
            traceWarning("No application.name, skipping");
            return ;
        }
        
        if(isBase)
        {
            name = "0" ;
            id = 0;
        }
        
        traceDebug("parsing application " + name);
        
        Application application = getApplicationById(id); 
        if (application == null)
        {
        	application = new Application(name, id);
        }
    	applicationByName.put(name, application);
    	applicationById.put(Integer.toString(id), application);

        application.parseApplication(root);
        application.fillGroupedAvpsReferences();
    }
    
    private Application getApplicationById(int code)
    {
        return applicationById.get(Integer.toString(code));
    }
    
    private Application getApplicationByName(String name)
    {
        return applicationByName.get(name);
    }
    
    public Application getApplication(String key)
    {
        if(Utils.isInteger(key))  return getApplicationById(Integer.parseInt(key));
        else                      return getApplicationByName(key);
    }
    
    public VendorDef getVendorDefByName(String name, String applicationId )
    {
        VendorDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getVendorDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != application) result = applicationBase.getVendorDefByName(name);
        if(null != application) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getVendorDefByName(name);
            if(null != result)
            {
                traceWarning("got Vendor definition for " + result.get_vendor_id() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public VendorDef getVendorDefByCode(int code, String applicationId )
    {
        Application application ;
        VendorDef result = null ;
        
        // try with specified application
        application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getVendorDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getVendorDefByCode(code);
        if(null != applicationBase) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getVendorDefByCode(code);
            if(null != result)
            {
                traceWarning("got Vendor definition for " + result.get_vendor_id() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public TypeDef getTypeDefByName(String name, String applicationId )
    {
        TypeDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getTypeDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getTypeDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getTypeDefByName(name);
            if(null != result)
            {
                traceWarning("got Type definition for " + result.get_type_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public CommandDef getCommandDefByName(String name, String applicationId )
    {
        CommandDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getCommandDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getCommandDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getCommandDefByName(name);
            if(null != result)
            {
                traceWarning("got Command definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public CommandDef getCommandDefByCode(int code, String applicationId )
    {
        CommandDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getCommandDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getCommandDefByCode(code);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getCommandDefByCode(code);
            if(null != result)
            {
                traceWarning("got Command definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public AvpDef getAvpDefByCode(int code, String applicationId )
    {
        AvpDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getAvpDefByCode(code);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getAvpDefByCode(code);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getAvpDefByCode(code);
            if(null != result)
            {
                traceWarning("got AVP definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public AvpDef getAvpDefByName(String name, String applicationId )
    {
        AvpDef result = null ;
        
        // try with specified application
        Application application = getApplication(applicationId);
        if(null != application)
        {
            applicationId = application.get_name();
            result = application.getAvpDefByName(name);
        }
        if(null != result) return result ;
        
        // try with base application
        Application applicationBase = getApplication("0");
        if(null != applicationBase) result = applicationBase.getAvpDefByName(name);
        if(null != result) return result ;
        
        // try with other applications
        for(Application a:applicationByName.values())
        {
            if(a !=application && a != applicationBase) result = a.getAvpDefByName(name);
            if(null != result)
            {
                traceWarning("got AVP definition for " + result.get_name() + " not from specified application (" + applicationId + ") nor base AVPs but " + a.get_name());
                return result;
            }
        }
        
        return null;
    }
    
    public static boolean isInteger(String string)
    {
        if(null == string) return false ;
        for(int i=0; i<string.length(); i++) if(!Character.isDigit(string.charAt(i))) return false ;
        return true ;
    }
    
    public static void traceWarning(String text)
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Dictionary: ", text);
    }
    
    public static void traceDebug(String text)
    {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Dictionary: ", text);
    }
    
}
