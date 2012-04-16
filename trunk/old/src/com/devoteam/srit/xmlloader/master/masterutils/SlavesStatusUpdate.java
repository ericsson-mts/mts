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

package com.devoteam.srit.xmlloader.master.masterutils;

import com.devoteam.srit.xmlloader.master.node.NodeIdentifier;
import com.devoteam.srit.xmlloader.master.node.NodeParameters;

/**
 *
 * @author gpasquiers
 */
public class SlavesStatusUpdate
{
    static public final int SLAVE_CONNECTED = 0;
    static public final int SLAVE_DISCONNECTED = 1;
    static public final int SLAVE_UPDATED = 2;
    static public final int SLAVE_RELEASED = 3;
    static public final int SLAVE_RESERVED = 4;
    
    private int event;
    
    private NodeIdentifier nodeIdentifier;
    
    public SlavesStatusUpdate(int event, NodeIdentifier nodeIdentifier)
    {
        this.event = event;
        this.nodeIdentifier = nodeIdentifier;
    }

    public int getEvent()
    {
        return event;
    }

    public NodeIdentifier getNodeIdentifier()
    {
        return nodeIdentifier;
    }
    
}
