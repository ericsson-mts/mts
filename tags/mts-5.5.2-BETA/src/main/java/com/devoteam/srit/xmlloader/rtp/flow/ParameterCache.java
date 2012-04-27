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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.ExpireHashMap;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.Removable;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class ParameterCache {
    static private Map<Parameter, ExpirableObject<Long>> _paramToVersion = new ExpireHashMap("versions", 30);
    static private Map<Parameter, ExpirableObject<List<Array>>> _paramToAsciiArrayList = new ExpireHashMap("ascii", 30);
    static private Map<Parameter, ExpirableObject<List<Array>>> _paramToHexArrayList = new ExpireHashMap("hexa", 30);
    static private Map<Parameter, ExpirableObject<List<Float>>> _paramToFloatList = new ExpireHashMap("float", 30);
    static private Map<Parameter, ExpirableObject<List<Long>>> _paramToLongList = new ExpireHashMap("long", 30);
    static private Map<Parameter, ExpirableObject<List<Integer>>> _paramToIntegerList = new ExpireHashMap("integer", 30);
    
    static synchronized public List<Array> getAsHexArrayList(Parameter parameter){
        Long version = null;
        ExpirableObject<Long> versionObject = _paramToVersion.get(parameter);
        if(versionObject != null){
            version = versionObject.getObject();
        }

        List<Array> list = null;
        ExpirableObject<List<Array>> listObject = _paramToHexArrayList.get(parameter);
        if(listObject != null){
            list = listObject.getObject();
        }
                
        if(version != null && list != null && version == parameter.getVersion()){
            // already present in cache; return it
            _paramToHexArrayList.put(parameter, listObject);
            return list;
        }
        else{
            // not in cache or wrong version (parameter changed); parse, add to cache, then return it
            list = new ArrayList<Array>(parameter.length());
            for(Object object:parameter.getArray()){
                list.add(Array.fromHexString(object.toString()));
            }
            _paramToVersion.put(parameter, new ExpirableObject<Long>(parameter.getVersion()));
            _paramToHexArrayList.put(parameter, new ExpirableObject<List<Array>>(list));
            return list;
        }
    }
    
    static synchronized public List<Array> getAsAsciiArrayList(Parameter parameter){
        Long version = null;
        ExpirableObject<Long> versionObject = _paramToVersion.get(parameter);
        if(versionObject != null){
            version = versionObject.getObject();
        }

        List<Array> list = null;
        ExpirableObject<List<Array>> listObject = _paramToAsciiArrayList.get(parameter);
        if(listObject != null){
            list = listObject.getObject();
        }
                
        if(version != null && list != null && version == parameter.getVersion()){
            // already present in cache; return it
            _paramToAsciiArrayList.put(parameter, listObject);
            return list;
        }
        else{
            // not in cache or wrong version (parameter changed); parse, add to cache, then return it
            list = new ArrayList<Array>(parameter.length());
            for(Object object:parameter.getArray()){
                list.add(new DefaultArray(object.toString().getBytes()));
            }
            _paramToVersion.put(parameter, new ExpirableObject<Long>(parameter.getVersion()));
            _paramToAsciiArrayList.put(parameter, new ExpirableObject<List<Array>>(list));
            return list;
        }
    }
    
    
    static synchronized public List<Long> getAsLongList(Parameter parameter){
        Long version = null;
        ExpirableObject<Long> versionObject = _paramToVersion.get(parameter);
        if(versionObject != null){
            version = versionObject.getObject();
        }

        List<Long> list = null;
        ExpirableObject<List<Long>> listObject = _paramToLongList.get(parameter);
        if(listObject != null){
            list = listObject.getObject();
        }

        if(version != null && list != null && version == parameter.getVersion()){
            // already present in cache; return it
            _paramToLongList.put(parameter, listObject);
            return list;
        }
        else{
            // not in cache or wrong version (parameter changed); parse, add to cache, then return it
            list = new ArrayList<Long>(parameter.length());
            for(Object object:parameter.getArray()){
                list.add(Long.parseLong(object.toString()));
            }
            _paramToVersion.put(parameter, new ExpirableObject<Long>(parameter.getVersion()));
            _paramToLongList.put(parameter, new ExpirableObject<List<Long>>(list));
            return list;
        }
    }

    static synchronized public List<Integer> getAsIntegerList(Parameter parameter){
        Long version = null;
        ExpirableObject<Long> versionObject = _paramToVersion.get(parameter);
        if(versionObject != null){
            version = versionObject.getObject();
        }

        List<Integer> list = null;
        ExpirableObject<List<Integer>> listObject = _paramToIntegerList.get(parameter);
        if(listObject != null){
            list = listObject.getObject();
        }

        if(version != null && list != null && version == parameter.getVersion()){
            // already present in cache; return it
            _paramToIntegerList.put(parameter, listObject);
            return list;
        }
        else{
            // not in cache or wrong version (parameter changed); parse, add to cache, then return it
            list = new ArrayList<Integer>(parameter.length());
            for(Object object:parameter.getArray()){
                list.add(Integer.parseInt(object.toString()));
            }
            _paramToVersion.put(parameter, new ExpirableObject<Long>(parameter.getVersion()));
            _paramToIntegerList.put(parameter, new ExpirableObject<List<Integer>>(list));
            return list;
        }
    }

    static synchronized public void clear(){
        _paramToVersion.clear();
        _paramToFloatList.clear();
        _paramToLongList.clear();
        _paramToAsciiArrayList.clear();
        _paramToHexArrayList.clear();
    }

    static public class ExpirableObject<T> implements Removable{
        private T _object;
        
        public ExpirableObject(T object){
            _object = object;
        }

        public T getObject(){
            return _object;
        }

        public void onRemove() throws Exception {

        }
    }
}
