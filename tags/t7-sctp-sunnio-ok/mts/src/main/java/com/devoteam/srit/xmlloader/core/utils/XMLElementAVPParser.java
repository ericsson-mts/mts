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
package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.diameter.MsgDiameterParser;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * specific replacer for <sendMessageDIAMETER> operation : duplicate the XML tags if there are multiple value in all parameters on the XML tag (like avp) if the parameter is empty, then the XML tag is
 * removed also used for <stats> operation
 *
 * @author gpasquiers
 */
public class XMLElementAVPParser implements XMLElementReplacer, Serializable 
{

    static private XMLElementReplacer instance = null;

    static public XMLElementReplacer instance() {
        if (null == instance) {
            instance = new XMLElementAVPParser();
        }
        return instance;
    }
    private XMLElementReplacer xmlElementDefaultParser;
    private XMLElementReplacer xmlElementTextOnlyParser;

    protected XMLElementAVPParser() {
        xmlElementDefaultParser = XMLElementDefaultParser.instance();
        xmlElementTextOnlyParser = XMLElementTextOnlyParser.instance();
    }

    public List<Element> replace(Element element, ParameterPool parameterPool) throws Exception {
        List<Element> result;

        if (element.getName().equalsIgnoreCase("header") || element.getName().startsWith("send")) {
            result = xmlElementDefaultParser.replace(element, parameterPool);
        }
        else // <avp .../>
        {
            LinkedList list = new LinkedList<Element>();

            List<Attribute> attributes;
            attributes = element.attributes();

            int allowedParameterLength = -1;
            boolean hasParameter = false;

            for (Attribute attribute : attributes) {
                String value = attribute.getValue();
                Matcher matcher = Parameter.pattern.matcher(value);

                while (matcher.find()) {
                    String variableStr = matcher.group();

                    if (false == parameterPool.isConstant(variableStr)) {
                        Parameter variable = parameterPool.get(variableStr);
                        hasParameter = true;
                        if (variable != null) {
                            if (allowedParameterLength == -1 || allowedParameterLength == 1) {
                                allowedParameterLength = variable.length();
                            }
                            else if (variable.length() != 1 && allowedParameterLength != variable.length()) {
                                throw new ExecutionException("Invalid length of variables : a variable of length " + allowedParameterLength + " has been found but " + variableStr + " has a length of " + variable.length());
                            }
                        }
                    }
                }
            }

            if (!hasParameter) {
                allowedParameterLength = 1;
            }

            for (int i = 0; i < allowedParameterLength; i++) {
                Element newElement = element.createCopy();

                List<Attribute> newElementAttributes;
                newElementAttributes = newElement.attributes();

                for (Attribute newAttribute : newElementAttributes) {
                    String value = newAttribute.getValue();

                    Pattern pattern = Pattern.compile(Parameter.EXPRESSION);
                    Matcher matcher = pattern.matcher(value);
                    int offset = 0;
                    while (matcher.find()) {
                        String before = value.substring(0, matcher.end() + offset - 1);
                        String after = value.substring(matcher.end() + offset - 1);

                        if (parameterPool.exists(matcher.group())) {
                            int index = i;
                            if (parameterPool.get(matcher.group()).length() == 1) {
                                index = 0;
                            }

                            value = before + "(" + index + ")" + after;
                            offset += ((String) "(" + index + ")").length();
                        }

                    }
                    newAttribute.setValue(value);
                }

                List<Element> tempList = xmlElementDefaultParser.replace(newElement, parameterPool);
                try {
                    for (Element e : tempList) {
                        // MsgDiameterParser.getInstance().doDictionnary(e, "0", false);

                        list.addAll(xmlElementTextOnlyParser.replace(e, parameterPool));
                    }
                }
                catch (Exception e) {
                    throw new ExecutionException("Error while checking parsed variables against dictionary", e);
                }




            }
            result = list;
        }

        return result;
    }
}
