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

package com.devoteam.srit.xmlloader.ucp.data;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import gp.utils.arrays.*;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class UcpMessage
{
    static public Array STX = new ConstantArray((byte)2, 1);
    static public Array ETX = new ConstantArray((byte)3, 1);
    static public Array SEP = new ConstantArray((byte)47, 1);//47 is 2f in hexa

    private String name;
    private String transactionNumber; //2 char num
    private String length; //5 char num
    private String messageType;    //O or R char
    private String operationType;  //2 char num
    private Vector<Attribute> listAttributes;
    private String checksum;
    private String dataRaw;

    private String logError = "";

    public UcpMessage()
    {        
        listAttributes = new Vector<Attribute>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(String value)
    {
        this.length = value;
        if(length != null)
        {
            while (length.length() < 5)
            {
                length = "0" + length;//padding with zero
            }
        }
    }

    public String getLength()
    {
        return this.length;
    }

    public void calculLength()
    {
        //calcul the message's length
        //useful before sending message from an xml
        //10 header + 2 checksum + 4 SEP no yet set at this time
        //=> STX TRN/LN/OR/OT/.../CHECKSUM ETX
        int lg = 0;
        for(int i = 0; i < listAttributes.size(); i++)
        {
            if(listAttributes.get(i).getValue() instanceof Vector)
            {
                Vector<UcpAttribute> vec = (Vector<UcpAttribute>) listAttributes.get(i).getValue();
                 for(int j = 0; j < vec.size(); j++)
                 {
                    lg += ((String)vec.get(j).getValue()).length() + 1;//+1 for separator
                 }
            }
            else
            {
                lg += ((String)listAttributes.get(i).getValue()).length() + 1;//+1 for separator
            }
        }
        length = Integer.toString(lg + 16);
    }

    public void setTransactionNumber(String value)
    {
        this.transactionNumber = value;
        if((transactionNumber != null) && (transactionNumber.length() < 2))
        {
            transactionNumber = "0" + transactionNumber;//padding with zero 
        }
    }

    public String getTransactionNumber()
    {
        return this.transactionNumber;
    }

    public void setMessageType(String value)
    {
        this.messageType = value;
    }

    public String getMessageType()
    {
        return this.messageType;
    }

    public void setOperationType(String status)
    {
        this.operationType = status;
    }

    public String getOperationType()
    {
        return this.operationType;
    }

    public void calculChecksum(Array array)
    {
        //check calcul on int is good
        byte sum = 0;
        for(int i = 0; i < array.length; i++)
        {
            sum += array.get(i);
        }
        this.checksum = Integer.toHexString(sum & 0xFF);
        if(checksum.length() < 2)
        {
            checksum = "0" + checksum;
        }
        checksum = checksum.toUpperCase();
    }

    public String getChecksum()
    {
        return this.checksum;
    }

    public void addAttribute(Attribute att)
    {
        listAttributes.add(att);
    }

    public String getDataRaw()
    {
        return dataRaw;
    }

    public void setDataRaw(String dataRaw)
    {
        this.dataRaw = dataRaw;
    }

    public String getLogError()
    {
        return logError;
    }

    public void setLogError(String logError)
    {
        this.logError += logError;
    }

    public UcpAttribute getAttribute(String name)
    {
        Attribute att = null;
        boolean result = false;
        
        for(int i = 0; (i < listAttributes.size()) && !result; i++)
        {
            att = listAttributes.get(i);

            if((att != null))
            {
                if(att instanceof UcpChoice)
                {
                    Vector<UcpAttribute> groupList = (Vector<UcpAttribute>)att.getValue();//get list of group in value
                    String choiceValue = ((String)listAttributes.get(i-1).getValue());//because choiceValue is attribute just before
                    UcpAttribute att2 = null;

                    for(int j = 0; (j < groupList.size()) && !result; j++)
                    {
                        att = groupList.get(j);
                        if(((UcpGroup)att).getChoiceValue().equals(choiceValue))
                        {
                            for(int k = 0; !result && (k < ((Vector<UcpAttribute>)att.getValue()).size()); k++)
                            {
                                att2 = ((Vector<UcpAttribute>)att.getValue()).get(k);
                                if(att2.getName().equalsIgnoreCase(name))
                                {
                                    att = att2;
                                    result = true;
                                }
                            }
                        }
                    }
                }
                else if(att.getName().equalsIgnoreCase(name))
                {
                    result = true;
                }
            }
        }
        
        if(!result)
        {
            att = null;
        }
        return (UcpAttribute)att;
    }

    public Vector<Attribute> getData()
    {
        return listAttributes;
    }

    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();

        for(Attribute att:listAttributes)
        {
            array.addLast(att.getArray());
        }

        //calcul the message's length
        //10 header + 2 checksum + 4 SEP no yet set at this time
        //=> STX TRN/LN/OR/OT/.../CHECKSUM ETX
        setLength(Integer.toString(array.length + 16));
        
        array.addFirst(SEP);
        array.addFirst(new DefaultArray(operationType.getBytes()));
        array.addFirst(SEP);
        array.addFirst(new DefaultArray(messageType.getBytes()));
        array.addFirst(SEP);
        array.addFirst(new DefaultArray(length.getBytes()));
        array.addFirst(SEP);
        array.addFirst(new DefaultArray(transactionNumber.getBytes()));

        calculChecksum(array);
        array.addLast(new DefaultArray(checksum.getBytes()));
        array.addFirst(STX);
        array.addLast(ETX);
        
        return array;
    }
    
    public void parseArray(Array array) throws Exception
    {
        String[] splittedMsg = Utils.splitNoRegex(new String(array.getBytes()), "/");
        int index = 0;
        UcpAttribute attribute = null;
        
        //check the message checksum
        calculChecksum(array.subArray(0, array.length - 2));//length - 2 to not include checksum
        if(!checksum.equals(splittedMsg[splittedMsg.length - 1]))
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Bad checksum of the message");
        }

        if(splittedMsg.length < 4)
        {
            throw new Exception("Cannot decode message " + array.toString() + " cause it is to short");
        }

        //the index take account of the separator
        setTransactionNumber(splittedMsg[index++]);
        setLength(splittedMsg[index++]);
        setMessageType(splittedMsg[index++]);
        setOperationType(splittedMsg[index++]);

        Attribute att = null;
        int i = 0;

        dataRaw = new String(array.getBytes());

        for(i = 0; (i < listAttributes.size()) && (index < splittedMsg.length); i++)
        {
            att = listAttributes.get(i);

            if(!att.getNotApplicable())
            {
                if(att instanceof UcpChoice)
                {
                    UcpChoice choice = (UcpChoice)att;
                    //get choice parameter
                    String choiceName = choice.getChoiceAttribute().getName();
                    Attribute att2 = null;
                    String value = null;
                    boolean result = false;
                    //search this choice parameter in the attribute list
                    for(int j = 0; !result && (j < listAttributes.size()); j++)
                    {
                        att2 = listAttributes.get(j);
                        if(choiceName.equals(att2.getName()))
                        {
                            //get its value
                            value = (String)att2.getValue();
                            result = true;
                        }
                    }
                    result = false;

                    if((value != null) && (value.length() != 0))
                    {
                        //search value of the choice parameter in the choice attribute group list
                        for(int j = 0; !result && (j < ((Vector<UcpGroup>)choice.getValue()).size()); j++)
                        {
                            att2 = ((Vector<UcpGroup>)choice.getValue()).get(j);
                            if(((UcpGroup)att2).getChoiceValue().equals(value))
                            {
                                //set value of the group
                                for(int k = 0; (k < ((Vector<UcpAttribute>)att2.getValue()).size()) && (index < splittedMsg.length); k++)
                                {
                                    checkAttribute(((Vector<UcpAttribute>)att2.getValue()).get(k), splittedMsg[index++]);
                                }
                                result = true;
                            }
                        }
                    }
                    else
                    {
                        //skip the next attribute which could have been useful if choiceParameter was set
                        index += ((Vector)((Vector<UcpGroup>)choice.getValue()).get(0).getValue()).size();
                    }
                }
                else if(att.getName().equals("XSer"))
                {
                    //decode xser
                    String xser = splittedMsg[index++];
                    att.setValue(new Vector<UcpXser>());
                    UcpXser ser = null;

                    while(xser.length() > 0)
                    {
                        ser = new UcpXser();
                        ser.setType(xser.substring(0, 2));
                        ser.setLength(Integer.parseInt(xser.substring(2, 4), 16));//check store of value hexa in string
                        ser.setValue(xser.substring(4, ser.getLength()*2 + 4));//*2 because data is in hexa
                        ((Vector<UcpXser>)att.getValue()).add(ser);
                        xser = xser.substring(ser.getLength()*2 + 4);
                    }

                }
                else if(att.getValue() instanceof Vector)//for imbricate attribute
                {
                    Vector<UcpAttribute> vec = (Vector<UcpAttribute>)att.getValue();

                    for(int j = 0; (j < vec.size()) && (index < splittedMsg.length); j++)
                    {
                        checkAttribute(((UcpAttribute)vec.get(j)), splittedMsg[index++]);
                    }
                }
                else
                {
                    if(att.getOccurenceAttribute() != null)
                    {
                        //copy information of the attribute
                        UcpAttribute att2 = (UcpAttribute)att.clone();
                        att.setValue(new Vector<UcpAttribute>());
                        String value = (String)listAttributes.get(i-1).getValue();
                        int cpt = 0;

                        if((value != null) && (value.length() != 0))
                        {
                            //search occurence value (normally value of precedent attribute)
                            cpt = Integer.parseInt(value);
                        }

                        //get value for each value count in occurence
                        while(cpt != 0)
                        {
                            att2.setValue(splittedMsg[index++]);
                            ((Vector<UcpAttribute>)att.getValue()).add(att2);
                            att2 = att2.clone();//not not erase precedent value
                            cpt--;
                        }
                    }
                    else
                    {
                        checkAttribute((UcpAttribute)att, splittedMsg[index++]);
                    }
                }
            }
            else
            {
                //skip this parameter which should be empty
                //if not empty
                if(splittedMsg[index++].length() != 0)
                {
                    setLogError("Attribute " + att.getName() + " is not applicable to this message, it must have empty value\r\n");
                }
            }
        }

        if(index < (splittedMsg.length - 1))//all parameters were not set, (splittedMsg.length - 1) to not include checksum
        {
            setLogError("Incorrect parsing, some attributes are plus in message\r\n");
        }
        else if (index > (splittedMsg.length - 1))
        {
            setLogError("Incorrect parsing, some attributes are missing in message\r\n");
        }

        i=0;//reset i to count unknown attributes
        while(index < (splittedMsg.length - 1))
        {
            //register last parameters as unknown attributes
            attribute = new UcpAttribute();
            attribute.setValue(splittedMsg[index++]);
            attribute.setName("Unknown" + i);
            i++;
            addAttribute(attribute);
        }
    }

    private void checkAttribute(UcpAttribute att, String value) throws Exception
    {
        int lg = att.getLength();

        if((value.length() == 0) && att.isMandatory())//check if mandatory
        {
            setLogError("Malformed message: attribute " + att.getName() + " must not be empty\r\n");
        }
        else if((value.length() != 0) && (lg != -1) && (value.length() != lg))//check size
        {
            setLogError("Size of attribute " + att.getName() + " should be " + lg + " instead of " + value.length() + "\r\n");
        }
        else if(att.getFormat().equalsIgnoreCase("numericString"))//check format numericString
        {
            for(int i = 0; i < value.length(); i++)
            {
                if((value.charAt(i) < 0x30) || (value.charAt(i) > 0x39))
                {
                    setLogError("Attribute " + att.getName() + " should be a numeric string\r\n");
                    break;
                }
            }
        }
        else if(att.getFormat().equalsIgnoreCase("encodedString"))//check format string encoding
        {
            //Nothing to do usually
        }
        att.setValue(value);
    }

    @Override
    public UcpMessage clone()
    {
        UcpMessage clone = new UcpMessage();

        clone.setName(getName());
        clone.setMessageType(getMessageType());
        clone.setOperationType(getOperationType());
        for(int i = 0; i< listAttributes.size(); i++)
            clone.listAttributes.add(listAttributes.get(i).clone());
        
        return clone;
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        if(getLogError().length() != 0)
        {
            str.append(getLogError());
        }

        str.append(name).append(", operationType ").append(operationType);
        str.append(", transactionNumber ").append(transactionNumber).append(", length ");
        str.append(length).append(", O/R ").append(messageType).append("\r\n");
        
        for(int i = 0; i < listAttributes.size(); i++)
            str.append(listAttributes.get(i).toString());

        return str.toString();
    }
}
