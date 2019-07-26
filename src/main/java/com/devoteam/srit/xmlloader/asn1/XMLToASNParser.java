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

import com.devoteam.srit.xmlloader.asn1.data.ElementValue;
import com.devoteam.srit.xmlloader.asn1.dictionary.ASNDictionary;
import com.devoteam.srit.xmlloader.asn1.dictionary.Embedded;
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumLongField;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumStringField;
import com.devoteam.srit.xmlloader.core.coding.binary.FieldAbstract;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssSourceDiagnostic;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueOC;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceUser;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.EmbeddedData;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.ObjectId;
import com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssResult;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IEncoder;
import org.bn.annotations.ASN1EnumItem;
import org.bn.types.BitString;
import org.bn.types.ObjectIdentifier;
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

    public void parseFromXML(String resultPath, ASNMessage message, Object objClass, Element root, String ClasseName) throws Exception 
    {
    	// calculate resultPath
        resultPath = resultPath + "." + getSignificantXMLTag(root);

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

    public Object instanceClass(String Classe, String ClasseName) throws Exception
    {
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

	private String getSignificantXMLTag(Element element) throws Exception 
	{
		String XMLTag = element.getName();
        int pos = XMLTag.indexOf('.');
    	if (pos >= 0)
    	{
    		XMLTag = XMLTag.substring(0, pos);
    	}
		return XMLTag;
	}

    public Object parseField(String resultPath, ASNMessage message, Element element, Field field, String type, Object object, String className) throws Exception 
    {    	
    	// get the embedded definition form the message (for conditional) and the dictionary
    	Embedded embedded =  null; 
    	String name = null;
    	if (field != null)
    	{
    		name = field.getName();
    	}
    	if (message != null)
    	{
    		if (field != null)
    		{
    			embedded =  message.getEmbeddedFromDico(name, type);
    		}
    	}
    	// process embedded object
		if (embedded != null) 
		{            
            // calculate resultPath
            resultPath = resultPath + "." + getSignificantXMLTag(element);
            
            String replace = embedded.getReplace();
            Class<?> subClass = Class.forName(replace);
            Object objEmbedded = subClass.newInstance();
                       
            if (!element.elements().isEmpty())
            {
            	parseFromXML(resultPath, message, objEmbedded, (Element) element.elements().get(0), className);
            }
            return processEmbeddedObject(objEmbedded, type);
		}
		
		Object obj = null;
		String value = null;
        if (type.equals("java.lang.Boolean") || type.equals("boolean"))  
        {
        	value = element.getTextTrim();
        	obj = Utils.parseBoolean(value, name);
        }
        else if (type.equals("java.lang.Byte") || type.equals("byte"))  
        {
        	value = element.getTextTrim();
        	Long objLong = processEnumLong(name, resultPath, message, object, value);
        	if (objLong != null)
        	{
        		obj = new Byte(objLong.byteValue());
        	} 
        	else
        	{
        		obj = Byte.parseByte(value);
        	}
            value = obj.toString();
        }

        else if (type.equals("java.lang.Short") || type.equals("short"))  
        {
        	value = element.getTextTrim();
        	Long objLong = processEnumLong(name, resultPath, message, object, value);
        	if (objLong != null)
        	{
        		obj = new Short(objLong.shortValue());
        	} 
        	else
        	{
        		obj = Short.parseShort(value);
        	}
            value = obj.toString();
        }
        else if (type.equals("java.lang.Integer") || type.equals("int")) 
        {
        	value = element.getTextTrim();
        	Long objLong = processEnumLong(name, resultPath, message, object, value);
        	if (objLong != null)
        	{
        		obj = new Integer(objLong.intValue());
        	} 
        	else
        	{
        		obj = Integer.parseInt(value);
        	}
            value = obj.toString();
        }
        else if (type.equals("java.lang.Long") || type.equals("long"))  
        {
        	value = element.getTextTrim();
        	obj = processEnumLong(name, resultPath, message, object, value);
        	if (obj == null)
        	{
        		obj = Long.parseLong(value);
        	}
        	value = obj.toString();
        }
        else if (type.equals("java.lang.Float") || type.equals("float"))  
        {
        	value = element.getTextTrim();
            obj = Float.parseFloat(value);
            value = obj.toString();
        }
        else if (type.equals("java.lang.Double") || type.equals("double"))  
        {
        	value = element.getTextTrim();
            obj = Double.parseDouble(value);
            value = obj.toString();
        }
        else if (type.equals("org.bn.types.BitString")) 
        {
        	// calculate resultPath
            resultPath = resultPath + "." + name;
            
        	Element elt = element.element("BitString");
        	if (elt != null)
        	{
	        	value = elt.attributeValue("value");
	        	value = processEnumString(name, resultPath, message, object, value);
	        	String trailing = elt.attributeValue("trailing");
	        	int intTrail = Integer.parseInt(trailing);
	        	obj = new BitString();
	        	Array array = Array.fromHexString(value);	        		
	        	((BitString) obj).setValue(array.getBytes(), intTrail);
        	}
        }
        else if (type.equals("java.lang.String")||type.equals("String")) 
        {
        	value = element.getTextTrim();
        	value = processEnumString(name, resultPath, message, object, value);
        	obj =  value;
        }
        else if (type.equals("org.bn.types.ObjectIdentifier")) 
        {
        	// calculate resultPath
            resultPath = resultPath + "." + name;

        	value = element.element("ObjectIdentifier").getTextTrim();
        	value = processEnumString(name, resultPath, message, object, value);
        	obj =  new ObjectIdentifier();
        	((ObjectIdentifier) obj).setValue(value);
        }
        else if (type.equals("byte[]")) 
        {   
        	String elementName = object.getClass().getSimpleName();
        	boolean logWarn =  
        			  !elementName.equals("OrigTransactionID") &&
        			  !elementName.equals("DestTransactionID") &&
        			  !elementName.equals("Invoke") &&
        			  !elementName.equals("ReturnResult") &&
        			  !elementName.equals("ReturnError") &&
					  !elementName.equals("PrivateExtension") &&
					  !elementName.equals("CallReferenceNumber");

        	// get the element definition (enumeration binary data) from the dictionary
        	ElementAbstract elementDico = null;
        	if (message != null)
        	{
    	    	elementDico = message.getElementFromDico(name, object, resultPath);
        	}
        	if (elementDico == null && logWarn)
        	{
        		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, null, 
        			"The ASN1 element \"" + elementName + "." + element.getName() + "\" is not defined into the dictionary to analyze the received messages more finely.");
        	}

        	// TODO bug dans la fonction copyToClone() : retourne toujours un IntegerField
    		// Est ce que c'est réellement un pb ? a voir à l'usage
    		//ElementAbstract elementClone = new ElementValue();
    		//elementClone.copyToClone(elementDico);
    		ElementAbstract elmt = new ElementValue(null);
        	elmt.parseFromXML(element, message.dictionary, elementDico, false);
        	Array array = elmt.encodeToArray(message.dictionary);
        	if (array.length > 0)
        	{
        		return array.getBytes();
        	}
        	
        	if (logWarn)
        	{
        		GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, null, 
        			"The ASN1 element \"" + elementName + "." + element.getName() + "\" is not specified as a list of XML <field> tag.");
        	}

        	// not defined as a list of XML <field> tag
        	value = element.getTextTrim();
        	byte[] bytes = Utils.parseBinaryString("h" + value);
    		array = new DefaultArray(bytes);
    		value = Array.toHexString(array);
    		value = processEnumString(name, resultPath, message, object, value);
    		array = Array.fromHexString(value);
    		return array.getBytes();
        }
        else if (type.endsWith(".EnumType"))  
        {
        	value = element.getTextTrim();
        	int position = value.indexOf(ASNToXMLConverter.TAG_SEPARATOR);
        	String strVal = null;
        	int intVal = -1;
        	if (position > 0)
        	{
        		strVal = value.substring(0, position);
        		intVal = Integer.parseInt(value.substring(position + 1));
        	}
        	else
        	{
        		strVal = value;
        		intVal = Integer.parseInt(value);
        	}

			Class[] classes = object.getClass().getClasses();
			Object[] objects = null;
			if (classes.length >= 1)
			{
				objects = classes[0].getEnumConstants();
				Object objFind = null;
				boolean found = false;
				for (int i=0; i <objects.length; i++)
				{
					objFind = objects[i];
					if (objFind.toString().equals(strVal))
					{
						found = true;
						break;
					}
				}
				ASN1EnumItem enumObj = null;
	            for(Field enumItem: objFind.getClass().getDeclaredFields()) 
	            {
	                if(enumItem.isAnnotationPresent(ASN1EnumItem.class)) 
	                {
	                    if(enumItem.getName().equals(strVal)) 
	                    {
	                    	enumObj = enumItem.getAnnotation(ASN1EnumItem.class);
	                        break;
	                    }
	                }
	            }
	            if (enumObj != null && enumObj.tag() != intVal)
	            {
	            	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, null, 
	            			"For the ASN1 enumeration \"" + object.getClass().getSimpleName() + "\", the label \"" + strVal + "\" does not match the value \"" + intVal + "\"");
	            }
	            
				if (!found)
				{
					String possibleValues = "";
					for (int i=0; i <objects.length; i++)
					{
						possibleValues += objects[i];
						possibleValues += ",";
					}
					possibleValues = possibleValues.substring(0, possibleValues.length() - 1);  
					throw new ParsingException("For the ANS1 enumeration \"" + object.getClass().getSimpleName() + 
						"\", the value \"" + value + "\" is not allowed according to the ASN1 file; possible values are one of [" + possibleValues + "]");
				}
				// not a simple value so return
				return objFind;
			}
            return null;
        }
        else 
        {      
        	// case of static inner classes
        	if (type.startsWith(className))
        	{
	            String typeWithoutPackage = type.substring(className.length());
	            typeWithoutPackage = typeWithoutPackage.replace('.', '$');
	            type = className + typeWithoutPackage;
        	}
            obj = Class.forName(type).newInstance();
            //Object objComplexClass = this.instanceClass(obj.getClass().getName(), className);
            parseFromXML(resultPath, message, obj, element, className);
            // not a simple value so return
            return obj;
        }
            	
        // we add a embedded record in the list
        if (message != null)
        {
        	// get the condition for embedded objects
	    	String XMLTag = resultPath;
	    	int pos = resultPath.lastIndexOf('.');
	    	if (pos >= 0)
	    	{
	    		XMLTag = resultPath.substring(pos + 1);
	    	}
	    	String condition = XMLTag + "=" + value;
	    	List<Embedded> embeddedList = message.getEmbeddedByCondition(condition);
	    	if (embeddedList != null)
	    	{
	    		message.addConditionalEmbedded(embeddedList);
	    	}
        }
    	return obj;
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
            // Récupérer le type des élements de la collection
        	ParameterizedType genType = (ParameterizedType) field.getGenericType();
			Type[] typeActualTypeArg = genType.getActualTypeArguments();

            // Exception si la collection n'a pas un seul argument
            if (typeActualTypeArg.length != 1) 
            {
                throw new RuntimeException("Message d'erreur");
            }

            String nameClass = null; 
			if ("byte[]".equals(typeActualTypeArg[0].toString()))
			{
				 nameClass = typeActualTypeArg[0].toString();
			}
			else
			{	
				nameClass = ((Class) typeActualTypeArg[0]).getCanonicalName();
			}

            // creer la collection
            ArrayList<Object> listInstance = new ArrayList<Object>();

            // parcourir les enfants <instance> de element
            List<Element> children = element.elements();
            for (Element elementInstance : children) 
            {
            	Object value = parseField(resultPath, message, elementInstance, null, nameClass, objClass, className);
                // pour chaque <instance>
                listInstance.add(value);
            }
            
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
    
    public static Object processEmbeddedObject(Object objEmbedded, String type) throws Exception
	{    
		IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		encoderEmbedded.encode(objEmbedded, outputStream);
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

    public static Long processEnumLong(String name, String resultPath, ASNMessage message, Object object, String value) throws Exception
    {
    	Long obj = null;
		// get the element definition (enumeration binary data) from the dictionary
		ElementAbstract elementDico = null;
		if (message != null)
		{
	    	elementDico = message.getElementFromDico(name, object, resultPath);
		}
		if (elementDico != null)
		{
			FieldAbstract field = elementDico.getField(0);
			if (field instanceof EnumLongField)
			{
				EnumLongField fld = (EnumLongField) elementDico.getField(0);
				obj = fld.getEnumLong(value);
			}
		}
		return obj;
    }
    
    public static String processEnumString(String name, String resultPath, ASNMessage message, Object object, String value) throws Exception
    {
		// get the element definition (enumeration binary data) from the dictionary
    	ElementAbstract elementDico = null;
    	if (message != null)
    	{
	    	elementDico = message.getElementFromDico(name, object, resultPath);
    	}
    	if (elementDico != null)
    	{
    		FieldAbstract field = elementDico.getField(0);
    		if (field instanceof EnumStringField)
			{
    			EnumStringField fld = (EnumStringField) elementDico.getField(0);
    			value = fld.getEnumString(value);
			}
    	}
    	return value;
    }
}
