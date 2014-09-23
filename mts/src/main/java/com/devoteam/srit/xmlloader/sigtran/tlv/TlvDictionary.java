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

package com.devoteam.srit.xmlloader.sigtran.tlv;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.sigtran.MsgSigtran;
import com.devoteam.srit.xmlloader.sigtran.StackSigtran;

/**
 *
 * @author Julien Brisseau
 */
public class TlvDictionary {

    private HashMap<String, Integer> messageClassValue;
    private HashMap<Integer, String> messageClassName;
    private HashMap<String, String> messageTypeValue;
    private HashMap<String, String> messageTypeName;
    private HashMap<String, TlvParameter> parameter;
    private HashMap<Integer, String> parameterName;
    private HashMap<String, String> enumerationFromCode;
    private HashMap<String, String> enumerationFromName;
    private int _ppid;


    public TlvDictionary(InputStream stream, Stack stack) throws Exception {


        this.messageClassValue = new HashMap<String, Integer>();
        this.messageClassName = new HashMap<Integer, String>();
        this.messageTypeValue = new HashMap<String, String>();
        this.messageTypeName = new HashMap<String, String>();
        this.parameter = new HashMap<String, TlvParameter>();
        this.parameterName = new HashMap<Integer, String>();
        this.enumerationFromCode = new HashMap<String, String>();
        this.enumerationFromName = new HashMap<String, String>();

        this.parseFile(stream);
    }

    private void parseFile(InputStream stream) throws Exception {
        //		variables
        int i = 0;
        int j = 0;
        int k = 0;

        SAXReader reader = new SAXReader();
        Document document = reader.read(stream);

        Element root = (Element) document.selectSingleNode("/dictionary");
        String ppidStr = root.attributeValue("ppid");
        _ppid = Integer.parseInt(ppidStr);

        //Class and type
        List listClassType = document.selectNodes("/dictionary/classType/class");
        for (i = 0; i < listClassType.size(); i++) {
            Element classNode = (Element) listClassType.get(i);
            String classId = classNode.attributeValue("id");
            String className = classNode.attributeValue("name");

            List listType = classNode.elements("type");
            for (j = 0; j < listType.size(); j++) {
                Element typeNode = (Element) listType.get(j);
                String typeId = typeNode.attributeValue("id");
                String typeName = typeNode.attributeValue("name");
                String class_Type = classId;
                class_Type += ":" + typeId;

                messageTypeValue.put(typeName, typeId);
                messageTypeName.put(class_Type, typeName);
            }
            try {
                messageClassValue.put(className, (Integer.decode(classId)));
                messageClassName.put((Integer.decode(classId)), className);
            }
            catch (Exception e) {
                throw new ExecutionException("a class id must be an Integer\n" + classNode.asXML().replace("	", ""));
            }

        }
        //parameters

        List listParameters = document.selectNodes("/dictionary/parameters/parameter");
        for (i = 0; i < listParameters.size(); i++) {
            Element parameterNode = (Element) listParameters.get(i);
            String parameterName = parameterNode.attributeValue("name");
            String parameterTag = parameterNode.attributeValue("tag");
            TlvParameter parameter = new TlvParameter(null, this);
            if (parameterTag != null) {
                try {
                    parameter.setTag(Integer.decode(parameterTag));
                }
                catch (Exception e) {
                    throw new ExecutionException("a tag of a parameter must be an Integer\n" + parameterNode.asXML().replace("	", ""));
                }
            }
            parameter.setName(parameterName);
            this.parameter.put(parameterName, parameter);
            this.parameterName.put(parameter.getTag(), parameterName);

            List listFields = parameterNode.elements("field");
            for (j = 0; j < listFields.size(); j++) {
                TlvField field = parseField((Element) listFields.get(j));
                parameter.getFields().add(field);

            }
        }

        //enumerations
        List listEnumeration = document.selectNodes("/dictionary/enumerations/enumeration");
        for (i = 0; i < listEnumeration.size(); i++) {
            Element enumerationNode = (Element) listEnumeration.get(i);
            List listValue = enumerationNode.elements("value");
            String enumerationName = enumerationNode.attributeValue("name");

            for (j = 0; j < listValue.size(); j++) //-value-
            {
                Element valueNode = (Element) listValue.get(j);
                List listField = valueNode.elements("field");
                String valueName = valueNode.attributeValue("name");
                String valueCode = valueNode.attributeValue("code");

                if (valueCode != null) {
                    try {
                        Integer.decode(valueCode);
                    }
                    catch (Exception e) {
                        throw new ExecutionException("a valueCode must be an Integer\n" + valueNode.asXML().replace("	", ""));
                    }
                }

                String enumerationName_code = enumerationName + ":" + valueCode;
                enumerationFromCode.put(enumerationName_code, valueName);
                String enumerationName_name = enumerationName + ":" + valueName;
                enumerationFromName.put(enumerationName_name, valueCode);
            }
        }


    }

    private TlvField parseField(Element fieldNode) throws Exception {

        String fieldName = fieldNode.attributeValue("name");
        String fieldStart = fieldNode.attributeValue("start");
        String fieldFormat = fieldNode.attributeValue("format");
        String fieldLengthBit = fieldNode.attributeValue("lengthBit");
        String fieldLength = fieldNode.attributeValue("length");
        String fieldStartBit = fieldNode.attributeValue("startBit");
        String fieldValue = fieldNode.attributeValue("enumerationId");
        TlvField field = new TlvField(null, this);
        if (fieldStart != null) {
            try {
                //field.setStart(Integer.decode(fieldStart));
            }
            catch (Exception e) {
                throw new ExecutionException("a fieldStart must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        if (fieldStartBit != null) {
            try {
                //field.setStartBit(Integer.decode(fieldStartBit));
            }
            catch (Exception e) {
                throw new ExecutionException("a fieldStartBit must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        if (fieldLengthBit != null) {
            try {
                field.setLengthBit(Integer.decode(fieldLengthBit));
            }
            catch (Exception e) {
                throw new ExecutionException("a fieldfieldLengthBit must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        if (fieldLength != null) {
            try {
                field.setLength(Integer.decode(fieldLength));
            }
            catch (Exception e) {
                throw new ExecutionException("a fieldfieldLength must be an Integer\n" + fieldNode.asXML().replace("	", ""));
            }
        }
        field.setFormat(fieldFormat);
        field.setValue(fieldValue);
        field.setName(fieldName);

        return field;
    }

    public int getPpid(){
        return _ppid;
    }

    public String messageClassName(int classValue) {
        return messageClassName.get(classValue);
    }

    public int messageClassValue(String className) {
        return messageClassValue.get(className);
    }

    public String messageTypeName(int classValue, int typeValue) {
        String classType = ((Integer) classValue).toString();
        classType += ":" + ((Integer) typeValue).toString();
        return messageTypeName.get(classType);
    }

    public int messageTypeValue(String typeName) {
        return Integer.decode(messageTypeValue.get(typeName));
    }

    public String parameterName(int tagValue) {
        return parameterName.get(tagValue);
    }

    public TlvParameter parameter(int tagValue, Msg msg) {
        String tagName = parameterName.get(tagValue);
        TlvParameter param = parameter.get(tagName);
        if (param != null) {
            param.setMsg(msg);
        }
        return param;
    }

    public TlvParameter parameter(String tagName, Msg msg) {
        try {
            int tagValue = Integer.decode(tagName);
            return parameter(tagValue, msg);
        }
        catch (Exception e) {
        }
        TlvParameter param = parameter.get(tagName);
        if (param != null) {
            param.setMsg(msg);
        }
        return param;
    }


    public String getEnumerationNameFromCode(String fieldName, String enumName){
        return enumerationFromCode.get(fieldName + ":" + enumName);
    }

    public String getEnumerationCodeFromName(String fieldName, String enumCode){
        return enumerationFromName.get(fieldName + ":" + enumCode);
    }

//    public TlvParameter enumeration(String enumerationName, int code, MsgSigtran msg) {
//        String enumerationName_code = enumerationName + ":" + ((Integer) code).toString();
//        TlvParameter param = enumerationFromCode.get(enumerationName_code);
//        if (param != null) {
//            param.setMsg(msg);
//        }
//        return param;
//    }
//
//    public TlvParameter enumeration(String enumerationName,String valueName, MsgSigtran msg) {
//        String enumerationName_name = enumerationName + ":" + valueName;
//        TlvParameter param = enumerationFromName.get(enumerationName_name);
//        if (param != null) {
//            param.setMsg(msg);
//        }
//        return param;
//    }
}
