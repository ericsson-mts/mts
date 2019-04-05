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

package com.devoteam.srit.xmlloader.sigtran.fvo;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;

import org.dom4j.Element;

/**
 *
 * @author Julien Brisseau
 */
public class FvoField {

    private Msg _msg;

    private String _name = null;
    private String _format = null;
    private String _value = null;
    
    private int _lengthBit = 0;

    private boolean _littleEndian = false;

    // valid values are
    // - integer (lsb ---> msb, little endian) (default)
    static public final String formatInteger = "integer";
    // - string
    static public final String formatString = "string";
    // - binary
    static public final String formatBinary = "binary";
    // - digit
    static public final String formatDigit = "digit";

    public FvoField(Msg msg) {
        _msg = msg;
    }

    public FvoField(Msg msg, FvoField other) {
        this(msg);
        _name = other._name;
        _lengthBit = other._lengthBit;
        _value = other._value;
        _format = other._format;
        _littleEndian = other._littleEndian;
    }

    private static int toLittlenEndianOffsetBit(int offsetBit){
        // "symetric" in current octet
        int octet = offsetBit / 8;
        int bit = offsetBit % 8;

        offsetBit = (octet * 8) + (7 - bit) ;

        return offsetBit;
    }

    private int toLittleEndianIndexBit(int offsetBit, int indexBit, int lengthBit, boolean littleEndian){
        if(littleEndian){
            int litEndianEndBit = toLittlenEndianOffsetBit(offsetBit + lengthBit - 1);
            int litEndianBitIndex = litEndianEndBit % 8;
            int litEndianOctIndex = litEndianEndBit / 8;

            for(int i=offsetBit; i < offsetBit + lengthBit; i++){

                if(litEndianBitIndex == 8){
                    litEndianBitIndex = 0;
                    litEndianOctIndex--;
                }

                if(i == indexBit + offsetBit){
                    return litEndianOctIndex * 8 + litEndianBitIndex;
                }
                else{
                    litEndianBitIndex++;
                }
            }
            return -1;
        }
        else{
            return offsetBit + indexBit;
        }
    }

    public void encode(Array target, int offsetBit) throws Exception {
        // set default format
        if(null == _format){
            _format = FvoField.formatInteger;
        }

        if(FvoField.formatInteger.equalsIgnoreCase(_format)){
            int value = Integer.decode(_value);

            int mask = 0x01;
            for(int i=0; i<_lengthBit; i++){
                int bit;

                if((value & mask) == mask){
                    bit = 1;
                }
                else{
                    bit = 0;
                }

                target.setBit(toLittleEndianIndexBit(offsetBit, _lengthBit - i - 1, _lengthBit, _littleEndian), bit);

                mask = mask << 1;
            }
        }
        else if(FvoField.formatString.equalsIgnoreCase(_format)) {
            Array value = new DefaultArray(_value.getBytes());
            int len = value.length * 8;
            for (int i = 0; i < len; i++) {
                target.setBit(toLittleEndianIndexBit(offsetBit, _lengthBit - i - 1, _lengthBit, _littleEndian), value.getBit(i));
            }
            _lengthBit = len;
        }
        else if(FvoField.formatBinary.equalsIgnoreCase(_format)) {
            Array value = Array.fromHexString(_value);
            int len = value.length * 8;
            if (_littleEndian)
        	{
            	// perform littleEndian converting
            	for (int i = 0; i < len; i++) {
            		target.setBit(toLittleEndianIndexBit(offsetBit, _lengthBit - i - 1, _lengthBit, _littleEndian), value.getBit(i));
            	}
        	}
            else
            {
            	// copy the binary as identical
            	for (int i = 0; i < len; i = i + 8) {
            		target.setBits(i + offsetBit, 8, value.getBits(i, 8));
            	}
        	}

            _lengthBit = len;
        }
        else if(FvoField.formatDigit.equalsIgnoreCase(_format)) {
            Array value = convertDigitToBinary(_value);
            int len = value.length * 8;
            if (_littleEndian)
        	{
            	// perform littleEndian converting
            	for (int i = 0; i < len; i++) {
            		target.setBit(toLittleEndianIndexBit(offsetBit, _lengthBit - i - 1, _lengthBit, _littleEndian), value.getBit(i));
            	}
        	}
            else
            {
            	// copy the binary as identical
            	for (int i = 0; i < len; i = i + 8) {
            		target.setBits(i + offsetBit, 8, value.getBits(i, 8));
            	}
        	}

            _lengthBit = len;
        }
        
    }
    
    public void setFormat(String format) {
        _format = format;
    }

    public String getFormat() {
        return _format;
    }

    public int getLengthBit() {
    	if(formatBinary.equalsIgnoreCase(_format)){
    		if (_value != null)
    			return (_value.length() / 2) * 8;
    		else
    			return 0;
        }
        else if(formatDigit.equalsIgnoreCase(_format)){
    		if (_value != null)
    			return ((_value.length() + 1) / 2) * 8;
    		else
    			return 0;
        }
        else if(formatString.equalsIgnoreCase(_format)){
    		if (_value != null)
    			return _value.length() * 8;
    		else
    			return 0;        	
        }
    	else
    	{
    		return _lengthBit;
    	}
    }

    public void setLength(int bytes) {
        setLengthBit(bytes, 0);
    }

    public void setLengthBit(int bytes, int bits) {
        if(formatBinary.equalsIgnoreCase(_format)){
            throw new RuntimeException("can not set length on binary format field");
        }
        else if(formatBinary.equalsIgnoreCase(_format)){
            throw new RuntimeException("can not set length on binary format field");
        }        
        else if(formatString.equalsIgnoreCase(_format)){
            throw new RuntimeException("can not set length on string format field");
        }
        else{
            this._lengthBit = bytes*8 + bits;
        }
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public void setValue(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public boolean isLittleEndian(){
        return _littleEndian;
    }

    public void setLittleEndian(boolean value){
        _littleEndian = value;
    }

    /**
     * Get a parameter from the field
     *
     * @param path		: The path of the parameter requested
     * @return			: The parameter requested
     * @throws Exception
     */
    public Parameter getParameter(String path) throws Exception {

        Parameter parameter = new Parameter();
        String[] params = Utils.splitPath(path);

        if (params[0].equalsIgnoreCase("name")) {
            parameter.add(this._name);
        }
        else if (params[0].equalsIgnoreCase("format")) {
            parameter.add(this._format);
        }
        else if (params[0].equalsIgnoreCase("value")) {
            parameter.add(this._value);
        }
        else if (params[0].equalsIgnoreCase("lengthBit")) {
            parameter.add(this._lengthBit);
        }        
        else if (params[0].equalsIgnoreCase("littleEndian")) {
            parameter.add(this._littleEndian);
        }       
        else {
            parameter = null;
        }

        return parameter;
    }
    
    public void parseElement(Element root) throws Exception {
        String name = root.attributeValue("name");
        if(name == null)
        {
        	//throw new ExecutionException("A name is required for fvo fields\n" + root.asXML());
        }
        else
        {
            _name = name;
        }
        String value = root.attributeValue("value");
        // test for errors (value is mandatory attribute)
        if (value == null) 
        {
            throw new ExecutionException("A value is required for fvo fields\n" + root.asXML());
        }
        else
        {
        	this._value = value;
        }

        String format = root.attributeValue("format");
        // field info should already have been completed from dicionary, using constructor that clones
        // test for errors (value is mandatory attribute)
        if (format != null &&
           !format.equalsIgnoreCase(FvoField.formatDigit) &&
           !format.equalsIgnoreCase(FvoField.formatBinary) &&
           !format.equalsIgnoreCase(FvoField.formatInteger) &&
           !format.equalsIgnoreCase(FvoField.formatString)) {
            throw new ExecutionException("The format of a FvoField must be one of " +
                    FvoField.formatBinary + ", " +
                    FvoField.formatDigit + ", " +
                    FvoField.formatInteger + ", " +
                    FvoField.formatString + ". Error in XML :\n" + root.asXML());
        }
        else{
            this._format = format;
        }

        String length = root.attributeValue("length");
        String lengthBit = root.attributeValue("lengthBit");
        if(null != length || null != lengthBit){
            _lengthBit = 0;
            if(null != length){
                _lengthBit += Integer.decode(length) * 8;
            }

            if(null != lengthBit){
                _lengthBit += Integer.decode(lengthBit);
            }
        }
        
        String littleEndian = root.attributeValue("littleEndian");
        if (littleEndian != null) {
            setLittleEndian(Utils.parseBoolean(littleEndian, "littleEndian"));
        }        
    }

    public void parseArray(Array array, int offsetBit) throws ExecutionException, Exception {
        // if the length is unknown then the length will be all the remaining bits
        if(_lengthBit <= 0){
            _lengthBit = array.length * 8 - offsetBit;
        }

        // override default format
        if (_format == null) {
            _format = "integer";
        }

        // parse the value depending on the type integer // string // binary
        if(FvoField.formatInteger.equalsIgnoreCase(_format)){
            Integer32Array buff = new Integer32Array(0);
            for (int i = 0; i < _lengthBit; i++){
                buff.setBit((buff.length * 8) - _lengthBit + i, array.getBit(toLittleEndianIndexBit(offsetBit, i, _lengthBit, _littleEndian)));
            }

            _value = String.valueOf(buff.getValue());
        }
        else if(FvoField.formatBinary.equalsIgnoreCase(_format)){
        	_lengthBit = array.length * 8 - offsetBit;
            // offset and length should be multiple of 8
            DefaultArray buff = new DefaultArray(_lengthBit/8);
            for (int i = 0; i < _lengthBit; i++){
                buff.setBit((buff.length * 8) - _lengthBit + i, array.getBit(toLittleEndianIndexBit(offsetBit, i, _lengthBit, _littleEndian)));
            }
            _value = Array.toHexString(buff);
        }
        else if(FvoField.formatDigit.equalsIgnoreCase(_format)){
        	_lengthBit = array.length * 8 - offsetBit;
            // offset and length should be multiple of 8
            DefaultArray buff = new DefaultArray(_lengthBit/8);
            for (int i = 0; i < _lengthBit; i++){
                buff.setBit((buff.length * 8) - _lengthBit + i, array.getBit(toLittleEndianIndexBit(offsetBit, i, _lengthBit, _littleEndian)));
            }
            _value = convertBinaryToDigit(buff);
            //_value = Array.toHexString(buff);
        }
        else if(FvoField.formatString.equalsIgnoreCase(_format)){
        	_lengthBit = array.length * 8 - offsetBit;
            // offset and length should be multiple of 8
            DefaultArray buff = new DefaultArray(_lengthBit/8);
            for (int i = 0; i < _lengthBit; i++){
                buff.setBit((buff.length * 8) - _lengthBit + i, array.getBit(toLittleEndianIndexBit(offsetBit, i, _lengthBit, _littleEndian)));
            }
            _value = new String(buff.getBytes());
        }
    }

    @Override
    public String toString() {
        String str = "\n      <field ";
        if (_name != null) {
            str += " name=\"" + _name + "\"";
        }
        if (_value != null) {
            str += " value=\"" + _value + "\"";
        }
        if (_lengthBit % 8 != 0) {
            str += " lengthBit=\"" + _lengthBit + "\"";
        }
        else {
            str += " length=\"" + (_lengthBit / 8) + "\"";
        }        
        if (_format != null) {
            if (!_format.equalsIgnoreCase("integer")) {
                str += " format=\"" + _format + "\"";
            }
        }
        if (_littleEndian) {
            str += " littleEndian=\"" + _littleEndian + "\"";
        }        
        str += " />";
        return str;
    }
    
    private String convertBinaryToDigit(Array array) 
    {
		String string = Array.toHexString(array);
		byte[] bytes = string.getBytes();  
		for (int i = 0; i < bytes.length; i+=2)
		{
			byte temp = bytes[i];
			bytes[i] = bytes[i + 1];
			bytes[i + 1] = temp;
		}
		String resultString = new String(bytes);
		if (resultString.endsWith("f"))
		{
			resultString = resultString.substring(0, resultString.length() - 1);
		}
		return resultString;
    }

    private Array convertDigitToBinary(String string) 
    {
    	if (string.length() % 2 != 0)
    	{
    		string = string + 'f';
    	}
		//Array array = Array.fromHexString(string);
		byte[] bytes = string.getBytes();  
		for (int i = 0; i < bytes.length; i+=2)
		{
			byte temp = bytes[i];
			bytes[i] = bytes[i + 1];
			bytes[i + 1] = temp;
		}
		String resultString = new String(bytes);
		Array resultArray = Array.fromHexString(resultString);
		return resultArray;
    }

}
