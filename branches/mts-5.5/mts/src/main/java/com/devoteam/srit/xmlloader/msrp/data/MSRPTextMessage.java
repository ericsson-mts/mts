package com.devoteam.srit.xmlloader.msrp.data;

import java.util.HashMap;
import java.util.HashSet;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.Header;

public class MSRPTextMessage{
			
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
	 * List of the multi-value headers : the different values are on the same line 
	 * separated with the ',' character (SIP only) 
	 */
	private HashSet<String> multiHeader = new HashSet<String>();
	/**
	 * Table of the compressed headers : some headers are compressed with just a leter
	 * This table has the corresponding long headers values (SIP only) 
	 */	
	private HashMap<String, String> compressedHeader = new HashMap<String, String>();

	private MSRPMsgParser parser;
	
	// --- construct --- //
	public MSRPTextMessage(String protocol) throws Exception
	{
		this.protocol = protocol;
	}

	public void parse(String msg) throws Exception 
	{
		// case a header is contining at the next line
		
        msg = Utils.replaceNoRegex(msg, "\r\n", "\n");        
		msg = Utils.replaceNoRegex(msg, "\n", "\r\n");
		
		msg = msg.replace('\t', ' ');
		msg = Utils.replaceNoRegex(msg, "\r\n ", " ");		

		// get of the content of the message
		int iPosContent = msg.indexOf("\r\n\r\n");
		if (iPosContent < 0)
		{
			iPosContent = msg.length();
		}
		String content = msg.substring(iPosContent).trim();
        
		// get of the headers of the message 
		this.headers = msg.substring(0, iPosContent).trim();
		// Calculate the Content-Length header is not present in the message or has an invalid value
		
		// parse the headers
		this.parser = new MSRPMsgParser(multiHeader, compressedHeader); 
		parser.parse(headers, "\r", ':', "<>", "\"\"");		
		parser.processHeaders();		
        
		// parse the firstline of the message
		String fl = parser.getHeader(null).getHeader(0);
		this.firstline = new MSRPFirstLine(fl, protocol);

		// parsing of the content of the message
		Header contentType = parser.getHeader("Content-Type");
		Header boundary = contentType.parseParameter("boundary", ";", '=', "<>", "\"\"");
        contentParser = new MSRPContentParser(content, boundary.getHeader(0));
		
        // calculate the complete message
		StringBuilder buff = new StringBuilder(this.headers);
		if(iPosContent != msg.length())//just for message with no content
            buff.append("\r\n\r\n").append(content);
        buff.append("\r\n");
		this.msg = buff.toString();
	}

	public void addContentParameter(Parameter var, String[] params, String path) throws Exception 
	{
		contentParser.addContentParameter(var, params, path);
	}

    /*
     * 	Calculate the Content-Length header is not present in the message or has an invalid value
     * 
     */
    public static String getHeaderValue(String headers, String hName) throws Exception
    {
    	// get the Content-Length header value
        int iPosLength = headers.indexOf("\r\n" + hName);
        if (iPosLength >= 0)
        {
        	int iPosLengthValue = headers.indexOf(":", iPosLength + 2);        	
        	int iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
        	if (iPosLengthEnd < 0)
        	{
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
    private static String removeHeader(String headers, String name) throws Exception
    {
    	// get the Content-Length header value
        int iPosLength = headers.indexOf("\r\n" + name);
        if (iPosLength >= 0)
        {        	
        	int iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
        	if (iPosLengthEnd < 0)
        	{
        		iPosLengthEnd = headers.length();
        	} 
        	// remove the Content-Length header
    		return headers.substring(0, iPosLength) + headers.substring(iPosLengthEnd, headers.length()); 
        }
        else
        {
        	return headers;
        }
    }
    
    /*
     * 	Remove the all occurences of a header from an index
     * 
     */
    public static String removeAllHeader(String headers, String name) throws Exception
    {   
    	int iPosLength = 0; 
    	int iPosLengthEnd = 0;
    	while (iPosLength >= 0) 
    	{
	    	// get the Content-Length header value
	        iPosLength = headers.indexOf("\r\n" + name, iPosLength);
	        if (iPosLength >= 0)
	        {        	
	        	iPosLengthEnd = headers.indexOf("\r\n", iPosLength + 2);
	        	if (iPosLengthEnd < 0)
	        	{
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
    public void addHeaderIntoParameter(Parameter var) throws Exception
    {
    	parser.addHeaderIntoParameter(var);
    }

	/*
     * 	Add other headers into Parameter
     * 
     */
    public void addOtherIntoParameter(Parameter var) throws Exception
    {
    	parser.addOtherIntoParameter(var);
    }

    
	public String getMessage(){
		return this.msg;
	}
	
	public MSRPFirstLine getFirstline() {
		return firstline;
	}

	public Header getHeader(String name){
		Header header = parser.getHeader(name);
		if (header != null)
		{
			return header;
		}
		else
		{
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
	
	public char getContinuationFlag(){
		String endLine = msg.substring(msg.indexOf("-------"), msg.length());
		if(endLine.contains("$")){
			return '$';
		}else if(endLine.contains("+")){
			return '+';
		}else {
			return '#';
		}
		
	}
		
}
