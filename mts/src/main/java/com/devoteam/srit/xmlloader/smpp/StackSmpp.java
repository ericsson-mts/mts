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
import com.devoteam.srit.xmlloader.smpp.data.SmppMessage;
import com.devoteam.srit.xmlloader.core.ParameterPool;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.gsm.GSMConversion;
import com.devoteam.srit.xmlloader.smpp.data.SmppChoice;
import com.devoteam.srit.xmlloader.smpp.data.SmppGroup;
import com.devoteam.srit.xmlloader.smpp.data.SmppTLV;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer32Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import org.dom4j.Element;

/**
 *
 * @author bbouvier
 */
public class StackSmpp extends Stack
{
    private SmppDictionary smppDictionary;

    /** Constructor */
    public StackSmpp() throws Exception
    {
        super();
        String dictionaryVersion = getConfig().getString("smpp.DICTIONARY_VERSION");

        if(dictionaryVersion.equalsIgnoreCase("3.4"))
            dictionaryVersion = "../conf/smpp/dictionary_v3.4.xml";
        else if(dictionaryVersion.equalsIgnoreCase("5.0"))
            dictionaryVersion = "../conf/smpp/dictionary_v5.0.xml";
        else
            throw new Exception("SMPP dictionary version " + dictionaryVersion + " not supported");
        
        smppDictionary = new SmppDictionary(SingletonFSInterface.instance().getInputStream(new URI(dictionaryVersion)));
        
        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
        	Listenpoint listenpoint = new ListenpointSmpp(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_SMPP);
        }
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointSmpp(this, root);
        return listenpoint;        
    }

    /** Creates a Channel specific to each Stack */
    @Override
    public Channel parseChannelFromXml(Element root, String protocol) throws Exception
    {
        String name       = root.attributeValue("name");
        String localHost  = root.attributeValue("localHost");
        String localPort  = root.attributeValue("localPort");
        String remoteHost = root.attributeValue("remoteHost");
        String remotePort = root.attributeValue("remotePort");

        if (existsChannel(name))
        {
            return getChannel(name);
        }
        else
        {
            if(null != localHost) localHost = InetAddress.getByName(localHost).getHostAddress();
            else                  localHost = "0.0.0.0";

            if(null != remoteHost) remoteHost = InetAddress.getByName(remoteHost).getHostAddress();

            return new ChannelSmpp(name, localHost, localPort, remoteHost, remotePort, protocol);
        }
    }

	/** Creates a specific Msg */
    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        SmppMessage smppMessage = null;

        // header
        Element header = root.element("header");
        String msgName = header.attributeValue("name");
        String msgId = header.attributeValue("id");

        if((msgId != null) && (msgName != null))
            throw new Exception("id and name of the message " + msgName + " must not be set both");

        if((msgId == null) && (msgName == null))
            throw new Exception("One of the parameter id and name of the message header must be set");

        if(msgName != null)
        {
            smppMessage = smppDictionary.getMessageFromName(msgName);
            smppMessage.setId(smppDictionary.getMessageIdFromName(msgName));
        }

        if(msgId != null)
            smppMessage = smppDictionary.getMessageFromId((int)Long.parseLong(msgId, 16));

        if((smppMessage.getId() == 0) || (smppMessage.getName().equalsIgnoreCase("Unknown message")))
            smppMessage.setLogError("Message <" + msgName + "> is not present in the dictionary\r\n");
 
        String msgStatus = header.attributeValue("status");
        String msgSeqNum = header.attributeValue("sequence_number");

        if(msgStatus != null)
            smppMessage.setStatus((int)Long.parseLong(msgStatus));
        smppMessage.setSequenceNumber((int)Long.parseLong(msgSeqNum));

        parseAttributes(root, smppMessage);
        parseTLVs(root, smppMessage);

        return new MsgSmpp(smppMessage);
    }

    public void parseAttributes(Element root, SmppMessage msg) throws Exception
    {
        List<Element> attributes = root.elements("attribute");
        List<Element> imbricateAttributesInScenario = null;
        SmppAttribute att = null;
        SmppAttribute att2 = null;
        SmppGroup attGroup = null;
        String value = null;
        String name = null;
        String ret = null;
        Element element2 = null;

        for(int cpt = 0; cpt < attributes.size(); cpt++)
        {
            Element element = attributes.get(cpt);
            att = msg.getAttribut(element.attributeValue("name"));

            if(att != null)
            {
                if(!(att.getValue() instanceof Vector))//simple attribute
                {
                    value = element.attributeValue("value");

                    //cas particulier pour sm_length
                    if(att.getName().equalsIgnoreCase("sm_length"))
                    {
                        if(value.equalsIgnoreCase("auto"))
                            value = "-1";//value is et to -1 to later set it automattically with the good length of the short message
                    }

                    ret = setAttributeValue(att, value, element.attributeValue("type"));
                    if(ret != null)
                        msg.setLogError(ret);
                }
                else//attribute containing multiple occurence
                {
                    //get vector values of att
                    Vector vec = (Vector)att.getValue();

                    int occurenceValue = 0;
                    if(att.getOccurenceAttribute() != null)//if occurence is defined
                    {
                        //get the occurence value
                        String occurence = att.getOccurenceAttribute();
                        occurenceValue = (Integer) msg.getAttribut(occurence).getValue();
                        //need to duplicate vector of imbricatte attribute in the occurence vector
                        for(int i = 1; i < occurenceValue; i++)
                        {
                            //this way is used to the clone method
                            Vector<Attribute> newVec = new Vector<Attribute>();
                            for(int j = 0; j < ((Vector)vec.get(0)).size(); j++)
                                newVec.add(((Attribute)((Vector)vec.get(0)).get(j)).clone());
                            vec.add(newVec);
                        }
                    }

                    for(int cptOc = 0; cptOc < occurenceValue; cptOc++)//run through multiple occurence
                    {
                        //get imbricate attribute or attribute in choice for att
                        imbricateAttributesInScenario = element.selectNodes("attribute");

                        Vector vecImbricateAtt = (Vector) vec.get(cptOc);
                        //run through attribute in att,(list of attribute, or attribute and choice)
                        for(int i = 0; i < vecImbricateAtt.size(); i++)
                        {
                            if(vecImbricateAtt.get(i) instanceof SmppChoice)
                            {
                                SmppChoice choice = (SmppChoice) vecImbricateAtt.get(i);
                                //get choice parameter
                                String choiceName = choice.getChoiceAttribute().getName();

                                //search this choice parameter in the attribute list
                                for(int j = 0; j < vecImbricateAtt.size(); j++)
                                {
                                    att2 = (SmppAttribute)vecImbricateAtt.get(j);
                                    if(choiceName.equals(att2.getName()))
                                    {
                                        //get its value
                                        value = Integer.toString((Integer)att2.getValue());
                                        //set choice attribute value in choice with this value
                                        choice.getChoiceAttribute().setValue((Integer)att2.getValue());
                                        break;
                                    }
                                }

                                if((value != null) && (value.length() != 0))
                                {
                                    //search value of the choice parameter in the choice attribute group list
                                    for(int j = 0; j < ((Vector<SmppGroup>)choice.getValue()).size(); j++)
                                    {
                                        attGroup = ((Vector<SmppGroup>)choice.getValue()).get(j);
                                        if(attGroup.getChoiceValue().equals(value))
                                        {
                                            //set value of the group with value in the scenario
                                            for(int k = 0; k < ((Vector<SmppAttribute>)attGroup.getValue()).size(); k++)
                                            {
                                                //check name and then set value
                                                att2 = ((Vector<SmppAttribute>)attGroup.getValue()).get(k);
                                                element2 = imbricateAttributesInScenario.get(0);//get first following the order in scenario
                                                if(att2.getName().equalsIgnoreCase(element2.attributeValue("name")))
                                                {
                                                    ret = setAttributeValue(att2, element2.attributeValue("value"), element2.attributeValue("type"));
                                                    if(ret != null)
                                                        msg.setLogError(ret);
                                                    //remove from the list to not retrieve it later
                                                    imbricateAttributesInScenario.remove(0);
                                                }
                                                else
                                                {
                                                    throw new Exception("Name of attribute " + att2.getName() + "doesn't correspond with attribute " + element2.attributeValue("name") + " in scenario");
                                                }
                                            }
                                            break;
                                        }
                                    }
                                }
                                else
                                {
                                    throw new Exception("value of attribute " + " must not be empty");
                                }
                            }
                            else
                            {
                                att2 = ((SmppAttribute)vecImbricateAtt.get(i));

                                //run through attribute in scenario to find att with the same name
                                for(int j = 0; j < imbricateAttributesInScenario.size(); j++)
                                {
                                    element2 = imbricateAttributesInScenario.get(j);
                                    name = element2.attributeValue("name");

                                    if(att2.getName().equalsIgnoreCase(name))//if found
                                    {
                                        ret = setAttributeValue(att2, element2.attributeValue("value"), element2.attributeValue("type"));
                                        if(ret != null)
                                            msg.setLogError(ret);
                                        //remove from the list to not retrieve it later
                                        imbricateAttributesInScenario.remove(j);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        //increment for next access to a new occurence:
                        if(((cptOc + 1) == (occurenceValue - 1)) && ((cpt + 1) < attributes.size()))
                        {
                            cpt++;
                            element = attributes.get(cpt);
                        }
                    }
                }
            }
            //add attribute to message even if unknown and if the message is not present in the dictionary
            else if(msg.getName().equals("UnknownMesage") || (msg.getId() == 0))
            {
                value = element.attributeValue("value");
                att = new SmppAttribute();
                att.setName(element.attributeValue("name"));
                att.setFormat("OCTETSTRING");
                att.setValue(new DefaultArray(value.getBytes()));
                msg.addAttribut(att);
            }
            else
            {
                msg.setLogError("attribute <" + element.attributeValue("name") + "> not added in message because it is unknown for this message\r\n");
            }
        }
    }

    public void parseTLVs(Element root, SmppMessage msg) throws Exception
    {
        List<Element> tlvs = root.elements("tlv");
        SmppTLV tlv = null;
        String value = null;
        
        for(Element element:tlvs)
        {
            int length = 0;
            tlv = msg.getTLV(element.attributeValue("name"));
            if(tlv != null)
            {
                value = element.attributeValue("value");
                if(tlv.getFormat().equalsIgnoreCase("INT"))
                {
                    tlv.setValue((int)Long.parseLong(value));
                    int tmp = ((Integer)tlv.getValue());
                    while(tmp != 0)
                    {
                        tmp >>= 8;
                        length++;
                    }
                }
                else if(tlv.getFormat().equalsIgnoreCase("OCTETSTRING") && (element.attributeValue("type") != null) && element.attributeValue("type").equalsIgnoreCase("binary"))
                {
                    tlv.setValue(DefaultArray.fromHexString(value));// do conversion in string here to don't have pb with charset
                    length = ((DefaultArray)tlv.getValue()).length;
                }
                else if(tlv.getFormat().equalsIgnoreCase("OCTETSTRING"))
                {
                    tlv.setValue(new DefaultArray(GSMConversion.toGsmCharset(value)));
                    length = ((SupArray)tlv.getValue()).length;
                }
                else
                {
                    tlv.setValue(new String(GSMConversion.toGsmCharset(value)));
                    length = ((String)tlv.getValue()).length();
                }
                
                value = element.attributeValue("length");
                if(value != null)
                {
                    if(value.equalsIgnoreCase("auto"))
                        tlv.setLength(length);
                    else
                        tlv.setLength(Integer.parseInt(value));
                }
                
                if((tlv.getLength() > tlv.getSizeMax()) || (tlv.getLength() < tlv.getSizeMin()))
                {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "TLV length for ", tlv.toString(), "is not according to size given in dictionary");
                }
            }
            else
            {
                //add tlv to message even if unknown
                value = element.attributeValue("value");
                tlv = new SmppTLV();
                tlv.setName(element.attributeValue("name"));
                tlv.setLength(Integer.parseInt(element.attributeValue("length")));
                tlv.setFormat("OCTETSTRING");
                tlv.setValue(new DefaultArray(GSMConversion.toGsmCharset(value)));
                msg.addTLV(tlv);
            }
        }
    }

    public String setAttributeValue(SmppAttribute att, String value, String type) throws Exception
    {
        String ret = null;
        if(att.getFormat().equalsIgnoreCase("INT"))
        {
            att.setValue(Integer.parseInt(value));

            if((att.getSizeMax() == 1) && (Integer.parseInt(value) > 255))
                ret = "for attribute " + att.getName() + " ,value " + value + " is superior to 255 max value, so value sent is trunkated\r\n";
            else if((att.getSizeMax() == 2) && (Integer.parseInt(value) > 65535))
                ret = "for attribute " + att.getName() + " ,value " + value + " is superior to 2^16-1 max value, so value sent is trunkated";
            else if((att.getSizeMax() == 4) && (Integer.parseInt(value) > 2147483647))
                ret = "for attribute " + att.getName() + " ,value " + value + " is superior to 2^32-1 max value, so value sent is trunkated";
        }
        else if(att.getFormat().equalsIgnoreCase("OCTETSTRING") && (type != null) && type.equalsIgnoreCase("binary"))
        {
            att.setValue(DefaultArray.fromHexString(value));// do conversion in string here to don't have pb with charset
        }
        else if(att.getFormat().equalsIgnoreCase("OCTETSTRING"))
        {
            att.setValue(new DefaultArray(GSMConversion.toGsmCharset(value)));
        }
        else
        {
            att.setValue(new String(GSMConversion.toGsmCharset(value)));
        }
        return ret;
    }

    @Override
    public Msg readFromStream(InputStream inputStream, Channel channel) throws Exception
    {
        byte[] lg = new byte[4];
        byte[] buf = null;
        int nbCharRead = 0;
        int msgLength = 0;
        Integer32Array lgArray = null;
        SupArray msgArray = new SupArray();
        
        synchronized (inputStream)
        {
            //read the length
            nbCharRead = inputStream.read(lg, 0, 4);
            if(nbCharRead == -1)
                throw new Exception("End of stream detected");
            else if(nbCharRead < 4)
                throw new Exception("Not enough char read");

            lgArray = new Integer32Array(new DefaultArray(lg));
            msgLength = lgArray.getValue();
            buf = new byte[msgLength-4];
            //read the staying message's data
            nbCharRead = inputStream.read(buf, 0, msgLength-4);
        }

        if(nbCharRead == -1)
            throw new Exception("End of stream detected");
        else if(nbCharRead < (msgLength-4))
            throw new Exception("Not enough char read");

        msgArray.addFirst(lgArray);
        msgArray.addLast(new DefaultArray(buf));
        DefaultArray array = new DefaultArray(msgArray.getBytes());
        
        //get id from message to get message from dictionary
        int id  = new Integer32Array(array.subArray(4, 4)).getValue();
        SmppMessage msg = smppDictionary.getMessageFromId(id);
        msg.parseArray(array);
        
        return new MsgSmpp(msg);
    }

    /** Returns the Config object to access the protocol config file*/
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("smpp.properties");
    }

    /** Returns the replacer used to parse sendMsg Operations */
    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }

}
