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

package com.devoteam.srit.xmlloader.core.coding.binary;

import gp.utils.arrays.Array;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public abstract class Field 
{

	protected String _name;
    protected int _length;
    protected int _offset;

    public Field(Element rootXML) {

        _name = rootXML.attributeValue("name");
        String lengthBit = rootXML.attributeValue("lengthBit");
        if (lengthBit != null) {
            this._length = Integer.parseInt(lengthBit);
        }
        else if(lengthBit == null && rootXML.attributeValue("value") != null){
            this._length = rootXML.attributeValue("value").length() * 8;
        }
        else if((rootXML.attributeValue("type")).equalsIgnoreCase("string"))
        {
            this._length=0;
        }
        else if((rootXML.attributeValue("type")).equalsIgnoreCase("binary"))
        {
            this._length=0;
        }
    }

    public int getLength() {
        return this._length;
    }

    public int getOffset() {
        return _offset;
    }

    public String getName() {
        return _name;
    }
     public void setLength(int _length) {
        this._length = _length;
    }

    public abstract String getValue(Array array)throws Exception;

    public abstract Array setValue(String value, int offset, Array array) throws Exception;
    
    public String toString(Array array) {

        StringBuilder elemString = new StringBuilder();
        elemString.append("    <field ");
        elemString.append("name=\"" + getName() + "\" ");
        try
        {
        	elemString.append("value=\"" + this.getValue(array) + "\" ");
        }
        catch (Exception e)
        {
        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
        }
        elemString.append("type=\"" + this.getClass().getSimpleName().split("Field")[0] + "\" ");
        if (!this.getClass().getName().equalsIgnoreCase("String")) {
            elemString.append("lengthBit=\"" + getLength() + "\" ");
        }
        elemString.append("/>\n");
        return elemString.toString();


    }

    public LinkedHashMap<String, Integer> getHashMapEnumByName() {
        return null;
    }
}
