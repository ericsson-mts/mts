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

package com.devoteam.srit.xmlloader.http2;

import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.URIScheme;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.hybridnio.IOReactor;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
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
public class StackHttp2 extends Stack
{

    public static IOReactor ioReactor = new IOReactor();

    
    /**Constructor 
     * Should replace the method used in Http2Channel
     */
    public StackHttp2() throws Exception{
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


        StackHttp2.context = SSLContext.getInstance(certificateSSLVersion);
        StackHttp2.context.init(keyManagers, trustAllCerts, null);
        Http2Channel.context = SSLContext.getInstance(certificateSSLVersion);
        Http2Channel.context.init(keyManagers, trustAllCerts, null);
    }
    
    public static SSLContext context;
    
    public Http2Channel http2Channel;

    /** reset the instance of this stack */
    public void reset()
    {
        super.reset();
        try
        {
            http2Channel.close();
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while stopping SocketServerListener");
        }
    }

    /** 
     * Creates a channel specific to each Stack 
     * Need to be implemented
     */
    @Override
    public Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception
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
        /*
        if(secure)
        {
        	return new Http2Channel(URIScheme.HTTPS);
        }
        else
        {
        	return new Http2Channel(URIScheme.HTTP);
        }
        */
        
	     return null;   
        
    }

    /** 
     * Creates a specific HTTP Msg
     * Needs to be implemented
     */
    @Override
    public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception
    {
    	return null;
    }
      


}
