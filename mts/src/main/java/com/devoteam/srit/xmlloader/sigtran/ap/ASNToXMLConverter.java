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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
public class ASNToXMLConverter 
{

	static ASNToXMLConverter _instance;
	    
    public static ASNToXMLConverter getInstance()
    {
    	if (_instance != null)
    	{
    		return _instance;
    	}
    	return new ASNToXMLConverter();
    }

    public ASNToXMLConverter() 
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

    public String toXML(String name, Object objClass, int indent)  
    {
    	String ret = "";
		try 
		{
			if (objClass ==  null)
	    	{
	    		return ret;
	    	}

	    	Field[] fields = objClass.getClass().getDeclaredFields();
	    	int countFields = 0;
	    	boolean complex = true;
	    	
			if (name != null)
			{
				if (!objClass.getClass().getCanonicalName().startsWith("java.lang."))
				{
					ret += "\n" + indent(indent);
				}
				if ("value".equals(name))
				{
					ret += "<" + returnClassName(objClass) + ">";
				}
				else
				{
					ret += "<" + name + ">";
				}
			}
				    	
	    	String retObject = returnXMLObject(objClass, indent);
	    	if (retObject != null)
	    	{
	    		ret += retObject;
	    		complex = false;
	    	}
	    	else
	    	{
				// parsing object object fields  
		    	//for (int i= fields.length - 1; i >=0; i--)
		    	for (int i= 0; i < fields.length; i++)
		    	{
	    			Field f = fields[i];
	    			f.setAccessible(true);
	    			//System.out.println(f);
	    			
					Object subObject = f.get(objClass);
					
					String typeField = f.getType().getCanonicalName();
					if (subObject == null)
			    	{
			    		// nothing to do
			    	}
					else if (typeField != null && typeField.equals("org.bn.coders.IASN1PreparedElementData") )
			    	{
			    		// nothing to do
			    	}
					else if (typeField != null && typeField.equals("java.util.Collection"))
					{
						Collection coll = (Collection) subObject;
						Iterator iter = coll.iterator();
						indent = indent + 2;
						ret += "\n" + indent(indent);
						ret += "<Collection>";
						while (iter.hasNext())
						{
							Object subObj = iter.next();
							ret += toXML(f.getName(), subObj, indent + 2);
						}
						ret += "\n" + indent(indent);
						ret += "</Collection>";
					}
					else
					{
						ret += toXML(f.getName(), subObject, indent + 2);
	   				}
					countFields++;
		    	}
	    	}
	    	
			if (name != null)
			{
				if (!objClass.getClass().getCanonicalName().startsWith("java.lang.") && fields.length > 2 && complex)
				{
					ret += "\n" + indent(indent);
				}
				if ("value".equals(name))
				{
					ret += "</" + returnClassName(objClass) + ">";
				}
				else
				{
					ret += "</" + name + ">";
				}
			}

		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	return ret;
    }
    
    private String returnClassName(Object objClass) throws Exception
    {
    	String ret = objClass.getClass().getSimpleName();
    	ret = ret.replace("[]", "s");
    	return ret;
    }
    private String returnXMLObject(Object subObject, int indent) throws Exception
    {
    	String ret = "";
    	Class subClass = subObject.getClass();
    	String type = subClass.getCanonicalName();
    	if (type == null)
    	{
    		return null;
    	}
    	if (type.equals("byte[]"))
    	{
			byte[] bytes = (byte[]) subObject;
			ret += Utils.toHexaString(bytes, "");
			return ret;

    	}
    	else if (type.equals("java.lang.Boolean") || type.equals("boolean"))
    	{
			ret +=subObject.toString();
			return ret;

    	}
		else if (type.equals("java.lang.Long") || type.equals("long"))
		{
			ret +=subObject.toString();
			return ret;
		}
		else if (type.equals("java.lang.Integer") || type.equals("int"))
		{
			ret +=subObject.toString();
			return ret;
		} 
		else if (type.equals("java.lang.String"))
		{
			ret +=subObject.toString();
			return ret;
		}
		else if (type.endsWith("EnumType"))
		{
			ret +=subObject.toString();
			return ret;
		}
		return null;
    }
    
    /**
     * generates a string of nb*"    " (four spaces nb times), used for intentation in printAvp
     */
    private static String indent(int nb)
    {
        String str = "";
        for (int i = 0; i < nb; i++)
        {
            str += " ";
        }
        return str;
    }

}
