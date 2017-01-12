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

import com.devoteam.srit.xmlloader.core.ScenarioReference;
import java.util.HashMap;
import java.util.LinkedList;
import org.dom4j.Element;

/**
 * This class acts as a cache for the Function objects.
 * It provides (creates if necessary) a Function instance. It also checks that the
 * instance is up to date with the version in "FunctionRegistry".
 * @author gpasquiers
 */
public class FunctionsCache {
    // singleton part
    static private FunctionsCache _instance = null;
    static synchronized public FunctionsCache instance(){
        if(null == _instance){
            _instance = new FunctionsCache();
        }
        return _instance;
    }

    // instance part

    private HashMap<String, LinkedList<Function>> _cache;

    public FunctionsCache(){
        _cache = new HashMap();
    }

    /**
     * gets the Function instance; from cache if possible, else, create it.
     */
    public Function getFunction(String name, ScenarioReference scenario) throws Exception{
        Function function = null;

        // get the list of function instances matching the name and try to get one
        // that has the same version as the dom tree in the functions registry
        synchronized(this){
            Long currentVersion = FunctionsRegistry.instance().version(name);
            LinkedList<Function> list = _cache.get(name);
            while(null != list && !list.isEmpty() && null == function){
                if(list.getFirst().getVersion() == currentVersion){
                    function = list.getFirst();
                }
                else{
                    list.removeFirst();
                }
            }
        }

        // if we found nothing, then create a new Function object from the dom tree
        // in the function registry
        if(null == function){
            Element root;
            Long version;
            synchronized(this){
                root = FunctionsRegistry.instance().element(name);
                version = FunctionsRegistry.instance().version(name);
            }

            if(null != root && null != version){
                function = new Function(root, scenario.getScenario(), version);
            }
        }
        return function;
    }

    /**
     * called to put back a function in the cache after it was used.
     * the function is only put back if it's version is up-to-date
     */
    public synchronized void freeFunction(Function function) throws Exception{
        Long currentVersion = FunctionsRegistry.instance().version(function.getName());
        if(function.getVersion() == currentVersion){
            LinkedList list = _cache.get(function.getName());

            if(list == null){
                list = new LinkedList<Function>();
                _cache.put(function.getName(), list);
            }
            list.addLast(function);
        }
    }
}
