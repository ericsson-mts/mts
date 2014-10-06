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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bn.types.ObjectIdentifier;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author fhenry
 */
public class ASNReferenceFinder 
{

	private static ASNReferenceFinder _instance;
    
    public static ASNReferenceFinder getInstance()
    {
    	if (_instance == null)
    	{
    		_instance = new ASNReferenceFinder();
    	}
    	return _instance;
    }

    public ASNReferenceFinder() 
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

    public void findAndRemoveReferences(Map<String, Class> classes, Object obj)  
    {
		try 
		{
	        // parsing object object fields 
	    	Field[] fields = obj.getClass().getDeclaredFields();
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
					continue;
				} 
				else if (typeField != null && typeField.equals("java.lang.Long"))
				{
					continue;
				}
				else if (typeField != null && typeField.equals("java.lang.Integer"))
				{
					continue;
				} 
				else if (typeField != null && typeField.equals("java.lang.String"))
				{
					continue;
				}
				else if (typeField != null && typeField.equals("byte[]"))
				{
					continue;
				}
				else if (typeField != null && typeField.equals("java.util.Collection"))
				{
					continue;
					/*
					ParameterizedType genType = (ParameterizedType) f.getGenericType();
					Type[] typeActualTypeArg = genType.getActualTypeArguments();
					LinkedList list = new LinkedList();
					if (typeActualTypeArg.length > 0)
					{
						String tabClassName = ((Class) typeActualTypeArg[0]).getCanonicalName();
						for (int j = 0; j <= 1; j++)
						{	
							Class tabClass = Class.forName(typeField);
							// get an instance
	        		        Object objClass = tabClass.newInstance();
	    					f.set(objClass, objClass);
            		        findAndRemoveReferences(classes, tabClass);
						}
					}
					*/
				}
				else if (typeField != null && typeField.equals("org.bn.types.ObjectIdentifier"))
				{
					continue;
				}
				else if (typeField != null && typeField.endsWith("EnumType"))
				{
					continue;
				} 
				else if (classes.containsKey(typeField))
				{
					classes.remove(typeField);
				}
				/*
				else
				{
					Class subClass = Class.forName(typeField);
					// get an instance
    		        Object subObj =subClass.newInstance();
					f.set(obj, subObj);
					findAndRemoveReferences(classes, subClass);
   				}
   				*/
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

}
