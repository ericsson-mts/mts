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

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import gp.utils.arrays.*;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class SmppAttribute extends Attribute
{

    public SmppAttribute()
    {}

    public void setValue(Object value) throws Exception {
        if(value instanceof Integer)
        {
            if(getFormat().equalsIgnoreCase("INT"))
            {
                this.value = value;
                value_q = true;
            }
            else
            {
                throw new Exception("Format " + getFormat() + " cannot set INT for attribute named <" + getName() + ">");
            }
        }
        else if(value instanceof String)
        {
            if(getFormat().startsWith("C-OCTETSTRING"))
            {
                if((((String)value).length() <= (getSizeMax() - 1)) && ((((String)value).length() + 1) >= getSizeMin()))//-1 and +1 because a \0 is put by default at the end of string
                {
                    this.value = value + "\0";
                    value_q = true;
                }
                else
                {
                    throw new Exception("Size " + (((String)value).length() + 1) + " of C-OCTETSTRING \"" + value + "\0\" must be >= " + getSizeMin()  + " and <= " + getSizeMax() + "for attribute " + getName());
                }
            }
            else
            {
                throw new Exception("Format " + getFormat() + " cannot set C-OCTETSTRING for attribute named <" + getName() + ">");
            }
        }
        else if(value instanceof Array)
        {
            if(getFormat().equalsIgnoreCase("OCTETSTRING"))
            {
                this.value = new SupArray();
                ((SupArray)getValue()).addFirst((Array) value);
                value_q = true;
            }
            else
            {
                throw new Exception("Format " + getFormat() + " cannot set OCTETSTRING for attribute named <" + getName() + ">");
            }
        }
        else//for other value like vector
        {
            this.value = value;
            value_q = true;
        }
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        if(getValueQuality())
        {
            if(getValue() instanceof Integer)//for imbricate attribute or list of xser
            {
                switch(getSizeMax())
                {
                    case 1:
                        array.addFirst(new Integer08Array((Integer)getValue()));
                        break;
                    case 2:
                        array.addFirst(new Integer16Array(((Integer)getValue())));
                        break;
                    case 4:
                        array.addFirst(new Integer32Array(((Integer)getValue())));
                        break;
                    default:
                        throw new Exception("size " + getSizeMax() + " of attribute named <" + getName() + "> for int don't exists, format is " + getFormat());
                }
            }
            else if(getValue() instanceof String)
            {
                array.addFirst(new DefaultArray(((String)getValue()).getBytes()));
            }
            else if(getValue() instanceof Array)
            {
                array.addFirst((Array)getValue());
            }
            else if(getValue() instanceof Vector)//for occurence vector containing imbricate attribute
            {
                for(int i = 0; i < ((Vector)getValue()).size(); i++)//occurence vector
                {
                    for(int j = 0; j < ((Vector)((Vector)getValue()).get(i)).size(); j++)//imbricate attribute or choice vector
                    {
                        array.addLast(((Vector<Attribute>)((Vector)getValue()).get(i)).get(j).getArray());
                    }
                }
            }
        }
        return array;
    }

    @Override
    public SmppAttribute clone()
    {
        SmppAttribute clone = new SmppAttribute();
        clone.setName(getName());
        clone.setFormat(getFormat());
        clone.setSizeMin(getSizeMin());
        clone.setSizeMax(getSizeMax());
        clone.setOccurenceAttribute(getOccurenceAttribute());

        if(getValueQuality())
        {
            if(getValue() instanceof Vector)//for occurence attribute
            {
                try {
                    clone.setValue(new Vector());
                }
                catch (Exception ex)
                {}

                for(int i = 0; i < ((Vector)getValue()).size(); i++)//run through occurence vector
                {
                    if(((Vector)getValue()).get(i) instanceof Vector)
                    {
                        Vector vec = (Vector) ((Vector)getValue()).get(i);
                        //if it is a vector like for multiple occurence, run through it to clone
                        ((Vector)clone.getValue()).add(new Vector());
                        //run through vector to clone all value
                        for(int j = 0; j < vec.size(); j++)
                        {
                            ((Vector)((Vector)clone.getValue()).get(i)).add(((Attribute)vec.get(j)).clone());
                        }
                    }
                }
            }
        }

        return clone;
    }

    @Override
    public String toString()
    {
        String str = new String();

        str += "Attribute: " + name + ", format " + format + ", sizeMin " + sizeMin  + ", sizeMax " + sizeMax;

        if(getValueQuality())
        {
            str += ", value ";
            if(getValue() instanceof Vector)
                str += "\r\n";
            str += getValue();
        }

        if(occurenceAttribute != null)
        {
            str += ", occurence attribute <" + occurenceAttribute + ">";
        }

        if(!str.endsWith("\r\n"))
            str += "\r\n";
        
        return str;
    }
    
}
