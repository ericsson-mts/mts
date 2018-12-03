package com.devoteam.srit.xmlloader.http2;

import java.net.URI;
import java.util.UUID;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.protocol.Msg.ParseFromXmlContext;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author qqin
 */
public class StackHttp2 extends Stack {

	/** Constructor */
	public StackHttp2() throws Exception {
		super();
	}

	/** Creates a Channel specific to each Stack */
	@Override
	public Channel parseChannelFromXml(Element root, Runner runner, String protocol) throws Exception {	
		System.out.println("StackHttp2.parseChannelFromXml()");
		System.out.println("root = " + root.asXML());

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

        System.out.println("localHost = " + localHost + ", localPort = " + localPort + ", remoteHost = " + remoteHost + ", remotePort = " + remotePort);
        
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
		System.out.println("StackHttp2.parseMsgFromXml()");
		
		System.out.println("context = " + context.toString());
		System.out.println("root = " + root.asXML());
		System.out.println("runner = " + runner.getName());
		
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
            //TransactionId transactionId = new TransactionId(UUID.randomUUID().toString());
			//msg.setTransactionId(transactionId);
            msg.setChannel(channel);
        }
        else
        {        	
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


	/** Receive a message */
	@Override
	public boolean receiveMessage(Msg msg) throws Exception {
		System.out.println("StackHttp2.receiveMessage()");
		return super.receiveMessage(msg);
	}

}
