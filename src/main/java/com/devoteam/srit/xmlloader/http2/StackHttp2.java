package com.devoteam.srit.xmlloader.http2;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Map.Entry;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author qqin
 */
public class StackHttp2 extends Stack {

	private static String keystorePath; 
	private static String keystorePassword;	
	private static String truststorePath; 
	private static String truststorePassword;	

	/** Constructor */
	public StackHttp2() throws Exception {
		super();
		
		keystorePath = Config.getConfigByName("tls.properties").getString("cert.KEYSTORE.DIRECTORY");
		keystorePassword = Config.getConfigByName("tls.properties").getString("cert.KEYSTORE.PASSWORD");
		truststorePath = Config.getConfigByName("tls.properties").getString("cert.TRUSTSTORE.DIRECTORY");
		truststorePassword = Config.getConfigByName("tls.properties").getString("cert.TRUSTSTORE.PASSWORD");
	}

	/** Create the listenpoint */
	@Override
	public boolean createListenpoint(Listenpoint listenpoint, String protocol) throws Exception
	{
		boolean result = true;
		synchronized (listenpoints)
		{        	
			Listenpoint oldListenpoint = listenpoints.get(listenpoint.getName());
			if ((oldListenpoint != null) && (!listenpoint.equals(oldListenpoint))) {
				throw new ExecutionException("A listenpoint called \"" + listenpoint.getName() + "\" already exists with other attributes.");
			}
			if (oldListenpoint != null)
			{
				return false;
			}

			if(oldListenpoint == null) {
				String listenpointAddress = listenpoint.getHost();
				int port = listenpoint.getPort();
				if (port==0)  {
					port = listenpoint.getPortTLS();					
				}
				
				if (port < 0)
				{
					throw new ExecutionException("Port of listenpoint cant not be negatif: port = \""  + port + "\".");
				}

				for(Entry<String, Listenpoint> lp : listenpoints.entrySet()) {
					if(lp.getValue().getHost().equals(listenpointAddress) && lp.getValue().getPort() == port) {
						throw new ExecutionException("A listenpoint with host = \"" + listenpointAddress + "\", port = \"" + port + "\" already exists.");
					}
				}
			}

			listenpoints.put(listenpoint.getName(), listenpoint);

			if (listenpoints.size() % 1000 == 999)
			{
				GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack : List of listenpoints : size = ", listenpoints.size());
			}
			GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in listenpoints list : size = ", listenpoints.size(), " the listenpoint \n", listenpoint);
		}

		result = listenpoint.create(protocol);

		return result;
	}

	/** Creates a Channel specific to each Stack */
	@Override
	public Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception {	
		String channelName = root.attributeValue("name");

		// part to don't have regression
		if (channelName == null || channelName.equalsIgnoreCase("")) {
			channelName = root.attributeValue("connectionName");
		}		

		String localHost = root.attributeValue("localHost");
		String localPort = root.attributeValue("localPort");
		String remoteUrl = root.attributeValue("remoteURL");
		String remoteHost = null;
		String remotePort = null;

		URI uri = null;
		try {
			uri = new URI(remoteUrl);
			remotePort = String.valueOf(uri.getPort());
		} catch (Exception e) {
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
			return new ChannelHttp2(channelName, localHost, localPort, remoteHost, remotePort, protocol, secure);
		}

	}

	/** Creates a specific Msg */
	@Override
	public Msg parseMsgFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception {		
		MsgHttp2 msg = (MsgHttp2) super.parseMsgFromXml(context, root, runner);		

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
		if (msg.isRequest())
		{
			ChannelHttp2 channel = null;
			// case the channelName is specified
			if (channelName != null && !channelName.equalsIgnoreCase(""))
			{           
				channel = (ChannelHttp2) getChannel(channelName);
			}
			// case the remoteXXX is specified
			if (remoteUrl != null && !remoteUrl.equalsIgnoreCase(""))
			{
				channel = (ChannelHttp2) getChannel(remoteUrl);
				if (channel == null)
				{
					//part to don't have regression
					DefaultElement defaultElement = new DefaultElement("openChannelHTTP2");
					defaultElement.addAttribute("remoteURL", remoteUrl);
					defaultElement.addAttribute("name", remoteUrl);
					channel = (ChannelHttp2) this.parseChannelFromXml(defaultElement, runner, StackFactory.PROTOCOL_HTTP2);
					openChannel(channel);
					channel = (ChannelHttp2) getChannel(remoteUrl);
				}
			}
			if (channel == null)
			{
				throw new ExecutionException("The channel named " + channelName + " does not exist");
			}
			//Set transactionId in message for a request
			TransactionId transactionId1 = new TransactionId(UUID.randomUUID().toString());
			msg.setTransactionId(transactionId1);
			msg.setChannel(channel);         
		}
		else
		{
			TransactionId transactionId = new TransactionId(root.attributeValue("transactionId"));
			Trans transaction = getInTransaction(transactionId);	
			msg.setTransactionId(transactionId);
			msg.setTransaction(transaction);
			msg.setChannel(transaction.getBeginMsg().getChannel());
			msg.setListenpoint(transaction.getBeginMsg().getListenpoint());
			
			if (channelName != null)
			{
				throw new ExecutionException("You can not specify the \"channel\" attribute while sending a response (provided by the HTTP2 protocol).");
			}
			if (remoteUrl != null)
			{
				throw new ExecutionException("You can not specify the \"remoteURL\" attribute while sending a response (provided by the HTTP2 protocol).");
			}

		}      
		return msg;
	}

	public static SSLContext createServerSSLContext() throws Exception {
        final URL keyStoreURL = new File(keystorePath).toURI().toURL();
        return SSLContextBuilder.create()
                .loadTrustMaterial(keyStoreURL, keystorePassword.toCharArray())
                .loadKeyMaterial(keyStoreURL, keystorePassword.toCharArray(), keystorePassword.toCharArray())
                .build();
    }

    public static SSLContext createClientSSLContext() throws Exception {
    	final URL keyStoreURL = new File(truststorePath).toURI().toURL();
        return SSLContextBuilder.create()
                .loadTrustMaterial(keyStoreURL, truststorePassword.toCharArray())
                .build();
    }
}
