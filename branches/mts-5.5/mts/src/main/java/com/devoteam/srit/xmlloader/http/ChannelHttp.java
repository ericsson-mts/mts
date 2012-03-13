/*
 * ConnectionHttp.java
 *
 * Created on 26 juin 2007, 10:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;


/**
 *
 * @author gpasquiers
 */
public abstract class ChannelHttp extends Channel
{
    protected SocketServerHttp socketServerHttp;
    protected SocketClientHttp socketClientHttp;
    protected boolean secure;

    protected int initialLocalport = 0;

    protected int nbOpens = 0;

    protected long startTimestamp = 0;
    
    public ChannelHttp(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol, boolean secure) throws Exception
    {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
        if (localPort != null)
        {
            this.initialLocalport = Integer.parseInt(localPort);
        }

        this.secure = secure;
        this.socketClientHttp = null;
        this.socketServerHttp = null;
    }
    
    public void setSocketServerHttp(SocketServerHttp socketServerHttp)
    {
        this.socketServerHttp = socketServerHttp;
    }
    
    /** Open a connexion to each Stack */
    @Override
    public abstract boolean open() throws Exception;
    
    /** Close a connexion */
    @Override
    public boolean close()
    {
        if(null != this.socketClientHttp)
        {
            try
            {
                this.socketClientHttp.shutdown();
                this.socketClientHttp = null;
            }
            catch(Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SocketClientHttp");
            }
        }
        
        if(null != this.socketServerHttp)
        {
            try
            {
                this.socketServerHttp.shutdown();
                this.socketServerHttp = null;
            }
            catch(Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing SocketServerHttp");
            }
        }
        
        return true;
    }
    
    /** Send a Msg to Channel */
    @Override
    public synchronized boolean sendMessage(Msg msg) throws Exception
    {
        if(null != this.socketClientHttp)
        {
            this.socketClientHttp.sendMessage((MsgHttp) msg);
        }
        else if(null != this.socketServerHttp)
        {
            this.socketServerHttp.sendMessage((MsgHttp) msg);
        }
        else
        {
            throw new ExecutionException("Can't send message using this connection: " + this);
        }
        this.nbOpens = 0;
        return true;
    }
    
    /** Get the transport protocol of this message */
    @Override
    public String getTransport() 
    {
    	return StackFactory.PROTOCOL_TCP;
    }
}
