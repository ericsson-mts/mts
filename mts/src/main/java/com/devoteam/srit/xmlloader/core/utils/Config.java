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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.gui.conf.JPanelContainer;
import com.devoteam.srit.xmlloader.gui.conf.JPanelGeneric;

import java.net.DatagramSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Config
{
    // <editor-fold desc=" static part of class ">
    
    static private HashMap<String, Config> configs = new HashMap();
    static private HashMap<String, PropertiesEnhanced> overridingProperties = new HashMap();
    static private long lastReset = 0;

    //static private String testPath = null;

    static synchronized public Config getConfigByName(String name)
    {
        try
        {
            Config config = configs.get(name);

            if(null == config)
            {
                config = new Config(name);
                configs.put(name, config);
            }
            return config;
        }
        catch(Exception e)
        {
        	System.out.println("EXCEPTION : reading config file " + name + " : " + e.getMessage());
        	e.printStackTrace();
//            System.exit(100);
            return null;
        }
    }

    static synchronized public void overrideProperties(String name, PropertiesEnhanced overridingProperties)
    {
        
        // store overriding properties
        PropertiesEnhanced properties = Config.overridingProperties.get(name);
        
        if(null == properties)
        {

            Config.overridingProperties.put(name, overridingProperties);
            properties = overridingProperties;
        }
        else
        {            
            for(Iterator<String> it = overridingProperties.getConfig().keySet().iterator(); it.hasNext();)
            {
                String tmp = it.next();
                properties.setDefaultValue(tmp, overridingProperties.getConfig().get(tmp).getDefaultValue());
            }
        }
        
        // update overriding properties in existing config objects
        Config existingConfig = configs.get(name);
        if(null != existingConfig && null == existingConfig.propertiesOver)
        {
            existingConfig.propertiesOver = properties;
        }
    }

    static synchronized public Set<String> getListOfPorpertiesFile(){
        return Config.configs.keySet();
    }
    
    static public void reset()
    {
        configs.clear();
        lastReset = System.currentTimeMillis();
    }
    static public long getLastReset(){
        return lastReset;
    }
    // </editor-fold>
    
    
    // <editor-fold desc=" instancied part of class ">
    private PropertiesEnhanced propertiesOver = null;
    private PropertiesEnhanced propertiesEnhanced = null;
    
    
    /**
     * Constructor
     *
     * @param name Configuration filename (without path and extention)
     * @throws ParsingException
     */
    public Config(String name) throws Exception
    {
        this.propertiesOver = Config.overridingProperties.get(name);
        this.propertiesEnhanced = new PropertiesEnhanced();                
        
        URI pathConf = URIFactory.newURI("../conf/" + name);
        if(SingletonFSInterface.instance().exists(pathConf))
        {
            this.propertiesEnhanced.parse(SingletonFSInterface.instance().getInputStream(pathConf), true, false);
        }                
        if(null != URIRegistry.MTS_CONFIG_HOME)
        {
            URI pathTest = URIRegistry.MTS_CONFIG_HOME.resolve(name);

            if(SingletonFSInterface.instance().exists(pathTest))
            {
                this.propertiesEnhanced.parse(SingletonFSInterface.instance().getInputStream(pathTest), false, false);
            }            
        }
        if(0 == this.propertiesEnhanced.getNumberOfParameters())
        {
            throw new Exception("could not load any properties file named " + name);
        }        
    }

    public void build (String fileName, boolean global) throws Exception{
        String filepathConf = "../conf/" + fileName;
        URI filePathInputConf = URIRegistry.MTS_BIN_HOME.resolve(filepathConf);        
        URI filePathInputLoc = URIRegistry.MTS_CONFIG_HOME.resolve(fileName);
        if(SingletonFSInterface.instance().exists(filePathInputConf)){
            if (!global && SingletonFSInterface.instance().exists(filePathInputLoc)){
                this.propertiesEnhanced = this.propertiesEnhanced.parse(SingletonFSInterface.instance().getInputStream(filePathInputConf), SingletonFSInterface.instance().getInputStream(filePathInputLoc), false);
            }
            else{
                this.propertiesEnhanced.parse(SingletonFSInterface.instance().getInputStream(filePathInputConf), true, false);
            }
        }
        else if (SingletonFSInterface.instance().exists(filePathInputLoc)){
            //cas ou un fichier est present en local, mais pas en global
            this.propertiesEnhanced.parse(SingletonFSInterface.instance().getInputStream(filePathInputLoc), true, true);
        }
    }

    public void save(JPanelContainer jPanelContainer, String fileName){
        URI filePathLoc = URIRegistry.MTS_CONFIG_HOME.resolve(fileName);
        for(JPanelGeneric panel:jPanelContainer.getListOfGeneric()){
            if(panel.isModified()) panel.save();
        }
        propertiesEnhanced.saveFile(filePathLoc);
    }

    public PropertiesEnhanced getPropertiesEnhanced (){
        return this.propertiesEnhanced;
    }

    public void setParameter(String name, String value){
        String fileName = "tester.properties";
        URI filePathLoc = URIRegistry.MTS_CONFIG_HOME.resolve(fileName);
        Config.getConfigByName(fileName).getPropertiesEnhanced().setLocaleValue(name, value);
        Config.getConfigByName(fileName).getPropertiesEnhanced().saveFile(filePathLoc);
    }      
    
    /**
     * Get a String from the right property file.
     * This method is called by all other gets.
     */
    public String getString(String key) throws Exception
    {
        String value;
        value = null;

        if(propertiesOver != null)
        {
            value = this.propertiesOver.getDefaultValue(key);
        }
        if (null == value){
            value = this.propertiesEnhanced.getlocaleValue(key);
        }
        if (null == value){
            value = this.propertiesEnhanced.getDefaultValue(key);
        }
        if (value == null)
        {
        	value="";
        }
        //if (null != value){
            return value.trim();
        //}
        //else
        //    return null;
    }
    
    public String getString(String key, String defaultValue)
    {
        String result;

        try
        {
            result = getString(key);
            if(result == null)
                result = defaultValue;
        }
        catch (Exception e)
        {
            result = defaultValue;
        }
        return result;
    }
    
    public long getLong(String key) throws ParsingException
    {
        try
        {
            String value = getString(key);
            return Long.parseLong(value);
        }
        catch (Exception e)
        {
            throw new ParsingException("Can't read configuration parameter " + key);
        }
    }
    
    public long getLong(String key, long defaultValue)
    {
        try
        {
            return getLong(key);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    public int getInteger(String key) throws ParsingException
    {
        try
        {
            String value = getString(key);
            return Integer.parseInt(value);
        }
        catch (Exception e)
        {
            throw new ParsingException("Can't read configuration parameter " + key);
        }
    }
    
    public int getInteger(String key, int defaultValue)
    {
        try
        {
            return getInteger(key);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key) throws ParsingException
    {
        try
        {
            String value = getString(key).trim().toLowerCase();
            if (value == null) 
            {
            	throw new ParsingException("Can't read configuration parameter " + key);            	
            }
            if (("false".equals(value)) || ("0".equals(value)))  
            {
            	return false;
            } 
            else if (("true".equals(value)) || ("1".equals(value)))
            {
            	return true;            	
            } 
            else 
            {
            	throw new ParsingException("Can't read configuration parameter " + key);            	
            }
            
        }
        catch (Exception e)
        {
            throw new ParsingException("Can't read configuration parameter " + key);
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue)
    {
        try
        {
            return getBoolean(key);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    public double getDouble(String key) throws ParsingException
    {
        // do not reload local properties, done in getString
        try
        {
            String value = getString(key);
            return Double.parseDouble(value);
        }
        catch (Exception e)
        {
            throw new ParsingException("Can't read configuration parameter " + key);
        }
    }

    public double getDouble(String key, double defaultValue)
    {
        try
        {
            return getDouble(key);
        }
        catch (Exception e)
        {
            return defaultValue;
        }
    }
    
    public static void getConfigForTCPSocket(Socket socket, boolean tlsTransport) throws Exception
    {
		boolean keepAlive = Config.getConfigByName("tcp.properties").getBoolean("socket.KEEP_ALIVE", false);
		socket.setKeepAlive(keepAlive);
		if (!tlsTransport)
		{
			boolean oobInline = Config.getConfigByName("tcp.properties").getBoolean("socket.OOB_INLINE", false);
			socket.setOOBInline(oobInline);
		}
		int receiveBuffer = Config.getConfigByName("tcp.properties").getInteger("socket.RECEIVE_BUFFER", 8192);
		socket.setReceiveBufferSize(receiveBuffer);
		boolean reuseAddress = Config.getConfigByName("tcp.properties").getBoolean("socket.REUSE_ADDRESS", false);
		socket.setReuseAddress(reuseAddress);
		int sendBuffer = Config.getConfigByName("tcp.properties").getInteger("socket.SEND_BUFFER", 8192);
		socket.setSendBufferSize(sendBuffer);
		int lingerTimeout = Config.getConfigByName("tcp.properties").getInteger("socket.LINGER_TIMEOUT", -1);
		if (lingerTimeout >= 0)
			socket.setSoLinger(true, lingerTimeout);
		else
			socket.setSoLinger(false, 0);
		int timeout = Config.getConfigByName("tcp.properties").getInteger("socket.TIMEOUT", 0);
		socket.setSoTimeout(timeout);
		boolean tcpNoDelay = Config.getConfigByName("tcp.properties").getBoolean("socket.TCP_NO_DELAY", false);
		socket.setTcpNoDelay(tcpNoDelay);
		int trafficClass = Config.getConfigByName("tcp.properties").getInteger("socket.TRAFFIC_CLASS", 0);
		socket.setTrafficClass(trafficClass);
    }

    public static void getConfigForUDPSocket(DatagramSocket socket) throws Exception
    {
		int receiveBuffer = Config.getConfigByName("udp.properties").getInteger("socket.RECEIVE_BUFFER", 8192);
		socket.setReceiveBufferSize(receiveBuffer);
		boolean reuseAddress = Config.getConfigByName("udp.properties").getBoolean("socket.REUSE_ADDRESS", false);
		socket.setReuseAddress(reuseAddress);
		int sendBuffer = Config.getConfigByName("udp.properties").getInteger("socket.SEND_BUFFER", 8192);
		socket.setSendBufferSize(sendBuffer);
		int timeout = Config.getConfigByName("udp.properties").getInteger("socket.TIMEOUT", 0);
		socket.setSoTimeout(timeout);
		int trafficClass = Config.getConfigByName("udp.properties").getInteger("socket.TRAFFIC_CLASS", 0);
		socket.setTrafficClass(trafficClass);
    }

}