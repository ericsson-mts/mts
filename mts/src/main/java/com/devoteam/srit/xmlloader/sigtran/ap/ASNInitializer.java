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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

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

	static ASNInitializer _instance;
    
    public static ASNInitializer getInstance()
    {
    	if (_instance != null)
    	{
    		return _instance;
    	}
    	return new ASNInitializer();
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

    public void setValue(Object objClass) throws Exception  
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
        	/*
        	else if (typeName != null && typeName.equals("extId") )
	    	{
        		// nothing to do
        		// because error when decoding this field
	    	}
        	else if (typeName != null && typeName.equals("extType") )
	    	{
        		// nothing to do
        		// because error when decoding this field
	    	}
	    	*/	    	
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
						Object tabObject = getSubObject(objClass, tabClass);	
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
	    		Object subObject = getSubObject(objClass, f.getType());
	    		if (subObject != null)
	    		{
	    			f.set(objClass, subObject);
	    		}
			}				
		}
    }

    
    
    
    private Object getSubObject(Object obj, Class subClass) throws Exception
    {
    	String type = subClass.getCanonicalName();
    	if (type.equals("org.bn.coders.IASN1PreparedElementData") )
    	{
    		return null;
    		// nothing to do
    	}
    	else if (type.equals("byte[]"))
    	{
    		byte[] bytes = new byte[]{4,8,0,1,2,3,4,5,6,7};
    		//byte[] bytes = new byte[]{1};
    		return bytes;
    	}
    	else if (type.equals("java.lang.Boolean") || type.equals("boolean"))
    	{
			return Boolean.valueOf("true").booleanValue();
		} 
		else if (type.equals("java.lang.Long") || type.equals("long"))
		{
			return Long.parseLong("11111111111111");
		}
		else if (type.equals("java.lang.Integer") || type.equals("int"))
		{
			return Integer.parseInt("11");
		} 
		else if (type.equals("java.lang.String"))
		{
			return "0.1.2.3.4.5.6.7.8.9";
		}
		else if (type.equals("org.bn.types.ObjectIdentifier"))
		{
			ObjectIdentifier objId = new ObjectIdentifier();
			objId.setValue("0.1.2.3.4.5.6.7.8.9");
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
					return objects[0];
				}
				else
				{
					Class[] subClasses = classes[0].getClasses();
					objects = subClasses[0].getEnumConstants();
					if (objects !=null && objects.length > 0)
					{
						return objects[0];
					}
				}
			}
			return null;
		}
    	/*
		else if (type.equals("UnknownSubscriberParam"))
		{
			Class[] classes = obj.getClass().getClasses();
			Object[] objects = null;
			if (classes.length >= 1)
			{
				objects = classes[0].getEnumConstants();
			}
			if (objects !=null && objects.length > 0)
			{
				return objects[0];
			}
			else
			{
				return null;
			}
		}*/
		else
		{
			Constructor constr = subClass.getConstructor();
			constr.setAccessible(true);
			Object subObj = constr.newInstance();
			setValue(subObj);
			return subObj;
		}
    }
    
}
