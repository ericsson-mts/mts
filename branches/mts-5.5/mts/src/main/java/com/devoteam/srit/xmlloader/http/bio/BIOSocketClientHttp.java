/*
 * SocketClientReceiver.java
 *
 * Created on 26 juin 2007, 10:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http.bio;
import org.apache.http.impl.DefaultHttpClientConnection;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import com.devoteam.srit.xmlloader.http.MsgHttp;
import com.devoteam.srit.xmlloader.http.SocketClientHttp;
import org.apache.http.HttpResponse;

/**
 *
 * @author sngom
 */
public class BIOSocketClientHttp extends SocketClientHttp implements Runnable
{

    /** Creates a new instance of SocketClientReceiver */
    public BIOSocketClientHttp(DefaultHttpClientConnection aClientConnection, BIOChannelHttp connHttp)
    {
        this.clientConnection = aClientConnection;
        this.connHttp = connHttp;
    }

    public void run()
    {
        try
        {
            while (!Thread.interrupted() && clientConnection.isOpen())
            {
                HttpResponse response = clientConnection.receiveResponseHeader();
                clientConnection.receiveResponseEntity(response);

                MsgHttp msgResponse = new MsgHttp(response);

                //
                // Get corresponding msgRequest to read transactionId
                //
                if(isValid)
                {
                    MsgHttp msgRequest = requestsSent.take();
                    msgResponse.setTransactionId(msgRequest.getTransactionId());
                    msgResponse.setChannel(this.connHttp);
                    msgResponse.setType(msgRequest.getType());
                }

                //
                // Callback vers la Stack generic
                //
                StackFactory.getStack(StackFactory.PROTOCOL_HTTP).receiveMessage(msgResponse);
            }
        }
        catch(Exception e)
        {
            if(requestsSent.isEmpty())
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketClientHttp without pending messages");
            }
            else
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketClientHttp with pending messages");
            }
        }

        synchronized(this.connHttp)
        {
            try
            {
                restoreConnection();
            }
            catch(Exception e)
            {
                GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception while trying to restore connection ", this.connHttp);
            }
        }
    }

}
