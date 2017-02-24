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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.coap.ElementCOAPMessage;
import com.devoteam.srit.xmlloader.core.coding.binary.coap.ElementCOAPOption;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementMessage extends ElementAbstract
{

    public ElementMessage(ElementAbstract parent)
    {
    	super(parent);
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{		
    	// decode the message header elements
		ElementAbstract elementHeader = this.getElement(0);
		int currentLength = 0;
		if (elementHeader != null)
		{
        	currentLength = elementHeader.decodeFromArray(array, dictionary);        
        	String fieldNameForType = dictionary.getFieldNameForType();	 
        	String key = "Message";        	
        	FieldAbstract fieldType = elementHeader.getFieldsByName(fieldNameForType);
        	if (fieldType != null && fieldType.offset / 8 < array.length)
        	{	  
        		String typeValue = fieldType.getValue(array);
        		typeValue = typeValue.replace(":", "_");
        		key += "_" + typeValue;
        	}
			ElementAbstract elementMessage = dictionary.getElementByLabel(key);
			if (elementMessage != null)
			{
				elementMessage.setLabel(key);
				Array data = array.subArray(currentLength);
				currentLength += elementMessage.decodeFromArray(data, dictionary);
				this.elements.add(elementMessage);
			}
			else
			{
				//currentLength = decodeNotTagElementsFromArray(array, dictionary);
	            Array data = array.subArray(currentLength);
	        
	            List<ElementAbstract> elementsTempo = new ArrayList<ElementAbstract>(); 							
	            Iterator<ElementAbstract> iter = this.elements.iterator();
	            elementsTempo.add(iter.next());
				while (iter.hasNext() && currentLength != 0)
				{
					ElementAbstract elemInfo = iter.next();
					elemInfo = ElementAbstract.buildFactory(elemInfo.coding, this);
					int tag =  ((ElementCOAPOption) elemInfo).decodeTagFromArray(data, dictionary);
					if (tag >= 0)
					{
						ElementAbstract elemDico = dictionary.getElementByTag(tag);
						if (elemDico != null)
						{
							elemInfo.copyToClone(elemDico);
						}
						currentLength +=  elemInfo.decodeFromArray(data, dictionary);
						elementsTempo.add(elemInfo);
						data = array.subArray(currentLength);
					}
				}
				this.elements = elementsTempo;
		        this.subelementsArray = new SupArray();
		    	this.subelementsArray.addFirst(array);
			}
		}
    	if (array.length > currentLength)
    	{
        	//decode the message for tag elements
            Array data = array.subArray(currentLength);
	        this.subelementsArray = new SupArray();
	    	this.subelementsArray.addFirst(data);
	    	List<ElementAbstract> elementsRead = ElementAbstract.decodeTag1OctetElementsFromArray(data, dictionary);
	    	this.elements.addAll(elementsRead);
    	}
        return array.length;        
    }

	@Override    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray(dictionary);
        
        SupArray sup = new SupArray();
    	int length = this.fieldsArray.length + this.subelementsArray.length;
    	ElementAbstract elementHeader = getElement(0);
    	String fieldNameForLength = dictionary.getFieldNameForLength();
    	if (elementHeader != null)
    	{
    		FieldAbstract fieldLength = elementHeader.getFieldsByName(fieldNameForLength);
	    	if (fieldLength != null)
	    	{
	    		fieldLength.setValue(Integer.toString(length), fieldLength.offset, elementHeader.fieldsArray);
	    	}
    	}
	    sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);
	    
        return sup;
    }

    
}
