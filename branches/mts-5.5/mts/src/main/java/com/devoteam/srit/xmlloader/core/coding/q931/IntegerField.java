/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;


import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class IntegerField extends Field{
    public IntegerField(Element field, ElementInformationQ931 elem, Dictionary dictionary) {
        super(field, elem, dictionary);
    }

    @Override
    public void setValue(String value, int offset, ElementInformationQ931V elemV) throws Exception {
    	_offset = offset;
        try{
	    	elemV.getFieldsArray().setBits(offset, getLength(), Integer.parseInt(value));
	    }catch(Exception e){
        	throw new ExecutionException("ISDN layer : The value \"" + value + "\" for the integer field : \"" + getName() + "\" is not valid.", e);            	            	
        }
    }
    
    @Override
    public String getValue(ElementInformationQ931V elemV) throws Exception {       
       return Integer.toString(elemV.getFieldsArray().getBits(getOffset(), getLength()));
    }
   
}
