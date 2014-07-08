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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
//import java.net.SocketOption;
// import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;
// import java.util.Set;


import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

/**
 * A secure socket channel implementation adding SSL engine based cryprography
 * to an adapted non-secure concrete <code>SocketChannel</code>
 * implementation.
 * 
 * <p>
 * This implementation extends abstract <code>SocketChannel</code> and
 * forwards applicable calls to methods of the adapted concrete implementation.
 * It also implements <code>AdaptableChannel</code> as selectors typically
 * don't accept channel implementations from other vendors, so the selector
 * registration must be done with the adaptee channel.
 * </p>
 * 
 * <p>
 * The additional <code>SecureChannel</code> methods help handshake and
 * shutdown even though they can also be handled by the read, write and close
 * methods. Note that the handshake method performs only one such channel read
 * or write operation during each call that is enabled by the ready set
 * parameter. Any other way of action seems to cause browsers to block
 * occasionally.
 * </p>
 * 
 * @author Ilkka Priha
 */
public class SSLSocketChannel extends SocketChannel implements
    SecureChannel, AdaptableChannel
{
    /**
     * The unsecure socket channel.
     */
    private final SocketChannel socketChannel;

    /**
     * The SSL engine to apply.
     */
    private final SSLEngine sslEngine;

    /**
     * The active SSL session.
     */
    private final SSLSession sslSession;

    /**
     * The minimum cache size.
     */
    private final int minCacheSize;

    /**
     * The decrypted input cache.
     */
    private final ByteBuffer[] inputCache;

    /**
     * The minimum buffer size.
     */
    private final int minBufferSize;

    /**
     * The encrypted input buffer.
     */
    private final ByteBuffer[] inputBuffer;

    /**
     * The encrypted output buffer.
     */
    private final ByteBuffer[] outputBuffer;

    /**
     * An empty buffer for handshaking.
     */
    private final ByteBuffer emptyBuffer;

    /**
     * The engine handshake status.
     */
    private SSLEngineResult.HandshakeStatus handshake;

    /**
     * The initial handshake ops.
     */
    private int initialized = -1;

    /**
     * The engine shutdown flag.
     */
    private boolean shutdown;

    /**
     * Construct a new channel.
     * 
     * @param channel the unsecure socket channel.
     * @param engine the SSL engine.
     */
    public SSLSocketChannel(SocketChannel channel, SSLEngine engine)
    {
        super(channel.provider());
        socketChannel = channel;
        sslEngine = engine;
        sslSession = engine.getSession();
        minCacheSize = sslSession.getApplicationBufferSize();
        inputCache = new ByteBuffer[]{ ByteBuffer.allocate(minCacheSize) };
        minBufferSize = sslSession.getPacketBufferSize();
        inputBuffer = new ByteBuffer[]{ ByteBuffer.allocate(minBufferSize) };
        outputBuffer = new ByteBuffer[]{ ByteBuffer.allocate(minBufferSize) };
        emptyBuffer = ByteBuffer.allocate(0);

        // Set initial values.
        inputCache[0].limit(0);
        outputBuffer[0].limit(0);
    }

    public Socket socket()
    {
        return socketChannel.socket();
    }

    public boolean isConnected()
    {
        return socketChannel.isConnected();
    }

    public boolean isConnectionPending()
    {
        return socketChannel.isConnectionPending();
    }

    public boolean connect(SocketAddress remote) throws IOException
    {
        return socketChannel.connect(remote);
    }

    public boolean finishConnect() throws IOException
    {
        return socketChannel.finishConnect();
    }

    public synchronized int read(ByteBuffer dst) throws IOException
    {
        if (socketChannel.socket().isInputShutdown())
        {
            throw new ClosedChannelException();
        }
        else if (initialized != 0)
        {
            handshake(SelectionKey.OP_READ);
            
            return 0;
        }
        else if (shutdown)
        {
            shutdown();
            return 0;
        }
        else if (sslEngine.isInboundDone())
        {
            return -1;
        }
        else if ((fill(inputBuffer[0]) < 0) && (inputBuffer[0].position() == 0))
        {
            return -1;
        }

        SSLEngineResult result;
        Status status;
        do
        {
            if (!prepare(inputCache, minCacheSize))
            {
                // Overflow!
                break;
            }

            inputBuffer[0].flip();
            try
            {
                result = sslEngine.unwrap(inputBuffer[0], inputCache[0]);
            }
            finally
            {
                inputBuffer[0].compact();
                inputCache[0].flip();
            }

            status = result.getStatus();
            if ((status == Status.OK) || (status == Status.BUFFER_UNDERFLOW))
            {
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK)
                {
                    runTasks();
                }
            }
            else
            {
                if (status == Status.CLOSED)
                {
                    shutdown();
                }

                throw new IOException("Read error '" + result.getStatus()
                    + '\'');
            }
        } while ((inputBuffer[0].position() != 0)
            && (status != Status.BUFFER_UNDERFLOW));

        int n = inputCache[0].remaining();
        if (n > 0)
        {
            if (n > dst.remaining())
            {
                n = dst.remaining();
            }
            for (int i = 0; i < n; i++)
            {
                dst.put(inputCache[0].get());
            }
        }
        return n;
    }

    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException
    {
        long n = 0;
        for (int i = offset; i < length; i++)
        {
            if (dsts[i].hasRemaining())
            {
                int x = read(dsts[i]);
                if (x > 0)
                {
                    n += x;
                    if (!dsts[i].hasRemaining())
                    {
                        break;
                    }
                }
                else
                {
                    if ((x < 0) && (n == 0))
                    {
                        n = -1;
                    }
                    break;
                }
            }
        }
        return n;
    }

    public synchronized int write(ByteBuffer src) throws IOException
    {
        if (socketChannel.socket().isOutputShutdown())
        {
            throw new ClosedChannelException();
        }
        else if (initialized != 0)
        {
            handshake(SelectionKey.OP_WRITE);
            return 0;
        }
        else if (shutdown)
        {
            shutdown();
            return 0;
        }

        // Check how much to write.
        int t = src.remaining();
        int n = 0;

        // Write as much as we can.
        SSLEngineResult result;
        Status status;
        do
        {
            if (!prepare(outputBuffer, minBufferSize))
            {
                // Overflow!
                break;
            }

            inputBuffer[0].flip();
            try
            {
                result = sslEngine.wrap(src, outputBuffer[0]);
            }
            finally
            {
                outputBuffer[0].flip();
            }
            n += result.bytesConsumed();
            status = result.getStatus();
            if (status == Status.OK)
            {
                if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK)
                {
                    runTasks();
                }
            }
            else
            {
                if (status == Status.CLOSED)
                {
                    shutdown();
                }

                throw new IOException("Write error '" + result.getStatus() + '\'');
            }
        } while (n < t);

        // Try to flush what we got.
        flush();

        return n;
    }

    public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException
    {
        long n = 0;
        for (int i = offset; i < length; i++)
        {
            if (srcs[i].hasRemaining())
            {
                int x = write(srcs[i]);
                if (x > 0)
                {
                    n += x;
                    if (!srcs[i].hasRemaining())
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }
        }
        return n;
    }

    public Channel getAdapteeChannel()
    {
        return socketChannel;
    }

    public boolean finished()
    {
        return initialized == 0;
    }

    public int encrypted()
    {
        return outputBuffer[0].remaining();
    }

    public int decrypted()
    {
        return inputCache[0].remaining();
    }

    public synchronized int handshake(int ops) throws IOException
    {
        if (initialized != 0)
        {
            if (handshake == null)
            {
                sslEngine.beginHandshake();
                handshake = sslEngine.getHandshakeStatus();
            }

            if (outputBuffer[0].hasRemaining())
            {
                if ((ops & SelectionKey.OP_WRITE) != 0)
                {
                    flush(outputBuffer[0]);
                    if (outputBuffer[0].hasRemaining())
                    {
                        initialized = SelectionKey.OP_WRITE;
                    }
                    else
                    {
                        initialized = SelectionKey.OP_READ;
                    }
                    ops = 0;
                }
                else
                {
                    initialized = SelectionKey.OP_WRITE;
                }
            }
            else
            {
                initialized = SelectionKey.OP_READ;
            }

            while (initialized != 0)
            {
                if (handshake == HandshakeStatus.FINISHED)
                {
                    initialized = 0;
                }
                else if (handshake == HandshakeStatus.NEED_TASK)
                {
                    handshake = runTasks();
                }
                else if (handshake == HandshakeStatus.NEED_UNWRAP)
                {
                    ops = unwrap(ops);
                    if (ops != 0)
                    {
                        initialized = ops;
                        return initialized;
                    }
                }
                else if (handshake == HandshakeStatus.NEED_WRAP)
                {
                    ops = wrap(ops);
                    if (ops != 0)
                    {
                        initialized = ops;
                        return initialized;
                    }
                }
                else
                {
                    // NOT_HANDSHAKING
                    throw new IllegalStateException("Unexpected handshake status '" + handshake + '\'');
                }
            }
        }
        return initialized;
    }

    public synchronized boolean shutdown() throws IOException
    {
        shutdown = true;

        if (!sslEngine.isOutboundDone())
        {
            sslEngine.closeOutbound();
        }

        // Try to "fire-and-forget" the closed notification (RFC2616).
        SSLEngineResult result;
        if (prepare(outputBuffer, minBufferSize))
        {
            result = sslEngine.wrap(emptyBuffer, outputBuffer[0]);
            if (result.getStatus() != Status.CLOSED)
            {
                throw new SSLException("Unexpected shutdown status '" + result.getStatus() + '\'');
            }
            outputBuffer[0].flip();
        }
        else
        {
            result = null;
        }
        flush(outputBuffer[0]);
        return !outputBuffer[0].hasRemaining() && (result != null)
            && (result.getHandshakeStatus() != HandshakeStatus.NEED_WRAP);
    }

    public synchronized void flush() throws IOException
    {
        flush(outputBuffer[0]);
    }

    public String toString()
    {
        return "SSLSocketChannel[" + socket().toString() + "]";
    }
    
    /**
     * Gets the SSL session.
     * 
     * @return the session.
     */
    public SSLSession getSession()
    {
        return sslSession;
    }

    protected synchronized void implCloseSelectableChannel() throws IOException
    {
        try
        {
            shutdown();
        }
        catch (Exception x)
        {
        }

        socketChannel.close();
        notifyAll();
    }

    protected void implConfigureBlocking(boolean block) throws IOException
    {
        socketChannel.configureBlocking(block);
    }

    /**
     * Handshake unwrap.
     * 
     * @param ops the current ready operations set.
     * @return the interest set to continue or 0 if finished.
     * @throws IOException on I/O errors.
     */
    private synchronized int unwrap(int ops) throws IOException
    {
        // Fill the buffer, if applicable.
        if ((ops & SelectionKey.OP_READ) != 0)
        {
            fill(inputBuffer[0]);
        }

        // Unwrap the buffer.
        SSLEngineResult result;
        Status status;
        do
        {
            // Prepare the input cache, although no app
            // data should be produced during handshake.
            prepare(inputCache, minCacheSize);
            inputBuffer[0].flip();
            try
            {
                result = sslEngine.unwrap(inputBuffer[0], inputCache[0]);
            }
            finally
            {
                inputBuffer[0].compact();
                inputCache[0].flip();
            }
            handshake = result.getHandshakeStatus();

            status = result.getStatus();
            if (status == Status.OK)
            {
                if (handshake == HandshakeStatus.NEED_TASK)
                {
                    handshake = runTasks();
                }
            }
            else if (status == Status.BUFFER_UNDERFLOW)
            {
                return SelectionKey.OP_READ;
            }
            else
            {
                // BUFFER_OVERFLOW/CLOSED
                throw new IOException("Handshake failed '" + status + '\'');
            }
        } while (handshake == HandshakeStatus.NEED_UNWRAP);

        return 0;
    }

    /**
     * Handshake wrap.
     * 
     * @param ops the current ready operations set.
     * @return the interest set to continue or 0 if finished.
     * @throws IOException on I/O errors.
     */
    private synchronized int wrap(int ops) throws IOException
    {
        // Prepare the buffer.
        if (prepare(outputBuffer, minBufferSize))
        {
            // Wrap the buffer.
            SSLEngineResult result;
            Status status;
            try
            {
                result = sslEngine.wrap(emptyBuffer, outputBuffer[0]);
            }
            finally
            {
                outputBuffer[0].flip();
            }
            handshake = result.getHandshakeStatus();

            status = result.getStatus();
            if (status == Status.OK)
            {
                if (handshake == HandshakeStatus.NEED_TASK)
                {
                    handshake = runTasks();
                }
            }
            else
            {
                // BUFFER_OVERFLOW/BUFFER_UNDERFLOW/CLOSED
                throw new IOException("Handshake failed '" + status + '\'');
            }
        }

        // Flush the buffer, if applicable.
        if ((ops & SelectionKey.OP_WRITE) != 0)
        {
            flush(outputBuffer[0]);
        }

        return outputBuffer[0].hasRemaining() ? SelectionKey.OP_WRITE : 0;
    }

    /**
     * Fills the specified buffer.
     * 
     * @param in the buffer.
     * @return the number of read bytes.
     * @throws IOException on I/O errors.
     */
    private synchronized long fill(ByteBuffer in) throws IOException
    {
        try
        {
            long n = socketChannel.read(in);
            if (n < 0)
            {
                // EOF reached.
                sslEngine.closeInbound();
            }
            return n;
        }
        catch (IOException x)
        {
            // Can't read more bytes...
            sslEngine.closeInbound();
            throw x;
        }
    }

    /**
     * Flushes the specified buffer.
     * 
     * @param out the buffer.
     * @return the number of written bytes.
     * @throws IOException on I/O errors.
     */
    private synchronized long flush(ByteBuffer out) throws IOException
    {
        try
        {
            // Flush only if bytes available.
            return out.hasRemaining() ? socketChannel.write(out) : 0;
        }
        catch (IOException x)
        {
            // Can't write more bytes...
            sslEngine.closeOutbound();
            shutdown = true;
            throw x;
        }
    }

    /**
     * Runs delegated handshaking tasks.
     * 
     * @return the handshake status.
     */
    private SSLEngineResult.HandshakeStatus runTasks()
    {
        Runnable runnable;
        while ((runnable = sslEngine.getDelegatedTask()) != null)
        {
            runnable.run();
        }
        return sslEngine.getHandshakeStatus();
    }

    /**
     * Prepares the specified buffer for the remaining number of bytes.
     * 
     * @param src the source buffer.
     * @param remaining the number of bytes.
     * @return true if prepared, false otherwise.
     */
    private boolean prepare(ByteBuffer[] src, int remaining)
    {
        ByteBuffer bb = src[0];
        if (bb.compact().remaining() < remaining)
        {
            int position = bb.position();
            int capacity = position + remaining;
            if (capacity <= 2 * remaining)
            {
                bb = ByteBuffer.allocate(capacity);
                if (position > 0)
                {
                    src[0].flip();
                    bb.put(src[0]);
                    src[0] = bb;
                }
            }
            else
            {
                bb.flip();
                bb = null;
            }
        }
        return bb != null;
    }
    
    public SSLSocketChannel setOption(java.net.SocketOption arg0, Object arg1) throws IOException
    {
    	return null;
    }

    public Set<SocketOption<?>>  supportedOptions()
    {
    	return null;
    }

    public SocketAddress getLocalAddress() throws IOException
    {
    	return null;
    }
    
    public SocketAddress getRemoteAddress() throws IOException
    {
    	return null;
    }
    
    public <T> T getOption(SocketOption<T> name) throws IOException
    {
    	return null;
    }

     public SSLSocketChannel bind(SocketAddress local)
     {
    	return null;
     }
    
     public SocketChannel shutdownInput() throws IOException
     {
    	return null;
     }

     public SocketChannel shutdownOutput() throws IOException
     {
    	return null;
     }

}
