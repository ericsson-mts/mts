/*
 * UcpAttribute.java
 * 
 */

package com.devoteam.srit.xmlloader.ucp.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.core.utils.gsm.GSMConversion;
import gp.utils.arrays.*;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class UcpAttribute extends Attribute
{        
    public UcpAttribute()
    {} 

    @Override
    public void setValue(Object value) throws Exception {
        this.value = value;
        value_q = true;
    }
    
    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        if(getValueQuality())
        {
            if(getValue() instanceof Vector)//for imbricate attribute or list of xser
            {
                UcpAttribute att = null;
                for(int i = 0; i < ((Vector<UcpAttribute>)getValue()).size(); i++)
                {
                    att = ((Vector<UcpAttribute>)getValue()).get(i);
                    array.addLast(att.getArray());
                    //NO NEED to add SEP here, because for imbricate att, added just after each att value
                }
                
                if(att instanceof UcpXser)
                {
                    array.addLast(UcpMessage.SEP);
                }
            }
            else if(getValue() instanceof String)
            {
                String byteStr = new String(((String)getValue()).getBytes());
                byteStr = new String(GSMConversion.toGsmCharset(byteStr));

                if((getFormat() != null) && getFormat().equalsIgnoreCase("encodedString"))
                {
                    array.addLast(new DefaultArray(
                        DefaultArray.toHexString(
                            new DefaultArray(byteStr.getBytes())).toUpperCase()
                        .getBytes()));
                }
                else
                {
                    array.addLast(new DefaultArray(byteStr.getBytes()));
                }
                array.addLast(UcpMessage.SEP);
            }
        }
        
        return array;
    }

    @Override
    public UcpAttribute clone()
    {
        UcpAttribute clone = new UcpAttribute();

        clone.setName(getName());
        clone.setFormat(getFormat());
        clone.setLength(getLength());
        clone.setMandatory(isMandatory());
        clone.setOccurenceAttribute(getOccurenceAttribute());
        clone.setNotApplicable(getNotApplicable());
        
        if(getValueQuality())
        {
            if(getValue() instanceof Vector)
            {
                try {
                    clone.setValue(new Vector<UcpAttribute>());
                }
                catch (Exception ex)
                {}

                for(int i = 0; i < ((Vector)getValue()).size(); i++)
                {
                    ((Vector<UcpAttribute>)clone.getValue()).add(((Vector<UcpAttribute>)getValue()).get(i));
                }
            }
        }

        return clone;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append("Attribute: ").append(name);// + ", format " + format + ", length " + length;
        if(isMandatory())
        {
            str.append(", mandatory ");
        }

        if(occurenceAttribute != null)
        {
            str.append(" , occurence attribute ").append(occurenceAttribute);// + ", occurence " + occurence;
        }

        if(getValueQuality())
        {
            str.append(", value ");
            if(getValue() instanceof Vector)
                str.append("\r\n");
            str.append(getValue());
        }
        if((str.charAt(str.length()-1) != '\n') && (str.charAt(str.length()-2) != '\r'))
            str.append("\r\n");
        
        return str.toString();
    }
    
}
