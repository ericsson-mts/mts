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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.Map;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorBoolean extends AbstractPluggableParameterOperator
{
    final private String NAME_AND = "and";
    final private String NAME_OR  = "or";
    final private String NAME_NAND= "nand";
    final private String NAME_NOR = "nor";
    final private String NAME_XOR = "xor";
    final private String NAME_NOT = "not";

    final private String NAME_B_AND    = "boolean.and";
    final private String NAME_B_OR     = "boolean.or";
    final private String NAME_B_NAND   = "boolean.nand";
    final private String NAME_B_NOR    = "boolean.nor";
    final private String NAME_B_XOR    = "boolean.xor";
    final private String NAME_B_NOT    = "boolean.not";
    final private String NAME_B_EQUALS = "boolean.equals";
    final private String NAME_B_RAND   = "boolean.random";
    
    public PluggableParameterOperatorBoolean()
    {
        this.addPluggableName(new PluggableName(NAME_AND, NAME_B_AND));
        this.addPluggableName(new PluggableName(NAME_OR, NAME_B_OR));
        this.addPluggableName(new PluggableName(NAME_NAND, NAME_B_NAND));
        this.addPluggableName(new PluggableName(NAME_NOR, NAME_B_NOR));
        this.addPluggableName(new PluggableName(NAME_XOR, NAME_B_XOR));
        this.addPluggableName(new PluggableName(NAME_NOT, NAME_B_NOT));

        this.addPluggableName(new PluggableName(NAME_B_AND));
        this.addPluggableName(new PluggableName(NAME_B_OR));
        this.addPluggableName(new PluggableName(NAME_B_NAND));
        this.addPluggableName(new PluggableName(NAME_B_NOR));
        this.addPluggableName(new PluggableName(NAME_B_XOR));
        this.addPluggableName(new PluggableName(NAME_B_NOT));
        this.addPluggableName(new PluggableName(NAME_B_EQUALS));
        this.addPluggableName(new PluggableName(NAME_B_RAND));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);

        Parameter param_1 = null;
        if(!name.equals(NAME_B_RAND)) param_1 = assertAndGetParameter(operands, "value");

        Parameter param_2 = null;
        if(!name.equals(NAME_NOT) && !name.equals(NAME_B_NOT) && !name.equals(NAME_B_RAND)) param_2 = assertAndGetParameter(operands, "value2");
        
        Parameter result = new Parameter();
        
        int size = 0;
        if(null != param_1) size = param_1.length();
        if(name.equals(NAME_AND) || name.equals(NAME_B_AND))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(bool1 & bool2));
            }
        }
        else if(name.equals(NAME_OR) || name.equals(NAME_B_OR))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(bool1 | bool2));
            }
        }
        else if(name.equals(NAME_NAND) || name.equals(NAME_B_NAND))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(!(bool1 & bool2)));
            }
        }
        else if(name.equals(NAME_NOR) || name.equals(NAME_B_NOR))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(!(bool1 | bool2)));
            }
        }
        else if(name.equals(NAME_XOR) || name.equals(NAME_B_XOR))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(bool1 ^ bool2));
            }
        }
        else if(name.equals(NAME_NOT) || name.equals(NAME_B_NOT))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                result.add(String.valueOf(!bool1));
            }
        }
        else if(name.equals(NAME_B_EQUALS))
        {
            for(int i=0;i<size;i++)
            {
                boolean bool1 = Utils.parseBoolean(param_1.get(i).toString(), name);
                boolean bool2 = Utils.parseBoolean(param_2.get(i).toString(), name);
                result.add(String.valueOf(bool1 == bool2));
            }
        }
        else if(name.equals(NAME_B_RAND))
        {
            result.add(String.valueOf(Math.random() >= 0.5));
        }
        else throw new RuntimeException("unsupported operation " + name);
        
        return result;
    }

}

