package com.devoteam.srit.xmlloader.importsipp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

 
public class readxml2 {
	
	static String filename = "branchs";
	static String filetype = ".xml";
	static NamedNodeMap attributes_list = null; 
 	static boolean next = false; 
 	static boolean optional = false; 
 	static String[] values;
  	static String valeur_globale = null;
	static Object result = null; 
 public static void main(String argv[]) {
 
	 try {
		String filepath = filename+filetype; 
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse("../mts/src/main/tutorial/importsipp/"+filepath);
		docFactory.setValidating(true);
		docFactory.setIgnoringElementContentWhitespace(true);

		/*CREATE ANOTHER DOCUMENT WHERE TO WRITE THE RESULTS*/
		DocumentBuilderFactory doc2Factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder doc2Builder = doc2Factory.newDocumentBuilder();
		Document doc2 = doc2Builder.newDocument(); 
		/* Create a root element in the new file */
		Element rootElement = doc2.createElement("scenario");
		doc2.appendChild(rootElement);
		
		/* Get the root of the current document */
		Node root = doc.getDocumentElement();
		
		if(root.hasAttributes())
			{ 	
				NamedNodeMap attrs = root.getAttributes();
				String[] names = new String[attrs.getLength()];
				for (int n=0; n<names.length; n++) {
				    names[n] = attrs.item(n).getNodeName();
				}
				for (int n=0; n<names.length; n++) {
				    attrs.removeNamedItem(names[n]);
				}
			}
		
		/*All nodes of the current document*/
		NodeList allnodes = root.getChildNodes();
		
		/*Add global templates in head of the file */ 
		addGlobalNode("global_template.xml", rootElement, doc, doc2, "");
		
		for(int x=0;x<allnodes.getLength();x++)
		{			
			/* Get the name of the node we are working on */
			String nodename = allnodes.item(x).getNodeName().toString();
			
			//SAVE THE TEXT OF THE NODE IN A STRING
			String saved_text = allnodes.item(x).getTextContent();
			
			/*GET ALL ATTRIBUTES OF THE CURRENT NODE SO WE CAN ADD THEM LATER TO THE NEW
			NODE IN THE RESULT FILE */
			if(allnodes.item(x).hasAttributes())
			{
				attributes_list = allnodes.item(x).getAttributes();
			}
			
			//If it's a commentary then we create a new commentary in the result document
			if(nodename.equals("#comment"))
			{
				Comment commentary = doc2.createComment(allnodes.item(x).getNodeValue().toString());
				rootElement.appendChild(commentary);
			}
			
			if(!nodename.equals("#comment") && !nodename.equals("#text")){	
				//Adding the corresponding node from template document to result document
				if(nodename.equals("recv"))
				{	
					String siblingname = allnodes.item(x).getNextSibling().getNodeName().toString();
					if(siblingname.equals("recv"))
					{
						System.out.println("hi");
					}
				}
				if(allnodes.item(x).hasChildNodes())
				{	  
					NodeList childNodeList = allnodes.item(x).getChildNodes(); 
					for(int i = 0; i<childNodeList.getLength(); i++)
					{
						String childNodename = childNodeList.item(i).getNodeName().toString();
						if(!childNodename.equals("#comment") && !childNodename.equals("#text")&& !childNodename.equals("#cdata-section"))
							{//addNode(childNodename+"_template.xml", rootElement, doc, doc2, saved_text);}
						addNode(childNodeList.item(i), rootElement, doc, doc2, saved_text);}
					}		
				}	
				addNode(allnodes.item(x), rootElement, doc, doc2, saved_text);
			}
		}

	// WRITE THE FINAL RESULT TO XML FILE
	rightFinalResult(doc2);
	
	//REPLACE WITH < & >
	replaceInFile(filename+"_mts.xml", "&lt;", "<");
	replaceInFile(filename+"_mts.xml", "&gt;", ">");
	replaceInFile(filename+"_mts.xml", "&#13;", "");

	//WRITE 'DONE'
	System.out.println("Done");
		
		} catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	   }  catch (IOException ioe) {
		ioe.printStackTrace();
	   } catch (SAXException sae) {
		sae.printStackTrace();
	   }
 }
	
 @SuppressWarnings({ "unused" })
public static void addNode(Node sippNode, Node main_root, Document doc, Document doc2, String saved_value) throws ParserConfigurationException, SAXException, IOException
	{	
	 optional = false; 
	 next = false; 
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);	
		
		Document file = builder.parse("../mts/src/main/tutorial/importsipp/Templates/"+sippNode.getNodeName().toString()+"_template.xml");
	
		//ROOT ELEMENT 
		Node root_element = file.getDocumentElement();
		String newline = System.getProperty("line.separator");
	
		//Get all the child nodes
		String valeur = null; 
		NodeList allnodes = root_element.getChildNodes();
		for(int i=0;i<allnodes.getLength();i++)
		{	
			//Get the name of the child node
			String template_node_name = allnodes.item(i).getNodeName().toString();
			String saved_template_text = allnodes.item(i).getTextContent();
			
			//If it's not a commentary or text replace the original node
			if(!template_node_name.equals("#text") && !template_node_name.equals("#comment"))
			{	
				/* Create a new element*/ 
				Element newelement  = doc2.createElement(template_node_name);
				newelement.setNodeValue(allnodes.item(i).getNodeValue());
				newelement.setTextContent(saved_template_text);
				
				if(newelement.getNodeName().equals("sendMessageSIP")){	
					newelement.setTextContent(newline+"<![CDATA["+saved_value+"]]>"+newline);
				}
				
				if(newelement.getNodeName().toString().equals("receiveMessageSIP"))
				{	
					for (int p=0; p<attributes_list.getLength();p++){
						newelement.setAttribute(attributes_list.item(p).getNodeName().toString(), attributes_list.item(p).getNodeValue().toString());
						
						if(attributes_list.item(p).getNodeName().toString().equals("optional"))
							optional = true;
						if(attributes_list.item(p).getNodeName().toString().equals("next"))
							next = true; 
					}
					
					if (optional && next)
						main_root.appendChild(newelement);
					else if(!optional)
						main_root.appendChild(newelement);
				}
				
				/* Set the newelement attributes same as from the template file */ 
				else if(allnodes.item(i).hasAttributes())
				{
					NamedNodeMap template_nodemap = allnodes.item(i).getAttributes(); 
					for (int k=0; k<template_nodemap.getLength();k++){
						String attribut_value = template_nodemap.item(k).getNodeValue().toString(); 
					if(attribut_value.startsWith("xpath:"))
						{ 		
				            try {
								XPathFactory xpathfactory = XPathFactory.newInstance();
								XPath xpath = xpathfactory.newXPath();
								XPathExpression expr;
								String xpath_value = attribut_value.substring(6);
								expr = xpath.compile(xpath_value);
								result = expr.evaluate(sippNode, XPathConstants.STRING);
								if(!result.toString().isEmpty())
								{
									newelement.setAttribute(template_nodemap.item(k).getNodeName().toString(),result.toString());
									main_root.appendChild(newelement);
								}
				            } 
								catch (XPathExpressionException e) {
									e.printStackTrace();
								}
						}
					else 
						{
						newelement.setAttribute(template_nodemap.item(k).getNodeName().toString(), template_nodemap.item(k).getNodeValue().toString());
							if(newelement.getNodeName().equals("parameter")){
								if(saved_value.contains(newelement.getAttribute("name"))){
									main_root.appendChild(newelement);
								}
							}
							else
							main_root.appendChild(newelement);
						}
					}
				} 
				else 
					main_root.appendChild(newelement);
			}
		}

	}

 public static void addGlobalNode(String template_filename, Node main_root, Document doc, Document doc2, String saved_value) throws ParserConfigurationException, SAXException, IOException
	{	
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);	
		Document file = builder.parse("../mts/src/main/tutorial/importsipp/Templates/"+template_filename);
		
		//Root Element
		Node root_element = file.getDocumentElement();
		
		//Get all the child nodes
		NodeList allnodes = root_element.getChildNodes();
		for(int i=0;i<allnodes.getLength();i++)
		{	
			//Get the name of the child node
			String template_node_name = allnodes.item(i).getNodeName().toString();
	
			//If it's not a commentary or text replace the original node
			if(!template_node_name.equals("#text") && !template_node_name.equals("#comment"))
			{	
				Element newelement  = doc2.createElement(allnodes.item(i).getNodeName().toString());
				newelement.setNodeValue(allnodes.item(i).getNodeValue());
				
				if(allnodes.item(i).hasAttributes())
				{
					NamedNodeMap template_nodemap = allnodes.item(i).getAttributes(); 
					for (int k=0; k<template_nodemap.getLength();k++)
					{
						newelement.setAttribute(template_nodemap.item(k).getNodeName().toString(), template_nodemap.item(k).getNodeValue().toString());
					}
				}
				main_root.appendChild(newelement);
	
			}
		}
	}
		
 public static void rightFinalResult(Document main_doc)
	{	
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(main_doc);
			StreamResult result = new StreamResult(new File(filename+"_mts.xml"));
			//Keep the original structure
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "no");
			transformer.transform(source, result);
			}
		catch (TransformerException tfe) {
				tfe.printStackTrace();
			   }
  }
	
	//Remove empty text nodes
 public static void stripSpace(Node node)
 {                      
		Node child = node.getFirstChild();
	    while(child!=null){
	    //Save the sibling of the node that will perhaps be removed and set to null
	    Node c = child.getNextSibling();                        
	    if((child.getNodeType()==Node.TEXT_NODE && child.getNodeValue().trim().length()==0))
	    	node.removeChild(child);                            
	    else // process children recursively
	    	stripSpace(child);                                     
	       	child=c;
	    }
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
  
  public static void removeAllAttributes(Element element)
  {
		if(element.hasAttributes())
		{ 	
			NamedNodeMap attrs = element.getAttributes();
			String[] names = new String[attrs.getLength()];
			for (int n=0; n<names.length; n++) {
			    names[n] = attrs.item(n).getNodeName();
			}
			for (int n=0; n<names.length; n++) {
			    attrs.removeNamedItem(names[n]);
			}
		}
  }

}