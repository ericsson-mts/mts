/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.coding.q931;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.maps.LinkedHashMap;
import gp.utils.arrays.Integer08Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 *
 * @author indiaye
 */
public class ElementInformationQ931 {

    private int _id;
    private String _name;
    
    private LinkedHashMap<String, Field> _hashMapFields = new LinkedHashMap<String, Field>();

    public ElementInformationQ931(Element element, Dictionary dictionary) throws Exception {
        //si elem ds dico on prend dico sinon on envoi ce qu'il y  ads le fichier xml
        String idStr = element.attributeValue("identifier").trim();
        ElementInformationQ931 elemDico = null;
    	try 
    	{
    		byte[] idBytes = Utils.parseBinaryString(idStr);
            if (idBytes.length > 1)
            {
            	throw new ExecutionException("ISDN layer : Reading the element Id from XML file : value is too long " + idStr);
            }                
    		_id = idBytes[0] & 0xff;
    		if (dictionary != null)
    		{
    			elemDico = dictionary.getMapElementById().get(_id);
    		}
    	}
    	catch (Exception e) 
    	{
    		if (dictionary == null)
    		{
    			throw new ExecutionException("ISDN layer : The element identifier \"" + idStr + "\" is not valid : " + idStr);
    		}
    		elemDico = dictionary.getMapElementByName().get(idStr);    		
    		if (elemDico == null)
    		{
            	throw new ExecutionException("ISDN layer : The element \"" + idStr + "\" for the ISDN layer is not present in the dictionnary.");            	            	
            }        				
    		_id = elemDico.getId();
    	}        	    		
        if (elemDico != null)
        {
        	_name = elemDico.getName();
        }
        else
        {
        	_name = element.attributeValue("name");
        }
        List<Element> listField = element.elements("field");
        for (Iterator<Element> it = listField.iterator(); it.hasNext();) {
            Element elemField = it.next();
            String name = elemField.attributeValue("name");
            Field field = null;
            if (elemDico != null)
            {
            	field = elemDico.getHashMapFields().get(name); 
            }
            if (field == null)
            {
            	String type = elemField.attributeValue("type");
	            if (type.equalsIgnoreCase("integer")) {
	                field = new IntegerField(elemField, this, dictionary);
	
	            } else if (type.equalsIgnoreCase("boolean")) {
	
	                field = new BooleanField(elemField, this, dictionary);
	
	            } else if (type.equalsIgnoreCase("enumeration")) {
	                field = new EnumerationField(elemField, this, dictionary);
	
	            } else if (type.equalsIgnoreCase("string")) {
	                field = new StringField(elemField, this, dictionary);	
	            } else if (type.equalsIgnoreCase("binary")) {
	                field = new BinaryField(elemField, this, dictionary);
	
	            }else
	            {
	            	throw new ExecutionException("ISDN layer : The field type \"" + type + "\" is not supported : " + idStr);    
	            }
            }
            else
            {
            	// int length = Integer.parseInt(elemField.attributeValue("lengthBit"));
            	// field.setLength(length);
            }
            _hashMapFields.put(elemField.attributeValue("name"), field);
        }
       
    }

    public int getLengthElem() {
        int length = 0;
        for (Entry<String, Field> field : _hashMapFields.entrySet()) {
            length += field.getValue().getLength();
        }
        return length;
    }

    public LinkedHashMap<String, Field> getHashMapFields() {
        return _hashMapFields;
    }

    public int getId() {
        return _id;
    }

	public String getName() {
		return _name;
	}
}
