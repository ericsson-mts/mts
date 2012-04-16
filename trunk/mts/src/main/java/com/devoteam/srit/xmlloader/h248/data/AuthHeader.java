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

public class AuthHeader 
{
	
	//--- attribut --- //
	private String line = null;
	
	private String securityParamIndex = null;
	private String sequenceNum = null;
	private String authData = null;

	// --- construct --- //
	public AuthHeader()
	{
	}
	
	public String getLine() {
		return line;
	}

	/** 
	 * Parse the authentication header of the H248 message and build the object
	 * 
	 * The generic format for a header is (see spec at #7.1.1 chapter) : 
	 * <AUTH_TOKEN> '=' <securityParamIndex> ':' <sequenceNum> ':' <authData>
	 * where 
	 * 		<securityParamIndex> is the security param index
	 * 		<sequenceNum> is the sequence number
	 * 		<authData> is the authentication data
	 * 
	 * @param String line : the msg header line
	 * @param int from : the index of the character to start the parsing from
	 * @return int : the next index to parse 
	 * @throws Exception
	 */
	public int parseHeader(String line, int from) throws Exception
	{	
		this.line = line;
		
		int index = ABNFParser.indexOfKWDictionary(this.line, ABNFParser.AUTH_TOKEN, from);
		if (index < 0)
		{
			// Warning : no AuthToken character in the header part
			return index; 
		}
		from = index + 1;
		
		index = ABNFParser.indexOfKeyword(this.line, ABNFParser.EQUAL, from);
		if (index < 0)
		{
			// Warning : no EQUAL character in the header part
			return index; 
		}
		from = index + 1;

		// get the securityParamIndex
		index = ABNFParser.indexOfKeyword(this.line, ABNFParser.COLON, from);
		if (index < 0)
		{
			// Warning : no COLON character in the header part
			return index; 
		}
		this.securityParamIndex = this.line.substring(from, index).trim();
		from = index + 1;
		
		// get the sequenceNum
		index = ABNFParser.indexOfKeyword(this.line, ABNFParser.COLON, from);
		if (index < 0)
		{
			// Warning : no COLON character in the header part
			return index; 
		}
		this.sequenceNum = this.line.substring(from, index).trim();
		from = index + 1;

		this.authData = this.line.substring(from, line.length() - 1).trim();
		from = index + 1;

		return index;
	}

    /** Add to a parameter from the message */
    public void addParameter(Parameter var, String path) throws Exception
    {
	    if (path.equalsIgnoreCase("securityParamIndex"))
	    {
	    	var.add(this.securityParamIndex);
	    }
	    else if (path.equalsIgnoreCase("sequenceNum"))
	    {
	    	var.add(this.sequenceNum);
	    }
	    else if (path.equalsIgnoreCase("authData"))
	    {
	    	var.add(this.authData);
	    }
	    else 
	    {
	    	Parameter.throwBadPathKeywordException(path);
	    }
    }

	/** 
	 * toString() method    
	 * */
	public String toString()
	{		
		String res = "";
		String resolvedName = null;
		try
		{
			resolvedName = Dictionary.getInstance().getLongToken(ABNFParser.AUTH_TOKEN);
		}
		catch (Exception e)
		{
			// nothing to do
		}
		
		res += resolvedName;
		res += "(" + ABNFParser.AUTH_TOKEN + ")=";
		if (this.securityParamIndex != null)
		{
			res += this.securityParamIndex;
		}
		res += ":";
		if (this.sequenceNum != null)
		{
			res += this.sequenceNum;
		}
		res += ":";
		if (this.authData != null)
		{
			res += this.authData;
		}
		res += " ";
		return res;
	}

}
