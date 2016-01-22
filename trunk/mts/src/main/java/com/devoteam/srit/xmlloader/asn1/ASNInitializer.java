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
import com.devoteam.srit.xmlloader.core.coding.binary.ElementAbstract;
import com.devoteam.srit.xmlloader.core.coding.binary.EnumLongField;
import com.devoteam.srit.xmlloader.core.coding.binary.FieldAbstract;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;

import gp.utils.arrays.Array;
import gp.utils.arrays.RandomArray;
import gp.utils.arrays.SupArray;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import org.bn.CoderFactory;
import org.bn.IEncoder;
import org.bn.coders.ASN1PreparedElementData;
import org.bn.metadata.ASN1ElementMetadata;
import org.bn.types.BitString;
import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 *
 * @author fhenry
 */
public class ASNInitializer 
{

	private static ASNInitializer _instance;
    
    public static ASNInitializer getInstance()
    {
    	if (_instance == null)
    	{
    		_instance = new ASNInitializer();
    	}
    	return _instance;
    }

    public ASNInitializer() 
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

    public void initValue(int indexChoice, int index, String resultPath, ASNMessage message, Object parentObj, String name, Object objClass, ASN1ElementMetadata objElementInfo) throws Exception  
    {
		if (objClass ==  null)
    	{
	    	return;
	    }

		String strClass = objClass.getClass().toString();
    	int pos = strClass.lastIndexOf('.');
    	if (pos >= 0)
    	{
    		strClass = strClass.substring(pos + 1);
    	}
    	
		// get the XML tag
        String XMLTag = ASNToXMLConverter.getSignificantXMLTag(objClass, name);
    	
    	// we add a embedded record in the list		
        if (message !=null)
        {
			// get the condition for embedded objects
        	List<Embedded> embeddedList = message.getEmbeddedListWithCondition(resultPath, parentObj, name, objClass);
        	if (embeddedList != null)
        	{
        		message.addConditionalEmbedded(embeddedList);
        	}
        }
        
		// calculate resultPath
        resultPath = resultPath + "." + XMLTag; 

		// get the ASN1PreparedElementData
		ASN1PreparedElementData objPreparedEltData = ASNToXMLConverter.getASN1PreparedElementData(objClass);
		// calculate the metadata for the object
		String metadata = ASNToXMLConverter.calculateMetadata(objElementInfo, objPreparedEltData);
        
    	Field[] fields = objClass.getClass().getDeclaredFields();
		if (indexChoice < 0 || !metadata.contains(".Choice"))
		{
	        // parsing all fields 
	    	for (int i = 0; i < fields.length; i++)
	    	{
	    		Field field = fields[i];    		
	    		setValueField(indexChoice, i, field, resultPath, message, parentObj, objClass, objPreparedEltData);
			}
		}
		else
		{
			// parsing field # 0
			index = indexChoice % (fields.length - 1);
    		Field field = fields[index];    		
    		setValueField(indexChoice, index, field, resultPath, message, parentObj, objClass, objPreparedEltData);			
		}
    }

    
    private void setValueField(int indexChoice, int index, Field field, String resultPath, ASNMessage message, Object parentObj, Object objClass, ASN1PreparedElementData objElementInfo) throws Exception
    {
		field.setAccessible(true);
		//System.out.println(f);
		
		String typeField = field.getType().getCanonicalName();
		ASN1ElementMetadata subobjElementInfo = null;
		if (typeField != null && !typeField.equals("org.bn.coders.IASN1PreparedElementData"))
		{
			// get the PreparedElementSubData for the i index
			subobjElementInfo = ASNToXMLConverter.getASN1PreparedElementSubData(objElementInfo, index);
		}

		String name  = null;
		if (field != null)
		{
			name = field.getName();
		}
		
    	if (typeField == null)
    	{
    		// nothing to do
    	}
    	else if (typeField != null && typeField.equals("org.bn.coders.IASN1PreparedElementData") )
    	{
    		// nothing to do
    	}
    	else if (name != null && name.equals("integerForm") )
    	{
    		// nothing to do
    		// because error when decoding this field
    	}
    	else if (typeField != null && typeField.equals("java.util.Collection"))
		{
			ParameterizedType genType = (ParameterizedType) field.getGenericType();
			Type[] typeActualTypeArg = genType.getActualTypeArguments();
			LinkedList list = new LinkedList();
			if (typeActualTypeArg.length > 0)
			{
			for (int j = 0; j <= 2; j++)
				{	
					Object tabObject = null;
					if ("byte[]".equals(typeActualTypeArg[0].toString()))
					{
						int numByte = (int) Utils.randomLong(1, 10L);
						tabObject = Utils.randomBytes(numByte);
					}
					else
					{
						Class tabClass = (Class) typeActualTypeArg[0];
						tabObject = getSubObject(indexChoice, index, resultPath, message, parentObj, name, objClass, tabClass, subobjElementInfo);
					}
		    		if (tabObject != null)
		    		{
		    			list.add(tabObject);
		    		}
				}
				field.set(objClass, list);
			}
		}
		else
		{
    		Object subObject = getSubObject(indexChoice, index, resultPath, message, parentObj, name, objClass, field.getType(), subobjElementInfo);
    		if (subObject != null)
    		{
    			field.set(objClass, subObject);
    		}
		}				
    	
    }
    
    private Object getSubObject(int indexChoice, int index, String resultPath, ASNMessage message, Object parentObj, String name, Object obj, Class subClass, ASN1ElementMetadata objElementInfo) throws Exception
    {
    	String type = subClass.getCanonicalName();
    	if (type.equals("org.bn.coders.IASN1PreparedElementData") )
    	{
    		return null;
    		// nothing to do
    	}
		// manage the embedded objects
    	Embedded embedded = message.getEmbeddedByInitial(type);
		if (embedded != null) 
		{
			String replace = embedded.getReplace();
			Class cl = Class.forName(replace);
        	Object objEmbedded = getSubObject(indexChoice, index, resultPath, message, parentObj, name, obj, cl, objElementInfo);
        	
        	IEncoder<Object> encoderEmbedded = CoderFactory.getInstance().newEncoder("BER");
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        	encoderEmbedded.encode(objEmbedded, outputStream);
            byte[] bytesEmbedded = outputStream.toByteArray();
            // Array arraybytesEmbedded = new DefaultArray(bytesEmbedded);
            
            Constructor constr = subClass.getConstructor();
			constr.setAccessible(true);
			Object subObject = constr.newInstance();
			Field[] fields = subObject.getClass().getDeclaredFields();
			fields[0].setAccessible(true);
			fields[0].set(subObject, bytesEmbedded);
            
            return subObject;
        }
    	else if (type.equals("byte[]"))
    	{				
    		String simpleClassName = obj.getClass().getSimpleName();
			// get the element definition (enumeration binary data) from the dictionary
	    	ElementAbstract elementDico = null;
	    	byte[] bytes = null;
	    	if (message != null)
	    	{
	    		if ("Sm_RP_UI".equals(simpleClassName))
		    	{
	        		// case of the Sm-RP-UI element
		    		index = (byte) Utils.randomLong(0, 2);
		    		byte byteIndex = (byte) index;
		    		bytes = new byte[]{byteIndex};
		    	}
		    	elementDico = message.getElementFromDico(name, obj, resultPath, bytes);
	        	if (elementDico != null)
	        	{
	        		// TODO bug dans la fonction copyToClone() : retourne toujours un IntegerField
	        		// Est ce que c'est réellement un pb ? a voir à l'usage
	        		//ElementValue elementClone = new ElementValue(); 
	        		//elementClone.copyToClone(elementDico);
	        		elementDico.initValue(index, message.dictionary);
	        		
	        		// case of the Sm-RP-UI element
	        		ElementAbstract element0 = elementDico.getElement(0);
	        		if (element0 != null && "Sm_RP_UI".equals(simpleClassName))
	        		{
		        		SupArray arrayElement0 = element0.getFieldsArray();
		        		FieldAbstract fieldTPMTI = element0.getFieldsByName("TP-MTI");
		        		String strIndex = new Integer(index).toString();
		        		fieldTPMTI.setValue(strIndex, 6, arrayElement0);
	        		}
	        		
	        		Array array = null;
	        		try
	        		{
	        			array = elementDico.encodeToArray(message.dictionary);
	            		return array.getBytes();
	        		}
	        		catch (Exception e)
	        		{
	        			// nothing to do
	        		}
	        	}
	    	}
	    	
	        int numByte = (int) Utils.randomLong(1, 10L);
        	return Utils.randomBytes(numByte);
    	}
    	else if (type.equals("java.lang.Boolean") || type.equals("boolean"))
    	{
			return Utils.randomBoolean();
		} 
		else if (type.equals("java.lang.Long") || type.equals("long"))
		{
			long l = Utils.randomLong(0, 4294967295L);
			return l;
		}
		else if (type.equals("java.lang.Integer") || type.equals("int"))
		{
			/* TODO
	    	ElementAbstract elementDico = null;
	    	if (message != null)
	    	{
		    	elementDico = message.getElementFromDico(obj, resultPath);
	    	}

        	if (elementDico != null)
        	{
        		elementDico.initValue(index, message.dictionary);
        		FieldAbstract field = elementDico.getField(0);
        		
    			if (field instanceof EnumLongField)
    			{
    				Long objLong = ((EnumLongField) field).getLongValue(elementDico.getFieldsArray());
	        		if (objLong != null)
	            	{
	            		obj = new Integer(objLong.byteValue());
	            		return obj;
	            	}
    			}
        	}
        	*/

			//return (int) Utils.randomLong(0, 265735L);
			return (int) Utils.randomLong(0, 2L);
		} 
		else if (type.equals("java.lang.String"))
		{
			int numChar = (int) Utils.randomLong(0, 20L);
			return Utils.randomString(numChar);
		}
		else if (type.equals("org.bn.types.BitString"))
		{
			int numBit = (int) Utils.randomLong(0, 20L);
    		Array data = new RandomArray(numBit);
			String str = new String(data.getBytes());
			BitString bstr = new BitString();
			bstr.setValue(str.getBytes());
			return bstr;
		}
		else if (type.equals("org.bn.types.ObjectIdentifier"))
		{
			int numInt = (int) Utils.randomLong(0L, 10L);
			ObjectIdentifier objId = new ObjectIdentifier();
			String str = Utils.randomObjectIdentifier(numInt);
			// oid shall start with some predeined prefixes
			objId.setValue("0.1.2" + str);
			return  objId;
		}
		else if (type.endsWith(".EnumType"))
		{
			Class[] classes = obj.getClass().getClasses();
			Object[] objects = null;
			if (classes.length >= 1)
			{
				objects = classes[0].getEnumConstants();
				if (objects !=null && objects.length > 0)
				{
					int numChar = (int) Utils.randomLong(0, objects.length - 1);
					Field[] fields = objects[numChar].getClass().getDeclaredFields();
					return objects[numChar];
				}
				else
				{
					Class[] subClasses = classes[0].getClasses();
					objects = subClasses[0].getEnumConstants();
					if (objects !=null && objects.length > 0)
					{
						int numChar = (int) Utils.randomLong(0, objects.length - 1);
						return objects[numChar];
					}
				}
			}
			return null;
		}
		else
		{
			Constructor constr = subClass.getConstructor();
			constr.setAccessible(true);
			Object subObj = constr.newInstance();
			initValue(indexChoice, index, resultPath, message, obj, name, subObj, objElementInfo);
			return subObj;
		}
    }
        
}
