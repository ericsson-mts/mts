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

package com.devoteam.srit.xmlloader.udp.nio;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 *
 * @author gpasquiers
 */
public class DatagramReactor
{
    final private Selector selector;

    final private Object selectorLock;

    static private DatagramReactor instance;

    static public DatagramReactor instance()
    {
        if(null == instance) instance = new DatagramReactor();

        return instance;
    }


    public DatagramReactor()
    {
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


        final LinkedBlockingQueue<SelectionKey> writingQueue = new LinkedBlockingQueue();
        final Semaphore writingSemaphore = new Semaphore(0);
//        for(int i=0; i<2 ; i++)
//        {
            Thread writer = new Thread(){
                @Override
                public void run(){
                    while(true)
                    {
                        try
                        {
                            SelectionKey key = writingQueue.take();
                            if (key.isValid() && key.isWritable())
                            {
                                DatagramHandler handler = (DatagramHandler) key.attachment();
                                handler.outputReady();
                            }
                            writingSemaphore.release();
                        }
                        catch(Exception e)
                        {
                            System.err.println("writer thread error !");
                            e.printStackTrace();
                        }
                    }
                }
            };
            writer.setDaemon(true);
            writer.start();
//        }
        
        final LinkedBlockingQueue<SelectionKey> readingQueue = new LinkedBlockingQueue();
        final Semaphore readingSemaphore = new Semaphore(0);
//        for(int i=0; i<2; i++)
//        {
        Thread reader = new Thread(){
            @Override
            public void run(){
                while(true)
                {
                    try
                    {
                        SelectionKey key = readingQueue.take();
                        if (key.isValid() && key.isReadable())
                        {
                            DatagramHandler handler = (DatagramHandler) key.attachment();
                            handler.inputReady();
                        }
                        readingSemaphore.release();
                    }
                    catch(Exception e)
                    {
                        System.err.println("writer thread error !");
                        e.printStackTrace();
                    }
                }
            }
        };
        reader.setDaemon(true);
        reader.start();
//        }

        Thread dispatcher = new Thread(){
            @Override
            public void run(){
                try
                {
                    Set selectedKeys;
                    while(true)
                    {
                        synchronized(selectorLock){}

                        selector.select();
                        selectedKeys = selector.selectedKeys();

                        int writes = 0;
                        int reads = 0;
                        for (Iterator iterator = selectedKeys.iterator(); iterator.hasNext();)
                        {
                            SelectionKey key = (SelectionKey) iterator.next();
                            iterator.remove();

                            if (!key.isValid())
                            {
                                key.cancel();
                            }
                            else
                            {
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

    public void open(SocketAddress localSocketAddress, DatagramHandler handler) throws Exception
    {
        // Create a non-blocking socket channel
        DatagramChannel channel = DatagramChannel.open();
		// read all properties for the UDP socket 
		Config.getConfigForUDPSocket(channel.socket());

        channel.socket().bind(localSocketAddress);
        channel.configureBlocking(false);

        synchronized(selectorLock)
        {
            this.selector.wakeup();
            handler.init(channel.register(selector, SelectionKey.OP_READ, handler));
        }
    }

}
