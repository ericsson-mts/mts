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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author fhenry
 */
public class Embeddeds 
{    
	private List<Embedded> embeddedlist = new ArrayList<Embedded> ();
	
    public Embeddeds()
    {
    }
		
    public List<Embedded> getEmbedded() 
	{
    	return this.embeddedlist;
	}

	public void addEmbedded(Embedded embedded) 
	{
		this.embeddedlist.add(embedded);
	}
        
}
