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

package com.devoteam.srit.xmlloader.core.coding.text;

import java.util.Iterator;
import java.util.Vector;

import com.devoteam.srit.xmlloader.core.Parameter;

public class Header{
	
	//--- attribut --- //
	private Object name = null;
		
	//--- attribut --- //
	private Vector<String> headers;
	
	private MsgParser parser;
	
	// --- construct --- //
	public Header(Object name){
		this.headers = new Vector<String>();
		this.name = name;
	}	
	// --- get/set attribut --- //
	public void setHeader(int index, String value){
		this.headers.set(index, value);
		parser = null;
	}	
	// --- get/set attribut --- //
	public void addHeader(String val){
		// delete the " character at the begining and at the end
		if (val != null && val.length() > 0)
		{    				
	    	if (val.charAt(0) == '"')
	    	{
		    	int iPos = val.indexOf('"', 1);
		    	if (iPos == val.length() - 1)
		    	{
		    		val = val.substring(1);
		    		val = val.substring(0, iPos - 1);
		    	}
	    	}
		}		
		this.headers.add(val);
	}
	public String getHeader(int index){
		if ((index >= 0) && (index < this.headers.size()))
		{
			return this.headers.elementAt(index);
		}
		else
		{
			return null;
		}
			
	}

	public int getSize() {
		return headers.size();
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}
	
	public Header parseParameter(String name, String delimitor, char separator, String... escapeSeq) {
		if (parser == null)
		{
			this.parser = new MsgParser(); 
			parser.parseHeader(this, delimitor, separator, escapeSeq);			
		}
		Header header = parser.getHeader(name);
		if (header != null)
		{
			return header;
		} 
		else
		{
			return new Header(name);			
		}
	}
	// --- public methods --- //
	public void parseMultiValue(String delimitor, String... escapeSeq){
		Vector<String> list = new Vector<String>(); 
		for (int i = 0; i < headers.size(); i++)
		{
	    	String str = getHeader(i);
	    	MsgParser.split(list, str, delimitor, true, escapeSeq);
		}
		headers = list;
	}

	/*
     * 	Add a header into Parameter
     * 
     */
    public void addHeaderIntoParameter(Parameter var, Object name) throws Exception
    {
		if (name != null)
		{
	    	String val = "";
	    	for (int i = 0; i < headers.size(); i++)
	    	{
    			val += headers.get(i) + ',';
	    	}
	    	val = val.substring(0, val.length() - 1);
	    	var.add(name.toString() + ": " + val);
		}
    }

}
