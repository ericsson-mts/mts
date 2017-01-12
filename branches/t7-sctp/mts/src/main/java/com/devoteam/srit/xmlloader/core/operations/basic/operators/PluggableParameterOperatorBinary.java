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

import com.devoteam.srit.xmlloader.asn1.ASNMessage;
import com.devoteam.srit.xmlloader.asn1.ASNToXMLConverter;
import com.devoteam.srit.xmlloader.asn1.BN_ASNMessage;
import com.devoteam.srit.xmlloader.asn1.XMLToASNParser;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.GenericWrapper;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.CipherArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.MacArray;
import gp.utils.arrays.RandomArray;
import gp.utils.arrays.SupArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.dom4j.Document;
import org.dom4j.Element;

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
    final private String NAME_BIN_AUTHRTP	= "binary.authRTP";
    final private String NAME_BIN_RTPKEYDERIVATION = "binary.RTPKeyDerivation";
    final private String NAME_BIN_DIFFERENCE = "binary.difference";
    final private String NAME_BIN_XMLTOASN = "binary.xmlToAsn";
    final private String NAME_BIN_ASNTOXML = "binary.asnToXml";
    final private String NAME_BIN_ENDIAN = "binary.endian";
    final private String NAME_BIN_ENCODE = "binary.encode";
    final private String NAME_BIN_DECODE = "binary.decode";
    final private String NAME_BIN_ELEMENT_FROMXML = "binary.elementFromXml";
    final private String NAME_BIN_ELEMENT_TOXML = "binary.elementToXml";
    final private String NAME_BIN_ELEMENT_SETFROM = "binary.elementSetFrom";
    final private String NAME_BIN_COMPRESS = "binary.compress";
    final private String NAME_BIN_UNCOMPRESS = "binary.uncompress";

    
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
        this.addPluggableName(new PluggableName(NAME_BIN_AUTHRTP));
        this.addPluggableName(new PluggableName(NAME_BIN_RTPKEYDERIVATION));
        this.addPluggableName(new PluggableName(NAME_BIN_DIFFERENCE));
        this.addPluggableName(new PluggableName(NAME_BIN_XMLTOASN));
        this.addPluggableName(new PluggableName(NAME_BIN_ASNTOXML));
        this.addPluggableName(new PluggableName(NAME_BIN_ENDIAN));
        this.addPluggableName(new PluggableName(NAME_BIN_ENCODE));
        this.addPluggableName(new PluggableName(NAME_BIN_DECODE));
        this.addPluggableName(new PluggableName(NAME_BIN_ELEMENT_FROMXML));
        this.addPluggableName(new PluggableName(NAME_BIN_ELEMENT_TOXML));
        this.addPluggableName(new PluggableName(NAME_BIN_ELEMENT_SETFROM));
        this.addPluggableName(new PluggableName(NAME_BIN_COMPRESS));
        this.addPluggableName(new PluggableName(NAME_BIN_UNCOMPRESS));
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
                	Array array1 = Array.fromHexString(param_1.get(i).toString());
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
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
                	Parameter param1 = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
                	String value1 = param1.get(i).toString();
                	Parameter paramCharset = operands.get("value2");
                	String alphabet = null;
                    if (paramCharset != null)
                    {
                    	alphabet = paramCharset.get(i).toString();
                    }
                    Array array = Array.fromHexString(value1);
                    // for GSM SMS alphabet perform a 7bits encoding
                    if (alphabet != null && alphabet.contains("Default alphabet"))
                    {
                    	array = PluggableParameterOperatorBinary.decodeNumberBits(array, 7, true);
                    }
                    byte[] bytes = array.getBytes();
                    
                    String stringResult;
                    if (paramCharset != null)
                    {
                    	String charset = alphabet;
                    	int posCS = charset.indexOf('{');
                    	if (posCS >= 0)
                    	{
                    		charset = charset.substring(posCS + 1);
                    	}
                    	posCS = charset.lastIndexOf('}');
                    	if (posCS >= 0)
                    	{
                    		charset = charset.substring(0, posCS);
                    	}
                    	stringResult = new String(bytes, charset);
                    }
                    else
                    {
                    	stringResult = new String(bytes);
                    }
                    // for GSM SMS alphabet perform a 7bits encoding
                    if (alphabet != null && alphabet.contains("Default alphabet"))
                    {
                    	// remove twice the @ character
                    	int lastPos = stringResult.length() - 1;
                    	if (lastPos >= 0 && stringResult.charAt(lastPos) == '@')
                    	{
                    		stringResult = stringResult.substring(0, lastPos);
                    	}
                    	/*
                    	lastPos = stringResult.length() - 1;
                    	if (lastPos >= 0 && stringResult.charAt(lastPos) == '@')
                    	{
                    		stringResult = stringResult.substring(0, lastPos);
                    	}
                    	*/
                    }
                    result.add(stringResult);
                }
                else if (name.equals(NAME_BIN_SUBBINARY))
                {
                	Array arrayValue = Array.fromHexString(param_1.get(i).toString());
                	
                    int offset = Integer.valueOf(assertAndGetParameter(operands, "value2").get(i).toString()).intValue();
                    
                    Parameter paramLength = operands.get("value3");
                    int length;
                    if (paramLength != null)
                    {
                    	length = Integer.valueOf(paramLength.get(i).toString()).intValue();
                    }
                    else
                    {
                    	length = arrayValue.length - offset;
                    }
                  

                    result.add(Array.toHexString(arrayValue.subArray(offset, length)));
                }
                else if (name.equals(NAME_BIN_RANDOM))
                {
                	Array random = new RandomArray(new Integer(param_1.get(i).toString()));
                    result.add(Array.toHexString(random));
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
                	String val1 = param_1.get(i).toString();
                	InetAddress inetAddr = InetAddress.getByName(val1); 
                	byte[] bytesRes = inetAddr.getAddress();
                	String strRes =  Array.toHexString(new DefaultArray(bytesRes));
                	result.add(strRes);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_TOIP))
                {
                	String val1 = param_1.get(i).toString();
                	byte[] bytesVal1 = DefaultArray.fromHexString(val1).getBytes();
                	String strRes = Utils.toIPAddress(bytesVal1);
                	result.add(strRes);
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
                else if (name.equalsIgnoreCase(NAME_BIN_DIFFERENCE))
                {
                	String string1 = param_1.get(i).toString().replace(" ", "");
                    
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    String string2 = param_2.get(i).toString().replace(" ", "");
                    
                    GenericWrapper wrapper1 = new GenericWrapper(string1);
                    GenericWrapper wrapper2 = new GenericWrapper(string2);
                    String stringRes = calculateDifference(wrapper1, wrapper2);
                    string1 = (String) wrapper1.getObject();
                    string2 = (String) wrapper2.getObject();
                    
                    param_1.set(i, string1);
                    param_2.set(i, string2);
                    result.add(stringRes);
                    if (i == param_1.length() - 1)
                    {
	                    runner.getParameterPool().traceInfo("SET", "[value  ]", param_1.toString());
	                    runner.getParameterPool().traceInfo("SET", "[value2 ]", param_2.toString());
                    }
                }
                else if (name.equalsIgnoreCase(NAME_BIN_XMLTOASN))
                {
                	String xmlData = param_1.get(i).toString();
                	
                	Parameter param_2 = assertAndGetParameter(operands, "value2");
                    String className = param_2.get(i).toString().replace(" ", "");
                    
                    Parameter param_3 = assertAndGetParameter(operands, "value3");
                    String dictionary = param_3.get(i).toString().replace(" ", "");
                    
                    
                    Class thisClass = Class.forName(className);
                    int pos = className.lastIndexOf('.');
                    String packageName = "";
                    if (pos > 0)
                    {
                    	packageName = className.substring(0, pos + 1);
                    }
                    
                    Document doc = Utils.stringParseXML(xmlData, false);
                    Element element = doc.getRootElement();
                    
                    Object objASN;
                    objASN = thisClass.newInstance();
                    String resultPath = "";
                    ASNMessage message = new BN_ASNMessage(dictionary); 
                    XMLToASNParser.getInstance().parseFromXML(resultPath, message, objASN, element, packageName);

                    // Library binarynotes
                	IEncoder<java.lang.Object> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
                	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    encoderMAP.encode(objASN, outputStream);
                    byte[] bytesMAP = outputStream.toByteArray();
                    Array arrayMAP = new DefaultArray(bytesMAP);
                    
                    String ret =  Array.toHexString(arrayMAP);
                    result.add(ret);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ASNTOXML))
                {
                	String string1 = param_1.get(i).toString();
                	Array array = Array.fromHexString(string1);
                    
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    String className = param_2.get(i).toString().replace(" ", "");
                    
                    Parameter param_3 = assertAndGetParameter(operands, "value3");
                    String dictionary = param_3.get(i).toString().replace(" ", "");
                    
                    // Library binarynotes
                	IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
                    InputStream inputStream = new ByteArrayInputStream(array.getBytes());
                    Class<?> cl = Class.forName(className);
                    Object objASN = cl.newInstance();
                    objASN = decoder.decode(inputStream, cl);
                    
                    String ret = "";
                    String resultPath = "";
                    ASNMessage message = new BN_ASNMessage(dictionary); 
                    ret += ASNToXMLConverter.getInstance().toXML(resultPath, message, null, "value", objASN, null, ASNToXMLConverter.NUMBER_SPACE_TABULATION * 2);
                    result.add(ret);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ENDIAN))
                {
                	String string1 = param_1.get(i).toString();
                	Array array = Array.fromHexString(string1);
                	Array arrayResult = transformEndian(array);
                	result.add(Array.toHexString(arrayResult));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ENCODE))
                {
                	String string1 = param_1.get(i).toString();
                	Array array = Array.fromHexString(string1);
                	Parameter param_2 = assertAndGetParameter(operands, "value2");
                	int nbBits = Integer.valueOf(param_2.get(i).toString()).intValue();
                	Parameter paramEndian = operands.get("value3");               
                    boolean endian = true;
                    if (paramEndian != null)
                    {
                    	String strEndian = paramEndian.get(i).toString();
                    	endian = Utils.parseBoolean(strEndian, "value3");
                    }
                    Array arrayResult = encodeNumberBits(array, nbBits, endian);
                	result.add(Array.toHexString(arrayResult));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_DECODE))
                {
                	String string1 = param_1.get(i).toString();
                	Array array = Array.fromHexString(string1);
                	Parameter param_2 = assertAndGetParameter(operands, "value2");
                	int nbBits = Integer.valueOf(param_2.get(i).toString()).intValue();
                	Parameter paramEndian = operands.get("value3");
                	String strEndian = paramEndian.get(i).toString();
                    boolean endian = true;
                    if (paramEndian != null)
                    {
                    	endian = Utils.parseBoolean(strEndian, "value3");
                    }
                    Array arrayResult = decodeNumberBits(array, nbBits, endian);
                	result.add(Array.toHexString(arrayResult));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ELEMENT_FROMXML))
                {
                	String xmlData = param_1.get(i).toString();

                	Parameter param_2 = assertAndGetParameter(operands, "value2");
                	String dicoFile = param_2.get(i).toString();
                	Dictionary dico = Dictionary.getInstance(dicoFile.trim());

                    Document doc = Utils.stringParseXML(xmlData, false);
                    Element xmlRoot = doc.getRootElement();
                    
                    // parse the XML file using the dictionary
            	    ElementAbstract elemDico = dico.getElementFromXML(xmlRoot);
            	    ElementAbstract newElement = (ElementAbstract) elemDico.cloneAttribute();
        	        newElement.parseFromXML(xmlRoot, dico, elemDico, false);
        	                    	    
            	    // encode the element
                	result.add(Array.toHexString(newElement.encodeToArray(dico)));
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ELEMENT_TOXML))
                {
                	String string1 = param_1.get(i).toString();                	
                                        
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    String dicoFile = param_2.get(i).toString();
            	    Dictionary dico = Dictionary.getInstance(dicoFile.trim());

                    Parameter param_3 = operands.get("value3");
                    String elementName = null;
                    if (param_3 != null)
                    {
                    	elementName = param_3.get(i).toString();
                    }
                    ElementAbstract newElement = elementDecodeToXml(string1, dico, elementName);
                                        
                    // get the XML data                  
                    String xmlData = newElement.toXml(0);
                	result.add(xmlData);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_ELEMENT_SETFROM))
                {
                	String string1 = param_1.get(i).toString();                	
                   
                    Parameter param_2 = assertAndGetParameter(operands, "value2");
                    String path = "element." + param_2.get(i).toString();
                    String[] params = Utils.splitNoRegex(path, ".");

                    Parameter param_3 = assertAndGetParameter(operands, "value3");
                    String dicoFile = param_3.get(i).toString();
                    Dictionary dico = Dictionary.getInstance(dicoFile.trim());
                             
                    Parameter param_4 = operands.get("value4");
                    String elementName = null;
                    if (param_4 != null)
                    {
                    	elementName = param_4.get(i).toString();
                    }
                    ElementAbstract newElement = elementDecodeToXml(string1, dico, elementName);
                                        
                    // get the data from element using path        
                    newElement.getParameter(result, params, path, 0, dico);
                }
                else if (name.equalsIgnoreCase(NAME_BIN_COMPRESS))
                {
                    String string1 = param_1.get(i).toString();                 
                    String algo    = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2").get(i).toString();
                
                    if (algo.equalsIgnoreCase("gzip"))
                    {
                        byte[] data = DefaultArray.fromHexString(string1).getBytes() ;
                        
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        GZIPOutputStream      gzos = new GZIPOutputStream(baos);
                                  
                        gzos.write(data);
                        gzos.close();

                        DefaultArray array = new DefaultArray(baos.toByteArray()) ;
                        
                        result.add( DefaultArray.toHexString(array) ) ;
                    }
                
                }
                else if (name.equalsIgnoreCase(NAME_BIN_UNCOMPRESS))
                {
                    String string1 = param_1.get(i).toString();                 
                    String algo    = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2").get(i).toString();
                
                    if (algo.equalsIgnoreCase("gzip"))
                    {
                        byte[] data_in = DefaultArray.fromHexString(string1).getBytes() ;
                        
                        GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data_in, 0, data_in.length));
                        
                        byte[] data_out = new byte[data_in.length];
                        
                        ByteArrayOutputStream baos =  new ByteArrayOutputStream();
                        
                        int len;
                        
                        while( (len=gzis.read(data_out, 0, data_out.length)) != -1 )
                        {
                            baos.write(data_out, 0, len);
                        }
                        
                        result.add( DefaultArray.toHexString(new DefaultArray(baos.toByteArray())) ) ;
                    }
                }
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

    /**
     * Calculate the difference between 2 hexadecimal strings
     * Use wrapper because String class is immutable
     * 
     * @param wrapperN : the wrapper around the string to calculate the difference with
     * @return : the resulting string
     */
    public static String calculateDifference(GenericWrapper wrapper1, GenericWrapper wrapper2)
    {
    	String str1 = (String) wrapper1.getObject();
    	String str2 = (String) wrapper2.getObject();
    	
	    String stringRes = "";
	    int j = 0;
	    while (j < str1.length() - 1 && j < str2.length() - 1)
	    {
	    	String sub1 = str1.substring(j, j + 2);
	    	int int1 = Integer.decode("#" + sub1);
	    	
	    	String sub2 = str2.substring(j, j + 2);
	    	int int2 = Integer.decode("#" + sub2);
	    	
	    	byte byteRes = (byte) (Math.abs(int2 - int1) & 0xff);
	    	if (byteRes != 0)
	    	{
	        	BenefPos benefPos = new BenefPos(); 
	        	benefPos.pos = j;
	    		calculateBeneficeToShift(str1, str2, benefPos);
	    		int bene1 = benefPos.benef;
	    		int pos1 = benefPos.pos;
	    		benefPos.pos = j;;
	    		calculateBeneficeToShift(str2, str1, benefPos);
	    		int bene2 = benefPos.benef;
	    		int pos2 = benefPos.pos;
	    		//if (bene1 > Integer.MIN_VALUE && bene1 > bene2)
	    		if (bene1 > 0 && bene1 > bene2)
	    		{
	        		str1 = insertStringAt(str1, j, " ", pos1 - j);
	        		stringRes = insertStringAtEnd(stringRes, "X", pos1 - j);
	        		j = pos1;
	    		}
	    		//else if (bene2 > Integer.MIN_VALUE && bene2 > bene1)
	    		else if (bene2 > 0 && bene2 > bene1)
	    		{
	        		str2 = insertStringAt(str2, j, " ", pos2 - j);
	        		stringRes = insertStringAtEnd(stringRes, "X", pos2 - j);
	        		j = pos2;
	    		}
	    		// the bytes are different
	        	else
	        	{
	        		stringRes = insertByteAtEnd(stringRes, byteRes);
	        		j = j + 2;
	        	}
	    		
	    	}
	    	// the bytes are equals
	    	else
	    	{
	    		stringRes = insertStringAtEnd(stringRes, " ", 2);;
	    		j = j + 2;
	    	}
	    	
	    }
	    
	    // normalize the length of both strings
	    int l1 = str1.length();
	    int l2 = str2.length();
	    if (l1 > l2)
	    {
	    	str2 = insertStringAtEnd(str2, " ", l1-l2);
	    	stringRes = insertStringAtEnd(stringRes, "X", l1-l2);
	    }
	    if (l2 > l1)
	    {
	    	str1 = insertStringAtEnd(str1, " ", l2-l1);
	    	stringRes = insertStringAtEnd(stringRes, "X", l2-l1);
	    }
	   
	    wrapper1.setObject(str1);
	    wrapper2.setObject(str2);
	    
	    return stringRes;
    }
	    	
    /**
     * Calculate the maximum benefice to shift at the pos position in order to match the string2
     * and return a new resulting benefice; pos integer contains the new position after shifting
     * IMPROVMENT but NOT TESTED !
     * 
     * @param stringN : the string to replace
     * @param pos : the position to insert into
     * @param pattern : the pattern string to insert
     * @return : the resulting string
     */
    private static int calculateMaximumBenefice(String string1, String string2, Integer pos)
    {
    	List<Integer> listPos = new ArrayList<Integer>();
    	List<Integer> listBenef = new ArrayList<Integer>();
    	Integer newPos = pos;
    	Integer newBenef = 0;
    	while (newBenef != Integer.MIN_VALUE)
    	{
    		// newBenef = calculateBeneficeToShift(string1, string2, newPos);
    		if (newBenef != Integer.MIN_VALUE)
    		{
    			listPos.add(newPos);
    			listBenef.add(newBenef);
    		}
    		newPos = newPos + 2;
    	}
    	
    	if (!listBenef.isEmpty())
    	{
    		int indexMax = 0;
    		int benefMax = 0; 
    		for (int k = 0; k < listBenef.size(); k++)
    		{
    			int benef = listBenef.get(k); 
    			if (benef > benefMax)
    			{
    				indexMax = k;
    				benefMax = benef;
    			}
    		}
    		pos = indexMax;
    		return benefMax;
    	}
    	    	
    	return Integer.MIN_VALUE;
    }
    
    /**
     * Calculate the benefice to shift the string1 at the pos position in order to match the string2
     * and return a new resulting benefice; pos integer contains the new position after shifting
     * 
     * @param stringN : the string to replace
     * @param pos : the position to insert into
     * @param pattern : the pattern string to insert
     * @return : the resulting string
     */
    private static void calculateBeneficeToShift(String string1, String string2, BenefPos benefPos)
    {    
    	int pos = benefPos.pos;
    	String sub = string1.substring(pos, pos + 2);
    	int posFind = string2.indexOf(sub, pos);
    	if (posFind > 0  && posFind % 2 == 0) 
    	{
    		int ind1 = pos + 2;
    		int ind2 = posFind + 2; 
    		while (ind1 < string1.length() && ind2 < string2.length())
    		{
    			String sub1 = string1.substring(ind1, ind1 + 2);
    			String sub2 = string2.substring(ind2, ind2 + 2);
    			if (!sub1.equals(sub2))
    			{
    				break;
    			}
    			ind1 = ind1 + 2;
    			ind2 = ind2 + 2;
    		}
    		benefPos.pos = posFind;
    		int benef = ind2 - posFind - (posFind - pos);
    		benefPos.benef = benef;
    	}
    	else
    	{
    		benefPos.pos = Integer.MIN_VALUE;
    	    benefPos.benef = Integer.MIN_VALUE;
    	}
    }
    
    /**
     * Insert string pattern at the pos position into a given string and return a new resulting string
     * 
     * @param string : the string to replace
     * @param pos : the position to insert into
     * @param pattern : the pattern string to insert
     * @return : the resulting string
     */
    private static String insertStringAt(String string, int pos, String pattern, int number)
    {                     
		String res = string.substring(0, pos);
		for (int i = 1; i <= number; i++)
		{
			res += pattern;
		}
		res += string.substring(pos);
		return res;
    }

    /**
     * Insert a byte at the end into a given string and return a new resulting string
     * 
     * @param string : the string to replace
     * @param b : the byte to insert into
     * @return : the resulting string
     */
    private static String insertByteAtEnd(String string,  byte b)
    {
    	String strByte = String.format("%02X", b);
    	if (strByte.length() == 1)
    	{
    		string += "0" + strByte;
    	}
    	else
    	{
    		string += strByte;
    	}
		return string;
    }

    /**
     * Insert string pattern at the end into a given string and return a new resulting string
     * 
     * @param string : the string to replacer
     * @param pattern : the pattern string to insert
     * @param nb : the number of character to insert into
     * @return : the resulting string
     */
    private static String insertStringAtEnd(String string, String pattern, int number)
    {
		for (int i = 1; i <= number; i ++)
		{
			string += pattern;
		}
		return string;
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
    		throw new ParameterException("concatened master key and master salt length is not equal to 30 bytes : " + new String(decodeInlineByteTab));
    	
    	byte[] masterKey = new byte[16];
    	byte[] masterSalt = new byte[14];
    	
    	for (int i = 0; i < 16; i++)
    		masterKey[i] = decodeInlineByteTab[i];
    	for (int i = 0; i < 14; i++)
    		masterSalt[i] = decodeInlineByteTab[i + 16];
    	ret[0] = new String(masterKey);
    	ret[1] = new String(masterSalt);
    	
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

    private static Array transformEndian(Array array)
    {
		Array arrayEndian = new DefaultArray(array.length);
		for (int indexByte = 0; indexByte < array.length; indexByte++)
		{
			for (int indexBit = 0; indexBit < 8; indexBit++)
			{
				int b = array.getBit(indexByte * 8 + indexBit);
				arrayEndian.setBit(indexByte * 8 + 7 - indexBit, b);
			}
		}
		return arrayEndian;
    }

    private static Array encodeBinary(Array array, int nbBits)
    {
    	//int nbBitsResult = (array.length * nbBits - 1 ) / 8 + 1;
    	int nbBitsResult = array.length * nbBits / 8 + 1;
		Array arrayResult = new DefaultArray(nbBitsResult);
		for (int indexBit = 0; indexBit < array.length * 8; indexBit++)
		{
				int b = array.getBit(indexBit);
				int indexBitNew = (indexBit / 8) * nbBits + indexBit % 8;
				arrayResult.setBit(indexBitNew, b);
		}
		return arrayResult;
    }

    protected static Array encodeNumberBits(Array array, int nbBits, boolean endian)
    {
	    if (endian)
	    {
	    	array = transformEndian(array);
	    }
		Array arrayResult = encodeBinary(array, nbBits);
		if (endian)
		{
			arrayResult = transformEndian(arrayResult);
		}
		return arrayResult;
    }
    
    private static Array decodeBinary(Array array, int nbBits)
    {
    	int nbBitsResult = array.length * 8 / nbBits + 1;
		Array arrayResult = new DefaultArray(nbBitsResult);
		for (int indexBit = 0; indexBit < array.length * 8; indexBit++)
		{
				int b = array.getBit(indexBit);
				int indexBitNew = (indexBit / nbBits) * 8 + indexBit % nbBits;
				arrayResult.setBit(indexBitNew, b);
		}
		return arrayResult;
    }

    protected static Array decodeNumberBits(Array array, int nbBits, boolean endian)
    {
	    if (endian)
	    {
	    	array = transformEndian(array);
	    }
		Array arrayResult = decodeBinary(array, nbBits);
		if (endian)
		{
			arrayResult = transformEndian(arrayResult);
		}
		return arrayResult;
    }

    public static ElementAbstract elementDecodeToXml(String data, Dictionary dico, String elementLabel) throws Exception
    {    
    	Array array = Array.fromHexString(data);
	    
	    // get the element from the dictionary and clone it
    	ElementAbstract elemDico = null;
    	if (elementLabel != null)
    	{
    		elemDico = dico.getElementByLabel(elementLabel);
    	}
    	else
    	{
    		elemDico = dico.getElementForMessage();
    	}
    	
	    ElementAbstract newElement = ElementAbstract.buildFactory(elemDico.getCoding(), null); 
	    if (elemDico != null)
		{
	    	newElement.copyToClone(elemDico);
		}
	    // decode the elements list
	    newElement.decodeFromArray(array, dico);
	    return newElement;
    }
}

