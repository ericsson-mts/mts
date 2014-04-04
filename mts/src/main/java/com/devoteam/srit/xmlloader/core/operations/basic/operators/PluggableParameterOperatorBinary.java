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
import gp.utils.arrays.Base64Coder;
import gp.utils.arrays.CipherArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.MacArray;
import gp.utils.arrays.RandomArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    final private String NAME_BIN_LENGTH     = "binary.length";
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
    final private String NAME_BIN_CIPHERRTP	= "binary.cipherRTP";
    final private String NAME_BIN_UNCIPHERRTP	= "binary.uncipherRTP";
    final private String NAME_BIN_AUTHRTP	= "binary.authRTP";
    final private String NAME_BIN_RTPKEYDERIVATION = "binary.RTPKeyDerivation";

    
    
    public PluggableParameterOperatorBinary()
    {
        this.addPluggableName(new PluggableName(NAME_BIN_EQUALS));
        this.addPluggableName(new PluggableName(NAME_BIN_PARSE));
        this.addPluggableName(new PluggableName(NAME_BIN_CONTAINS));
        this.addPluggableName(new PluggableName(NAME_BIN_LENGTH));
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
        this.addPluggableName(new PluggableName(NAME_BIN_CIPHERRTP));
        this.addPluggableName(new PluggableName(NAME_BIN_UNCIPHERRTP));
        this.addPluggableName(new PluggableName(NAME_BIN_AUTHRTP));
        this.addPluggableName(new PluggableName(NAME_BIN_RTPKEYDERIVATION));
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
                else if (name.equalsIgnoreCase(NAME_BIN_LENGTH))
                {
                    result.add(String.valueOf(param_1.get(i).toString().length() / 2));
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
                else if (name.equalsIgnoreCase(NAME_BIN_RTPKEYDERIVATION))
                {
                	Parameter param_2 = assertAndGetParameter(operands, "value2");
                	Parameter param_3 = assertAndGetParameter(operands, "value3");
                	String lifetime = ""; String mki = "";
                	
                	if (!param_1.get(0).toString().contains("AES_CM_128_HMAC_SHA1"))
                		throw new ParameterException("Algorithm " + param_1.get(0).toString().split(" ")[1] + " is not supported, only AES_CM_128_HMAC_SHA1 is supported");
                	
                	String inline = param_1.get(0).toString().split(" ")[2].substring(7);
                	String[] inlineSplitted = inline.replace('|', ' ').split(" ");
                	String keyAndSalt = inlineSplitted[0];
                	if (inlineSplitted.length == 2)
                	{
                		if (inlineSplitted[1].replace(':', ' ').split(" ").length == 2)
                			mki = inlineSplitted[1];
                		else
                			lifetime = inlineSplitted[1];
                	}
                	if (inlineSplitted.length == 3)
                	{
                		lifetime = inlineSplitted[1];
                		mki = inlineSplitted[2];
                	}
                	String algo = param_1.get(0).toString().split(" ")[1];
                	String[] decodedInline = decodeInline(keyAndSalt);
                	long calculatedLifeTime = parseSRTPLifeTime(lifetime);
                	
                	Cipher AESCipher = Cipher.getInstance("AES/ECB/NOPADDING");
                	byte[][] derivatedKeys = deriveSRTPKeys(AESCipher, 0, calculatedLifeTime, decodedInline[1].getBytes(), decodedInline[0].getBytes());
                	String[] ret = new String[3]; ret[0] = ""; ret[1] = ""; ret[2] = "";
                	int a;
                	for (a = 0; a < derivatedKeys[0].length; a++)
                		ret[0] += String.format("%02x", derivatedKeys[0][a], 16);
                	for (a = 0; a < derivatedKeys[1].length; a++)
                		ret[1] += String.format("%02x", derivatedKeys[1][a], 16);
                	for (a = 0; a < derivatedKeys[2].length; a++)
                		ret[2] += String.format("%02x", derivatedKeys[2][a], 16);
                	for (a = 0; a < ret.length; a++)
                		result.add(ret[a]);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_AUTHRTP))
                {
                	// param_1 = tab holding encrypted RTP packets to authenticate 
                	Parameter param_2 = assertAndGetParameter(operands, "value2"); // AUTH KEY
                	Parameter param_3 = assertAndGetParameter(operands, "value3"); // ROC
                	Parameter param_4 = assertAndGetParameter(operands, "value4"); // INLINE BLOCK
                	
                	String[] algo = param_4.get(0).toString().split(" ")[1].split("_");
                	int authTagLength = Integer.parseInt(algo[algo.length - 1]) / 8;
                	String data = param_1.get(0).toString();
                	byte[] authKey = DatatypeConverter.parseHexBinary(param_2.get(0).toString());
                	int ROC = Integer.parseInt(param_3.get(0).toString());
                	

                	Mac hmacSha1 = Mac.getInstance("HmacSHA1");
                	SecretKey key = new SecretKeySpec(authKey, "HMAC");
                	try 
                    {
                        hmacSha1.init(key);
                    } 
                    catch (InvalidKeyException e) 
                    {
                        e.printStackTrace();
                    }
                	
                	hmacSha1.update(data.getBytes(), 0, data.getBytes().length);
                	byte[] rb = new byte[4];
                    rb[0] = (byte) (ROC >> 24);
                    rb[1] = (byte) (ROC >> 16);
                    rb[2] = (byte) (ROC >> 8);
                    rb[3] = (byte) ROC;
                    hmacSha1.update(rb);
                    	
                    byte[] authTag = hmacSha1.doFinal();
                    String ret = "";
                    for (int a = 0; a < authTagLength; a++)
                    	ret += String.format("%02x", authTag[a], 16);
                    result.add(ret);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_CIPHERRTP))
                {
                	// param_1 = tab holding RTP payloads to authenticate 
                	Parameter param_2 = assertAndGetParameter(operands, "value2"); // tab holding RTP seq num
                	Parameter param_3 = assertAndGetParameter(operands, "value3"); // encryption key
                	Parameter param_4 = assertAndGetParameter(operands, "value4"); // ROC
                }
                else if (name.equalsIgnoreCase(NAME_BIN_UNCIPHERRTP))
                {
                	
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

    private static String[] decodeInline(String inline) throws ParameterException, UnsupportedEncodingException
    {
    	String[] ret = new String[2];
    	
    	byte[] decodeInlineByteTab = Array.fromBase64String(inline).getBytes();
    	
    	if (decodeInlineByteTab.length != 30)
    		throw new ParameterException("concatened master key and master salt length is not equal to 30 bytes : " + new String(decodeInlineByteTab, "UTF-8"));
    	
    	byte[] masterKey = new byte[16];
    	byte[] masterSalt = new byte[14];
    	
    	for (int i = 0; i < 16; i++)
    		masterKey[i] = decodeInlineByteTab[i];
    	for (int i = 0; i < 14; i++)
    		masterSalt[i] = decodeInlineByteTab[i + 16];
    	ret[0] = new String(masterKey, "UTF-8");
    	ret[1] = new String(masterSalt, "UTF-8");
    	
    	return ret;
    }
    
    private static long parseSRTPLifeTime(String lifetime) throws ParameterException
    {
    	String[] str = lifetime.replace('^', ' ').split(" ");
    	if (str.length != 2)
    		throw new ParameterException("lifetime is malformed : expected x^y, got " + lifetime);
    	return (long) Math.pow(Integer.parseInt(str[0]), Integer.parseInt(str[1]));
    }
    
    private static void computeIv(byte[] iv, long label, long index, long kdv, byte[] masterSalt)
    {
    	long key_id;

        if (kdv == 0)
        {
            key_id = label << 48;
        }
        else
        {
            key_id = ((label << 48) | (index / kdv));
        }

        for (int i = 0; i < 7; i++)
        {
            iv[i] = masterSalt[i];
        }

        for (int i = 7; i < 14; i++)
        {
            iv[i] = (byte)
                   ((byte)(0xFF & (key_id >> (8 * (13 - i)))) ^ masterSalt[i]);
        }

        iv[14] = iv[15] = 0;
    }
    
    private static byte[][] deriveSRTPKeys(Cipher AEScipher, long index, long keyDerivationRate, byte[] masterSalt, byte[] masterKey) throws UnsupportedEncodingException
    {
    	byte[] iv = new byte[16];
    	byte[] encKey = new byte[16];
    	byte[] authKey = new byte[20];
    	byte[] saltKey = new byte[14];
    	byte[][] ret = new byte[3][];
    	long label = 0;

        // compute the session encryption key
        computeIv(iv, label, index, keyDerivationRate, masterSalt);
        SecretKey encryptionKey = new SecretKeySpec(masterKey, 0, 16, "AES");
        
        try 
        {
            AEScipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        } 
        catch (InvalidKeyException e1) 
        {
            e1.printStackTrace();
        }
        
        getCipherStream(AEScipher, encKey, 16, iv);
        
        label = 0x01;
        computeIv(iv, label, index, keyDerivationRate, masterSalt);
        getCipherStream(AEScipher, authKey, 20, iv);

        label = 0x02;
        computeIv(iv, label, index, keyDerivationRate, masterSalt);
        getCipherStream(AEScipher, saltKey, 14, iv);       
        ret[0] = encKey;
        ret[1] = authKey;
        ret[2] = saltKey;
        return ret;
    }
    
    private static void getCipherStream(Cipher aesCipher, byte[] out, int length, byte[] iv)
    {
    	final int BLKLEN = 16;
        
        byte[] in  = new byte[BLKLEN];
        byte[] tmp = new byte[BLKLEN];

        System.arraycopy(iv, 0, in, 0, 14);

        try 
        {
            
            int ctr;
            for (ctr = 0; ctr < length / BLKLEN; ctr++)
            {
                // compute the cipher stream
                in[14] = (byte) ((ctr & 0xFF00) >> 8);
                in[15] = (byte) ((ctr & 0x00FF));

                aesCipher.update(in, 0, BLKLEN, out, ctr * BLKLEN);
            }

            // Treat the last bytes:
            in[14] = (byte) ((ctr & 0xFF00) >> 8);
            in[15] = (byte) ((ctr & 0x00FF));

            aesCipher.doFinal(in, 0, BLKLEN, tmp, 0);
            System.arraycopy(tmp, 0, out, ctr * BLKLEN, length % BLKLEN);
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
    }
}

