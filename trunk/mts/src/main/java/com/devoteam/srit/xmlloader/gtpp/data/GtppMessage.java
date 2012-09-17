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

package com.devoteam.srit.xmlloader.gtpp.data;

import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.gtpp.GtpHeader;
import com.devoteam.srit.xmlloader.gtpp.GtpHeaderPrime;
import com.devoteam.srit.xmlloader.gtpp.GtppDictionary;
import com.devoteam.srit.xmlloader.gtpp.Header;
import com.devoteam.srit.xmlloader.gtpp.StackGtpp;
import gp.utils.arrays.*;

import java.util.LinkedList;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class GtppMessage
{	
	private Header header; 
	
    public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	private Vector<GtppTLV> tlvs;
    private Array data = null;

    private String logError = "";
    
    public GtppMessage()
    {        
        tlvs = new Vector<GtppTLV>();
    }
    
    public String getLogError()
    {
        return logError;
    }

    public void setLogError(String logError)
    {
        this.logError += logError;
    }   

    public void addTLV(GtppTLV tlv)
    {
        if(tlvs.contains(tlv))//used to remove a TLV if it is override by a new one with the same name or tag
            tlvs.remove(tlv);
        tlvs.add(tlv);
    }

    public GtppTLV getTLV(String name)
    {
        GtppTLV tlv;
        for(int i=0; i < tlvs.size(); i++)
        {
            tlv = tlvs.get(i);
            if(tlv.getName().equalsIgnoreCase(name))
            {
                return tlv;
            }
        }
        return null;
    }

    public GtppTLV getTLV(int tag)
    {
        GtppTLV tlv;
        for(int i=0; i < tlvs.size(); i++)
        {
            tlv = tlvs.get(i);
            if(tlv.getTag() == tag)
            {
                return tlv;
            }
        }
        return null;
    }

    public Array getData()
    {
        return data;
    }

    
    public void parseArray(Array array, GtppDictionary dictionary) throws Exception
    {	
    	
        int protocolType = (array.subArray(0, 1).getBits(3, 1));
        if(protocolType == 0)
        {
    		header = new GtpHeaderPrime();	
        }
        else if(protocolType == 1)
        {
        	header = new GtpHeader(); 
        }
        header.parseArray(array, dictionary);
        int headerSize = header.getSize();
        data = new DefaultArray(array.subArray(headerSize, header.getLength()).getBytes());
        GtppTLV tlv = null;
        int tag = 0;
        int index = 0; //reset index because lenght fiel don't count header
        
        while(index < header.getLength())
        {
            tag = new Integer08Array(array.subArray(index + headerSize, 1)).getValue();
            index++;
            //search in hashmap to see if its a TV or TLV
            tlv = ((StackGtpp)StackFactory.getStack(StackFactory.PROTOCOL_GTPP)).getDictionary().getTLVFromTag(tag);

            if(!tlv.isFixedLength()) {
                tlv.setLength(new Integer16Array(array.subArray(index + headerSize, 2)).getValue());
                index += 2;
            }
            //then get value or length
            Array value = array.subArray(index + headerSize, tlv.getLength());
            index += tlv.getLength();
            if(tlv.getFormat().equals("int"))
            {
                if(tlv.getLength() == 1)
                    tlv.setValue(new Integer08Array(value).getValue());
                else if (tlv.getLength() == 2)
                    tlv.setValue(new Integer16Array(value).getValue());
            }
            else if(tlv.getFormat().equals("list"))
            {
                parseLinkedList(value, tlv, 0);
            }
            else
            {
                tlv.setValue(value.getBytes());
            }
            addTLV(tlv);
        }
        //display an error log if not all mandatory TLV are set
    }
    private int parseLinkedList(Array valueToDecode, Attribute att, int index) throws Exception
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
    
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        for(GtppTLV tlv:tlvs)
        {
            if(tlv.getValueQuality())
            {
                array.addLast(tlv.getArray());
            }
        }

        if((header.getMessageType() == 0) || (header.getName().equalsIgnoreCase("Unknown message")))//in case of unknown message and data present
        {
            if(data != null)
                array.addLast(data);
        }
        
        header.setLength(array.length);
        
        Array supArray = header.getArray();
        array.addFirst(supArray);
        
        return array; 
     }
    
    @Override
    public GtppMessage clone()
    {
    	GtppMessage clone = new GtppMessage();
    	
    	clone.setHeader(header.clone());
        for(int i=0; i< tlvs.size(); i++)
            clone.tlvs.add(tlvs.get(i).clone());

        return clone;
    }
    
    @Override
    public String toString()
    {
        String str = new String();

        if(getLogError().length() != 0)
        {
            str += getLogError();
        }
        
        str += header.toString(); 
        
        for(int i = 0; i < tlvs.size(); i++)
            str += tlvs.get(i).toString();
        if((tlvs.size() == 0) && (data != null))
            str += "data: " + data.toString();
        return str;
    }
}
