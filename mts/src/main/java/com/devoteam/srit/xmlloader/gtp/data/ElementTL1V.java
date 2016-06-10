package com.devoteam.srit.xmlloader.gtp.data;

import gp.utils.arrays.Array;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;

/**
 * 
 * @author jdufour
 * This class has been created to handle particular 141 IE as its length field is encoded in one byte.
 */

public class ElementTL1V extends ElementAbstract {

	public ElementTL1V(ElementAbstract parent)
    {
    	super(parent);
    }
	
	@Override
    public int decodeFromArray(Array array, Dictionary dictionary) throws Exception
    {
    	this.tag = new Integer08Array(array.subArray(0, 1)).getValue();
    	int length = new Integer08Array(array.subArray(1, 1)).getValue();
    	
        Array data = array.subArray(2, length);
        decodeFieldsTagElementsFromArray(data, dictionary);

        return length + 2;
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
       	Integer08Array lengthArray = new Integer08Array(length);
       	sup.addLast(lengthArray);

       	sup.addLast(this.fieldsArray);
	    sup.addLast(this.subelementsArray);
	    
        return sup;
    }

}
