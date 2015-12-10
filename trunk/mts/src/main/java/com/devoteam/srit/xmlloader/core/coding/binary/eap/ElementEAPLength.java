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

    public ElementEAPLength()
    {
    	
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
        this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
        
        if (!this.fieldsByName.isEmpty() || !this.elements.isEmpty())
        {
            int length = new Integer16Array(array.subArray(2, 2)).getValue();
            Array dataArray = array.subArray(4, length);
            decodeFieldsTagElementsFromArray(dataArray, dictionary);
            int lengthDiv4 = new Integer08Array(array.subArray(1, 1)).getValue();
            return lengthDiv4 * 4;
	    }
        
        return 1;
        
    }

	@Override    
    public SupArray encodeToArray() throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray();

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
		    // real length
		    Integer16Array lengthArray = new Integer16Array(length);
		    sup.addLast(lengthArray);			    
		    sup.addLast(this.fieldsArray);
		    sup.addLast(this.subelementsArray);
		    //int lengthPadding = (4 - length % 4) % 4;
		    int lengthPadding = (lengthDiv4 - 1) * 4 - length;
		    // padding
		    Array paddingArray = new DefaultArray(new byte[]{0});
		    for (int i = 0; i < lengthPadding;i++)
		    {
		    	sup.addLast(paddingArray);
		    }		    
        }
        
        return sup;
    }

    
}
