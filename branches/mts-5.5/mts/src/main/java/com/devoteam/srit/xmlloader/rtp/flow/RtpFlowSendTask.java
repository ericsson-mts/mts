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

package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.rtp.MsgRtp;

import gp.utils.scheduler.Scheduler;
import gp.utils.scheduler.Task;

public class RtpFlowSendTask implements Task {

    private MsgRtpFlow msgRtpFlow;
    private Listenpoint listenpoint;
    private Scheduler scheduler;
    private int cptPacket = 0;
    private long lastScheduledTimestamp = 0;

    public RtpFlowSendTask(MsgRtpFlow aRtpFlow, Scheduler aScheduler)
    {
        msgRtpFlow = aRtpFlow;
        listenpoint = msgRtpFlow.getListenpoint();
        scheduler = aScheduler;
    }

    //execute the task
    @Override
    public void execute()
    {
    	try
        {
            //register timestamp of the send
            long currentTimestamp = System.currentTimeMillis();
            if(lastScheduledTimestamp == 0)
                lastScheduledTimestamp = currentTimestamp;

            //sendMessageRTP
            if(msgRtpFlow.isPacketToBeSend())
            {
                msgRtpFlow.getPacket().setTimestamp(currentTimestamp);//set timestamp of message to be used for jitter calculation at the send

                if (listenpoint != null)
                {
                	MsgRtp msgRtp = ((MsgRtpFlow)msgRtpFlow).getPacket();
                    listenpoint.sendMessage(msgRtp, msgRtpFlow.getRemoteHost(), msgRtpFlow.getRemotePort(), msgRtpFlow.getTransport());
                    if (msgRtpFlow.getChannel() == null)
                    {
                    	msgRtpFlow.setChannel(msgRtp.getChannel());
                    }
                    msgRtpFlow.firstMsgSent.release(); 
                }
                else
                {
                    throw new ExecutionException("No listenpoint to transport the message : \r\n" + msgRtpFlow.toString());
                }

                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, msgRtpFlow.getTransport(), StackFactory.PROTOCOL_RTP, msgRtpFlow.getPacket().getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_transportNumber"), 1);
                StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TRANSPORT, msgRtpFlow.getTransport(), StackFactory.PROTOCOL_RTP, msgRtpFlow.getPacket().getTypeComplete() + StackFactory.PREFIX_OUTGOING, "_transportBytes"), (float) msgRtpFlow.getPacket().getLength() / 1024 / 1024);

                if (((StackRtpFlow)StackFactory.getStack(StackFactory.PROTOCOL_RTPFLOW)).qosMeasurment)
                    msgRtpFlow.getQoSinfo().checkPacket(msgRtpFlow.getPacket());//calculate Qos Information at the send
            }
            cptPacket++;

            if(cptPacket < msgRtpFlow.getPacketNumber())
            {
                //prepare the next send
                msgRtpFlow.prepareNextMessage();
                
                long nextSend = msgRtpFlow.getDeltaTime() - (currentTimestamp - lastScheduledTimestamp);

                //calcul of next send
                lastScheduledTimestamp = currentTimestamp + nextSend;

                //insert jitter if there is
                nextSend += msgRtpFlow.calculJitter();

                if(nextSend > 0)//in case next send is programmed in 1 milliseconds minimum
                {
                    //wait in the timer up to the next wake up at the date to send message
                    scheduler.scheduleIn(this, nextSend);
                }
                else//in case we are late, send it as urgent task into the scheduler
                {
                    // this call to execute is done on the scheduler, so it is not recursive call
                    scheduler.execute(this, true);//true mean urgent task
                }
            }
            else
            {
                //release mutex for the synchronous wait
                msgRtpFlow.getSynchronousSemaphore().release();
            }

        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Error while trying to send flow RTP:");
        }
    }
}
