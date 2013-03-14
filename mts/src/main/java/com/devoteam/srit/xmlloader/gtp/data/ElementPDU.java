package com.devoteam.srit.xmlloader.gtp.data;

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.binary.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class ElementPDU extends ElementAbstract {

	@Override
	public int decodeFromArray(Array array, Dictionary dictionary)
			throws Exception {
		this.tag = 255;
        this.fieldsArray = new SupArray();
        this.fieldsArray.addFirst(array);
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toXml() 
    {
        return Utils.byteTabToString(this.fieldsArray.getBytes());
    }
	
	@Override
	public String toString() 
    {
    	return toXml();
    }

	@Override
	 public void getParameter(Parameter var, String[] params, String path, int offset, Dictionary dictionary) throws Exception 
	    {
			var.add(Array.toHexString(fieldsArray));
	    }
	
}
