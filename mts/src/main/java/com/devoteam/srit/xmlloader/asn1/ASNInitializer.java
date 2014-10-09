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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.RandomArray;
import gp.utils.arrays.SupArray;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import org.bn.CoderFactory;
import org.bn.IEncoder;
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

    public void setValue(ASNMessage message, Object objClass) throws Exception  
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
    	
        // parsing object object fields 
    	Field[] fields = objClass.getClass().getDeclaredFields();
    	for (int i= 0; i < fields.length; i++)
    	{
    		Field f = fields[i];
    		f.setAccessible(true);
    		//System.out.println(f);

    		String typeField = f.getType().getCanonicalName();
    		String typeName  = f.getName();
        	if (typeField == null)
        	{
        		// nothing to do
        	}
        	else if (typeField != null && typeField.equals("org.bn.coders.IASN1PreparedElementData") )
	    	{
	    		// nothing to do
	    	}
        	else if (typeName != null && typeName.equals("integerForm") )
	    	{
        		// nothing to do
        		// because error when decoding this field
	    	}
        	else if (typeField != null && typeField.equals("java.util.Collection"))
			{
				ParameterizedType genType = (ParameterizedType) f.getGenericType();
				Type[] typeActualTypeArg = genType.getActualTypeArguments();
				LinkedList list = new LinkedList();
				if (typeActualTypeArg.length > 0)
				{
				for (int j = 0; j <= 2; j++)
					{		
						Class tabClass = (Class) typeActualTypeArg[0];
						Object tabObject = getSubObject(message, objClass, tabClass);	
			    		if (tabObject != null)
			    		{
			    			list.add(tabObject);
			    		}
					}
					f.set(objClass, list);
				}
			}
			else
			{
	    		Object subObject = getSubObject(message, objClass, f.getType());
	    		if (subObject != null)
	    		{
	    			f.set(objClass, subObject);
	    		}
			}				
		}
    }

    
    
    
    private Object getSubObject(ASNMessage message, Object obj, Class subClass) throws Exception
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
        	Object objEmbedded = getSubObject(message, obj, cl);
        	
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
    		int numByte = (int) Utils.randomLong(0, 20L);
    		Array data = new RandomArray(numByte);
    		SupArray supArray = new SupArray();
    		supArray.addLast(data);
    		// add a tag to be compliant with asn1 data
    		Array tag = new DefaultArray(new byte[]{4, (byte)numByte});
    		supArray.addFirst(tag);
    		return supArray.getBytes();
    	}
    	else if (type.equals("java.lang.Boolean") || type.equals("boolean"))
    	{
			return Utils.randomBoolean();
		} 
		else if (type.equals("java.lang.Long") || type.equals("long"))
		{
			long l = Utils.randomLong(0, 1000000000000000000L);
			return l;
		}
		else if (type.equals("java.lang.Integer") || type.equals("int"))
		{
			return (int) Utils.randomLong(0, 1000000000L);
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
			String str = randomObjectIdentifier(numInt);
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
			setValue(message, subObj);
			return subObj;
		}
    }
    
	public static String randomObjectIdentifier(int numInt)
	{
	    StringBuilder strBuilder = new StringBuilder();
	    for (int j = 0; j < numInt; j++)
	    {
	    	int b = (byte) Utils.randomLong(0, 128L) & 0x00FF;	    	
	    	strBuilder.append(String.valueOf(b));
	    	if (j != numInt - 1)
	    	{
	    		strBuilder.append('.');
	    	}
	    }
	    return strBuilder.toString();
	}
    
}
