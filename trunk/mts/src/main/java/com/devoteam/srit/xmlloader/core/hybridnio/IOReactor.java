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
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * This class is a singleton.
 * It handles the NIO Selector. It has the necessary methods to open:
 *  - UDP Sockets
 *  - TCP [SSL]Sockets
 *  - TCP [SSL]ServerSockets
 *
 * As NIO are somewhat event-based IO, this class dispatches those event (init,
 * read, write, connect, accept) to IOHandlers.
 *
 * This class has 3+ threads:
 *  - 1 selector handler
 *  - N writes handlers (2 hardcoded)
 *  - N reads handlers (2 hardcoded)
 *
 * @author gpasquiers
 */
public class IOReactor
{
    /**
     * Selector that allows the selector handler to know which channel has IO
     * operations to do.
     */
    final private Selector selector;

    /**
     * Object used as a lock to register new channels into the selector.
     */
    final private Object selectorLock;

    static private IOReactor instance;

    static public IOReactor instance()
    {
        if(null == instance) instance = new IOReactor();

        return instance;
    }


    public IOReactor()
    {
        // First simply create the selector.
        // TODO: better handling of exception, but it should not fail.
        Selector _selector = null;
        try
        {
            _selector = Selector.open();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        this.selector = _selector;

        this.selectorLock = new Object();

        // writingQueue is a thread-safe queue used by the selector handler to
        // dispatch the write tasks to the N write handlers
        final LinkedBlockingQueue<SelectionKey> writingQueue = new LinkedBlockingQueue();

        // this semaphore is used by the selector thread, the selector thread
        // waits for all the writes operation to be done before getting the list
        // of available events again.
        final Semaphore writingSemaphore = new Semaphore(0);
        for(int i=0; i<2 ; i++)
        {
            // this anonymous thread is the write handler
            Thread writer = new Thread(){
                @Override
                public void run(){
                    while(true)
                    {
                        try
                        {
                            // when we manage to get a task, we fire the event
                            SelectionKey key = writingQueue.take();
                            if (key.isValid() && key.isWritable())
                            {
                                IOHandler handler = (IOHandler) key.attachment();
                                handler.outputReady();
                            }
                        }
                        catch(Exception e)
                        {
                            // ignore
                            //System.err.println("writer thread error !");
                            //e.printStackTrace();
                        }
                        finally
                        {
                            writingSemaphore.release();
                        }
                    }
                }
            };
            writer.setDaemon(true);
            writer.start();
        }

        // readingQueue is a thread-safe queue used by the selector handler to
        // dispatch the read tasks to the N read handlers
        final LinkedBlockingQueue<SelectionKey> readingQueue = new LinkedBlockingQueue();

        // this semaphore is used by the selector thread, the selector thread
        // waits for all the read operation to be done before getting the list
        // of available events again.
        final Semaphore readingSemaphore = new Semaphore(0);
        for(int i=0; i<2; i++)
        {
            // this anonymous thread is the read handler
            Thread reader = new Thread(){
                @Override
                public void run(){
                    while(true)
                    {
                        try
                        {
                            SelectionKey key = readingQueue.take();
                            try
                            {
                                // when we manage to get a task, we fire the event
                                //if (key.channel().isOpen() && key.isValid() && key.isReadable())
                                {
                                    IOHandler handler = (IOHandler) key.attachment();
                                    handler.inputReady();
                                }
                            }
                            catch(Exception e)
                            {
                                // ignore
                                //System.err.println("reader thread error !");
                                //e.printStackTrace();
                                key.cancel();
                                key.channel().close();
                            }
                        }
                        catch(Exception e)
                        {
                            // ignore
                        }
                        finally
                        {
                            readingSemaphore.release();
                        }
                    }
                }
            };
            reader.setDaemon(true);
            reader.start();
        }

        // this thread dispatches the event to the two different queues for them
        // to be handled by the reader and writer threads.
        //
        // There is this dispatching thing in order to have multiple threads
        // working, to be able to use at least the available cores of the
        // processor.
        Thread dispatcher = new Thread(){
            @Override
            public void run(){
                try
                {
                    Set selectedKeys;
                    while(true)
                    {
                        // this synchronized bock here is intended to be blocking in some cases :
                        // When another thread calls selector.wakeUp() and then channel.register()
                        // it does so in a synchronized(selectorLock) block. That way, the selector
                        // thread wont enter in selector.select() before the register operation
                        // is done.
                        //
                        // It is necessary because as long as a thread is in selector.select()
                        // the register methods become blocking.
                        synchronized(selectorLock){}

                        // Wait for IO event to be availables
                        selector.select();

                        // Get thoses IO event (the selectionKeys)
                        selectedKeys = selector.selectedKeys();

                        // Then for each SelectionKey we dispatch the available
                        // event by testing the key. Then we wait for thoses event to
                        // be completed using the read and write semaphores.
                        int writes = 0;
                        int reads = 0;
                        for (Iterator iterator = selectedKeys.iterator(); iterator.hasNext();)
                        {
                            try
                            {
                                SelectionKey key = (SelectionKey) iterator.next();
                                iterator.remove();

                                if (!key.isValid() || !key.channel().isOpen())
                                {
                                    key.channel().close();
                                    key.cancel();
                                }
                                else
                                {
                                    if (key.isValid() && key.isAcceptable())
                                    {
                                        IOHandler handler = (IOHandler) key.attachment();
                                        handler.acceptReady();
                                    }
                                    if (key.isValid() && key.isConnectable())
                                    {
                                        IOHandler handler = (IOHandler) key.attachment();
                                        handler.connectReady();
                                    }
                                    if (key.isValid() && key.isReadable())
                                    {
                                        readingQueue.put(key);
                                        reads++;
                                    }
                                    if (key.isValid() && key.isWritable())
                                    {
                                        writingQueue.put(key);
                                        writes++;
                                    }
                                }
                            }
                            catch(Exception e)
                            {
                                // ignore exception
                                //System.err.println("error in dispatcher thread");
                                //e.printStackTrace();
                            }

                        }
                        writingSemaphore.acquire(writes);
                        readingSemaphore.acquire(reads);
                    }
                }
                catch(Exception e)
                {
                    System.err.println("dispatcher thread died !");
                    e.printStackTrace();
                }
            }
        };
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    public void openUDP(SocketAddress localSocketAddress, IOHandler handler) throws IOException
    {
        // Create a non-blocking socket channel
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            handler.init(channel.register(selector, SelectionKey.OP_READ, handler), channel);
        }
    }

    public void openTCP(SocketAddress localSocketAddress, SocketAddress remoteSocketAddress, IOHandler handler) throws IOException
    {
        // Create a non-blocking socket channel
        SocketChannel channel = SocketChannel.open();
        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(true);
        channel.connect(remoteSocketAddress);
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            handler.init(channel.register(selector, SelectionKey.OP_READ, handler), channel);
        }
    }

    public void openTCP(SocketChannel channel, IOHandler handler) throws IOException
    {
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            handler.init(channel.register(selector, SelectionKey.OP_READ, handler), channel);
        }
    }

    public void openTCPServer(SocketAddress localSocketAddress, IOHandler handler) throws IOException
    {
        // Create a non-blocking socket channel
        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            handler.init(channel.register(selector, SelectionKey.OP_ACCEPT, handler), channel);
        }
    }

    /**
     * Open a SSL Socket. This method is based on class SSLSocketChannel we got
     * from some LGPL library.
     * @param localSocketAddress
     * @param remoteSocketAddress
     * @param handler
     * @param context
     * @throws Exception
     */
    public void openTLS(SocketAddress localSocketAddress, SocketAddress remoteSocketAddress, IOHandler handler, SSLContext context) throws Exception
    {
        // create an engine based on an already initialized context. This context
        // contains the certificates.
        SSLEngine engine = context.createSSLEngine();
        engine.setUseClientMode(true);
        // create, connect (TCP only, no exchange yet).
        SSLSocketChannel channel = new SSLSocketChannel(SocketChannel.open(), engine);
        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(true);
        channel.connect(remoteSocketAddress);
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            // wakeup the selector (it will leave the .select() method then block
            // on the synchronized(selectorLock){]} instruction. If we don't do this,
            // the .register method is blocking until select() leaves, which can
            // take some time if there is no network traffic.
            this.selector.wakeup();
            SocketChannel adapteeChannel = ((SocketChannel)channel.getAdapteeChannel());
            
            // call the init() method of the handler to give him the channel and
            // selectionKey he will use for later calls to outputReadey and inputReady.
            //
            // NB for SSL: we do not give the handler the same channel we register into
            //             the selector because we can only register sun's channels
            //             into the selector.
            handler.init(adapteeChannel.register(selector, SelectionKey.OP_READ, handler), channel);
        }
    }

    /**
     * This method registers an already existing channel (created from a ServerSocket).
     * @param channel
     * @param handler
     * @throws IOException
     */
    public void openTLS(SSLSocketChannel channel, IOHandler handler) throws IOException
    {
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            SocketChannel adapteeChannel = ((SocketChannel)channel.getAdapteeChannel());
            handler.init(adapteeChannel.register(selector, SelectionKey.OP_READ, handler), channel);
        }
    }

    /**
     * Register a ServerSocket. A ServerSocket will only trigger .accept() events.
     * It cannot do any input-output.
     * @param localSocketAddress
     * @param handler
     * @param context
     * @throws Exception
     */
    public void openTLSServer(SocketAddress localSocketAddress, IOHandler handler, SSLContext context) throws Exception
    {
        // Create a non-blocking socket channel
        SSLServerSocketChannel channel = new SSLServerSocketChannel(ServerSocketChannel.open(), context);
        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(false);

        // should be used to enable SSL two-ways.
        channel.setWantClientAuth(false);
        channel.setNeedClientAuth(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            ServerSocketChannel adapteeChannel = ((ServerSocketChannel)channel.getAdapteeChannel());
            adapteeChannel.register(selector, SelectionKey.OP_ACCEPT, handler);
            handler.init(adapteeChannel.register(selector, SelectionKey.OP_ACCEPT, handler), channel);
        }
    }
}
