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

package com.devoteam.srit.xmlloader.core.coding.binary;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import com.devoteam.srit.xmlloader.core.utils.Utils;


/**
 *
 * @author fhenry
 */
public class EnumRange 
{
    private long beginValue;
	private long endValue;

	private String name = null;
	private String strBeginName = null;
	private String strEndName = null;
	
	private double beginName = Double.MIN_VALUE;
	private double endName = Double.MIN_VALUE;
	
	public long getBeginValue()
	{
		return beginValue;
	}

	public EnumRange(String beginStr, String endStr, String name) 
    {
    	byte[] beginBytes = Utils.parseBinaryString(beginStr.trim());
    	this.beginValue = toLong(beginBytes);
    	byte[] endBytes = Utils.parseBinaryString(endStr.trim());
    	this.endValue = toLong(endBytes);

    	this.name = name;
    	
		String strRange = name;
		int iPosBegin = strRange.indexOf('[');
    	if (iPosBegin >= 0)
    	{
    		this.strBeginName = strRange.substring(0, iPosBegin);
    		this.name = null;
    		strRange = strRange.substring(iPosBegin);
    	}
		int iPosEnd = strRange.lastIndexOf(']');
    	if (iPosEnd >= 0)
    	{
    		this.strEndName = strRange.substring(iPosEnd + 1);
    		this.name =  null;
    		strRange = strRange.substring(1, iPosEnd);
    	}
    	
    	// we have a range in the label
    	if (this.name == null)
    	{
	    	String strRangeBegin = strRange;
	    	String strRangeEnd = strRange;
			int iPosMinus = strRange.indexOf('-');
	    	if (iPosMinus >= 0)
	    	{
	    		strRangeBegin = strRange.substring(0, iPosMinus);
	    		this.beginName = Double.parseDouble(strRangeBegin);
	    		strRangeEnd = strRange.substring(iPosMinus + 1);
	        	this.endName = Double.parseDouble(strRangeEnd);
	    	}
    	}
    }

	public Long getValueFromLabel(String name)
	{
		String patternBegin = this.strBeginName;
		int posBeginPattern = this.strBeginName.indexOf('(');
		if (posBeginPattern >= 0)
		{
			patternBegin = patternBegin.substring(0, posBeginPattern).trim();
		}
		int posBegin = name.indexOf(patternBegin);
		if (posBegin < 0)
		{
			return null;
		}

		String patternEnd = this.strEndName;
		int posEndPattern = this.strEndName.indexOf('(');
		if (posEndPattern >= 0)
		{
			patternEnd = patternEnd.substring(0, posEndPattern).trim();
		}
		int posEnd = name.lastIndexOf(patternEnd);
		if (posEnd < 0)
		{
			return null;
		}
		
		String strName = name.substring(this.strBeginName.length(), posEnd);
		double doubleLabel = Double.parseDouble(strName.trim());
		
		if (doubleLabel < this.beginName)
		{
			return null;
		}
		if (doubleLabel > this.endName)
		{
			return null;
		}
		
		double doubleName = (this.endName - this.beginName);
		doubleName = (this.endValue - this.beginValue) / doubleName;
		double doubleValue = (doubleLabel - this.beginName) * doubleName + this.beginValue;
		
		return (long) doubleValue;
	}

	public String getLabelFromValue(long value)
	{
		if ((value >= this.beginValue) && (value <= this.endValue))
		{
			if (this.name != null)
			{
				return this.name;
			}
			else
			{
				double doubleLabel = (this.endName - this.beginName);
				doubleLabel = doubleLabel / (this.endValue - this.beginValue);
				doubleLabel = this.beginName + (value - this.beginValue) * doubleLabel;
				
				DecimalFormat df = new DecimalFormat("#.##");
				df.setRoundingMode(RoundingMode.HALF_UP);
				String doubleName = df.format(doubleLabel);
				
				return this.strBeginName + doubleName + this.strEndName;
			}
		}
		else
		{
			return null;
		}
	}

    public long getRandomValue() throws Exception 
    {
    	return Utils.randomLong(beginValue, endValue);
    }
	
	public String toString()
	{
		String ret = "<enum ";
		ret += " value=\""+ this.beginValue + "-"+ this.endValue + "\"";
		if (this.name != null)
    	{
			ret += " name=\"" + this.name + "\"";
    	}
		// we have a range in the label
		else
		{
			ret += " name=\"" + this.strBeginName + "[";
	    	DecimalFormat df = new DecimalFormat("#.##");
	    	df.setRoundingMode(RoundingMode.HALF_UP);
	    	ret += df.format(beginName) + "-" + df.format(endName);
	    	ret += "]" + this.strEndName + "\"";
	    }
		ret += "/>";
		return ret;
	}
	
	public static long toLong(byte[] bytes)
	{	
		Array array = new DefaultArray(bytes);
		String hexa = Array.toHexString(array);
		BigInteger BI = new BigInteger(hexa, 16);
		//BigInteger BI = new BigInteger(array.getBytes()); DON'T WORK because signed values
		return BI.longValue();
	}

}
