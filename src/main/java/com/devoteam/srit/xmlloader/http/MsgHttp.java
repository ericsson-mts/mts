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

package com.devoteam.srit.xmlloader.http;

import java.io.InputStream;

import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

/**
 * @author gpasquiers
 */
public class MsgHttp extends Msg {

    private HttpMessage message;
    private String messageContent;
    private boolean ignoreContents;

    private String type = null;

    /**
     * Creates a new instance
     */
    public MsgHttp(Stack stack) {
        super(stack);
    }

    /**
     * Creates a new instance
     */
    public MsgHttp(Stack stack, HttpMessage aMessage) throws Exception {
        this(stack);

        this.ignoreContents = getConfig();
        this.message = aMessage;

        //
        // Get the entity
        //
        HttpEntity entity;
        if (message instanceof ClassicHttpResponse) {
            entity = ((ClassicHttpResponse) message).getEntity();
        } else if (message instanceof ClassicHttpRequest) {
            entity = ((ClassicHttpRequest) message).getEntity();
        } else {
            entity = null;
        }

        //
        // Convert the entity to a String
        //
        if (null != entity && (entity.getContentLength() != -1 || entity.isChunked())) {
            if (this.ignoreContents) {
                entity.getContent();
                messageContent = "";
            } else {
                InputStream inputStream = entity.getContent();
                int l;
                byte[] tmp = new byte[2048];
                SupArray array = new SupArray();

                while ((l = inputStream.read(tmp)) != -1) {
                    byte[] bytes = new byte[l];
                    for (int i = 0; i < l; i++) bytes[i] = tmp[i];
                    array.addLast(new DefaultArray(bytes));

                }
                inputStream.close();
                messageContent = new String(array.getBytes());
            }
        } else {
            messageContent = null;
        }
    }

    /**
     * Returns the HTTP message without entity
     */
    public HttpMessage getMessage() {
        return message;
    }

    /**
     * Returns the entity of a HTTP message
     */
    public String getMessageContent() {
        return messageContent;
    }

    public void setMessage(HttpMessage message) {
        this.message = message;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public boolean getConfig() {
        return Config.getConfigByName("http.properties").getBoolean("message.IGNORE_RECEIVED_CONTENTS", false);
    }

    public HttpVersion retrieveHttpVersion(String[] parts, boolean response) throws ParsingException {

        HttpVersion httpVersion = HttpVersion.HTTP_1_1;
        // Message is a response
        if (response) {
            if (parts[0].endsWith("HTTP/1.0")) {
                httpVersion = HttpVersion.HTTP_1_0;
            } else if (parts[0].endsWith("HTTP/2.0")) {
                throw new ParsingException("Bad HTTP Version in message (should be HTTP/1.x) : \"version:" + parts[0]);
            }
        } // Message is a request
        else {
            if ((parts.length == 3) && (parts[2].endsWith("HTTP/1.0"))) {
                httpVersion = HttpVersion.HTTP_1_0;
            } else if ((parts.length == 3) && (parts[2].endsWith("HTTP/2.0"))) {
                throw new ParsingException("Bad HTTP Version in message (should be HTTP/1.x) : \"version:" + parts[2]);
            }
        }
        return httpVersion;
    }

    @Override
    public void setTransactionId(TransactionId transactionId) {
        super.setTransactionId(transactionId);

        if (isRequest() && null != getType()) return;

        try {
            Trans trans = this.stack.getInTransaction(transactionId);
            if (trans != null) {
                Msg req = trans.getBeginMsg();
                this.setType(req.getType());
            }
        } catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Could not find the request matching this answer", this, "\n and thus, couldn't set the Type of the answer");
        }
    }

    /**
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() {
        if (message instanceof HttpRequest) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters
     */
    @Override
    public String getType() {
        if (null == type) {
            if (message instanceof HttpRequest) {
                type = ((HttpRequest) message).getMethod();
            }
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters
     */
    @Override
    public String getResult() {
        if (message instanceof HttpResponse) {
            return Integer.toString(((HttpResponse) message).getCode());
        } else {
            return null;
        }
    }

    /**
     * Return the transport of the message
     */
    public String getTransport() {
        return StackFactory.PROTOCOL_TCP;
    }

    private String getTextMessage() {
        // get the first line
        String ret = getFirstLine();
        ret += "\r\n";

        // get the headers list
        Header[] header = message.getHeaders();
        for (int i = 0; i < header.length; i++) {
            ret += header[i] + "\r\n";
        }
        ret += "\r\n";

        // get the message content
        if (messageContent != null) {
            ret += messageContent;
        }
        return ret;
    }

    private String getFirstLine() {
        if (this.message instanceof HttpResponse) {
            return ((HttpResponse) message).getVersion() + " " + ((HttpResponse) message).getCode() + " " + ((HttpResponse) message).getReasonPhrase();
        } else {
            // if authority isn't set, the execution won't succeed
            if (((HttpRequest) message).getAuthority() != null) {
                return type + " " + ((HttpRequest) message).getScheme() + "://" + ((HttpRequest) message).getAuthority().toString() + " " + ((HttpRequest) message).getVersion();
            } else {
                return type + " " + ((HttpRequest) message).getPath() + " " + ((HttpRequest) message).getVersion();
            }
        }
    }


    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /**
     * encode the message to binary data
     */
    @Override
    public byte[] encode() throws Exception {
        return getTextMessage().getBytes();
    }

    /**
     * decode the message from binary data
     */
    @Override
    public void decode(byte[] data) throws Exception {
        // nothing to do : we use external Tomcat HTTP stack to transport messages
    }


    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** Returns a short description of the message. Used for logging as INFO level */
    /**
     * This methods HAS TO be quick to execute for performance reason
     */
    @Override
    public String toShortString() throws Exception {
        String ret = super.toShortString();
        ret += "\n";
        ret += getFirstLine();
        ret += "\n";
        ret += "<MESSAGE transactionId =\"" + getTransactionId() + "\">";
        return ret;
    }

    /**
     * Convert the message to XML document
     */
    @Override
    public String toXml() throws Exception {
        return getTextMessage();
    }

    /**
     * Parse the message from XML element
     */
    @Override
    public void parseFromXml(ParseFromXmlContext request1, Element root, Runner runner) throws Exception {
        super.parseFromXml(request1, root, runner);

        String text = root.getText();

        BasicClassicHttpResponse responseMessage = null;
        BasicClassicHttpRequest requestMessage = null;

        HttpMessage currentMessage = null;

        int endOfLine;
        String line;

        //
        // Clean beginning of first line (XML-related control characters)
        //
        while (text.startsWith("\n") || text.startsWith("\r") || text.startsWith(" ") || text.startsWith("\t")) {
            text = text.substring(1);
        }

        while (text.endsWith("\n") || text.endsWith("\r") || text.endsWith(" ") || text.endsWith("\t")) {
            text = text.substring(0, text.length() - 1);
        }

        String headers = text.split("[\\r]?[\\n][\\r]?[\\n]")[0];
        String datas = text.substring(headers.length());
        if (datas.startsWith("\r")) datas = datas.substring(1);
        if (datas.startsWith("\n")) datas = datas.substring(1);
        if (datas.startsWith("\r")) datas = datas.substring(1);
        if (datas.startsWith("\n")) datas = datas.substring(1);

        //
        // Use only \n: remove \r
        //
        headers = headers.replace("\r", "");


        //
        // Whatever the message is, it seems it should end with a new line
        //
        headers += "\n";

        //
        // Get first line
        //
        endOfLine = headers.indexOf("\n");
        line = headers.substring(0, endOfLine);

        // Remove first line from data
        headers = headers.substring(endOfLine + 1, headers.length());

        // Message is a response
        if (line.startsWith("HTTP")) {
            String[] parts = line.split(" ");

            HttpVersion httpVersion = retrieveHttpVersion(parts, true);

            String phrase = "";
            if (parts.length > 2) {
                phrase = parts[2];
            }

            responseMessage = new BasicClassicHttpResponse(Integer.parseInt(parts[1]), phrase);
            currentMessage = responseMessage;
            currentMessage.setVersion(httpVersion);
        } // Message is a request
        else {
            String[] parts = line.split(" ");
            HttpVersion httpVersion = retrieveHttpVersion(parts, false);

            requestMessage = new BasicClassicHttpRequest(parts[0], parts[1]);
            requestMessage.setPath(parts[1]);
            currentMessage = requestMessage;
            currentMessage.setVersion(httpVersion);
        }

        //
        // Parse headers
        //
        endOfLine = headers.indexOf("\n");
        if (endOfLine != -1) {
            line = headers.substring(0, endOfLine);
        }
        boolean contentLengthPresent = false;
        while (endOfLine != -1 && line.length() > 0) {
            // Remove line from data since we parse it here
            headers = headers.substring(endOfLine + 1, headers.length());

            // Add header to message
            int index = line.indexOf(":");

            if (index == -1) {
                throw new ExecutionException("Invalid header ':' not found " + line);
            }

            String name = line.substring(0, index).trim();
            String value = line.substring(index + 1, line.length()).trim();
            if (!name.equalsIgnoreCase("Content-Length") || Utils.isInteger(value)) {
                currentMessage.addHeader(name, value);
            } else {
                contentLengthPresent = true;
            }

            // Read next line
            endOfLine = headers.indexOf("\n");
            if (endOfLine != -1) {
                line = headers.substring(0, endOfLine);
            }
        }

        //
        // Register parsed message into the class variable
        //
        message = currentMessage;

        //
        // Set entity into the message
        //
        if (datas.length() > 0) {
            //
            // Check if we are allowed to have an entity in this message
            //
            if (null != requestMessage) {
                if (requestMessage.getMethod().toLowerCase().equals("get") ||
                        requestMessage.getMethod().toLowerCase().equals("head")) {
                    GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Request ", requestMessage.getMethod(), " is not allowed to contain an entity");
                }
            }

            if (null != responseMessage || null != requestMessage) {
                messageContent = datas;
            }

            //
            // Set the Content-Length header if the transfer-encoding is not "chunked"
            //
            Header[] contentEncoding = currentMessage.getHeaders("Content-Encoding");
            Header[] transferEncoding = currentMessage.getHeaders("Transfer-Encoding");
            Header[] contentTypeHeaders = currentMessage.getHeaders("Content-Type");
            Header[] contentLength = currentMessage.getHeaders("Content-Length");

            if (contentTypeHeaders.length > 0 && contentTypeHeaders[0].getValue().toLowerCase().contains("multipart")) {
                // do nothing, we should not add a CRLF at the end of the data when multipart
            }

            String contentType = contentTypeHeaders[0].getValue();
            int semicolonIndex = contentType.indexOf(";");
            if(-1 != semicolonIndex) {
                contentType = contentType.substring(0, semicolonIndex);
            }

            /**
             * Do not add CRLF at the end of the content in case Content-Encoding is GZIP
             * This is a (bad) patch :/
             *
             * @Todo Must be re-think and fixed. 
             *       We should not add CRLF except in case of chunked feature (at least)
             *       But if I try to delete this additional CRLF, most of tutorials tests does not
             *       work anymore.   
             */
            else if (contentEncoding.length == 0 || !contentEncoding[0].getValue().toLowerCase().contains("gzip")) {
                messageContent += "\r\n"; // it seems necessary to end the data by a CRLF (???)
            }

            if (transferEncoding.length == 0 || !transferEncoding[0].getValue().contains("chunked")) {
                if (contentLength.length == 0) {
                    if (null != messageContent) {
                        currentMessage.addHeader("Content-Length", Integer.toString(messageContent.length()));
                    } else {
                        currentMessage.addHeader("Content-Length", Integer.toString(0));
                    }
                }
            } else {
                messageContent += "\r\n"; // when chunked, there must be a double CRLF after the last chunk size
            }

            HttpEntity entity = new ByteArrayEntity(messageContent.getBytes(), ContentType.create(contentType));

            if (null != responseMessage) {
                responseMessage.setEntity(entity);
            } else if (null != requestMessage) {
                requestMessage.setEntity(entity);
            } else {
                entity = null;
            }
        } else {
            if (contentLengthPresent) {
                currentMessage.addHeader("Content-Length", Integer.toString(0));
            }
        }

    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /**
     * Get a parameter from the message
     */
    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if (var != null) {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if (params.length > 1 && params[0].equalsIgnoreCase("header")) {
            //------------------------------------------------------------------------ header:xxx -
            Header[] listhead = getMessage().getHeaders(params[1]);
            for (int i = 0; i < listhead.length; i++) {
                var.add(listhead[i].getValue());
            }
        } else if (params.length == 1 && params[0].equalsIgnoreCase("firstline")) {
            //---------------------------------------------------------------------- firstline -
            if (message instanceof HttpRequest) {

                var.add(getType() + " " + ((HttpRequest) message).getScheme() + "://" + ((HttpRequest) message).getAuthority().toString() + " " + ((HttpRequest) message).getVersion());
            } else {
                var.add(((HttpResponse) message).getVersion() + " " + ((HttpResponse) message).getCode() + " " + ((HttpResponse) message).getReasonPhrase());

            }
        } else if (params.length > 1 && params[0].equalsIgnoreCase("firstline")) {
            //---------------------------------------------------------------------- firstline:Version -
            if (params[1].equalsIgnoreCase("version")) {
                var.add(message.getVersion().toString());
            }
            //---------------------------------------------------------------------- firstline:Method -
            else if (params[1].equalsIgnoreCase("method")) {
                if (message instanceof HttpRequest) {
                    var.add(((HttpRequest) message).getMethod());
                }
            }
            //---------------------------------------------------------------------- firstline:URI -
            else if (params[1].equalsIgnoreCase("uri")) {
                if (message instanceof HttpRequest) {

                    var.add(((HttpRequest) message).getScheme() + "://" + ((HttpRequest) message).getAuthority().toString());

                }
            } else if (params[1].equalsIgnoreCase("reasonPhrase")) {
                if (message instanceof HttpResponse) {
                    var.add(((HttpResponse) message).getReasonPhrase());
                }
            } else if (params[1].equalsIgnoreCase("statuscode")) {
                if (message instanceof HttpResponse) {
                    var.add(Integer.toString(((HttpResponse) message).getCode()));
                }
            } else {
                Parameter.throwBadPathKeywordException(path);
            }
        } else if (params.length >= 1 && params[0].equalsIgnoreCase("content")) {
            if (params.length == 1) {
                var.add(getMessageContent());
            } else if ((params.length > 1) && (params[1].equalsIgnoreCase("xml"))) {
                if ((params.length > 3) && (params[2].equalsIgnoreCase("xpath"))) {
                    String strXpath = params[3];
                    for (int i = 4; i < params.length; i++) {
                        strXpath += "." + params[i];
                    }
                    var.applyXPath(getMessageContent(), strXpath, true);
                } else {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
            // not documented features
            else if (path.equalsIgnoreCase("content:xml:operation")) {
                //
                // Read name of method
                //
                String content = messageContent;

                //
                // Read and delete the 3 first tags
                //
                for (int i = 0; i < 3; i++) {
                    content = content.substring(content.indexOf(">") + 1);
                }

                //
                // Read the first tag of the remaining content
                //
                String tag = content.substring(content.indexOf("<"), content.indexOf(">"));

                //
                // Read tag name
                //
                String name = tag.substring(1);
                name.trim();
                int endOfName;
                name = name.replace("\r", "");
                endOfName = name.indexOf(" ");
                if (endOfName == -1 || name.indexOf("\n") != -1 && name.indexOf("\n") < endOfName) {
                    endOfName = name.indexOf("\n");
                }

                if (endOfName == -1) {
                    endOfName = name.length();
                }

                name = name.substring(0, endOfName);

                //
                // Remove namespace
                //
                if (name.indexOf(":") != -1) {
                    name = name.substring(name.indexOf(":") + 1);
                }

                var.add(name);
            } else {
                Parameter.throwBadPathKeywordException(path);
            }
        } else {
            Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }

}
