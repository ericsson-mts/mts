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

package com.devoteam.srit.xmlloader.core.coding.binary.eap;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.StringField;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementEAPLength extends ElementAbstract
{

    public ElementEAPLength(ElementAbstract parent)
    {
    	super(parent);
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
        this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
        
        if (!this.fieldsByName.isEmpty() || !this.elements.isEmpty())
        {
            int lengthDiv4 = new Integer08Array(array.subArray(1, 1)).getValue();
            Array elementData = array.subArray(4, (lengthDiv4 - 1) * 4);        	
            int length = new Integer16Array(array.subArray(2, 2)).getValue();
            if (length > elementData.length)
            {
            	length = elementData.length;
            }
            Array dataArray = elementData.subArray(0, length);

            // remove padding data if the last field is a type of String
            if (!this.fields.isEmpty() && this.fields.get(this.fields.size() - 1) instanceof StringField)
            {
            	int lengthPadding = ElementEAP.removePaddingBytes(dataArray);
            	length = length - lengthPadding;
            }
            dataArray = dataArray.subArray(0, length);
            
            decodeFieldsTagElementsFromArray(dataArray, dictionary);
            return lengthDiv4 * 4;
	    }
        
        return 1;
        
    }

	@Override    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray(dictionary);

        SupArray sup = new SupArray();
        Integer08Array idArray = new Integer08Array(this.tag);
        sup.addLast(idArray);
        
        if (!this.fieldsByName.isEmpty() || !this.elements.isEmpty())
        {
        	int length = this.getLengthElem() / 8;
        	if (length <= 0)
        	{
        		length = this.fieldsArray.length + this.subelementsArray.length;
        	}
        	int lengthDiv4 = (length + 3)/ 4 + 1;
        	// length divide by 4
		    Integer08Array lengthDiv4Array = new Integer08Array(lengthDiv4);		    
		    sup.addLast(lengthDiv4Array);
		    // real length in bytes
		    Integer16Array lengthArray = new Integer16Array(length);
		    sup.addLast(lengthArray);			    
		    sup.addLast(this.fieldsArray);
		    sup.addLast(this.subelementsArray);
		    // padding
		    int lengthPadding = (lengthDiv4 - 1) * 4 - length;
		    byte[] bytesPadding = ElementEAP.getPaddingBytes(lengthPadding);
		    sup.addLast(new DefaultArray(bytesPadding));
        }
        
        return sup;
    }

    
}
