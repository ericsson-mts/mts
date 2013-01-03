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

package com.devoteam.srit.xmlloader.core.coding.binary.q931;

import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementQ931 extends ElementAbstract
{

    public ElementQ931()
    {
    	
    }
    
	@Override
    public void decodeFromArray(Array array, boolean bigLength) {
        _bigLength = bigLength;
        this.id = new Integer08Array(array.subArray(0, 1)).getValue();
        if (this._hashMapFields.size() >= 1)
        {
	        if (bigLength == true) {
	            int length = new Integer16Array(array.subArray(1, 2)).getValue();
	            this._fields = new SupArray();
	            this._fields.addFirst(array.subArray(0, length + 3).subArray(3));
	        }
	        else {
	            int length = new Integer08Array(array.subArray(1, 1)).getValue();
	            this._fields = new SupArray();
	            this._fields.addFirst(array.subArray(0, length + 2).subArray(2));
	        }	            	
	    }
    }

	@Override    
    public Array encodeToArray() {
        SupArray sup = new SupArray();
        Integer08Array idArray = new Integer08Array(this.id);
        sup.addLast(idArray);
        if (_fields != null)
        {
		    Integer08Array length8 = new Integer08Array(_fields.length);
		    if (length8.getValue() != 0)
		    {
		    	if (_bigLength) {
		    		sup.addLast(new Integer16Array(_fields.length));
		    	}
		    	else {
		    		sup.addLast(length8);
		    	}        
		    	sup.addLast(_fields);
		    }
        }
        return sup;
    }

    
}
