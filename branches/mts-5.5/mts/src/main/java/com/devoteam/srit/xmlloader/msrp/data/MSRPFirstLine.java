package com.devoteam.srit.xmlloader.msrp.data;

import com.devoteam.srit.xmlloader.core.utils.Utils;

public class MSRPFirstLine {
	//--- attribut --- //
	private String line = null;
	private String method = null;
	private String comment = null;
	private String statusCode = null;
	private String transId = null;
	private boolean isRequest = false;
	
	public MSRPFirstLine(String line, String protocol){
        if (line == null)
        {
            line = "";
        }
		this.line = line.trim();
		
		String[] parts = Utils.splitNoRegex(line, " ");
		
//		if(!parts[0].equalsIgnoreCase("MSRP"))
		if(!parts[0].equalsIgnoreCase(protocol))
		{
			try {
				throw new Exception("Mismatch Protocol. Got " + line + " while " + protocol + " expected" );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.transId = parts[1].trim();
		
		if (parts.length == 3 && !Utils.isInteger(parts[2]))
		{
			this.isRequest = true;
			this.method = parts[2];
		}
		else
		{
			this.isRequest = false;
			this.statusCode = parts[2];
			if(parts.length > 3)
			{
				this.comment = parts[3].trim();
			}
		}			
	}

	
	
	public String getLine() {
		return line;
	}

	public boolean isRequest() {
		return isRequest;
	}

	public String getMethod() {
		return method;
	}

	public String getComment() {
		return comment;
	}

	public String getStatusCode() {
		return statusCode;
	}
	
	public String getTransID() {
		return transId;
	}
	}
