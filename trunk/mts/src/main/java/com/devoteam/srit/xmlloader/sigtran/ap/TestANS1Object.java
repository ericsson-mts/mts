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

package com.devoteam.srit.xmlloader.sigtran.ap;

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
    
        //String testFilename = args[0];
        //String packageName = "com.devoteam.srit.xmlloader.sigtran.ap.generated.map.";
        String packageName = "com.devoteam.srit.xmlloader.sigtran.ap.generated.map.";
        
        //String className = "TCMessage";
        //String className = "DialoguePDU";
        //String className = "Components";
        String className = "ProvideSubscriberInfoArg";
        //String className = "ForwardingInfo";
        //String className = "NoteMM_EventRes";
        //String className = "AnyTimeSubscriptionInterrogationRes";
        //String className = "ReportSM_DeliveryStatusArg";
        //String className = "UnauthorizedLCSClient_Param";
        //String className = "UpdateLocationRes";
        //String className = "ShortTermDenialParam";
        //String className = "MAP_Protocol";
        //String className = "UnknownSubscriberParam";
        //String className = "AnyTimeInterrogationArg";
        //String className = "LongForwardedToNumber";
        //String className = "UpdateLocationArg";
        //String className = "ReadyForSM_Arg";
        //String className = "AbsentSubscriberParam";

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
    	
		try 
		{
			Class classObj = Class.forName(packageName + className);
			Object obj = classObj.newInstance();
			ASNReferenceFinder.getInstance().findAndRemoveReferences(mapClasses, obj);
			testProcess(1, packageName, classObj);
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    			testProcess(i, packageName, classObject);
    		} 
    		catch (Exception e) 
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}

    	}		
    }
            
    public static void testProcess(int i, String packageName, Class classObj) throws Exception
    {
        String className = classObj.getSimpleName();

		// initialize the ASN1 object
		Object objectInit = classObj.newInstance();		
		ASNInitializer.getInstance().setValue(objectInit);
		
		// convert the ASN1 object into XML data
        String retInit = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retInit += "\n\n";
        retInit += "<AP>";
        retInit += ASNToXMLConverter.getInstance().toXML(null, objectInit, 0);
        retInit += "\n";
        retInit += "</AP>";
        

        System.out.print("Process class[" + i + "] = " + className + ".xml (" + retInit.length() + ") => ");
        
        // write XML data into a file
        File fileInit = new File("./asn1/" + className + ".xml");
        fileInit.delete();
        OutputStream out = new FileOutputStream(fileInit, false);
        Array array = new DefaultArray(retInit.getBytes());
        out.write(array.getBytes());
        out.close();

        // read XML data from file
        File fileRead = new File("./asn1/" + className + ".xml");
        if(!fileRead.exists()) fileRead.createNewFile();
        InputStream in = new FileInputStream(fileRead);
        SAXReader reader = new SAXReader(false);
        Document document = reader.read(in);	 
        Element root = document.getRootElement();
        //Element apElement = root.elements("AP");
        List<Element> elements = root.elements();
        if (elements.size() > 0)
        {
        	Element apElement = (Element) elements.get(0);
        }
        in.close();
	        
        // parse ASN1 object from XML file
        Class thisClass = Class.forName(packageName + className);
        Object objectXML = thisClass.newInstance();
        XMLToASNParser.getInstance().initObject(objectXML, root, packageName);

		// convert the ASN1 object ibnto XML data
        String retXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retXML += "\n\n";
        retXML += "<AP>";
        retXML += ASNToXMLConverter.getInstance().toXML(null, objectXML, 0);
        retXML += "\n";
        retXML += "</AP>";

        File fileXML = new File("./asn1/" + className + "_XML ");
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
    	IEncoder<Object> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoderMAP.encode(objectXML, outputStream);
        byte[] bytesMAP = outputStream.toByteArray();
        Array arrayMAP = new DefaultArray(bytesMAP);
        
        // 	decode ASN1 object from binary
		IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
	    InputStream inputStream = new ByteArrayInputStream(arrayMAP.getBytes());
	    Object objectBin = decoder.decode(inputStream, classObj);

		// convert the ASN1 object into XML data
        String retBin = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retBin += "\n\n";
        retBin += "<AP>";
        retBin += ASNToXMLConverter.getInstance().toXML(null, objectBin, 0);
        retBin += "\n";
        retBin += "</AP>";
        
        // write XML data into a file
        File fileBin = new File("./asn1/" + className + "_BINARY.xml");
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