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

package com.devoteam.srit.xmlloader.core.report;
// @author Christian Ullenboom
// @url http://java-tutor.com
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.ArrayList;

public final class ToStringHelper {
	 public static String toString( Object o ) {
	   if (o != null){
			 ArrayList list = new ArrayList();
			   toString( o, o.getClass(), list );
			   return o.getClass().getName().concat( list.toString() );
		   
	   }
	   else
		   return "";
	 }

	 private static void toString( Object o, Class clazz, ArrayList list ) {
	   Field f[] = clazz.getDeclaredFields();
	   AccessibleObject.setAccessible( f, true );
	   for ( int i = 0; i < f.length; i++ ) {
	     try {
	       list.add( f[i].getName() + "=" + f[i].get(o) );
	       }
	     catch ( IllegalAccessException e ) { e.printStackTrace(); }
	     }
	     if ( clazz.getSuperclass().getSuperclass() != null )
	        toString( o, clazz.getSuperclass(), list );
	     }
	}

