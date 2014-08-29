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
public class Asn1ToXml 
{

    public Asn1ToXml() 
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
	 
			// parsing object object fields  
	    	Field[] fields = objClass.getClass().getDeclaredFields();
	    	
			if (name != null)
			{
				ret += "\n" + indent(indent);
		    	ret += "<" + name + ">";
			}
			
	    	int countFields = 0;
	    	boolean complex = false; 
	    	//for (int i= fields.length - 1; i >=0; i--)
	    	for (int i= 0; i < fields.length; i++)
	    	{
    			Field f = fields[i];
    			f.setAccessible(true);
    			//System.out.println(f);
    			
				Object subObject = f.get(objClass);
				if (subObject == null) 
				{
					// nothing to do
					continue;
				}

				Class subClass = subObject.getClass();
				if (subClass.getCanonicalName().equals("org.bn.coders.ASN1PreparedElementData") )
				{
					// nothing to do
					continue;
				}
				else if (subClass.getCanonicalName().equals("java.lang.Boolean"))
				{
					if (countFields >= 1)
					{
						ret += "\n" + indent(indent);
					}
					ret += "<boolean>"; 
					ret +=subObject.toString();
					ret += "</boolean>";
				} 
				else if (subClass.getCanonicalName().equals("java.lang.Long"))
				{
					if (countFields >= 1)
					{
						ret += "\n" + indent(indent);
					}
					ret += "<long>";
					ret +=subObject.toString();
					ret += "</long>";
				}
				else if (subClass.getCanonicalName().equals("java.lang.Integer"))
				{
					if (countFields >= 1)
					{
						ret += "\n" + indent(indent);
					}
					ret += "<integer>";
					ret +=subObject.toString();
					ret += "</integer>";
				} 
				else if (subClass.getCanonicalName().equals("java.lang.String"))
				{
					if (countFields >= 1)
					{
						ret += "\n" + indent(indent);
					}
					ret += "<string>";
					ret +=subObject.toString();
					ret += "</string>";
				}
				else if (subClass.getCanonicalName().equals("byte[]"))
				{
					if (countFields >= 1)
					{
						ret += "\n" + indent(indent);
					}
					byte[] bytes = (byte[]) subObject;
					ret += "<bytes>";
					ret += Utils.toHexaString(bytes, "");
					ret += "</bytes>";
				}
				else if (subClass.getCanonicalName().equals("java.util.LinkedList"))
				{
					Collection coll = (Collection) subObject;
					Iterator iter = coll.iterator();
					indent = indent + 2;
					ret += "\n" + indent(indent);
					ret += "<collection>";
					while (iter.hasNext())
					{
						Object subObj = iter.next();
						ret += toXML(f.getName(), subObj, indent + 2);
					}
					ret += "\n" + indent(indent);
					ret += "</collection>";
					complex = true;
				}
				else if (subClass.getCanonicalName().equals("org.bn.types.NullObject"))
				{
					continue;
				}
				/*
				else if (subClass.getCanonicalName().equals("org.bn.types.ObjectIdentifier"))
				{
					ObjectIdentifier objId = (ObjectIdentifier) subObject;
					ret += "\n" + indent(indent + 2);
					ret += "<ObjectIdentifier>";
					ret += objId.getValue();
					ret += "</ObjectIdentifier>";
				}
				*/
				else
				{
					ret += toXML(f.getName(), subObject, indent + 2);
					complex = true;
   				}
				countFields++;
	    	}
	    	
			if (name != null)
			{
				if (countFields > 1 || complex)
				{
					ret += "\n" + indent(indent);
				}
				ret += "</" + name + ">";
			}

		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

    	return ret;
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
