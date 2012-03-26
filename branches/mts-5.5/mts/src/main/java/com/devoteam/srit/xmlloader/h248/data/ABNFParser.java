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

import com.devoteam.srit.xmlloader.h248.data.Dictionary;

public class ABNFParser 
{
	// separator for general
	public static String MEGACOP_TOKEN = "!";
	public static String SLASH = "/";	
	public static String MINUS = "-";
	public static String COMMA = ",";
	
	// separator for AuthHeader
	static String AUTH_TOKEN = "AU";
	static String COLON = ":";
	
	// separator for AuthHeader
	static String SEP = " \t\r\n";

	// separator for mId
	static String LSBRKT = "[";
	static String RSBRKT = "]";
	static String LT = "<";
	static String GT = ">";

	// separator for Descriptor
	static String EQUAL = "=";
	static String INEQUAL = "<>#";
	static String CRLF = "\r\n";
	static String LBRKT = "{";
	static String RBRKT = "}";

	/**
	 * search the next occurrence of a list of characters (provided as a string) 
	 * from a given index into a string
	 * 
	 * @param str : the string to search in
	 * @param charlist : the characters list to search as a string
	 * @param fromIndex : the index to search from
	 * @return the index of the next occurrence; negative means no occurrence
	 * @throws Exception
	 */
	public static int indexOfKeyword(String str, String charlist, int fromIndex) throws Exception
	{
		for (int i = fromIndex; i < str.length(); i++)
		{
			for (int j = 0; j < charlist.length(); j++)
			{
				if (str.charAt(i) == charlist.charAt(j))
				{
					return i;
				}
			}
		}
				
		return -999999;	
	}

	/**
	 * search the next occurrence of a keyword string (called keyword) 
	 * from a given index into a string. The keyword could be the short form 
	 * of a descriptor name; so we perform a dictionary resolvation to get 
	 * the long form of the keyword. 
	 * 
	 * @param String str : the string to search in
	 * @param String keyword : the keyword to search
	 * @param int fromIndex : the index to search from
	 * @return String : the index of the next occurrence; negative means no occurrence
	 * @throws Exception
	 */
	public static int indexOfKWDictionary(String str, String keyword, int fromIndex) throws Exception
	{
		int iPosShort = str.indexOf(keyword, fromIndex);
		if (iPosShort >= 0)
		{
			return iPosShort; 
		}
		String resolvedName = Dictionary.getInstance().getLongToken(keyword);
		int iPosLong = str.indexOf(resolvedName, fromIndex);
		if (iPosLong >=0)
		{
			return iPosLong;
			
		}
		return -999999;	
	}

	/**
	 * Remove the " (Double Quote) character at the beginning and at the end of a given string 
	 * 
	 * @param String str : the string to remove 
	 * @return String : the string after removing
	 * @throws Exception
	 */

	public static String removeDoubleQuote(String str) throws Exception
	{
		str = str.trim();
		if (str == null) 
		{
			return str;
		}		
		if (str.length() <= 0) 
		{
			return str;
		}
		if (str.charAt(0) == '"') 
		{
			str = str.substring(1);
		}
		if (str.charAt(str.length() - 1) == '"') 
		{
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

}
