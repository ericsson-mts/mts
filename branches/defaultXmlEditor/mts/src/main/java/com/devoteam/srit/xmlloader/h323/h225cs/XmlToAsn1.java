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

package com.devoteam.srit.xmlloader.h323.h225cs;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;

import gp.utils.arrays.DefaultArray;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author gansquer
 */
public class XmlToAsn1 {

    public XmlToAsn1() {
    }

    public static Document getDocumentXML(final String xmlFileName)
    {
        Document document = null;
        SAXReader reader = new SAXReader();
        reader.setEntityResolver(new XMLLoaderEntityResolver());       
        try 
        {
            document = reader.read(xmlFileName);
        }
        catch (DocumentException ex) 
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Wrong ASN1 file : ");
        }
        return document;
    }

    public void initObject(Object objClass, Element root, String ClasseName) throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InstantiationException 
    {
        // parsing XML
        List<Element> children = root.elements();
        for (Element element : children) 
        {
            Class thisClass = objClass.getClass();
            Field field = this.findField(objClass, element);
            initField(objClass, element, field, ClasseName);
        }
    }

    public Object instanceClass(String Classe, String ClasseName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!Classe.contains(ClasseName)) 
        {
            ClasseName = ClasseName + Classe;
        }
        else 
        {
            ClasseName = Classe;
        }
        Class thisClass = Class.forName(ClasseName);
        // get an instance
        Object iClass = thisClass.newInstance();
        return iClass;
    }

    public Field findField(Object objClass, Element element) 
    {
        for (Field field : objClass.getClass().getDeclaredFields()) 
        {
            if (element.getName().equals("instance")) 
            {
                return field;
            }
            if (field.getName().equals(element.getName())) {
                return field;
            }
        }
        return null;
    }

    public Object parseField(Element element, String type, String className) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException 
    {
        if (type.equals("java.lang.Boolean")||type.equals("boolean"))  
        {
            return Utils.parseBoolean(element.getTextTrim(), "type");
        }
        else if (type.equals("java.lang.String")||type.equals("String")) 
        {
            return element.getTextTrim();
        }
        else if (type.equals("java.lang.Integer")||type.equals("int")) 
        {
            return Integer.parseInt(element.getTextTrim());
        }
        else if (type.equals("java.lang.Float")||type.equals("float"))  
        {
            return Float.parseFloat(element.getTextTrim());
        }
        else if (type.equals("java.lang.Short")||type.equals("short"))  
        {
            return Short.parseShort(element.getTextTrim());
        }
        else if (type.equals("java.lang.Long")||type.equals("long"))  
        {
            return Long.parseLong(element.getTextTrim());
        }
        else if (type.equals("java.lang.Byte")||type.equals("byte"))  
        {
            return Byte.parseByte(element.getTextTrim());
        }
        else if (type.equals("byte[]")) 
        {
            return new DefaultArray(Utils.parseBinaryString(element.getTextTrim())).getBytes();
        }
        else 
        {
            String classNameCurrent = type.substring(type.lastIndexOf(".") + 1);
            if ((type.contains(className)) && (!(type.equals(className + classNameCurrent)))) 
            {
                // static class : h225.h323_className$staticClass
                type = type.substring(0, type.lastIndexOf(".")) + "$" + type.substring(type.lastIndexOf(".") + 1);
            }
            if (!type.contains(className)) 
            {
                className = "";
            }
            Object obj = Class.forName(type).newInstance();
            Object objComplexClass = this.instanceClass(obj.getClass().getName(), className);
            initObject(objComplexClass, element, className);
            return objComplexClass;
        }
    }

    public void initField(Object objClass, Element element, Field field, String ClasseName) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException 
    {
        // si le champ est privé, pour y accéder
        field.setAccessible(true);
        // pour ne pas traiter les static
        if (field.toGenericString().contains("static")) 
        {
            return;
        }
        if (field.getType().getCanonicalName().contains("Collection")) 
        {
            // type DANS la collection

            // Récupérer le type des élements de la collection
            Type[] elementParamTypeTab = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();

            // Exception si la collection n'a pas un seul argument
            if (elementParamTypeTab.length != 1) 
            {
                throw new RuntimeException("Message d'erreur");
            }

            Class collectionElementType = (Class) elementParamTypeTab[0];

            // creer la collection
            ArrayList<Object> listInstance = new ArrayList<Object>();

            // parcourir les enfants <instance> de element
            List<Element> children = element.elements("instance");
            for (Element elementInstance : children) 
            {
                // pour chaque <instance>
                listInstance.add(parseField(elementInstance, collectionElementType.getCanonicalName(), ClasseName));
            }
            // set la collection dans le field
            field.set(objClass, listInstance);
        }
        else 
        {
            field.set(objClass, parseField(element, field.getType().getCanonicalName(), ClasseName));
        }
    }
}
