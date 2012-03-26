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

import com.devoteam.srit.xmlloader.core.utils.CountingLatch;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is used to send data through the socket.
 *
 * @author gpasquiers
 */
public class HybridOutputStream extends OutputStream
{

    private Queue<ByteBuffer> bytes;

    private CountingLatch bufferedData;

    private SelectionKey selectionKey;

    private SocketChannel socketChannel;

    private Exception currentException;

    private boolean couldNotSendEverything;

    public HybridOutputStream(SelectionKey selectionKey, SelectableChannel channel)
    {
        this.selectionKey = selectionKey;
        this.socketChannel = (SocketChannel) channel;
        
        bytes = new LinkedBlockingQueue();
        bufferedData = new CountingLatch(0);

        couldNotSendEverything = false;
    }

    /**
     * Used to enque bytes and then either write them at once into the SocketChannel
     * or buffer them and change the InterestsOps in order to have the HybridSocket
     * come get and write them into the SocketChannel once it is ready.
     *
     * This method is non-blocking and adds everything into a queue so the method
     * flush() should be used to make sure datas were sent.
     *
     * @param b
     * @param off
     * @param len
     * @throws IOException
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        if(null != currentException)
        {
            throw new IOException(currentException.getMessage());
        }

        // we first wrap those bytes into a ByteBuffer
        ByteBuffer buffer = ByteBuffer.wrap(b, off, len);

        // if there is no data buffered, we can try to write those datas in the
        // channel now (doing this does a great improvement in performances)
        if(0 == bufferedData.value())
        {
            socketChannel.write(buffer);
        }

        // we then queue the remaining datas
        if(buffer.hasRemaining())
        {
            bytes.add(buffer);
            bufferedData.up(buffer.remaining());
        }

        // if there is data to read we then change the interrets ops of the selection key
        // for the selector to fire event when the socketChannel is ready for writes.
        synchronized(this)
        {
            if (!bytes.isEmpty())
            {
                if ((selectionKey.interestOps() | SelectionKey.OP_WRITE) != SelectionKey.OP_WRITE)
                {
                    selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_WRITE);
                    selectionKey.selector().wakeup();
                }
            }
        }
    }

    /**
     * Feed an exception that will be thrown on flush().
     * @param exception
     */
    public void feed(Exception exception)
    {
        this.currentException = exception;
        if(bufferedData.value()>0) couldNotSendEverything = true;
        bufferedData.down(bufferedData.value());
    }

    /**
     * Writing byte per byte has very bad performances et would need a complicated
     * coding to optimize it. Since it isn't used yet, we'll do it later.
     * @param b
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException
    {
        throw new UnsupportedOperationException("not yet");
    }

    /**
     * This method (called by the HybridSocket) returns the first non-empty ByteBuffer
     * to send through the SocketChannel.
     *
     * @return
     * @throws IOException
     */
    protected ByteBuffer consume() throws IOException
    {
        if(null != bytes.peek() && !bytes.peek().hasRemaining())
        {
            bytes.remove();
        }

        synchronized(this)
        {
            if(bytes.isEmpty())
            {
                if(socketChannel instanceof SSLSocketChannel)
                {
                    // special case for SSLSocketChannel: it is possible to have
                    // data to write even when there is app data (certificates ...)
                    if(((SSLSocketChannel)socketChannel).encrypted() == 0)
                    {
                        selectionKey.interestOps(selectionKey.interestOps() & (0xffff - SelectionKey.OP_WRITE));
                    }
                }
                else
                {
                    selectionKey.interestOps(selectionKey.interestOps() & (0xffff - SelectionKey.OP_WRITE));
                }
            }
        }

        return bytes.peek();
    }

    protected CountingLatch bufferedData()
    {
        return this.bufferedData;
    }

    /**
     * This method will block until there is no data left to send. It can alos
     * be unblocked when an exception is fed to this HybridOutputStream.
     * It will then throw it.
     */
    @Override
    public void flush()
    {
        try
        {
            this.bufferedData.waitforZero();

            if(couldNotSendEverything && null != currentException)
            {
                throw currentException;
            }
            
            if(this.socketChannel instanceof SSLSocketChannel)
            {
                ((SSLSocketChannel)this.socketChannel).flush();
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
