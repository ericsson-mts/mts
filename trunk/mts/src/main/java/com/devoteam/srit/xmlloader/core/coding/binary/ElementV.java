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
public class ElementV extends ElementAbstract
{

    public ElementV()
    {
    	this.coding = "V";
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
		if (!this.elements.isEmpty())
		{
			int length = super.decodeFromArray(array, dictionary);
			this.subelementsArray = new SupArray();
	        this.subelementsArray.addFirst(array.subArray(0, length));
	        return length;
		}
		else if (!this.fieldsByName.isEmpty())
		{
			int length = this.getLengthElem() / 8;
			if (length < array.length)
			{
				length = array.length;
			}
	        this.fieldsArray = new SupArray();
	        this.fieldsArray.addFirst(array.subArray(0, length));
	        return length;
		}
		return 0;
    }

	@Override    
    public SupArray encodeToArray() throws Exception
	{
		this.subelementsArray = super.encodeToArray();

        SupArray sup = new SupArray();
	    sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);
	    
        return sup;
    }

}
