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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.q931.ElementAbstract;
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
public class ElementTLIV extends ElementAbstract
{

	// protected int spare;
	protected int instances;
	
    public ElementTLIV()
    {
    	
    }
    
    public void decodeFromArray(Array array, boolean bigLength, boolean fromdata) 
    {
        if (fromdata) {
	        this._idArray = new Integer08Array(array.subArray(0, 1));
	        int length = new Integer16Array(array.subArray(1, 2)).getValue();
	        this.instances = new Integer08Array(array.subArray(3, 1)).getValue();
	        _value = array.subArray(0, length + 4);
	        _fields = _value.subArray(4);
        }
        else 
        {
	    	this._idArray = new Integer08Array(array.subArray(0, 1));
	        this._idArray.setValue(getId());
	        this.instances = new Integer08Array(array.subArray(3, 1)).getValue();
	        _value = array;
		    _fields = _value.subArray(4);
        }
    }

    public Array encodeToArray() {
        SupArray sup = new SupArray();
        sup.addLast(_idArray);
	    Integer16Array lengthArray = new Integer16Array(_fields.length);
	    sup.addLast(lengthArray);
	    Integer08Array instancesArray = new Integer08Array(this.instances);
	    sup.addLast(instancesArray);
		sup.addLast(_fields);
        return sup;
    }

    
}
