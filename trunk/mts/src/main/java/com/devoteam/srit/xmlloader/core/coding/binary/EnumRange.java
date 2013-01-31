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
    private int begin;

	private int end;

	private String name;


	public int getBegin()
	{
		return begin;
	}

    public int getEnd() 
    {
		return end;
	}

	public String getName() {
		return name;
	}
	public EnumRange(String beginStr, String endStr, String name) 
    {
    	byte[] beginBytes = Utils.parseBinaryString(beginStr.trim());
    	this.begin = (int) beginBytes[0] & 0xFF;
    	byte[] endBytes = Utils.parseBinaryString(endStr.trim());
    	this.end = (int) endBytes[0] & 0xFF;
    	this.name = name;
    }

	public boolean isEnclosedInto(int value)
	{
		if ((value >= begin) && (value <= end))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		String ret = "<enum ";
		ret += " value=\""+ this.begin + ":"+ this.end + "\"";
		ret += " name=\"" + this.name + "\"";
		ret += "\"/>";
		return ret;
	}
}
