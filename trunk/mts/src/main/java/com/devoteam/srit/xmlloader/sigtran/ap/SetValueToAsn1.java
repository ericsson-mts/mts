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

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author fhenry
 */
public class SetValueToAsn1 
{

    public SetValueToAsn1() 
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

    public void setValue(Object objClass)  
    {
		try 
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

				String typeField = f.getType().getCanonicalName();
				if (typeField != null && typeField.equals("org.bn.coders.ASN1PreparedElementData") )
				{
					// nothing to do
				}
				else if (typeField != null && typeField.equals("java.lang.Boolean"))
				{
					f.set(objClass, Boolean.valueOf("true").booleanValue());
				} 
				else if (typeField != null && typeField.equals("java.lang.Long"))
				{
					f.set(objClass, Long.parseLong("11111111111111"));
				}
				else if (typeField != null && typeField.equals("java.lang.Integer"))
				{
					f.set(objClass, Integer.parseInt("77"));
				} 
				else if (typeField != null && typeField.equals("java.lang.String"))
				{
					f.set(objClass, "0.1.2.3.4.5.6.7.8.9");
				}
				else if (typeField != null && typeField.equals("byte[]"))
				{
					byte[] bytes = new byte[]{0,1,2,3,4,5,6,7};
					f.set(objClass, bytes);
				}
				else if (typeField != null && typeField.equals("java.util.Collection"))
				{
					ParameterizedType genType = (ParameterizedType) f.getGenericType();
					Type[] typeActualTypeArg = genType.getActualTypeArguments();
					LinkedList list = new LinkedList();
					if (typeActualTypeArg.length > 0)
					{
						String tabClassName = ((Class) typeActualTypeArg[0]).getCanonicalName();
						for (int j = 0; j <= 1; j++)
						{		
    						Class tabClass = Class.forName(tabClassName);
    						// get an instance
            		        Object tabObject = tabClass.newInstance();
            		        setValue(tabObject);
            		        list.add(tabObject);
						}
						f.set(objClass, list);
					}
				}
				else if (typeField != null && typeField.equals("org.bn.types.ObjectIdentifier"))
				{
					ObjectIdentifier objId = new ObjectIdentifier();
					objId.setValue("0.1.2.3.4.5.6.7.8.9");
					f.set(objClass, objId);
				}
				else
				{
					try
					{
						Class thisClass = Class.forName(typeField);
						// get an instance
        		        Object iClass = thisClass.newInstance();
    					setValue(iClass);
    					f.set(objClass, iClass);
					}
					catch (Exception e)
					{
						// nothing to do
						break;
					}
   				}
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

}
