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

package com.devoteam.srit.xmlloader.sigtran.ap;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

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
public class XMLToASNParser 
{

	static XMLToASNParser _instance;
	
    
    public static XMLToASNParser getInstance()
    {
    	if (_instance != null)
    	{
    		return _instance;
    	}
    	return new XMLToASNParser();
    }

    public XMLToASNParser() 
    {
    }

    public static Document getDocumentXML(final String xmlFileName)
    {
        Document document = null;
        SAXReader reader = new SAXReader();
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
            if (field != null)
            {
            	//Object subObject = instanceClass(field.getType().getCanonicalName(), ClasseName);
            	//field.setAccessible(true);            	
            	//field.set(objClass, subObject);
            	//initObject(subObject, element, ClasseName);
            	
            	//field.setAccessible(true); 
            	//field.set(objClass, parseField(element, field.getType().getCanonicalName(), ClasseName));
            	
            	initField(objClass, element, field, ClasseName);
            }
        }
    }

    public Object instanceClass(String Classe, String ClasseName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        if (!Classe.contains(".")) 
        {
            ClasseName = ClasseName + Classe;
        }
        else 
        {
            ClasseName = Classe;
        }
        Class thisClass = Class.forName(ClasseName);
        Object iClass = thisClass.newInstance();
        return iClass;
    }

    public Field findField(Object objClass, Element element) 
    {
    	String elementName = element.getName();
        for (Field field : objClass.getClass().getDeclaredFields()) 
        {
        	String name = field.getName(); 
        	String type = field.getType().getCanonicalName();
            if (name.contains(elementName)) 
            {
                return field;
            }
            else if (type.contains(elementName))
            {
                return field;
            }
            else if (type.equals("byte[]") && elementName.equals("bytes")) 
            {
                return field;
            }
        }
        return null;
    }

    public Object parseField(Element element, String type, Object object, String className) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException 
    {
        if (type.equals("java.lang.Boolean")||type.equals("boolean"))  
        {
            return Boolean.valueOf(element.getTextTrim()).booleanValue();
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
            return new DefaultArray(Utils.parseBinaryString("h" + element.getTextTrim())).getBytes();
        }
        else if (type.contains("EnumType"))  
        {
			Class[] classes = object.getClass().getClasses();
			Object[] objects = null;
			if (classes.length >= 1)
			{
				objects = classes[0].getEnumConstants();
				Object objFind = null;
				for (int i=0; i <objects.length; i++)
				{
					objFind = objects[i];
					if (objFind.toString().equals(element.getTextTrim()))
					{
						break;
					}
				}
				return objFind;
			}
            return null;
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
		//System.out.println(f);
        
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
            List<Element> children = element.elements();
            for (Element elementInstance : children) 
            {
                // pour chaque <instance>
                listInstance.add(parseField(elementInstance, collectionElementType.getCanonicalName(), objClass, ClasseName));
            }
            /*
            List<Element> children1 = element.elements("value");
            for (Element elementInstance : children1) 
            {
                // pour chaque <instance>
                listInstance.add(parseField(elementInstance, collectionElementType.getCanonicalName(), objClass, ClasseName));
            }
            */
            
            // set la collection dans le field
            field.set(objClass, listInstance);
        }
        else 
        {
            field.set(objClass, parseField(element, field.getType().getCanonicalName(), objClass, ClasseName));
        }
    }
}
