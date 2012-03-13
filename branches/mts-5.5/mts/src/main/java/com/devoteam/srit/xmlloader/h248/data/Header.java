package com.devoteam.srit.xmlloader.h248.data;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.h248.data.Dictionary;

public class Header 
{	
	//--- attribut --- //
	private String line = null;
	
	private String version = null;
	private String mid = null;
	private String midDomainAddress = null;
	private String midDomainName = null;
	private String midPortNumber = null;
	private String midMTPAddress = null;
	private String midDeviceName = null;
	
	// --- construct --- //
	public Header()
	{
	}
	
	public String getLine() {
		return line;
	}

	/** 
	 * Parse the header of the H248 message and build the object
	 * 
	 * The generic format for a header is (see spec at #7.1.1 chapter) : 
	 * <MEGACO_TOKEN> '/' <version> ' ' <mid> ' '
	 * where 
	 * 		<version> is the version of the protocol
	 * 		<mid> is the message identifier (mid)
	 * 
	 * @param String line : the msg header line
	 * @param int from : the index of the character to start the parsing from
	 * @return int : the next index to parse 
	 * @throws Exception
	 */
	public int parseHeader(String msg, int from) throws Exception
	{	
		int indexFrom = from;
		int indexTo = ABNFParser.indexOfKeyword(msg, ABNFParser.SLASH, indexFrom);
		if (indexTo < 0)
		{
			// Warning : no SLASH character in the header part
			return indexTo; 
		}
		indexFrom = indexTo + 1;
		
		// get the version
		indexTo = ABNFParser.indexOfKeyword(msg, ABNFParser.SEP, indexFrom);
		if (indexTo < 0)
		{
			// Warning : no SEP#1 character in the header part
			return indexTo; 
		}
		this.version = msg.substring(indexFrom, indexTo);
		indexFrom = indexTo + 1;
		
		// get the mId
		indexTo = ABNFParser.indexOfKeyword(msg, ABNFParser.SEP, indexFrom);
		if (indexTo < 0)
		{
			// Warning : no SEP#2 character in the header part
			return indexTo; 
		}
		this.mid = msg.substring(indexFrom, indexTo);
		this.line = msg.substring(from, indexTo);
				
		parseMID(this.mid, 0);
		
		return indexTo;
	}

	private int parseMID(String mid, int from) throws Exception
	{			
		int indexFrom = from;
		// get the mId domain address
		int indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.LSBRKT, indexFrom);
		if (indexTo >= 0)
		{
			indexFrom = indexTo + 1;
			indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.RSBRKT, indexFrom);
			if (indexTo < 0)
			{
				// Warning : no GT character in the mid part
				return indexTo; 
			}
			this.midDomainAddress = this.mid.substring(indexFrom, indexTo);
			indexFrom = indexTo + 1;
			indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.COLON, indexFrom);
			if (indexTo < 0)
			{
				this.midPortNumber = "";
				return indexTo;
			}
			indexFrom = indexTo + 1;
			this.midPortNumber = mid.substring(indexFrom, mid.length());
			return indexTo;
		}

		// get the mId domain name
		indexTo = ABNFParser.indexOfKeyword(mid, ABNFParser.LT, indexFrom);
		if (indexTo >= 0)
		{
			indexFrom = indexTo + 1;
			indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.GT, indexFrom);
			if (indexTo < 0)
			{
				// Warning : no GT character in the mid part
				return indexTo; 
			}
			this.midDomainName = this.mid.substring(indexFrom, indexTo);
			indexFrom = indexTo + 1;
			indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.COLON, indexFrom);
			if (indexTo < 0)
			{
				this.midPortNumber = "";
				return indexTo;
			}
			indexFrom = indexTo + 1;
			this.midPortNumber = this.mid.substring(indexFrom, mid.length());
			return indexTo;
		}

		// get the mId MTP address
		indexTo = ABNFParser.indexOfKeyword(mid, ABNFParser.LBRKT, indexFrom);
		if (indexTo >= 0)
		{
			indexFrom = indexTo + 1;
			indexTo = ABNFParser.indexOfKeyword(this.mid, ABNFParser.RBRKT, indexFrom);
			if (indexTo < 0)
			{
				// Warning : no RBRKT character in the mid part
				return indexTo; 
			}
			this.midMTPAddress = this.mid.substring(indexFrom, indexTo);
			return indexTo;
		}
		
		// get the mId device name
		this.midDeviceName = this.mid;
		return indexTo;
	}
	
    /** Add to a parameter from the message */
    public void addParameter(Parameter var, String path) throws Exception
    {
	    if (path.equalsIgnoreCase("version"))
	    {
	    	var.add(this.version);
	    }
	    else if (path.equalsIgnoreCase("mid"))
	    {
	    	var.add(this.mid);
	    }
	    else if (path.equalsIgnoreCase("midDomainAddress"))
	    {
	    	var.add(this.midDomainAddress);
	    }
	    else if (path.equalsIgnoreCase("midDomainName"))
	    {
	    	var.add(this.midDomainName);
	    }
	    else if (path.equalsIgnoreCase("midPortNumber"))
	    {
	    	var.add(this.midPortNumber);
	    }
	    else if (path.equalsIgnoreCase("midMTPAddress"))
	    {
	    	var.add(this.midMTPAddress);
	    }
	    else if (path.equalsIgnoreCase("midDeviceName"))
	    {
	    	var.add(this.midDeviceName);
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
			resolvedName = Dictionary.getInstance().getLongToken(ABNFParser.MEGACOP_TOKEN);
		}
		catch (Exception e)
		{
			// nothing to do
		}
		
		res += resolvedName;
		res += "(" + ABNFParser.MEGACOP_TOKEN + ")";
		if (version != null)
		{
			res += "/" + version;
		}
		if (mid != null)
		{
			res += " " + mid + " ";
		}
		return res;
	}

}
