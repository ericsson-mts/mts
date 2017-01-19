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

import java.util.HashMap;
import java.util.HashSet;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.Header;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

public class MSRPTextMessage {

    //--- attribut --- //
    private String msg = null;
    private MSRPFirstLine firstline = null;
    private String headers = null;
    private MSRPContentParser contentParser = null;

    /*
     * Protocol string to parse 
     * 
     */
    private String protocol;

    /**
     * List of the multi-value headers : the different values are on the same line separated with the ',' character (SIP only)
     */
    private HashSet<String> multiHeader = new HashSet<String>();
    /**
     * Table of the compressed headers : some headers are compressed with just a leter This table has the corresponding long headers values (SIP only)
     */
    private HashMap<String, String> compressedHeader = new HashMap<String, String>();

    private MSRPMsgParser parser;

    // --- construct --- //
    public MSRPTextMessage(String protocol) throws Exception {
        this.protocol = protocol;
    }

    public void parse(String msg) throws Exception {

        // we can trim because msrp starts and ends with non-whitespace characters
        msg = msg.trim();

        // find index of separation between headers and content
        int iDoubleNL = msg.indexOf("\n\n");
        if (iDoubleNL < 0) {
            iDoubleNL = msg.indexOf("\r\n\r\n");
        }
        if (iDoubleNL < 0) {
            iDoubleNL = msg.length();
        }

        String content = msg.substring(iDoubleNL).trim();

        // get of the headers of the message 
        this.headers = msg.substring(0, iDoubleNL).trim();

        // make sure we have only CRLF within headers
        this.headers = Utils.replaceNoRegex(this.headers, "\r\n", "\n");
        this.headers = Utils.replaceNoRegex(this.headers, "\n", "\r\n");

        // parse the headers
        this.parser = new MSRPMsgParser(multiHeader, compressedHeader);
        parser.parse(this.headers, "\r", ':', "<>", "\"\"");
        parser.processHeaders();

        // parse the firstline of the message
        String fl = parser.getHeader(null).getHeader(0);
        this.firstline = new MSRPFirstLine(fl, protocol);

        // parsing of the content of the message
        Header contentType = parser.getHeader("Content-Type");
        Header boundary = contentType.parseParameter("boundary", ";", '=', "<>", "\"\"");
        contentParser = new MSRPContentParser(content, boundary.getHeader(0));

        
        if (this.headers.contains("Byte-Range")) {
            Header byteRangeHeader = parser.getHeader("Byte-Range");
            String value = byteRangeHeader.getHeader(0).trim();

            boolean shouldReplace = false;

            try {
                String start = value.split("-")[0];
                String end = value.split("/")[0].split("-")[1];
                String total = value.split("/")[1];
                if (!Utils.isInteger(total) || !Utils.isInteger(start) || !Utils.isInteger(end)) {
                    shouldReplace = true;
                }
            }
            catch (Exception e) {
                shouldReplace = true;
            }

            if (shouldReplace) {
                int contentSize = content.indexOf("-------");
                this.headers = this.headers.replaceFirst("Byte-Range.*\\r\\n", "Byte-Range: 1-" + contentSize + "/" + contentSize + "\r\n");
            }
        }

        // compute the complete message
        StringBuilder buff = new StringBuilder(this.headers);
        if (!content.isEmpty()) {
            buff.append("\r\n\r\n");
            buff.append(content);
        }
        buff.append("\r\n");
        this.msg = buff.toString();
    }

    public void addContentParameter(Parameter var, String[] params, String path) throws Exception {
        contentParser.addContentParameter(var, params, path);
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
        }
        else {
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

    public MSRPFirstLine getFirstline() {
        return firstline;
    }

    public Header getHeader(String name) {
        Header header = parser.getHeader(name);
        if (header != null) {
            return header;
        }
        else {
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

    public char getContinuationFlag() {
        String endLine = msg.substring(msg.indexOf("-------"), msg.length());
        if (endLine.contains("$")) {
            return '$';
        }
        else if (endLine.contains("+")) {
            return '+';
        }
        else {
            return '#';
        }

    }

}
