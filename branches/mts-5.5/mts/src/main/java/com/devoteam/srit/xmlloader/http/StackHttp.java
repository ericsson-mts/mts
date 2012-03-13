/**
 * StackHttp.java
 *
 * Created on 11 avril 2007, 15:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

        String certificatePath = Config.getConfigByName("tls.properties").getString("cert.DIRECTORY");
        String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
        String certificatePassword = Config.getConfigByName("tls.properties").getString("cert.PASSWORD");
        String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");

        char[] certificatePasswordArray;

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

        KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
        keyStore.load(new FileInputStream(certificatePath), certificatePasswordArray);

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

        String authority;
        String authorityHost;
        String authorityPort;
        boolean secure;

        String scheme;

        int index;

        //
        // Separate the authority and the scheme
        //
        index = remoteUrl.indexOf("://");

        if (index != -1)
        {
            authority = remoteUrl.substring(index + 3);
            scheme = remoteUrl.substring(0, index);
        }
        else
        {
            authority = remoteUrl;
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

        //
        // Read the authority host and port
        //
        index = authority.indexOf(":");
        if (index != -1)
        {
            authorityHost = authority.substring(0, index);
            authorityPort = authority.substring(index + 1);
        }
        else
        {
            authorityHost = authority;

            if (scheme.equalsIgnoreCase("http"))
            {
                authorityPort = getConfig().getString("client.DEFAULT_HTTP_PORT", "80");
            }
            else
            {
                if (scheme.equalsIgnoreCase("https"))
                {
                    authorityPort = getConfig().getString("client.DEFAULT_HTTPS_PORT", "443");
                }
                else
                {
                    throw new ExecutionException("Invalid scheme: " + scheme + "\n Only http: and https: are supported");
                }
            }
        }

        if (existsChannel(channelName))
        {
            return getChannel(channelName);
        }
        else
        {
            if(getConfig().getBoolean("USE_NIO")){
                return new NIOChannelHttp(channelName, localHost, localPort, authorityHost, authorityPort, protocol, secure);
            }
            else{
                return new BIOChannelHttp(channelName, localHost, localPort, authorityHost, authorityPort, protocol, secure);
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

    public XMLElementReplacer getElementReplacer(ParameterPool variables)
    {
        return new XMLElementTextMsgParser(variables);
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName(configFileName);
    }
}
