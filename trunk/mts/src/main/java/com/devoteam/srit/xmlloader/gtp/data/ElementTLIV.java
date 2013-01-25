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


import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementTLIV extends ElementAbstract
{
	
    public ElementTLIV()
    {
    	
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
    {
        this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
        int length = new Integer16Array(array.subArray(1, 2)).getValue();
        this.instances = new Integer08Array(array.subArray(3, 1)).getValue();
        this._fields = new SupArray();
        this._elements = new SupArray();
        // cas when there are no field
        if (this._hashMapFields.size() != 0)
        {
            this._fields.addFirst(array.subArray(4, length));
        }
        else
        {
        	this._elements.addFirst(array.subArray(4, length));
        	elements = ElementAbstract.decodeElementsFromArray(this._elements, dictionary);
        }
        return length + 4;
    }

	@Override
    public SupArray encodeToArray() 
	{
		// encode the sub-element
		this._elements = super.encodeToArray();

        SupArray sup = new SupArray();
        Integer08Array idArray = new Integer08Array(this.tag);
        sup.addLast(idArray);
        Integer16Array lengthArray = new Integer16Array(this._fields.length + this._elements.length);
        sup.addLast(lengthArray);
	    Integer08Array instancesArray = new Integer08Array(this.instances);
	    sup.addLast(instancesArray);
	    
	    sup.addLast(this._fields);
	    sup.addLast(this._elements);
	    
        return sup;
    }

    
}
