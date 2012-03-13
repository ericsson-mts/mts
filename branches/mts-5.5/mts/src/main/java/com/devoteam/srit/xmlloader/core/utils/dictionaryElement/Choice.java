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
