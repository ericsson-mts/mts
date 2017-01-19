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

import gp.utils.arrays.*;

import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;

import java.util.LinkedList;

/**
 *
 * @author Julien Brisseau
 */
public class TlvParameter {

    private Msg _msg;
    private TlvDictionary _dictionary;
    private String _name;
    private Integer16Array _tag;
    private Integer16Array _length;
    private Array _data;
    private String _format;
    private LinkedList<TlvParameter> _parameters;
    private LinkedList<TlvField> _fields;

    /**
     * Constructor
     * 
     * @param msg		: Sigtran message
     */
    public TlvParameter(Msg msg, TlvDictionary dictionary) {
        this._msg = msg;
        this._dictionary = dictionary;
        this._name = null;
        this._tag = new Integer16Array(0);
        this._length = new Integer16Array(0);
        this._format = null;
        this._parameters = new LinkedList();
        this._fields = new LinkedList();
    }

    /**
     * Get the fields list of the tlvParameter
     * 
     * @return : the fields list
     */
    public LinkedList<TlvField> getFields() {
        return _fields;
    }

    /**
     * Set the fields list of the tlvParameter
     *
     * @param fields : the fields list
     */
    public void setFields(LinkedList<TlvField> fields) {
        this._fields = fields;
    }

    /**
     * Get the format of the tlvParameter
     *
     * @return : the format of the tlvParameter
     */
    public String getFormat() {
        return _format;
    }

    /**
     * Set the format of the tlvParameter
     *
     * @param format : the format of the tlvParameter
     */
    public void setFormat(String format) {
        this._format = format;
    }

    /**
     * Get the length of the tlvParameter
     *
     * @return : the length of the tlvParameter
     */
    public int getLength() {
        return _length.getValue();
    }

    /**
     * Set the length of the tlvParameter
     *
     * @param length: the length of the tlvParameter
     */
    public void setLength(int value) {
        _length.setValue(value);
    }

    /**
     * Get the tag of the tlvParameter
     *
     * @return : the tag of the tlvParameter
     */
    public int getTag() {
        return _tag.getValue();
    }

    /**
     * Set the tag of the tlvParameter
     *
     * @param tag : the tag of the tlvParameter
     */
    public void setTag(int value) {
        _tag.setValue(value);
    }

    /**
     * Get the Sigtran message which contains this parameter
     *
     * @return	: the Sigtran message
     */
    public Msg getMsg() {
        return _msg;
    }

    /**
     *  Set the Sigtran message which contains this parameter
     *
     * @param msg		: the Sigtran message
     */
    public void setMsg(Msg msg) {
        this._msg = msg;
    }

    /**
     * Get the parameters list of the tlvParameter
     *
     * @return : the parameters list of the tlvParameter
     */
    public LinkedList<TlvParameter> getParameters() {
        return _parameters;
    }

    /**
     * Set the parameters list of the tlvParameter
     *
     * @param parameters: the parameters list of the tlvParameter
     */
    public void setParameters(LinkedList<TlvParameter> parameters) {
        this._parameters = parameters;
    }

    /**
     * Get a parameter from the parameter name
     * @param name		: Name of the parameter
     * @return			: the tlvParameter
     */
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

    /**
     * Get a field from the field name
     * @param name		: Name of the field
     * @return			: the tlvField
     */
    public TlvField getTlvField(String name) {
        TlvField field = null;
        for (int i = 0; i < _fields.size(); i++) {
            field = _fields.get(i);
            if ((field != null) && (name.equalsIgnoreCase(field.getName()))) {
                return field;
            }
        }
        try {
            return _fields.get(Integer.decode(name));
        }
        catch (Exception e) {
        }
        return null;
    }

    /**
     * Get a parameter from the message
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
        else if (params[0].equalsIgnoreCase("tag")) {
            parameter.add(this.getTag());
        }
        else if (params[0].equalsIgnoreCase("length")) {
            parameter.add(this.getLength());
        }
        else if (params[0].equalsIgnoreCase("format")) {
            parameter.add(this.getFormat());
        }
        else if ((params[0].equalsIgnoreCase("parameter")) && (params.length > 2)) {
            TlvParameter tlvParameter = this.getTlvParameter(params[1]);
            if (path.contains(":")) {
                path = path.substring(path.indexOf(":") + 1);
                parameter = tlvParameter.getParameter(path.substring(path.indexOf(":") + 1));
            }
            else {
                path = path.substring(path.indexOf(".") + 1);
                parameter = tlvParameter.getParameter(path.substring(path.indexOf(".") + 1));
            }
        }
        else if ((params[0].equalsIgnoreCase("field")) && (params.length >= 2)) {
            TlvField tlvField = this.getTlvField(params[1]);
            if (path.contains(":")) {
                path = path.substring(path.indexOf(":") + 1);
                parameter = tlvField.getParameter(path.substring(path.indexOf(":") + 1));
            }
            else {
                path = path.substring(path.indexOf(".") + 1);
                parameter = tlvField.getParameter(path.substring(path.indexOf(".") + 1));
            }
        }
        else {
            parameter = null;
        }

        return parameter;
    }

    public Array encode() throws Exception {
        SupArray array = new SupArray();
        // encode the header (T & L)
        array.addLast(_tag);
        array.addLast(_length);

        // encode the value (V)
        if (!_parameters.isEmpty()) {
            // grouped (contains other parameters)
            for (TlvParameter parameter : _parameters) {
                array.addLast(parameter.encode());
            }
        }
        else {
            // normal (contains fields)
            // compute the final length (bits) of the parameter
            int parameterLengthBit = 0;
            for (TlvField field : _fields) {
                parameterLengthBit += field.getLength() * 8 + field.getLengthBit();
            }

            // extend to next multiple of 8
            parameterLengthBit += (8 - (parameterLengthBit % 8)) % 8;

            // prepare the byte array that will receive the data
            Array buffer = new DefaultArray(parameterLengthBit / 8);

            // encode the data into the buffer
            int offsetBit = 0;
            for (TlvField field : _fields) {
                try {
                    buffer = field.encode(buffer, offsetBit);
                    offsetBit += field.getLength() * 8 + field.getLengthBit();
                }
                catch (Exception e) {
                    throw new ParsingException("Error while encoding field :\n" + field + "\nFor parameter :\n" + this, e);
                }
            }

            array.addLast(buffer);
        }


        _length.setValue(array.length);

        // now add padding if necessary (to next multiple of 4 octets)
        int padding = (4 - (array.length % 4)) % 4;
        if (padding > 0) {
            array.addLast(new ConstantArray((byte) 0, padding));
        }

        return array;
    }

    /**
     * Parse the TLV parameter from the XML element
     */
    public void parseElement(Element root) throws Exception {
        String tagStr = root.attributeValue("tag");
        List<Element> listParameters = root.elements("parameter");
        List<Element> listFields = root.elements("field");

        // try to get parameter def from dictionary
        TlvParameter dictionaryParameter;
        int tagInt;
        try {
            tagInt = Integer.decode(tagStr);

            // the tag is already an integer
            dictionaryParameter = _dictionary.parameter(tagInt, _msg);
        }
        catch (Exception e) {
            // the tag was not an integer
            dictionaryParameter = _dictionary.parameter(tagStr, _msg);
            tagInt = dictionaryParameter.getTag();
            if (null == dictionaryParameter) {
                throw new ParsingException("Tag was not an integer and was not found in dictionary.\n", e);
            }
        }

        // set the tag
        _tag.setValue(tagInt);

        // if possible, copy human readable name from dictionary
        if (null != dictionaryParameter) {
            _name = dictionaryParameter._name;
        }

        // parse the inner fields
        if (!listFields.isEmpty()) {
            for (Element parameter : listFields) {
                TlvField tlvField = new TlvField(_msg, _dictionary);

                // parse a first time (to init name...)
                tlvField.parseElement(parameter);

                // if the parameter is defined in the dictionary, then try to find definition of the field
                if(null != dictionaryParameter){
                    for(TlvField dictionaryField:dictionaryParameter.getFields()){
                        if(dictionaryField.getName().equalsIgnoreCase(tlvField.getName())){
                            tlvField.init(dictionaryField);
                        }
                    }
                }

                // parse a second time (to override dictionary values...)
                tlvField.parseElement(parameter);

                // check the length has been defined
                if (("integer".equalsIgnoreCase(tlvField.getFormat()) || "spare".equalsIgnoreCase(tlvField.getFormat()))
                        && (-1 == tlvField.getLength() && -1 == tlvField.getLengthBit())) {
                    throw new ParsingException("length attributes are not defined, neither in dictionary or in scenario\n" + root.asXML());
                }

                _fields.add(tlvField);
            }
        }
        else if (!listParameters.isEmpty()) {
            // parse the inner parameters
            for (Element parameter : listParameters) {
                TlvParameter tlvParameter = new TlvParameter(_msg, _dictionary);
                tlvParameter.parseElement(parameter);
                _parameters.add(tlvParameter);
            }
        }
    }

    public void parseArray(Array array) throws Exception {

        // Give the length in bits of the parameter already encoding

        _tag = new Integer16Array(array.subArray(0, 2));
        _length = new Integer16Array(array.subArray(2, 2));
        _data = array.subArray(4, _length.getValue() - 4);

        _name = _dictionary.parameterName(getTag());
        _format = "grouped";

        TlvParameter dictionaryParameter = _dictionary.parameter(getTag(), _msg);

        int offsetBit = 0;
        if (dictionaryParameter != null && dictionaryParameter.getFields() != null) {
            TlvField dictionaryField = null;
            // try to decode multiple sets of the dictionary definition (for lists)
            parsing: while (offsetBit < _data.length * 8) {
                // decode all fields defined in the dictionary
                for (int i = 0; i < dictionaryParameter.getFields().size(); i++) {
                    dictionaryField = dictionaryParameter.getFields().get(i);
                    TlvField field = new TlvField(_msg, _dictionary);
                    field.init(dictionaryField);
                    if("string".equalsIgnoreCase(field.getFormat()) ||
                       "fvo".equalsIgnoreCase(field.getFormat()) ||
                       "binary".equalsIgnoreCase(field.getFormat()) ||
                       offsetBit + field.getLength() * 8 + field.getLengthBit() <= _data.length * 8){
                        // other cases, check that the remaining data is enough
                        field.parseArray(_data, offsetBit);
                        getFields().add(field);

                        offsetBit += field.getLength() * 8 + field.getLengthBit();
                    }
                    else{
                        // stop here, we got over the end of the field
                        break parsing;
                    }
                }
            }

            // if there is still undecoded data at the end of the parameter
            while (offsetBit < _data.length * 8) {
                // TODO : should create and handle a binary field with the rest of the data
                //        for now we create a list of 1 bit integers
                TlvField field = new TlvField(_msg, _dictionary);
                field.setLength(0);
                field.setLengthBit(1);
                field.setFormat("integer");
                field.setName("unknown");
                field.parseArray(array, offsetBit);
                        
                getFields().add(field);
                offsetBit++;
            }
        }
        else {
            // create a list of integer08 fields
            // TODO : shoud create a binary format field with all data
            for (int i = 0; i < _data.length; i++) {
                TlvField field = new TlvField(_msg, _dictionary);
                field.setLength(1);
                field.setName("undefined");
                getFields().add(field);
                field.setValue(Integer.toString(_data.get(i) & 0xff));
            }
        }
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String toString() {
        String str = "\n   <parameter tag=\"";
        if (_name != null) {
            str += _name + ":" + _tag.getValue();
        }
        else{
            str += _tag.getValue();
        }

        if(_parameters.isEmpty() && _fields.isEmpty()){
            str += "\"\\ >";
        }
        else{
            str += "\" >";
            for (TlvParameter parameter: _parameters) {
                str += parameter.toString();
            }
            for (TlvField field: _fields) {
                str += field.toString();
            }
            str += "\n   </parameter>";
        }
        return str;
    }
}
