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

package com.devoteam.srit.xmlloader.gtppr.data;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.TLV;
import com.devoteam.srit.xmlloader.gtppr.GtppDictionary;

import gp.utils.arrays.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 *
 * @author Benjamin Bouvier
 */
public abstract class Tag extends TLV
{
    private GtppAttribute att = new GtppAttribute();
    private boolean fixedLength = false;

	// public Tag clone(){ return null; };
	public abstract Array getArray() throws Exception; 
    public abstract int parseArray(Array array, int index, GtppDictionary dictionary) throws Exception;
    public abstract Tag clone();
	// public abstract void parseXml(Element header, GtppDictionary dictionary) throws Exception;
	// public String toString() {return null;}; 
    
    public Tag()
    {}

    /*useful to override this method to override existing tlv with the same name or tag in a message*/
    @Override
    public boolean equals(Object tlv) {
        if((getName().equals(((Tag)tlv).getName())) || (getTag() == ((Tag)tlv).getTag()))
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
    
    protected int parseLinkedList(Array valueToDecode, Attribute att, int index) throws Exception
    {
        LinkedList<Object> list = (LinkedList<Object>)att.getValue();
        int lastSize = 0;
        while(index < valueToDecode.length)
        {
            for(int i = lastSize; i < list.size(); i++)
            {
                if(index < valueToDecode.length)
                {
                    GtppAttribute att2 = (GtppAttribute)list.get(i);
                    if(att2.getFormat().equals("list"))
                    {
                        index += parseLinkedList(valueToDecode, att2, index);//recursive call
                    }
                    else if(att2.getFormat().equals("int"))
                    {
                        int lg = att2.getLength();
                        if(lg == 1)
                            att2.setValue(new Integer08Array(valueToDecode.subArray(index, lg)).getValue());
                        else if(lg == 2)
                            att2.setValue(new Integer16Array(valueToDecode.subArray(index, lg)).getValue());
                        else if(lg == 4)
                            att2.setValue(new Integer32Array(valueToDecode.subArray(index, lg)).getValue());
                        else if(lg == 8)
                            att2.setValue(new Integer64Array(valueToDecode.subArray(index, lg)).getValue());                        
                        index += lg;
                    }
                    else if(att2.getValueQuality() || att2.getLength() == -1)//so based on the latest attribute
                    {
                        if(att2.getValueQuality())//duplicate att because already set
                        {
                            GtppAttribute duplicateAtt = att2.clone();
                            att2 = duplicateAtt;
                            list.add(att2);
                        }

                        att2.setLength((Integer)((GtppAttribute)list.get(i-1)).getValue());
                        att2.setValue(new DefaultArray(valueToDecode.getBytes(), index, att2.getLength()).getBytes());
                        index += att2.getLength();
                        //pb here to analyze
                    }
                }
                else
                {
                    System.out.println("index > valueTodecode.length");
                    break;
                }
            }
            lastSize = list.size();
            if(index < valueToDecode.length)//if the list is end, but we don't have decode all data=> duplicate last 2 attributes for data records and continue to decode
            {
                if(att.getName().equals("dataRecords"))//for data records clone the last 2 attributes
                {
                    //duplicate last 2 Attributes
                    list.add(((GtppAttribute)list.get(list.size() - 2)).clone());//size - 2 to clone length
                    list.add(((GtppAttribute)list.get(list.size() - 2)).clone());//anothert time size - 2 because just above, one other att added, so clone record
                    ((GtppAttribute)list.get(list.size() - 1)).setLength(-1);//reset lenght of last attribute dataRecords
                }
                else//else clone just the last attribute when attribute is a list of a lot of the same attribute
                    list.add(((GtppAttribute)list.get(list.size() - 1)).clone());
            }
        }
        return index;
    }

     protected void copyFrom(Tag from)
     {
        setAtt(new GtppAttribute());

        setLength(from.getLength());
        setName(from.getName());
        setTag(from.getTag());
        setFormat(from.getFormat());
        setSizeMin(from.getSizeMin());
        setSizeMax(from.getSizeMax());
        setMandatory(from.isMandatory());
        setFixedLength(from.isFixedLength());

        if((from.getValue() != null) && (from.getValue() instanceof LinkedList))
        {
            LinkedList<GtppAttribute> list = (LinkedList)from.getValue();
            LinkedList cloneList = new LinkedList();
            try {
                cloneLinkedList(list, cloneList);
                setValue(cloneList);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
