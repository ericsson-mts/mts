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

import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.*;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;
import com.devoteam.srit.xmlloader.sigtran.StackSigtran;

import java.util.LinkedList;

/**
 *
 * @author Julien Brisseau
 */
public class TlvMessage {

    private Msg _msg;
    private TlvDictionary _dictionary;
    private String name;

    // protocol identifier (used in SCTP payload protocol identifier) only way to discriminate
    public static final int IUA = 1;
    public static final int M3UA = 3;
    public static final int V5UA = 6;

    private Integer08Array _version;
    private Integer08Array _reserved;
    private Integer08Array _messageClass;
    private Integer08Array _messageType;
    private Integer32Array _messageLength;
    private Array _messageData;

    private LinkedList<TlvParameter> _parameters = new LinkedList();

    public TlvMessage(Msg msg, TlvDictionary dictionary) {
        _msg = msg;
        _dictionary = dictionary;
        _version = new Integer08Array(1);
        _reserved = new Integer08Array(0);
        _messageClass = new Integer08Array(0);
        _messageType = new Integer08Array(0);
        _messageLength = new Integer32Array(0);
    }

    public TlvMessage(Msg msg, Array array, int protocolIdentifier) throws Exception {
        _msg = msg;

        // TODO : move to message class later for cleaner separation
        // TODO : read all dico to fill a mapping hashmap between ppid and dictionary
        switch(protocolIdentifier){
            case IUA:
                _dictionary = StackSigtran.instance().getTlvDictionnary("iua.xml");
                break;
            case M3UA:
                _dictionary = StackSigtran.instance().getTlvDictionnary("m3ua.xml");
                break;
            case V5UA:
                _dictionary = StackSigtran.instance().getTlvDictionnary("v5ua.xml");
                break;
            default:
            	throw new ExecutionException("Unknown value for protocol indentifier : " + protocolIdentifier);
        }
        
        try {
            _version = new Integer08Array(array.subArray(0, 1));
            _reserved = new Integer08Array(array.subArray(1, 1));
            _messageClass = new Integer08Array(array.subArray(2, 1));
            _messageType = new Integer08Array(array.subArray(3, 1));
            _messageLength = new Integer32Array(array.subArray(4, 4));
            _messageData = array.subArray(8);

            // parse all parameters
            int offset = 0;
            while(offset < _messageData.length){
                TlvParameter parameter = new TlvParameter(_msg, _dictionary);
                parameter.parseArray(_messageData.subArray(offset));
                _parameters.add(parameter);
                offset += parameter.getLength();
                // handle padding; go to next multiple of 4 if not already
                if(offset % 4 != 0){
                    offset += 4 - (offset % 4);
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException("The TLV message can't be decoded" + array.toString(), e);
        }
    }

    public TlvDictionary getDictionary(){
        return _dictionary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMessageClass() {
        return _messageClass.getValue();
    }

    public void setMessageClass(int value) {
        this._messageClass.setValue(value);
    }

    public int getMessageLength() {
        return _messageLength.getValue();
    }

    public void setMessageLength(int value) {
        this._messageLength.setValue(value);
    }

    public int getMessageType() {
        return _messageType.getValue();
    }

    public void setMessageType(int value) {
        this._messageType.setValue(value);
    }

    public LinkedList<TlvParameter> getParameters() {
        return _parameters;
    }

    public void setParameters(LinkedList<TlvParameter> parameters) {
        this._parameters = parameters;
    }

    public int getReserved() {
        return _reserved.getValue();
    }

    public void setReserved(int value) {
        this._reserved.setValue(value);
    }

    public int getVersion() {
        return _version.getValue();
    }

    public void setVersion(int value) {
        this._version.setValue(value);
    }

    public TlvParameter getTlvParameter(String name) {
        TlvParameter param = null;
        for (int i = 0; i < _parameters.size(); i++) {
            param = _parameters.get(i);
            if ((param != null) && (name.equalsIgnoreCase(param.getName()))) {
                return param;
            }
            if ((param != null) && (name.equalsIgnoreCase(((Integer) param.getTag()).toString()))) {
                return param;
            }
        }
        return null;
    }

    //TODO MEP
    /**
     * Get a parameter from the message
     *
     * @param path		: The path of the parameter requested
     * @return			: The parameter requested
     * @throws Exception
     */
    public Parameter getParameter(String path) throws Exception {

        Parameter parameter = null;
        String[] params = Utils.splitPath(path);

        if (params[0].equalsIgnoreCase("header")) {
            if (params.length == 2) {
                parameter = new Parameter();

                if (params[1].equalsIgnoreCase("version")) {
                    parameter.add(this.getVersion());
                }
                else if (params[1].equalsIgnoreCase("reserved")) {
                    parameter.add(this.getReserved());
                }
                else if (params[1].equalsIgnoreCase("messageClass")) {
                    parameter.add(this.getMessageClass());
                }
                else if (params[1].equalsIgnoreCase("messageType")) {
                    parameter.add(this.getMessageType());
                }
                else if (params[1].equalsIgnoreCase("messageLength")) {
                    parameter.add(this.getMessageLength());
                }
                else {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
        }
        else if (params[0].equalsIgnoreCase("parameter")) {
            if (params.length > 2) {
                //get attribute given
                TlvParameter tlvParameter = this.getTlvParameter(params[1]);
                if(null != tlvParameter){
                    if (path.contains(":")) {
                        path = path.substring(path.indexOf(":") + 1);
                        parameter = tlvParameter.getParameter(path.substring(path.indexOf(":") + 1));
                    }
                    else {
                        path = path.substring(path.indexOf(".") + 1);
                        parameter = tlvParameter.getParameter(path.substring(path.indexOf(".") + 1));
                    }
                }
            }
        }
        else {
            Parameter.throwBadPathKeywordException(path);
        }

        return parameter;
    }

    public Array encode() throws Exception {
        SupArray supArray = new SupArray();

        // encode and append header fields
        supArray.addLast(_version);
        supArray.addLast(_reserved);
        supArray.addLast(_messageClass);
        supArray.addLast(_messageType);
        supArray.addLast(_messageLength);

        // encode and append tlv parameters (padding included)
        for (TlvParameter tlvParameter:_parameters) {
            supArray.addLast(tlvParameter.encode());
        }

        // set the length now that we know it, will change in the encoded array too
        _messageLength.setValue(supArray.length);

        // return the encoded message
        return supArray;
    }

    public Array getArray() throws Exception {
        return encode();
    }

    public void parseMsgFromXml(Element root) throws Exception {
        //header
        Element header = root.element("header");
        String version = header.attributeValue("version");
        String reserved = header.attributeValue("reserved");
        String messageClass = header.attributeValue("messageClass");
        String messageType = header.attributeValue("messageType");
        String messageLength = header.attributeValue("messageLength");

        if (version != null) {
            setVersion(Integer.decode(version));
        }

        if (reserved != null) {
            setReserved(Integer.decode(reserved));
        }

        if (messageClass != null) {
            try {
                setMessageClass(Integer.decode(messageClass));
            }
            catch (Exception e) {
                try {
                    setMessageClass(_dictionary.messageClassValue(messageClass));
                }
                catch (Exception e2) {
                    throw new ExecutionException("The messageClass of the message header is unrecognized\n" + root.asXML().replace("	", ""));
                }
            }
        }
        else {
            throw new ExecutionException("The messageClass of the message header must be set\n" + root.asXML().replace("	", ""));
        }

        if (messageType != null) {
            try {
                setMessageType(Integer.decode(messageType));
            }
            catch (Exception e) {
                try {
                    setMessageType(_dictionary.messageTypeValue(messageType));
                }
                catch (Exception e2) {
                    throw new ExecutionException("The messageType of the message header is unrecognized\n" + root.asXML().replace("	", ""));
                }
            }
        }
        else {
            throw new ExecutionException("The messageType of the message header must be set\n" + root.asXML().replace("	", ""));
        }

        if (messageLength != null) {
            setMessageLength(Integer.decode(messageLength));
        }

        List<Element> parameters = root.elements("parameter");
        for (int cpt = 0; cpt < parameters.size(); cpt++) {
            Element element = parameters.get(cpt);
            TlvParameter parameter = new TlvParameter(_msg, _dictionary);
            parameter.parseElement(element);
            getParameters().add(parameter);
        }
    }

    public String toShortString(String layer) {
        String layerUpper = layer.toUpperCase();
        String str = new String();
        str += "<" + layerUpper + " class/type=\"";
        String className = _dictionary.messageClassName(getMessageClass());
        if (className != null) {
            str += className;
        }
        str += "(" + getMessageClass() + ")";
        String typeName = _dictionary.messageTypeName(getMessageClass(), getMessageType());
        if (typeName != null) {
            str += "/" + typeName;
        }
        str += "(" + getMessageType() + ")";
        str += "\"/>";
        return str;
    }

    @Override
    public String toString() {
        String str = new String();
        str += "<UA>";
        str += "<header";
        str += " version=\"" + getVersion() + "\"";
        str += " reserved=\"" + getReserved() + "\"";
        String className = _dictionary.messageClassName(getMessageClass());
        if (className != null) {
            str += " messageClass=\"" + className + "\"";
        }
        else {
            str += " messageClass=\"" + getMessageClass() + "\"";
        }
        String typeName = _dictionary.messageTypeName(getMessageClass(), getMessageType());
        if (typeName != null) {
            str += " messageType=\"" + typeName + "\"";
        }
        else {
            str += "messageType=\"" + getMessageLength() + "\"";
        }
        if (getMessageLength() != 0) {
            str += " messageLength=\"" + getMessageLength() + "\"";
        }
        str += "/>";

        for (int i = 0; i < _parameters.size(); i++) {
            str += _parameters.get(i).toString();
        }
        str += "\n</UA>";
        return str;
    }
}
