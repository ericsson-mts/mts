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
import com.devoteam.srit.xmlloader.gtppr.GtppDictionary;

import gp.utils.arrays.*;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class GtppMessage
{	
	private Header header; 
	
	private Vector<Tag> tlvs;
    private Array data = null;

    private String logError = "";
    
    public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}
    
    public GtppMessage()
    {        
        tlvs = new Vector<Tag>();
    }
    
    public String getLogError()
    {
        return logError;
    }

    public void setLogError(String logError)
    {
        this.logError += logError;
    }   

    public void addTLV(Tag tlv)
    {
        if(tlvs.contains(tlv))//used to remove a TLV if it is override by a new one with the same name or tag
            tlvs.remove(tlv);
        tlvs.add(tlv);
    }

    public Tag getTLV(String name)
    {
        Tag tlv;
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

    public Tag getTLV(int tag)
    {
        Tag tlv;
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
        Tag tlv = null;
        int tag = 0;
        int index = 0; //reset index because length field don't count header
        
        while(index < header.getLength())
        {
            tag = new Integer08Array(array.subArray(index, 1)).getValue();
            index++;
            //search in hashmap to see if its a TV or TLV
            tlv = dictionary.getTLVFromTag(tag);
            index = tlv.parseArray(array , index, dictionary);
            addTLV(tlv);
        }
    }
    
    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        for(Tag tlv:tlvs)
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
