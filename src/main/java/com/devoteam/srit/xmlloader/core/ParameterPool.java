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

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;

/**
 *
 * @author gpasquiers
 */
public class ParameterPool
{
    public enum Level
    {
        test,
        testcase,
        scenario,
        function,
        standalone
    }
    
    private ParameterPool parentParameterPool; 
    
    protected final Level level;
    
    private Object runner;
    
    private final HashMap<String, Parameter> parametersByName ;

    /** Creates a new ParameterPool */
    public ParameterPool(Object runner, Level level, ParameterPool parentParameterPool)
    {
        if(runner instanceof ScenarioRunner)
        {
            this.runner = runner;
        }
        else
        {
            this.runner = null;
        }

        this.level = level;
        this.parentParameterPool = parentParameterPool;
        this.parametersByName = new HashMap<String, Parameter>() ;
    }

    public ParameterPool getParent(){
        return parentParameterPool;
    }

    // <editor-fold desc=" public Parameters methods get, set, create, exists, delete ">
    public Parameter get(String name) throws ParameterException
    {
        testbrackets(name);
        
        Level level = getLevel(name);
        Parameter parameter;
        if(null != level)
        {
            parameter = getPool(level).getLocal(name);
        }
        else
        {
            parameter = getLocal(name);
            if(null == parameter && null != this.parentParameterPool) return this.parentParameterPool.get(name);
        }

        if(null == parameter) throw new ParameterException("Parameter named " + name + " don't exists in pool of level " + level);


        return parameter ;
    }

    public Parameter create(String name) throws ParameterException
    {
        testbrackets(name);
        
        Level level = getLevel(name);
        ParameterPool parameterPool;
        if(null != level)
        {
            parameterPool = getPool(level);
        }
        else
        {
            parameterPool = this;
        }

        Parameter parameter = new Parameter(name);       
        parameterPool.set(name, parameter);
        
        traceInfo("CREATE", name, parameter.toString());      
        return parameter ;
    }
    
    public Parameter createSimple(String name, Object value) throws ParameterException
    {
        Parameter param = new Parameter(name);
        param.add(value);
        setLocal(bracket(name), param);
        return param;
    }

    public void set(String name, Parameter parameter) throws ParameterException
    {
        testbrackets(name);
        
        Level level = getLevel(name);
        ParameterPool parameterPool;
        if(null != level)
        {
            parameterPool = getPool(level);
        }
        else
        {
            parameterPool = this;
        }

        parameterPool.setLocal(removeLevel(name), parameter);
    }
    
    public void delete(String name) throws ParameterException
    {
        testbrackets(name);
        
        Level level = getLevel(name);
        ParameterPool parameterPool;
        if(null != level)
        {
            parameterPool = getPool(level);
        }
        else
        {
            parameterPool = this;
        }
        
        parameterPool.deleteLocal(removeLevel(name));

    }
    
    public boolean exists(String name) throws ParameterException
    {
        testbrackets(name);
        
        Level level = getLevel(name);

        ParameterPool parameterPool;
        if(null != level)
        {
            parameterPool = getPool(level);
        }
        else
        {
            if(existsLocal(removeLevel(name)))
            {
               return true; 
            }
            else if(null != parentParameterPool)
            {
                return parentParameterPool.exists(name);
            }
            else
            {
                return false;
            }
        }
        
        return parameterPool.existsLocal(removeLevel(name));

    }
    // </editor-fold>
    
    // <editor-fold desc=" Parameters methods to access local pool ">
    private Parameter getLocal(String name) throws ParameterException
    {
        Parameter parameter;               
        synchronized(parametersByName)
        {
            parameter = parametersByName.get(removeLevel(name));          
        }
        traceDebug("GET", name, parameter);                 
        return parameter;
    }
    
    private void setLocal(String name, Parameter parameter)
    {
        synchronized(parametersByName)
        {
            parametersByName.put(removeLevel(name), parameter);
        }
        if (parameter.length() > 0)
        {
        	traceInfo("SET", name, parameter.toString());
        }
    }
    
    public boolean existsLocal(String name) throws ParameterException
    {
        testbrackets(name);

        synchronized(parametersByName){
            return parametersByName.containsKey(name);
        }
    }

    private void deleteLocal(String name)
    {
        Parameter parameter;       
        synchronized(parametersByName)
        {
            parameter = parametersByName.get(removeLevel(name));            
            parametersByName.remove(name);
        }
        if (parameter != null)
        {
        	traceInfo("REMOVE", name, parameter.toString());
        }
    }
    
    public Set<String> getParametersNameLocal()
    {
        synchronized(parametersByName){
            return this.parametersByName.keySet();
        }
    }
    // </editor-fold>
           
    public ParameterPool getPool(Level aLevel) throws ParameterException
    {
        if(this.level == aLevel)
        {
            return this;
        }
        
        if(null != parentParameterPool)
        {
            return parentParameterPool.getPool(aLevel);
        }
        
        throw new ParameterException("Can't get pool of level " + aLevel);
    }   
    
    
    public boolean isConstant(String string) throws ParameterException
    {
        Matcher matcher = Parameter.pattern.matcher(string);
        
        while(matcher.find())
        {
            if(!isConstantParameter(matcher.group()))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /** Returns true if the Parameter is a constant, ex: myVar(0) */
    public boolean isConstantParameter(String name) throws ParameterException
    {
        return !exists(name);
    }
    
    /** Returns the value of a constant, ex: myVar(5) => toto */
    public String getValueAsConstant(String name) throws ParameterException
    {
        if(null == name || name.charAt(0) != '[' || name.charAt(name.length()) != ']')
        {
            return name;
        }
        
        int idx_1 = name.indexOf('(') ;
        int idx_2 = name.indexOf(')', idx_1) ;
        
        if(idx_1 != -1 && idx_2 != -1)
        {
            if(idx_2 == (name.length()-2))
            {
                if(Utils.isInteger(name.substring(idx_1+1,idx_2)))
                {
                    Parameter parameter = get(getName(name));
                    if(null != parameter)
                    {
                        return parameter.get(Integer.parseInt(name.substring(idx_1+1,idx_2))).toString();
                    }
                }
            }
        }
        return name ;
    }
    
    
    public LinkedList<String> parse(String source) throws ParameterException
    {
        LinkedList<String> result = new LinkedList<String>();
        
        for(String firstPassSource:doParse(source))
        {
            result.addAll(doParse(firstPassSource));
        }
        
        return result;
    }
    
    private LinkedList<String> doParse(String source) throws ParameterException
    {
        Matcher matcher = Parameter.pattern.matcher(source);
        LinkedList<ParameterReference> parameters = new LinkedList<ParameterReference>();
        
        while(matcher.find())
        {
            String completeName = matcher.group();
            String name = getName(completeName);

            if(exists(name))
            {
                ParameterReference reference = new ParameterReference();
                reference.completeName = completeName;
                reference.parameter = get(name);
                reference.hasIndex = name.length() != completeName.length();
                if(reference.hasIndex) reference.index = getIndex(reference.completeName);
                parameters.add(reference);
            }
        }
        
        int size = -1;
        
        for(ParameterReference ref:parameters)
        {
            
            if(!ref.hasIndex)
            {
                if(size == -1)
                {
                    size = ref.parameter.length();
                }

                // handle error case (invalid lengths)
                if(size != -1 && size != ref.parameter.length())
                {
                    String sizes = "";
                    for(ParameterReference ref2:parameters)
                    {
                        sizes += ref2.completeName + ":" + ref2.parameter.length() + " ";
                    }                    
                    
                    throw new ParameterException("Invalid length of parameters, " + sizes);
                }
            }
        }
        
        if(size == -1)
        {
            size = 1;
        }
        
        LinkedList<String> result = new LinkedList<String>();
        
        for(int i=0; i<size; i++)
        {
            String line = source;
            for(ParameterReference ref:parameters)
            {
                if(ref.hasIndex)
                {
                    line = Utils.replaceNoRegex(line, ref.completeName, ref.parameter.get(ref.index).toString());
                }
                else
                {
                    line = Utils.replaceNoRegex(line, ref.completeName, ref.parameter.get(i).toString());
                }
            }
            result.add(line);
        }
        
        return result;
    }
    
    public void clear()
    {
        synchronized(parametersByName){
            parametersByName.clear();
        }
    }
    
    private void traceDebug(String action, String name, Parameter param)
    {
    	String nameWithoutBr = name.trim().substring(1, name.trim().length() -1);
        if(null != this.runner)
        {
            GlobalLogger.instance().getSessionLogger().debug((ScenarioRunner)runner, TextEvent.Topic.PARAM, action, " [", this.level, ":", nameWithoutBr, "] <= ", param);    
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PARAM, action, " [", this.level, ":", nameWithoutBr, "] <= ", param);
        }
    }

    public void traceInfo(String action, String name, String message)
    {
    	String nameWithoutBr = name.trim().substring(1, name.trim().length() -1);
        if(null != this.runner)
        {
            GlobalLogger.instance().getSessionLogger().info((ScenarioRunner)runner, TextEvent.Topic.PARAM, action, " [", this.level, ":", nameWithoutBr, "] => ", message);    
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PARAM, action, " [", this.level, ":", nameWithoutBr, "] => ", message);
        }
    }
    
    public static String bracket(String name)
    {
        StringBuilder string = null;
        if(name.charAt(0) != '[')
        {
            string = new StringBuilder();
            string.append('[').append(name);
        }
        
        if(name.charAt(name.length() - 1) != ']')
        {
            if(null == string) string = new StringBuilder(name);
            string.append(']');
        }

        if(null != string) return string.toString();
        else return name;
    }
    
    public static String unbracket(String name)
    {
        int begin = 0;

        if(name.charAt(0) == '[')
        {
            begin = 1;
        }

        int end = name.length();
        if(name.charAt(end - 1) == ']')
        {
            end--;
        }    
    
        return name.substring(begin, end);
    }    

    public static String getName(String name)
    {
        int index = name.indexOf('(', 2);
        
        if(index != -1)
        {
            return name.substring(0, index) + ']';
        }
        else
        {
            return name;
        }
    }
    
    public static boolean hasIndex(String name)
    {
        return name.indexOf('(') != -1;
    }
     
    public static int getIndex(String name) throws ParameterException
    {
        int indexStart = name.indexOf('(');
        int indexStop  = name.indexOf(')', indexStart);
        
        if(indexStart  != -1 && indexStop != -1)
        {
            int index = Integer.parseInt(name.substring(indexStart+1, indexStop));
            if(index < 0) throw new ParameterException(index + " is not a valid index value (" + name + ")");
            return index;
        }
        else
        {
            throw new ParameterException("can't get index on name " + name);
        }
    }
     
    public static void testbrackets(String name) throws ParameterException
    {
        if(name.charAt(0) != '[' || name.charAt(name.length()-1) != ']')
        {
            throw new ParameterException("Parameter " + name + "'s name does not start with [ nor ends with ]");
        }
    }    
    
    public static String removeLevel(String name)
    {
        name = unbracket(name);

        int index = name.indexOf(':');
        
        if(index < 0)
        {
            return bracket(name);
        }
        else
        {
            try
            {
                Level.valueOf(name.substring(0, index));
                return bracket(name.substring(index + 1));
            }
            catch(Exception e)
            {
            	return bracket(name);
            }
        }
    }

    public static Level getLevel(String name) throws ParameterException
    {
        int index = name.indexOf(':');
        
        if(index != -1)
        {
            int start;
            if(name.charAt(0) == '[') start = 1;
            else start =0;

            try
            {
                return Level.valueOf(name.substring(start, index));
            }
            catch(Exception e)
            {
            	return null;
            }
        }
        else
        {
            return null;
        }
        
    }

    private class ParameterReference
    {
        public Parameter parameter;
        public String completeName;
        public boolean hasIndex;
        public int index;
    }
}
