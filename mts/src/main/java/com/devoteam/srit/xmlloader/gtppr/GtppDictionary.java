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

package com.devoteam.srit.xmlloader.gtppr;

import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.gtppr.data.GtpHeaderPrime;
import com.devoteam.srit.xmlloader.gtppr.data.GtppAttribute;
import com.devoteam.srit.xmlloader.gtppr.data.GtppMessage;
import com.devoteam.srit.xmlloader.gtppr.data.Tag;
import com.devoteam.srit.xmlloader.gtppr.data.TagTLIV;
import com.devoteam.srit.xmlloader.gtppr.data.TagTLV;
import com.devoteam.srit.xmlloader.gtppr.data.TagTV;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Benjamin Bouvier
 */
public class GtppDictionary
{
    private HashMap<String, GtppMessage> messagesList;
    private HashMap<String, Integer> messageNameToTypeList;
    private HashMap<Integer, String> messageTypeToNameList;

    private HashMap<String, Tag> tlvList;
    private HashMap<String, Integer> tlvNameToTagList;
    private HashMap<Integer, String> tlvTagToNameList;

    public GtppDictionary(InputStream stream) throws Exception
    {
        this.messagesList = new HashMap<String, GtppMessage>();
        this.messageNameToTypeList = new HashMap<String, Integer>();
        this.messageTypeToNameList = new HashMap<Integer, String>();

        this.tlvList = new HashMap<String, Tag>();
        this.tlvNameToTagList = new HashMap<String, Integer>();
        this.tlvTagToNameList = new HashMap<Integer, String>();

        this.parseFile(stream);
    }

    public GtppMessage getMessageFromName(String name) {
        GtppMessage msg = messagesList.get(name);
        if(msg != null)
        {
            msg = msg.clone();
        }
        else
        {
            //create a default message for unknown name
            msg = new GtppMessage();
            msg.getHeader().setLength(6);
            msg.getHeader().setMessageType(0);
            msg.getHeader().setName(name);
        }
        return msg;
    }

    public GtppMessage getMessageFromType(int id) {
        String name = messageTypeToNameList.get(id);
        GtppMessage msg = null;

        if(name != null)
             msg = messagesList.get(name);

        if(msg != null)
        {
            msg = msg.clone();
        }
        else
        {
            //create a default message for unknown id
            msg = new GtppMessage();
            msg.getHeader().setLength(6);
            msg.getHeader().setMessageType(0);
            msg.getHeader().setName("Unknown message");
        }
        return msg;
    }

    public Tag getTLVFromName(String name) {
        Tag tlv = tlvList.get(name);
        if(tlv != null)
        {
            tlv = tlv.clone();
        }
        else
        {
            //create a default message for unknown name
            tlv = new TagTLV();
            tlv.setLength(6);
            tlv.setTag(0);
            tlv.setName(name);
        }
        return tlv;
    }

    public Tag getTLVFromTag(int id) {
        String name = tlvTagToNameList.get(id);
        Tag tlv = null;

        if(name != null)
             tlv = tlvList.get(name);

        if(tlv != null)
        {
            tlv = tlv.clone();
        }
        else
        {
            //create a default message for unknown id
            tlv = new TagTLV();
            tlv.setLength(6);
            tlv.setTag(0);
            tlv.setName("Unknown TLV");
        }
        return tlv;
    }

    public String getMessageNameFromType(int type) {
        return messageTypeToNameList.get(type);
    }

    public int getMessageTypeFromName(String name) {
        Integer type = messageNameToTypeList.get(name);
        return (type != null) ? type : 0;
    }

    private void parseFile(InputStream stream) throws Exception
    {
        Element node  = null;
        GtppMessage msg = null;
        Tag tlv = null;
        String length = null;
        String format = null;
        int    i  = 0;

        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);

        //parsing des messages
        List listMessages = document.selectNodes("/dictionary/message");
        for(i = 0; i < listMessages.size(); i++)
        {
            node = (Element)listMessages.get(i);

            msg = new GtppMessage();
            msg.setHeader(new GtpHeaderPrime()); 
            msg.getHeader().setName(node.attributeValue("name"));
            msg.getHeader().setMessageType(Integer.parseInt(node.attributeValue("messageType")));

            for (Iterator it = node.elementIterator(); it.hasNext();) {
                Element element = (Element) it.next();
                
                String name = element.getName();
                if(name.equalsIgnoreCase("tv") || name.equalsIgnoreCase("tlv") || name.equalsIgnoreCase("tliv"))
                {
                	if (name.equalsIgnoreCase("tv"))
                	{
                		tlv = new TagTV();
                	}
                	else if (name.equalsIgnoreCase("tlv"))
                	{
                		tlv = new TagTLIV();
                	}
                	else if (name.equalsIgnoreCase("tliv"))
                	{
                		tlv = new TagTLIV();
                	}
                	
                    tlv.setName(element.attributeValue("name"));
                    String value = element.attributeValue("mandatory");
                    if(value != null && !value.equalsIgnoreCase("cond"))
                    {
                        tlv.setMandatory(Boolean.parseBoolean(value));
                    }
                    else//case of cond, considered as optional
                    {
                        tlv.setMandatory(false);
                    }
                    msg.addTLV(tlv);
                }
            }

            messagesList.put(msg.getHeader().getName(), msg);
            messageTypeToNameList.put(msg.getHeader().getMessageType(), msg.getHeader().getName());
            messageNameToTypeList.put(msg.getHeader().getName(), msg.getHeader().getMessageType());
        }

        //parsing des TLV
        List listTLV = document.selectNodes("/dictionary/tv | /dictionary/tlv | /dictionary/tliv");
        for(i = 0; i < listTLV.size(); i++)
        {
            node = (Element)listTLV.get(i);
            
            String name = node.getName();            
        	if (name.equalsIgnoreCase("tv"))
        	{
        		tlv = new TagTV();
        	}
        	else if (name.equalsIgnoreCase("tlv"))
        	{
        		tlv = new TagTLV();
        	}
        	else if (name.equalsIgnoreCase("tliv"))
        	{
        		tlv = new TagTLIV();
        	}
            tlv.setName(node.attributeValue("name"));
            tlv.setTag(Integer.parseInt(node.attributeValue("tag")));
            length = node.attributeValue("length");
            if(length != null)
            {
                tlv.setLength(Integer.parseInt(length));
                tlv.setFixedLength(true);
            }
            format = node.attributeValue("format");
            if(format != null)
            {
                tlv.setFormat(format);
                if(format.equals("list"))
                {
                    tlv.setValue(new LinkedList<GtppAttribute>());
                    parseAtt((Attribute)tlv, node);
                }
            }
            
            tlvList.put(tlv.getName(), tlv);
            tlvTagToNameList.put(tlv.getTag(), tlv.getName());
            tlvNameToTagList.put(tlv.getName(), tlv.getTag());
        }
    }

    private void parseAtt(Attribute att, Element node) throws Exception
    {
        String length = null;
        String format = null;

        //travel through all attribute
        for (Iterator it = node.elementIterator(); it.hasNext();) {
            Element element = (Element) it.next();
            GtppAttribute attribute = new GtppAttribute();
            attribute.setName(element.attributeValue("name"));

            length = element.attributeValue("length");
            if(length != null && (length.length() != 0))
                attribute.setLength(Integer.parseInt(length));

            format = element.attributeValue("format");
            if(format != null)
            {
                attribute.setFormat(format);
                if(format.equals("list"))
                {
                    attribute.setValue(new LinkedList<GtppAttribute>());
                    parseAtt((Attribute) attribute, element);//recursif call
                }
            }
            ((LinkedList<GtppAttribute>)att.getValue()).add(attribute);
        }
    }
}
