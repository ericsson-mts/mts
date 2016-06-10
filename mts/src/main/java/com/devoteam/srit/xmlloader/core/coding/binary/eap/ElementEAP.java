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
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.SupArray;


/**
 *
 * @author Fabien Henry
 */
public class ElementEAP extends ElementAbstract
{

    public ElementEAP(ElementAbstract parent)
    {
    	super(parent);
    }
    
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
	{
        this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
        
	    int lengthDiv4 = new Integer08Array(array.subArray(1, 1)).getValue();
	    int length = lengthDiv4 * 4 - 2;
	    Array data = array.subArray(2, length);
	    
	    // remove padding data if the last field is a type of String
	    if (!this.fields.isEmpty() && this.fields.get(this.fields.size() - 1) instanceof StringField)
	    {
	        int lengthPadding = ElementEAP.removePaddingBytes(data);
	        length = length - lengthPadding; 
	    }
	    Array dataArray = data.subArray(0, length);
	
	    decodeFieldsTagElementsFromArray(dataArray, dictionary);            
	    return lengthDiv4 * 4;        
    }

	@Override    
    public SupArray encodeToArray(Dictionary dictionary) throws Exception
	{
		// encode the sub-element
		this.subelementsArray = super.encodeToArray(dictionary);

        SupArray sup = new SupArray();
        Integer08Array idArray = new Integer08Array(this.tag);
        sup.addLast(idArray);
        
    	int length = this.fieldsArray.length + this.subelementsArray.length;
    	int lengthDiv4 = (length + 1) / 4 + 1;
	    Integer08Array lengthDiv4Array = new Integer08Array(lengthDiv4);
	    sup.addLast(lengthDiv4Array);		    
	    sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);

	    // padding		    
	    int lengthPadding = (lengthDiv4 - 1) * 4 + 2 - length;
	    byte[] bytesPadding = getPaddingBytes(lengthPadding);
	    sup.addLast(new DefaultArray(bytesPadding));
	    
        return sup;
    }

	/*
	 * get padding array
	 */
    protected static byte[] getPaddingBytes(int lengthPadding)
	{
	    byte[] bytes = new byte[lengthPadding];
	    for (int i = 0; i < lengthPadding; i++)
	    {
	    	bytes[i] = 0;
	    }
	    return bytes;
	}
	
	/*
	 * remove padding data
	 */
    protected static int removePaddingBytes(Array data)
    {
	    int i = data.length - 1;
	    int lengthPadding = 0;
	    if (data.length > 0)
	    {
		    while (i >= 0 && data.get(i) == 0)
		    {
		    	lengthPadding++;
		    	i--;
		    }
		    if (i == 0)
		    {
		    	return 0;
		    }
	    }
	    return lengthPadding;
    }
}
