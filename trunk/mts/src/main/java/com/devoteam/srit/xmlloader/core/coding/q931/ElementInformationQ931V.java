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

package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.Parameter;

import gp.utils.arrays.*;

import java.util.Map.Entry;


/**
 *
 * @author indiaye
 */
public class ElementInformationQ931V {

    private ElementInformationQ931 _elementInformation;
    private Array _value;
    private Array _fields;
    boolean _bigLength;
    private Integer08Array _id;
    boolean _fromdata;

    public ElementInformationQ931V(Array array, boolean bigLength, boolean fromdata, ElementInformationQ931 elem) {
        _elementInformation = elem;
        _fromdata = fromdata;
        _bigLength = bigLength;
        if (fromdata) {
	        _id = new Integer08Array(array.subArray(0, 1));
            if ((elem != null) && (elem.getHashMapFields().size() >= 1))
            {
		        if (bigLength == true) {
		            int length = new Integer16Array(array.subArray(1, 2)).getValue();
		            _value = array.subArray(0, length + 3);
		            _fields = _value.subArray(3);
		        }
		        else {
		            int length = new Integer08Array(array.subArray(1, 1)).getValue();
		            _value = array.subArray(0, length + 2);
		            _fields = _value.subArray(2);
		        }	            	
		    }
        }
        else {
	    	_id = new Integer08Array(array.subArray(0, 1));
	        setId();
            if ((elem != null) && (elem.getHashMapFields().size() >= 1))
            {
		        _value = array;
		        if (bigLength) {
		            Integer16Array length = new Integer16Array(array.subArray(1, 2));
		            length.setValue(array.length - 3);
		            _fields = _value.subArray(3);
		        }
		        else {
		            Integer08Array length = new Integer08Array(array.subArray(1, 1));
		            length.setValue(array.length - 2);
		            _fields = _value.subArray(2);
		        }
            }
        }
    }

    public ElementInformationQ931 getElementInformation() {
        return _elementInformation;
    }

    public Array getArray() {
        SupArray sup = new SupArray();
        sup.addLast(_id);
        if (_fields != null)
        {
		    Integer08Array length8 = new Integer08Array(_fields.length);
		    if (length8.getValue() != 0)
		    {
		    	if (_bigLength) {
		    		sup.addLast(new Integer16Array(_fields.length));
		    	}
		    	else {
		    		sup.addLast(length8);
		    	}        
		    	sup.addLast(_fields);
		    }
        }
        return sup;
    }

    public int getId() {
        return _id.getValue();
    }

    public Array getFieldsArray() {
        return _fields;
    }

    public void setFields(Array _fields) {
        this._fields = _fields;
    }

    private void setId() {
        _id.setValue(_elementInformation.getId());
    }

    public void setElementInformation(ElementInformationQ931 _elementInformation) {
        this._elementInformation = _elementInformation;
    }

    @Override
    public String toString() {

        StringBuilder elemString = new StringBuilder();
        elemString.append("<element ");
        elemString.append("identifier=\"");
        if (_elementInformation != null)
        {
        	if (_elementInformation.getName() != null)
        	{
        		elemString.append(_elementInformation.getName() + ":");
        	}
        	elemString.append(_id.getValue());
        }
        if ((_elementInformation == null) && (_fields != null)){
            elemString.append(" value=\"" + Array.toHexString(_fields));
        }
        elemString.append("\">");
        elemString.append("\n");
        if (_elementInformation != null) {
            for (Entry<String, Field> e : _elementInformation.getHashMapFields().entrySet()) {
                elemString.append(e.getValue().toString(this));
            }
        }
        elemString.append("</element>");
        elemString.append("\n");
        return elemString.toString();
    }

    public void getParameter(Parameter var, String[] params, String path) throws Exception {
        if (params.length ==3) 
        {
        	if (this._value != null)
        	{
        		var.add(Array.toHexString(this._value));
        	}
        }
        else if (params.length > 4 && (params[3].equalsIgnoreCase("field"))) 
        {
        	Field field = getField(params[4]);
        	if (field != null)
        	{	
        		var.add(field.getValue(this));
        	}
        }
        else
        {
           	Parameter.throwBadPathKeywordException(path);
        }
    }

    public Field getField(String name) {
        return _elementInformation.getHashMapFields().get(name);
    }
}
