/*
 * GtppTLV.java
 * 
 */

package com.devoteam.srit.xmlloader.gtpp.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.TLV;
import gp.utils.arrays.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 *
 * @author Benjamin Bouvier
 */
public class GtppTLV extends TLV
{
    private GtppAttribute att = new GtppAttribute();
    private boolean fixedLength = false;

    public GtppTLV()
    {}

    /*useful to override this method to override existing tlv with the same name or tag in a message*/
    @Override
    public boolean equals(Object tlv) {
        if((getName().equals(((GtppTLV)tlv).getName())) || (getTag() == ((GtppTLV)tlv).getTag()))
            return true;
        else
            return false;
    }

    protected GtppAttribute getAtt() {
        return att;
    }

    protected void setAtt(GtppAttribute att) {
        this.att = att;
    }

    public boolean isFixedLength() {
        return fixedLength;
    }

    public void setFixedLength(boolean fixedLength) {
        this.fixedLength = fixedLength;
    }

    @Override
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
        if(getValueQuality())
        {
            array.addFirst(new Integer08Array(getTag()));
            if(!isFixedLength())
                array.addLast(new Integer16Array(getLength()));

            if(getFormat().equals("int"))
            {
                if(getLength() == 1)
                    array.addLast(new Integer08Array((Integer)getValue()));
                else if(getLength() == 2)
                    array.addLast(new Integer16Array((Integer)getValue()));
            }
            else if(getFormat().equals("list"))
            {
                if(((LinkedList)getValue()).size() > 0)//search in attribute list
                {
                    for(int i = 0; i < ((LinkedList)getValue()).size(); i++)
                    {
                        Array ar = ((GtppAttribute)((LinkedList)getValue()).get(i)).getArray();
                        if(ar != null)
                            array.addLast(ar);
                    }
                }
            }
            else//same for ip format
                array.addLast(new DefaultArray((byte[])getValue()));
        }
        return array;
    }

    @Override
    public GtppTLV clone()
    {
        GtppTLV clone = new GtppTLV();
        clone.setAtt(new GtppAttribute());

        clone.setLength(getLength());
        clone.setName(getName());
        clone.setTag(getTag());
        clone.setFormat(getFormat());
        clone.setSizeMin(getSizeMin());
        clone.setSizeMax(getSizeMax());
        clone.setMandatory(isMandatory());
        clone.setFixedLength(isFixedLength());

        if((getValue() != null) && (getValue() instanceof LinkedList))
        {
            LinkedList/*<GtppAttribute>*/ list = (LinkedList)getValue();
            LinkedList/*<GtppAttribute>*/ cloneList = new LinkedList();
            try {
                //                for(int i = 0; i < list.size(); i++)
                //                    cloneList.add(list.get(i).clone());
                cloneLinkedList(list, cloneList);
                clone.setValue(cloneList);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return clone;
    }

    private void cloneLinkedList(LinkedList list, LinkedList newList) throws CloneNotSupportedException
    {
        for(int i = 0; i < list.size(); i++)
        {
            if(list.get(i) instanceof LinkedList)
            {
                LinkedList<GtppAttribute> cloneList = new LinkedList();
                cloneLinkedList(((LinkedList)list.get(i)), cloneList);
            }
            else
                newList.add(((GtppAttribute)list.get(i)).clone());
        }
    }

    @Override
    public String toString()
    {
        String str = new String();
        if(isMandatory() || getValueQuality())
        {
            str += "TLV: " + getName() + ", tag " + getTag() + ", length " + getLength() + ", format " + getFormat();
            
            if(isMandatory())
                str += ", mandatory";
            
            if(getFormat().equals("int"))
                str += ", value " + (Integer)getValue();
            else if(getFormat().equals("ip"))
            {
                try {
                    str += ", value " + InetAddress.getByAddress((byte[]) getValue()).getHostAddress();
                } catch (UnknownHostException ex) {
                }
            }
            else if(getFormat().equals("list"))
            {
                str += ", value\r\n";
                for(int i = 0; i < ((LinkedList<GtppAttribute>)getValue()).size(); i++)
                    str += ((LinkedList<GtppAttribute>)getValue()).get(i).toString();
            }
            else
                str += ", value " + new String((byte[])getValue());
            
            str += "\r\n";
        }
        return str;
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
