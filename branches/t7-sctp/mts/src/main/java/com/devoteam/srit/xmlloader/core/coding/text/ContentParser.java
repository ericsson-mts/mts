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

package com.devoteam.srit.xmlloader.core.coding.text;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.PluggableParameterOperatorSetFromSDP;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.HashMap;

public class ContentParser {

    private String[] multipartArray = null;
    private String content2 = null;

    // --- construct --- //
    public ContentParser(String protocol, String content, String contentBoundary) throws Exception {
        this.content2 = content;
        multipartArray = contentSDPPart(protocol, content, contentBoundary);
    }

    public void addContentParameter(Parameter var, String[] params, String path) throws Exception {
    	String content1 = content2;
        if (params.length == 1 && params[0].toLowerCase().startsWith("content")) 
        {
            try 
            {
                // case no content
                if (content2 == null || content2.length() == 0) 
                {
                    return;
                }

                // case no index in the path => the entire content
                int posBegin = params[0].indexOf("(");
                int posEnd = params[0].indexOf(")");
                if ((posBegin < 0) || (posEnd < 0)) 
                {
                    var.add(content2);
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
                int part = 0;
                if ((posBegin >= 0) && (posEnd >= 0)) 
                {
                    part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                }
                content1 = multipartArray[part].trim();
            }
            //---------------------------------------------------------------------- content(X):Type -
            if (params[1].equalsIgnoreCase("Type")) 
            {
                try 
                {
                    if ((content1 != null) && multipartArray != null && multipartArray.length > 1) 
                    {
                        var.add(content1.substring(0, content1.indexOf("\r\n")));
                    }
                } 
                catch (Exception e) 
                {
                    throw new ExecutionException("Error in content content:Type : " + e.getMessage());
                }
            } //---------------------------------------------------------------------- content(X):Sdp -
            else if (params[1].equalsIgnoreCase("Sdp")) 
            {
                try 
                {
                    if (content1 != null) 
                    {
                        var.add(content1.substring(content1.indexOf("\r\n\r\n") + 1, content1.length()));
                    } 
                    else 
                    {
                        String sdpContent = content1;
                        sdpContent = content1.substring(content1.indexOf("\r\n\r\n") + 1, content1.length());
                        var.add(sdpContent);
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
            String sdpContent = content1;
            // case an index in the path => the specific content
            int posBegin = params[0].indexOf("(");
            int posEnd = params[0].indexOf(")");
            if ((posBegin >= 0) && (posEnd >= 0)) 
            {
                int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                if (multipartArray != null) {
                    try 
                    {
                        sdpContent = multipartArray[part].trim();
                    } catch (Exception e) 
                    {
                        e.printStackTrace();
                        throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
                    }
                } 
                else 
                {
                    sdpContent = content1;
                }
            }
            sdpContent = sdpContent.substring(sdpContent.indexOf("\r\n\r\n") + 1, sdpContent.length()).trim();
            if (sdpContent == null || sdpContent.length() <= 0) 
            {
                return;
            }
            PluggableParameterOperatorSetFromSDP.addSDPParameter(var, params, sdpContent, 2, path);
        }
    	else if ((params.length > 1) && (params[1].equalsIgnoreCase("xml")))
      	{
            String xmlContent = content1;
            // case an index in the path => the specific content
            int posBegin = params[0].indexOf("(");
            int posEnd = params[0].indexOf(")");
            if ((posBegin >= 0) && (posEnd >= 0)) 
            {
                int part = Integer.valueOf(params[0].substring(posBegin + 1, posEnd));
                if (multipartArray != null) {
                    try 
                    {
                        xmlContent = multipartArray[part].trim();
                    } catch (Exception e) 
                    {
                        e.printStackTrace();
                        throw new ExecutionException("Error in content:Sdp : " + e.getMessage());
                    }
                } 
                else 
                {
                    xmlContent = content1;
                }
            }
            xmlContent = xmlContent.substring(xmlContent.indexOf("\r\n\r\n") + 1, xmlContent.length()).trim();
            if (xmlContent == null || xmlContent.length() <= 0) 
            {
                return;
            }

    		if ((params.length > 3) && (params[2].equalsIgnoreCase("xpath")))
    		{
	            String strXpath = params[3];
	            for (int i = 4; i < params.length; i++)
	            {
	            	strXpath += "." + params[i];
	            }
	            var.applyXPath(xmlContent, strXpath, false);
    		}
            else
            {
            	Parameter.throwBadPathKeywordException(path);
            }
      	}
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        } 

    }

    public String[] contentSDPPart(String protocol, String content, String contentBoundary) throws Exception {
        // extract the Content-Type header and needed informations (firstId / mimeSeparator)
        String localMimeSeparator = null;

        if (null != contentBoundary) {
            localMimeSeparator = "--" + contentBoundary;
        } else if (null == contentBoundary && protocol.equals("MGCP")) {
            localMimeSeparator = "\r\n\r\n";
        } else {

            return new String[]{content};
        }

        // if the content starts with a separator, we remove it to avoid having
        // an empty cell in the output array of split method
        if (!protocol.equals("MGCP")) {
            if (content.startsWith(localMimeSeparator)) {
                content = content.substring(localMimeSeparator.length());
            }
            if (content.endsWith("\r\n" + localMimeSeparator + "--")) {
                content = content.substring(0, content.length() - localMimeSeparator.length() - 4);
            }
            if (content.endsWith("\n" + localMimeSeparator + "--")) {
                content = content.substring(0, content.length() - localMimeSeparator.length() - 3);
            }
        }

        // populate the different hashmaps
        if (protocol.equals("MGCP")){
            String[] parts = Utils.splitNoRegex(content, localMimeSeparator);
            return parts;
        }

        String[] parts = Utils.splitNoRegex(content, "\r\n" + localMimeSeparator + "\r\n");
//        HashMap<String, String> contents = new HashMap();
//        HashMap<String, String> types = new HashMap();
//        HashMap<String, String> encodings = new HashMap();
//
//        for(String string:parts)
//        {
//            // extract id
//            String idHeader = string.substring(string.toLowerCase().indexOf("content-id"));
//            idHeader = idHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            idHeader = idHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//
//            String id = idHeader.split(":", 2)[1].replace("<", "").replace(">", "").trim();
//            contents.put(id, string.split("[\\r]?\\n[\\r]?\\n", 3)[1]);
//
//            // extract types
//            String typeHeader = string.substring(string.toLowerCase().indexOf("content-type"));
//            typeHeader = typeHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            typeHeader = typeHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//            String type = typeHeader.split(":", 2)[1].trim();
//            types.put(id, type);
//
//            // extract encoding
//            String encodingHeader = string.substring(string.toLowerCase().indexOf("content-transfer-encoding"));
//            encodingHeader = encodingHeader.split("[\\r]?\\n[\\r]?\\n", 2)[0];
//            encodingHeader = encodingHeader.split("[\\r]?\\n\\p{Alnum}", 2)[0];
//            String encoding = encodingHeader.split(":", 2)[1].trim();
//            encodings.put(id, encoding);
//        }

        // now, get the XML and compute it
        return parts;
    }
}
