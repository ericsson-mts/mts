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
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.TextExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.core.utils.maps.HashMap;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 *
 * @author
 * fhenry
 */
public class TestANS1Object 
{

	// default java package 
	// FH modifprivate static String JAVA_PACKAGE = "com.devoteam.srit.xmlloader.sigtran.ap";
	private static String JAVA_PACKAGE = "com.devoteam.srit.xmlloader.sigtran.ap";
			
	// destination directory for resulting files
	private static String destDirectory = "../tutorial/asn1/ap/";	
	// maximum number of iterations
	private static int maxIterations = 5;
	// rules list
	//private static String listRules = "BER,DER,PER";
	private static String[] tabRules = {"XML", "BER", "DER", "PER"};
	
	// error counters
	private static int[] tabError = {0,0,0,0,0};

    static public void main(String... args) 
    {    	
        ExceptionHandlerSingleton.setInstance(new TextExceptionHandler());

        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());
        
        /*
         * Register the File logger provider
         */
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());
    
        // mandatory
        if (args.length <= 0)
        {
        	usage("no class name or package to process");
        }
        String name = args[0];
        // if the name does not start with '.' character then we add the default package before
        if (name.startsWith("."))
        {
        	name = JAVA_PACKAGE + name;
        }

        // default destination package directory
        String destPackage = "";
        int index1 = name.lastIndexOf(".");
        if (index1 > 0)
        {
        	int index2 = name.lastIndexOf(".", index1 - 1);
        	if (index2 > 0)
        	{
        		destPackage = name.substring(index2 + 1, index1);
        	}
        }
        
        // destination directory
        if (args.length >= 2)
        {	
        	destDirectory = args[1];
        }
        else
        {
        	destDirectory = destDirectory + destPackage;
        }
        destDirectory = destDirectory.replace('\\', '/');
        if (!destDirectory.endsWith("/"))
        {
        	destDirectory = destDirectory + "/"; 
        }
        
        // maximum number of iterations
        if (args.length >= 3)
        {	
        	maxIterations = Integer.parseInt(args[2]);
        }
        
        // rules list (, separator)
        if (args.length >= 4)
        {	
        	tabRules = args[3].split(",");
        }

        // create the directory
        File dir = new File(destDirectory);
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
    			Class<?> classObj = Class.forName(className);
    			Object obj = classObj.newInstance();
    			//ASNReferenceFinder.getInstance().findAndRemoveReferences(mapClasses, obj);
    			testProcess(0, packageName, classObj);
    		} 
    		catch (Exception e) 
    		{
    			// TODO Auto-generated catch block
    	        System.out.println(e);
    			e.printStackTrace();
    		}
        }
        // case the user enters a package name
        else
        {
        	String packageName = name;
        	
        	// dans les .jar le séparateur est / au lieu de .
        	//packageName = packageName.replaceAll("\\.", "/");        	
			// inspect the classes for the given package        	
	    	List<Class> listClasses = ClassInspector.find(packageName);
        	//packageName = packageName.replaceAll("/", "\\.");        	
	    	
	    	// build the hashmap to find the high level classes
	    	Map<String, Class> mapClasses = new HashMap<String, Class>();
	    	for (Class<?> classObj : listClasses)
	    	{
	    		if (!classObj.isEnum() && !classObj.isMemberClass())
	            {
	    			mapClasses.put(classObj.getCanonicalName(), classObj);
	            }
	    	}
	    	
	    	// remove the reference into the hashmap
	    	for (Class<?> classObj : listClasses)
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
	    	int index = 0;
	    	long beginTT = new GregorianCalendar().getTimeInMillis();
	    	for (Class<?> classObject : collect)
	    	{
	    		try 
	    		{	index++;
	    			testProcess(index, packageName, classObject);
	    		} 
	    		catch (Exception e) 
	    		{
	    			// TODO Auto-generated catch block
	    	        System.out.println(e);
	    			e.printStackTrace();
	    		}
	
	    	}
	    	
	    	System.out.print("ERROR");
	    	for (int i = 0; i < tabRules.length; i++)
	        {
	    		System.out.print(" " + tabRules[i] + "=" + tabError[i] + "/" + collect.size() + ", ");
	        }
	    	System.out.println();
	    	
	        long endTT = new GregorianCalendar().getTimeInMillis();
	        float duration = ((float)(endTT - beginTT)) / 1000;
	        int iter = 2 * maxIterations * index;
	        float flow =  iter / duration;  
	    	System.out.print(" " +  iter + " iterations, ");
	        System.out.print("duration = " + duration + " sec, ");
	        System.out.print("flow = " + flow + " iter/sec.");
	        System.out.println();
        }
        
        System.exit(0);
    }

    public static void testProcess(int classIndex, String packageName, Class<?> classObj)
    {
        String className = classObj.getSimpleName();
        System.out.print("Process class[" + classIndex + "] = " + className + ".xml => ");
        
        long beginTT = new GregorianCalendar().getTimeInMillis();
        String dictionaryFile = null;
        if (packageName.endsWith(".map."))
        {
        	dictionaryFile = "map/dictionary_MAP.xml";
        }
        else if (packageName.endsWith(".tcap."))
        {
        	dictionaryFile = "tcap/dictionary_TCAP.xml";
        }
        else if (packageName.endsWith(".cap."))
        {
        	dictionaryFile = "cap/dictionary_CAP.xml";
        }
        else if (packageName.endsWith(".S1AP."))
        {
        	dictionaryFile = "S1AP/dictionary_S1AP.xml";
        }
        
        
        boolean error = false;
        for (int i = 0; i < tabRules.length; i++)
        {
        	try	
	        {
		    	if (tabRules[i].equalsIgnoreCase("XML")) 
		    	{
			    	if (!testProcessAllIndexXML(dictionaryFile, classObj))
			    	{
			    		System.out.print("ERROR XML");
			    		tabError[i] ++;
			    		error = true;
			    	}
		    	}
		    	else if (!testProcessAllIndexBIN(dictionaryFile, classObj, tabRules[i]))
		    	{
		    		tabError[i] ++;
		    		error = true;
		    	}
			} 
			catch (Exception e) 
			{
		        System.out.println(e);
				e.printStackTrace();
				Utils.pauseMilliseconds(100);
				tabError[i] ++;
	    		error = true;
			}
    	}
    
    	if (!error)
    	{
            System.out.print("OK");
    	}
    	else
    	{
    		System.out.println("KO : FAILED");
    	}
    	
        long endTT = new GregorianCalendar().getTimeInMillis();
        float duration = ((float)(endTT - beginTT)) / 1000;
        int iter = 2 * maxIterations;
        float flow = iter / duration;
        System.out.print("                              " + iter + ", ");
        System.out.print(duration + " s, ");
        System.out.print(flow + " /s.");

        System.out.println("");
    }

    public static boolean testProcessAllIndexXML(String dictionaryFile, Class<?> classObj) throws Exception
    {          
    	boolean result = true;
    	for (int i = 0; i <= maxIterations; i++)
    	{
    		if (!testProcessXML(i, dictionaryFile, classObj))
    		{
    			System.out.print("XML");
    			result = false;
    		}
    	}
    	return result;
    }

    public static boolean testProcessXML(int index, String dictionaryFile, Class<?> classObj) throws Exception
    {                  
		// initialize the ASN1 object
		Object objectInit = classObj.newInstance();
		BN_ASNMessage msgInit = new BN_ASNMessage(dictionaryFile, objectInit);
		ASNInitializer.getInstance().initValue(-1, index, "", msgInit, null, null, objectInit, null);
		
		// convert the ASN1 object into XML data
        String retInit = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n\n";
        retInit += msgInit.toXML();
        
        // write XML data into a file
        String simpleClassName = classObj.getSimpleName();
        //String fileNameInit = destDirectory + simpleClassName + "_" + index;
        String fileNameInit = destDirectory + simpleClassName;
        File fileInit = new File(fileNameInit + ".xml");
        //fileInit.delete();
        OutputStream out = new FileOutputStream(fileInit, false);
        Array array = new DefaultArray(retInit.getBytes());
        out.write(array.getBytes());
        out.close();
        
        // read XML data from file
        File fileRead = new File(fileNameInit + ".xml");
        if(!fileRead.exists()) fileRead.createNewFile();
        InputStream in = new FileInputStream(fileRead);
        SAXReader reader = new SAXReader(false);
        reader.setEntityResolver(new XMLLoaderEntityResolver());
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
		// initialize the ASN1 object
		Object objectXML = classObj.newInstance();
		BN_ASNMessage msgXML = new BN_ASNMessage(dictionaryFile, objectXML);
		String className = classObj.getCanonicalName();
        msgXML.parseFromXML(root, className);

		// convert the ASN1 object into XML data
        String retXML = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n\n";
        retXML += msgXML.toXML();

        File fileXML = new File(destDirectory + simpleClassName + "_difference.xml");
        //fileXML.delete();

        // test with initial value
        if (!retInit.equals(retXML))
        {	
            // write XML data into a file
            OutputStream out1 = new FileOutputStream(fileXML, false);
            Array array1 = new DefaultArray(retXML.getBytes());
            out1.write(array1.getBytes());
            out1.close();
            return false;
        }
        
        return true;
    }

    public static boolean testProcessAllIndexBIN(String dictionaryFile, Class<?> classObj, String rule) throws Exception
    {          
    	boolean result = true;
    	for (int i = 0; i <= maxIterations; i++)
    	{
    		if (!testProcessBIN(i, dictionaryFile, classObj, rule))
    		{
    			System.out.print(rule + "(" + i + ") ");
    			result = false;
    		}
    	}
    	return result;
    }

    public static boolean testProcessBIN(int index, String dictionaryFile, Class<?> classObj, String rule) throws Exception
    {               
		// initialize the ASN1 object
		Object objectInit = classObj.newInstance();
		BN_ASNMessage msgInit = new BN_ASNMessage(dictionaryFile, objectInit);
		ASNInitializer.getInstance().initValue(index, index, "", msgInit, null, null, objectInit, null);
		
		// convert the ASN1 object into XML data
        String retInit = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n\n";
        retInit += msgInit.toXML();
                
        // encode ASN1 object into binary
        Array arrayInit = msgInit.encode(rule);
        
        // 	decode ASN1 object from binary
		// initialize the ASN1 object
        String className = classObj.getCanonicalName();
		Object objectBin = classObj.newInstance();
        BN_ASNMessage msgBin = new BN_ASNMessage(dictionaryFile, objectBin);
        msgBin.decode(arrayInit, className, rule);

		// convert the ASN1 object into XML data
        String retBin = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n\n";
        retBin += msgBin.toXML();
        
        String simpleClassName = classObj.getSimpleName();
        String fileNameInit = destDirectory + simpleClassName + "_"  + rule + "_" + index;
        File fileInit = new File(fileNameInit + ".xml");
        //fileInit.delete();
        String fileNameDiff = fileNameInit + "_difference";
        File fileDiff = new File(fileNameDiff + ".xml");
        //fileDiff.delete();
        
        // test with initial value
        if (!retInit.equals(retBin))
        {
            // write XML data into a file
            OutputStream out = new FileOutputStream(fileInit, false);
            Array array = new DefaultArray(retInit.getBytes());
            out.write(array.getBytes());
            out.close();

	        OutputStream out2 = new FileOutputStream(fileDiff, false);
	        Array array2 = new DefaultArray(retBin.getBytes());
	        out2.write(array2.getBytes());
	        out2.close();
	        return false;
        }
        
        return true;
    }

    static public void usage(String message) {
        System.out.println(message);
        System.out.println("Generate the template xml files for a given class name or a java package.\n"
        		+ "Usage: asn1_templater.[bat|sh] <className> | <package.>\n"
                + "    [outputDirectory] [numberIteration] [listRules]\n");
        System.exit(10);
    }
}