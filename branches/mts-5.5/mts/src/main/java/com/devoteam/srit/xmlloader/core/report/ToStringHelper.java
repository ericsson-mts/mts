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

