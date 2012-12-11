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

package com.devoteam.srit.xmlloader.gtpp.data;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.coding.q931.Dictionary;
import com.devoteam.srit.xmlloader.core.coding.q931.ElementQ931;
import com.devoteam.srit.xmlloader.core.coding.q931.Field;
import com.devoteam.srit.xmlloader.core.coding.q931.HeaderAbstract;
import com.devoteam.srit.xmlloader.core.coding.q931.XMLDoc;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;


/**
 *
 * @author Fabien Henry
 */
public class MessageGTP {
   
    public static HashMap<String, Dictionary> dictionaries = new  HashMap<String, Dictionary>();
	
	private String syntax;
	
    private Dictionary dictionary;
    	
    private HeaderAbstract header;
    
    private LinkedHashMap<Integer, ElementQ931> hashElementInformationQ931Vs;
    
    public MessageGTP()
    {
    	
    }
    
     public MessageGTP(Element root) throws Exception {              
        Element elementHeader;
        elementHeader = root.element("headerPrime");
        if (elementHeader == null)
        {
	        // this.header = new GtpHeaderPrime();
	        // initDictionary(header.getSyntax()); 
            // this.header.parseFromXML(elementHeader, dictionary);
        }
        elementHeader = root.element("headerV1");
        if (elementHeader!= null)
        {
	        this.header = new HeaderGTPV1();
	        this.syntax = header.getSyntax();
	        initDictionary(this.syntax); 
	        this.header.parseFromXML(elementHeader, dictionary);
        }
        elementHeader = root.element("headerV2");
        if (elementHeader!= null)
        {
	        this.header = new HeaderGTPV2();
	        this.syntax = header.getSyntax();
	        initDictionary(this.syntax); 
	        this.header.parseFromXML(elementHeader, dictionary);
        }        
        
        hashElementInformationQ931Vs = new LinkedHashMap<Integer, ElementQ931>();
        List<Element> elementsInf = root.elements("element");
        ElementQ931 elem = null;
        for (Element element : elementsInf) {
            List<Element> listField = element.elements("field");
            elem = new ElementQ931(element, dictionaries.get(this.syntax));
            // TODO améliorer pour prendre en compte la valeur en binaire, décimal, hexa...            
            /* FH remove because not well decoded with Wireshark  
            if (elem.getId() == 126) {

                Array array = new DefaultArray(elem.getLengthElem() / 8 + 3);
                elemV = new ElementInformationQ931V(array, true, false, elem);

            }
            else
            */ 
            {
                Array array = new DefaultArray(elem.getLengthElem() / 8 + 2);
                elem.decodeFromArray(array, false, false);

            }
            //boucle pour setter tous les field de elemV
            int offset = 0;
            for (Iterator<Element> it = listField.iterator(); it.hasNext();) 
            {
                Element element1 = it.next();
                Field field = elem.getHashMapFields().get(element1.attributeValue("name"));
                if (field != null) 
                {
                    Array result = field.setValue(element1.attributeValue("value"), offset, elem.getFieldsArray());
                    if (result !=null)
                    {
                    	elem.setFields(result);
                    }
                    	
                    offset += field.getLength(); 
                }
                else 
                {
                    throw new ExecutionException("The field " + element1.attributeValue("name") + " is not found in element : " + elem.getName() + ":" + elem.getId());
                }
            }
            this.hashElementInformationQ931Vs.put(elem.getId(), elem);

        }
    }

     public void parseFromStream(InputStream inputStream) throws Exception
     {
         byte[] flag = new byte[1];
         //read the header
         int nbCharRead= inputStream.read(flag, 0, 1);
     	if(nbCharRead == -1){
     		throw new Exception("End of stream detected");
     	}
     	else if (nbCharRead < 1) {
             throw new Exception("Not enough char read");
         }
         
         DefaultArray flagArray = new DefaultArray(flag);
         // int messageType = flagArray.getBits(3, 1);         
         int version = flagArray.getBits(0, 3);
         
         if (version == 0)
         {
         	// header = new GtpHeaderPrime(flagArray);
         }
         else if (version == 1)
         {
        	 header = new HeaderGTPV1(flagArray);
         }
         else if (version == 2)
         {
        	 header = new HeaderGTPV2(flagArray); 
         }
         
	     this.syntax = header.getSyntax();
         initDictionary(syntax);

         header.decodeFromStream(inputStream, dictionary);
                
         int msgLength = header.getLength(); 

         byte[] fieldBuffer = new byte[msgLength];
         //read the staying message's data
         nbCharRead = inputStream.read(fieldBuffer, 0, msgLength);
         if(nbCharRead == -1)
            throw new Exception("End of stream detected");
         else if(nbCharRead < msgLength)
            throw new Exception("Not enough char read");
 		Array fieldArrayTag = new DefaultArray(fieldBuffer);
              	
     	parseFieldFromArray(fieldArrayTag);
     }

    private void parseFieldFromArray(Array data) throws Exception {
        hashElementInformationQ931Vs = new LinkedHashMap<Integer, ElementQ931>();
        int offset = header.getLength();
        while (offset < data.length) {
            int id = new Integer08Array(data.subArray(offset, 1)).getValue();
            ElementQ931 elemInfo = dictionaries.get(syntax).getMapElementById().get(id);
            boolean bigLength = false; 
            /* FH remove because not well decoded with Wireshark
            bigLength = id == 126;
            
            if (elemInfo == null) { //gerer le cas ou l'element n'est pas connu du dictionnaire
                elem = new ElementInformationQ931V(data.subArray(offset), bigLength, true, null);
            }
            else
            */ 
            {
                elemInfo.decodeFromArray(data.subArray(offset), bigLength, true);
            }

            if (elemInfo.encodeToArray().length > 0) {
                offset += elemInfo.encodeToArray().length;
            }
            else {
                offset += 1;
            }
            hashElementInformationQ931Vs.put(id, elemInfo);
        }

    }

    /** Get a parameter from the message */
    public void getParameter(Parameter var, String[] params, String path) throws Exception {
        if (params.length > 2 && params[1].equalsIgnoreCase("header")) 
        {
            this.header.getParameter(var, params[2]);
        }
        else if ((params[1].equalsIgnoreCase("element") && params.length > 2)) 
        {
        	int id = 0;
        	try 
        	{
                byte[] idBytes = Utils.parseBinaryString(params[2]);
                if (idBytes.length > 1)
                {
                	throw new ExecutionException("Reading the element Id for path keyword : value is too long " + params[2]);
                }                
                id = idBytes[0] & 0xff;
        	}
        	catch (Exception e) 
        	{
        		ElementQ931 elem = dictionaries.get(this.syntax).getMapElementByName().get(params[2]);
        		if (elem != null)
        		{
        			id = elem.getId();
        		}
        	}        	
        	ElementQ931 elemV = hashElementInformationQ931Vs.get(id);
        	if (elemV != null)
        	{
        		elemV.getParameter(var, params, path);
        	}
        }
        else
        {
           	Parameter.throwBadPathKeywordException(path);
        }
    }

    public Array getValue() {
        SupArray array = new SupArray();
        for (Entry<Integer, ElementQ931> entry : hashElementInformationQ931Vs.entrySet()) {
            array.addLast(entry.getValue().encodeToArray());
        }
        header.setLength(array.length);
        array.addFirst(header.encodeToArray());
        return array;
    }

    public String getType() {
	    return header.getType();
    }
    
    @Override
    public String toString() {
        StringBuilder messageToString = new StringBuilder();
        messageToString.append(header.toString());

        for (Entry<Integer, ElementQ931> entry : hashElementInformationQ931Vs.entrySet()) {

            messageToString.append(entry.getValue().toString());
        }
        return messageToString.toString();

    }

    public HeaderAbstract getHeader() {
        return header;
    }

    public ElementQ931 getElementInformationQ931s(int id) {
        return hashElementInformationQ931Vs.get(id);

    }

    public int getLength() {

        int msglength = 0;
        msglength = header.encodeToArray().length;
        for (Entry<Integer, ElementQ931> entry : hashElementInformationQ931Vs.entrySet()) {

            msglength += entry.getValue().encodeToArray().length;

        }
        return msglength;
    }
    
    public void initDictionary(String syntax) throws Exception {
    	this.dictionary = MessageGTP.dictionaries.get(syntax);
    	if (this.dictionary == null)
    	{
	        XMLDoc xml = new XMLDoc();
	        String file = "../conf/gtpp/dictionary_" + header.getSyntax() + ".xml";
	        xml.setXMLFile(new URI(file));
	        xml.parse();
	        Element rootDico = xml.getDocument().getRootElement();
	        this.dictionary = new Dictionary(rootDico);
	        MessageGTP.dictionaries.put(syntax, dictionary);
    	}
    }
}
