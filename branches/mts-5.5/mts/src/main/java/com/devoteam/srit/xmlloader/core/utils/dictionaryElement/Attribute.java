/*
 * Attribute.java
 * 
 */

package com.devoteam.srit.xmlloader.core.utils.dictionaryElement;

import gp.utils.arrays.*;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public abstract class Attribute
{
    protected String      name     = null;
    protected int         length  = -1;
    protected Object      value = null;
    protected boolean     value_q = false;
    protected String      format   = "";
    protected int         sizeMin  = 0;
    protected int         sizeMax  = 0;
    protected String      occurence = null;
    protected String      occurenceAttribute = null;
    protected boolean     mandatory = false;
    protected boolean     notApplicable = false;

    public Attribute()
    {}

    public abstract void setValue(Object value) throws Exception;

    public Object getValue() {
        return value;
    }
    
    public boolean getValueQuality() {
        return value_q;
    }

    public void setValueQuality(boolean quality)
    {
        this.value_q = quality;
    }
    
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSizeMin() {
        return sizeMin;
    }

    public void setSizeMin(int sizeMin) {
        this.sizeMin = sizeMin;
    }

    public int getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(int sizeMax) {
        this.sizeMax = sizeMax;
    }

    public String getOccurence() {
        return occurence;
    }

    public void setOccurence(String occurence) {
        this.occurence = occurence;
    }

    public String getOccurenceAttribute()
    {
        return occurenceAttribute;
    }
    
    public void setOccurenceAttribute(String value) {
        this.occurenceAttribute = value;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean getNotApplicable() {
        return notApplicable;
    }

    public void setNotApplicable(boolean notApplicable) {
        this.notApplicable = notApplicable;
    }
    
    public abstract Array getArray() throws Exception;

    @Override
    public abstract Attribute clone();

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
