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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.testmanager;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.master.MasterImplementation;
import com.devoteam.srit.xmlloader.master.masterutils.SlavesStatusSingleton;
import com.devoteam.srit.xmlloader.master.node.NodeIdentifier;
import com.devoteam.srit.xmlloader.master.node.NodeParameters;
import java.net.InetAddress;
import java.util.Iterator;

/**
 * This class provides a TestManager depending on a slave name.
 * If the slave name is null then the TestManager will be a local stub.
 * @author gpasquiers
 */
public class RemoteTesterProvider
{
    
    private NodeIdentifier slaveIdentifier;
    
    public RemoteTester get(String slave) throws Exception
    {
        // local TestManager
        if(null == slave)
        {
            return null;
        }
        // remote TestManager
        else
        {
            NodeIdentifier old = this.slaveIdentifier;
            this.computeSlaveIdentifier(slave);
            
            if(null != old && !old.equals(this.slaveIdentifier)) this.free(old);
            
            return MasterImplementation.instance().getMasterNode().getSlaveInterface(this.slaveIdentifier);
        }
    }
    
    public void free() throws Exception
    {
        this.free(this.slaveIdentifier);
    }
    
    private void free(NodeIdentifier slaveIdentifier) throws Exception
    {
        if(null != slaveIdentifier)
        {
            SlavesStatusSingleton.instance().freeSlave(slaveIdentifier);
        }
    }
    
    /**
     * If this method does not throw any exception then the test can be run.
     * This method mainly tries to get a slave to run the test.
     * It is done in three steps :
     *  - assume the string "slave" is of the format "host:port" and:
     *     - try to get a slave already connected to us, from this host/port.
     *     - try to connect to a new slave at this host/port
     * @throws java.lang.Exception
     */
    private void computeSlaveIdentifier(String slave) throws Exception
    {
        // check that the slaveIdentifier still matches a slave
        NodeParameters slaveParameters = MasterImplementation.instance().getMasterNode().getSlaveNodesParameters(this.slaveIdentifier);
        if(null == slaveParameters)
        {
            this.slaveIdentifier = null;
        }

        
        // do we have one slave having the same address ?
        if(null == this.slaveIdentifier)
        {
            GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: trying to find a slave for the url ", slave, ".");
            String host;
            int port;
            if(slave.indexOf(":") != -1)
            {
                try
                {
                    host = InetAddress.getByName(slave.substring(0, slave.indexOf(":"))).getHostAddress();
                }
                catch(Exception e)
                {
                    throw new Exception("The test cannot be run. Cannot find slave " + slave + " invalid host in address.", e);
                }
                
                String portStr = slave.substring(slave.indexOf(":") + 1);
                if(Utils.isInteger(portStr))
                {
                    port = Integer.parseInt(portStr);
                }
                else
                {
                    throw new Exception("The test cannot be run. Cannot find slave " + slave + " invalid address: not in format host:port.");
                }
            }
            else
            {
                throw new Exception("The test cannot be run. Cannot find slave " + slave + " invalid address: not in format host:port.");
            }

            Iterator<NodeIdentifier> iterator = SlavesStatusSingleton.instance().getFreeSlaves();
            while(iterator.hasNext())
            {
                NodeIdentifier nodeIdentifier = iterator.next();
                NodeParameters nodeParameters = MasterImplementation.instance().getMasterNode().getSlaveNodesParameters(nodeIdentifier);
                String url = nodeParameters.getHost() + ":" + nodeParameters.getPort();
                String slaveUrl = host + ":" + port;
                if((url).equals(slaveUrl))
                {
                    GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: found already connected from ", url);
                    if(SlavesStatusSingleton.instance().isFree(nodeIdentifier))
                    {
                        GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: slaved connected from ", url, " is free");
                        SlavesStatusSingleton.instance().reserveSlave(nodeIdentifier);
                        GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: reserved slave ", nodeParameters);
                        this.slaveIdentifier = nodeIdentifier;
                        break;
                    }
                    else
                    {
                        GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, "Master: slave named ", slave, " is not free");
                    }
                }
            }

            if(null == this.slaveIdentifier)
            {
                GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: try to connect to ", host, ":", port);

                NodeIdentifier nodeIdentifier = new NodeIdentifier();
                NodeParameters nodeParameters = new NodeParameters(host, port, "imsloader.slave", nodeIdentifier);

                try
                {
                    MasterImplementation.instance().getMasterNode().connectToSlave(nodeParameters);
                    SlavesStatusSingleton.instance().reserveSlave(nodeIdentifier);
                }
                catch(Exception e)
                {
                    throw new Exception("The test cannot be run. Cannot connect to slave " + slave + ":" + e.getMessage(), e);                
                }
                this.slaveIdentifier = nodeIdentifier;            
            }
            
        }
        
        
        // should never happen
        if(null == this.slaveIdentifier)
        {
            throw new Exception("The test cannot be run. It has no dedicated slave.");
        }
        
        GlobalLogger.instance().getApplicationLogger().info(Topic.CORE, "Master: test will use slave ", this.slaveIdentifier);
    }
}
