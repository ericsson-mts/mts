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

package com.devoteam.srit.xmlloader.master.node;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.io.Serializable;

/**
 *
 * @author gpasquiers
 */
public class NodeIdentifier implements Serializable
{
    private String UID;
    
    
    public NodeIdentifier()
    {
        this(null);
    }

    public NodeIdentifier(String UID)
    {
        if(null == UID )
        {
            this.UID = Utils.newUID();
        }
        else
        {
            this.UID = UID;
        }
    }
    
    
    public String getUID()
    {
        return UID;
    }

    @Override
    public String toString()
    {
        return this.UID;
    }

    @Override
    public int hashCode()
    {
        //TODO: this could use some optimization
        return this.UID.hashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        if(object == null)
        {
            return false;
        }
        
        if(getClass() != object.getClass())
        {
            return false;
        }

        return this.hashCode() == object.hashCode();
    }
}
