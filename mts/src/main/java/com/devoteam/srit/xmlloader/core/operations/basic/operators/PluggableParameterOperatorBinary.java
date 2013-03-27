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
import gp.utils.arrays.CipherArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.MacArray;
import gp.utils.arrays.RandomArray;

import java.math.BigInteger;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorBinary extends AbstractPluggableParameterOperator
{

    final private String NAME_BIN_EQUALS     = "binary.equals";
    final private String NAME_BIN_PARSE      = "binary.parse";
    final private String NAME_BIN_TOSTRING   = "binary.tostring";
    final private String NAME_BIN_FROMBASE64 = "binary.frombase64";
    final private String NAME_BIN_TOBASE64   = "binary.tobase64";
    final private String NAME_BIN_CONTAINS   = "binary.contains";
    final private String NAME_BIN_INDEXOF    = "binary.indexof";
    final private String NAME_BIN_SUBBINARY  = "binary.subbinary";
    final private String NAME_BIN_RANDOM     = "binary.random";
    final private String NAME_BIN_DIGEST     = "binary.digest";
    final private String NAME_BIN_HMAC       = "binary.hmac";
    final private String NAME_BIN_ENCRYPT    = "binary.encrypt";
    final private String NAME_BIN_DECRYPT    = "binary.decrypt";
    final private String NAME_BIN_STATMAX 	 = "binary.statMax";
    final private String NAME_BIN_STATMIN 	 = "binary.statMin";
    final private String NAME_BIN_STATAVERAGE = "binary.statAverage";
    final private String NAME_BIN_STATDEVIATION = "binary.statDeviation";
    final private String NAME_BIN_STATVARIANCE = "binary.statVariance";
    final private String NAME_BIN_STATPOPULAR = "binary.statPopular";
    final private String NAME_BIN_STATMAXFREQ = "binary.statMaxFreq";
    final private String NAME_BIN_FROMIP	= "binary.fromIp";
    final private String NAME_BIN_TOIP		= "binary.toIp";
    final private String NAME_BIN_TONUMBER	= "binary.toNumber";

    
    
    public PluggableParameterOperatorBinary()
    {
        this.addPluggableName(new PluggableName(NAME_BIN_EQUALS));
        this.addPluggableName(new PluggableName(NAME_BIN_PARSE));
        this.addPluggableName(new PluggableName(NAME_BIN_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_BIN_TOSTRING));
        this.addPluggableName(new PluggableName(NAME_BIN_INDEXOF));
        this.addPluggableName(new PluggableName(NAME_BIN_FROMBASE64));
        this.addPluggableName(new PluggableName(NAME_BIN_TOBASE64));
        this.addPluggableName(new PluggableName(NAME_BIN_SUBBINARY));
        this.addPluggableName(new PluggableName(NAME_BIN_RANDOM));
        this.addPluggableName(new PluggableName(NAME_BIN_DIGEST));
        this.addPluggableName(new PluggableName(NAME_BIN_HMAC));
        this.addPluggableName(new PluggableName(NAME_BIN_ENCRYPT));
        this.addPluggableName(new PluggableName(NAME_BIN_DECRYPT));
        this.addPluggableName(new PluggableName(NAME_BIN_STATMAX));
        this.addPluggableName(new PluggableName(NAME_BIN_STATMIN));
        this.addPluggableName(new PluggableName(NAME_BIN_STATAVERAGE));
        this.addPluggableName(new PluggableName(NAME_BIN_STATDEVIATION));
        this.addPluggableName(new PluggableName(NAME_BIN_STATVARIANCE));
        this.addPluggableName(new PluggableName(NAME_BIN_STATPOPULAR));
        this.addPluggableName(new PluggableName(NAME_BIN_STATMAXFREQ));
        this.addPluggableName(new PluggableName(NAME_BIN_FROMIP));
        this.addPluggableName(new PluggableName(NAME_BIN_TOIP));
        this.addPluggableName(new PluggableName(NAME_BIN_TONUMBER));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        
        Parameter param_1 = assertAndGetParameter(operands, "value");

        Parameter result = new Parameter();

        try
        {
            int size = param_1.length();
            for (int i = 0; i < size; i++)
            {
                if (name.equals(NAME_BIN_EQUALS))
                {
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    Array array1 = Array.fromHexString(param_1.get(i).toString());
                    Array array2 = Array.fromHexString(param_2.get(i).toString());
                    result.add(array1.equals(array2));
                }
                else if (name.equals(NAME_BIN_PARSE))
                {
                    result.add(Array.toHexString(new DefaultArray(Utils.parseBinaryString(param_1.get(i).toString()))));
                }
                else if (name.equals(NAME_BIN_FROMBASE64))
                {
                    result.add(Array.toHexString(Array.fromBase64String(param_1.get(i).toString())));
                }
                else if (name.equals(NAME_BIN_TOBASE64))
                {
                    result.add(Array.toBase64String(Array.fromHexString(param_1.get(i).toString())));
                }
                else if (name.equals(NAME_BIN_CONTAINS))
                {
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    Array array1 = Array.fromHexString(param_1.get(i).toString());
                    Array array2 = Array.fromHexString(param_2.get(i).toString());
                    result.add(String.valueOf(array1.indexOf(array2) != -1));
                }
                else if (name.equals(NAME_BIN_INDEXOF))
                {
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    
                    Array array_data = Array.fromHexString(param_1.get(i).toString());
                    Array array_searched = Array.fromHexString(param_2.get(i).toString());

                    int index = 0;
                    Parameter param_3 = operands.get("value3");
                    if(null != param_3)
                    {
                        index = Integer.valueOf(param_3.get(i).toString());
                    }

                    result.add(String.valueOf(array_data.indexOf(array_searched, index)));
                }
                else if (name.equals(NAME_BIN_TOSTRING))
                {
                    result.add(new String(Array.fromHexString(param_1.get(i).toString()).getBytes()));
                }
                else if (name.equals(NAME_BIN_SUBBINARY))
                {
                    int offset = Integer.valueOf(assertAndGetParameter(operands, "value2").get(i).toString()).intValue();
                    int length = Integer.valueOf(assertAndGetParameter(operands, "value3").get(i).toString()).intValue();

                    result.add(Array.toHexString(Array.fromHexString(param_1.get(i).toString()).subArray(offset, length)));
                }
                else if (name.equals(NAME_BIN_RANDOM))
                {
                    result.add(Array.toHexString(new RandomArray(new Integer(param_1.get(i).toString()))));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_DIGEST))
                {
                    Parameter algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                    Array data = Array.fromHexString(param_1.get(i).toString());
                    DigestArray array = new DigestArray(data, algo.get(i).toString());
                    result.add(Array.toHexString(array));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_HMAC))
                {
                    Parameter algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                    Array data = Array.fromHexString(param_1.get(i).toString());
                    Array secret = Array.fromHexString(PluggableParameterOperatorList.assertAndGetParameter(operands, "value3").get(i).toString());
                    MacArray array = new MacArray(data, algo.get(i).toString(), secret);
                    result.add(Array.toHexString(array));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ENCRYPT))
                {
                    Array data = Array.fromHexString(param_1.get(i).toString());
                    String algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2").get(i).toString();
                    String keyAlgo = algo.contains("/") ? algo.substring(0, algo.indexOf('/')):algo;
                    Array secret = Array.fromHexString(PluggableParameterOperatorList.assertAndGetParameter(operands, "value3").get(i).toString());
                    Array salt = Array.fromHexString(PluggableParameterOperatorList.assertAndGetParameter(operands, "value4").get(i).toString());
                    CipherArray array = new CipherArray(data, secret, salt, algo, keyAlgo, Cipher.ENCRYPT_MODE);
                    result.add(Array.toHexString(array));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_DECRYPT))
                {
                    Array data = Array.fromHexString(param_1.get(i).toString());
                    String algo = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2").get(i).toString();
                    String keyAlgo = algo.contains("/") ? algo.substring(0, algo.indexOf('/')):algo;
                    Array secret = Array.fromHexString(PluggableParameterOperatorList.assertAndGetParameter(operands, "value3").get(i).toString());
                    Array salt = Array.fromHexString(PluggableParameterOperatorList.assertAndGetParameter(operands, "value4").get(i).toString());
                    CipherArray array = new CipherArray(data, secret, salt, algo, keyAlgo, Cipher.DECRYPT_MODE);
                    result.add(Array.toHexString(array));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATMAX))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    byte res = Byte.MIN_VALUE;
                    for (int j = 0; j < data.length; j++)
                    {
                    	if (data[j] > res)
                    	{
                    		res = data[j]; 
                    	}
                    }
                    byte[] tabBytes =  new byte[1];
                    tabBytes[0] = res;
                    result.add(Array.toHexString(new DefaultArray(tabBytes)));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATMIN))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    byte res = Byte.MAX_VALUE;
                    for (int j = 0; j < data.length; j++)
                    {
                    	if (data[j] < res)
                    	{
                    		res = data[j]; 
                    	}
                    }
                    byte[] tabBytes =  new byte[1];
                    tabBytes[0] = res;
                    result.add(Array.toHexString(new DefaultArray(tabBytes)));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATAVERAGE))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    byte[] res =  new byte[1];
                    res[0] = calculeAverage(data);
                    result.add(Array.toHexString(new DefaultArray(res)));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATVARIANCE))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
            	    double variance = calculeDeviation(data);
                    result.add(formatDouble(variance));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATDEVIATION))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
            	    double variance = calculeDeviation(data);
            	    double deviation = Math.sqrt(variance); 
                    result.add(formatDouble(deviation));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATPOPULAR))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    byte[] res =  new byte[1];
                    res[0] = (byte) calculePopular(data, false);
                    result.add(Array.toHexString(new DefaultArray(res)));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATMAXFREQ))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    result.add(formatDouble(((double) calculePopular(data, true)) / data.length));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_FROMIP))
                {
                	StringTokenizer ip = new StringTokenizer(param_1.get(i).toString(), "."); 
                	String ret = "";
                	if (ip.countTokens() != 4)
                		throw new ParameterException("Error in operation, " + param_1.get(i) + " isn't a valid IP address");
                	while (ip.hasMoreTokens())
                		ret += Integer.toHexString(Integer.parseInt(ip.nextToken()));
                	result.add(ret);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_TOIP))
                {
                	byte[] ip = DatatypeConverter.parseHexBinary(param_1.get(i).toString());
                	String ret = "";
                	if (ip.length != 4)
                		throw new Exception();
                	for (int j = 0; j < ip.length - 1; j++)
                		ret += (ip[j]&0xff) + ".";
                	ret += (ip[ip.length - 1]&0xff);
                	result.add(ret);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_TONUMBER))
                {
                	BigInteger n = new BigInteger(param_1.get(i).toString(), 16);
                	result.add(n.toString());
                }
                /* experimental                
                else if (name.equalsIgnoreCase(NAME_BIN_STATHISTOVALUE))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    int[] freq = calculeFrequency(data);
            	    for (int k = 0; k < freq.length; k++)
            	    {
            	    	if (freq[k] != 0)
            	    	{	
                            byte[] res =  new byte[1];
            	            res[0] = (byte)(k - 128);
                            result.add(Array.toHexString(new DefaultArray(res)));
            	    	}
            	    }
                }
                else if (name.equalsIgnoreCase(NAME_BIN_STATHISTOFREQ))
                {
                    Array array = Array.fromHexString(param_1.get(i).toString());
                    byte[] data = array.getBytes();
                    int[] freq = calculeFrequency(data);
            	    for (int k = 0; k < freq.length; k++)
            	    {
            	    	if (freq[k] != 0)
            	    	{	
            	    		result.add(formatDouble(((double)freq[k]) / data.length * 100));
            	    	}
            	    }
                }
                */
                else
                {
                    throw new RuntimeException("unsupported operation " + name);
                }
            }
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in operation " + name, e);
        }


        return result;
    }

    private static byte calculeAverage(byte[] data)
    {    
	    int res = data[0];
	    for (int j = 1; j < data.length; j++)
	    {
	   		res += (int) data[j]; 
	    }
	    return (byte)(res / data.length);
    }
    
    private static double calculeDeviation(byte[] data)
    {    
        byte average = calculeAverage(data);
        
	    int avSquare = data[0]*data[0];
	    for (int j = 1; j < data.length; j++)
	    {
	    	avSquare += (int) data[j] * data[j]; 
	    }

	    return avSquare / data.length - average * average;
    }

    public static int calculePopular(byte[] data, boolean frequency)
    {    	  
    	int[] freq = calculeFrequency(data);    	

    	int maxFreq  = -1; 
	    int maxValue  = -1;
	    for (int k = 0; k < freq.length; k++)
	    {
	    	if (freq[k] > maxValue)
	    	{
	    		maxValue = freq[k];
	    		maxFreq = k;
	    	}
	    }
	    if (frequency)
	    {
	    	return maxValue;
	    }
	    else
	    {
	    	return maxFreq - 128;
	    }
    }

    private static int[] calculeFrequency(byte[] data)
    {
    	int[] freq = new int[256];    	
	    for (int j = 0; j < data.length; j++)
	    {
	    	int index = data[j] + 128;			// change the signe
	    	freq[index]++;
	    }
	    return freq;
    }

}

