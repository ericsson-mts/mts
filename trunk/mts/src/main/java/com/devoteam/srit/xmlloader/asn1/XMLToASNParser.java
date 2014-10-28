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

package com.devoteam.srit.xmlloader.asn1;

import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssSourceDiagnostic;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueOC;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceUser;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.EmbeddedData;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ObjectId;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssResult;

import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IEncoder;
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

	private static XMLToASNParser _instance;
	
    
    public static XMLToASNParser getInstance()
    {
    	if (_instance == null)
    	{
    		_instance = new XMLToASNParser();
    	}
    	return _instance;
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

    public void parseFromXML(String resultPath, ASNMessage message, Object objClass, Element root, String ClasseName) throws Exception 
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
            	
            	initField(resultPath, message, objClass, element, field, ClasseName);
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

    public Field findField(Object objClass, Element element) throws Exception
    {
    	String elementName = element.getName();
    	int pos = elementName.indexOf(ASNToXMLConverter.TAG_SEPARATOR);
    	if (pos > 0)
    	{
    		elementName = elementName.substring(0, pos);
    	}
        for (Field field : objClass.getClass().getDeclaredFields()) 
        {
        	String name = field.getName(); 
        	String type = field.getType().getSimpleName();
            if (name.equals(elementName)) 
            {
                return field;
            }
            else if (type.equals(elementName))
            {
                return field;
            }
            else if (type.equals("byte[]") && elementName.equalsIgnoreCase(ASNToXMLConverter.LABEL_TABLE_BYTE)) 
            {
                return field;
            }
            else if (name.equalsIgnoreCase("oidString") && elementName.equalsIgnoreCase("ObjectIdentifier")) 
            {
                return field;
            }
        }
        throw new ParsingException ("Can not find the attribute '" + elementName + "' in the ASN object '" + objClass.getClass().getName());
    }

    public Object parseField(String resultPath, ASNMessage message, Element element, Field field, String type, Object object, String className) throws Exception 
    {    	
    	// manage the embedded objects
    	Embedded embedded = message.getEmbeddedByInitial(type);
    	if (embedded == null && field != null)
    	{
    		embedded = message.getEmbeddedByInitial(field.getName());
    	}
		if (embedded != null) 
		{            
            String replace = embedded.getReplace();
            
            Class subClass = Class.forName(replace);
            Object objEmbbeded = subClass.newInstance();
            parseFromXML(resultPath, message, objEmbbeded, (Element) element.elements().get(0), className);
        	
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbeded, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
         
            Object obj = null;
            if (!type.equals("byte[]"))
            {
            	Class cl = Class.forName(type);
            	Constructor constr = cl.getConstructor();
				constr.setAccessible(true);
				obj = constr.newInstance();
				Field[] fields = cl.getDeclaredFields();
				fields[0].setAccessible(true);
				fields[0].set(obj, bytesEmbedded);
            }
            else
            {
            	obj = bytesEmbedded;
            }
            return obj;

		}
		Object value = null;
        if (type.equals("java.lang.Boolean")||type.equals("boolean"))  
        {
            value = Boolean.valueOf(element.getTextTrim()).booleanValue();
        }
        else if (type.equals("java.lang.String")||type.equals("String")) 
        {
            value =  element.getTextTrim();
        }
        else if (type.equals("java.lang.Integer")||type.equals("int")) 
        {
            value = Integer.parseInt(element.getTextTrim());
        }
        else if (type.equals("java.lang.Float")||type.equals("float"))  
        {
            value = Float.parseFloat(element.getTextTrim());
        }
        else if (type.equals("java.lang.Short")||type.equals("short"))  
        {
            value = Short.parseShort(element.getTextTrim());
        }
        else if (type.equals("java.lang.Long")||type.equals("long"))  
        {
            value = Long.parseLong(element.getTextTrim());
        }
        else if (type.equals("java.lang.Byte")||type.equals("byte"))  
        {
            value = Byte.parseByte(element.getTextTrim());
        }
        else if (type.equals("byte[]")) 
        {
        	// not a simple value so return
            return new DefaultArray(Utils.parseBinaryString("h" + element.getTextTrim())).getBytes();
        }
        else if (type.endsWith(".EnumType"))  
        {
        	String elementText = element.getTextTrim();
        	int pos = elementText.indexOf(ASNToXMLConverter.TAG_SEPARATOR);
        	if (pos > 0)
        	{
        		elementText = elementText.substring(0, pos);
        	}

			Class[] classes = object.getClass().getClasses();
			Object[] objects = null;
			if (classes.length >= 1)
			{
				objects = classes[0].getEnumConstants();
				Object objFind = null;
				for (int i=0; i <objects.length; i++)
				{
					objFind = objects[i];
					if (objFind.toString().equals(elementText))
					{
						break;
					}
				}
				// not a simple value so return
				return objFind;
			}
            return null;
        }
        else 
        {
            String classNameCurrent = type.substring(type.lastIndexOf(".") + 1);
            if (!className.equals("") && (type.contains(className)) && (!(type.equals(className + classNameCurrent)))) 
            {
                // static class : h225.h323_className$staticClass
                type = type.substring(0, type.lastIndexOf(".")) + "$" + type.substring(type.lastIndexOf(".") + 1);
            }

        	// calculate the element name to build the result path
        	String elementName = element.getName();
        	int iPos = elementName.indexOf(ASNToXMLConverter.TAG_SEPARATOR);
        	if (iPos > 0)
        	{
        		elementName = elementName.substring(0, iPos);
        	}
        	resultPath = resultPath + "." + elementName;

            Object obj = Class.forName(type).newInstance();
            //Object objComplexClass = this.instanceClass(obj.getClass().getName(), className);
            parseFromXML(resultPath, message, obj, element, className);
            // not a simple value so return
            return obj;
        }
        
        // get the condition for embedded objects
        String elementName = resultPath;
        int iPos = resultPath.lastIndexOf(".");
        if (iPos > 0)
        {
        	elementName = resultPath.substring(iPos + 1);
        }
    	String condition = elementName + "=" + value;
    	List<Embedded> embeddedList = ASNDictionary.getInstance().getEmbeddedByCondition(condition);
    	if (embeddedList != null)
    	{
    		message.addConditionalEmbedded(embeddedList);
    	}
    	
    	return value;
    }

    public void initField(String resultPath, ASNMessage message, Object objClass, Element element, Field field, String className) throws Exception 
    {
        // si le champ est privé, pour y accéder
        field.setAccessible(true);
		//System.out.println(f);
        
        // pour ne pas traiter les static
        if (field.toGenericString().contains("static")) 
        {
            return;
        }
        else if (field.getType().getCanonicalName().contains("Collection")) 
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
            	Object value = parseField(resultPath, message, elementInstance, null, collectionElementType.getCanonicalName(), objClass, className);
                // pour chaque <instance>
                listInstance.add(value);
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
        	// we add a embedded record in the list 
        	Object value = parseField(resultPath, message, element, field, field.getType().getCanonicalName(), objClass, className);
            field.set(objClass, value);
        }
    }
}
