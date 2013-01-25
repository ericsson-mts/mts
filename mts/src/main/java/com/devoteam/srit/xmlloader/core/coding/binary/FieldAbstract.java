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
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import org.dom4j.Element;


/**
 *
 * @author indiaye
 */
public abstract class FieldAbstract 
{

	protected String _name;
    protected int _length;

	protected int _offset;

    public FieldAbstract() 
    {
    	this._name = "";
    	this._length = 0;
    	this._offset = 0;
	}

    public FieldAbstract(Element rootXML) 
    {
        _name = rootXML.attributeValue("name");
        this._length = 0;
        String lengthBit = rootXML.attributeValue("lengthBit");
        if (lengthBit != null) 
        {
            this._length = Integer.parseInt(lengthBit);
        }
    }
    
    public abstract String getValue(Array array)throws Exception;

    public abstract void setValue(String value, int offset, SupArray array) throws Exception;
    
    public abstract FieldAbstract clone();
    
    protected void copyToClone(FieldAbstract source) 
    {
    	this._name = source._name;
    	this._length = source._length;
    	this._offset = source._offset;
    }
    
    public String toString(Array array) {

        StringBuilder elemString = new StringBuilder();
        elemString.append("    <field ");
        elemString.append("name=\"" + this._name + "\" ");
        try
        {
        	elemString.append("value=\"" + this.getValue(array) + "\" ");
        }
        catch (Exception e)
        {
        	// GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Exception in toString() method for field " + this._name);
        	// nothing to do 
        }
        elemString.append("type=\"" + this.getClass().getSimpleName().split("Field")[0] + "\" ");
        elemString.append("lengthBit=\"" + this._length + "\" ");
        elemString.append("/>\n");
        return elemString.toString();


    }

}
