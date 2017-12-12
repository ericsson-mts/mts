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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Class for parsing CSV files.
 * It manages quotes and commentaries.
 *
 * @author rbarbot
 */
public class CSVReader 
{
    private String separator;
    private String comment;
    private String quote;

    /**
     *
     * @param fileName
     * @param comment - comment character - can be null
     * @param quote - quote character - can be null
     * @throws Exception
     */
    public CSVReader(String comment, String separator, String quote) throws Exception
    {
        this.separator = separator;
        this.comment = comment;
        this.quote = quote;
    }
   
    private String[] parseCSVLine(String line, boolean checkDifferent) throws Exception 
    {
		// blank line
		line = line.trim();
	    if ("".equals(line)) 
	    {
	        return null;
	    }
	    // comment line
	    if (line.startsWith(comment)) 
	    {
	        return null;
	    }
	    
	    Vector<String> parseLine = new Vector<String>();
	    MsgParser.split(parseLine, line, separator, checkDifferent, quote);

    	String[] data = new String[parseLine.size()];
	    for (int i = 0; i < parseLine.size(); i++)
	    {
	    	String value = (String) parseLine.get(i).trim();
	    	if ((value.length() > 0) && (quote != null))
	    	{
	    		if (value.charAt(0) == quote.charAt(0))
	        	{
	        		value = value.substring(1);
	        	}
	        	if (value.charAt(value.length() - 1) == quote.charAt(1))
	        	{
	        		value = value.substring(0, value.length() - 1);
	        	}
	    	}
	    	data[i] = value.trim();
	    }
	    return data;
    }

    public List<String> loadData(URI uri, int column, boolean ignoreFirst) throws Exception
    {
    	ArrayList<String> result = new ArrayList<String>();
		
        BufferedReader in = null;
        try {
	        in = new BufferedReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(uri)));
	        String line = "";
	        int num = 0;
	        while (line != null)
	        {
	        	line = in.readLine();
	        	// line is null => end of file
	        	if (line == null) 
	        	{
	        		break;
	        	}
	        	String[] data = parseCSVLine(line, false);
	        	if (data != null)
	        	{
		        	if (num != 0 || !ignoreFirst)
		        	{
			        	if (column < data.length)
			        	{
			        		result.add(data[column]);
			        	}
		        	}
		        	num = num + 1;
	        	}
	        }
	        in.close();
        }
        catch(Exception e)
        {
        	if (in != null)	in.close();
            throw e;
        }		        

        return result;
    }
    
    public String[] loadHeader(URI uri) throws Exception
    {
    	String[] result = null;
		
        BufferedReader in = null;
        try {
	        in = new BufferedReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(uri)));
	        String line = "";
	        while (result == null)
	        {
	        	line = in.readLine();
	        	// line is null => end of file
	        	if (line == null) 
	        	{
	        		break;
	        	}
	        	result = parseCSVLine(line, false);
	        }
	        in.close();
        }
        catch(Exception e)
        {
        	if (in != null)	in.close();
            throw e;
        }		        

        return result;
    }
    
    public List<String[]> loadAllData(URI uri) throws Exception
    {
    	ArrayList<String[]> csvData = new ArrayList<String[]>();
		
        BufferedReader in = null;
        try {
	        in = new BufferedReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(uri)));
	        String line = "";
	        while (line != null)
	        {
	        	line = in.readLine();
	        	// line is null => end of file
	        	if (line == null) 
	        	{
	        		break;
	        	}
	        	String[] data = parseCSVLine(line, false);
	        	if (data != null)
	        	{
	        		csvData.add(data);
	        	}
	        }
	        in.close();
        }
        catch(Exception e)
        {
        	if (in != null)	in.close();
            throw e;
        }		        

        return csvData;
    }
}
