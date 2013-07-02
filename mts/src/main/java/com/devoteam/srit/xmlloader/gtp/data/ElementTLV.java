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

package com.devoteam.srit.xmlloader.gtp.data;

import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementTLV extends ElementAbstract
{

    public ElementTLV()
    {
    	
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
    {
    	this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
    	int length;
    	/**
         * SPECIAL CASE FOR GTPv1 I.E 141 ExtensionHeaderTypeList => length of length field is 1 byte and not 2.
         */
    	if (this.tag == 141)
    	{
    		length = new Integer08Array(array.subArray(1, 1)).getValue();
    		this.fieldsArray = new SupArray();
            this.fieldsArray.addFirst(array.subArray(2, length));
            return length + 2;
    	}
    	else
    	{
    		length = new Integer16Array(array.subArray(1, 2)).getValue();
    		this.fieldsArray = new SupArray();
            this.fieldsArray.addFirst(array.subArray(3, length));
            return length + 3;
    	}
    }

	@Override
    public SupArray encodeToArray() 
	{
        SupArray sup = new SupArray();
        Integer08Array idArray = new Integer08Array(this.tag);
        sup.addLast(idArray);
        /**
         * SPECIAL CASE FOR GTPv1 I.E 141 ExtensionHeaderTypeList => length of length field is 1 byte and not 2.
         */
        if (this.tag == 141)
        {
        	Integer08Array lengthArray = new Integer08Array(this.fieldsArray.length);
        	sup.addLast(lengthArray);
        }
        else
        {
        	Integer16Array lengthArray = new Integer16Array(this.fieldsArray.length);
        	sup.addLast(lengthArray);
        }
		
	    sup.addLast(this.fieldsArray);
	    
        return sup;
    }

    
}
