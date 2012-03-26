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

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Buffer containing the messages to be dispatch by a scenario
 *
 */
public class BufferMsg
{

    private LinkedBlockingQueue<Msg> newMessagesQueue;
    private LinkedList<Msg> oldMessagesQueue;
    private Iterator<Msg> oldMessagesIterator;

    /** Constructor */
    public BufferMsg()
    {
        this.newMessagesQueue = new LinkedBlockingQueue();
        this.oldMessagesQueue = new LinkedList();
        this.oldMessagesIterator = null;
    }

    /** Reads and returns a message from the scenario's Msg stack, throws exception if timeout occurs */
    public Msg readMessageFromStack(long timeout) throws ExecutionException
    {
        if (timeout <= 0)
        {
            throw new ExecutionException("readMessageFromStack: timeout, no message was received");
        }

        try
        {
            if (null != this.oldMessagesIterator)
            {
                Msg msg = null;
                if(this.oldMessagesIterator.hasNext())
                {
                    msg = this.oldMessagesIterator.next();
                }
                if(!this.oldMessagesIterator.hasNext())
                {
                    this.oldMessagesIterator = null;
                }
                return msg;
            }

            Msg msg = this.newMessagesQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if(null == msg)
            {
                return null;
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "BufferMsg : readMessageFromStack the message ", msg.getMessageId(), " in queue : size = ", this.newMessagesQueue.size(), "/", this.oldMessagesQueue.size());
            
            this.oldMessagesQueue.addLast(msg);

            return msg;
        }
        catch (Exception e)
        {
            throw new ExecutionException(e);
        }
    }

    public void resetMsgStackFlag()
    {
        if(0 != this.oldMessagesQueue.size()) this.oldMessagesIterator = this.oldMessagesQueue.iterator();
        else this.oldMessagesIterator = null;
    }

    public void dispatchMessage(Msg msg)
    {
        this.newMessagesQueue.add(msg);

        if ((this.newMessagesQueue.size() + this.oldMessagesQueue.size()) % 100 == 99)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "BufferMsg: queue of message : size = ", newMessagesQueue.size());
        }

        try
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "BufferMsg : addFirst msg ", msg.getMessageId(), "in queue : size = ", newMessagesQueue.size());
        }
        catch (Exception e)
        {
            // nothing to do
        }

    }

    /** Removes a message from the stack (if has been consumed by the ReceiveMsg Ope */
    public void removeMsgFromStack(Msg msg)
    {
        this.oldMessagesQueue.remove(msg);
        
        if ((this.newMessagesQueue.size() + this.oldMessagesQueue.size()) % 100 == 99)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "BufferMsg: queue of message : size = ", newMessagesQueue.size());
        }

        try
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "BufferMsg : remove msg ", msg.getMessageId(), "in queue : size = ", newMessagesQueue.size());
        }
        catch (Exception e)
        {
            // nothing to do
        }
    }

    public void clear()
    {
        this.newMessagesQueue.clear();
        this.oldMessagesQueue.clear();
        if(0 != this.oldMessagesQueue.size()) this.oldMessagesIterator = this.oldMessagesQueue.iterator();
        else this.oldMessagesIterator = null;
    }
}