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

package com.devoteam.srit.xmlloader.sigtran.fvo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import java.util.LinkedList;
import org.dom4j.Node;

/**
 *
 * @author Julien Brisseau
 */
public class FvoDictionary {

    private FvoParameter header;
    private HashMap<Integer, FvoMessage> codeToMessage;

    /**
     * Constructor
     *
     * @param stream		: fvoDictionary XML file
     * @throws Exception
     */
    public FvoDictionary(InputStream stream) throws Exception {
        codeToMessage = new HashMap<Integer, FvoMessage>();
        parseFile(stream);
    }

    /**
     * Get the header structure of the protocol
     * 
     * @return				: the header structure of the protocol
     */
    public FvoParameter getHeader() {
        return header;
    }

    /**
     * Parse the dictionary from the XML file
     * 
     * @param stream		:fvoDictionary XML file
     * @throws Exception
     */
    final void parseFile(InputStream stream) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);

        //HEADER
        Node headerNode = document.selectSingleNode("/dictionary/header");
        if (headerNode != null) 
        {
            Element root = (Element) headerNode;
            header = parseParameter(root);
            header.setMessageLength(Integer.decode(root.attributeValue("length")));
        }

        //MESSAGES
        List listMessages = document.selectNodes("/dictionary/messages/message");
        for (Object object:listMessages) 
        {
            Element root = (Element) object;
            FvoMessage message = new FvoMessage(null, this);

            String typeName = root.attributeValue("typeName");
            message.setName(typeName);
            String typeCode = root.attributeValue("typeCode");
            message.setMessageType(Integer.decode(typeCode));
            List parameters = root.elements("parameter");
            //PARAMETER OF EACH MESSAGE
            for (int j = 0; j < parameters.size(); j++) 
            {
                Element param = (Element) parameters.get(j);

                String name = param.attributeValue("name");
                
                FvoParameter parameter;
                // try to find a detailed definition of the parameter
                Element def = (Element) document.selectSingleNode("/dictionary/parameters/parameter[@name=\"" + name + "\"]");
                if(null != def)
                {
                    parameter = parseParameter(def);
                }
                else
                {
                    parameter = new FvoParameter(null);
                }
                if (name != null)
                {
                	parameter.setName(name);
                }
                String type = param.attributeValue("type");
                if (type != null)
                {
                	parameter.setType(type);
                }
                String length = param.attributeValue("length");
                if (length != null)
                {
                	parameter.setMessageLength(Integer.decode(length));
                }
                String littleEndian = param.attributeValue("littleEndian");
                if(littleEndian != null){
                    parameter.setLittleEndian(Boolean.parseBoolean(littleEndian));
                }

                if (parameter.getType().equalsIgnoreCase("F")) {
                    message.getFparameters().add(parameter);
                }
                if (parameter.getType().equalsIgnoreCase("V")) {
                    message.getVparameters().add(parameter);
                }
                if (parameter.getType().equalsIgnoreCase("O")) {
                    message.getOparameters().add(parameter);
                }
            }
            this.codeToMessage.put(message.getMessageType(), message);
        }


        //ENUMERATIONS
        //TODO
    }

    /**
     * Parse a parameter from the dictionary file
     * 
     * @param node			: root of the parameter
     * @throws Exception
     */
    FvoParameter parseParameter(Element node) throws Exception {

        //ATTRIBUTES
        FvoParameter parameter = new FvoParameter(null);
        String parameterName = node.attributeValue("name");
        parameter.setName(parameterName);
        String parameterId = node.attributeValue("id");
        if(parameterId != null){
            parameter.setId(Integer.decode(parameterId));
        }
        String longParameter = node.attributeValue("longParameter");
        if (longParameter != null) {
            parameter.setLongParameter(Boolean.parseBoolean(longParameter));
        }
        String littleEndian = node.attributeValue("littleEndian");
        if(littleEndian != null){
            parameter.setLittleEndian(Boolean.valueOf(littleEndian));
        }

        //FIELDS
        List listFields = node.elements("field");
        parameter.setFields(new LinkedList<FvoField>());
        for (int i = 0; i < listFields.size(); i++) {
            FvoField field = parseField((Element) listFields.get(i));

            // inherit littleEndian status from parameter
            field.setLittleEndian(parameter.isLittleEndian());

            parameter.getFields().add(field);
        }
        return parameter;
    }

    /**
     * Parse a field from the dictionary file
     * 
     * @param fieldNode		: root of the field to parse
     * @return				: the fvoField parsed
     * @throws Exception
     */
    FvoField parseField(Element fieldNode) throws Exception {
        String fieldName = fieldNode.attributeValue("name");
        String fieldFormat = fieldNode.attributeValue("format");
        String fieldLengthBit = fieldNode.attributeValue("lengthBit");
        String fieldLength = fieldNode.attributeValue("length");
        String fieldValue = fieldNode.attributeValue("enumerationId");
        FvoField field = new FvoField(null);
        if (fieldLengthBit != null) {
            try {
                field.setLengthBit(0, Integer.decode(fieldLengthBit));
            }
            catch (Exception e) {
                throw new ExecutionException("a field fieldLengthBit must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        if (fieldLength != null) {
            try {
                field.setLength(Integer.decode(fieldLength));
            }
            catch (Exception e) {
                throw new ExecutionException("a field fieldLength must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        if (fieldFormat != null) {
            field.setFormat(fieldFormat);
        }
        else {
            field.setFormat("integer");
        }
        field.setValue(fieldValue);
        field.setName(fieldName);

        return field;
    }

    /**
     * Return the FvoMessage from the id of the message
     * 
     * @param messageType	: id of the message
     * @return	the FvoMessage from the id of the message
     */
    FvoMessage getMessage(int messageType) {
        return codeToMessage.get(messageType);
    }
}
