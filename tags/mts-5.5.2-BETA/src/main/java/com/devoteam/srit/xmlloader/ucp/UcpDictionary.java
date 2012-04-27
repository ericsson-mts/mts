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

package com.devoteam.srit.xmlloader.ucp;

import com.devoteam.srit.xmlloader.ucp.data.*;
import java.io.File;
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
public class UcpDictionary
{
    private HashMap<String, UcpMessage> requestsList;
    private HashMap<String, UcpMessage> positivesResponsesList;
    private HashMap<String, UcpMessage> negativesResponsesList;
    private HashMap<String, String> messageNameToOperationTypeList;
    private HashMap<String, String> messageOperationTypeToNameList;

    public UcpDictionary(InputStream stream) throws Exception
    {
        this.requestsList = new HashMap<String, UcpMessage>();
        this.positivesResponsesList = new HashMap<String, UcpMessage>();
        this.negativesResponsesList = new HashMap<String, UcpMessage>();

        this.messageNameToOperationTypeList = new HashMap<String, String>();
        this.messageOperationTypeToNameList = new HashMap<String, String>();

        parseFile(stream);
    }

    public String getMessageNameFromOperationType(String OT)
    {
        return messageOperationTypeToNameList.get(OT);
    }

    public String getMessageOperationTypeFromName(String name)
    {
        return messageNameToOperationTypeList.get(name);
    }

    public UcpMessage getMessage(String RR, String OT)
    {
        UcpMessage msg = null;
        if(RR == null)
        {
            msg = requestsList.get(getMessageNameFromOperationType(OT));
            if(msg != null)
                msg = msg.clone();
        }
        else if(RR.equals("A"))//ACK
        {
            msg = positivesResponsesList.get(getMessageNameFromOperationType(OT));
            if(msg == null)//get generic response if not found
            {
                msg = positivesResponsesList.get("GenericResponsePositive").clone();
                msg.setName(getMessageNameFromOperationType(OT));
            }
            else
            {
                msg = msg.clone();
            }
        }
        else if(RR.equals("N"))//NACK
        {
            msg = negativesResponsesList.get(getMessageNameFromOperationType(OT));
            if(msg == null)//get generic response if not found
            {
                msg = negativesResponsesList.get("GenericResponseNegative").clone();
                msg.setName(getMessageNameFromOperationType(OT));
            }
            else
            {
                msg = msg.clone();
            }
        }
        return msg;
    }

    public String getMessageNameFromId(int id) {
        return messageOperationTypeToNameList.get(id);
    }

    public String getMessageIdFromName(String name) {
        return messageNameToOperationTypeList.get(name);
    }

    private void parseFile(InputStream stream) throws Exception
    {
        Element messageElement  = null;
        Element attributeElement = null;
        Element group = null;
        UcpMessage   msg = null;
        UcpAttribute  att = null;
        UcpAttribute  attImbricate = null;
        UcpChoice attChoice = null;
        UcpGroup attGroup = null;
        Vector<UcpGroup> choiceList = null;
        Vector<UcpAttribute> imbricateAtt = null;
        List groupList = null;
        int    i  = 0;
        int    j  = 0;
        int    msgNumber = 0;
        String value = null;
        
        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);

        //parsing des messages
        List listMessages = document.selectNodes("/dictionary/message");
        for(i = 0; i < listMessages.size(); i++)
        {
            messageElement = (Element)listMessages.get(i);

            msg = new UcpMessage();
            msg.setName(messageElement.attributeValue("name"));
            msg.setOperationType(messageElement.attributeValue("id"));
            value = messageElement.attributeValue("type");
            if(value.equals("operation"))
            {
                msg.setMessageType("O");
            }
            else if(value.equals("response"))
            {
                msg.setMessageType("R");
            }

            for (Iterator it = messageElement.elementIterator(); it.hasNext();) {
                attributeElement = (Element) it.next();
                value = attributeElement.getName();

                if(value.equals("attribute"))
                {
                    att = setAttribute(attributeElement);
                    
                    //check if attribute imbricate in attribute
                    List listAtt = attributeElement.selectNodes("//attribute[@name='" + attributeElement.attributeValue("name") + "']/attribute");
                    if(listAtt.size() > 0)
                    {
                        att.setValue(new Vector<UcpAttribute>());
                        for(j = 0; j < listAtt.size(); j++)
                        {
                            attImbricate = setAttribute((Element)listAtt.get(j));
                            ((Vector<UcpAttribute>)att.getValue()).add(attImbricate);
                        }
                    }
                    msg.addAttribute(att);
                }
                else if(value.equals("choice"))
                {
                    msgNumber = i + 1;
                    groupList = attributeElement.selectNodes("/dictionary/message[" + msgNumber + "]/choice/group");
                    choiceList = new Vector<UcpGroup>();

                    attChoice = new UcpChoice();
                    //work because att is the last attribute set and is the att base on choice
                    attChoice.setChoiceAttribute(att);
                    
                    //parse all group of the choice, could be only an attribute or a group of attribute
                    for(j = 0; j < groupList.size(); j++)
                    {
                        group = (Element)groupList.get(j);
                        //create an attribute for each group
                        attGroup = new UcpGroup();

                        attGroup.setChoiceValue(group.attributeValue("value"));
                        imbricateAtt = new Vector<UcpAttribute>();
                        
                        //parse attribute present in the group
                        for (Iterator iter = group.elementIterator(); iter.hasNext();)
                        {
                            imbricateAtt.add(setAttribute((Element)iter.next()));
                        }

                        attGroup.setValue(imbricateAtt);
                        choiceList.add(attGroup);
                    }
                    attChoice.setValue(choiceList);
                    msg.addAttribute(attChoice);
                }
            }

            if(msg.getMessageType().equals("O"))//operation = request
            {
                requestsList.put(msg.getName(), msg);
                //these two hashmap are used by the response or request
                messageOperationTypeToNameList.put(msg.getOperationType(), msg.getName());
                messageNameToOperationTypeList.put(msg.getName(), msg.getOperationType());
            }
            else if(msg.getMessageType().equals("R"))//response
            {
                if(msg.getAttribute("ACK") != null)
                {
                    positivesResponsesList.put(msg.getName(), msg);
                }
                else if(msg.getAttribute("NACK") != null)
                {
                    negativesResponsesList.put(msg.getName(), msg);
                }
            }
        }
        
//        //affichage des message
//        Collection<UcpMessage> collecMsg = requestsList.values();
//        System.out.println("\r\nnb de message dans la collection de requetes: " + collecMsg.size());
//        for(Iterator<UcpMessage> iter = collecMsg.iterator(); iter.hasNext();)
//        {
//            UcpMessage msgEle = (UcpMessage)iter.next();
//            //affichage des attributs
//            System.out.println(msgEle.toString());
//        }
//
//        //affichage des message
//        collecMsg = positivesResponsesList.values();
//        System.out.println("\r\nnb de message dans la collection de reponses positives: " + collecMsg.size());
//        for(Iterator<UcpMessage> iter = collecMsg.iterator(); iter.hasNext();)
//        {
//            UcpMessage msgEle = (UcpMessage)iter.next();
//            //affichage des attributs
//            System.out.println(msgEle.toString());
//        }
//
//        //affichage des message
//        collecMsg = negativesResponsesList.values();
//        System.out.println("\r\nnb de message dans la collection de reponses negatives: " + collecMsg.size());
//        for(Iterator<UcpMessage> iter = collecMsg.iterator(); iter.hasNext();)
//        {
//            UcpMessage msgEle = (UcpMessage)iter.next();
//            //affichage des attributs
//            System.out.println(msgEle.toString());
//        }
    }

    public UcpAttribute setAttribute(Element element) throws Exception
    {
        UcpAttribute att = new UcpAttribute();
        String value = null;

        att.setName(element.attributeValue("name"));
        att.setFormat(element.attributeValue("format"));
        value = element.attributeValue("size");
        if(value != null)
            att.setLength(Integer.parseInt(value));
        
        att.setValue(element.attributeValue("value"));
        value = element.attributeValue("mandatory");
        if(value != null)
        {
            att.setMandatory(Boolean.parseBoolean(value));
        }
        else
        {
            att.setNotApplicable(true);
        }

        value = element.attributeValue("numberOfTime");
        if(value != null)
        {
            att.setOccurenceAttribute(value);
        }
        return att;
    }

}
