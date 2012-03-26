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

package com.devoteam.srit.xmlloader.sigtran.operation;

import com.devoteam.srit.xmlloader.core.operations.basic.operators.*;
import java.util.Map;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorBat extends AbstractPluggableParameterOperator {
    public PluggableParameterOperatorBat() {
        this.addPluggableName(new PluggableName("bat.convertlen"));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException {
        normalizeParameters(operands);

        Parameter result = new Parameter();
        Parameter valueParameter = assertAndGetParameter(operands, "value");

        // value should have a len of 1
        if(valueParameter.length() != 1){
            throw new ParameterException("value should have size of 1");
        }

        // value should be an integer
        if(!Utils.isInteger(valueParameter.get(0).toString())){
            throw new ParameterException("value should be an integer");
        }

        int valueInteger = Integer.parseInt(valueParameter.get(0).toString());

        result.add(Integer.toString(convert(valueInteger)));
        
        return result;
    }

    private int convert(int valueInteger) throws ParameterException{
        // value should be positive and lower than 2047 (11 bits)
        if(valueInteger < 0 || valueInteger > 2047){
            throw new ParameterException("value should be 0 <= value <= 2047");
        }

        if(valueInteger <= 15){
            // encoded on 1 byte
            valueInteger = 0xff & (0x80 | valueInteger);
        }
        else{
            int msb = ((valueInteger & 0x7f80) >> 7) & 0xff;
            int lsb = (valueInteger & 0x007f);

            // encoded on 2 bytes
            valueInteger = 0xffff & ((lsb << 8) + (msb | 0x80));
        }
        return valueInteger;
    }
}
