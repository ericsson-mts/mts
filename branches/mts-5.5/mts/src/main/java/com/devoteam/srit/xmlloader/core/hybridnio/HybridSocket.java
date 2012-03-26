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

package com.devoteam.srit.xmlloader.core.hybridnio;

import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * This class, with the help of HybridInputStream and HybridOutputStream is used
 * to re-create the behaviour of a blocking socket (writing and reading based on
 * blocking stream) but with and underlying NIO-Reactor.
 *
 * The interface HybridSocketInputHandler is used to fire event when there are data
 * on the InputStream. That way it is possible to only use a pool of a fixed number
 * of threads in order to handle the parsings. There will alway be a fixed number
 * of thread even if it is a little more than with "pure" NIO.
 *
 * @author gpasquiers
 */
public class HybridSocket extends Socket implements IOHandler {

    private Socket socket;

    private HybridInputStream hybridInputStream;

    private HybridOutputStream hybridOutputStream;

    private HybridSocketInputHandler outputHandler;

    private static final Scheduler parsingScheduler = new Scheduler(10);

    private boolean parsingPending = false;

    private boolean exceptionPending = false;

    private boolean _continue = true;

    private final HybridSocket _this = this;

    /**
     * This is the parsing task for this HybridSocket. This task will be plannified
     * within the parsingScheduler if:
     *  - the task isn't already plannified / running
     *  - there is at least one byte of available data in the inputstream
     *  - there is an exception pending
     */
    private final Task parsingTask = new Task(){
        public void execute()
        {
            // the implementation of the handle() method of the interface
            // should parse one message (and only one) from the inputstream
            _this._continue = _this.outputHandler.handle(_this);

           synchronized(_this)
           {
               if(_continue && (hybridInputStream.size() > 0 || (exceptionPending == true && hybridInputStream.size() != -1)))
               {
                   parsingScheduler.execute(this, false);
               }
               else parsingPending = false;
           }
        }
    };

    public HybridSocket(HybridSocketInputHandler outputHandler) throws Exception
    {
        this.outputHandler = outputHandler;
    }

    @Override
    public InputStream getInputStream()
    {
        return this.hybridInputStream;
    }

    @Override
    public OutputStream getOutputStream()
    {
        return this.hybridOutputStream;
    }

    private SelectionKey selectionKey;
    private SocketChannel socketChannel;
    private ByteBuffer buffer;

    public void init(SelectionKey selectionKey, SelectableChannel channel)
    {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) channel;
        this.hybridInputStream = new HybridInputStream();
        this.hybridOutputStream = new HybridOutputStream(this.selectionKey, channel);
        this.socket = socketChannel.socket();
        try
        {
            this.buffer = ByteBuffer.allocateDirect(socket.getReceiveBufferSize());
        }
        catch(Exception e)
        {
            this.buffer = ByteBuffer.allocateDirect(10240);
        }
        this.outputHandler.init(this);
    }

    /**
     * This method is called when there is at least one byte to read from the channel.
     *
     * NB for SSL: since we did not register the SSLSocketChannel but the AdapteeChannel
     * (the underlying SocketChannel), it is possible to have available data in this
     * underlying SocketChannel without having app data to read from the SSLSocketChannel
     * (during the certificate negociation).
     *
     * This method reads the data from the SocketChannel and then feeds the HybridInputStream
     * with it. If an exception happens it will also feed the HybridInputStream with it
     * since that exception should be thrown by the parsing thread (that reads from the
     * HybridInputStream) for it to be correctly handled by the app.
     */
    public void inputReady()
    {
        try
        {
            int len;
            do
            {
                // read the data from the socketChannel to a ByteBuffer
                this.buffer.clear();
                len = this.socketChannel.read(buffer);
                // either feed ByteBuffer or Exception to the HybridInputStream
                if(len > 0) this.hybridInputStream.feed(buffer);
                if(len == -1) throw new SocketException("connection closed");

                // special thing for SSL, trigger writes if there are encrypted datas ready (certificates...)
                if (socketChannel instanceof SSLSocketChannel && len == 0)
                {
                    if (((SSLSocketChannel) socketChannel).encrypted() != 0)
                    {
                        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                        selectionKey.selector().wakeup();
                    }
                }
            }
            while(len == this.buffer.capacity());
        }
        catch(Exception e)
        {
            try{ this.socketChannel.close(); }catch(Exception ee){}
            this.selectionKey.cancel();
            this.hybridInputStream.feed(e);
            this.hybridOutputStream.feed(e);
            this.exceptionPending = true;
        }
        
        // try to schedule a parsing if the necessary conditions are satisfied
        synchronized(this)
        {
            if(_continue && parsingPending == false && (hybridInputStream.size() > 0 || (exceptionPending == true && hybridInputStream.size() != -1)))
            {
                parsingPending = true;
                parsingScheduler.execute(parsingTask, false);
            }
        }
    }


    private ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

    /**
     * This methods handles writes from the HybridOutputStream to the SocketChannel.
     *
     * In the same way has the inputReader method, any exception will be fed to both the
     * HybridOutputStream and the HybridInputStream.
     */
    public void outputReady()
    {
        try
        {
            ByteBuffer writeBuffer = this.hybridOutputStream.consume();
            int len = 0;
            if(null != writeBuffer)
            {
                len = socketChannel.write(writeBuffer);
                if(len > 0)
                {
                    this.hybridOutputStream.bufferedData().down(len);
                    this.outputReady();
                }
            }
            else if(socketChannel instanceof SSLSocketChannel)
            {
                // ssl specific
                // if outputReady is called it is that we asked it; if there is
                // nothing to write it means that we had some encrypted data to
                // write (certificates negociation) even if there is nothing in
                // the HybridOutputStream.
                len = socketChannel.write(emptyBuffer);

                if (((SSLSocketChannel) socketChannel).encrypted() == 0)
                {
                    selectionKey.interestOps(selectionKey.interestOps() & (0xff - SelectionKey.OP_WRITE));
                }
            }
        }
        catch(Exception e)
        {
            try{ this.socketChannel.close(); }catch(Exception ee){}
            this.selectionKey.cancel();
            this.hybridInputStream.feed(e);
            this.hybridOutputStream.feed(e);
            this.exceptionPending = true;

            // if an error occured while writing, we feed the exception to both streams.
            // however we also have to planify a parsing in order to have the handler
            // "notified" of the exception.
            synchronized(this)
            {
                if(_continue && parsingPending == false && (hybridInputStream.size() > 0 || (exceptionPending == true && hybridInputStream.size() != -1)))
                {
                    parsingPending = true;
                    parsingScheduler.execute(parsingTask, false);
                }
            }
        }
    }

    public void connectReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public synchronized void close() throws IOException
    {
        this.socketChannel.close();
        socket.close();
    }


    // <editor-fold defaultstate="collapsed" desc="Socket delegated methods">
    public int hashCode()
    {
        return socket.hashCode();
    }

    public boolean equals(Object obj)
    {
        return socket.equals(obj);
    }

    public String toString()
    {
        return socket.toString();
    }

    public void shutdownOutput() throws IOException
    {
        socket.shutdownOutput();
    }

    public void shutdownInput() throws IOException
    {
        socket.shutdownInput();
    }

    public void setTrafficClass(int tc) throws SocketException
    {
        socket.setTrafficClass(tc);
    }

    public void setTcpNoDelay(boolean on) throws SocketException
    {
        socket.setTcpNoDelay(on);
    }

    public synchronized void setSoTimeout(int timeout) throws SocketException
    {
        socket.setSoTimeout(timeout);
    }

    public void setSoLinger(boolean on, int linger) throws SocketException
    {
        socket.setSoLinger(on, linger);
    }

    public synchronized void setSendBufferSize(int size) throws SocketException
    {
        socket.setSendBufferSize(size);
    }

    public void setReuseAddress(boolean on) throws SocketException
    {
        socket.setReuseAddress(on);
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException
    {
        socket.setReceiveBufferSize(size);
    }

    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
    {
        socket.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    public void setOOBInline(boolean on) throws SocketException
    {
        socket.setOOBInline(on);
    }

    public void setKeepAlive(boolean on) throws SocketException
    {
        socket.setKeepAlive(on);
    }

    public void sendUrgentData(int data) throws IOException
    {
        socket.sendUrgentData(data);
    }

    public boolean isOutputShutdown()
    {
        return socket.isOutputShutdown();
    }

    public boolean isInputShutdown()
    {
        return socket.isInputShutdown();
    }

    public boolean isConnected()
    {
        return socket.isConnected();
    }

    public boolean isClosed()
    {
        return socket.isClosed();
    }

    public boolean isBound()
    {
        return socket.isBound();
    }

    public int getTrafficClass() throws SocketException
    {
        return socket.getTrafficClass();
    }

    public boolean getTcpNoDelay() throws SocketException
    {
        return socket.getTcpNoDelay();
    }

    public synchronized int getSoTimeout() throws SocketException
    {
        return socket.getSoTimeout();
    }

    public int getSoLinger() throws SocketException
    {
        return socket.getSoLinger();
    }

    public synchronized int getSendBufferSize() throws SocketException
    {
        return socket.getSendBufferSize();
    }

    public boolean getReuseAddress() throws SocketException
    {
        return socket.getReuseAddress();
    }

    public SocketAddress getRemoteSocketAddress()
    {
        return socket.getRemoteSocketAddress();
    }

    public synchronized int getReceiveBufferSize() throws SocketException
    {
        return socket.getReceiveBufferSize();
    }

    public int getPort()
    {
        return socket.getPort();
    }

    public boolean getOOBInline() throws SocketException
    {
        return socket.getOOBInline();
    }

    public SocketAddress getLocalSocketAddress()
    {
        return socket.getLocalSocketAddress();
    }

    public int getLocalPort()
    {
        return socket.getLocalPort();
    }

    public InetAddress getLocalAddress()
    {
        return socket.getLocalAddress();
    }

    public boolean getKeepAlive() throws SocketException
    {
        return socket.getKeepAlive();
    }

    public InetAddress getInetAddress()
    {
        return socket.getInetAddress();
    }

    public SocketChannel getChannel()
    {
        return socket.getChannel();
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException
    {
        socket.connect(endpoint, timeout);
    }

    public void connect(SocketAddress endpoint) throws IOException
    {
        socket.connect(endpoint);
    }

    public void bind(SocketAddress bindpoint) throws IOException
    {
        socket.bind(bindpoint);
    }

    public void acceptReady()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    // </editor-fold>
}
