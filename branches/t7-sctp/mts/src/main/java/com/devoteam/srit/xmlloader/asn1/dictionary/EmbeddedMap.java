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

package com.devoteam.srit.xmlloader.asn1.dictionary;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author fhenry
 */
public class EmbeddedMap 
{    
	private HashMap<String, Embedded> embeddedsByIniial;
	
	private HashMap<String, Embeddeds> embeddedsByCondition;
	
    public EmbeddedMap()
    {
    	this.embeddedsByIniial = new HashMap<String, Embedded> ();
    	this.embeddedsByCondition = new HashMap<String, Embeddeds> ();
    }
		
    public Embedded getEmbeddedByInitial(String initial) 
	{
    	return this.embeddedsByIniial.get(initial);
	}

    public List<Embedded> getEmbeddedByCondition(String condition) 
	{
    	Embeddeds embeddedList = this.embeddedsByCondition.get(condition);
    	if (embeddedList != null)
    	{
    		return embeddedList.getEmbedded();
    	}
    	return null;
	}

	public void addEmbedded(Embedded embedded) 
	{
		this.embeddedsByIniial.put(embedded.getInitial(), embedded);
		String condition = embedded.getCondition();
		Embeddeds embeddeds = this.embeddedsByCondition.get(condition);
		if (embeddeds == null)
		{
			embeddeds = new Embeddeds();
			this.embeddedsByCondition.put(condition, embeddeds);
		}
		embeddeds.addEmbedded(embedded);
	}
    
	public void removeEmbedded(Embedded embedded) 
	{
		embeddedsByIniial.remove(embedded.getInitial());
		embeddedsByCondition.remove(embedded.getCondition());
	}
    
}
