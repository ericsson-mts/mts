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

package com.devoteam.srit.xmlloader.imap;

import java.io.InputStream;
import java.net.Socket;
import java.util.Vector;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.protocol.Stack;

public class StackImap extends Stack
{
    public StackImap() throws Exception
    {        
        super();

        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointImap(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_IMAP);
        }
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointImap(this, root);
        return listenpoint;        
    }

    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String name = root.attributeValue("name");
        String localHost = root.attributeValue("localHost");
        String localPort = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");
        String transport = root.attributeValue("transport");

        if((remoteHost == null) || (remotePort == null))
        {
            throw new Exception("Missing one of the remoteHost or remotePort parameter to create channel.");
        }
        
        if(localHost == null)
        {
            localHost = Utils.getLocalAddress().getHostAddress();
        }

        if(transport == null)
            transport = "tcp";
        
        if(!transport.equalsIgnoreCase("tcp") && !transport.equalsIgnoreCase("tls"))
            throw new Exception("Allowed transport for IMAP channel are tcp or tls.(not " + transport +")");

        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
            return new ChannelImap(name, localHost, localPort, remoteHost, remotePort, protocol, transport);
        }
    }

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        MsgImap msg = new MsgImap(root.getText().trim());

        String channelName = root.attributeValue("channel");
        if (existsChannel(channelName))
        {
            ChannelImap channel = (ChannelImap) getChannel(channelName);

            // code imported from channel. we must now set the transaction ID BEFORE
            // sending the request (modifications on the generic stack)
            msg.setTransactionId(channel.getTransactionId());

            if(channel.isServer())//pour un server (envoi d'une reponse)
            {
                channel.checkTransationResponse(msg, channel.getChannel().getRemoteHost() + channel.getChannel().getRemotePort());
            }
            else//pour un client (envoi d'une requete)
            {
                msg = (MsgImap) channel.checkTransationRequest(msg, channel.getChannel().getLocalHost() + channel.getChannel().getLocalPort());
            }
        }
        return msg;
    }

    /** 
     * Creates a Msg specific to each Channel type
     * should become ABSTRACT later  
     */
    @Override    
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {       
        Vector<String> message = new Vector<String>();
        String messageStr = null;
        boolean needToRead = true;
        boolean dataGet = false;
        int nbCharToRead = 0;
        if(null != channel)
        {
            nbCharToRead = ((ChannelImap) channel).getAndResetNextNbCharToRead();
        }
        
        while (needToRead)
        {
            synchronized (inputStream)
            {
                messageStr = this.reader(inputStream, nbCharToRead);
            }
            
            if(nbCharToRead == 0)
            {
                String[] msgSplit = Utils.splitNoRegex(messageStr.trim(), " ");

                //check if there is n byte indicate to be read next character as {xx}
                if(msgSplit[msgSplit.length-1].matches("\\{\\d*\\}"))
                {
                    //get number of character to read
                    int index = messageStr.lastIndexOf("{");
                    int index2 = messageStr.lastIndexOf("}");
                    nbCharToRead = Integer.parseInt(messageStr.substring(index+1, index2));
                    
                    if(((ChannelImap) channel).isServer())//if channel linked to listenpoint, so is a server
                    {
                        //stop read, and store nbCharToRead for next read
                        needToRead = false;
                    }
                    ((ChannelImap) channel).setNextNbCharToRead(nbCharToRead);
                    //else continue to read
                }
                //check message is a tag request or response or a "+ " tagged response to stop read
                //or just a line for an authenticate mecanism
                else if(msgSplit[0].matches("\\p{Alnum}{1,4}")
                        || msgSplit[0].equalsIgnoreCase("+")
                        || (dataGet && ((ChannelImap) channel).isServer())
                        || ((ChannelImap) channel).isAuthenticateMecanismActually())
                {
                    needToRead = false;
                }
            }
            else
            {
                nbCharToRead = 0;
                ((ChannelImap) channel).setNextNbCharToRead(0);
                if(((ChannelImap) channel).isServer())//if channel linked to listenpoint, so is a server
                {
                    dataGet = true;
                    needToRead = false;
                }
            }
            message.add(messageStr);            
        }
        
        return new MsgImap(message);
    }
    
    private String reader(InputStream inputStream, int nbCharToRead) throws Exception 
    {
        StringBuilder buf = new StringBuilder();

        String ret;
        if(nbCharToRead == 0)
        {
            ret = Utils.readLineFromInputStream(inputStream);
        }
        else
        {
            //read n character
            byte[] tab = new byte[nbCharToRead];
            int aByte;
            int index = 0;
            while(index<tab.length && (aByte = inputStream.read()) != -1)
            {
                if(aByte >= 0) tab[index++] = (byte) aByte;
                else throw new Exception("End of stream detected");
            }
            buf.append(new String(tab));
            ret = buf.toString();
        }
        return ret;
    }

    @Override
    public Channel buildChannelFromSocket(Listenpoint listenpoint, Socket socket) throws Exception
    {
        ChannelImap channelImap = new ChannelImap("Channel #" + Stack.nextTransactionId(), listenpoint, socket);
        return channelImap;
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("imap.properties");
    }

    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }
}
