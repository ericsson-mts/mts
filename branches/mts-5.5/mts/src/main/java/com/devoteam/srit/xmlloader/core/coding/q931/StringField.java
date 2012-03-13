/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class StringField extends Field {

    private Array array;

    public StringField(Element field, ElementInformationQ931 elem, Dictionary dictionary) {
        super(field, elem, dictionary);
    }

    @Override
    public void setValue(String value, int offset, ElementInformationQ931V elemV) throws Exception {
    	_offset = offset;

        array = new DefaultArray(value.getBytes());

        if (this.dictionary.getMapElementById().get(elemV.getId()) == null) {


            for (int i = 0; i < array.length; i++) {
                elemV.getFieldsArray().set(i + getOffset() / 8, array.get(i));
            }
        }
        else {
            SupArray suparray = new SupArray();
            suparray.addLast(elemV.getFieldsArray());
            suparray.addLast(array);
            elemV.setFields(suparray);
        }
    }

    @Override
    public String getValue(ElementInformationQ931V elemV) throws Exception {
        return new String(array.getBytes());
    }
}
