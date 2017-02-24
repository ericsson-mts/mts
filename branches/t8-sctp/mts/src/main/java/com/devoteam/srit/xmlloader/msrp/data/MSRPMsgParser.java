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
import java.util.Iterator;
import java.util.Vector;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.Header;

public class MSRPMsgParser {

    private HashSet<String> multiHeader = new HashSet<String>();
    private HashMap<String, String> compressedHeader = new HashMap<String, String>();

    private HashMap<Object, Header> parsingList;

    // --- construct --- //
    public MSRPMsgParser() {
        this.parsingList = new HashMap<Object, Header>();
    }

    // --- construct --- //
    public MSRPMsgParser(HashSet<String> multiHeader, HashMap<String, String> compressedHeader) {
        this();
        this.multiHeader = multiHeader;
        this.compressedHeader = compressedHeader;
    }

    // --- public methods --- //
    public void parseHeader(Header headerTable, String delimitor, char separator, String... escapeSeq) {
        for (int i = 0; i < headerTable.getSize(); i++) {
            String str = headerTable.getHeader(i);
            parse(str, delimitor, separator, escapeSeq);
        }
    }

    // --- public methods --- //
    public void splitHeader(Header headerTable, String delimitor, String... escapeSeq) {
        for (int i = 0; i < headerTable.getSize(); i++) {
            String str = headerTable.getHeader(i);
            split(str, delimitor, escapeSeq);
        }
    }

    // --- public methods --- //
    public void processHeaders() {
        processMultiValueHeaders(',', "<>", "\"\"");
    }

    // --- public methods --- //
    public void processMultiValueHeaders(char delimitor, String... escapeSeq) {
        Iterator iter = parsingList.values().iterator();
        while (iter.hasNext()) {
            Header header = (Header) iter.next();
            String name = (String) header.getName();
            if (name != null) {
                name = name.toLowerCase();
            }
            if (multiHeader.contains(name)) {
                header.parseMultiValue(",", "<>", "\"\"");
            }
        }
    }

    // --- public methods --- //
    public void parse(String str, String delimitor, char separator, String... escapeSeq) {
        Vector<String> list = new Vector<String>(); //TODO récupération des parametres par le parser
        MSRPMsgParser.split(list, str, delimitor, escapeSeq);

        // process line 0
        if (list.size() > 0) {
            addHeaderWrapper(null, list.get(0));

            String[] firstLineParts = Utils.splitNoRegex(list.get(0), " ");
            addHeaderWrapper("TransactionID", firstLineParts[1]);

            if (firstLineParts.length > 3) {
                addHeaderWrapper("StatusCode", firstLineParts[2]);
                addHeaderWrapper("Comment", firstLineParts[3]);
            }
            else {
                addHeaderWrapper("Method", firstLineParts[2]);
            }
//            addHeaderWrapper(null, list.get(0));
        }

        // process other lines
        for (int i = 1; i < list.size(); i++) {
            String line = list.get(i);
            int iPos = indexOfEquals(line, separator, 0, escapeSeq);
            String name = line;
            String value = "";
            if (iPos > 0) {
                name = line.substring(0, iPos).trim();
                value = line.substring(iPos + 1, line.length()).trim();
                // process the compressed header
                String complete = compressedHeader.get(name);
                if (complete != null) {
                    addHeaderWrapper(complete, value);
                }
            }
            addHeaderWrapper(name, value);
        }
    }

    // --- public methods --- //
    public void split(String str, String delimitor, String... escapeSeq) {
        Vector<String> list = new Vector<String>();
        MSRPMsgParser.split(list, str, delimitor, escapeSeq);

        // process other lines
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            addHeaderWrapper(new Integer(i), line);
        }
    }

    public static void split(Vector<String> list, String str, String delimitor, String... escapeSeq) {
        str = str + delimitor;
        int i = 0;
        int begin = 0;
        char delimit = delimitor.charAt(0);
        while (i < str.length()) {
            if ((i > 0) && (delimitor.length() > 1)) {
                delimit = delimitor.charAt(1);
            }
            i = indexOfEquals(str, delimit, i, escapeSeq);
            if (i <= 0) {
                return;
            }
            if (str.charAt(i) == delimit) {
                String splitLine = str.substring(begin, i);
                list.add(splitLine.trim());
                begin = i + 1;
            }
            i = indexOfDifferent(str, delimit, i++);
            if (i < 0) {
                return;
            }
        }
    }

    private static int indexOfEquals(String str, char ch, int fromIndex, String... escapeSeq) {
        int escIndex = -1;
        while (fromIndex < str.length()) {
            if ((escapeSeq != null) && (escapeSeq.length > 0)) {
                if (escIndex < 0) {
                    for (int i = 0; i < escapeSeq.length; i++) {
                        if (escapeSeq[i] != null) {
                            if (str.charAt(fromIndex) == escapeSeq[i].charAt(0)) {
                                escIndex = i;
                            }
                        }
                    }
                }
                else {
                    if (str.charAt(fromIndex) == escapeSeq[escIndex].charAt(1)) {
                        escIndex = -1;
                    }
                }
            }
            if ((escIndex < 0) && (str.charAt(fromIndex) == ch)) {
                return fromIndex;
            }

            fromIndex++;
        }
        return -1;
    }

    private static int indexOfDifferent(String str, char ch, int fromIndex) {
        while (fromIndex < str.length()) {
            if (str.charAt(fromIndex) != ch) {
                return fromIndex;
            }

            fromIndex++;
        }
        return -1;
    }

    private void putParsingList(Object name, Header header) {
        if (name != null && name instanceof String) {
            name = ((String) name).toLowerCase();
        }
        parsingList.put(name, header);
    }

    private Header getParsingList(Object name) {
        if (name != null && name instanceof String) {
            name = ((String) name).toLowerCase();
        }
        return parsingList.get(name);
    }

    private void addHeaderWrapper(Object name, String value) {
        Header header = getParsingList(name);
        if (null == header) {
            header = new Header(name);
            if (null != value) {
                header.addHeader(value);
            }
            putParsingList(name, header);
        }
        else {
            header.addHeader(value);
        }
    }

    public Header getHeader(Object name) {
        Header header = getParsingList(name);
        if (header != null) {
            return header;
        }
        else {
            return new Header(name);
        }
    }

    /*
     * 	Add all headers into Parameter
     * 
     */
    public void addHeaderIntoParameter(Parameter var) throws Exception {
        Iterator iter = parsingList.values().iterator();
        while (iter.hasNext()) {
            Header header = (Header) iter.next();
            Object name = header.getName();
            if (name != null) {
                header.addHeaderIntoParameter(var, name);
            }
        }
    }

    /*
     * 	Add other headers into Parameter except 
     * To, From, Contact, Call-ID, CSeq, Route, Record-Route, Via
     * 
     */
    public void addOtherIntoParameter(Parameter var) throws Exception {
        Iterator iter = parsingList.values().iterator();
        while (iter.hasNext()) {
            Header header = (Header) iter.next();
            String name = (String) header.getName();
            if (!("To-path".equalsIgnoreCase(name))
                    && !("from-path".equalsIgnoreCase(name))
                    && !("Message-ID".equalsIgnoreCase(name))
                    && !("Transaction-ID".equalsIgnoreCase(name))
                    && !("Failure-Report".equalsIgnoreCase(name))
                    && !("Success-Report".equalsIgnoreCase(name))
                    && !("Status".equalsIgnoreCase(name))
                    && !("Content-Type".equalsIgnoreCase(name))
                    && !("Byte-Range".equalsIgnoreCase(name))
                    && !("via".equalsIgnoreCase(name))) {
                header.addHeaderIntoParameter(var, header.getName());
            }
        }
    }

}
