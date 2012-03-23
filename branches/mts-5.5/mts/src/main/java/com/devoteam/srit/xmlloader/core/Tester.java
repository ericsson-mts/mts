/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
 */
package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationFunction;
import com.devoteam.srit.xmlloader.core.operations.functions.FunctionsRegistry;
import com.devoteam.srit.xmlloader.core.parameters.EditableParameterProvider;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterOperatorRegistry;
import com.devoteam.srit.xmlloader.core.pluggable.ParameterTestRegistry;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.net.URI;


/**
 * Off-line tester
 * @author JM. Auffret
 */
public class Tester
{
    static private Tester tester = null;
    static public String mode = "";

    public static Tester buildInstance() throws Exception, IllegalArgumentException
    {
        if (null == tester)
        {
            tester = new Tester();
        }
        else
        {
            throw new IllegalArgumentException("Tester already available");
        }
        
        return tester;
    }
    
    public static void cleanInstance()
    {
        tester = null;
    }

    public static Tester getInstance()
    {
        return tester;
    }

    public static Tester instance()
    {
        return tester;
    }
    
    public final static GlobalLogger getGlobalLogger()
    {
        return GlobalLogger.instance();
    }
    
    public static String getRelease()
    {
        try{
            return new String(SingletonFSInterface.instance().getBytes(URIRegistry.IMSLOADER_BIN.resolve("../conf/version.txt")));
        }
        catch(Exception e){
            return "unknown";
        }
    }
      
    private Test test = null;
       
    /**
     * Constructor.
     * 
     * @param logg the globalLogger used by the application. Set the static variable to this value;
     * @throws Exception
     */
    private Tester() throws Exception
    {

        //
        // Initialize tread pool
        //
        int value = Config.getConfigByName("tester.properties").getInteger("core.NUMBER_THREADS_POOL");
        ThreadPool.init(value);
        
        //
        // Initialize the pluggable components
        //
        ParameterOperatorRegistry.initialize();
        ParameterTestRegistry.initialize();
    }
         
    public void open_reset()
    {
        StackFactory.reset();
        FunctionsRegistry.instance().clear();
        Config.reset();
        XMLDocumentCache.reset();
    }
    
    public void open_openFile(URI path, EditableParameterProvider provider) throws Exception
    {
        OperationFunction.importDir("../functions/", URIRegistry.IMSLOADER_BIN);

        this.test = new Test(XMLDocumentCache.get(path, URIFactory.newURI("../conf/schemas/test.xsd")), provider);

        Config.reset();
    }
    
    public XMLDocument getTestXMLDocument()
    {
        return test.getXMLDocument();
    }
    
    /**
     * Close the opened file. destroy all testcases
     */
    public void close()
    {
        this.test        = null;
    }
    
    /**
     * Returns the config.
     * @return Config
     */
    public Config getConfig()
    {
        try
        {
            return Config.getConfigByName("tester.properties");
        }
        catch(Exception e)
        {
            return null ;
        }
    }
    
    public Test getTest()
    {
        return test;
    }

}