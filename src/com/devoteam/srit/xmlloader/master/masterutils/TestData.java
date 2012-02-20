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

import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import java.net.URI;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class TestData //implements NotificationListener<Notification<String, RunnerState>>, NotificationSender<Notification<String, RunnerState>>
{
    private URI path;
    
    private Element root;
    
    //private boolean isActive;
    
    //private boolean hasMaster;

    private long zeroTimestamp;
    
    
    public TestData(Element root, URI path) throws Exception
    {
        this.root = root;
        this.path   = URIFactory.resolve(path, root.attributeValue("path"));
        if(null != root.attributeValue("home") && !root.attributeValue("home").endsWith("/"))
        {
            root.attribute("home").setValue(root.attributeValue("home") + "/");
        }
    }
    
    public String attributeValue(String name)
    {
        return root.attributeValue(name);
    }
    
    public Element getRoot()
    {
        return this.root;
    }
    
    public void setZeroTimestamp(long timestamp)
    {
        this.zeroTimestamp = timestamp;
    }
    
    public long getZeroTimestamp()
    {
        return this.zeroTimestamp;
    }

    public URI getPath()
    {
        return this.path;
    }
    
    public void setRunner(String runner)
    {
        this.root.attribute("runner").setValue(runner);
    }
    
    public void setSlave(String slave)
    {
        this.root.attribute("slave").setValue(slave);
    }
}
