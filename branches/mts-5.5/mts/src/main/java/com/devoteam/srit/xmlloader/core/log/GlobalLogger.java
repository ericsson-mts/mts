package com.devoteam.srit.xmlloader.core.log;

import com.devoteam.srit.xmlloader.core.utils.ConfigCache;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 * @author pn007888
 */
public class GlobalLogger
{
    // Logger
    private GenericLogger genericLogger;

    // Log storage
    public static final int LOG_STORAGE_DISABLE   =  0;
    public static final int LOG_STORAGE_FILE    =  1;
    public static final int LOG_STORAGE_MEMORY =  2;
	    
    // Log level
    public static final int LOG_LEVEL_CONFIG_DEBUG   =  0;
    public static final int LOG_LEVEL_CONFIG_INFO    =  1;
    public static final int LOG_LEVEL_CONFIG_WARNING =  2;
    public static final int LOG_LEVEL_CONFIG_ERROR   =  3;

    private ConfigCache logLevelCache =  new ConfigCache("tester.properties", "logs.MAXIMUM_LEVEL");
    private ConfigCache logStorageCache =  new ConfigCache("tester.properties", "logs.STORAGE_LOCATION");

    static private GlobalLogger instance = null;;

    static public GlobalLogger instance()
    {
    	if (instance == null){
            instance = new GlobalLogger();
    	}
        return instance;
    }
        
    private GlobalLogger()
    {
        // Create empty logger for sessions
        genericLogger = new GenericLogger();
    }

    /**
     * @return the logger used to display application-relative messages
     */
    public GenericLogger getApplicationLogger()
    {
        return genericLogger;
    }
    
    /**
     * @return a logger used to display message relative to a session
     */
    public GenericLogger getSessionLogger()
    {
        return genericLogger;
    }

    /**
     * @return the log level
     */
    public int getLogStorage() {
        String strStorage = this.logStorageCache.getStringValue("2");
        int logStorage = 2;
        if (Utils.isInteger(strStorage)) {
            logStorage = Integer.parseInt(strStorage);
        }
        else if ("disable".equalsIgnoreCase(strStorage)) {
            logStorage = LOG_STORAGE_DISABLE;
        }
        else if ("file".equalsIgnoreCase(strStorage)) {
            logStorage = LOG_STORAGE_FILE;
        }
        else if ("memory".equalsIgnoreCase(strStorage)) {
            logStorage = LOG_STORAGE_MEMORY;
        }
        else {
            System.out.println("Config parameter \"logs.STORAGE_LOCATION\" + should be an integer from [0-2] or a string from the list {DISABLE, FILE, MEMORY}");
            new Exception().printStackTrace();
            System.exit(10);
        }
        return logStorage;
    }

    /**
     * @return the log level
     */
    public int getLogLevel() {
        String level = this.logLevelCache.getStringValue("3");
        // changement de l'ordre des levels
        int intLevel = -1;
        if (Utils.isInteger(level)) {
            intLevel = 3 - Integer.parseInt(level);
        }
        else if (TextEvent.DEBUG_STRING.equalsIgnoreCase(level)) {
            intLevel = TextEvent.DEBUG;
        }
        else if (TextEvent.INFO_STRING.equalsIgnoreCase(level)) {
            intLevel = TextEvent.INFO;
        }
        else if (TextEvent.WARN_STRING.equalsIgnoreCase(level)) {
            intLevel = TextEvent.WARN;
        }
        else if (TextEvent.ERROR_STRING.equalsIgnoreCase(level)) {
            intLevel = TextEvent.ERROR;
        }
        else {
            System.out.println("Config parameter \"logs.MAXIMUM_LEVEL\" + should be an integer from [0-3] or a string from the list {DEBUG, INFO, WARN, ERROR}");
            new Exception().printStackTrace();
            System.exit(10);
        }
        return intLevel;
    }
    
    public void logDeprecatedMessage(String operation, String insteadOfOperation)
    {
    	genericLogger.warn(TextEvent.Topic.CORE, 
    			"Deprecated  operation : <" , operation , ">. \n", 
    			"Instead of, please use : <" , insteadOfOperation , ">."
    			);
    }
}
