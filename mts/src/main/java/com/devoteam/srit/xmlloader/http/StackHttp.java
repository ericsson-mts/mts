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

package com.devoteam.srit.xmlloader.http;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.http.bio.BIOChannelHttp;
import com.devoteam.srit.xmlloader.http.bio.BIOSocketServerListener;
import com.devoteam.srit.xmlloader.http.nio.NIOChannelHttp;
import com.devoteam.srit.xmlloader.http.nio.NIOSocketServerListener;
import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.LinkedList;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.dom4j.tree.DefaultElement;

/**
 *
 * @author gpasquiers
 */
public class StackHttp extends Stack
{
    private LinkedList<SocketServerListener> socketServerListeners;
    private static final String configFileName = "http.properties";

    public static IOReactor ioReactor = new IOReactor();

    /** Constructor */
    public StackHttp() throws Exception
    {
        super();

        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager(){

                public java.security.cert.X509Certificate[] getAcceptedIssuers(){
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){
                    //No need to implement.
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){
                    //No need to implement.
                }
            }
        };

        String certificatePath = Config.getConfigByName("tls.properties").getString("cert.SERVER.DIRECTORY");
        String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
        String certificatePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEY_PASSWORD");
        String keystorePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEYSTORE_PASSWORD");
        String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");

        char[] certificatePasswordArray;
        char[] keystorePasswordArray;
        //
        // If password is an empty string (allowed) or not defined (allowed), do not use a password
        //
        if (null == certificatePassword || certificatePassword.length() == 0)
        {
            certificatePasswordArray = null;
        }
        else
        {
            certificatePasswordArray = certificatePassword.toCharArray();
        }
        if (null == keystorePassword || keystorePassword.length() == 0)
        {
            keystorePasswordArray = null;
        }
        else
        {
            keystorePasswordArray = keystorePassword.toCharArray();
        }

        KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
        keyStore.load(new FileInputStream(certificatePath), keystorePasswordArray);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, certificatePasswordArray);

        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(keyStore);


        StackHttp.context = SSLContext.getInstance(certificateSSLVersion);
        StackHttp.context.init(keyManagers, trustAllCerts, null);

        socketServerListeners = new LinkedList();
        
        // Read Http server port
        String listPorts = getConfig().getString("listenpoint.LOCAL_PORT", "");
        String[] splittedList = Utils.splitNoRegex(listPorts, ",");
        if ((splittedList.length == 1 && !Utils.isInteger(splittedList[0])) || splittedList[0].length() == 0)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Did not start HTTP server. configuration port=", listPorts);
        }
        else
        {
            SocketServerListener socketServerListener;
            // Instanciate and start the HTTP listener
            for (int i = 0; i < splittedList.length; i++)
            {
                if(getConfig().getBoolean("USE_NIO"))
                    socketServerListener = new NIOSocketServerListener(Integer.parseInt(splittedList[i]), false);
                else
                    socketServerListener = new BIOSocketServerListener(Integer.parseInt(splittedList[i]), false);
                
                socketServerListeners.add(socketServerListener);
            }
        }

        // Read Https server port
        listPorts = getConfig().getString("listenpoint.LOCAL_PORT_TLS", "");
        splittedList = Utils.splitNoRegex(listPorts, ",");
        if ((splittedList.length == 1 && !Utils.isInteger(splittedList[0])) || splittedList[0].length() == 0)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Did not start HTTPS server. configuration port=", listPorts);
        }
        else
        {
            SocketServerListener socketServerListener;
            // Instanciate and start the HTTPS listener
            for (int i = 0; i < splittedList.length; i++)
            {
                if(getConfig().getBoolean("USE_NIO"))
                    socketServerListener = new NIOSocketServerListener(Integer.parseInt(splittedList[i]), true);
                else
                    socketServerListener = new BIOSocketServerListener(Integer.parseInt(splittedList[i]), true);

                socketServerListeners.add(socketServerListener);
            }
        }
    }
    
    public static SSLContext context;

    /** reset the instance of this stack */
    public void reset()
    {
        super.reset();
        try
        {
            while (!socketServerListeners.isEmpty())
            {
                socketServerListeners.removeFirst().shutdown();
            }
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while stopping SocketServerListener");
        }
    }

    /** Creates a channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String channelName = root.attributeValue("name");

        //part to don't have regression
        if (channelName == null || channelName.equalsIgnoreCase(""))
        {
            channelName = root.attributeValue("connectionName");
        }

        String localHost = root.attributeValue("localHost");
        String localPort = root.attributeValue("localPort");
        String remoteUrl = root.attributeValue("remoteURL");
        String remoteHost = null;
        String remotePort = null;
        
        URI uri = null;
        try
        {
            uri = new URI(remoteUrl);
            remotePort = String.valueOf(uri.getPort());
        }
        catch (Exception e)
        {
            throw new ExecutionException("Can't create URI from : " + remoteUrl, e);
        }

        boolean secure = false;
        String scheme = uri.getScheme();
        if (scheme == null)
        {
        	scheme = getConfig().getString("client.DEFAULT_PROTOCOL");
        }
        
        if (scheme.equalsIgnoreCase("https"))
        {
            secure = true;
        }
        else
        {
            secure = false;
        }
        
        remoteHost = Utils.formatIPAddress(uri.getHost());
        
        if (uri.getPort() > 0)
        {
        	remotePort = String.valueOf(uri.getPort());
        }
        else
        {
        	if (secure)
        	{
        		remotePort = getConfig().getString("client.DEFAULT_HTTPS_PORT", "443");
        	}
        	else
        	{
        		remotePort = getConfig().getString("client.DEFAULT_HTTP_PORT", "80");
        	}
        }

        if (existsChannel(channelName))
        {
            return getChannel(channelName);
        }
        else
        {
            if(getConfig().getBoolean("USE_NIO")){
                return new NIOChannelHttp(channelName, localHost, localPort, remoteHost, remotePort, protocol, secure);
            }
            else{
                return new BIOChannelHttp(channelName, localHost, localPort, remoteHost, remotePort, protocol, secure);
            }
        }
    }

    /** Creates a specific HTTP Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        String text = root.getText();

        MsgHttp msgHttp = new MsgHttp(text);

        //
        // Try to find the channel
        //
        String channelName = root.attributeValue("channel");
        //part to don't have regression
        if (channelName == null || channelName.equalsIgnoreCase(""))
        {
            channelName = root.attributeValue("connectionName");
        }

        String remoteUrl = root.attributeValue("remoteURL");
        //part to don't have regression
        if (remoteUrl == null || remoteUrl.equalsIgnoreCase(""))
        {
        	remoteUrl = root.attributeValue("server");
        }

        //
        // If the message is not a request, it is a response.
        // The channel to use will be obtained from the
        // channel of the transaction-associated request.
        if (msgHttp.isRequest())
        {
            Channel channel = null;
        	// case the channelName is specified
            if (channelName != null && !channelName.equalsIgnoreCase(""))
            {           
            	channel = getChannel(channelName);
            }
        	// case the remoteXXX is specified
            if (remoteUrl != null && !remoteUrl.equalsIgnoreCase(""))
            {
            	channel = getChannel(remoteUrl);
            	if (channel == null)
            	{
	                //part to don't have regression
		            DefaultElement defaultElement = new DefaultElement("openChannelHTTP");
		            defaultElement.addAttribute("remoteURL", remoteUrl);
		            defaultElement.addAttribute("name", remoteUrl);
		            channel = this.parseChannelFromXml(defaultElement, StackFactory.PROTOCOL_HTTP);
		            openChannel(channel);
	                channel = getChannel(remoteUrl);
            	}
            }
            if (channel == null)
            {
                throw new ExecutionException("The channel named " + channelName + " does not exist");
            }
            // call to getTransactionId to generate it NOW (important)
            // it can be generated now because this is a request from xml
            msgHttp.getTransactionId();

            msgHttp.setChannel(channel);
        }
        else
        {        	
            if (channelName != null)
            {
                throw new ExecutionException("You can not specify the \"channel\" attribute while sending a response (provided by the HTTP protocol).");
            }
            if (remoteUrl != null)
            {
                throw new ExecutionException("You can not specify the \"remoteURL\" attribute while sending a response (provided by the HTTP protocol).");
            }
        }

        return msgHttp;
    }

    /** Send a Msg to Stack */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {

        // copy the channel from the request into the response using the transaction
        Trans trans = msg.getTransaction();
        if (trans != null)
        {
            Channel channel = trans.getBeginMsg().getChannel();
            msg.setChannel(channel);
        }

        return super.sendMessage(msg);
    }

    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName(configFileName);
    }
}
