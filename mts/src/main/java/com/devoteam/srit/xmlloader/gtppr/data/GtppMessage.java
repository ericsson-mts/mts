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

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.gtppr.GtppDictionary;

import gp.utils.arrays.*;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.dom4j.Element;

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
    
    /** 
     * Parse the message from XML element 
     */
    public void parseMsgFromXml(Element root) throws Exception
    {
        GtppMessage gtppMessage = new GtppMessage();
        Header gtpHeader = null;

        // header parsing
        Element xmlHeaderPrime = root.element("headerPrime");
        Element xmlHeaderV1 = root.element("headerV1");
        Element xmlHeaderV2 = root.element("headerV2");
        
        Element xmlHeader = null;
        if(xmlHeaderPrime != null)
        {
        	gtpHeader = new GtpHeaderPrime();
        	xmlHeader = xmlHeaderPrime; 
        }
        else if(xmlHeaderV1 != null)
        {
        	gtpHeader = new GtpHeaderV1();
        	xmlHeader = xmlHeaderV1;
        }
        else if(xmlHeaderV2 != null)
        {
        	gtpHeader = new GtpHeaderV2();
        	xmlHeader = xmlHeaderV2;
        }        
        else
        {
        	 GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Not GTP message. <header> or <headeP> is missing.");
        }
        
        // get the right dictionary in from of the version
        GtppDictionary dictionary = GtppDictionary.getDictionary(gtpHeader.getVersionName());
        
        gtpHeader.parseXml(xmlHeader, dictionary); 
    	gtppMessage.setHeader(gtpHeader);

        // dictionary translation
        String msgName = gtpHeader.getName(); 
        if((gtpHeader.getMessageType() == 0) || (gtpHeader.getName().equalsIgnoreCase("Unknown message")))
            gtppMessage.setLogError("Message <" + msgName + "> is not present in the dictionary\r\n");
        
        // TLV parsing
        parseTLVs(root, gtppMessage, dictionary);
    }
    
    private void parseTLVs(Element root, GtppMessage msg, GtppDictionary dictionary) throws Exception
    {
        List<Element> tlvs = root.elements();
        List<Element> attributes = null;
        Tag tlv = null;
        String value = null;
        String length = null;

        for(Element element:tlvs)
        {
        	String name = element.getName();
            if (name.equalsIgnoreCase("tv") || name.equalsIgnoreCase("tlv") || name.equalsIgnoreCase("tliv"))
            {
	            value = element.attributeValue("name");
	            if(value != null)
	                tlv = dictionary.getTLVFromName(value);
	            else
	            {
	                value = element.attributeValue("tag");
	                if(value != null)
	                    tlv = dictionary.getTLVFromTag(Integer.parseInt(value));
	            }
	
	            if(tlv != null)
	            {   
	                length = element.attributeValue("length");
	                if(length != null)
	                {
	                    if((tlv.getLength() > 0) && (tlv.getLength() != Integer.parseInt(length)))
	                    {
	                        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "TLV length for " + tlv.toString() + "is not according to size given in dictionary");
	                    }
	                    
	                    if(value.equalsIgnoreCase("auto"))
	                        tlv.setLength(value.length());
	                    else
	                        tlv.setLength(Integer.parseInt(length));
	                }
	
	                value = element.attributeValue("value");
	                if(value != null)
	                {
	                    setAttributeValue((Attribute)tlv, value);
	                }
	                else
	                {
	                    //check if there is attribute inside the TLV
	                    attributes = element.elements("attribute");
	                    parseAtt((Attribute)tlv, attributes);
	                }
	
	                if(tlv.getLength() == -1)//if no size is given in the dictionary
	                {
	                    if(!(tlv.getValue() instanceof LinkedList))
	                        tlv.setLength(((byte[])tlv.getValue()).length);
	                    else
	                        tlv.setLength(tlv.getArray().length - 3);//- 3 to remove the tag and length bytes
	                }
	            }   
	            else
	            {
	                //add tlv to message even if unknown
	                value = element.attributeValue("value");
	                tlv = new TagTLV();
	                tlv.setName(element.attributeValue("name"));
	                tlv.setLength(Integer.parseInt(element.attributeValue("length")));
	                tlv.setValue(value.getBytes());
	            }
	            msg.addTLV(tlv);
	            tlv = null;
	        }
        }
    }

    private void parseAtt(Attribute att, List<Element> attributes) throws Exception
    {
        LinkedList<Object> listAtt = null;
        String value = null;

        for(Element elementAtt:attributes)
        {
            value = elementAtt.attributeValue("name");
            listAtt = ((LinkedList)att.getValue());
            for(int i = 0; i < listAtt.size(); i++)
            {
                if(listAtt.get(i) instanceof GtppAttribute)
                {
                    GtppAttribute attribute = (GtppAttribute)listAtt.get(i);
                    if(attribute.getName().equalsIgnoreCase(value))
                    {
                        if(!attribute.getFormat().equalsIgnoreCase("list"))
                        {
                            if(attribute.getValueQuality())//duplicate att in case it is already set
                            {
                                GtppAttribute duplicateAtt = attribute.clone();
                                attribute = duplicateAtt;
                                listAtt.add(attribute);
                            }
                            
                            value = elementAtt.attributeValue("value");
                            if(attribute.getValueQuality())//if an attribute already exist with the same name, duplicate it in the list
                            {
                                listAtt.add(i+1, attribute.clone());
                                attribute = (GtppAttribute)listAtt.get(i+1);
                            }
                            setAttributeValue(attribute, value);
                            break;
                        }
                        else//if this attribute is also a list => recursive call
                        {
                            parseAtt(attribute, elementAtt.elements("attribute"));
                        }
                    }
                }
                else if (listAtt.get(i) instanceof LinkedList)
                {
                    LinkedList list = (LinkedList)listAtt.get(i);
                    for(int j = 0; j < list.size(); j++)
                        parseAtt((Attribute)list.get(j), elementAtt.elements("attribute"));
                }

                if(i == listAtt.size())//insert attribute at the place given in this list
                {
                    GtppAttribute newAtt = new GtppAttribute();
                    newAtt.setName(value);
                    value = elementAtt.attributeValue("value");
                    newAtt.setValue(value.getBytes());
                    listAtt.add(attributes.indexOf(elementAtt), newAtt);
                    break;
                }
            }
        }
    }
    
    private void setAttributeValue(Attribute attribute, String value) throws Exception
    {
        if(attribute.getFormat().equals("int"))
            attribute.setValue(Integer.parseInt(value));
        else if(attribute.getFormat().equals("ip"))
            attribute.setValue(InetAddress.getByName(value).getAddress());
        else
            attribute.setValue(value.getBytes());
    }
}
