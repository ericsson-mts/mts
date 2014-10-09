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

package com.devoteam.srit.xmlloader.asn1;

import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.TextExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.maps.HashMap;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.bn.CoderFactory;
import org.bn.IDecoder;
import org.bn.IEncoder;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author
 * fhenry
 */
public class TestANS1Object {

    static public void main(String... args) 
    {
        //
        // Handle arguments
        //
    	/*
        if (args.length <= 0) 
        {
            usage("At least one argument is required : the test file path");
        }
		*/
    	
        ExceptionHandlerSingleton.setInstance(new TextExceptionHandler());

        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());
        
        /*
         * Register the File logger provider
         */
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());
    
        String name = args[0];
        String dest = args[1];
        
        // create the directory
        File dir = new File(dest);
        dir.delete();
        try
        {
        	dir.mkdirs();
        }
        catch (Exception e)
        {
        }
        
        // case the user enters a class name
        if (!name.endsWith("."))
        {
        	String className = name;
        	int pos = className.lastIndexOf('.');
        	String packageName = className.substring(0, pos + 1);
        	
    		try 
    		{
    			Class classObj = Class.forName(className);
    			Object obj = classObj.newInstance();
    			//ASNReferenceFinder.getInstance().findAndRemoveReferences(mapClasses, obj);
    			testProcess(1, packageName, classObj, dest);
    		} 
    		catch (Exception e) 
    		{
    			// TODO Auto-generated catch block
    	        System.out.println("");
    			e.printStackTrace();
    		}
        }
        // case the user enters a package name
        else
        {
        	String packageName = name;        
       
			// inspect the classes for the given package
	    	List<Class> listClasses = ClassInspector.find(packageName);
	    	
	    	// build the hashmap to find the high level classes
	    	Map<String, Class> mapClasses = new HashMap<String, Class>();
	    	for (Class classObj : listClasses)
	    	{
	    		if (!classObj.isEnum() && !classObj.isMemberClass())
	            {
	    			mapClasses.put(classObj.getCanonicalName(), classObj);
	            }
	    	}
	    	
	    	// remove the reference into the hashmap
	    	for (Class classObj : listClasses)
	    	{
	    		try 
	    		{
		            if (!classObj.isEnum())
		            {
			    		Object object = classObj.newInstance();
			    		ASNReferenceFinder.getInstance().findAndRemoveReferences(mapClasses, object);
		            }
	       		} 
	    		catch (Exception e) 
	    		{
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    	}
	    	Collection<Class> collect = mapClasses.values();
	    	int i = 0;
	    	for (Class classObject : collect)
	    	{
	    		try 
	    		{	i++;
	    			testProcess(i, packageName, classObject, dest);
	    		} 
	    		catch (Exception e) 
	    		{
	    			// TODO Auto-generated catch block
	    	        System.out.println("");
	    			e.printStackTrace();
	    		}
	
	    	}
        }
    }
            
    public static void testProcess(int i, String packageName, Class classObj, String dest) throws Exception
    {
        String className = classObj.getSimpleName();

        //System.out.print("Process class[" + i + "] = " + className + ".xml (" + retInit.length() + ") => ");
        System.out.print("Process class[" + i + "] = " + className + ".xml => ");
        
		// initialize the ASN1 object
		Object objectInit = classObj.newInstance();
		BN_ASNMessage msgInit = new BN_ASNMessage(objectInit);
		ASNInitializer.getInstance().setValue(msgInit, objectInit);
		
		// convert the ASN1 object into XML data
        String retInit = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
        retInit += msgInit.toXML();

        // write XML data into a file
        File fileInit = new File(dest + className + ".xml");
        fileInit.delete();
        OutputStream out = new FileOutputStream(fileInit, false);
        Array array = new DefaultArray(retInit.getBytes());
        out.write(array.getBytes());
        out.close();

        // read XML data from file
        File fileRead = new File(dest + className + ".xml");
        if(!fileRead.exists()) fileRead.createNewFile();
        InputStream in = new FileInputStream(fileRead);
        SAXReader reader = new SAXReader(false);
        Document document = reader.read(in);	 
        Element root = document.getRootElement();
        List<Element> elements = root.elements();
        Element apElement = null;
        if (elements.size() > 0)
        {
        	apElement = (Element) elements.get(0);
        }
        in.close();
	        
        // parse ASN1 object from XML file
		BN_ASNMessage msgXML = new BN_ASNMessage(packageName + className);
        msgXML.parseFromXML(root);
        // Object objectXML = msgParseFromXML.getAsnObject(); 

		// convert the ASN1 object into XML data
        String retXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
        retXML += msgXML.toXML();

        File fileXML = new File(dest + className + "_XML ");
        fileXML.delete();

        // test with initial value
        if (!retInit.equals(retXML))
        {
        	System.out.print("KO : XML format,");
        	
            // write XML data into a file
            OutputStream out1 = new FileOutputStream(fileXML, false);
            Array array1 = new DefaultArray(retXML.getBytes());
            out1.write(array1.getBytes());
            out1.close();
        }
        
        // encode ASN1 object into binary
        Array arrayInit = msgInit.encode();
        
        // 	decode ASN1 object from binary
        BN_ASNMessage msgBin = new BN_ASNMessage(packageName + className);
        msgBin.decode(arrayInit);

		// convert the ASN1 object into XML data
        String retBin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n";
        retBin += msgBin.toXML();
        
        // write XML data into a file
        File fileBin = new File(dest + className + "_BINARY.xml");
        fileBin.delete();
        
        // test with initial value
        if (!retInit.equals(retBin))
        {
        	System.out.print("KO : BINARY.");
        	
	        OutputStream out2 = new FileOutputStream(fileBin, false);
	        Array array2 = new DefaultArray(retBin.getBytes());
	        out2.write(array2.getBytes());
	        out2.close();
        }
        
        System.out.println("");
    }

    static public void usage(String message) {
        System.out.println(message);
        System.out.println("Usage: startCmd <testFile>|<masterFile>\n"
                + "    -seq[uential]|-par[allel]|<testcaseName>\n"
                + "    -testplan\n"
                + "    [-param[eter]:<paramName>+<paramValue>]\n"
                + "    [-config[uration]:<configName>+<configValue>]\n"
                + "    [-level[Log]:ERROR=0|WARN=1|INFO=2|DEBUG=3]\n"
                + "    [-stor[ageLog]:disable=0|file=1]\n"
                + "    [-gen[Report]:false|true]\n"
                + "    [-show[Report]:false|true]\n");
        System.exit(10);
    }
}