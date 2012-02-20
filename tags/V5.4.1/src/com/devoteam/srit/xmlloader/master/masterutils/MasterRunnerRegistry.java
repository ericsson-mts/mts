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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author gpasquiers
 */
public class MasterRunnerRegistry
{
    static private HashMap<String, MasterRunner> registry = new HashMap();
    static private HashMap<MasterRunner, String> invRegistry = new HashMap();
    
    static public void register(String id, MasterRunner runner)
    {
        registry.put(id, runner);
        invRegistry.put(runner, id);
    }
    
    static public MasterRunner get(String id)
    {
        return registry.get(id);
    }
    
    static public MasterRunner remove(String id)
    {
        return registry.remove(id);
    }
    
    static public String remove(MasterRunner runner)
    {
        return invRegistry.remove(runner);
    }

    static public Set<String> getNames()
    {
        return Collections.unmodifiableSet(registry.keySet());
    }

    static public Collection<MasterRunner> getRunners()
    {
        return Collections.unmodifiableCollection(registry.values());
    }
}
