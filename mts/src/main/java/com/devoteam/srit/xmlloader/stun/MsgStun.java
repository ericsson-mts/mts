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

package com.devoteam.srit.xmlloader.stun;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.utils.arrays.Array;
import gp.utils.arrays.ConstantArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import gp.utils.arrays.Integer16Array;
import gp.utils.arrays.MacArray;
import gp.utils.arrays.SupArray;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
/**
 *
 * @author indiaye
 */
public class MsgStun extends Msg {

    private HeaderStun header;
    private LinkedList<AttributeStun> listAttributeStun = new LinkedList<AttributeStun>();
    private Array secret = null;
    private boolean longTermCredential = false;
    private String realm = "";
    private String username = "";

    MsgStun(Element root) {
        super();
        header = new HeaderStun();
        parseHeader(root.element("header"));

        List<Element> attributes = root.elements("attribute");
        for (Element attribute : attributes) {
            if (attribute.attributeValue("type").equalsIgnoreCase("realm")) {
                this.longTermCredential = true;
            }
            parseAttribute(attribute);
        }
    }

    MsgStun(Array data) throws Exception {
        super();
        header = new HeaderStun(data);
        int offset = 20;
        int length = data.length;
        while (offset < length) {
            int typeInt = new Integer16Array(data.subArray(offset, 2)).getValue();
            // make sure offset is multiple of 4
            AttributeStun att = null;
            switch (typeInt) {
                case 3:
                    att = new AttributeStunChangeRequest(data.subArray(offset));
                    break;
                case 1:
                case 2:
                case 4:
                case 5:
                case 11:
                case 32:
                case 32803:
                    att = new AttributeStunAddress(data.subArray(offset));
                    break;
                case 20:
                    this.longTermCredential = true;
                case 6:
                case 7:
                case 32802:
                case 21:
                    att = new AttributeStunText(data.subArray(offset));
                    break;
                case 9:
                    att = new AttributeStunErrorCode(data.subArray(offset));
                    break;
                case 10:
                    att = new AttributeStunUnknownAttribute(data.subArray(offset));
                    break;
                default:
                    att = new AttributeStunBinary(data.subArray(offset));
                    break;
            }

            listAttributeStun.addLast(att);
            offset += att.getPaddedLength() + 4;
        }


    }

    @Override
    public String getProtocol() {
        return StackFactory.PROTOCOL_STUN;
    }

    @Override
    public String getType() throws Exception {
        return Integer.toString(this.header.getType());
    }

    @Override
    public String getResult() throws Exception {
        int type = this.header.getHeader().getBits(2, 2);
        switch (type) {
            case 2:
                return "SUCCESS";
            case 3:
                return "FAILED";

            default:
                return Integer.toString(type);

        }
    }

    @Override
    public boolean isRequest() throws Exception {

        if (this.header.getHeader().getBit(7) == 0) {

            return true;
        } else {

            return false;
        }
    }

    private void parseHeader(Element elem) {
        header.setType(Integer.parseInt(elem.attributeValue("type")));
        header.setTransactionId(Array.fromHexString(elem.attributeValue("transactionID")));

    }

    private void parseAttribute(Element elem) {
        List<Element> children = elem.elements();
        for (Element child : children) {
            AttributeStun attribute = null;
            String typeInHexa = (String) DictionnaryStun.readProperties().get(elem.attributeValue("type"));
            if (child.getName().equalsIgnoreCase("address")) {
                attribute = new AttributeStunAddress(Array.fromHexString(typeInHexa), Integer.parseInt(child.attributeValue("family")), Integer.parseInt(child.attributeValue("port")), child.attributeValue("addressIP"));
            } else if (child.getName().equalsIgnoreCase("text")) {
                if (elem.attributeValue("type").equalsIgnoreCase("username"))//attribute username
                {
                    this.username = child.attributeValue("text");
                }
                if (elem.attributeValue("type").equalsIgnoreCase("realm"))//attribute realm
                {
                    this.realm = child.attributeValue("text");
                }


                attribute = new AttributeStunText(Array.fromHexString(typeInHexa), child.attributeValue("value"));

            } else if (child.getName().equalsIgnoreCase("binary")) {
                attribute = new AttributeStunBinary(Array.fromHexString(typeInHexa), Array.fromHexString(child.attributeValue("value")));

            } else if (child.getName().equalsIgnoreCase("errorCode")) {
                attribute = new AttributeStunErrorCode(Array.fromHexString(typeInHexa), Integer.parseInt(child.attributeValue("code")), child.attributeValue("reasonPhrase"));

            } else if (child.getName().equalsIgnoreCase("changeRequest")) {
                attribute = new AttributeStunChangeRequest(Array.fromHexString(typeInHexa), child.attributeValue("changeIP"), child.attributeValue("changePort"));

            } else if (child.getName().equalsIgnoreCase("unknownAttribute")) {
                String[] tab = child.attributeValue("type").split(",");
                int[] tabType = new int[tab.length];
                for (int i = 0; i < tabType.length; i++) {
                    tabType[i] = Integer.parseInt(tab[i]);
                }
                attribute = new AttributeStunUnknownAttribute(Array.fromHexString(typeInHexa), tabType);

            } else if (child.getName().equalsIgnoreCase("messageIntegrity")) {
                try {

                    String valueMessageIntegrity = child.attributeValue("value");
                    Array integrityArray = null;
                    if (null != valueMessageIntegrity) {
                        try {
                            integrityArray = Array.fromHexString(valueMessageIntegrity);
                        } catch (Exception e) {
                            valueMessageIntegrity = null;
                        }
                    }

                    if (null == valueMessageIntegrity) {
                        if (this.longTermCredential) {
                            String key = username + ":" + realm + ":" + child.attributeValue("secret");
                            DigestArray keyarray = new DigestArray(new DefaultArray(key.getBytes()), "HmacMD5");
                            integrityArray = new MacArray(new DefaultArray(this.getBytesData()), "HmacSHA1", keyarray);
                        } else {
                            DefaultArray arraysecret = new DefaultArray(child.attributeValue("secret").getBytes());
                            integrityArray = new MacArray(new DefaultArray(this.getBytesData()), "HmacSHA1", arraysecret);
                        }
                    }

                    attribute = new AttributeStunBinary(Array.fromHexString(typeInHexa), integrityArray);

                } catch (Exception e) {
                    throw new IllegalArgumentException("The secret can not be empty", e);
                }

            }
            this.listAttributeStun.add(attribute);
            header.setLength(header.getLength() + attribute.getPaddedLength() + 4);

        }



    }

    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if ((null != var) && (var.length() > 0)) {
            return var;
        }
        
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if (params[0].equalsIgnoreCase("header")) {
            if (!params[1].equalsIgnoreCase("")) {
                var = this.header.getParameterHeader(params[1]);
            }
        } else if (params[0].equalsIgnoreCase("attribute") && params.length == 3) {
            int typeAtt = Integer.valueOf((String) DictionnaryStun.readProperties().get(params[1]), 16).intValue();
            for (AttributeStun att : this.listAttributeStun) {
                if (att.getType() == typeAtt) {
                    var = att.getParameterAtt(params[2]);
                }
            }
        } else {
            throw new Exception("the value must be header.XX or attribute.XXX.XXX");
        }
        return var;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData() {
        SupArray array = new SupArray();
        array.addLast(header.getValue());
        for (AttributeStun attributeStun : listAttributeStun) {
            array.addLast(attributeStun.getArray());
            int padding = attributeStun.getPaddedLength() - attributeStun.length.getValue();
            if (padding > 0) {
                array.addLast(new ConstantArray((byte) 0, padding));
            }
        }
        return array.getBytes();
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
        StringBuilder StringBuilder = new StringBuilder();
        StringBuilder.append(super.toShortString());
        StringBuilder.append("<MESSAGE type= \"" + this.getType() + "\"/>");
        return StringBuilder.toString();
    }
    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
        StringBuilder message = new StringBuilder();
        message.append("\r\n");
        message.append(this.header.toString());
        message.append("\r\n");
        for (AttributeStun attributeStun : listAttributeStun) {
            message.append(attributeStun.toString());
            message.append("\r\n");
        }
        return message.toString();

    }
}
