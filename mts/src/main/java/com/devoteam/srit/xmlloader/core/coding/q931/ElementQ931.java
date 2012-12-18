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

package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public class ElementQ931 extends ElementAbstract
{

    public ElementQ931()
    {
    	
    }
    
    public void decodeFromArray(Array array, boolean bigLength, boolean fromdata) {
        _bigLength = bigLength;
        if (fromdata) {
	        this.id = new Integer08Array(array.subArray(0, 1)).getValue();
	        if (this._hashMapFields.size() >= 1)
            {
		        if (bigLength == true) {
		            int length = new Integer16Array(array.subArray(1, 2)).getValue();
		            _value = array.subArray(0, length + 3);
		            _fields = _value.subArray(3);
		        }
		        else {
		            int length = new Integer08Array(array.subArray(1, 1)).getValue();
		            _value = array.subArray(0, length + 2);
		            _fields = _value.subArray(2);
		        }	            	
		    }
        }
        else {
            if (this._hashMapFields.size() >= 1)
            {
		        _value = array;
		        if (bigLength) {
		            _fields = _value.subArray(3);
		        }
		        else {
		            Integer08Array length = new Integer08Array(array.subArray(1, 1));
		            length.setValue(array.length - 2);
		            _fields = _value.subArray(2);
		        }
            }
        }
    }

    
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
