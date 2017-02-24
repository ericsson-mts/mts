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

package com.devoteam.srit.xmlloader.core.coding.binary.coap;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.StringField;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementCOAPOption extends ElementAbstract
{

    public ElementCOAPOption(ElementAbstract parent)
    {
    	super(parent);
    }

    public int decodeTagFromArray(Array array, Dictionary dictionary) throws Exception
	{
		if (array.length <= 0)
		{
			return -1;
		}

		// get the current because tag encoding is relative to the previous one
		int currentTag = 0;
		if (this.parentElement != null)
		{
			ElementCOAPMessage messageCOAP = (ElementCOAPMessage) this.parentElement;
			currentTag = messageCOAP.getCurrentTag();
		}

		Array deltaLengthArray = array.subArray(0, 1);
	    int deltaLength = new Integer08Array(deltaLengthArray).getValue();
	    int delta = deltaLength / 16; 
	    
    	if (delta <= 12)
    	{
    	    this.tag = currentTag + (deltaLength / 16);
    	}
    	else if (delta == 13)
    	{
    		Array delta1LengthArray = array.subArray(1, 1);
    	    int delta1 = new Integer08Array(delta1LengthArray).getValue();
    	    this.tag = currentTag + delta1 + (13);
    	}
    	else if (delta == 14)
    	{
    		Array delta2LengthArray = array.subArray(1, 2);
    	    int delta2 = new Integer16Array(delta2LengthArray).getValue();
    	    this.tag = currentTag + delta2 + (14) + 255;
    	}
    	else
    	{
    		this.tag = 1000000;
    	}    	    	

	    return this.tag;		
	}
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
		if (array.length <= 0)
		{
			return 0;
		}
		
		// get the current because tag encoding is relative to the previous one
		int currentTag = 0;
		if (this.parentElement != null)
		{
			ElementCOAPMessage messageCOAP = (ElementCOAPMessage) this.parentElement;
			currentTag = messageCOAP.getCurrentTag();
		}

		Array deltaLengthArray = array.subArray(0, 1);
	    int deltaLength = new Integer08Array(deltaLengthArray).getValue();
	    int delta = deltaLength / 16; 
	    
	    int length = 0;
	    length = deltaLength % 16;
	    Array dataArray;
    	if (delta <= 12)
    	{
    	    this.tag = currentTag + (deltaLength / 16);
    	    dataArray = array.subArray(1, length);
    	    length = length + 1;
    	}
    	else if (delta == 13)
    	{
    		Array delta1LengthArray = array.subArray(1, 1);
    	    int delta1 = new Integer08Array(delta1LengthArray).getValue();
    	    this.tag = currentTag + delta1 + (13);
    	    dataArray = array.subArray(2, length);
    	    length = length + 2;
    	}
    	else if (delta == 14)
    	{
    		Array delta2LengthArray = array.subArray(1, 2);
    	    int delta2 = new Integer16Array(delta2LengthArray).getValue();
    	    this.tag = currentTag + delta2 + (14) + 255;
    	    dataArray = array.subArray(3, length);
    	    length = length + 3;
    	}
    	else
    	{
    		dataArray = array.subArray(1);
    		length = array.length;
    	}    	    	
	    	  		    
	    decodeFieldsTagElementsFromArray(dataArray, dictionary);
	    
		// set the current because tag encoding is relative to the previous one
		if (this.parentElement != null)
		{
			ElementCOAPMessage messageCOAP = (ElementCOAPMessage) this.parentElement;
			messageCOAP.setCurrentTag(this.tag);
		}
	    
	    return length;
    }

	@Override    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray(dictionary);

		// get the current because tag encoding is relative to the previous one
		int currentTag = 0;
		if (this.parentElement != null)
		{
			ElementCOAPMessage messageCOAP = (ElementCOAPMessage) this.parentElement;
			currentTag = messageCOAP.getCurrentTag();
		}
		
		SupArray sup = new SupArray();
    
    	int length = this.fieldsArray.length + this.subelementsArray.length;
    	int deltaTag = this.tag - currentTag;
    	if (deltaTag <= 12)
    	{
	    	int deltaLength = (this.tag - currentTag) * 16 + length;
	        Integer08Array deltaArray = new Integer08Array(deltaLength);
	        sup.addLast(deltaArray);
    	}
    	else if (deltaTag <= 255)
    	{
	    	int deltaLength = (13) * 16 + length;
	    	Integer08Array delta1Array = new Integer08Array(deltaLength);
	    	sup.addLast(delta1Array);
	    	Integer08Array tag1Array = new Integer08Array(deltaTag - (13));
	    	sup.addLast(tag1Array);
    	}
    	else if (deltaTag <= 65535)
    	{
	    	int deltaLength = (14) * 16 + length;
	    	Integer08Array delta2Array = new Integer08Array(deltaLength);
	    	sup.addLast(delta2Array);
	    	Integer16Array tag2Array = new Integer16Array(deltaTag - (14) -255);
	    	sup.addLast(tag2Array);
    	}
    	else
    	{
	    	int deltaLength = (15) * 16 + (15);
	    	Integer08Array delta3Array = new Integer08Array(deltaLength);
	    	sup.addLast(delta3Array);
    	}    	    	
	    sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);
	    
		// set the current because tag encoding is relative to the previous one
		if (this.parentElement != null)
		{
			ElementCOAPMessage messageCOAP = (ElementCOAPMessage) this.parentElement;
			messageCOAP.setCurrentTag(this.tag);
		}
		
	    return sup;
    }

}
