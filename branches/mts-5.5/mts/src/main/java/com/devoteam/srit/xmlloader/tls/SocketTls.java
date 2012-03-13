package com.devoteam.srit.xmlloader.tls;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.tcp.StackTcp;

import javax.net.ssl.SSLException;

/**
 *
 * @author fvandecasteele
 */
public class SocketTls extends Thread
{

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private ChannelTls channel;

    /** Creates a new instance of SocketClientReceiver */
    public SocketTls(Socket socket) throws Exception
    {
        this.socket = socket;

        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = socket.getOutputStream();

    }

    public void run()
    {
        Stack stack = null;
        try
        {
            stack = StackFactory.getStack(channel.getProtocol());
        }
        catch (Exception e)
        {
            return;
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTls listener thread started : ", channel);

        boolean exception = false;
        while (!exception)
        {
            try
            {
                Msg msg = stack.readFromStream(inputStream, stack.getChannel(channel.getName()));
                if (msg != null)
                {
                    if (msg.getChannel() == null)
                    {
                        msg.setChannel(channel);
                    }
                    if (msg.getListenpoint() == null)
                    {
                        msg.setListenpoint(channel.getListenpointTLS());
                    }
                    stack.getChannel(channel.getName()).receiveMessage(msg);
                }
            }
            catch (SocketException e)
            {
                exception = true;
                
				try
				{
					// Create an empty message for transport connection actions (open or close) 
					// and on server side and dispatch it to the generic stack
					((StackTls) StackFactory.getStack(StackFactory.PROTOCOL_TLS)).receiveTransportMessage("FIN-ACK", channel, null);
				}
				catch (Exception ex)
				{
					GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, ex, "Exception : SocketTcp thread", channel);
				}                
            }
            catch (SSLException e)
            {
                exception = true;
            }
            catch (Exception e)
            {
                if ((null != e.getMessage()) && e.getMessage().equalsIgnoreCase("End of stream detected"))
                {
                    exception = true;
                } else
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception : SocketTls thread", channel);
                }
            }
        }

        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "SocketTls listener thread stopped : ", channel);

        try
        {
            synchronized (this)
            {
                if (null != socket)
                {
                    StackFactory.getStack(channel.getProtocol()).closeChannel(channel.getName());
                }
            }
        }
        catch (Exception e)
        {
            // nothing to do
        }
    }

    public void setChannelTls(ChannelTls channel)
    {
        this.channel = channel;
    }

    public synchronized void send(Msg msg) throws Exception
    {
        try
        {
            {
                outputStream.write(msg.getBytesData());
                // outputStream.flush();
            }
        }
        catch (Exception e)
        {
            throw new ExecutionException("Exception : Send a message " + msg.toShortString(), e);
        }
    }

    public void close()
    {
        try
        {
            synchronized (this)
            {
                socket.close();
                socket = null;
            }
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Error while closing TLS socket");
        }
    }
}
