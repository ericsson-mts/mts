/*
 * UcpAttribute.java
 * 
 */

package com.devoteam.srit.xmlloader.ucp.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Group;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class UcpGroup extends Group
{
    private UcpAttribute att = new UcpAttribute();

    public UcpGroup()
    {}

    public UcpAttribute getAtt() {
        return att;
    }

    public void setAtt(UcpAttribute att) {
        this.att = att;
    }

    @Override
    public UcpGroup clone()
    {
        UcpGroup clone = new UcpGroup();
        clone.setAtt(new UcpAttribute());

        clone.setName(getName());
        clone.setFormat(getFormat());
        clone.setLength(getLength());
        clone.setMandatory(isMandatory());
        clone.setValueQuality(getValueQuality());
        clone.setChoiceValue(getChoiceValue());
        
        if(getValueQuality())
        {
            try
            {
                clone.setValue(new Vector<UcpAttribute>());
                for(int i = 0; i < ((Vector<UcpAttribute>)getValue()).size(); i++)
                    ((Vector<UcpAttribute>)clone.getValue()).add(((Vector<UcpAttribute>)getValue()).get(i).clone());
            }
            catch(Exception ex)
            {
                System.out.println("Exception in clone of value for UcpGroup");
                ex.printStackTrace();
            }
        }
        return clone;
    }

    @Override
    public void setValue(Object value) throws Exception {
        att.setValue(value);
    }

    @Override
    public Object getValue() {
        return att.getValue();
    }

    @Override
    public boolean getValueQuality() {
        return att.getValueQuality();
    }

    @Override
    public void setValueQuality(boolean quality) {
        att.setValueQuality(quality);
    }

    @Override
    public String getFormat() {
        return att.getFormat();
    }

    @Override
    public void setFormat(String format) {
        att.setFormat(format);
    }

    @Override
    public String getName() {
        return att.getName();
    }

    @Override
    public void setName(String name) {
        att.setName(name);
    }

    @Override
    public int getLength() {
        return att.getLength();
    }

    @Override
    public void setLength(int length) {
        att.setLength(length);
    }

    @Override
    public int getSizeMin() {
        return att.getSizeMin();
    }

    @Override
    public void setSizeMin(int sizeMin) {
        att.setSizeMin(sizeMin);
    }

    @Override
    public int getSizeMax() {
        return att.getSizeMax();
    }

    @Override
    public void setSizeMax(int sizeMax) {
        att.setSizeMax(sizeMax);
    }

    @Override
    public String getOccurence() {
        return att.getOccurence();
    }

    @Override
    public void setOccurence(String occurence) {
        att.setOccurence(occurence);
    }

    @Override
    public String getOccurenceAttribute() {
        return att.getOccurenceAttribute();
    }

    @Override
    public void setOccurenceAttribute(String value) {
        att.setOccurenceAttribute(value);
    }

    @Override
    public boolean isMandatory() {
        return att.isMandatory();
    }

    @Override
    public void setMandatory(boolean mandatory) {
        att.setMandatory(mandatory);
    }
    
}
