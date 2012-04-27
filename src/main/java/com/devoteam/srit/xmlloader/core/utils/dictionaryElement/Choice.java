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
public abstract class Choice extends Attribute
{
    protected Attribute  choiceAttribute = null; //parameter on which the choice is based on

    public Choice()
    {}

    public Attribute getChoiceAttribute()
    {
        return choiceAttribute;
    }

    public void setChoiceAttribute(Attribute choiceAttribute)
    {
        this.choiceAttribute = choiceAttribute;
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
        if (getValueQuality())
        {
            for(int i = 0; i < ((Vector<Group>)getValue()).size(); i++)
            {
                Group group = (Group) ((Vector<Group>)getValue()).get(i);
                if(group.getChoiceValue().equalsIgnoreCase(getChoiceAttribute().getValue().toString()))
                {
                    array.addFirst(group.getArray());
                    break;
                }
            }
        }
        return array;
    }
    
    @Override
    public String toString()
    {
        String str = new String();
        if (getValueQuality())
        {
            str += "Choice based on " + getChoiceAttribute() + getValue();
        }

        if(!str.endsWith("\r\n"))
            str += "\r\n";

        return str;
    }
}
