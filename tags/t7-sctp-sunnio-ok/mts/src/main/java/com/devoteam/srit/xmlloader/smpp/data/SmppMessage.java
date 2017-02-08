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

package com.devoteam.srit.xmlloader.smpp.data;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.dictionaryElement.Attribute;
import com.devoteam.srit.xmlloader.core.utils.gsm.GSMConversion;
import gp.utils.arrays.*;
import java.util.Vector;

/**
 *
 * @author Benjamin Bouvier
 */
public class SmppMessage
{
    private String name;
    private Array header;
    private Integer32Array length;
    private Integer32Array id;
    private Integer32Array status;
    private Integer32Array sequenceNumber;

    private Vector<SmppAttribute> attributs;
    private Vector<SmppTLV> tlvs;
    private Array data = null;

    private String logError = "";
    
    public SmppMessage()
    {        
        header         = new DefaultArray(16);
        length         = new Integer32Array(header.subArray(0, 4));
        id             = new Integer32Array(header.subArray(4, 4));
        status         = new Integer32Array(header.subArray(8, 4));
        sequenceNumber = new Integer32Array(header.subArray(12, 4));

        attributs      = new Vector<SmppAttribute>();
        tlvs           = new Vector<SmppTLV>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(int value)
    {
        this.length.setValue(value);
    }

    public int getLength()
    {
        return this.length.getValue();
    }
    
    public void setId(int value)
    {
        this.id.setValue(value);
    }

    public int getId()
    {
        return this.id.getValue();
    }

    public void setStatus(int status)
    {
        this.status.setValue(status);
    }

    public int getStatus()
    {
        return this.status.getValue();
    }

    public void setSequenceNumber(int value)
    {
        this.sequenceNumber.setValue(value);
    }

    public int getSequenceNumber()
    {
        return this.sequenceNumber.getValue();
    }

    public String getLogError()
    {
        return logError;
    }

    public void setLogError(String logError)
    {
        this.logError += logError;
    }
    
    public void addAttribut(SmppAttribute att)
    {
        attributs.add(att);
    }

    public SmppAttribute getAttribut(String name)
    {
        SmppAttribute att = null;
        for(int i = 0; i < attributs.size(); i++)
        {
            att = attributs.get(i);
            if((att != null) && (att.getName().equalsIgnoreCase(name)))
            {
                return att;
            }
        }
        return null;
    }

    public void addTLV(SmppTLV tlv)
    {
        tlvs.add(tlv);
    }

    public SmppTLV getTLV(String name)
    {
        SmppTLV tlv;
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

    public SmppTLV getTLV(int tag)
    {
        SmppTLV tlv;
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

    public Array getArray() throws Exception
    {
        SupArray array = new SupArray();
        SmppAttribute att;
        for(int i = 0; i < attributs.size(); i++)
        {
            att = attributs.get(i);
            if(att.getName().equalsIgnoreCase("sm_length") && ((Integer)att.getValue() == -1))
            {
                //set sm_length field automatically with the good length
                att.setValue(((Array)attributs.get(i+1).getValue()).length);
            }

            array.addLast(att.getArray());
        }
        for(SmppTLV tlv:tlvs)
        {
            array.addLast(tlv.getArray());
        }

        if((getId() == 0) || (getName().equalsIgnoreCase("Unknown message")))//in case of unknown message and data present
        {
            if(data != null)
                array.addLast(data);
        }
        
        setLength(array.length + 16);
        array.addFirst(this.header);
        return array;
    }
    
    public void parseArray(Array array) throws Exception
    {
        setLength(new Integer32Array(array.subArray(0, 4)).getValue());
        setId(new Integer32Array(array.subArray(4, 4)).getValue());
        setStatus(new Integer32Array(array.subArray(8, 4)).getValue());
        setSequenceNumber(new Integer32Array(array.subArray(12, 4)).getValue());

        SmppAttribute att = null;
        Object att2 = null;
        SmppTLV tlv = null;
        int i = 0;
        int index = 16;

        data = new DefaultArray(array.subArray(index, length.getValue() - index).getBytes());

        if(getId() != 0)//for message wich are known
        {
            for(i = 0; i < attributs.size(); i++)
            {
                att = attributs.get(i);
                if(att.getValue() instanceof Vector)//in case of attribute occurence
                {
                    //get the occurence value
                    String occurence = att.getOccurenceAttribute();
                    int occurenceValue = 0;
                    for(int j = 0; j < attributs.size(); j++)
                    {
                        if(attributs.get(j).getName().equals(occurence))
                        {
                            occurenceValue = (Integer) attributs.get(j).getValue();
                        }
                    }

                    //need to duplicate vector of imbricatte attribute in the occurence vector
                    for(int j = 1; j < occurenceValue; j++)
                    {
                        //this way is to use the clone method
                        Vector<Attribute> newVec = new Vector<Attribute>();
                        for(int k = 0; k < ((Vector)((Vector)att.getValue()).get(0)).size(); k++)
                            newVec.add(((Attribute)((Vector)((Vector)att.getValue()).get(0)).get(k)).clone());
                        ((Vector)att.getValue()).add(newVec);
                    }

                    //manage multiple occurence of attribute
                    for(int cptOc = 0; cptOc < occurenceValue; cptOc++)
                    {
                        if(index >= getLength())
                        {
                            setLogError("Message " + getName() +" is incomplete\r\n");
                        }
                        Vector<SmppAttribute> newVec = (Vector) ((Vector)att.getValue()).get(cptOc);

                        //manage choice here in plus
                        for(int j = 0; j < newVec.size(); j++)
                        {
                            att2 = newVec.get(j);
                            if(att2 instanceof SmppChoice)
                            {
                                SmppChoice choice = (SmppChoice)att2;
                                String value = null;
                                SmppGroup attGroup = null;

                                //get the choice parameter in the attribute list
                                att2 = (SmppAttribute)newVec.get(j-1);
                                
                                //get its value
                                value = Integer.toString((Integer)((SmppAttribute)att2).getValue());
                                //set choice attribute value in choice with this value
                                choice.getChoiceAttribute().setValue((Integer)((SmppAttribute)att2).getValue());

                                if((value != null) && (value.length() != 0))
                                {
                                    //search value of the choice parameter in the choice attribute group list
                                    for(int k = 0; k < ((Vector<SmppGroup>)choice.getValue()).size(); k++)
                                    {
                                        attGroup = ((Vector<SmppGroup>)choice.getValue()).get(k);
                                        if(attGroup.getChoiceValue().equals(value))
                                        {
                                            //set value of the group with value in the scenario
                                            for(int l = 0; l < ((Vector<SmppAttribute>)attGroup.getValue()).size(); l++)
                                            {
                                                //check name and then set value
                                                att2 = ((Vector<SmppAttribute>)attGroup.getValue()).get(l);
                                                index = decodeValue(((SmppAttribute)att2), (Vector<SmppAttribute>)attGroup.getValue(), array, index, j);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            else //for a simple attribute
                            {
                                index = decodeValue(((SmppAttribute)att2), (Vector<SmppAttribute>)att.getValue(), array, index, j);
                            }
                        }
                    }
                }
                else
                {
                    index = decodeValue(att, attributs, array, index, i);
                    if(index > getLength())
                    {
                        setLogError("Message " + getName() +" is incomplete\r\n");
                    }
                }
            }

            //get Mandatory TLV
            for(i = 0; i < tlvs.size(); i++)
            {
                tlv = tlvs.get(i);//first tlv are mandatory, the next are optional
                if(tlv.isMandatory())
                {
                    if((index + 4) >= getLength())
                    {
                        setLogError("Message " + getName() +" is incomplete\r\n");
                    }

                    //check the tag and then get the length
                    if(tlv.getTag() == (new Integer16Array(array.subArray(index, 2)).getValue()))
                    {
                        index += 2;
                        tlv.setLength(new Integer16Array(array.subArray(index, 2)).getValue());
                        index += 2;

                        //check that length is comprise between sizeMin and sizeMax to set sizeMax for reading
                        if((tlv.getLength() < tlv.getSizeMin()) || (tlv.getLength() > tlv.getSizeMax()))
                            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "TLV length for " + tlv.toString() + "is not according to size given in dictionary");
                        tlv.setSizeMax(tlv.getLength());//use to call generic decode method

                        index = decodeValue(tlv, null, array, index, i);
                    }
                    else
                    {
                        setLogError("TLV <" + new Integer16Array(array.subArray(index, 2)).getValue() + "> don't correspond with expected TLV <" + tlv.getTag() + ">");
                    }
                }
                else//when all mandatory tlv have been parsed
                {
                    break;
                }
            }

            //if index is not at the message length, there are optionnal tlvs
            while(index < getLength())
            {
                if((index + 2) < getLength())//check tag exists
                {
                    //check that tlv tag get is known in the message and get it
                    tlv = getTLV(new Integer16Array(array.subArray(index, 2)).getValue());
                    if(tlv != null)
                    {
                        index += 2;

                        if((index + 2) < getLength())//check length exists
                        {
                            tlv.setLength(new Integer16Array(array.subArray(index, 2)).getValue());
                            index += 2;

                            if((index + tlv.getLength()) <= getLength())//check value exists
                            {
                                tlv.setSizeMax(tlv.getLength());//use to call generic decode method
                                index = decodeValue(tlv, null, array, index, 0);
                            }
                            else
                            {
                                setLogError("Cannot decode the value of optionnal TLV " + tlv.getName() + ", stop decoding message\r\n");
                                break;
                            }
                        }
                        else
                        {
                            setLogError("Cannot decode the length of optionnal TLV " + tlv.getName() + ", stop decoding message\r\n");
                            break;
                        }
                    }
                    else
                    {
                        setLogError("Optionnal TLV " + new Integer16Array(array.subArray(index, 2)).getValue() + " is unknown, stop decoding message\r\n");
                        break;
                    }
                }
                else
                {
                    setLogError("Cannot decode optionnal TLV, stop decoding message\r\n");
                    break;
                }
            }
        }
    }

    private int decodeValue(Attribute attribut, Vector<SmppAttribute> listAtt, Array array, int index, int cpt) throws Exception
    {
        if(attribut.getFormat().equalsIgnoreCase("INT"))
        {
            switch(attribut.getSizeMax())
            {
                case 1:
                    attribut.setValue(new Integer08Array(array.subArray(index, 1)).getValue());
                    index += 1;
                    break;
                case 2:
                    attribut.setValue(new Integer16Array(array.subArray(index, 2)).getValue());
                    index += 2;
                    break;
                case 4:
                    attribut.setValue(new Integer32Array(array.subArray(index, 4)).getValue());
                    index += 4;
                    break;
                default:
                    setLogError("size " + attribut.getSizeMax() + " of attribute for int don't exists, format is " + attribut.getFormat());
            }
        }
        else if(attribut.getFormat().equalsIgnoreCase("C-OCTETSTRING"))
        {
            StringBuilder value = new StringBuilder();
            char octet = (char)array.get(index);
            index += 1;
            value.append(octet);
            if((octet != 0) && (attribut.getSizeMax() > attribut.getSizeMin()))
            {
                //-2 because the last octet is \0 and there is no need to put it in the string because added in the attribute
                value.append(new String(array.subArray(index, attribut.getSizeMax()-2).getBytes()));
                index += attribut.getSizeMax()-1;//-1 to increment index over the \0 octet

                attribut.setValue(new String(GSMConversion.fromGsmCharset(value.toString())));
            }
            else
            {
                attribut.setValue("");
            }
        }
        else if(attribut.getFormat().equalsIgnoreCase("C-OCTETSTRING-VAR"))
        {
            StringBuilder value = new StringBuilder();
            char octet = 1;
            int j = 0;

            while((octet != 0) && (j < attribut.getSizeMax()))//get octet by octet
            {
                octet = (char)array.get(index);
                index += 1;
                if(octet != 0)
                    value.append(octet);
                j++;
            }

            attribut.setValue(new String(GSMConversion.fromGsmCharset(value.toString())));
        }
        else if(attribut.getFormat().equalsIgnoreCase("OCTETSTRING"))
        {
            //WARNING: this type is just used for short_message attribute
            //which is based on sm_length, the precedent attribute
            int size = 0;
            if(attribut.getName().equalsIgnoreCase("short_message"))//for attribute
            {
                size = (Integer)listAtt.get(cpt-1).getValue();
            }
            else//for tlv
            {
                size = attribut.getSizeMax();
            }

            //check staying byte in array before getting them
            int lastByteCount = array.length - index;
            if(lastByteCount >= size)
                attribut.setValue(new DefaultArray(GSMConversion.fromGsmCharset(new String(array.subArray(index, size).getBytes()))));
            else
                attribut.setValue(new DefaultArray(GSMConversion.fromGsmCharset(new String(array.subArray(index, lastByteCount).getBytes()))));
            index += size;//add size to index in the twa case to return a bad index if size > lastBytesCount and print a log message
        }
        return index;
    }

    @Override
    public SmppMessage clone()
    {
        SmppMessage clone = new SmppMessage();

        clone.setName(getName());
        clone.setLength(length.getValue());
        clone.setId(id.getValue());
        clone.setStatus(status.getValue());
        clone.setSequenceNumber(sequenceNumber.getValue());
        for(int i=0; i< attributs.size(); i++)
            clone.attributs.add(attributs.get(i).clone());
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

        str += name + ", length " + length + ", id " + id.toString() + ", status " + status + ", seqNum " + sequenceNumber + "\r\n";
        for(int i = 0; i < attributs.size(); i++)
            str += attributs.get(i).toString();
        for(int i = 0; i < tlvs.size(); i++)
            str += tlvs.get(i).toString();
        if((attributs.size() == 0) && (tlvs.size() ==0) && (data != null))
            str += "data: " + data.toString();
        return str;
    }
}
