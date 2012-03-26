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

import gp.utils.arrays.Array;
import gp.utils.arrays.SupArray;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public abstract class Group extends Attribute
{
    //a group is always composed of a list of attribute
    protected String choiceValue = null;

    public Group()
    {}

    public String getChoiceValue()
    {
        return choiceValue;
    }

    public void setChoiceValue(String choiceValue)
    {
        this.choiceValue = choiceValue;
    }

    @Override
    public Array getArray() throws Exception
    {
        Array array = new SupArray();

        if(getValueQuality())
        {
            for(int i = 0; i < ((Vector<Attribute>)getValue()).size(); i++)
            {
                ((SupArray)array).addLast(((Vector<Attribute>)getValue()).get(i).getArray());
            }
        }

        return array;
    }

    @Override
    public String toString()
    {
        String str = new String();

        if((((Vector<Attribute>)getValue()).size() != 0) && (((Vector<Attribute>)getValue()).get(0).getValueQuality()))
        {
            str += "For choice value : " + choiceValue + " => " + getValue();
        }

        return str.toString();
    }
    
}
