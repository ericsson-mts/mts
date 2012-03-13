/*
 * Copyright (c) 2005 Srit Devoteam. All rights reserved.
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