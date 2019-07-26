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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;

import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;

/* 
 * This classes implements a dictionary to convert long token into short one 
 * or the reverse think. 
 * We use a double hash map object to speed the search  
 * */
public class Dictionary
{	
	
	/* singleton */
	private static Dictionary instance = null; 
	
	private static String LBRKT = "(";
	private static String RBRKT = ")";
	private static String SLASH = "/";
	
	/* path for the dictionary file */
	private static String DICTIONARY_FILE_PATH = "../conf/h248/dictionary.txt";
	
	/* index to search in dictionary */
	private HashMap<String, String> indexByLong;
	private HashMap<String, String> indexByShort;

	// --- construct --- //
	private Dictionary() throws Exception
	{
		this.indexByLong = new HashMap<String, String>();
		this.indexByShort = new HashMap<String, String>();
		
		InputStream stream = SingletonFSInterface.instance().getInputStream(new URI(DICTIONARY_FILE_PATH));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line = bufferedReader.readLine();
        while (line != null)
        {
        	int pos1 = line.indexOf(LBRKT);
        	int pos2 = line.indexOf(SLASH);
        	int pos3 = line.indexOf(RBRKT);
        	if ((pos1 >= 0) && (pos2 >= 0) && (pos3 >= 0))  
        	{
	        	String strLong = line.substring(pos1 + 1, pos2).trim();
	        	strLong = strLong.substring(1, strLong.length() - 1).trim();
	        	String strShort = line.substring(pos2 + 1, pos3).trim();
	        	strShort = strShort.substring(1, strShort.length() - 1).trim();
	        	this.indexByLong.put(strLong, strShort);
	        	this.indexByShort.put(strShort, strLong);
        	}
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
    }

	public static Dictionary getInstance() throws Exception
	{
		if (instance == null)
		{
			instance = new Dictionary();			
		}
		return instance;
	}
	
	/**
	 * Get the short token corresponding to a given token by performing a 
	 * dictionary resolution.
	 * 
	 * @param String token : the token string to resolve
	 * @return String : the short token
	 */
	public String getShortToken(String token)
	{
		String name = token;
    	// delete a prefix
		int pos = token.indexOf("-");
		if (pos >= 0)
		{			
			name = token.substring(pos + 1);
		}
		String resolvedName = this.indexByLong.get(name);		
		if (resolvedName == null)
		{
			resolvedName = name;
		}  	
		if (pos >= 0)
		{
			resolvedName = token.substring(0, pos + 1) + resolvedName;
		}		
		return resolvedName;
	}

	/**
	 * Get the long token corresponding to a given token by performing a 
	 * dictionary resolution.
	 * 
	 * @param String token : the token string to resolve
	 * @return String : the long token 
	 */
	public String getLongToken(String token)
	{
		String name = token;
    	// delete a prefix
		int pos = token.indexOf("-");
		if (pos >= 0)
		{			
			name = token.substring(pos + 1);
		}
		String resolvedName = this.indexByShort.get(name);
		if (resolvedName == null)
		{
			return token;
		}
		if (pos >= 0)
		{
			resolvedName = token.substring(0, pos + 1) + resolvedName;
		}		
		return resolvedName;
	}

}
