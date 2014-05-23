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

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.DateUtils;
/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorNumber extends AbstractPluggableParameterOperator
{
    final private String NAME_ADD = "add"; 
    final private String NAME_SUBSTRACT = "substract"; 
    final private String NAME_DIVIDE = "divide"; 
    final private String NAME_MODULO = "mudulo";
    final private String NAME_MULTIPLY = "multiply"; 
    final private String NAME_LET = "lowerEqualThan"; 
    final private String NAME_LT = "lowerThan"; 
    final private String NAME_GET = "greaterEqualThan"; 
    final private String NAME_GT = "greaterThan";
    
    final private String NAME_N_ADD = "number.add";
    final private String NAME_N_SUBSTRACT = "number.substract";
    final private String NAME_N_DIVIDE = "number.divide";
    final private String NAME_N_MODULO = "number.modulo";
    final private String NAME_N_MULTIPLY = "number.multiply";
    final private String NAME_N_LET = "number.lowerEqualThan";
    final private String NAME_N_LT = "number.lowerThan";
    final private String NAME_N_GET = "number.greaterEqualThan";
    final private String NAME_N_GT = "number.greaterThan";
    final private String NAME_N_POWER = "number.power";
    final private String NAME_N_RANDOMGAUSSIAN = "number.randomGaussian";
    final private String NAME_N_PARSEDATE = "number.parseDate";
    final private String NAME_N_TODATE = "number.toDate";
    final private String NAME_N_TOBINARY = "number.toBinary";
    final private String NAME_N_MAX = "number.max";
    final private String NAME_N_MIN = "number.min";
    

    private DecimalFormat decimalFormat;

    public PluggableParameterOperatorNumber()
    {
        this.decimalFormat = new DecimalFormat("0.000000");
        this.decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        this.addPluggableName(new PluggableName(NAME_ADD, NAME_N_ADD));
        this.addPluggableName(new PluggableName(NAME_SUBSTRACT, NAME_N_SUBSTRACT));
        this.addPluggableName(new PluggableName(NAME_DIVIDE, NAME_N_DIVIDE));
        this.addPluggableName(new PluggableName(NAME_MODULO, NAME_N_MODULO));
        this.addPluggableName(new PluggableName(NAME_MULTIPLY, NAME_N_MULTIPLY));
        this.addPluggableName(new PluggableName(NAME_LET, NAME_N_LET));
        this.addPluggableName(new PluggableName(NAME_LT, NAME_N_LT));
        this.addPluggableName(new PluggableName(NAME_GET, NAME_N_GET));
        this.addPluggableName(new PluggableName(NAME_GT, NAME_N_GT));

        this.addPluggableName(new PluggableName(NAME_N_ADD));
        this.addPluggableName(new PluggableName(NAME_N_SUBSTRACT));
        this.addPluggableName(new PluggableName(NAME_N_DIVIDE));
        this.addPluggableName(new PluggableName(NAME_N_MODULO));
        this.addPluggableName(new PluggableName(NAME_N_MULTIPLY));
        this.addPluggableName(new PluggableName(NAME_N_LET));
        this.addPluggableName(new PluggableName(NAME_N_LT));
        this.addPluggableName(new PluggableName(NAME_N_GET));
        this.addPluggableName(new PluggableName(NAME_N_GT));
        this.addPluggableName(new PluggableName(NAME_N_POWER));
        this.addPluggableName(new PluggableName(NAME_N_RANDOMGAUSSIAN));
        this.addPluggableName(new PluggableName(NAME_N_PARSEDATE));
        this.addPluggableName(new PluggableName(NAME_N_TODATE));
        this.addPluggableName(new PluggableName(NAME_N_TOBINARY));
        this.addPluggableName(new PluggableName(NAME_N_MAX));
        this.addPluggableName(new PluggableName(NAME_N_MIN));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        
        Parameter param1 = assertAndGetParameter(operands, "value");
        Parameter result = new Parameter();
        try
        {
            for(int i=0; i<param1.length(); i++)
            {

                if(name.equalsIgnoreCase(NAME_ADD) || name.equalsIgnoreCase(NAME_N_ADD)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                	result.add(formatDouble(op1 + op2));
                }
                else if(name.equalsIgnoreCase(NAME_MULTIPLY) || name.equalsIgnoreCase(NAME_N_MULTIPLY)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(formatDouble(op1 * op2));
                }
                else if(name.equalsIgnoreCase(NAME_DIVIDE) || name.equalsIgnoreCase(NAME_N_DIVIDE)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    if (op2 != 0)
                    {
                    	result.add(formatDouble(op1 / op2));
                    }
                    else
                    {
                    	result.add(formatDouble((double) 0));
                    }
                }
                else if(name.equalsIgnoreCase(NAME_MODULO) || name.equalsIgnoreCase(NAME_N_MODULO)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(formatDouble(op1 % op2));
                }
                else if(name.equalsIgnoreCase(NAME_SUBSTRACT) || name.equalsIgnoreCase(NAME_N_SUBSTRACT)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(formatDouble(op1 - op2));
                }
                else if(name.equalsIgnoreCase(NAME_LET) || name.equalsIgnoreCase(NAME_N_LET)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(String.valueOf(op1 <= op2));
                }
                else if(name.equalsIgnoreCase(NAME_LT) || name.equalsIgnoreCase(NAME_N_LT)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(String.valueOf(op1 < op2));
                }
                else if(name.equalsIgnoreCase(NAME_GET) || name.equalsIgnoreCase(NAME_N_GET)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(String.valueOf(op1 >= op2));
                }
                else if(name.equalsIgnoreCase(NAME_GT) || name.equalsIgnoreCase(NAME_N_GT)) {
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(String.valueOf(op1 > op2));
                }
                else if(name.equalsIgnoreCase(NAME_N_POWER)) {
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(String.valueOf(Math.pow(op1, op2)));
                }
                else if(name.equalsIgnoreCase(NAME_N_RANDOMGAUSSIAN)) {
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op1 = Double.parseDouble(param1.get(i).toString());
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                	Double moyenne = op1;
                	Double deviation = op2;
                	Random generator;
                	generator = new Random();
                	double num = generator.nextGaussian();
                	double num2 = ( num  * deviation ) + moyenne;
                	result.add(String.valueOf(num2));
                }
                else if (name.equalsIgnoreCase(NAME_N_PARSEDATE)) {
                	long timestamp = DateUtils.parseDate(param1.get(i).toString());
                    result.add(String.valueOf(timestamp));
                }
                else if (name.equalsIgnoreCase(NAME_N_TODATE)) {
                	if (null != operands.get("value2")) {
                		Date date = new Date(Long.parseLong((String) param1.get(i)));
                		Parameter param2 = assertAndGetParameter(operands, "value2");
                    	result.add((new SimpleDateFormat(param2.get(i).toString())).format(date));
                	}
                	else {
                		Date date = new Date(Long.parseLong((String) param1.get(i)));
                    	result.add((new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date)));
                	}
                }
                else if (name.equalsIgnoreCase(NAME_N_TOBINARY))
                {
                	BigInteger n = new BigInteger(param1.get(i).toString());
                	result.add(n.toString(16));
                }
                else if (name.equalsIgnoreCase(NAME_N_MAX))
                {
                	Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    if (op1 < op2)
                    {
                    	result.add(formatDouble(op2));
                    }
                    else
                    {
                    	result.add(formatDouble(op1));
                    }
                }
                else if (name.equalsIgnoreCase(NAME_N_MAX))
                {
                	Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(formatDouble(Math.max(op1, op2)));
                }
                else if (name.equalsIgnoreCase(NAME_N_MIN))
                {
                	Double op1 = Double.parseDouble(param1.get(i).toString());
                    Parameter param2 = assertAndGetParameter(operands, "value2");
                    Double op2 = Double.parseDouble(param2.get(i).toString());
                    result.add(formatDouble(Math.min(op1, op2)));
                }
                else throw new RuntimeException("unsupported operation " + name);
            }
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in operation " + name + " with value=\"" + param1 +"\"", e);
        }
        return result;
    }
       
}
