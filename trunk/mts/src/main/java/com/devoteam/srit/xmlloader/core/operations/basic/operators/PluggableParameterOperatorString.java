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
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.MacArray;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorString extends AbstractPluggableParameterOperator
{

    final private String NAME_CONCAT = "concat";
    final private String NAME_CONTAINS = "contains";
    final private String NAME_EQUALS = "equals";
    final private String NAME_MATCHES = "matches";
    final private String NAME_LENGTH = "length";

    final private String NAME_S_CONTAINS = "string.contains";
    final private String NAME_S_LENGTH   = "string.length";
    final private String NAME_S_EQUALS   = "string.equals";
    final private String NAME_S_EQUALS_I = "string.equalsignorecase";
    final private String NAME_S_MATCHES  = "string.matches";
    final private String NAME_S_INDEXOF  = "string.indexof";
    final private String NAME_S_SPLIT  = "string.split";
    final private String NAME_S_SUBSTRING= "string.substring";
    final private String NAME_S_STARTSWITH  = "string.startswith";
    final private String NAME_S_ENDSWITH    = "string.endswith";
    final private String NAME_S_TOLOWERCASE = "string.tolowercase";
    final private String NAME_S_TOUPPERCASE = "string.touppercase";
    final private String NAME_S_TOBINARY = "string.tobinary";
    final private String NAME_S_RANDOM = "string.random";
    final private String NAME_S_DIGEST = "string.digest";
    final private String NAME_S_HMAC = "string.hmac";
    final private String NAME_TRIM  = "string.trim";

    public PluggableParameterOperatorString()
    {

        this.addPluggableName(new PluggableName(NAME_CONCAT, "no replacement"));
        this.addPluggableName(new PluggableName(NAME_CONTAINS, NAME_S_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_EQUALS, NAME_S_EQUALS));
        this.addPluggableName(new PluggableName(NAME_MATCHES, NAME_S_MATCHES));
        this.addPluggableName(new PluggableName(NAME_LENGTH, NAME_S_LENGTH));

        this.addPluggableName(new PluggableName(NAME_S_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_S_EQUALS));
        this.addPluggableName(new PluggableName(NAME_S_EQUALS_I));
        this.addPluggableName(new PluggableName(NAME_S_MATCHES));
        this.addPluggableName(new PluggableName(NAME_S_LENGTH));
        this.addPluggableName(new PluggableName(NAME_S_SUBSTRING));
        this.addPluggableName(new PluggableName(NAME_S_INDEXOF));
        this.addPluggableName(new PluggableName(NAME_S_SPLIT));
        this.addPluggableName(new PluggableName(NAME_S_STARTSWITH));
        this.addPluggableName(new PluggableName(NAME_S_ENDSWITH));
        this.addPluggableName(new PluggableName(NAME_S_TOLOWERCASE));
        this.addPluggableName(new PluggableName(NAME_S_TOUPPERCASE));
        this.addPluggableName(new PluggableName(NAME_S_TOBINARY));
        this.addPluggableName(new PluggableName(NAME_S_RANDOM));
        this.addPluggableName(new PluggableName(NAME_S_DIGEST));
        this.addPluggableName(new PluggableName(NAME_S_HMAC));
        this.addPluggableName(new PluggableName(NAME_TRIM));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);

        Parameter param1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter result = new Parameter();

        int length = param1.length();
        for (int i = 0; i < length; i++)
        {
            if (name.equalsIgnoreCase(NAME_CONCAT))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(param1.get(i).toString() + param2.get(i).toString());
            }
            else if (name.equalsIgnoreCase(NAME_CONTAINS) || name.equalsIgnoreCase(NAME_S_CONTAINS))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(String.valueOf(param1.get(i).toString().contains(param2.get(i).toString())));
            }
            else if (name.equalsIgnoreCase(NAME_EQUALS) || name.equalsIgnoreCase(NAME_S_EQUALS))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(String.valueOf(param1.get(i).toString().equals(param2.get(i).toString())));
            }
            else if (name.equalsIgnoreCase(NAME_S_EQUALS_I))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(String.valueOf(param1.get(i).toString().equalsIgnoreCase(param2.get(i).toString())));
            }
            else if (name.equalsIgnoreCase(NAME_S_INDEXOF))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                Parameter param3 = operands.get("value3");
                int index = 0;
                if(null != param3) index =Integer.valueOf(param3.get(i).toString());
                result.add(String.valueOf(param1.get(i).toString().indexOf(param2.get(i).toString(), index)));
            }
            else if (name.equalsIgnoreCase(NAME_S_SPLIT))
            {
                Parameter splitter = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                String[] array = Utils.splitNoRegex(param1.get(i).toString(), splitter.get(i).toString());
                for(String string:array) result.add(string);
            }
            else if (name.equalsIgnoreCase(NAME_S_SUBSTRING))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                int begin = new Integer(param2.get(i).toString());

                Parameter param3 = operands.get("value3");
                int end = param1.get(i).toString().length();
                if(null != param3) end =Integer.valueOf(param3.get(i).toString()) + 1;
                if (end > param1.get(i).toString().length()) end = param1.get(i).toString().length(); 

                result.add(String.valueOf(param1.get(i).toString().substring(begin, end)));
            }
            else if (name.equalsIgnoreCase(NAME_S_STARTSWITH))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(String.valueOf(param1.get(i).toString().startsWith(param2.get(i).toString())));
            }
            else if (name.equalsIgnoreCase(NAME_S_ENDSWITH))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                result.add(String.valueOf(param1.get(i).toString().endsWith(param2.get(i).toString())));
            }
            else if (name.equalsIgnoreCase(NAME_S_TOLOWERCASE))
            {
                result.add(String.valueOf(param1.get(i).toString().toLowerCase()));
            }
            else if (name.equalsIgnoreCase(NAME_S_TOUPPERCASE))
            {
                result.add(String.valueOf(param1.get(i).toString().toUpperCase()));
            }
            else if (name.equalsIgnoreCase(NAME_LENGTH) || name.equalsIgnoreCase(NAME_S_LENGTH))
            {
                result.add(String.valueOf(param1.get(i).toString().length()));
            }
            else if (name.equalsIgnoreCase(NAME_MATCHES) || name.equalsIgnoreCase(NAME_S_MATCHES))
            {
                Parameter param2 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                Pattern p = Utils.compilesRegex(param2.get(i).toString());
                Matcher m = p.matcher(param1.get(i).toString());
                result.add(Boolean.toString(m.find()));
            }
            else if (name.equalsIgnoreCase(NAME_S_TOBINARY))
            {
                result.add(Array.toHexString(new DefaultArray(param1.get(i).toString().getBytes())));
            }
            else if (name.equalsIgnoreCase(NAME_S_RANDOM))
            {
            	int numChar = Integer.valueOf(param1.get(i).toString());
            	String str = Utils.randomString(numChar);
            	result.add(str);
            }
            else if (name.equalsIgnoreCase(NAME_S_DIGEST))
            {
                Parameter algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                DefaultArray data = new DefaultArray(param1.get(i).toString().getBytes());
                DigestArray array = new DigestArray(data, algo.get(i).toString());
                result.add(Array.toHexString(array));
            }
            else if (name.equalsIgnoreCase(NAME_S_HMAC))
            {
                Parameter algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                DefaultArray secret = new DefaultArray(PluggableParameterOperatorList.assertAndGetParameter(operands, "value3").get(i).toString().getBytes());
                DefaultArray data = new DefaultArray(param1.get(i).toString().getBytes());
                MacArray array = new MacArray(data, algo.get(i).toString(), secret);
                result.add(Array.toHexString(array));
            }
            else if (name.equalsIgnoreCase(NAME_TRIM))
            {
                result.add(String.valueOf(param1.get(i).toString().trim()));
            } else
            {
                throw new RuntimeException("unsupported operator " + name);
            }
        }
        return result;
    }
}
