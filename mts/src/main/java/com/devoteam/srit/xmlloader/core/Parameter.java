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
    /** Maximum number of characters to write into the log */
    private static int MAX_STRING_LENGTH = Config.getConfigByName("tester.properties").getInteger("logs.MAX_STRING_LENGTH", 1000);
    /** Maximum number of records to write into the log */
    private static int MAX_LIST_SIZE = Config.getConfigByName("tester.properties").getInteger("logs.MAX_LIST_SIZE", 100);

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
			xml = "<?xml version='1.0'?>" + xml;
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
	            add(node.asXML());
	        }
	    }
	    else if (obj instanceof DefaultElement)
	    {
	        Node node = (Node) obj;
	        add(node.asXML());
	    }
	    else if (obj instanceof DefaultAttribute)
	    {
	    	Node node = (Node) obj;
	        add(node.getStringValue());
	    }
	    else if (obj instanceof DefaultText)
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
    public String toString() {
        String res = "";
        long length = length();

        if (length > MAX_LIST_SIZE) {
            res += "[" + MAX_LIST_SIZE + " of " + length + "]";
            length = MAX_LIST_SIZE;
        }
        else {
            res += "[" + length + "]";
        }

        res += "(";

        int i = 0;

        while (i < length) {
            if (array.get(i).toString().length() > MAX_STRING_LENGTH) 
            {
                res += "{" + MAX_STRING_LENGTH + " of " + array.get(i).toString().length() + "}" + array.get(i).toString().substring(0, MAX_STRING_LENGTH) + ",";
            }
            else {
                res += array.get(i) + "|";
            }
            i++;
        }

        if (i > 0) {
            res = res.substring(0, res.length() - 1);
        }

        return res + ")";
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

    public static void throwBadPathKeywordException(String operation, String path) throws ParameterException {
        throw new ParameterException("Bad path keyword for \"protocol." + operation + "\" operation: \"" + path + "\"");
    }

    public static void throwBadPathKeywordException(String path) throws ParameterException {
        throwBadPathKeywordException("setFromMessage", path);
    }
}
