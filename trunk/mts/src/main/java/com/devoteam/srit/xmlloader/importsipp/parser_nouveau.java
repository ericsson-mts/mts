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
	
	 
public class parser_nouveau {
		static String filename = "branchc";
		static String filetype = ".xml";
		//static NamedNodeMap attributes_list = null; 
	 	static boolean next = false; 
	 	static boolean optional = false;
	 	static boolean test = false ; 
	 	static String[] values;
	  	static String valeur_globale = null;
		static Object result = null; 
		static Element if_element = null;
		static String newline = System.getProperty("line.separator");
		static String saved_value = "";
			
	@SuppressWarnings({ "rawtypes" })
	public static void main(String argv[]) throws DocumentException, ParserConfigurationException, SAXException {
		
		ArrayList<Element> nodes = new ArrayList<Element>(); 
		
		 try {
			 	String filepath = filename+filetype; 
			 	
			 	SAXReader reader = new SAXReader();
			 	Document doc = reader.read("../mts/src/main/tutorial/importsipp/"+filepath);
			 
			 	Document doc2 = DocumentHelper.createDocument();
			 	Element rootElement = doc2.addElement("scenario");
			 	
			 	/* Get the root of the current document */
			 	Element root = doc.getRootElement(); 
			 	
				  for ( Iterator i = root.attributeIterator(); i.hasNext(); ) {
			            Attribute attribute = (Attribute) i.next();
			            root.remove(attribute);
				  }
				  addGlobalNode(doc2,rootElement);
				  
			for (Iterator i = root.elementIterator(); i.hasNext();) 
			{
	            Element element = (Element) i.next();
				String nodename = element.getName(); 
				//si le noeud est # de recv
				if(nodename.equals("recv"))
				{	
					if(element.attributes().toString().contains("optional")&& element.attributes().toString().contains("next")) 
						{	
							nodes.add(element);
						}
					else if(!element.attributes().toString().contains("optional"))
						{
							addNode(element, rootElement, doc2,nodename);
							addNode2(nodes, rootElement, doc2,"if_"+nodename); 
							nodes.clear();
						}
				}
				else
					addNode(element, rootElement, doc2,nodename);
			}
	
		// WRITE THE FINAL RESULT TO XML FILE
		rightFinalResult(doc2);
		
		//REPLACE WITH < & >
		replaceInFile(filename+"_mts.xml", "&lt;", "<");
		replaceInFile(filename+"_mts.xml", "&gt;", ">");
		replaceInFile(filename+"_mts.xml", "&#13;", "");
	
		//WRITE 'DONE'
		System.out.println("Done");
			
			} catch (IOException ioe) {
			ioe.printStackTrace();
		   }
	 }
	
	public static void rightFinalResult(Document main_doc) throws IOException
	{	
		 OutputFormat format = OutputFormat.createPrettyPrint(); 
		 XMLWriter writer = new XMLWriter(new FileWriter( "output.xml" ), format); 
		 writer.write(main_doc);
	     writer.close();
	}
	
	public static void replaceInFile(String filename, String what, String to) throws IOException {
		
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
		    String newtext = oldtext.replaceAll(what, to);   
		    FileWriter writer = new FileWriter(filename);
		    writer.write(newtext);
		    writer.close();
		    }
		       
		catch (IOException ioe){
		            ioe.printStackTrace();
		        }
	 }
	
	@SuppressWarnings("rawtypes")
	public static void addNode(Element sippNode, Element main_root, Document doc2, String template_file) throws SAXException, IOException, DocumentException
		{
		 	SAXReader reader = new SAXReader();
		 	Document template = reader.read("../mts/src/main/tutorial/importsipp/Templates/"+template_file+"_template.xml");
			Element template_root = template.getRootElement(); 
			
			for (Iterator i = template_root.elementIterator();i.hasNext();) 
			{	
				Element template_element = (Element) i.next();
				//Element new_element = null;
				
		        	if(template_element.getName().equals("parameter"))
		        	{
		        		if(sippNode.getStringValue().contains(template_element.attribute("name").getValue()))
		        		{	
		        			xPath(doc2,template_element,sippNode,main_root);
		        		}
		        	}
		        	else
		        		xPath(doc2,template_element,sippNode,main_root);
			}
		}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public static void xPath(Document doc, Element noeud, Element sippNode, Element main_root)
	{	
		Element newelement = null;
		Map<String,Attribute> att = new HashMap<String, Attribute>(); 
		boolean xpath_exist = false; 
		
			for (Iterator i = noeud.attributeIterator();i.hasNext();) 
			{
				Attribute attribute = (Attribute) i.next();
				String attribut_value = attribute.getValue().toString(); 
				if(attribut_value.startsWith("xpath:"))
					{
						xpath_exist= true;
						//for sur toutes les nodes 
						String xpath_value = attribut_value.substring(6);
				    	XPath xpath = doc.createXPath(xpath_value);
				    	Object obj = xpath.evaluate(sippNode);
				    	//String bilal = obj.toString();
		
				    	  if(obj instanceof Attribute)
				    	  {
				    		  att.put(attribut_value,(Attribute) obj); 
				    	  }
					}
			}
			 
			//boucle sur la plus grande liste
			  if(!att.isEmpty())
				{
				  newelement = main_root.addElement(noeud.getName());
				  for (Iterator i = noeud.attributeIterator();i.hasNext();) 
					{
					  Attribute attribute = (Attribute) i.next();
					  String attribut_value = attribute.getValue().toString(); 
					  Attribute new_attribut = att.get(attribut_value);
					  if(new_attribut != null)
						  newelement.addAttribute(attribute.getName(),new_attribut.getValue());
					}
				}
			  else
			  {	
				  if(!xpath_exist)
					  {
					  	newelement = main_root.addElement(noeud.getName());
					  	newelement.setAttributes(noeud.attributes());
					  }
				  	  
			  }
		
		if(noeud.getName().equals("sendMessageSIP"))
		{	
			newelement.addCDATA(sippNode.getStringValue());
		}
		
		for (Iterator k = noeud.elementIterator(); k.hasNext();)
	    {
			Element child_template = (Element) k.next();
			xPath(doc, child_template, sippNode,newelement); 
	    }
	}
	
	@SuppressWarnings("rawtypes")
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
	
	@SuppressWarnings("rawtypes")
	public static void getChilds(Element template_element, Element new_element)
	{
		for (Iterator k = template_element.elementIterator(); k.hasNext();)
	    {	
	    	Element child_template = (Element) k.next();
	    	Element child_element = new_element.addElement(child_template.getName());
	    	child_element.setAttributes(child_template.attributes());
	    	
	    	getChilds(child_template, new_element);
	    }
	
	}
	@SuppressWarnings("rawtypes")
	public static void addNode2(ArrayList<Element> nodes, Element main_root, Document doc2, String template_file) throws SAXException, IOException, DocumentException
	{	
		SAXReader reader = new SAXReader();
		Document template = reader.read("../mts/src/main/tutorial/importsipp/Templates/"+template_file+"_template.xml");
		Element template_root = template.getRootElement(); 
		for(int n = 0; n<nodes.size(); n++)
		{
			Element sippNode = nodes.get(n);
			for (Iterator i = template_root.elementIterator();i.hasNext();) 
			{
				Element template_element = (Element) i.next();
				Element new_element = null;
		       	        	
		        	if(template_element.getName().equals("parameter"))
		        	{
		        		if(sippNode.getStringValue().contains(template_element.attribute("name").getValue()))
		        		{
		        			xPath(doc2,template_element,sippNode,main_root);
		        		}
		        	}
		        	else
		        		xPath(doc2,template_element,sippNode,main_root);
			}
		}
	}
	
}