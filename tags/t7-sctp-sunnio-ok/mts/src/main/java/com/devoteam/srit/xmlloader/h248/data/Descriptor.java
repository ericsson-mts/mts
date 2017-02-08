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

import java.util.HashMap;
import java.util.Vector;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/** 
 * Represents a H248 descriptor which contains : 
 * 		* the name value part
 * 		* the parameters lists as a tree of sub-descriptor.
 * 
 */ 
public class Descriptor
{
	
	/* name value */
	private NameValue nameValue= null;

	/* parameters string */
	private String parameters = null;

	/* sub-descriptors list */
	private Vector<Descriptor> paramList;

	/* sub-descriptors map : for quick search */
	private HashMap<String, Vector<Descriptor>> paramMap;
	
	// --- construct --- //
	public Descriptor()
	{
		this.paramList = new Vector<Descriptor>();
		this.paramMap = new HashMap<String, Vector<Descriptor>>();
	}

	// --- construct --- //
	public Descriptor(NameValue nameValue) throws Exception
	{
		this();
		this.nameValue = nameValue;
	}

	public void setNameValue(NameValue nameValue ) {
		this.nameValue = nameValue ;
	}
	
	public NameValue getNameValue() {
		return nameValue;
	}

	public void setParameters(String parameters) throws Exception {
	    parameters = ABNFParser.removeDoubleQuote(parameters.trim());
		if (parameters.length() > 0)
		{
			this.parameters = parameters;
		}
	}

	public String getParameters() {
		return this.parameters;
	}

	/**
	 * Check if the descriptor is a SDP descriptor (Local or Remote descriptors)
	 * 
	 * @return boolean : true if it is a SDP descriptor, else false
	 */
	public boolean isSDPDescriptor() {
		return nameValue.isSDPDescriptor();
	}

	/**
	 * Add a new sub descriptor to the Object
	 * @param Descriptor newDescr : the new descriptor to add 
	 * 
	 * @return 
	 */
	private void addSubDescriptor(Descriptor newDescr) throws Exception
	{
		this.paramList.add(newDescr);
		Vector<Descriptor> paramVect = this.paramMap.get(newDescr.getNameValue().getN());
		if (paramVect == null)
		{
			paramVect = new Vector<Descriptor>();
			this.paramMap.put(newDescr.getNameValue().getN(), paramVect);
		}
		paramVect.add(newDescr);
	}	

	/** 
	 * Get the sub descriptors list that matches a given descriptor name. "*" means 
	 * all descriptors.  
	 * 
	 * @param String name : the descriptor name 
	 * @return Vector<Descriptor> : the sub descriptors list  
	 * @throws Exception
	 */
	private Vector<Descriptor> getSubDescriptors(String name) throws Exception 
	{
		if (!"*".equals(name))
		{
			String resolvedName = Dictionary.getInstance().getShortToken(name);
			return this.paramMap.get(resolvedName);
		} 
		else
		{		
			return paramList;
		}	
	}
	
	/** 
	 * Parse the descriptors part of the H248 message and build the object recursively
	 * 
	 * The generic format for a descriptor is (see spec at #7.1.1 chapter) : 
	 * <nameValue> '{' <descr1> ',' <descr2> ',' ... ',' <descrN>'}' (H248 descriptor)
	 * <nameValue> '{' <sdp1> '\r\n' <sdp2> '\r\n' ... '\r\n' <sdpN>'}'(SDP descriptor)
	 * where 
	 * 		<nameValue> is the descriptor name and value part
	 * 		<descrN> is the H248 sub-descriptor # N 
	 * 		<sdpN> is the SDP parameter # N (<name> '=' <value>)
	 * 
	 * @param String descriptors : the msg string 
	 * @param int from : the index of the character to start the parsing from
	 * @param boolean isSDP : the boolean to tell is the descriptor is a SDP block
	 * @return int : the next index to parse 
	 * @throws Exception
	 */
	public int parseDescriptors(String descriptors, int current, boolean isSDP) throws Exception
	{		
		NameValue nv = new NameValue(); 
	    int pos = nv.parseNameValue(descriptors, current, isSDP);
		if (nv.getN() == null) 
		{
			return pos;
		}
		setNameValue(nv);
		current = pos;
    	
		// find the begin the descriptor parameter list
		// RBRKT : means no parameter
		// COMMA || CRLF : means no parameter
		String keywords = ABNFParser.LBRKT + ABNFParser.RBRKT;
		if (isSDP)
		{
			keywords += ABNFParser.CRLF;
		}
		else 
		{
			keywords += ABNFParser.COMMA;
		}
		pos = ABNFParser.indexOfKeyword(descriptors, keywords , pos);
		if (pos < 0)
		{
			pos = descriptors.length();
		}
		
    	// LBRKT => some parameters are present
        if ((pos < descriptors.length()) && (descriptors.charAt(pos) == ABNFParser.LBRKT.charAt(0)))
        {
    		while (true)
    		{
    			pos = pos + 1;
    	    	// RBRKT : parameters list is empty : {}    			    
				if (descriptors.charAt(pos) == ABNFParser.RBRKT.charAt(0))
				{
					break;
				}
				// build the included descriptor : {param,param,...} 
		    	// RBRKT means no others parameters    			
				Descriptor newDescr = new Descriptor();
				pos = newDescr.parseDescriptors(descriptors, pos, isSDPDescriptor());
 	 			if (newDescr.getNameValue() != null)
				{
 	 				addSubDescriptor(newDescr);
				}
				
				// find the next descriptor or the end of the parameters list 
		    	// COMMA => there is another parameter
				// RBRKT => end of parameters list
 	 			keywords = ABNFParser.RBRKT;
 	 			if (isSDPDescriptor())
 				{
 					keywords += ABNFParser.CRLF;
 				}
 				else 
 				{
 					keywords += ABNFParser.COMMA;
 				}
    			pos = ABNFParser.indexOfKeyword(descriptors, keywords, pos);
    			// means end of the string
    			if (pos < 0)
    			{
    				pos = descriptors.length() - 1;
    				break;
    			}
				if (descriptors.charAt(pos) == ABNFParser.RBRKT.charAt(0))
				{
					break;
				}
    		}
    		
    		String strParam = descriptors.substring(current + 1, pos);
    	    setParameters(strParam);
        	pos = pos + 1;
        }
        
        return pos;
	}

	/**
	 * Add to a given Parameter object in function of the given path 
	 * keyword which specifies the part of the description the user wants 
	 * to retrieve
	 *  
	 * @param Parameter var : the Parameter object to add into  
	 * @param String path : the path keyword the user wants to retrieve 
	 * @return boolean: true if success, else false
	 */
	private boolean addParameter(Parameter var, String param) throws Exception
	{
	    if (nameValue.addParameter(var, param))
	    {
	    }
	    else if ("parameters".equalsIgnoreCase(param))
	    {
	    	var.add(this.parameters);
	    }
        else
        {
        	return false; 
        }
	    return true;
	}

	/** 
	 * Find recursively a given path keyword into the descriptor sub-tree lists, add the 
	 * information matching into a Parameter object; we process only that at a given depth 
	 * in the sub-tree lists.   
	 *  
	 * @param Parameter var : the Parameter object to add into
	 * @param String path : the path keyword the user wants to retrieve 
	 * @param int depth : the depth to process the matching
	 * @return boolean: true if success, else false
	 * @throws Exception
	 */
	public boolean findAddParameters(Parameter var, String[] paths, int depth) throws Exception
	{
    	String [] p = Utils.splitNoRegex(paths[depth], "=");
    	if (depth == paths.length - 1)
    	{
    		if (!addParameter(var, paths[depth]))
    		{
    			return false;
    		}

			return true;
    	}
    	
    	Vector<Descriptor> paramVect = getSubDescriptors((p[0]));
    	Descriptor des = null;
    	boolean res = true;
    	if (paramVect != null)
    	{
			for (int j = 0; j < paramVect.size(); j++)
			{
				des = paramVect.get(j);			
				if (des.getNameValue().equalsParameter(p))
				{
					if (!des.findAddParameters(var, paths, depth + 1))
					{
						res = false;
					}
				}
			}
    	}
		return res;
	}
	
	/** 
	 * toString() method    
	 * */
	public String toString()
	{
		String res = toString("", 0);
		return res;
	}

	/** 
	 * Build recursively the toString() for logging feature at a given depth with 
	 * a specified indent string for the presentation   
	 *  
	 * @param String indent : the indent string 
	 * @param int depth : the depth to process the matching
	 * @return String : the resulting string
	 * @throws Exception
	 */
	private String toString(String indent, int depth)
	{
		String res = "";
		if (depth > 0)
		{
			if (this.nameValue != null)
			{
				res = indent + this.nameValue.toString();
			}
		}
		if (this.paramList.size() > 0)
		{
			if (depth > 0)
			{
				indent += " ";
				res += "{" + "\n";
			}
			for (int i = 0; i < this.paramList.size(); i++)
			{
				Descriptor descr = paramList.elementAt(i);
				if (descr.paramList.size() > 0)
				{
					if (i > 0)
					{
						res += ",\n";
					}
					res += descr.toString(indent, depth + 1);
				}
				else
				{
					if (i > 0)
					{
						if (isSDPDescriptor())
						{
							res += "\n" + indent;
						}
						else
						{
							res += ", ";
						}
						res += descr.toString("", depth + 1);
					}
					else
					{
						res += descr.toString(indent, depth + 1);
					}
				}
			}
			if (depth > 0)
			{
				res += "}";
			}
		}
		return res;
	}

}
