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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is an implementation of an InputStream that is fed by ByteBuffers.
 * It can also be fed by an Exception. The exception will only be thrown once there
 * is no data left to read.
 * @author gpasquiers
 */
public class HybridInputStream extends InputStream
{
    // This list contains all the bytes that this inputstream returns.
    private LinkedBlockingQueue<ByteBuffer> bytes;

    // We just keep that reference for optimization
    private ByteBuffer currentBuffer;

    private Exception currentException;


    private final Object sizeLock;
    
    // Current size of the inputstream (available datas).
    private int size;

    public HybridInputStream()
    {
        sizeLock = new Object();
        currentBuffer = null;
        currentException = null;
        bytes = new LinkedBlockingQueue();
        size = 0;
    }

    /**
     * This method copy the byteBuffer and adds it to the queue.
     * We copy it because the HybridSocket that feeds this class re-uses the same
     * buffer again and again.
     * @param byteBuffer
     */
    public void feed(ByteBuffer byteBuffer)
    {
        byteBuffer.flip();

        synchronized(sizeLock)
        {
            size += byteBuffer.remaining();
        }

        ByteBuffer aByteBuffer = ByteBuffer.allocateDirect(byteBuffer.remaining());

        aByteBuffer.clear();
        
        
        aByteBuffer.put(byteBuffer);
        aByteBuffer.flip();
        aByteBuffer.rewind();

        bytes.offer(aByteBuffer);
    }

    /**
     * Feed an exception that the read() method will throw once there is no data
     * left. There is no check, but once an exception has been fed, the method
     * feed(ByteBuffer) should not be called ever again.
     *
     * An empty ByteBuffer is added to the queue to wake up the thread that could
     * be waiting on queue.take() so that it can see there is an exception (and no data).
     *
     * @param exception
     */
    public void feed(Exception exception)
    {
        this.currentException = exception;
        this.bytes.add(ByteBuffer.allocate(0));
    }

    public int size()
    {
        return size;
    }

    private ByteBuffer currentByteBuffer() throws Exception
    {
        if(null == currentBuffer || !currentBuffer.hasRemaining())
        {
            currentBuffer = bytes.take();

            if(size == 0 && null != currentException)
            {
                this.size = -1;
                throw currentException;
            }
        }

        return currentBuffer;
    }


    /**
     * Try to fill the byte[] with data from the ByteBuffers.
     *
     * Sockets's InputStream have a special behaviour. This method is supposed to
     * block until either the byte[] buffer is full or the stream ended.
     *
     * However in Sockets this method will only block if there is no data available.
     * When there is one or more bytes available it will fill the byte[] buffer as
     * much as possible and than return.
     *
     * @param b
     * @param off
     * @param length
     * @return
     * @throws IOException
     */
    @Override
    public int read(byte b[], int off, int length) throws IOException
    {
        // By calling this method we ensure we will return at least one byte or
        // an exception if there was one and no byte available.
        int index =0;
        b[off + index++] = readByte();

        // We then read as much bytes as possible, depending on b[] size and
        // the available datas.
        try
        {
            while(size > 0 && index < length)
            {
                ByteBuffer buffer = currentByteBuffer();

                int toRead = Math.min(length - index, buffer.remaining());

                buffer.get(b, off + index, toRead);

                index += toRead;
                synchronized(sizeLock)
                {
                    size -= toRead;
                }
            }
        }
        catch(Exception e)
        {
            throw new IOException(e.getMessage());
        }
        return index;
    }

    /**
     * Only read one byte
     * @return
     * @throws IOException
     */
    @Override
    public int read() throws IOException
    {
        return 0xff & readByte();
    }

    /**
     * Extracts bytes from the buffers one by one or throw an exception.
     * Blocks if there is nothing .
     * @return
     * @throws IOException
     */
    private byte readByte() throws IOException
    {
        try
        {
            if(currentByteBuffer().hasRemaining())
            {
                synchronized(sizeLock)
                {
                    size --;
                }

                return currentByteBuffer().get();
            }
            else
            {
                return readByte();
            }
        }
        catch(Exception e)
        {
            throw new IOException(e.getMessage());
        }
    }
}
