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

package com.devoteam.srit.xmlloader.snmp;

import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import org.snmp4j.AbstractTarget;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;

/**
 *
 * @author bbouvier
 */
public class ProcessMessage implements CommandResponder, ResponseListener {

    Listenpoint listenpoint = null;
    ProcessMessage(Listenpoint listen)
    {
        listenpoint = listen;
    }

    //process request and analyze content with mib file
    public void processPdu(CommandResponderEvent cre) {

        try {
            //create the SnmpMsg received
            MsgSnmp msg = new MsgSnmp();
            msg.setPdu(cre.getPDU());
            //TODO: how to know the version here to set communityTarget or UserTarget
            AbstractTarget target = null;
            if((cre.getMessageProcessingModel() == MessageProcessingModel.MPv1)
               || (cre.getMessageProcessingModel() == MessageProcessingModel.MPv2c))
            {
                target = new CommunityTarget();
                ((CommunityTarget)target).setCommunity(new OctetString(cre.getStateReference().getSecurityName()));
            }
            else//TODO: manage of snmpV3 not done
                target = new UserTarget();

            if(cre.getMessageProcessingModel() == MessageProcessingModel.MPv1)
            {
                target.setVersion(SnmpConstants.version1);
            }
            else if(cre.getMessageProcessingModel() == MessageProcessingModel.MPv2c)
            {
                target.setVersion(SnmpConstants.version2c);
            }
            else if(cre.getMessageProcessingModel() == MessageProcessingModel.MPv3)
            {
                target.setVersion(SnmpConstants.version3);
            }

            target.setAddress(cre.getPeerAddress());
            msg.setTarget((AbstractTarget)target);
            UdpAddress add = (UdpAddress) cre.getPeerAddress();
            msg.setRemotePort(add.getPort());
            msg.setRemoteHost(add.getInetAddress().getHostAddress());
            msg.setListenpoint(listenpoint);
        
            StackFactory.getStack(StackFactory.PROTOCOL_SNMP).receiveMessage(msg);
        } catch (Exception ex) {
        }
    }

    public void onResponse(ResponseEvent re) {
       // Always cancel async request when response has been received
       // otherwise a memory leak is created! Not canceling a request
       // immediately can be useful when sending a request to a broadcast
       // address.
       try {
            Object source = re.getSource();

           // test to ignore REPORTS from DISCOVERY messages in SNMPv3
           if (!(source instanceof Snmp)) return;

           ((Snmp) source).cancel(re.getRequest(), this);

           //create the SnmpMsg received
           MsgSnmp msg = new MsgSnmp();
           msg.setPdu(re.getResponse());
           
           //TODO: how to know the version here to set communityTarget or UserTarget
           Target target = new CommunityTarget();
//           ((CommunityTarget)target).setCommunity(new OctetString(re.getStateReference().getSecurityName()));
           target.setAddress(re.getPeerAddress());
           msg.setTarget((AbstractTarget)target);
           UdpAddress add = (UdpAddress) re.getPeerAddress();
           msg.setRemotePort(add.getPort());
           msg.setRemoteHost(add.getInetAddress().getHostAddress());
           msg.setListenpoint(listenpoint);

           StackFactory.getStack(StackFactory.PROTOCOL_SNMP).receiveMessage(msg);
       } catch (Exception ex) {
       }
    }

}
