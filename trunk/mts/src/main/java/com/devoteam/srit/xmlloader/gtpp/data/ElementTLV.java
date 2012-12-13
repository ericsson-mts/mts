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

package com.devoteam.srit.xmlloader.gtpp.data;

import com.devoteam.srit.xmlloader.core.coding.q931.ElementAbstract;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
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
    
    public void decodeFromArray(Array array, boolean bigLength, boolean fromdata) 
    {
        if (fromdata) {
	        this._idArray = new Integer08Array(array.subArray(0, 1));
	        int length = new Integer16Array(array.subArray(1, 2)).getValue();
	        _value = array.subArray(0, length + 3);
	        _fields = _value.subArray(3);
        }
        else 
        {
        	array = new DefaultArray(getLengthElem() / 8 + 3);
	    	this._idArray = new Integer08Array(array.subArray(0, 1));
	        this._idArray.setValue(getId());
	        _value = array;
		    _fields = _value.subArray(3);
        }
    }

    public Array encodeToArray() {
        SupArray sup = new SupArray();
        sup.addLast(_idArray);
	    Integer16Array lengthArray = new Integer16Array(_fields.length);
	    sup.addLast(lengthArray);
		sup.addLast(_fields);
        return sup;
    }

    
}
