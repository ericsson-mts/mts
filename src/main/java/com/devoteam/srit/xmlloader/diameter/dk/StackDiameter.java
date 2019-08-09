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

package com.devoteam.srit.xmlloader.diameter.dk;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.diameter.StackDiamCommon;

/**
 *
 * @author fhenry
 */
public class StackDiameter extends StackDiamCommon 
{
    
    /** Creates a new instance */
    public StackDiameter() throws Exception 
    {
        super();
    }
    
    
    //---------------------------------------------------------------------
    // methods for the transport
    //---------------------------------------------------------------------

    /** Open a channel */
    @Override
    public boolean openChannel(Channel channel) throws Exception
   {    	
    	// channel are made by the dk stack
        synchronized (channels)
        {
        	channels.put(channel.getName(), channel);
	        if (channels.size() % 1000 == 999)
	        {
	            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Stack : List of channels : size = ", channels.size());
	        }
        }
        GlobalLogger.instance().getApplicationLogger().debug(Topic.PROTOCOL, "Stack: put in channels list : size = ", channels.size(), " the channel \n", channel);
    	return false;
    }
    
    /** Close a channel */
    @Override
    public boolean closeChannel(String name) throws Exception
    {
        synchronized (channels)
        {
        	channels.remove(name);
        }
        return false;
    }

    
}
