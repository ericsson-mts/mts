/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import gp.utils.arrays.Array;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class BooleanField extends Field {

    public BooleanField(Element field, ElementInformationQ931 elem, Dictionary dictionary) {
        super(field, elem, dictionary);
    }

    @Override
    public void setValue(String value, int offset, ElementInformationQ931V elemV) throws Exception {
    	_offset = offset; 
        elemV.getFieldsArray().setBit(getOffset(), Integer.parseInt(value));
    }

    @Override
    public String getValue(ElementInformationQ931V elemV) throws Exception {
        return Integer.toString(elemV.getFieldsArray().getBits(getOffset(), getLength()));
    }
}
