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

package com.devoteam.srit.xmlloader.core.utils.dictionaryElement;

/**
 *
 * @author Benjamin Bouvier
 */
public abstract class TLV extends Attribute
{
    private int tag = -1;
            
    public TLV()
    {}

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    @Override
    public String toString()
    {
        String str = new String();
        if(isMandatory() || getValueQuality())
        {
            str += "TLV: " + getName() + ", tag " + getTag() + ", length " + getLength() + ", format " + getFormat()
                   + ", sizeMin " + getSizeMin()  + ", sizeMax " + getSizeMax();
            
            if(isMandatory())
                str += ", mandatory";
            
            if(getValueQuality())
                str += ", value " + getValue();
            
            str += "\r\n";
        }
        return str;
    }

}
