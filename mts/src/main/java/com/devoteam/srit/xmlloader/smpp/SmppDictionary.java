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

package com.devoteam.srit.xmlloader.smpp;

import com.devoteam.srit.xmlloader.smpp.data.SmppAttribute;
import com.devoteam.srit.xmlloader.smpp.data.SmppChoice;
import com.devoteam.srit.xmlloader.smpp.data.SmppGroup;
import com.devoteam.srit.xmlloader.smpp.data.SmppMessage;
import com.devoteam.srit.xmlloader.smpp.data.SmppTLV;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.dom4j.io.SAXReader;
import org.dom4j.*;

/**
 *
 * @author Benjamin Bouvier
 */
public class SmppDictionary
{
    private HashMap<String, SmppMessage> messagesList;
    private HashMap<String, Integer> messageNameToIdList;
    private HashMap<Integer, String> messageIdToNameList;

    public SmppDictionary(InputStream stream) throws Exception
    {
        this.messagesList = new HashMap<String, SmppMessage>();
        this.messageNameToIdList = new HashMap<String, Integer>();
        this.messageIdToNameList = new HashMap<Integer, String>();
        this.parseFile(stream);
    }

    public SmppMessage getMessageFromName(String name) {
        SmppMessage msg = messagesList.get(name);
        if(msg != null)
        {
            msg = msg.clone();
        }
        else
        {
            //create a default message for unknown name
            msg = new SmppMessage();
            msg.setLength(16);
            msg.setName(name);
            msg.setId(0);
        }
        return msg;
    }

    public SmppMessage getMessageFromId(int id) {
        String name = messageIdToNameList.get(id);
        SmppMessage msg = null;

        if(name != null)
             msg = messagesList.get(name);

        if(msg != null)
        {
            msg = msg.clone();
        }
        else
        {
            //create a default message for unknown id
            msg = new SmppMessage();
            msg.setLength(16);
            msg.setId(id);
            msg.setName("Unknown message");
        }
        return msg;
    }

    public String getMessageNameFromId(int id) {
        return messageIdToNameList.get(id);
    }

    public int getMessageIdFromName(String name) {
        Integer id = messageNameToIdList.get(name);
        return (id != null) ? id : 0;
    }

    private void parseFile(InputStream stream) throws Exception
    {
        Element  messageNode  = null;
        Element group = null;
        SmppMessage   msg = null;
        SmppAttribute  att = null;
        SmppAttribute  att2 = null;
        SmppGroup attGroup = null;
        SmppChoice attChoice = null;
        Vector<SmppGroup> choiceList = null;
        Vector<SmppAttribute> imbricateAtt = null;
        SmppTLV       tlv = null;
        int    i  = 0;
        int    j  = 0;
        long   id = 0;

        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);

        //parsing des messages
        List listMessages = document.selectNodes("/dictionary/message");
        for(i = 0; i < listMessages.size(); i++)
        {
            messageNode = (Element)listMessages.get(i);

            msg = new SmppMessage();
            msg.setName(messageNode.attributeValue("name"));
            id = Long.parseLong(messageNode.attributeValue("id"), 16);
            msg.setId((int)id);

            for (Iterator it = messageNode.elementIterator(); it.hasNext();) {
                Element element = (Element) it.next();
                String name = element.getName();

                if(name.equalsIgnoreCase("attribute"))
                {
                    att = setAttribute(element); //for simple attribute

                    //check if attribute imbricate in attribute or choice in attribute
                    List listAtt = element.selectNodes("//attribute[@name='" + element.attributeValue("name") + "']/attribute | //attribute[@name='" + element.attributeValue("name") + "']/choice" );
                    if(!listAtt.isEmpty())
                    {
                        ((Vector)att.getValue()).add(new Vector<SmppAttribute>());//set vector of imbricate attributes into the vector of occurence
                    }

                    for(j = 0; j < listAtt.size(); j++)
                    {
                        if(((Element)listAtt.get(j)).getName().equalsIgnoreCase("attribute"))
                        {
                            //add imbricate attribute to att
                            att2 = setAttribute((Element)listAtt.get(j));
                            ((Vector<SmppAttribute>)((Vector)att.getValue()).get(0)).add(att2);
                        }
                        else if(((Element)listAtt.get(j)).getName().equalsIgnoreCase("choice"))
                        {
                            List groupList = element.selectNodes("//attribute[@name='" + element.attributeValue("name") + "']/choice/group");
                            attChoice = new SmppChoice();
                            choiceList = new Vector<SmppGroup>();

                            //work because last imbricate att is the last attribute set and is the att base on choice
                            attChoice.setChoiceAttribute(att2);

                            //parse all group of the choice, could be only an attribute or a group of attribute
                            for(j = 0; j < groupList.size(); j++)
                            {
                                group = (Element)groupList.get(j);
                                //create an attribute for each group
                                attGroup = new SmppGroup();

                                attGroup.setChoiceValue(group.attributeValue("value"));
                                imbricateAtt = new Vector<SmppAttribute>();

                                for (Iterator iter = group.elementIterator(); iter.hasNext();)
                                {
                                    imbricateAtt.add(setAttribute((Element)iter.next()));
                                }

                                attGroup.setValue(imbricateAtt);
                                choiceList.add(attGroup);
                            }
                            attChoice.setValue(choiceList);
                            ((Vector<SmppChoice>)((Vector)att.getValue()).get(0)).add(attChoice);
                        }
                    }
                    msg.addAttribut(att);
                }
                else if(name.equalsIgnoreCase("tlv"))
                {
                    tlv = setTLV(element, msg);
                    if(tlv != null)
                        msg.addTLV(tlv);
                }
            }

            messagesList.put(msg.getName(), msg);
            messageIdToNameList.put(msg.getId(), msg.getName());
            messageNameToIdList.put(msg.getName(), msg.getId());
        }

//        //affichage des message
//        Collection<SmppMessage> collecMsg = messagesList.values();
//        System.out.println("\r\nnb de message dans la collection: " + collecMsg.size());
//        for(Iterator<SmppMessage> iter = collecMsg.iterator(); iter.hasNext();)
//        {
//            SmppMessage msgEle = (SmppMessage)iter.next();
//            if(msgEle.getName().equals("submit_multi"))
//            {
//                System.out.println(msgEle.getName());
//                //affichage des attributs
//                System.out.println(msgEle.toString());
//            }
//        }
    }

    public SmppAttribute setAttribute(Element element) throws Exception
    {
        SmppAttribute att = new SmppAttribute();
        String value = null;

        att.setName(element.attributeValue("name"));
        att.setFormat(element.attributeValue("format"));
        value = element.attributeValue("sizeMin");
        if(value != null)
            att.setSizeMin(Integer.parseInt(value));
        value = element.attributeValue("sizeMax");
        if(value != null)
            att.setSizeMax(Integer.parseInt(value));
        value = element.attributeValue("value");//always an INT value in the dictionnary
        if(value != null)
            att.setValue(Integer.parseInt(value));

        value = element.attributeValue("numberOfTime");//number of occurence of this attribut depend on which parameter
        if(value != null)
        {
            att.setOccurenceAttribute(value);
            att.setValue(new Vector<SmppAttribute>());
        }

        return att;
    }

    public SmppTLV setTLV(Element element, SmppMessage msg) throws Exception
    {
        SmppTLV tlv = new SmppTLV();
        String value = null;

        tlv.setName(element.attributeValue("name"));
        tlv.setTag(Integer.parseInt(element.attributeValue("tag"), 16));
        tlv.setFormat(element.attributeValue("format"));
        value = element.attributeValue("sizeMin");
        if(value != null)
            tlv.setSizeMin(Integer.parseInt(value));
        value = element.attributeValue("sizeMax");
        if(value != null)
        {
            if(value.equalsIgnoreCase("tlv_length"))
            {
                tlv.setSizeMax(65536);//size max of 2 octets in lenght field
            }
            else
            {
                tlv.setSizeMax(Integer.parseInt(value));
            }
        }
        value = element.attributeValue("mandatory");
        if(value != null)
        {
            tlv.setMandatory(Boolean.parseBoolean(value));
        }
        
        return tlv;
    }
}
