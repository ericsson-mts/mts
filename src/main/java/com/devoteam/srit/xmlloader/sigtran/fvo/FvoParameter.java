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

import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvField;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvParameter;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author Julien Brisseau
 */
public class FvoParameter {
    private Msg _msg;
    private int _id;
    private String _name;
    private int _messageLength = 0;
    private boolean _littleEndian = false;
    private boolean _longParameter = false;
    private String _type = "F";
    private LinkedList<FvoField> _fields = new LinkedList();

    public FvoParameter(Msg msg) {
        _msg = msg;
    }
    
    public FvoParameter(Msg msg, FvoParameter other) {
        _msg = msg;
        _id = other._id;
        _name = other._name;
        _messageLength = other._messageLength;
        _littleEndian = other._littleEndian;
        _longParameter = other._longParameter;
        _type = other._type;
        
        for(FvoField otherField:other.getFields()){
            _fields.add(new FvoField(_msg, otherField));
        }
    }

    public Array encode() throws Exception {
        // compute the final length of the parameter
        int parameterLengthBit = 0;
        for (FvoField field : _fields) {
            parameterLengthBit += field.getLengthBit();
        }

        // extend to next multiple of 8
        parameterLengthBit += (8 - (parameterLengthBit % 8)) % 8;

        // prepare the byte array that will receive the data
        DefaultArray buffer = new DefaultArray(parameterLengthBit / 8);

        // encode the data into the buffer
        int offsetBit = 0;
        for (FvoField field : _fields) {
            try {
                field.encode(buffer, offsetBit);
                offsetBit += field.getLengthBit();
            }
            catch (Exception e) {
                throw new ParsingException("Error while encoding field :\n" + field + "\nFor parameter :\n" + this, e);
            }
        }
        
        return buffer;
    }

    public int getMessageLength() {
        return _messageLength;
    }

    public void setMessageLength(int messageLength) {
        _messageLength = messageLength;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public void setLittleEndian(boolean value) {
        _littleEndian = value;
    }

    public boolean isLittleEndian() {
        return _littleEndian;
    }

    public void setLongParameter(boolean value) {
        _longParameter = value;
    }

    public boolean isLongParameter() {
        return _longParameter;
    }

    public LinkedList<FvoField> getFields() {
        return _fields;
    }

    public void setFields(LinkedList<FvoField> fields) {
        _fields = fields;
    }

    public FvoField getFvoField(String name) {
		Iterator<FvoField> iterParam = _fields.iterator();
		FvoField param = null;
		while (iterParam.hasNext())
		{
			param = (FvoField) iterParam.next();
			if (name.equalsIgnoreCase(param.getName()))
			{
				return param;
			}
		}
        return null;
    }
    
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
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
        else if (params[0].equalsIgnoreCase("id")) {
            parameter.add(this._id);
        }
        else if (params[0].equalsIgnoreCase("length")) {
            parameter.add(this._messageLength);
        }
        else if (params[0].equalsIgnoreCase("type")) {
            parameter.add(this._type);
        }
        else if (params[0].equalsIgnoreCase("longParameter")) {
            parameter.add(this._longParameter);
        }
        else if (params[0].equalsIgnoreCase("littleEndian")) {
            parameter.add(this._littleEndian);
        }        
        else if ((params[0].equalsIgnoreCase("field")) && (params.length >= 2)) {
            FvoField tlvField = this.getFvoField(params[1]);
            if (tlvField != null)
            {
	            if (path.contains(":")) {
	                path = path.substring(path.indexOf(":") + 1);
	                parameter = tlvField.getParameter(path.substring(path.indexOf(":") + 1));
	            }
	            else {
	                path = path.substring(path.indexOf(".") + 1);
	                parameter = tlvField.getParameter(path.substring(path.indexOf(".") + 1));
	            }
            }
        }
        else {
            parameter = null;
        }

        return parameter;
    }
    
    public void parseElement(Element root) throws Exception {           
        //Create the FvoParameter
        String id = root.attributeValue("id");
        if (id != null) {
        	String name = root.attributeValue("name");
            setName(name);
        }
        if (id != null) {
            setId(Integer.decode(id));
        }
        
        String type = root.attributeValue("type");
        if (type != null) {
            setType(type);
        }

        String littleEndian = root.attributeValue("littleEndian");
        if (littleEndian != null) {
            setLittleEndian(Utils.parseBoolean(littleEndian, "littleEndian"));
        }

        // search for errors in XML
        String longParameter = root.attributeValue("longParameter");
        if (type != null) {
            if (!(type.equalsIgnoreCase("F") || type.equalsIgnoreCase("V") || type.equalsIgnoreCase("O"))) {
                throw new ExecutionException("Parameter type value must be set at 'F', 'V' or 'O'\n" + root.asXML().replace("	", ""));
            }
            if ((type.equalsIgnoreCase("O")) && (id == null)) {
                throw new ExecutionException("O Parameters must have a name\n" + root.asXML().replace("	", ""));
            }
            if (!(type.equalsIgnoreCase("V")) && (longParameter != null)) {
                throw new ExecutionException("Only V Parameter may have longParameter flag\n" + root.asXML().replace("	", ""));
            }
        }

        List<Element> listFields = root.elements("field");
        if (!listFields.isEmpty()) {
            for (int cpt = 0; cpt < listFields.size(); cpt++) {
                Element element = listFields.get(cpt);
                FvoField field = new FvoField(_msg);
                field.setLittleEndian(isLittleEndian());
                field.parseElement(element);
                getFields().add(field);
            }
        }
    }

    public void parseArray(Array array) throws Exception 
    {
        if(!"F".equalsIgnoreCase(_type))
        {
            _messageLength = array.length;
        }

        // Give the length in bits of the parameter already encoding
        int offsetBit = 0;
        if (!_fields.isEmpty()) {
            // try to decode multiple sets of the dictionary definition (for lists)
            // decode all fields defined in parameter (maybe from scenario, maybe from dico)
            LinkedList<FvoField> fieldsToParse = _fields;
            _fields = new LinkedList<FvoField>();
            for(FvoField field:fieldsToParse){
                if (offsetBit + field.getLengthBit() <= array.length * 8)
                {
                    field.parseArray(array, offsetBit);
                    offsetBit += field.getLengthBit();
                    _fields.addLast(field);
                }
            }
        }
        
        // if there is still undecoded data at the end of the parameter
        if (offsetBit < array.length * 8)
        {
        	FvoField field = new FvoField(_msg);
        	field.setFormat(FvoField.formatBinary);
        	field.setName("Unknown_undefined_in_dictionary");
        	field.parseArray(array, offsetBit);
        	getFields().add(field);
        }            
    }

    @Override
    public String toString() {
        String str = "\n   <parameter";
        if (_name != null) {
            str += " name=\"" + _name + "\"";
        }
        if (_id != 0) {
            str += " id=\"" + _id + "\"";
        }
        if (_longParameter) {
            str += " longParameter=\"" + _longParameter + "\"";
        }
        if (_littleEndian) {
            str += " littleEndian=\"" + _littleEndian + "\"";
        }
        if (_messageLength != 0) {
            str += " length=\"" + _messageLength + "\"";
        }
        if (_type != null) {
            str += " type=\"" + _type + "\"";
        }
        str += ">";
        for (int i = 0; i < _fields.size(); i++) {
            str += _fields.get(i).toString();
        }
        str += "\n   </parameter>";
        return str;
    }
}
