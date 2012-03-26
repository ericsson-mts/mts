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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.security.MessageDigest;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorDigest extends AbstractPluggableParameterOperator
{

    final private String NAME_DIGEST = "digest";
    final private String NAME_MD5 = "MD5";
    final private String NAME_HMACMD5 = "HmacMD5";

    public PluggableParameterOperatorDigest()
    {

        this.addPluggableName(new PluggableName(NAME_DIGEST));
        this.addPluggableName(new PluggableName(NAME_MD5));
        this.addPluggableName(new PluggableName(NAME_HMACMD5));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        Parameter result = new Parameter();

        Parameter input = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter algorithm = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

        try
        {
            for (int i = 0; i < input.length(); i++)
            {
                String var1 = input.get(i).toString();
                String var2 = algorithm.get(i).toString();
                if (name.equalsIgnoreCase(NAME_DIGEST))
                {
                    GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, NAME_HMACMD5, " is now deprecated, please use string.digest with value1=\"MD5\"");
                    result.add(executeDigest(var1, var2));
                }
                else if (name.equalsIgnoreCase(NAME_MD5))
                {
                    GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, NAME_HMACMD5, " is now deprecated, please use binary.digest or string.digest with value1=\"MD5\"");

                    result.add(executeMD5(var1, var2));
                }
                else if (name.equalsIgnoreCase(NAME_HMACMD5))
                {
                    GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, NAME_HMACMD5, " is now deprecated, please use binary.hmac with value1=\"HmacMD5\"");

                    result.add(executeHMacMD5(var1, var2));
                }
                else throw new RuntimeException("unsupported operator " + name);
            }
        }
        catch (ParameterException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in MD5 operator", e);
        }

        return result;
    }

    public String executeDigest(String var1, String var2) throws Exception
    {
        MessageDigest msgDigest = MessageDigest.getInstance(var2);
        byte[] inputs = var1.getBytes();


        byte[] outputs = msgDigest.digest(inputs);
        String md5 = "";
        for (int i = 0; i < outputs.length; i++)
        {
            int integer = outputs[i];
            if (integer < 0)
            {
                integer = outputs[i] + 256;
            }
            String str = Integer.toHexString(integer);
            while (str.length() < 2)
            {
                str = "0" + str;
            }
            md5 += str;
        }
        return md5;
    }

    public String executeMD5(String var1, String var2) throws Exception
    {
        MessageDigest msgDigest = MessageDigest.getInstance("MD5");

        byte[] inputs;

        if (var2.equalsIgnoreCase("binary"))
        {
            inputs = Utils.parseBinaryString(var1);
        }
        else
        {
            inputs = var1.getBytes();
        }

        byte[] outputs = msgDigest.digest(inputs);
        String md5 = "";
        for (int i = 0; i < outputs.length; i++)
        {
            int integer = outputs[i];
            if (integer < 0)
            {
                integer = outputs[i] + 256;
            }
            String str = Integer.toHexString(integer);
            while (str.length() < 2)
            {
                str = "0" + str;
            }
            md5 += str;
        }

        return md5;
    }
    static public final String HEX_DIGITS = "0123456789abcdef";

    /** Creates a new instance of ListOperationAdd */
    public String executeHMacMD5(String input, String secret) throws Exception
    {
        SecretKeySpec key = new SecretKeySpec(secret.getBytes("UTF8"), "HmacMD5");

        // Create a MAC object using HMAC-MD5 and initialize with key
        Mac mac = Mac.getInstance(key.getAlgorithm());
        mac.init(key);

        // Encode the string into bytes using utf-8 and digest it
        byte[] utf8 = Utils.parseBinaryString(input);

        byte[] digest = mac.doFinal(utf8);

        // Convert the digest into a string
        StringBuilder output = new StringBuilder(digest.length * 2);
        for (int i = 0; i < digest.length; i++)
        {
            int b = digest[i] & 0xFF;
            output.append(HEX_DIGITS.charAt(b >>> 4)).append(HEX_DIGITS.charAt(b & 0x0F));
        }

        return output.toString();
    }
}
