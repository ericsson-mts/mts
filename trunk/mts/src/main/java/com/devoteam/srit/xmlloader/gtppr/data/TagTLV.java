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

package com.devoteam.srit.xmlloader.gtppr.data;

import com.devoteam.srit.xmlloader.gtppr.GtppDictionary;

import gp.utils.arrays.*;

import java.util.LinkedList;

/**
 *
 * @author Benjamin Bouvier
 */
public class TagTLV extends Tag
{
    
    public TagTLV()
    {}

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
        if(getValueQuality())
        {
            array.addFirst(new Integer08Array(getTag()));
            if(!isFixedLength())
                array.addLast(new Integer16Array(getLength()));

            if(getFormat().equals("int"))
            {
                if(getLength() == 1)
                    array.addLast(new Integer08Array((Integer)getValue()));
                else if(getLength() == 2)
                    array.addLast(new Integer16Array((Integer)getValue()));
                else if(getLength() == 4)
                    array.addLast(new Integer32Array((Integer)getValue()));
                else if(getLength() == 8)
                    array.addLast(new Integer64Array((Integer)getValue()));                
            }
            else if(getFormat().equals("list"))
            {
                if(((LinkedList)getValue()).size() > 0)//search in attribute list
                {
                    for(int i = 0; i < ((LinkedList)getValue()).size(); i++)
                    {
                        Array ar = ((GtppAttribute)((LinkedList)getValue()).get(i)).getArray();
                        if(ar != null)
                            array.addLast(ar);
                    }
                }
            }
            else//same for ip format
                array.addLast(new DefaultArray((byte[])getValue()));
        }
        return array;
    }
    
    @Override
    public int parseArray(Array array, int index, GtppDictionary dictionary) throws Exception
    {
        if(!isFixedLength()) {
            setLength(new Integer16Array(array.subArray(index, 2)).getValue());
            index += 2;
        }
        //then get value or length
        Array value = array.subArray(index, getLength());
        index += getLength();
        if(getFormat().equals("int"))
        {
            if(getLength() == 1)
                setValue(new Integer08Array(value).getValue());
            else if (getLength() == 2)
                setValue(new Integer16Array(value).getValue());
            else if (getLength() == 4)
                setValue(new Integer32Array(value).getValue());
            else if (getLength() == 8)
                setValue(new Integer64Array(value).getValue());
        }
        else if(getFormat().equals("list"))
        {
            parseLinkedList(value, this, 0);
        }
        else
        {
            setValue(value.getBytes());
        }
        return index;
    }
    
    @Override
     public Tag clone()
    {
        Tag clone = new TagTLV();
        clone.copyFrom(this);        
        return clone;
    }
 
}
