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

import java.util.HashMap;
import java.util.HashSet;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;


public class TextMessage {

    //--- attribut --- //
    private String msg = null;
    private GenericFirstLine genericfirstline = null;


    private String headers = null;
    private ContentParser contentParser = null;
    /*
     * Protocol string to parse
     *
     */
    private String protocol;
    /*
     * Flag to set or not the Content-Length header of the message
     * Normally, set is done only on message to send
     *
     */
    private boolean completeContentLength;
    /*
     * Flag to add automatically a CRLF charaters at the end of the content
     * Normally, set is done only on SIP message with a config parameter
     *
     */
    private int addCRLFContent;
    /**
     * List of the multi-value headers : the different values are on the same line
     * separated with the ',' character (SIP only)
     */
    private HashSet<String> multiHeader = new HashSet<String>();
    /**
     * Table of the compressed headers : some headers are compressed with just a leter
     * This table has the corresponding long headers values (SIP only)
     */
    private HashMap<String, String> compressedHeader = new HashMap<String, String>();
    private MsgParser parser;

    // --- construct --- //
    public TextMessage(String protocol, boolean completeContentLength, int addCRLFContent) throws Exception {
        this.protocol = protocol;
        this.addCRLFContent = addCRLFContent;
        this.completeContentLength = completeContentLength;
    }

    public void parse(String msg) throws Exception {
        // case a header is contining at the next line
        msg = Utils.trimLeft(msg);
        msg = Utils.replaceNoRegex(msg, "\r\n", "\n");
        msg = Utils.replaceNoRegex(msg, "\n", "\r\n");

        msg = msg.replace('\t', ' ');
        msg = msg.replaceAll(",[ ]*\r\n ", ", ");
        msg = msg.replaceAll(":[ ]*\r\n ", ": ");
        msg = msg.replaceAll("\\n[ ]+", "\n");

        // get of the content of the message
        int iPosContent = msg.indexOf("\r\n\r\n");
        String content;
        if (iPosContent > 0) {
        	content = msg.substring(iPosContent + 4);
        }
        else
        {
        	content = "";
        	iPosContent = msg.length();
        }
        if (addCRLFContent > 0 && content.length() > 0) {
            for (int i = 0; i < addCRLFContent; i++) {
                content += "\r\n";
            }
        }

        // get of the headers of the message
        this.headers = msg.substring(0, iPosContent).trim();
        // Calculate the Content-Length header is not present in the message or has an invalid value
        if (completeContentLength) {
            completeContentLengthHeader(content.length());
        }

        // parse the headers
        this.parser = new MsgParser(multiHeader, compressedHeader);
        parser.parse(headers, "\r", ':', "<>", "\"\"");
        parser.processHeaders();

       

        // parsing of the content of the message
        Header contentType = parser.getHeader("Content-Type");
        Header boundary = contentType.parseParameter("boundary", ";", '=', "<>", "\"\"");
        contentParser = new ContentParser(protocol, content.trim(), boundary.getHeader(0));

        // calculate the complete message
        StringBuilder buff = new StringBuilder(this.headers);
        buff.append("\r\n\r\n");
        if (content != null) {
            buff.append(content);
        }
        this.msg = buff.toString();
    }
    public String getFirstLineString(){
      return   parser.getHeader(null).getHeader(0);
    }


    public void addContentParameter(Parameter var, String[] params, String path) throws Exception {
        contentParser.addContentParameter(var, params, path);
    }

    /*
     * 	Calculate the Content-Length header is not present in the message or has an invalid value
     * 
     */
    private void completeContentLengthHeader(int contentLength) throws Exception {
        // get the Content-Length header value
        String strLength = getHeaderValue(headers, "Content-Length");

        // change the Content-Length header
        if (strLength != null) {
            // if the value is not an integer, then set the value to the content length
            if (strLength.length() <= 0 || !Utils.isInteger(strLength)) {
                strLength = new Integer(contentLength).toString();
            }
            // remove the Content-Length header
            headers = removeHeader(headers, "Content-Length");
            this.headers = this.headers + "\r\nContent-Length: " + strLength;
        }
    }

    /*
     * 	Calculate the Content-Length header is not present in the message or has an invalid value
     * 
     */
    public static String getHeaderValue(String headers, String hName) throws Exception {
        // get the Content-Length header value
        int iPosLength = headers.indexOf("\r\n" + hName);
        if (iPosLength >= 0) {
            int iPosLengthValue = headers.indexOf(":", iPosLength + 2);
            int iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
            if (iPosLengthEnd < 0) {
                iPosLengthEnd = headers.length() - 1;
            }
            return headers.substring(iPosLengthValue + 1, iPosLengthEnd + 1).trim();
        }
        return null;
    }

    /*
     * 	Remove the first occurence of a header from an index
     * 
     */
    private static String removeHeader(String headers, String name) throws Exception {
        // get the Content-Length header value
        int iPosLength = headers.indexOf("\r\n" + name);
        if (iPosLength >= 0) {
            int iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
            if (iPosLengthEnd < 0) {
                iPosLengthEnd = headers.length();
            }
            // remove the Content-Length header
            return headers.substring(0, iPosLength) + headers.substring(iPosLengthEnd, headers.length());
        } else {
            return headers;
        }
    }

    /*
     * 	Remove the all occurences of a header from an index
     * 
     */
    public static String removeAllHeader(String headers, String name) throws Exception {
        int iPosLength = 0;
        int iPosLengthEnd = 0;
        while (iPosLength >= 0) {
            // get the Content-Length header value
            iPosLength = headers.indexOf("\r\n" + name, iPosLength);
            if (iPosLength >= 0) {
                iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
                if (iPosLengthEnd < 0) {
                    iPosLengthEnd = headers.length() - 1;
                }
                // remove the Content-Length header
                headers = headers.substring(0, iPosLength) + headers.substring(iPosLengthEnd, headers.length());
            }
        }
        return headers;
    }

    /*
     * 	Add all headers into Parameter
     * 
     */
    public void addHeaderIntoParameter(Parameter var) throws Exception {
        parser.addHeaderIntoParameter(var);
    }

    /*
     * 	Add other headers into Parameter
     * 
     */
    public void addOtherIntoParameter(Parameter var) throws Exception {
        parser.addOtherIntoParameter(var);
    }

    public String getMessage() {
        return this.msg;
    }

  

    public Header getHeader(String name) {
        Header header = parser.getHeader(name);
        if (header != null) {
            return header;
        } else {
            return new Header(name);
        }
    }

    public String getHeaders() throws Exception {
        return this.headers;
    }

    public void setCompressedHeader(HashMap<String, String> compressedHeader) {
        this.compressedHeader = compressedHeader;
    }

    public void setMultiHeader(HashSet<String> multiHeader) {
        this.multiHeader = multiHeader;
    }
     public GenericFirstLine getGenericfirstline() {
        return genericfirstline;
    }

    public void setGenericfirstline(GenericFirstLine genericfirstline) {
        this.genericfirstline = genericfirstline;
    }
}
