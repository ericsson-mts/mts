/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.Integer08Array;
import gp.utils.arrays.SupArray;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class MessageQ931 {
   
    public static HashMap<String, Dictionary> dictionaries = new  HashMap<String, Dictionary>();
	
	private String syntax;
	
    private Dictionary dictionary;
    	
    private HeaderQ931 headerQ931;
    
    private LinkedHashMap<Integer, ElementInformationQ931V> hashElementInformationQ931Vs;
    
    
     public MessageQ931(Element root) throws Exception {
        this.syntax = root.attributeValue("syntax");
        initDictionary(syntax);       
        
        headerQ931 = new HeaderQ931(root.element("header"), dictionary);
        hashElementInformationQ931Vs = new LinkedHashMap<Integer, ElementInformationQ931V>();
        List<Element> elementsInf = root.elements("element");
        ElementInformationQ931 elem = null;
        ElementInformationQ931V elemV = null;
        for (Element element : elementsInf) {
            List<Element> listField = element.elements("field");
            elem = new ElementInformationQ931(element, dictionaries.get(this.syntax));
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
                elemV = new ElementInformationQ931V(array, false, false, elem);

            }
            //boucle pour setter tous les field de elemV
            int offset = 0;
            for (Iterator<Element> it = listField.iterator(); it.hasNext();) {
                Element element1 = it.next();
                Field field = elem.getHashMapFields().get(element1.attributeValue("name"));
                if (field != null) {
                    field.setValue(element1.attributeValue("value"), offset, elemV);
                    offset += field.getLength(); 
                }
                else {
                    throw new ExecutionException("The field " + element1.attributeValue("name") + " is not found in element : " + elemV.getElementInformation().getName() + ":" + elemV.getId());
                }
            }
            this.hashElementInformationQ931Vs.put(elemV.getId(), elemV);

        }
    }

    public MessageQ931(Array data, String syntax) throws Exception {
    	this.syntax = syntax;
        initDictionary(syntax);
        
        headerQ931 = new HeaderQ931(data, syntax, this.dictionary);
        
        hashElementInformationQ931Vs = new LinkedHashMap<Integer, ElementInformationQ931V>();
        int offset = headerQ931.getLength();
        ElementInformationQ931V elem = null;
        while (offset < data.length) {
            int id = new Integer08Array(data.subArray(offset, 1)).getValue();
            ElementInformationQ931 elemInfo = dictionaries.get(syntax).getMapElementById().get(id);
            boolean bigLength = false; 
            /* FH remove because not well decoded with Wireshark
            bigLength = id == 126;
            
            if (elemInfo == null) { //gerer le cas ou l'element n'est pas connu du dictionnaire
                elem = new ElementInformationQ931V(data.subArray(offset), bigLength, true, null);
            }
            else
            */ 
            {
                elem = new ElementInformationQ931V(data.subArray(offset), bigLength, true, elemInfo);
            }

            if (elem.getArray().length > 0) {
                offset += elem.getArray().length;
            }
            else {
                offset += 1;
            }
            hashElementInformationQ931Vs.put(id, elem);
        }

    }

    /** Get a parameter from the message */
    public void getParameter(Parameter var, String[] params, String path) throws Exception {
        if (params.length > 2 && params[1].equalsIgnoreCase("header")) 
        {
            this.headerQ931.getParameter(var, params[2]);
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
        		ElementInformationQ931 elem = dictionaries.get(this.syntax).getMapElementByName().get(params[2]);
        		if (elem != null)
        		{
        			id = elem.getId();
        		}
        	}        	
        	ElementInformationQ931V elemV = hashElementInformationQ931Vs.get(id);
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
        array.addLast(headerQ931.getValue());
        for (Entry<Integer, ElementInformationQ931V> entry : hashElementInformationQ931Vs.entrySet()) {
            array.addLast(entry.getValue().getArray());
        }
        return array;
    }

    public String getType() {
	    return headerQ931.getType();
    }
    
    @Override
    public String toString() {
        StringBuilder messageToString = new StringBuilder();
        messageToString.append("<ISDN>");
        messageToString.append(headerQ931.toString());

        for (Entry<Integer, ElementInformationQ931V> entry : hashElementInformationQ931Vs.entrySet()) {

            messageToString.append(entry.getValue().toString());
        }
        messageToString.append("</ISDN>");
        return messageToString.toString();

    }

    public HeaderQ931 getHeaderQ931() {
        return headerQ931;
    }

    public ElementInformationQ931V getElementInformationQ931s(int id) {
        return hashElementInformationQ931Vs.get(id);

    }

    public int getLength() {

        int msglength = 0;
        msglength = headerQ931.getValue().length;
        for (Entry<Integer, ElementInformationQ931V> entry : hashElementInformationQ931Vs.entrySet()) {

            msglength += entry.getValue().getArray().length;

        }
        return msglength;
    }
    
    public void initDictionary(String syntax) throws Exception {
    	this.dictionary = MessageQ931.dictionaries.get(syntax);
    	if (this.dictionary == null)
    	{
	        XMLDoc xml = new XMLDoc();
	        xml.setXMLFile(new URI(syntax));
	        xml.parse();
	        Element rootDico = xml.getDocument().getRootElement();
	        this.dictionary = new Dictionary(rootDico);
	        MessageQ931.dictionaries.put(syntax, dictionary);
    	}
    }
}
