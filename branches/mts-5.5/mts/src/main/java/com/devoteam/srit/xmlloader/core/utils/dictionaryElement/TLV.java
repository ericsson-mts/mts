/*
 * TLV.java
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
