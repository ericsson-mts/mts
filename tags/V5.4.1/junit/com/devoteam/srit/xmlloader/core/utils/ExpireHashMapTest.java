/*
 * VarOperationTest.java
 * JUnit based test
 *
 * Created on 18 mai 2007, 09:47
 */

package com.devoteam.srit.xmlloader.core.utils;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.*;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;

/**
 *
 * @author fhenry
 */
public class ExpireHashMapTest extends TestCase {
    
	
    public ExpireHashMapTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of execute method, of class com.devoteam.srit.sigloader.core.utils.ExpireHashMap
     */
    public void estTimestampHashMap_put() throws Exception {
    	/*
    	HashMap<String, Long> map = new HashMap<String, Long>();
    	for (int i = 0; i < 1000000; i++) {
        	GregorianCalendar gc = new GregorianCalendar();    		
    		map.put(new Integer(i).toString(), new Long(gc.getTimeInMillis()));
    		// System.out.println(map.size());
    	}    	
    	System.out.println(map.size());    	
    	*/
    	
    	Map<String, Long> timestampMap = new TimestampHashMap<String, Long>(1000);        	    	
    	for (int j = 0; j < 1; j++) {
    		String str = new Long(j).toString();
	    	for (long i = 0; i < 1000000; i++) {
	        	GregorianCalendar gc = new GregorianCalendar();    		
	        	timestampMap.put(str + new Long(i).toString(), new Long(gc.getTimeInMillis()));
	    		if ((i % 100000) == 0) {
	    	    	System.out.println("iterator=" + j + i + ", size=" + timestampMap.size());    			
	    		}
	    		// System.out.println(expireMap.size());
	    	}
    	}
    	System.out.println(timestampMap.size());
    }

    /**
     * Test of execute method, of class com.devoteam.srit.sigloader.core.utils.ExpireHashMap
     */
    public void estExpireHashMap_put() throws Exception {
    	Map<String, String> expireMap = new ExpireHashMap<String, String>(10000);    
    	    	
    	for (int j = 0; j < 1; j++) {
    		String str = new Long(j).toString();
	    	for (long i = 0; i < 1000000; i++) {
	        	String key = str + new Long(i).toString();
	    		expireMap.put(key, key);
	    		if ((i % 100000) == 0) 
	    		{
	    	    	System.out.println("iterator=" + key + ", size=" + expireMap.size());    			
	    		}
	    		// System.out.println(expireMap.size());
	    	}
    	}
    	System.out.println(expireMap.size());    	
    }

    /**
     * Test of execute method, of class com.devoteam.srit.sigloader.core.utils.ExpireHashMap
     */
    public void estExpireHashMap_putRemove() throws Exception {
    	Map<String, String> expireMap = new ExpireHashMap<String, String>(10000);    
    	    	
    	for (int j = 0; j < 1; j++) {
    		String str = new Long(j).toString();
	    	for (long i = 0; i < 1000000; i++) {
	        	String key = str + new Long(i).toString();
	    		expireMap.put(key, key);
	    		expireMap.remove(key);
	    		if ((i % 100000) == 0) 
	    		{
	    	    	System.out.println("iterator=" + key + ", size=" + expireMap.size());    			
	    		}
	    		// System.out.println(expireMap.size());
	    	}
    	}
    	System.out.println(expireMap.size());    	
    }

    /**
     * Test of execute method, of class com.devoteam.srit.sigloader.core.utils.ExpireHashMap
     */
    public void testExpireHashMap_put_remove() throws Exception {
    	Map<String, String> expireMap = new ExpireHashMap<String, String>(1);    
    	    	
    	for (int j = 0; j < 1; j++) {
    		String str = new Long(j).toString();
	    	for (long i = 0; i < 1000000; i++) {
	        	String key = str + new Long(i).toString();
	    		expireMap.put(key, key);
	    		if ((i % 100000) == 0) 
	    		{
	    	    	System.out.println("iterator=" + key + ", size=" + expireMap.size());    			
	    		}
	    		// System.out.println(expireMap.size());
	    	}
    	}
    	System.out.println(expireMap.size());
    	for (int j = 0; j < 1; j++) {
    		String str = new Long(j).toString();
	    	for (long i = 0; i < 1000000; i++) {
	        	String key = str + new Long(i).toString();
	        	expireMap.remove(key);
	    		if ((i % 100000) == 0) 
	    		{
	    	    	System.out.println("iterator=" + key + ", size=" + expireMap.size());    			
	    		}
	    		// System.out.println(expireMap.size());
	    	}
	    	System.out.println(expireMap.size());
    	}

    }

    private class TimestampHashMap<K, V> extends LinkedHashMap<K, V>
    {    

    	private long lifeTime;
    	
        public TimestampHashMap(int lifeTime) {
        	
        	super();
        	this.lifeTime = lifeTime;
        }
    	
        /**
         * Returns <tt>true</tt> if this map should remove its eldest entry.
         * This method is invoked by <tt>put</tt> and <tt>putAll</tt> after
         * inserting a new entry into the map.  It provides the implementer
         * with the opportunity to remove the eldest entry each time a new one
         * is added.  This is useful if the map represents a cache: it allows
         * the map to reduce memory consumption by deleting stale entries.
         *
         * <p>Sample use: this override will allow the map to grow up to 100
         * entries and then delete the eldest entry each time a new entry is
         * added, maintaining a steady state of 100 entries.
         * <pre>
         *     private static final int MAX_ENTRIES = 100;
         *
         *     protected boolean removeEldestEntry(Map.Entry eldest) {
         *        return size() > MAX_ENTRIES;
         *     }
         * </pre>
         *
         * <p>This method typically does not modify the map in any way,
         * instead allowing the map to modify itself as directed by its
         * return value.  It <i>is</i> permitted for this method to modify
         * the map directly, but if it does so, it <i>must</i> return
         * <tt>false</tt> (indicating that the map should not attempt any
         * further modification).  The effects of returning <tt>true</tt>
         * after modifying the map from within this method are unspecified.
         *
         * <p>This implementation merely returns <tt>false</tt> (so that this
         * map acts like a normal map - the eldest element is never removed).
         *
         * @param    eldest The least recently inserted entry in the map, or if 
         *           this is an access-ordered map, the least recently accessed
         *           entry.  This is the entry that will be removed it this
         *           method returns <tt>true</tt>.  If the map was empty prior
         *           to the <tt>put</tt> or <tt>putAll</tt> invocation resulting
         *           in this invocation, this will be the entry that was just
         *           inserted; in other words, if the map contains a single
         *           entry, the eldest entry is also the newest.
         * @return   <tt>true</tt> if the eldest entry should be removed
         *           from the map; <tt>false</t> if it should be retained.
         */
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        	GregorianCalendar gc = new GregorianCalendar();        	
        	return ((Long) eldest.getValue() + lifeTime ) < gc.getTimeInMillis();
        }
    }

}
