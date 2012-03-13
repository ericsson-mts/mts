/*
 * UcpXser.java
 * 
 */

package com.devoteam.srit.xmlloader.ucp.data;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

/**
 *
 * @author Benjamin Bouvier
 */
public class UcpXser extends UcpAttribute
{
    private String type = null; 

    public UcpXser()
    {}

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
       
        if (getValueQuality())
        {
            StringBuilder lg = new StringBuilder();

            array.addFirst(new DefaultArray(getType().getBytes()));
            String length = Integer.toHexString(getLength());
            if(length.length() < 2)//because lg should be on 2 octets
            {
                lg.append("0").append(length);
            }
            else
            {
                lg.append(length);
            }
            array.addLast(new DefaultArray(lg.toString().toUpperCase().getBytes()));
            array.addLast(new DefaultArray(((String)getValue()).getBytes()));
        }

        return array;
    }

    @Override
    public UcpXser clone()
    {
        UcpXser clone = new UcpXser();
        clone.setType(getType());
        clone.setLength(getLength());
        return clone;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append("Type ").append(getType()).append(", length ");
        str.append(getLength()).append(", value ").append(getValue());

        if((str.charAt(str.length()-1) != '\n') && (str.charAt(str.length()-2) != '\r'))
            str.append("\r\n");
        
        return str.toString();
    }
    
}
