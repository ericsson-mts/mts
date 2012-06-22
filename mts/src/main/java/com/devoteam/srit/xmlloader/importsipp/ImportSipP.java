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
		static String filename = "branchs";
		static String filetype = ".xml";

	public static void main(String argv[]) throws DocumentException, ParserConfigurationException, SAXException {
		
		ArrayList<Element> nodes = new ArrayList<Element>(); 
		 try {
			 	//Get the source xml file, and parse it using SAX parser */
			 	String filepath = filename+filetype; 
			 	SAXReader reader = new SAXReader();
			 	Document source_doc = reader.read("../mts/src/main/tutorial/importsipp/"+filepath);
			 		
			 	//Create the resulting xml file, with the root element 'scenario'
			 	Document resultDoc = DocumentHelper.createDocument();
			 	Element rootElement = resultDoc.addElement("scenario");
			 	
			 	//Get the root of the source xml file */
			 	Element root = source_doc.getRootElement(); 
			 	
			 	//Remove all attributes of the root element (scenario)
				for ( Iterator i = root.attributeIterator(); i.hasNext(); ) {
			            Attribute attribute = (Attribute) i.next();
			            root.remove(attribute);
				  }
				//Add the global parameters from the global template xml file
				addGlobalNode(resultDoc,rootElement, "global_template");
				
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
							addNode(nodes, rootElement, resultDoc,nodename);
							//We apply the if_recv template
							addNode2(nodes, rootElement, resultDoc,"if_"+nodename);
							//We clear the saved nodes list
							nodes.clear();
						}
					}
					//If the current node is NOT a 'recv' node
					else
					{	//We add the current node to the nodes list
						nodes.add(element);
						//We apply the corresponding template file
						addNode(nodes, rootElement, resultDoc,nodename);
						//We clear the saved nodes list
						nodes.clear();
					}
				}
		
				//Function to write the result, to the resulting xml file
				rightFinalResult(resultDoc, filename+"_mts");
				
				//Function to replace strings in a file
				replaceInFile(filename+"_mts.xml", "&lt;", "<");
				replaceInFile(filename+"_mts.xml", "&gt;", ">");
				replaceInFile(filename+"_mts.xml", "&#13;", "");
				
				//Create the corresponding TEST file
				createTestFile("test.xml", "001_stats"); 
				
				//Write 'DONE' on the system out
				System.out.println("Done");
			
		}
		 catch (IOException ioe) {
			ioe.printStackTrace();
		   }
}
	/**
	 * Function that writes something to an xml file
	 * @param document
	 * @throws IOException
	 */
	public static void rightFinalResult(Document document, String fileName) throws IOException
	{	
		 OutputFormat format = OutputFormat.createPrettyPrint();
		 //We define the result xml file name
		 XMLWriter writer = new XMLWriter(new FileWriter(fileName), format); 
		 writer.write(document);
	     writer.close();
	}
	
	/**
	 * Function that replaces a String by another one in a file 
	 * @param filename
	 * @param from
	 * @param to
	 * @throws IOException
	 */
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
	
	/**
	 * Function that adds a node to the resulting file
	 * It takes an array list of Nodes, the root of the result document, the result document and
	  	the template file name
	 * @param sippNode
	 * @param resultDocRoot
	 * @param resultDoc
	 * @param templateFile
	 * @throws SAXException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void addNode(ArrayList<Element> sippNode, Element resultDocRoot, Document resultDoc, String templateFile) throws SAXException, IOException, DocumentException
	{		
		//Parsing the corresponding template file
	 	SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+templateFile+"_template.xml");
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
        			xPath(resultDoc,template_element,sippNode,resultDocRoot);
        		}
        	}
        	else 
        		xPath(resultDoc,template_element,sippNode,resultDocRoot);
		}
	}

	/**
	 * xPath function, permits to add a node, manipulate attributes containing xpath expressions or not, 
	 * manipulate children and adding them to the resulting xml file
	 * @param resultDocument
	 * @param currentTemplateNode
	 * @param sippNode
	 * @param resultDocRoot
	 */
	public static void xPath(Document resultDocument, Element currentTemplateNode, ArrayList<Element> sippNode, Element resultDocRoot)
	{			
		Element newelement = null;
		Map<String,ArrayList<Attribute>> att = new HashMap<String, ArrayList<Attribute>>(); 
		boolean xpath_exist = false; 
		
		//Run through all attributes of the current template element
		for (Iterator i = currentTemplateNode.attributeIterator();i.hasNext();) 
		{
			Attribute attribute = (Attribute) i.next();
			//Get each attribute value
			String attribut_value = attribute.getValue().toString(); 
			//If the value contains an xpath expression
			if(attribut_value.startsWith("xpath:"))
			{	//Go through the nodes in the nodes list_
				for(int j=0; j<sippNode.size(); j++)
				{
					xpath_exist= true;
					//Get the xpath expression
					String xpath_value = attribut_value.substring(6);
					//Create an XPath on the result document
			    	XPath xpath = resultDocument.createXPath(xpath_value);
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
		for (ArrayList<Attribute> value : att.values()) 
		{
			if(value.size()>max)
			{
				max = value.size();
			}
		}
		//A loop from 0 to the max
		for(int k =0; k<max; k++)	
		{	//Create a new element in the result document with the same name as the current template 
			//node's name
			newelement = resultDocRoot.addElement(currentTemplateNode.getName());
			for (Iterator i = currentTemplateNode.attributeIterator();i.hasNext();) 
				{	
					/*We get the current TEMPLATE node attributes, and we create an equivalent attribute
					 * for the new element created, with the same attribute name and value. 
					 */
					Attribute attribute = (Attribute) i.next();
					String attribut_value = attribute.getValue().toString(); 
					ArrayList <Attribute> new_attribut = att.get(attribut_value);
					if(new_attribut != null)
					{	
						newelement.addAttribute(attribute.getName(),new_attribut.get(k).getValue()); 
					}
					else
					{	
						if(!attribute.getValue().contains("xpath"))
							newelement.addAttribute(attribute.getName(),attribut_value);
					}
					
				}
		}
		/*If there is no XPath in the current TEMPLATE node, we add the new element normally with the same
		 * attributes and same TEMPLATE name.
		 */
		if(!xpath_exist)
		{
			newelement = resultDocRoot.addElement(currentTemplateNode.getName());	
			newelement.setAttributes(currentTemplateNode.attributes());
		}
		/*If the TEMPLATE node is a 'sendMessageSIP', we have to add the same CDATA section from the SIPP
		 * node to the newly created element
		 */
		//A VOIR
		if(currentTemplateNode.getName().equals("sendMessageSIP"))
		{	
			newelement.addCDATA(sippNode.get(sippNode.size()-1).getStringValue());
		}
		//If the new element has been assigned successfully we do the same to the TEMPLATE node CHILDS
		if(newelement != null)
		for (Iterator k = currentTemplateNode.elementIterator(); k.hasNext();)
	    {
			Element child_template = (Element) k.next();
			xPath(resultDocument, child_template, sippNode,newelement); 
	    }
	}
	
	/**
	 * Add the global parameters as they are in the global templates file
	 * @param Doc
	 * @param mainNode
	 * @throws DocumentException
	 */
	public static void addGlobalNode(Document Doc, Element mainNode, String global_file) throws DocumentException
	{
		SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+global_file+".xml");
		Element template_root = template.getRootElement();
		for (Iterator i = template_root.elementIterator();i.hasNext();) 
		{
			Element template_element = (Element) i.next();
			Element new_element = mainNode.addElement(template_element.getName());
			new_element.setAttributes(template_element.attributes());
		}
	}
	
	/**
	 * Same as 'addNode' functionality but for the 'if_receive' templates
	 * @param nodes
	 * @param main_root
	 * @param doc2
	 * @param template_file
	 * @throws SAXException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void addNode2(ArrayList<Element> nodes, Element main_root, Document doc2, String template_file) throws SAXException, IOException, DocumentException
	{	
		SAXReader reader = new SAXReader();
		Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+template_file+"_template.xml");
		Element template_root = template.getRootElement(); 
		for(int n = 0; n<nodes.size(); n++)
		{
			Element sippNode = nodes.get(n);
			ArrayList<Element> sippNodeList = new ArrayList<Element>(); 
			sippNodeList.add(sippNode);
			for (Iterator i = template_root.elementIterator();i.hasNext();) 
			{
				Element template_element = (Element) i.next();
				xPath(doc2,template_element,sippNodeList,main_root);

			}
		}
	}
	
	public static void createTestFile(String testFileName,String testName)
	{	
		try {	
				boolean exists = (new File(testFileName)).exists();
				if (exists) {
					SAXReader reader = new SAXReader();
					Document doc = reader.read(testFileName);
					Element root = doc.getRootElement();
					Element testcase = root.element("testcase");
					Element scenario = testcase.element("scenario");
					if( scenario != null)
					{
						if(!scenario.getStringValue().toString().equals(filename+"_mts"+filetype))
						{	
							Element scenario2 = testcase.addElement("scenario");
							scenario2.addAttribute("name", "bob");
							scenario2.addText(filename+"_mts"+filetype);
						}
					}
					rightFinalResult(doc, testFileName);
				}
				else {
					Document doc = DocumentHelper.createDocument();
					Element rootElement = doc.addElement("test");
					addGlobalNode(doc, rootElement, "test_Template");
					Element testCase = rootElement.addElement("testcase");
					testCase.addAttribute("name", testName);
					testCase.addAttribute("description", "test sip"); 
					testCase.addAttribute("state", "true");
					
					Element scenario = testCase.addElement("scenario");
					scenario.addAttribute("name", "alice");
					scenario.addText(filename+"_mts"+filetype);
					rightFinalResult(doc, testFileName);
				}
			} 
		catch (DocumentException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}