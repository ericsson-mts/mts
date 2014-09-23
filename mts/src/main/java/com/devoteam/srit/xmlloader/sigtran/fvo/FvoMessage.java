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

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvField;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

import java.util.LinkedList;

import org.dom4j.Attribute;

/**
 *
 * @author Julien Brisseau
 */
public class FvoMessage {
    private Msg _msg;
    private String _name;
    private int _messageType;
    
    private FvoDictionary _dictionary;
    private FvoParameter _header;
    private LinkedList<FvoParameter> _fparameters;
    private LinkedList<FvoParameter> _vparameters;
    private LinkedList<FvoParameter> _oparameters;

    private Array _encodedCache;

    public FvoMessage(Msg msg, FvoDictionary dictionary) {
        _msg = msg;
        _dictionary = dictionary;
        _messageType = 0;
        _fparameters = new LinkedList<FvoParameter>();
        _vparameters = new LinkedList<FvoParameter>();
        _oparameters = new LinkedList<FvoParameter>();
        _header = null;
        _name = null;
        _encodedCache = null;
    }

    private void swapBytes(Array a, int i1, int i2){
        byte b = a.get(i1);
        a.set(i1, a.get(i2));
        a.set(i2, b);
    }

    public Array encode() throws Exception {
        if(null == _encodedCache){
            _encodedCache = doEncode();
        }
        return _encodedCache;
    }

    public int getLength() throws Exception{
        return encode().length;
    }

    private Array doEncode() throws Exception {
        SupArray supArray = new SupArray();
        //header
        Array encodedHeader = _header.encode();

        supArray.addLast(encodedHeader);

        // encode and add fixed parameters
        for(FvoParameter parameter:_fparameters){
            Array encoded = parameter.encode();
            supArray.addLast(encoded);
        }

        // compute the size of the array containing pointers
        boolean containsLongParameter = false;
        for(FvoParameter parameter:_vparameters){
            if(parameter.isLongParameter()){
                containsLongParameter = true;
            }
        }

        int pointersArraySize = _vparameters.size();

        boolean canContainOptionalParameters = false;
        // add one more pointer if there CAN be some optional parameters
        if(!_dictionary.getMessage(_messageType).getOparameters().isEmpty()){
            pointersArraySize++;
            canContainOptionalParameters = true;
        }

        // if the message contains at least one longParameter, then all the pointers are encoded on two bytes
        if(containsLongParameter){
            pointersArraySize *= 2;
        }

        // save the offset to the first pointer
        int pointerOffset = supArray.length;

        // create and add pointers array to message
        Array pointersArray = new DefaultArray(pointersArraySize);
        supArray.addLast(pointersArray);

        for(FvoParameter parameter:_vparameters){
            int parameterOffset = supArray.length;
            int pointerValue = parameterOffset - pointerOffset;
            
            if(containsLongParameter){
                Integer16Array pointer = new Integer16Array(supArray.subArray(pointerOffset, 2));
                pointer.setValue(pointerValue - 1);
                swapBytes(pointer, 0, 1);
            }
            else{
                supArray.set(pointerOffset, pointerValue);
            }

            Array encoded = parameter.encode();

            if(parameter.isLongParameter()){
                Integer16Array length = new Integer16Array(encoded.length);
                swapBytes(length, 0, 1);
                supArray.addLast(length);
            }
            else{
                supArray.addLast(new Integer08Array(encoded.length));
            }
            supArray.addLast(encoded);

            if(containsLongParameter){
                pointerOffset += 2;
            }
            else{
                pointerOffset++;
            }
        }

        // we must handle the optional parameters pointer
        if(canContainOptionalParameters){
            int parameterOffset = supArray.length;
            int pointerValue = 0;
            if(!_oparameters.isEmpty()){
                pointerValue = parameterOffset - pointerOffset;
                for(FvoParameter parameter:_oparameters){
                    supArray.addLast(new Integer08Array(parameter.getId()));

                    Array encoded = parameter.encode();

                    supArray.addLast(new Integer08Array(encoded.length));
                    supArray.addLast(encoded);
                }

                // add the end of optional parameters parameter
                supArray.addLast(new Integer08Array(0));
            }

            if(containsLongParameter){
                Integer16Array pointer = new Integer16Array(supArray.subArray(pointerOffset, 2));
                pointer.setValue(pointerValue - 1);
                swapBytes(pointer, 0, 1);
            }
            else{
                supArray.set(pointerOffset, pointerValue);
            }
        }

        return supArray;
    }

    public LinkedList<FvoParameter> getFparameters() {
        return _fparameters;
    }

    public void setFparameters(LinkedList<FvoParameter> fparameters) {
        this._fparameters = fparameters;
    }

    public int getMessageType() {
        return _messageType;
    }

    public void setMessageType(int messageType) {
        this._messageType = messageType;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public LinkedList<FvoParameter> getOparameters() {
        return _oparameters;
    }

    public void setOparameters(LinkedList<FvoParameter> oparameters) {
        this._oparameters = oparameters;
    }

    public LinkedList<FvoParameter> getVparameters() {
        return _vparameters;
    }

    public FvoParameter getVparameter(String name) {
		Iterator<FvoParameter> iterParam = _vparameters.iterator();
		FvoParameter param = null;
		while (iterParam.hasNext())
		{
			param = (FvoParameter) iterParam.next();
			if (name.equalsIgnoreCase(param.getName()))
			{
				return param;
			}
		}
        return null;
    }

    public void setVparameters(LinkedList<FvoParameter> vparameters) {
        this._vparameters = vparameters;
    }

    public FvoDictionary getDictionary() {
        return _dictionary;
    }

    public void parseElement(Element root) throws Exception {
        Element header = (Element) root.selectSingleNode("./header");
        Attribute messageTypeValue = (Attribute) root.selectSingleNode("./header/field[@name='Message_Type']/@value");
        
        //header
        if (header != null) {
            _header = new FvoParameter(_msg);
            _header.setLittleEndian( _dictionary.getHeader().isLittleEndian());
            _header.parseElement(header);
        }
        
        //typeCode
        if (messageTypeValue != null) {
            try {
                setMessageType(Integer.decode(messageTypeValue.getValue()));
            }
            catch (Exception e) {
                // TODO: handle exception recherche dans dictionnaire
                throw e;
            }
        }
        else {
            throw new ExecutionException("The Message_Type field must be set in the header\n" + root.asXML());
        }

        //parameters
        parseParametersFromXml(root.selectNodes("./parameter"));
    }

    public void parseParametersFromXml(List<Element> elements) throws Exception {
        String currentParameterType = "F";
        for (int cpt = 0; cpt < elements.size(); cpt++) {
            Element element = elements.get(cpt);
            FvoParameter parameter = new FvoParameter(_msg);
            parameter.setType(currentParameterType);
            parameter.parseElement(element);
            currentParameterType = parameter.getType();
            if (currentParameterType.equalsIgnoreCase("F")) {
                _fparameters.add(parameter);
            }
            else if (currentParameterType.equalsIgnoreCase("V")) {
                _vparameters.add(parameter);
            }
            else {
                _oparameters.add(parameter);
            }
        }
    }

    public void parseArray(Array array) throws Exception {
        _encodedCache = array;

        // TODO more generic header parsing
        // INIT
        int offset = 0;

        // HEADER
        FvoParameter header = _dictionary.getHeader();
        _header = new FvoParameter(_msg, header);
        _header.parseArray(array.subArray(offset, _header.getMessageLength()));
        offset += header.getMessageLength();

        // get the message type
        _messageType = -1;
        for(FvoField field:_header.getFields()){
            if("Message_Type".equalsIgnoreCase(field.getName())){
                _messageType = Integer.decode(field.getValue());
            }
        }

        if(-1 == _messageType){
            // TODO : some error
        }

        // get the list of parameters for this message from dictionary
        FvoMessage dictionaryMessage = _dictionary.getMessage(_messageType);

        // parse all the mandatory fixed parameters
        for(FvoParameter dictionaryParameter:dictionaryMessage.getFparameters()){
            FvoParameter parameter = new FvoParameter(_msg, dictionaryParameter);
            parameter.parseArray(array.subArray(offset, parameter.getMessageLength()));
            _fparameters.add(parameter);
            offset += parameter.getMessageLength();
        }

        // check if the message contains a longParameter param, thus, if pointers are on 2 bytes
        boolean containsLongParameter = false;
        for(FvoParameter dictionaryParameter:dictionaryMessage.getVparameters()){
            if(dictionaryParameter.isLongParameter()){
                containsLongParameter = true;
            }
        }

        // parse all the mandatory variable parameters
        for(FvoParameter dictionaryParameter:dictionaryMessage.getVparameters()){
            FvoParameter parameter = new FvoParameter(_msg, dictionaryParameter);
            int parameterOffset;
            if(containsLongParameter){
                parameterOffset = offset + 1 + (0xff & array.get(offset)) + (0xff & array.get(offset + 1)) * 256;
            }
            else{
                parameterOffset = offset + (0xff & array.get(offset));
            }

            int parameterLength;
            if(parameter.isLongParameter()){
                parameterLength = 0xff & array.get(parameterOffset) + (0xff & array.get(parameterOffset + 1)) * 256;
                parameter.parseArray(array.subArray(parameterOffset + 2, parameterLength));
            }
            else{
                parameterLength = 0xff & array.get(parameterOffset);
                parameter.parseArray(array.subArray(parameterOffset + 1, parameterLength));
            }

            _vparameters.add(parameter);

            if(containsLongParameter){
                offset +=2 ;
            }
            else{
                offset ++;
            }
        }

        // if there may be optional parameters 
        if(!dictionaryMessage.getOparameters().isEmpty()){
            // if there are optional parameters
            if(array.get(offset) != 0){
                // go the first optional parameter
                if(containsLongParameter){
                    offset = offset + 1 + (0xff & array.get(offset)) + (0xff & array.get(offset + 1)) * 256;
                }
                else{
                    offset = offset + (0xff & array.get(offset));
                }

                int code = 0xff & array.get(offset);
                while(code != 0){
                    // O param len should alway be encoded on one bye
                    int length = 0xff & array.get(offset+1);
                    // try to find an optional parameter with a matching code for this message
                    FvoParameter parameter = null;
                    for(FvoParameter dictionaryParameter:dictionaryMessage.getOparameters()){
                        if(dictionaryParameter.getId() == code){
                            parameter = new FvoParameter(_msg, dictionaryParameter);
                        }
                    }

                    // TODO default to binary parameter if not found
                    if(null != parameter){
                        parameter.parseArray(array.subArray(offset + 2, length));
                        _oparameters.add(parameter);
                    }
                    else{
                        System.out.println("ignored optional parameter code " + code);
                    }

                    offset += length + 2;
                    code = 0xff & array.get(offset);
                }
            }
        }
    }

    public String toShortString() {
        String str = new String();
        str += "<SS7 type=\"";
        str += this._messageType;
        if (_name != null) {
            str += "/(" + this._name;
        }
        str += "\")/>";
        return str;
    }

    public String toString() {
        String str = new String();
        str += "<SS7 typeCode=\"" + this._messageType + "\">";
        if (_name != null) {
            str += "\n name=\"" + this._name + "\">";
        }
        if (_header != null) {
            str += "\n   <header>";
            for (int i = 0; i < _header.getFields().size(); i++) {
                str += _header.getFields().get(i).toString();
            }
            str += "\n   </header>";
        }
        for (int i = 0; i < _fparameters.size(); i++) {
            str += _fparameters.get(i).toString();
        }
        for (int i = 0; i < _vparameters.size(); i++) {
            str += _vparameters.get(i).toString();
        }
        for (int i = 0; i < _oparameters.size(); i++) {
            str += _oparameters.get(i).toString();
        }
        str += "\n</SS7>";
        return str;
    }
}
