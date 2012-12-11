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

import java.io.InputStream;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public abstract class HeaderAbstract {
    
    protected Dictionary dictionary; 

    protected int length;
    
    protected String syntax;
    
    public abstract boolean isRequest();
    public abstract String getType();
    public abstract String getSyntax();
    
    public abstract void parseFromXML(Element header, Dictionary dictionary) throws Exception;
    public abstract String toXML();
    
    public abstract void decodeFromArray(Array data, String syntax, Dictionary dictionary) throws Exception;
	public abstract void decodeFromStream(InputStream stream, Dictionary dictionary) throws Exception;
    public abstract Array encodeToArray();

    public abstract void getParameter(Parameter var, String param);
    
    public String toString()
    {
    	return toXML();
    }

    public int getLength() 
    {
		return this.length;
	}
    
    public void setLength(int length)
    {
    	this.length = length;
    }
}
