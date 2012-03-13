/*
 * SmppAttribute.java
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
