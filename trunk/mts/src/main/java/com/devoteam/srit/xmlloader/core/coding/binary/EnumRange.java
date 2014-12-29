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

import com.devoteam.srit.xmlloader.core.utils.Utils;


/**
 *
 * @author fhenry
 */
public class EnumRange 
{
    private long beginValue;

	private long endValue;

	private String name;

	public long getBeginValue()
	{
		return beginValue;
	}

	public String getName() {
		return name;
	}
	public EnumRange(String beginStr, String endStr, String name) 
    {
    	byte[] beginBytes = Utils.parseBinaryString(beginStr.trim());
    	this.beginValue = (int) beginBytes[0] & 0xFF;
    	byte[] endBytes = Utils.parseBinaryString(endStr.trim());
    	this.endValue = (int) endBytes[0] & 0xFF;
    	this.name = name;
    }

	public boolean isEnclosedInto(long value)
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
	
	public String getEnclosedLabel(long value)
	{
		String strLabel = name;
		String strNameBegin = name;
		String strNameEnd = name;
		int iPosBegin = name.indexOf('[');
    	if (iPosBegin >= 0)
    	{
    		strNameBegin = strLabel.substring(0, iPosBegin);
    		strLabel = strLabel.substring(iPosBegin);
    	}
		int iPosEnd = strLabel.indexOf(']');
    	if (iPosEnd >= 0)
    	{
    		strNameEnd = strLabel.substring(iPosEnd + 1);
    		strLabel = strLabel.substring(1, iPosEnd);
    	}
    	String strLabelBegin = strLabel;
    	String strLabelEnd = strLabel;
		int iPosMinus = strLabel.indexOf('-');
    	if (iPosMinus >= 0)
    	{
    		strLabelBegin = strLabel.substring(0, iPosMinus);
    		strLabelEnd = strLabel.substring(iPosMinus + 1);
    	}
    	double labelBegin = Double.parseDouble(strLabelBegin);
    	double labelEnd = Double.parseDouble(strLabelEnd);
    	
    	double doubleLabel = (labelEnd - labelBegin);
    	doubleLabel = doubleLabel / (endValue - beginValue - 1);
    	doubleLabel = labelBegin + (value - beginValue) * doubleLabel;
    	//long intLabel = (long) Math.round(doubleLabel);
    	return strNameBegin + doubleLabel + strNameEnd;
	}
	public String toString()
	{
		String ret = "<enum ";
		ret += " value=\""+ this.beginValue + ":"+ this.endValue + "\"";
		ret += " name=\"" + this.name + "\"";
		ret += "\"/>";
		return ret;
	}
}
