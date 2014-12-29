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

	private String strBeginName;
	private String strEndName;
	
	private double beginName;
	private double endName;
	
	public long getBeginValue()
	{
		return beginValue;
	}

	public EnumRange(String beginStr, String endStr, String name) 
    {
    	byte[] beginBytes = Utils.parseBinaryString(beginStr.trim());
    	this.beginValue = (int) beginBytes[0] & 0xFF;
    	byte[] endBytes = Utils.parseBinaryString(endStr.trim());
    	this.endValue = (int) endBytes[0] & 0xFF;
    	
		String strRange = name;
		this.strBeginName = name;
		this.strEndName = name;
		int iPosBegin = name.indexOf('[');
    	if (iPosBegin >= 0)
    	{
    		this.strBeginName = strRange.substring(0, iPosBegin);
    		strRange = strRange.substring(iPosBegin);
    	}
		int iPosEnd = strRange.indexOf(']');
    	if (iPosEnd >= 0)
    	{
    		this.strEndName = strRange.substring(iPosEnd + 1);
    		strRange = strRange.substring(1, iPosEnd);
    	}
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

	public boolean isEnclosedFromValue(long value)
	{
		if ((value >= beginValue) && (value <= endValue))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isEnclosedFromLabel(String label)
	{
		/*
		if ((value >= beginValue) && (value <= endValue))
		{
			return true;
		}
		else
		{
			return false;
		}
		*/
		return false;
	}

	public String getEnclosedLabel(long value)
	{    	
    	double doubleLabel = (endName - beginName);
    	doubleLabel = doubleLabel / (endValue - beginValue);
    	doubleLabel = beginName + (value - beginValue) * doubleLabel;
    	
    	DecimalFormat df = new DecimalFormat("#.##");
    	df.setRoundingMode(RoundingMode.HALF_UP);
    	String resultLabel = df.format(doubleLabel);

    	return strBeginName + resultLabel + strEndName;
	}
	public String toString()
	{
		String ret = "<enum ";
		ret += " value=\""+ this.beginValue + "-"+ this.endValue + "\"";
		ret += " name=\"" + this.strBeginName + "[";
    	DecimalFormat df = new DecimalFormat("#.##");
    	df.setRoundingMode(RoundingMode.HALF_UP);
    	ret += df.format(beginName) + "-" + df.format(endName);
    	ret += this.strEndName + "\"";
		ret += "\"/>";
		return ret;
	}
}
