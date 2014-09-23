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

package com.devoteam.srit.xmlloader.sigtran.tlv;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;
import com.devoteam.srit.xmlloader.sigtran.StackSigtran;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoDictionary;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;

import gp.utils.arrays.*;

/**
 *
 * @author Julien Brisseau
 */
public class TlvField {

    private Msg _msg;
    private TlvDictionary _dictionary;
    private String _name = null;
    private int _length = -1;
    private int _lengthBit = -1;
    private String _value = null;
    private String _format = "integer";

    /**
     * Constructor
     * @param msg		: Sigtran message
     */
    public TlvField(Msg msg, TlvDictionary dictionary) {
        this._msg = msg;
        this._dictionary = dictionary;
    }

    /**
     * Initialize the tlvParameter from the dicoField informations
     * @param dicoField		: informations in the dictionnary to encode the field
     */
    public void init(TlvField dicoField) {
        _name = dicoField._name;
        _length = dicoField._length;
        _lengthBit = dicoField._lengthBit;
        checkLengths();
        if (null != dicoField._format) {
            _format = dicoField._format;
        }
    }

    private void checkLengths(){
        if(_length != -1 || _lengthBit != -1){
            if(_length == -1){
                _length = 0;
            }
            if(_lengthBit == -1){
                _lengthBit = 0;
            }
        }
    }

    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        if (null != format) {
            _format = format;
        }
        else {
            _format = "integer";
        }
    }

    public int getLength() throws Exception {
        if ("string".equalsIgnoreCase(_format)) {
            if(null != getValue()){
                return getValue().length();
            }
            else{
                return 0;
            }
        }
        else if ("binary".equalsIgnoreCase(_format)) {
            if(null != getValue()){
                return getValue().length() / 2;
            }
            else{
                return 0;
            }
        }
        else if ("fvo".equalsIgnoreCase(_format)) {
            if(null != _msg){
                return _msg.getFvoMessage().getLength();
            }
            else{
                return 0;
            }
        }
        else {
            return _length;
        }
    }

    public void setLength(int length) throws Exception {
        if ("string".equalsIgnoreCase(_format)) {
            throw new RuntimeException("can not set length on string format field");
        }
        else if ("fvo".equalsIgnoreCase(_format)) {
            throw new RuntimeException("can not set length on fvo format field");
        }
        else {
            _length = length;

            checkLengths();
        }
    }

    public int getLengthBit() {
        if ("string".equalsIgnoreCase(_format)) {
            // will be multiple of 8
            return 0;
        }
        else if ("fvo".equalsIgnoreCase(_format)) {
            // will be multiple of 8
            return 0;
        }
        else if ("binary".equalsIgnoreCase(_format)) {
            // will be multiple of 8
            return 0;
        }
        else {
            return _lengthBit;
        }
    }

    public void setLengthBit(int lengthBit) throws Exception {
        if ("string".equalsIgnoreCase(_format)) {
            throw new ExecutionException("Can not set length for string field : \"" + getName() + "\"");
        }
        else if ("fvo".equalsIgnoreCase(_format)) {
            throw new ExecutionException("Can not set length for fvo field : \"" + getName() + "\"");
        }
        else if ("binary".equalsIgnoreCase(_format)) {
            throw new ExecutionException("Can not set length for binary field : \"" + getName() + "\"");
        }
        else {
            _lengthBit = lengthBit;

            checkLengths();
        }
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        this._value = value;
    }

    public Msg getMsg() {
        return _msg;
    }

    public void setMsg(MsgSigtran msg) {
        this._msg = msg;
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
            parameter.add(this.getName());
        }
        else if (params[0].equalsIgnoreCase("length")) {
            parameter.add(this.getLength());
        }
        else if (params[0].equalsIgnoreCase("format")) {
            parameter.add(this.getFormat());
        }
        else if (params[0].equalsIgnoreCase("value")) {
            parameter.add(this.getValue());
        }
        else if (params[0].equalsIgnoreCase("lengthBit")) {
            parameter.add(this.getLengthBit());
        }
        else {
            parameter = null;
        }

        return parameter;
    }

    /**
     * Encode the TlvField and add the encoding field at the end of the supArray
     *
     * @param supArray : Array which contains the begin of the encoding message
     * @param bitsNonUtilises : number of bits at the end of supArray which are
     * 							not used to encode the message
     * @return  : 	number of bits which are not used in supArray to encode the message
     * 				at the end
     * @throws Exception
     */
    public Array encode(Array target, int offsetBit) throws Exception {
        // fvo
        if (_format.equalsIgnoreCase("fvo")) {
            Array array = _msg.getFvoMessage().encode();
            for(int i=0; i<array.length; i++){
                target.set(offsetBit/8 + i, array.get(i));
            }
        }
        else if (_format.equalsIgnoreCase("spare")) {
            int len = _length * 8 + _lengthBit;
            for (int i = 0; i < len; i++) {
                target.setBit(offsetBit + i, 0);
            }
        }
        else if (_format.equalsIgnoreCase("integer")) {
            int value = -1;
            try {
                // try to parse value as an integer (use long for "unsigned")
                value = (int) Long.parseLong(getValue());
            }
            catch (Exception e) {
                // if there is an error, then maybe value was an enumeration element
                try {
                    value = (int) Long.parseLong(_dictionary.getEnumerationCodeFromName(_name, getValue()));
                }
                catch (Exception ee) {
                    // still an error ?! -> invalid value
                    throw new ParsingException("invalid value in " + this + "\n value is" + getValue(), ee);
                }
            }

            int len = _length * 8 + _lengthBit;
            for (int i = 0; i < len; i++) {
                int mask = 0x01 << (len - i - 1);
                int bit = 0;
                if ((mask & value) == mask) {
                    bit = 1;
                }
                target.setBit(offsetBit + i, bit);
            }
        }
        else if (_format.equalsIgnoreCase("string")) {
            Array value = new DefaultArray(getValue().getBytes());
            int len = value.length * 8;
            for (int i = 0; i < len; i++) {
                target.setBit(offsetBit + i, value.getBit(i));
            }
        }
        else if (_format.equalsIgnoreCase("binary")) {
            target = Array.fromHexString(getValue());
        }
        return target;
    }

    /**
     * Parse a field from a XML file
     *
     * @param 	root		: path of the field in the XML scenario file
     * @throws Exception
     */
    public void parseElement(Element root) throws Exception {
        //Capture the different attribute from the XML file
        String name = root.attributeValue("name");
        String length = root.attributeValue("length");
        String lengthBit = root.attributeValue("lengthBit");
        String value = root.attributeValue("value");
        String format = root.attributeValue("format");

        // decode enumeration to value
        if(_dictionary.getEnumerationCodeFromName(name, value) != null){
            value = _dictionary.getEnumerationCodeFromName(name, value);
            value = "" + (int) Long.parseLong(value);
        }

        if(null != value){
            _value = value;
        }

        if(null != format){
            _format = format;
        }

        if(null != name){
            _name = name;
        }

        // check the type is valid
        if(!("fvo".equalsIgnoreCase(_format) ||
                "integer".equalsIgnoreCase(_format) ||
                "spare".equalsIgnoreCase(_format) ||
                "string".equalsIgnoreCase(_format) || 
                "binary".equalsIgnoreCase(_format))){
                throw new ExecutionException("UA layer : The format of a field must be set integer/string/binary/fvo\n" + root.asXML());
        }

        // if the type is fvo, then search if the fvo message is defined in message
        if ("fvo".equalsIgnoreCase(_format) && null == _msg.getFvoMessage()) {
            throw new ExecutionException("<SS7> tag should be defined in message because there is a 'fvo' type field\n" + root.asXML());
        }

        // override the length if defined in element
        if(null != lengthBit || null != length){
            _length = 0;
            _lengthBit = 0;
            
            if(null != length){
                _length = Integer.decode(length);
            }

            if(null != lengthBit){
                _length += Integer.decode(lengthBit) / 8;
                _lengthBit = Integer.decode(lengthBit) % 8;
            }
        }
   }

    /**
     * Decode a TlvField from an array and add this field at the end of parameter
     * 	listField
     *
     * @param array				: array whitch contains the encoding message
     * @param offsetBit	: length in bit of the array already decoded,
     * 							  begin in the array of the TlvField
     * @param dictionaryField	: structure of the field found in the Tlv dictionnary
     * @param parameter			: parameter which contains this field
     * @throws ExecutionException
     */
    public void parseArray(Array array, int offsetBit) throws ExecutionException, Exception {
        // compute the length of the field to parse
        int fieldLengthBit = _length * 8 + _lengthBit;

        // if the length is lower than zero or greater than the total size
        // then set it to the end of remaining data
        if(fieldLengthBit <= 0 || fieldLengthBit > array.length * 8 - offsetBit){
            fieldLengthBit = array.length * 8 - offsetBit;
        }

        // override default format
        if (_format == null) {
            _format = "integer";
        }

        if (_format.equalsIgnoreCase("integer")) {

            // copy bits to and integer32 array in order to get value (max 32bits)
            Integer32Array value = new Integer32Array(0);
            for(int i=0; i< fieldLengthBit; i++){
                value.setBit(31 - i, array.getBit(offsetBit + fieldLengthBit - i - 1));
            }

            _value = String.valueOf((long) value.getValue() & 0xffffffff);

            // if this is the right field, then set FvoProtocol in message
            if ("SI".equalsIgnoreCase(getName())) {
                _msg.setFvoProtocol(Integer.parseInt(_value));
            }

            // try to find the name of the value if it belongs to an enumeration
            if (_name != null) {
                String valueName = _dictionary.getEnumerationNameFromCode(_name, _value);
                if(valueName != null){
                    _value = valueName;
                }
            }
        }
        else if (_format.equalsIgnoreCase("spare")) {
            _value="N/A";
        }
        else if (_format.equalsIgnoreCase("fvo")) {
            // some error, we assume it is a multiple of 8
            if(fieldLengthBit % 8 != 0){
                throw new ParsingException("fvo field length (in bits) should be a multiple of 8");
            }

            // some error, we assume it is a multiple of 8
            if(offsetBit % 8 != 0){
                throw new ParsingException("fvo field parsing offset (in bits) should be a multiple of 8");
            }

            int fieldLength = fieldLengthBit / 8;
            int offset = offsetBit / 8;

            // get dictionnary
            String dictionaryName;
            switch(_msg.getFvoProtocol()){
                case 3:
                    dictionaryName = "sccp.xml";
                    break;
                case 13:
                    dictionaryName = "bicc.xml";
                    break;
                default:
                    dictionaryName = "bicc.xml";
                    //TODO throw new ParsingException("unknown FVO type : " + _msg.getFvoProtocol());
            }

            // parse the fvo message and put it into the sigtran message
            FvoDictionary fvoDictionary = StackSigtran.instance().getFvoDictionnary(dictionaryName);
            _msg.setFvoMessage(new FvoMessage(_msg, fvoDictionary));
            _msg.getFvoMessage().parseArray(array.subArray(offset, fieldLength));

            _name = _format;
            _value = null;
        }
        else if (_format.equalsIgnoreCase("string")) {
            // some error, we assume it is a multiple of 8
            if(fieldLengthBit % 8 != 0){
                throw new ParsingException("string field length (in bits) should be a multiple of 8");
            }
            
            // some error, we assume it is a multiple of 8
            if(offsetBit % 8 != 0){
                throw new ParsingException("string field parsing offset (in bits) should be a multiple of 8");
            }

            int fieldLength = fieldLengthBit / 8;
            int offset = offsetBit / 8;

            _value = new String(array.subArray(offset, fieldLength).getBytes());
        }
        else if (_format.equalsIgnoreCase("binary")) {
            // some error, we assume it is a multiple of 8
            if(fieldLengthBit % 8 != 0){
                throw new ParsingException("binary field length (in bits) should be a multiple of 8");
            }
            
            // some error, we assume it is a multiple of 8
            if(offsetBit % 8 != 0){
                throw new ParsingException("binary field parsing offset (in bits) should be a multiple of 8");
            }

            int fieldLength = fieldLengthBit / 8;
            int offset = offsetBit / 8;

            _value = Array.toHexString(array.subArray(offset, fieldLength));
        }
        
        
    }

    /**
     * Transform the TlvField into a String
     */
    @Override
    public String toString() {
        String str = "\n      <field ";
        if (_name != null) {
            str += " name=\"" + _name + "\"";
        }
        if (_value != null) {
            str += " value=\"" + _value + "\"";
        }
        if (_length != 0) {
            str += " length=\"" + _length + "\"";
        }
        if (_lengthBit != 0) {
            str += " lengthBit=\"" + _lengthBit + "\"";
        }
        if (_format != null) {
            if (!_format.equalsIgnoreCase("integer")) {
                str += " format=\"" + _format + "\"";
            }
        }
        str += " />";
        return str;
    }
}
