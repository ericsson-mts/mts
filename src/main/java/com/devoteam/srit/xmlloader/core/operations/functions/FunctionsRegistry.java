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

package com.devoteam.srit.xmlloader.core.operations.functions;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import java.util.HashMap;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class FunctionsRegistry {
    static private FunctionsRegistry _instance = null;

    static public FunctionsRegistry instance(){
        if(null == _instance){
            _instance = new FunctionsRegistry();
        }
        return _instance;
    }

    // hashmaps to store the dom elements and the version (a long incremented for each new parse)
    private HashMap<String, Element> _registry;
    private HashMap<String, Long> _versions;
    private long _version;

    private FunctionsRegistry(){
        _registry = new HashMap();
        _versions = new HashMap();
        _version = 0;
    }

    /**
     * just add a new dom tree for a given function to the registry (increments version)
     */
    public synchronized void register(String name, Element root){
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "register function ", name);
        _registry.put(name, root);
        _versions.put(name, _version++);
    }

    /**
     * return a copy of a dom tree (because it will be used to create a Function, and the
     * function will modify the dom tree with the replacers) or null if unknown
     */
    public synchronized Element element(String name){
        Element element = _registry.get(name);
        if(null == element){
            return null;
        }
        else{
            return element.createCopy();
        }
    }

    /**
     * return the version of a dom tree (should be null in the same time as element(...))
     */
    public synchronized Long version(String name){
        return _versions.get(name);
    }

    /**
     * cleanup everything, called on reload
     */
    public synchronized void clear(){
        _registry.clear();
        _versions.clear();
    }
}
