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

    public void initObject(Object objClass, Element root, String ClasseName) throws Exception 
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

    public Field findField(Object objClass, Element element) throws Exception
    {
    	String elementName = element.getName();
    	int pos = elementName.indexOf(".");
    	if (pos > 0)
    	{
    		elementName = elementName.substring(0, pos);
    	}
        for (Field field : objClass.getClass().getDeclaredFields()) 
        {
        	String name = field.getName(); 
        	String type = field.getType().getCanonicalName();
            if (name.equals(elementName)) 
            {
                return field;
            }
            else if (type.endsWith("." + elementName))
            {
                return field;
            }
            else if (type.equals("byte[]") && elementName.equals("bytes")) 
            {
                return field;
            }
        }
        throw new ParsingException ("Can not find the attribute '" + elementName + "' in the ASN object '" + objClass.getClass().getName());
    }

    public Object parseField(Element element, String type, Object object, String className) throws Exception 
    {
    	// manage the embedded objects
    	Embedded embedded = ASNDictionary.getInstance().getEmbeddedByInitial(type);
		if (embedded != null) 
		{            
            String replace = embedded.getReplace();
            
            Class subClass = Class.forName(replace);
            Object objEmbbeded = subClass.newInstance();
            initObject(objEmbbeded, (Element) element.elements().get(0), className);
        	
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbeded, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
         
            Class cl = Class.forName(type);
            Constructor constr = cl.getConstructor();
			constr.setAccessible(true);
			Object obj = constr.newInstance();
			Field[] fields = cl.getDeclaredFields();
			fields[0].setAccessible(true);
			fields[0].set(obj, bytesEmbedded);
            return obj;

		}
		else if (type.equals("java.lang.Boolean")||type.equals("boolean"))  
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
        else if (type.endsWith(".EnumType"))  
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
        else if (type.endsWith(".DialogueOC"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName(className + "ExternalPDU").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((DialogueOC) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".EmbeddedData"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName(className + "DialoguePDU").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((EmbeddedData) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".ObjectId"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName("org.bn.types.ObjectIdentifier").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((ObjectId) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".AssResult"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_result").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((AssResult) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".AssSourceDiagnostic"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_source_diagnostic").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((AssSourceDiagnostic) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".DialogueServiceUser"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_user").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((DialogueServiceUser) obj).setValue(bytesEmbedded);
            
            return obj;
        }
        else if (type.endsWith(".DialogueServiceProvider"))
        {
            Object obj = Class.forName(type).newInstance();

            Object objEmbeded = Class.forName("com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_provider").newInstance();
            Object objEmbbededClass = this.instanceClass(objEmbeded.getClass().getName(), className);
            initObject(objEmbbededClass, (Element) element.elements().get(0), className);
        	
            // encode ASN1 object into binary
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbbededClass, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            //Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            ((DialogueServiceProvider) obj).setValue(bytesEmbedded);
            
            return obj;
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
            initObject(obj, element, className);
            return obj;
        }
    }

    public void initField(Object objClass, Element element, Field field, String className) throws Exception 
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
                // pour chaque <instance>
                listInstance.add(parseField(elementInstance, collectionElementType.getCanonicalName(), objClass, className));
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
            field.set(objClass, parseField(element, field.getType().getCanonicalName(), objClass, className));
        }
    }
}
