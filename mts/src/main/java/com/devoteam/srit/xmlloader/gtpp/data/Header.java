/* 
 *  * Copyright 2012 Devoteam http://www.devoteam.com
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

package com.devoteam.srit.xmlloader.gtpp.data;

import java.io.InputStream;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.gtpp.GtppDictionary;

import gp.utils.arrays.Array;

/**
 *
 * @author Mohamad Bilal El Aly
 */
public abstract class Header {

	//Abstract method
	public Header clone(){ return null; };
	public abstract Array getArray()throws Exception; 
	public abstract void parseArray(InputStream stream, GtppDictionary dictionary) throws Exception;
	public abstract void parseXml(Element header, GtppDictionary dictionary) throws Exception;
	public String toString() {return null;}; 
	public abstract int getLength(); 
	public abstract int getSize();
	public abstract void setMessageType(int messageType);
	public abstract int getMessageType();
	public abstract void setName(String name);
	public abstract String getName();
	public abstract void setLength(int length); 
	public abstract int getSequenceNumber(); 
	public abstract String getVersionName();
	
}

 