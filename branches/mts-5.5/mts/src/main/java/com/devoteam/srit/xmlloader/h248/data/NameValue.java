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

package com.devoteam.srit.xmlloader.h248.data;

import com.devoteam.srit.xmlloader.core.Parameter;

public class NameValue 
{			
	/* name (short form)*/
	private String n = null;

	/* name (long form)*/
	private String name = null;
	
	/* comparator */
	private String comparator = null;

	/* value */
	private String value = null;
	
	public NameValue()
	{
	}

	public String getN() {
		return this.n;
	}

	public void setName(String name) throws Exception {
		name = name.trim();
		if (name.length() > 0)
		{
			this.n = Dictionary.getInstance().getShortToken(name);
			this.name = Dictionary.getInstance().getLongToken(name);
		}
	}

	public String getComparator() {
		return this.comparator;
	}

	public void setComparator(String comparator) throws Exception {
		comparator = comparator.trim();
		if (comparator.length() > 0)
		{
			this.comparator = comparator;
		}
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) throws Exception {
		value = ABNFParser.removeDoubleQuote(value.trim());
		if (value.length() > 0)
		{
			this.value = value;
		}
	}

	/**
	 * Check if the descriptor is a SDP descriptor (Local or Remote descriptors)
	 * 
	 * @return boolean : true if it is a SDP descriptor, else false
	 */
	public boolean isSDPDescriptor() {
		if (n.equals("L"))
		{
			return true;
		}
		if (n.equals("R"))
		{
			return true;
		}		
		return false;
	}

	/** 
	 * Parse nameValue part of the descriptor part string, build the object 
	 * and return the next position to parse
	 * The generic format for a descriptor is (see spec at annex #B chapter and ) :
	 * <name> 
	 * <name> <comparator> <value> 
	 * <name> '=' '{' <value1> ',' <value2> ',' ... ',' <valueN> '}'
	 * <name> '=' '[' <value1> ':' <value2> ']'
	 * <name> '=' '[' <value1> ',' <value2> ',' ... ',' <valueN> ']'
	 * where 
	 * 		<name> is the descriptor name
	 * 		<comparator> is the descriptor comparator : one of '='|>'|'<'|'#' character 
	 * 		<value>, <valueN> is the descriptor value
	 * 
	 * @param String descriptors : the descriptors string
	 * @param int current : the from index in the descr string 
	 * @param boolean : indicate the descriptor is a SDP one 
	 * @return int : the next index to parse
	 * @throws Exception
	 */
	public int parseNameValue(String descriptors, int current, boolean isSDP) throws Exception
	{
		// find the end of the descriptor header : name=value 
		String keywords = ABNFParser.LBRKT + ABNFParser.LSBRKT + ABNFParser.RBRKT;
		String keywordsComparator = ABNFParser.EQUAL + ABNFParser.INEQUAL;
		if (isSDP)
		{
			keywords += ABNFParser.CRLF;
		}
		else 
		{
			keywords += ABNFParser.COMMA;
		}
		int pos = ABNFParser.indexOfKeyword(descriptors, keywords + keywordsComparator, current + 1);
		if (pos < 0)
		{
			pos = descriptors.length();
		}
	    setName(descriptors.substring(current, pos));
	    
    	// find the comparator character of the descriptor header
    	// cut the name part
		if ((descriptors.length() > 0) && (keywordsComparator.indexOf(descriptors.charAt(pos)) >= 0))
		{
		    setComparator(descriptors.substring(pos, pos + 1));
		    current = pos;
			pos = ABNFParser.indexOfKeyword(descriptors, keywords, current + 1);
			if (pos < 0)
			{
				pos = descriptors.length();
			}
			
			String value = descriptors.substring(current + 1, pos).trim();
			if (value.length() > 0)
			{
				setValue(value);
			}		   
			else if (pos == descriptors.length())
			{
				
			}
	    	// find the '{' character of the descriptor header
			else if (descriptors.charAt(pos) == ABNFParser.LBRKT.charAt(0))
			{
		    	// find the '}' character of the descriptor header
				pos = ABNFParser.indexOfKeyword(descriptors, ABNFParser.RBRKT, current + 1);
				if (pos < 0)
				{
					pos = descriptors.length();
				}			
				pos = pos + 1;
				value = descriptors.substring(current + 1, pos);
				setValue(value);
			}
	    	// find the '[' character of the descriptor header
			else if (descriptors.charAt(pos) == ABNFParser.LSBRKT.charAt(0))
			{
		    	// find the ']' character of the descriptor header
				pos = ABNFParser.indexOfKeyword(descriptors, ABNFParser.RSBRKT, current + 1);
				if (pos < 0)
				{
					pos = descriptors.length();
				}			
				pos = pos + 1;
				value = descriptors.substring(current + 1, pos);
				setValue(value);
			}
		}
	    return pos;
	}

	/** 
	 * Extract some information provided by a path keyword, add it into a 
	 * Parameter object and return a status boolean (false = error)
	 * Possible value for path keyword is :
	 *    - n : the name store in the object (the short one)
	 *    - name : the long name (using dictionary)
	 *    - comparator : the comparator
	 *    - value : the value
	 * 
	 * @param Parameter param : the Parameter object to add into
	 * @param String path : the path keyword to extract the information : 
	 * enumeration : "n" | "name" | "comparator" | "value"
	 * @return boolean :the status boolean (false = error)
	 * @throws Exception
	 */
	boolean addParameter(Parameter param, String path) throws Exception
	{
	    if ("name".equalsIgnoreCase(path))
	    {
			param.add(this.name);
	    }
	    else if ("n".equalsIgnoreCase(path))
	    {
	    	param.add(this.n);
	    }
	    else if ("comparator".equalsIgnoreCase(path))
	    {
	    	{
	    		param.add(this.comparator);
	    	}
	    }
	    else if ("value".equalsIgnoreCase(path))
	    {
	    	{
	    		param.add(this.value);
	    	}
	    }
	    else
	    {
	    	return false;
	    }
	    return true;
	}

	/** 
	 * Compare a NameValue object to a given path keyword    
	 * 
	 * @param String[] path : the path keyword to compare 
	 * @return boolean :the boolean result (true = equals)
	 * @throws Exception
	 */
	public boolean equalsParameter(String[] param) throws Exception
	{	
		// case where name is different and param[1] != "*"
		if ((!param[0].equals(this.n) && (!param[0].equals(this.name))) && (!param[0].equals("*")))
	    { 
			return false;
	    }
		// case where value is null and param[1] = ""
		if ((param.length > 1) && (this.value == null) && (param[1].length() == 0))
		{
			return true;
		}
		// case where value is different and param[1] != "*"
		if ((param.length > 1) && (!param[1].equals(this.value)) && (!param[1].equals("*")))
		{
			return false;
		}
		return true;
	}
	
	/** 
	 * toString() method    
	 * */
	public String toString()
	{
		String res = "";
		res += this.name;
		if (!this.name.equals(this.n))
		{
			res += "(" + this.n + ")";
		}
		if ((value != null) && (value.length() > 0))
		{
			res += this.comparator + this.value;
		}
		return res;
	}

}
