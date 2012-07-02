/**
 * Copyright (c) 2006 The Norther Organization (http://www.norther.org).
 *
 * Tammi is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * Tammi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tammi; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02111-1307 USA
 *
 * $Id: SSLServerSocketChannel.java,v 1.7 2008/06/18 14:06:29 ipriha Exp $
 */

package com.devoteam.srit.xmlloader.core.hybridnio;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * A secure server socket channel implementation enclosing accepted non-secure
 * <code>SocketChannels</code> into <code>SSLSocketChannels</code>. The
 * accepted channels must be provided by an adapted non-secure concrete
 * <code>ServerSocketChannel</code> implementation.
 * 
 * <p>
 * This implementation extends abstract <code>ServerSocketChannel</code> and
 * forwards applicable calls to methods of the adapted concrete implementation.
 * It also implements <code>AdaptableChannel</code> as selectors typically
 * don't accept channel implementations from other vendors, so the selector
 * registration must be done with the adaptee channel.
 * </p>
 * 
 * @author Ilkka Priha
 */
public class SSLServerSocketChannel extends ServerSocketChannel implements
    AdaptableChannel
{
    /**
     * The unsecure server socket channel.
     */
    private final ServerSocketChannel socketChannel;

    /**
     * The SSL context to apply.
     */
    private final SSLContext sslContext;

    /**
     * The want authentication option.
     */
    private boolean wantClientAuth;

    /**
     * The need authentication option.
     */
    private boolean needClientAuth;

    /**
     * Construct a new channel.
     * 
     * @param channel the unsecure socket channel.
     * @param context the SSL context.
     */
    public SSLServerSocketChannel(ServerSocketChannel channel,
        SSLContext context)
    {
        super(channel.provider());
        if (context == null)
        {
            throw new NullPointerException("SSLContext context");
        }

        socketChannel = channel;
        sslContext = context;
    }

    public ServerSocket socket()
    {
        return socketChannel.socket();
    }

    public SocketChannel accept() throws IOException
    {
        SocketChannel channel = socketChannel.accept();
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        if (getWantClientAuth())
        {
            engine.setWantClientAuth(true);
        }
        if (getNeedClientAuth())
        {
            engine.setNeedClientAuth(true);
        }
        return new SSLSocketChannel(channel, engine);
    }

    public Channel getAdapteeChannel()
    {
        return socketChannel;
    }

    public String toString()
    {
        return "SSLServerSocketChannel[" + socket().toString() + "]";
    }

    /**
     * Checks whether client authentication is wanted.
     * 
     * @return true for client authentication, false otherwise.
     */
    public boolean getWantClientAuth()
    {
        return wantClientAuth;
    }

    /**
     * Sets whether client authentication is wanted.
     * 
     * @param flag true for client authentication, false otherwise.
     */
    public void setWantClientAuth(boolean flag)
    {
        wantClientAuth = flag;
    }

    /**
     * Checks whether client authentication is needed.
     * 
     * @return true for client authentication, false otherwise.
     */
    public boolean getNeedClientAuth()
    {
        return needClientAuth;
    }

    /**
     * Sets whether client authentication is needed.
     * 
     * @param flag true for client authentication, false otherwise.
     */
    public void setNeedClientAuth(boolean flag)
    {
        needClientAuth = flag;
    }

    protected void implCloseSelectableChannel() throws IOException
    {
        socketChannel.close();
    }

    protected void implConfigureBlocking(boolean block) throws IOException
    {
        socketChannel.configureBlocking(block);
    }
}
