/*
 * StatKeyTest.java
 * JUnit based test
 *
 * Created on 4 février 2008, 11:32
 */

package com.devoteam.srit.xmlloader.core.newstats;

import junit.framework.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author mjagodzinski
 */
public class StatKeyTest extends TestCase {
    
    public StatKeyTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        
    }

    protected void tearDown() throws Exception {
    }
    
    public void testStatKeyConstructor(){
        StatKey statKey = null;
        String[] attrArray =null;
        
        System.out.println("Testing three element long path");
        statKey = new StatKey("/aa/bb/cc");
        attrArray = statKey.getAllAttributes();
        assertEquals("aa",attrArray[0]);
        assertEquals("bb",attrArray[1]);
        assertEquals("cc",attrArray[2]);
        assertEquals(3,attrArray.length);
        
        System.out.println("Testing / path");
        statKey = new StatKey("/");
        attrArray = statKey.getAllAttributes();
        assertEquals("",attrArray[0]);
        assertEquals(1,attrArray.length);
        
        System.out.println("Testing a path with an ending slash");
        statKey = new StatKey("/aa/bb/cc/");
        attrArray = statKey.getAllAttributes();
        assertEquals("aa",attrArray[0]);
        assertEquals("bb",attrArray[1]);
        assertEquals("cc",attrArray[2]);
        assertEquals(3,attrArray.length);

        
    }
    
    public void testCompareTo() throws Exception{
    	StatKey s1,s2;
    	s1 = new StatKey("protocol","sip");
    	s2 = new StatKey("protocol","aaa");
  		assertTrue(s1.compareTo(s2) >0);
  		
    	s1 = new StatKey("protocol","sip","sendInvite");
    	s2 = new StatKey("protocol","aaa");
  		assertTrue(s1.compareTo(s2) >0);
  		
    	s1 = new StatKey("protocol","sip");
    	s2 = new StatKey("protocol","sip");
  		assertTrue(s1.compareTo(s2) ==0);

    	s1 = new StatKey("protocol","sip","sendInvite");
    	s2 = new StatKey("protocol","aaa");
  		assertTrue(s1.compareTo(s2) >0);
  		
    	s1 = new StatKey("protocol","sip");
    	s2 = new StatKey("protocol","aaa","sendmar");
  		assertTrue(s1.compareTo(s2) >0);

  		s1 = new StatKey("protocol","aaa");
    	s2 = new StatKey("protocol","bbb");
  		assertTrue(s1.compareTo(s2) <0);


  		
    }
    public void testHashCodeCalcul(){
        StatKey statKey1 = new StatKey("aaa","bbb","ccc");
        StatKey statKey2 = new StatKey("/aaa/bbb/ccc");
        assertEquals(statKey1.hashCode(),statKey2.hashCode());
        assertEquals("/aaa/bbb/ccc".hashCode(),statKey1.hashCode());
        
    }
 
}

