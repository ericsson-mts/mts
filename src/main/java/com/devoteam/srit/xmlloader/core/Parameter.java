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

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GenericLogger;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;
import com.devoteam.srit.xmlloader.core.coding.text.Header;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultText;

/**
 *
 * @author gpasquiers
 */
public class Parameter {
    
    public static String EXPRESSION = "\\[[^\\]\\[]+\\]";
    public static Pattern pattern = Pattern.compile(EXPRESSION);
    private static Matcher matcher = pattern.matcher("");

    private Vector<Object> array;
    private long version;

    public synchronized static boolean containsParameter(String value) {
        matcher.reset(value);

        return matcher.find();
    }

    public synchronized static boolean matchesParameter(String value) {
        if(value.isEmpty()){
            return false;
        }
        int len = value.length();
        int depth = 0;
        if(value.charAt(0) == '[' && value.charAt(len - 1) == ']'){
            for(int i=0; i<len; i++){
                if(value.charAt(i) == '['){
                    depth++;
                }

                if(depth > 0 && value.charAt(i) == ']'){
                    depth--;

                    if(depth == 0 && i<len-1){
                        return false;
                    }
                }
            }
            if(depth == 0){
                return true;
            }
        }
        return false;
    }

    /** Creates a new instance of Parameter */
    public Parameter() {
        this.array = new Vector<Object>();
        version = 0;
    }

    public Parameter(Vector<Object> values) {
        this.array = values;
    }

    /** Creates a new instance of Parameter */
    public Parameter(String name) {
        this();
    }

    public void add(Object value) {
        modified();
        if (null != value) {
            array.add(value);
        }
    }

    public void add(Object value, int index) {
        modified();
        if (null != value) {
            array.add(index, value);
        }
    }

    public void addHeader(Header value) {
    	addHeader(value, false);
    }
	
	public void addHeaderNamevalue(Header value) {
        modified();
        for (int i = 0; i < value.getSize(); i++) {
            if (value.getHeader(i) != null) {
           		add(value.getName() + ": " + value.getHeader(i).toString());
            }
        }
    }

    public void addHeader(Header value, boolean noMagic) {
        modified();
        for (int i = 0; i < value.getSize(); i++) {
            if (value.getHeader(i) != null) {
            	if (!noMagic)
            	{
            		add(value.getHeader(i).toString());
            	}
            	else
            	{
            		String valHeader = value.getHeader(i).toString();
            		if (valHeader.startsWith("z9hG4bK"))
            		{
            			add(valHeader.substring(7));
            		}   
            		else
            		{
            			add(valHeader);
            		}
            	}
            }
        }
    }

    public void addHeaderInteger(Header value) {
        modified();
        for (int i = 0; i < value.getSize(); i++) {
            String val = value.getHeader(i).toString();
            try {
                int valInt = Integer.parseInt(val);
                val = new Integer(valInt).toString();
            }
            catch (Exception e) {
                // Nothing to do
            }
            add(val);
        }
    }

    public void applyXPath(String xml, String xpath, boolean deleteNS) throws Exception
    {
		// remove beginning to '<' character
		int iPosBegin = xml.indexOf('<');
		if (iPosBegin > 0)
		{
			xml = xml.substring(iPosBegin);
		}
		// remove from '>' character to the end
		int iPosEnd = xml.lastIndexOf('>');
		if ((iPosEnd > 0) && (iPosEnd < xml.length() - 1))
		{
			xml = xml.substring(0, iPosEnd + 1);
		}
		
		int iPosXMLLine = xml.indexOf("<?xml");
		if (iPosXMLLine < 0)
		{
			// when java writes string, it is in ISO-8859-nn format
			xml = "<?xml version='1.0' encoding=\"ISO-8859-15\"?>\n" + xml;
		}
		
		// remove the namespace because the parser does not support them if there are not declare in the root node
		if (deleteNS)
		{
			xml = xml.replaceAll("<[a-zA-Z\\.0-9_]+:", "<");
			xml = xml.replaceAll("</[a-zA-Z\\.0-9_]+:", "</");
		}
		// remove doctype information (dtd files for the XML syntax)
		xml = xml.replaceAll("<!DOCTYPE\\s+\\w+\\s+\\w+\\s+[^>]+>", "");
		
		InputStream input = new ByteArrayInputStream(xml.getBytes());
	    SAXReader reader = new SAXReader(false);
	    reader.setEntityResolver(new XMLLoaderEntityResolver());
	    Document document = reader.read(input);
	    
	    XPath xpathObject = document.createXPath(xpath);
	    Object obj = xpathObject.evaluate(document.getRootElement());
	
	    if (obj instanceof List)
	    {
	        List<Node> list = (List<Node>) obj;
	        for (Node node : list)
	        {
	        	addObject(node);
	        }
	    }
	    else 
	    {
	    	addObject(obj);
	    }
    }
    
    public void addObject(Object obj) 
    {
	    if (obj instanceof DefaultElement)
	    {
	        Node node = (Node) obj;
	        add(node.asXML());
	    }
	    else if (obj instanceof DefaultAttribute)
	    {
	    	Node node = (Node) obj;
	        add(node.getStringValue());
	    }
	    else if (obj instanceof DefaultText || obj instanceof DefaultCDATA)
	    {
	    	Node node = (Node) obj;
	        add(node.getText());
	    }
	    else
	    {
	        add(obj.toString());
	    }  
    }
    
    public void addAll(Collection collection) {
        for(Object object:collection){
            if(null != object){
                add(object);
            }
        }
    }

    public Object remove(int index) {
        modified();
        return array.remove(index);
    }

    public Object get(int index) throws ParameterException {
        if (index < array.size()) {
            return array.get(new Integer(index));
        }

        throw new ParameterException("Can't get element of index " + index + " in array of length " + array.size());
    }

    public void set(int index, Object value) throws Exception {
        modified();
        if (index == array.size()) {
            add(value);
        }
        else {
        	
            if (null != value) {
                array.set(index, value);
            }
        }
    }

    public int length() {
        return array.size();
    }

    @Override
    public String toString() 
    {
    	// number of line at the end of the list to log
    	int NUMBER_LINE_END = 2;
    	
        StringBuffer strBuff = new StringBuffer();        

        int numberLineBefore = length();			// number of line before ...
        int numberLine = length();			// number total ...
        if (length() > GenericLogger.getMaxListSize()) 
        {
        	strBuff.append('[');
        	strBuff.append(GenericLogger.getMaxListSize());
        	strBuff.append('/');
        	strBuff.append(length());
        	strBuff.append(']');
        	numberLineBefore = GenericLogger.getMaxListSize() - NUMBER_LINE_END;
        	numberLine = GenericLogger.getMaxListSize();
        }
        else 
        {
        	strBuff.append('[');
        	strBuff.append(length());
        	strBuff.append(']');
        }
        // length of the header [n] or [p/n]
        int lengthHeader = strBuff.length();
        
        strBuff.append('[');
        if (length() > 1)
        {
        	strBuff.append('\n');
        }
        
        int maxLength = (int) (GenericLogger.getMaxStringLength() - lengthHeader - 12);
	    if (numberLine > 0)
	    {
	    	maxLength= maxLength / numberLine;
	    }

        for (int i = 0; i < numberLineBefore; i++) 
        {
        	toStringForLogging(strBuff, i, maxLength);
            if ((i < numberLineBefore - 1) ) 
            {
            	strBuff.append(",\n");
            }
        }
             
        if (length() > GenericLogger.getMaxListSize()) 
        {
        	strBuff.append(",\n...,\n");	        
            for (int i = length() - NUMBER_LINE_END; i < length(); i++) 
            {
            	toStringForLogging(strBuff, i, maxLength);
                if (i < length() - 1) 
                {
                	strBuff.append(",\n");
                }
            }
        }
        
        if (length() > 1)
        {
        	strBuff.append('\n');
        }
        strBuff.append(']');
        return strBuff.toString();
    }

    public void toStringForLogging(StringBuffer strBuff, int index, int maxLength) 
    {
    	String value = this.array.get(index).toString();
    	
    	// calculate the header length (M) [P/N]
	    int lengthHeader = 0;
    	if (length() > 1)
    	{
    		lengthHeader++;
    		lengthHeader += new Integer(index).toString().length();
    		lengthHeader++;
    	}
	    if (value.length() > maxLength) 
	    {
	    	lengthHeader++;
	    	lengthHeader += new Integer(maxLength).toString().length();
	        lengthHeader++;
	        lengthHeader += new Integer(value.length()).toString().length();
	        lengthHeader++;
	    }
	    // for the ... at the end of the line
	    lengthHeader += 3;
	    // for the ,\n at the end of the line
	    lengthHeader += 2;
	    
	    // substract the length of the header
	    maxLength = maxLength - lengthHeader;
	    if (maxLength < 0)
	    {
	    	maxLength= 0;
	    }
    	if (length() > 1)
    	{
    		strBuff.append('(');
    		strBuff.append(index);
    		strBuff.append(')');
    	}        	      	
	    if (value.length() > maxLength) 
	    {
	        strBuff.append('{');
	        strBuff.append(maxLength);
	        strBuff.append('/');
	        strBuff.append(value.length());
	        strBuff.append('}');
	        strBuff.append(value.substring(0, maxLength));
	        strBuff.append("...");
	    }
	    else
	    {
	    	strBuff.append(value);
	    }	    
    }
    
    public Vector<Object> getArray() {
        return array;
    }

    private void modified(){
        version++;
    }

    public long getVersion(){
        return version;
    }


    public static void throwBadPathKeywordException(ParameterKey key) throws ParameterException {
        throw new ParameterException("Bad path keyword \"" + key + "\"");
    }

    public static void throwBadPathKeywordException(String operation, String path) throws ParameterException {
        throw new ParameterException("Bad path keyword for \"protocol." + operation + "\" operation: \"" + path + "\"");
    }

    public static void throwBadPathKeywordException(String path) throws ParameterException {
        throwBadPathKeywordException("setFromMessage", path);
    }
}
