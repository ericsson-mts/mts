package com.devoteam.srit.xmlloader.importsipp;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


import org.xml.sax.SAXException;

@SuppressWarnings("rawtypes")
public class ImportSipP {
		static String filename = "branchc";
		static String filetype = ".xml";

	public static void main(String argv[]) throws DocumentException, ParserConfigurationException, SAXException {
		
		ArrayList<Element> nodes = new ArrayList<Element>(); 
		 try {
			 	//Get the source xml file, and parse it using SAX parser */
			 	String filepath = filename+filetype; 
			 	SAXReader reader = new SAXReader();
			 	Document source_doc = reader.read("../mts/src/main/tutorial/importsipp/"+filepath);
			 		
			 	//Create the resulting xml file, with the root element 'scenario'
			 	Document result_doc = DocumentHelper.createDocument();
			 	Element rootElement = result_doc.addElement("scenario");
			 	
			 	//Get the root of the source xml file */
			 	Element root = source_doc.getRootElement(); 
			 	
			 	//Remove all attributes of the root element (scenario)
				for ( Iterator i = root.attributeIterator(); i.hasNext(); ) {
			            Attribute attribute = (Attribute) i.next();
			            root.remove(attribute);
				  }
				//Add the global parameters from the global template xml file
				addGlobalNode(result_doc,rootElement);
				
				//Run through the elements (nodes) of the source xml file 
				for (Iterator i = root.elementIterator(); i.hasNext();) 
				{
		            Element element = (Element) i.next();
					String nodename = element.getName();
					
					//If the current node is a 'recv' node
					if(nodename.equals("recv"))
					{	//If it contains the 'next' & 'optional' attributes
						if(element.attributes().toString().contains("optional")&& element.attributes().toString().contains("next")) 
						{	
							//We add the current node to the list of nodes we already created
							nodes.add(element);
						}
						//If the recv node does not contain the 'optional' attribute, then it's the last
						//recv node after sequence of recv nodes
						else if(!element.attributes().toString().contains("optional"))
						{	
							//We add this node to the list of nodes
							nodes.add(element);
							//We apply the normal template of the recv
							addNode(nodes, rootElement, result_doc,nodename);
							//We apply the if_recv template
							addNode2(nodes, rootElement, result_doc,"if_"+nodename);
							//We clear the saved nodes list
							nodes.clear();
						}
					}
					//If the current node is NOT a 'recv' node
					else
					{	//We add the current node to the nodes list
						nodes.add(element);
						//We apply the corresponding template file
						addNode(nodes, rootElement, result_doc,nodename);
						//We clear the saved nodes list
						nodes.clear();
					}
				}
		
				//Function to write the result, to the resulting xml file
				rightFinalResult(result_doc);
				
				//Function to replace strings in a file
				replaceInFile(filename+"_mts.xml", "&lt;", "<");
				replaceInFile(filename+"_mts.xml", "&gt;", ">");
				replaceInFile(filename+"_mts.xml", "&#13;", "");
			
				//Write 'DONE' on the system out
				System.out.println("Done");
			
		}
		 catch (IOException ioe) {
			ioe.printStackTrace();
		   }
}
	//Function that writes something to an xml file
	public static void rightFinalResult(Document document) throws IOException
	{	
		 OutputFormat format = OutputFormat.createPrettyPrint();
		 //We define the result xml file name
		 XMLWriter writer = new XMLWriter(new FileWriter(filename+"_mts.xml" ), format); 
		 writer.write(document);
	     writer.close();
	}
	
	//Function that replaces a String by another one in a file 
	public static void replaceInFile(String filename, String from, String to) throws IOException {
		try{
			File file = new File(filename);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "", oldtext = "";
		    while((line = reader.readLine()) != null)
		    {
		        	oldtext += line + "\r\n";
		    }
		    reader.close();
		    //Replace a word in a file
		    String newtext = oldtext.replaceAll(from, to);   
		    FileWriter writer = new FileWriter(filename);
		    writer.write(newtext);
		    writer.close();
		}
		       
		catch (IOException ioe){
		            ioe.printStackTrace();
		        }
	 }
	
	//Function that adds a node to the resulting file
	/*It takes an array list of Nodes, the root of the result document, the result document and
	  the template file name */
	public static void addNode(ArrayList<Element> sippNode, Element result_doc_root, Document result_doc, String template_file) throws SAXException, IOException, DocumentException
	{		
		//Parsing the corresponding template file
	 	SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/tutorial/importsipp/Templates/"+template_file+"_template.xml");
		Element template_root = template.getRootElement(); 
		//Run through all template elements
		for (Iterator i = template_root.elementIterator();i.hasNext();) 
		{
			Element template_element = (Element) i.next();
			/*Test each element. If it's a 'parameter', the parameter name should exist in the 'send'
			  CDATA section to be added to the file. If not, this parameter wont be added*/
        	if(template_element.getName().equals("parameter"))
        	{
        		if(sippNode.get(sippNode.size()-1).getStringValue().contains(template_element.attribute("name").getValue()))
        		{	
        			//Execute the xPath function which is responsible of adding elements and attributes 
        			xPath(result_doc,template_element,sippNode,result_doc_root);
        		}
        	}
        	else
        		xPath(result_doc,template_element,sippNode,result_doc_root);
		}
	}
	
	/* xPath function, permits to add a node, manipulate attributes containing xpath expressions or not, 
	 * manipulate children and adding them to the resulting xml file
	 * */
	public static void xPath(Document result_document, Element current_template_node, ArrayList<Element> sippNode, Element result_doc_root)
	{			
		Element newelement = null;
		Map<String,ArrayList<Attribute>> att = new HashMap<String, ArrayList<Attribute>>(); 
		boolean xpath_exist = false; 
		
		//Run through all attributes of the current template element
		for (Iterator i = current_template_node.attributeIterator();i.hasNext();) 
		{
			Attribute attribute = (Attribute) i.next();
			//Get each attribute value
			String attribut_value = attribute.getValue().toString(); 
			//If the value contains an xpath expression
			if(attribut_value.startsWith("xpath:"))
				{	//Go through the nodes in the nodes list
					for(int j=0; j<sippNode.size(); j++)
					{
						xpath_exist= true;
						//Get the xpath expression
						String xpath_value = attribut_value.substring(6);
						//Create an XPath on the result document
				    	XPath xpath = result_document.createXPath(xpath_value);
				    	//Get the XPath returning value
				    	Object obj = xpath.evaluate(sippNode.get(j));
				    	  if(obj instanceof Attribute)
				    	  {
				    		  /*If the XPath is an instance of an Attribute we create a list for all xpath
				    		   * values, and we add every xpath attribute found, to the list. 
				    		   */
				    		  
				    		  ArrayList<Attribute> list = att.get(attribut_value); 
				    		  if(list == null)
				    		  {
				    			  list = new ArrayList<Attribute>() ; 
				    			  att.put(attribut_value,list); 
				    		  }
				    			  list.add((Attribute) obj); 
				    	  }
					}
				}
		}
	  	int max = 0 ;
	  	//We go through the values of the Attributes list, and we get the maximum size 
		for (ArrayList<Attribute> value : att.values()) {
				if(value.size()>max)
				{
					max = value.size();
				}
		}
		//A loop from 0 to the max
		for(int k =0; k<max; k++)	
		{	//Create a new element in the result document with the same name as the current template 
			//node's name
			newelement = result_doc_root.addElement(current_template_node.getName());
			for (Iterator i = current_template_node.attributeIterator();i.hasNext();) 
				{	
					/*We get the current TEMPLATE node attributes, and we create an equivalent attribute
					 * for the new element created, with the same attribute name and value. 
					 */
					Attribute attribute = (Attribute) i.next();
					String attribut_value = attribute.getValue().toString(); 
					ArrayList <Attribute> new_attribut = att.get(attribut_value);
					if(new_attribut != null)
						newelement.addAttribute(attribute.getName(),new_attribut.get(k).getValue());
				}
		}
		/*If there is no XPath in the current TEMPLATE node, we add the new element normally with the same
		 * attributes and same TEMPLATE name.
		 */
		if(!xpath_exist)
		{
			newelement = result_doc_root.addElement(current_template_node.getName());	
			newelement.setAttributes(current_template_node.attributes());
		}
		/*If the TEMPLATE node is a 'sendMessageSIP', we have to add the same CDATA section from the SIPP
		 * node to the newly created element
		 */
		if(current_template_node.getName().equals("sendMessageSIP"))
		{	
			newelement.addCDATA(sippNode.get(sippNode.size()-1).getStringValue());
		}
		//If the new element has been assigned successfully we do the same to the TEMPLATE node CHILDS
		if(newelement != null)
		for (Iterator k = current_template_node.elementIterator(); k.hasNext();)
	    {
			Element child_template = (Element) k.next();
			xPath(result_document, child_template, sippNode,newelement); 
	    }
	}
	
	//Add the global parameters as they are in the global templates file
	public static void addGlobalNode(Document Doc, Element mainNode) throws DocumentException
	{
		SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/tutorial/importsipp/Templates/global_template.xml");
		Element template_root = template.getRootElement();
		for (Iterator i = template_root.elementIterator();i.hasNext();) 
		{
			Element template_element = (Element) i.next();
			Element new_element = mainNode.addElement(template_element.getName());
			new_element.setAttributes(template_element.attributes());
		}
	}
	
	//Same as 'addNode' functionality but for the 'if_receive' templates
	public static void addNode2(ArrayList<Element> nodes, Element main_root, Document doc2, String template_file) throws SAXException, IOException, DocumentException
	{	
		SAXReader reader = new SAXReader();
		Document template = reader.read("../mts/src/main/tutorial/importsipp/Templates/"+template_file+"_template.xml");
		Element template_root = template.getRootElement(); 
		for(int n = 0; n<nodes.size(); n++)
		{
			Element sippNode = nodes.get(n);
			ArrayList<Element> sippNodeList = new ArrayList<Element>(); 
			sippNodeList.add(sippNode);
			for (Iterator i = template_root.elementIterator();i.hasNext();) 
			{
				Element template_element = (Element) i.next();
		        	if(template_element.getName().equals("parameter"))
		        	{
		        		if(sippNode.getStringValue().contains(template_element.attribute("name").getValue()))
		        		{
		        			xPath(doc2,template_element,sippNodeList,main_root);
		        		}
		        	}
		        	else
		        		xPath(doc2,template_element,sippNodeList,main_root);
			}
		}
	}
	
}