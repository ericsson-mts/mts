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
        String packageName = "com.devoteam.srit.xmlloader.sigtran.ap.generated.map.";
        //String className = "ForwardingInfo";
        //String className = "Component";
        //String className = "NoteMM_EventRes";
        //String className = "AnyTimeSubscriptionInterrogationRes";
        //String className = "ReportSM_DeliveryStatusArg";
        String className = "UnauthorizedLCSClient_Param";

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
			testProcess(packageName, classObj);
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
    	for (Class classObject : collect)
    	{
    		try 
    		{
    			testProcess(packageName, classObject);
    		} 
    		catch (Exception e) 
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}

    	}		
    }
            
    public static void testProcess(String packageName, Class classObj) throws Exception
    {
        String className = classObj.getSimpleName();
        System.out.println("class = " + className);

		// initialize the ASN1 object
		Object objectSet = classObj.newInstance();		
		ASNInitializer.getInstance().setValue(objectSet);
		
		// convert the ASN1 object into XML data
        String retInit = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retInit += "\n\n";
        retInit += "<AP>";
        retInit += ASNToXMLConverter.getInstance().toXML(null, objectSet, 0);
        retInit += "\n";
        retInit += "</AP>";
        
        // write XML data into a file
        File fileWrite = new File("./asn1/" + className + ".xml");
        if(!fileWrite.exists()) fileWrite.createNewFile();
        OutputStream out = new FileOutputStream(fileWrite, false);
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
        Element apElement = (Element) elements.get(0); 
        in.close();
	        
        // parse ASN1 object from XML file
        Class thisClass = Class.forName(packageName + className);
        Object objectRead = thisClass.newInstance();
        XMLToASNParser.getInstance().initObject(objectRead, root, packageName);

		// convert the ASN1 object ibnto XML data
        String retXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retXML += "\n\n";
        retXML += "<AP>";
        retXML += ASNToXMLConverter.getInstance().toXML(null, objectRead, 0);
        retXML += "\n";
        retXML += "</AP>";
        
        // test with initial value
        if (!retInit.equals(retXML))
        {
        	System.out.println("KO : problem after encodind / decoding to XML format.");
        }

        // write XML data into a file
        File fileWrite1 = new File("./asn1/" + className + "XML.xml");
        if(!fileWrite1.exists()) fileWrite1.createNewFile();
        OutputStream out1 = new FileOutputStream(fileWrite1, false);
        Array array1 = new DefaultArray(retXML.getBytes());
        out1.write(array1.getBytes());
        out1.close();
        
        // encode ASN1 object into binary
    	IEncoder<Object> encoderMAP = CoderFactory.getInstance().newEncoder("BER");
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoderMAP.encode(objectRead, outputStream);
        byte[] bytesMAP = outputStream.toByteArray();
        Array arrayMAP = new DefaultArray(bytesMAP);

        
        Array arrayMAP1 = new DefaultArray(Utils.parseBinaryString("h302c040a04080001020304050607040a040800010203040506070a010080010ba102a1000500050084010085010b"));
        //Array arrayMAP = new DefaultArray(Utils.parseBinaryString("h302C040A04080001020304050607040A040800010203040506070A010080010BA102A1008200830084010085010B"));
        // 	decode ASN1 object from binary
        Object objectDecoded =  null;
		IDecoder decoder = CoderFactory.getInstance().newDecoder("BER");
	    InputStream inputStream = new ByteArrayInputStream(arrayMAP.getBytes());
	    objectDecoded = decoder.decode(inputStream, classObj);

		// convert the ASN1 object into XML data
        String retBinary = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        retBinary += "\n\n";
        retBinary += "<AP>";
        retBinary += ASNToXMLConverter.getInstance().toXML(null, objectDecoded, 0);
        retBinary += "\n";
        retBinary += "</AP>";
        
        // write XML data into a file
        File file = new File("./asn1/" + className + "Binary.xml");
        if(!file.exists()) file.createNewFile();
        OutputStream out2 = new FileOutputStream(file, false);
        Array array2 = new DefaultArray(retBinary.getBytes());
        out2.write(array2.getBytes());
        out2.close();

        // test with initial value
        if (!retInit.equals(retBinary))
        {
        	System.out.println("KO : problem after encodind / decoding to binary data.");
        }

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