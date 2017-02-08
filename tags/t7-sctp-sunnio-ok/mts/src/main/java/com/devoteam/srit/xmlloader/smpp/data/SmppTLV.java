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

package com.devoteam.srit.xmlloader.smpp.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.TLV;
import gp.utils.arrays.*;

/**
 *
 * @author Benjamin Bouvier
 */
public class SmppTLV extends TLV
{
    private SmppAttribute att = new SmppAttribute();

    public SmppTLV()
    {}

    protected SmppAttribute getAtt() {
        return att;
    }

    protected void setAtt(SmppAttribute att) {
        this.att = att;
    }
    
    @Override
    public void setValue(Object value) throws Exception {
        att.setValue(value);
    }

    @Override
    public Object getValue() {
        return att.getValue();
    }

    @Override
    public boolean getValueQuality() {
        return att.getValueQuality();
    }

    @Override
    public void setValueQuality(boolean quality) {
        att.setValueQuality(quality);
    }

    @Override
    public String getFormat() {
        return att.getFormat();
    }

    @Override
    public void setFormat(String format) {
        att.setFormat(format);
    }

    @Override
    public String getName() {
        return att.getName();
    }

    @Override
    public void setName(String name) {
        att.setName(name);
    }

    @Override
    public int getLength() {
        return att.getLength();
    }

    @Override
    public void setLength(int length) {
        att.setLength(length);
    }

    @Override
    public int getSizeMin() {
        return att.getSizeMin();
    }

    @Override
    public void setSizeMin(int sizeMin) {
        att.setSizeMin(sizeMin);
    }

    @Override
    public int getSizeMax() {
        return att.getSizeMax();
    }

    @Override
    public void setSizeMax(int sizeMax) {
        att.setSizeMax(sizeMax);
    }

    @Override
    public String getOccurence() {
        return att.getOccurence();
    }

    @Override
    public void setOccurence(String occurence) {
        att.setOccurence(occurence);
    }

    @Override
    public String getOccurenceAttribute() {
        return att.getOccurenceAttribute();
    }

    @Override
    public void setOccurenceAttribute(String value) {
        att.setOccurenceAttribute(value);
    }

    @Override
    public boolean isMandatory() {
        return att.isMandatory();
    }

    @Override
    public void setMandatory(boolean mandatory) {
        att.setMandatory(mandatory);
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
        if(isMandatory() || getValueQuality())
        {
            array.addFirst(new Integer16Array(getTag()));
            array.addLast(new Integer16Array(getLength()));

            if(getValue() instanceof Integer)//for imbricate attribute or list of xser
            {
                switch(getLength())
                {
                    case 1:
                        array.addLast(new Integer08Array(((Integer)getValue())));
                        break;
                    case 2:
                        array.addLast(new Integer16Array(((Integer)getValue())));
                        break;
                    case 4:
                        array.addLast(new Integer32Array(((Integer)getValue())));
                        break;
                    default:
                        throw new Exception("size " + getLength() + " of tlv named <" + getName() + "> for int don't exists, format is " + getFormat());
                }
            }
            else if(getValue() instanceof String)
            {
                array.addLast(new DefaultArray(((String)getValue()).getBytes()));
            }
            else if(getValue() instanceof Array)
            {
                array.addLast((Array)getValue());
            }
            else
            {
                throw new Exception("Value was not set for tlv named <" + getName() + ">");
            }
        }
        return array;
    }

    @Override
    public SmppTLV clone()
    {
        SmppTLV clone = new SmppTLV();

        clone.setLength(getLength());
        clone.setName(getName());
        clone.setTag(getTag());
        clone.setFormat(getFormat());
        clone.setSizeMin(getSizeMin());
        clone.setSizeMax(getSizeMax());
        clone.setMandatory(isMandatory());
        return clone;
    }

}
