/*
 * ChannelPcp.java
 *
 * Created on 26 juin 2007, 10:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.pcp;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.tcp.ChannelTcp;
import com.portal.pcm.EBufException;
import com.portal.pcm.FList;
import com.portal.pcm.PortalContext;
import java.util.Properties;

public class ChannelPcp extends Channel //implements Runnable
{
    private Channel channel = null;
    private PortalContext context = null;
    private String infranetConnection = null;
    private String loginType = null;

    // --- constructor --- //
    public ChannelPcp(String name, String aLocalHost, String aLocalPort, String aRemoteHost, String aRemotePort, String aProtocol, String aInfranetConnection, String aLoginType) throws Exception {
    	super(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        channel = new ChannelTcp(name, aLocalHost, aLocalPort, aRemoteHost, aRemotePort, aProtocol);
        context = new PortalContext();
        infranetConnection = aInfranetConnection;
        loginType = aLoginType;
    }

    // --- basic methods --- //
    public boolean open() throws Exception {
        if(!isServer()) {
            Properties prop = new Properties();

            if(infranetConnection != null) {
                prop.setProperty("infranet.connection", infranetConnection);
                prop.setProperty("infranet.login.type", loginType);
            }
            else {
                prop.setProperty("infranet.connection", Config.getConfigByName("pcp.properties").getString("infranet.connection"));
                prop.setProperty("infranet.login.type", Config.getConfigByName("pcp.properties").getString("infranet.login.type"));
            }
            System.out.println("open of channel not server");//TODO: freeze in connect
            context.connect(prop);
            System.out.println("open of channel not server done");
        }

        return true;
    }
    
    /** Send a Msg to Channel */
    public boolean sendMessage(Msg msg) throws Exception{ 
        if (msg.getChannel() == null)
            msg.setChannel(this);

        System.out.println("send message in channel");
        if(!isServer()) {
            try{
                context.opcode(((MsgPcp)msg).getOpCode(), ((MsgPcp)msg).getFlist());//send an flist to the server
                //Log that message is sent
            }
            catch(EBufException e)
            {
                //get response ???
                FList resp = e.getArgsFList();
                System.out.println("exception catched when request sent from channel");
                //e is confidered as the response???
                e.printStackTrace();
            }
            System.out.println("message sent");
        }
        return true;
    }
    
    public boolean close(){
        try {
            context.close(true);
        } catch (Exception e) {
            // nothing to do
        }
        context = null;
        return true;
    }

    public boolean isServer()
    {
        return (((ChannelTcp)channel).getListenpointTcp() != null);
    }

    public Channel getChannel() {
        return channel;
    }

    /** Get the transport protocol of this message */
    public String getTransport() 
    {
        return StackFactory.PROTOCOL_TCP;
    }

}
