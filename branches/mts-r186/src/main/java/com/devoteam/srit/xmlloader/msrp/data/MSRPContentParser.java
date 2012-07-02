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

package com.devoteam.srit.xmlloader.msrp.data;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromSDP;
import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MSRPContentParser{	
	
	private String[] multipartArray = null;
			
	private String content = null;
	
	// --- construct --- //
	public MSRPContentParser(String content, String contentBoundary) throws Exception 
	{
		this.content = content; 
		multipartArray = contentSDPPart(content, contentBoundary);
	}
			
	public void addContentParameter(Parameter var, String[] params, String path) throws Exception 
	{
    if (params.length == 1 && params[0].toLowerCase().startsWith("content"))
    {		
	    try
	    {
	        // case no content
	        if (content == null || content.length() == 0)
	        {
	            return;
	        }
	        
	        // case no index in the path => the entire content
	        int posBegin = params[0].indexOf("(");
	        int posEnd = params[0].indexOf(")");
	        if ((posBegin < 0) || (posEnd < 0))
	        {
	            var.add(content.substring(0, content.indexOf("-------")));
	            return;
	        }
	        int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
	        var.add(multipartArray[part]);
	    }
	    catch (Exception e)
	    {
	        throw new ExecutionException("Error in content content: " + e.getMessage());
	    }
	}
	else if (params.length == 2 && params[0].toLowerCase().startsWith("content"))
	{
	    if (multipartArray != null)
	    {
	        // case an index in the path => the specific content
	        int posBegin = params[0].indexOf("(");
	        int posEnd = params[0].indexOf(")");
	        if ((posBegin >= 0) && (posEnd >= 0))
	        {
		        int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
		        content = multipartArray[part].trim();
	        }
	    }
	    //---------------------------------------------------------------------- content(X):Type -
	    if (params[1].equalsIgnoreCase("Type"))
	    {
	        try
	        {
	            if ((content != null) && multipartArray != null && multipartArray.length > 1) 
	            {
	                var.add(content.substring(0, content.indexOf("\r\n")));
	            }
	        }
	        catch (Exception e)
	        {
	            throw new ExecutionException("Error in content content:Type : " + e.getMessage());
	        }
	    }
	    //---------------------------------------------------------------------- content(X):Sdp -
	    else if (params[1].equalsIgnoreCase("Sdp"))
	    {
	        try
	        {
	            if (content != null)
	            {
	                var.add(content.substring(content.indexOf("\r\n\r\n") + 1, content.length()));
	            }
	            else
	            {
	                String sdpContent = content;
	                sdpContent = content.substring(content.indexOf("\r\n\r\n") + 1, content.length());
	                var.add(sdpContent);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
	        }
	    }
	    else if (params[1].equalsIgnoreCase("End-line"))
	    {
	        try
	        {
	            if (content != null)
	            {
	                var.add(content.substring(content.indexOf("-------") + 1, content.length()));
	            }
	            else
	            {
	                String endContent = content;
	                endContent = content.substring(content.indexOf("-------") + 1, content.length());
	                var.add(endContent);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
	        }
	    }
	}
	else if (params.length > 2 && params[1].equalsIgnoreCase("Sdp"))
	{
	    String sdpContent = content;
        // case an index in the path => the specific content
        int posBegin = params[0].indexOf("(");
        int posEnd = params[0].indexOf(")");
        if ((posBegin >= 0) && (posEnd >= 0))
        {
	        int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
	        if (multipartArray != null)
	        {
	    	    try
	    	    {
	    	    	sdpContent = multipartArray[part].trim();
	    	    }
	    		catch (Exception e)
	    		{
	    			e.printStackTrace();
	    		    throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
	    		}	    	    
	        }
	        else
	        {
	            sdpContent = content;
	        }	        
        }
	    sdpContent = sdpContent.substring(sdpContent.indexOf("\r\n\r\n") + 1, sdpContent.length()).trim();
	    if (sdpContent == null || sdpContent.length() <= 0)
	    {
	    	return;
	    }
	    PluggableParameterOperatorSetFromSDP.addSDPParameter(var, params, sdpContent, 2, path);
	    
    }
	}
    
    /** Get the parts content of this message */
    private static String[] contentSDPPart(String content, String contentBoundary) throws Exception
    {
        String[] multipartArray = null;
        String[] multipartFinalArray;
        if (contentBoundary != null)
        {
            multipartArray = Utils.splitNoRegex(content, contentBoundary);
            multipartFinalArray = new String[multipartArray.length];
            int j = 0;
            for (int i = 0; i < multipartArray.length; i++)
            {
            	if (multipartArray[i].length() > 0)
            	{
            		multipartFinalArray[j] = multipartArray[i];
            		j++;
            	}
            }
            return multipartFinalArray;
        }
        multipartFinalArray = new String[1];
        multipartFinalArray[0] = content;
        return multipartFinalArray;
    }
	    
}
