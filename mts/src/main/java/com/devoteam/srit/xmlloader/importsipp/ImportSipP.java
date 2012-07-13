package com.devoteam.srit.xmlloader.importsipp;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultElement;

import org.xml.sax.SAXException;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

@SuppressWarnings("rawtypes")
public class ImportSipP {
		//static String input_filename = "branchs";
		static String filetype = ".xml";
		
	public static void main(String... args) throws DocumentException, ParserConfigurationException, SAXException {
		
		 if (args.length <= 0) {
	            usage("All arguments are required !");
	     }
		 
		ArrayList<Element> nodes = new ArrayList<Element>(); 
		 try {
			 	//Get the source xml file, and parse it using SAX parser */
			 	String filepath = args[0]; 
			 	SAXReader reader = new SAXReader();
			 	Document sourceDocument = reader.read(filepath);
			 		
			 	//Create the resulting xml file, with the root element 'scenario'
			 	Document resultDocument = DocumentHelper.createDocument();
			 	Element rootElement = resultDocument.addElement("scenario");
			 	
			 	//Get the root of the source xml file */
			 	Element root = sourceDocument.getRootElement(); 
			 	
			 	//Remove all attributes of the root element (scenario)
				for ( Iterator i = root.attributeIterator(); i.hasNext(); ) {
			            Attribute attribute = (Attribute) i.next();
			            root.remove(attribute);
				  }
				//Add the global parameters from the global template xml file
				addGlobalNode(resultDocument,rootElement, "scenario"); 
				
				//Run through the elements (nodes) of the source xml file 
				for (Iterator i = root.elementIterator(); i.hasNext();) 
				{
		            Element element = (Element) i.next();
					String nodename = element.getName();
					
					//If the current node is a 'recv' node
					if(nodename.equals("recv"))
					{	
						//If it contains the 'next' & 'optional' attributes
						if(element.attributes().toString().contains("optional")&& element.attributes().toString().contains("next")) 
						{	
							//We add the current node to the list of nodes we already created
							nodes.add(element);
						}
						//If the recv node does not contain the 'optional' attribute, then it's the last
						//recv node after sequence of recv nodes
						else if(!element.attributes().toString().contains("optional")&& !nodes.isEmpty())
						{	
							//We add this node to the list of nodes
							nodes.add(element);
							//We apply the normal template of the recv
							addNodeOther(nodes, rootElement, resultDocument,nodename);
							//We apply the if_recv template
							addNodeReceive(nodes, rootElement, resultDocument,"if_"+nodename);
							//We clear the saved nodes list
							nodes.clear();
						}
						else if(!element.attributes().toString().contains("optional"))
						{	
							
							//We add this node to the list of nodes
							nodes.add(element);
							//We apply the normal template of the recv
							addNodeOther(nodes, rootElement, resultDocument,nodename);
							//We clear the saved nodes list
							nodes.clear();
						}
					}
					//If the current node is NOT a 'recv' node
					else
					{	
						//We add the current node to the nodes list
						nodes.add(element);
						//We apply the corresponding template file
						addNodeOther(nodes, rootElement, resultDocument,nodename);
						//We clear the saved nodes list
						nodes.clear();
					}
					for(Iterator j = element.elementIterator(); j.hasNext();)
					{	
						ArrayList<Element> childNodes = new ArrayList<Element>(); 
						Element childNodeElement = (Element) j.next();
						String childName = childNodeElement.getName(); 
						if(childName.equals("action"))
						{
							for(Iterator k = childNodeElement.elementIterator(); k.hasNext();)
							{	
								Element childOfAction = (Element) k.next();
								childNodes.add(childOfAction);
								addNodeReceive(childNodes, rootElement, resultDocument, childOfAction.getName()); 
								childNodes.clear();
							}
						}
						else
						{
							childNodes.add(childNodeElement); 
							addNodeReceive(childNodes, rootElement, resultDocument,childName);
							childNodes.clear();
						}
						
						
					}
				}
				//Function to write the result, to the resulting xml file
				rightFinalResult(resultDocument, args[1]);
				
				//Function to replace strings in a file
				replaceInFile(args[1], "&lt;", "<");
				replaceInFile(args[1], "&gt;", ">");
				replaceInFile(args[1], "&#13;", "");
				
				//Create the corresponding TEST file
				//testFileName,String testName, String outputFileName
				createTestFile(args[2], args[3], args[1]); 
				
				//Write 'DONE' on the system out
				System.out.println("Done");
		}
		 catch (IOException ioe) {
			log(ioe, "ERROR", "IO Exception");    
		}
	}
	
	/**
	 * Function that writes something to a file
	 * @param document
	 * @throws IOException
	 */
	public static void rightFinalResult(Document document, String fileName) throws IOException{	
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
			log(ioe, "ERROR", "IO Exception");           
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
	public static void addNodeReceive(ArrayList<Element> nodes, Element main_root, Document doc2, String template_file) throws SAXException, IOException, DocumentException{	
		SAXReader reader = new SAXReader();
		Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+template_file+".xml");
		Element template_root = template.getRootElement();
		main_root.addComment(nodes.get(0).getName());
		
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
	public static void addNodeOther(ArrayList<Element> sippNode, Element resultDocRoot, Document resultDoc, String templateFile) throws SAXException, IOException, DocumentException{		
		//Parsing the corresponding template file
	 	SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+templateFile+".xml");
		Element template_root = template.getRootElement(); 
		resultDocRoot.addComment(sippNode.get(0).getName());
		
		//Run through all template elements
		for (Iterator i = template_root.elementIterator();i.hasNext();) 
		{
			Element template_element = (Element) i.next();
			/*Test each element. If it's a 'parameter', the parameter name should exist in the 'send'
			  CDATA section to be added to the file. If not, this parameter wont be added*/
        	if(template_element.getName().equals("parameter") && !template_element.attribute("name").getValue().equals("[last_*]"))
        	{
        		if(sippNode.get(sippNode.size()-1).getStringValue().contains(template_element.attribute("name").getValue()))
        		{	
        			//Execute the xPath function which is responsible of adding elements and attributes 
        			xPath(resultDoc,template_element,sippNode,resultDocRoot );
        		}
        	}
        	else
        		xPath(resultDoc,template_element,sippNode,resultDocRoot);
		}	
	}
	
	public static String[] goThroughValues(Element currentTemplateNode)
	{ 	
		String[] values = new String[2]; 
		for(Iterator i = currentTemplateNode.elementIterator(); i.hasNext();)
		{
			Element templateChildElement = (Element) i.next();
			if(!templateChildElement.getStringValue().isEmpty())
			{
				values[0] = templateChildElement.getStringValue(); 
				values[1] = templateChildElement.getName();
			}
			goThroughValues(templateChildElement);
			//break;
		}
		
		return values; 
	}
	/**
	 * xPath function, permits to add a node, manipulate attributes containing xpath expressions or not, 
	 * manipulate children and adding them to the resulting xml file
	 * @param resultDocument
	 * @param currentTemplateNode
	 * @param sippNode
	 * @param resultDocRoot
	 */
	public static void xPath(Document resultDocument, Element currentTemplateNode, ArrayList<Element> sippNode, Element resultDocRoot){			
		Element newelement = null;
		Map<String,ArrayList<String>> att = new HashMap<String, ArrayList<String>>(); 
		boolean xpathExists = false; 
		ArrayList<String> numberOfLast = new ArrayList<String>();
		ArrayList<String> fileName = new ArrayList<String>(); 
		//Run through all attributes of the current template element
		for (Iterator i = currentTemplateNode.attributeIterator();i.hasNext();) 
		{
			Attribute attribute = (Attribute) i.next();
			//Get each attribute value
			String attributeValue = attribute.getValue().toString(); 
			//If the value contains an xpath expression
			if(attributeValue.contains("{xpath:"))
			{	//Go through the nodes in the nodes list
				for(int j=0; j<sippNode.size(); j++)
				{
					xpathExists= true;
					//Get the xpath expression
					
					Pattern pattern = Pattern.compile("[\\{}]");
					//String[] part = attributeValue.split("[{|}]");
					String[] part = pattern.split(attributeValue);
					String xpathValue = part[1].substring(6);
					//Create an XPath on the result document
			    	XPath xpath = resultDocument.createXPath(xpathValue);
			    	//Get the XPath returning value
			    	Object obj = xpath.evaluate(sippNode.get(j));
			    	
			    	//if the obj is an attribute
			    	  if(obj instanceof Attribute)
			    	  {
			    		  /*If the XPath is an instance of an Attribute we create a list for all xpath
			    		   * values, and we add every xpath attribute found, to the list. 
			    		   */
			    		  ArrayList<String> list = att.get(attributeValue); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeValue,list); 
			    		  }
			    			  list.add(((Attribute) obj).getValue()); 
			    	  }
			    	  //If the obj returns an Element
			    	  else if(obj instanceof Element)
			    	  {
			    		  ArrayList<String> list = att.get(attributeValue); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeValue,list); 
			    		  }
			    			  list.add(((Element) obj).getName()); 
			    	  }
			    	  //If the obj returns double
			    	  else if(obj instanceof Double)
			    	  {
			    		  ArrayList<String> list = att.get(attributeValue); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeValue,list); 
			    		  }
			    			  list.add(((Double) obj).toString()); 
			    	  }
			    	  //If it contains a CDATA bloc
			    	  else if(obj instanceof ArrayList)
			    	  {
						ArrayList cdataResult = null ; 
						cdataResult = (ArrayList) obj ; 
						DefaultCDATA cdataBloc = (DefaultCDATA) cdataResult.get(1); 
						String cdataContent = cdataBloc.getText();
						//Get everything variable name that contains 'last' from the CDATA bloc
						Pattern last = Pattern.compile("\\b(\\w*?last_)\\w+\\b");  
						//Get all the 'file' names in the CDATA section
						Pattern file = Pattern.compile("file=\"[^\"]*\"");
						//Get the matching variables
						Matcher variableMatcher = last.matcher(cdataContent);
						//Get the matching file names
						Matcher fileMatcher = file.matcher(cdataContent); 
						while (variableMatcher.find())  
						{  
							//Add every occurrence found to an array list
							numberOfLast.add(variableMatcher.group());
						}
						while(fileMatcher.find())
						{
							System.out.println((fileMatcher.group().replaceAll("file=", "")).replaceAll("\"", ""));
							fileName.add((fileMatcher.group().replaceAll("file=", "")).replaceAll("\"", ""));
						}
			    	  }
				}
			}
		}
	  	int max = 0 ;
	  	//We go through the values of the Attributes list, and we get the maximum size 
		for (ArrayList<String> value : att.values()) 
		{
			if(value.size()>max)
			{
				max = value.size();
			}
		}
		 if(!fileName.isEmpty())
		 {
			 for(int l =0; l<fileName.size(); l++)
			 {
				 Element newParameter = resultDocRoot.addElement("parameter");
				 newParameter.addAttribute("name", "[filedNumber]");
				 newParameter.addAttribute("operation", "file.readcsv"); 
				 newParameter.addAttribute("value", fileName.get(l));
			 }
		 }
		//Add the parameters corresponding to the variables used in the CDATA section
		 if(!numberOfLast.isEmpty())
		 {
			 for(int l =0; l<numberOfLast.size(); l++)
			{	
				 Element newParameter = resultDocRoot.addElement("parameter");
				 newParameter.addAttribute("name", "["+numberOfLast.get(l)+":]");
				 newParameter.addAttribute("operation", "setFromMessage"); 
				 newParameter.addAttribute("value", "header."+ numberOfLast.get(l).substring(numberOfLast.get(l).indexOf("_")+ "_".length())+".Namevalue");
				 newParameter.addAttribute("value2", "[last_Message]");
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
				ArrayList <String> newAttribute = att.get(attribut_value);
				//TEST
				Pattern pattern = Pattern.compile("[\\{}]");
				String[] part = pattern.split(attribute.getValue());
				String after = attribute.getValue().substring(attribute.getValue().lastIndexOf('}') + 1);
				//
				if(newAttribute != null)
				{	
					newelement.addAttribute(attribute.getName(),part[0]+newAttribute.get(k)+after); 
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
		if(!xpathExists)
		{	
			newelement = resultDocRoot.addElement(currentTemplateNode.getName());	
			newelement.setAttributes(currentTemplateNode.attributes());
		}
		/*If the TEMPLATE node is a 'sendMessageSIP', we have to add the same CDATA section from the SIPP
		 * node to the newly created element
		 */	
		if(currentTemplateNode.getText().contains("xpath"))
		{
			String content = currentTemplateNode.getText();
			Pattern pattern = Pattern.compile("[\\{}]");
			String[] part = pattern.split(content);
			XPath xpath = resultDocument.createXPath(part[1].substring(6));
			Object obj = xpath.evaluate(sippNode.get(0));
			if(obj instanceof Attribute)
	    	  {	
				  Attribute resultat = null ; 
	    		  resultat = (Attribute) obj ;
	    		  newelement.addText("\n"+resultat.getValue());
	    	  }

			else if(obj instanceof ArrayList)
	    	  {
				ArrayList cdataResult = null ; 
				cdataResult = (ArrayList) obj ; 
				DefaultCDATA result = (DefaultCDATA) cdataResult.get(1); 
				newelement.addCDATA(result.getText());
	    	  }
		}
		//If the new element has been assigned successfully we do the same to the TEMPLATE node CHILDS
		if(newelement != null)
			for (Iterator k = currentTemplateNode.elementIterator(); k.hasNext();)
		    {	
				Element child_template = (Element) k.next();
				//System.out.println(child_template.getName());
				//child_template.setText(template_content);
				xPath(resultDocument, child_template, sippNode,newelement); 
		    }
		resultDocRoot.getName();
	}
	
	/**
	 * Add the global parameters as they are in the global templates file
	 * @param Doc
	 * @param mainNode
	 * @throws DocumentException
	 */
	public static void addGlobalNode(Document Doc, Element mainNode, String global_file) throws DocumentException{
		SAXReader reader = new SAXReader();
	 	Document template = reader.read("../mts/src/main/conf/importsipp/Templates/"+global_file+".xml");
		Element template_root = template.getRootElement();
		mainNode.addComment("Global Nodes");
		for (Iterator i = template_root.elementIterator();i.hasNext();) 
		{
			Element template_element = (Element) i.next();
			Element new_element = mainNode.addElement(template_element.getName());
			new_element.setAttributes(template_element.attributes());
		}
	}
	
	/**
	 * This function creates the Test file to be used by MTS.  
	 * @param testFileName
	 * @param testName
	 * @param inputFileName
	 */
	public static void createTestFile(String testFileName,String testName, String outputFileName){	
		boolean scenarioExists = false ;
		boolean testcaseExists = false; 
		String fileName = outputFileName.substring(outputFileName.lastIndexOf("\\")+1);
		try {	
				boolean exists = (new File(testFileName)).exists();
				//If the file exists, add the element to it's corresponding place in the file
				if (exists) {
					SAXReader reader = new SAXReader();
					Document doc = reader.read(testFileName);
					Element root = doc.getRootElement();
					Element testCaseMain = null ; 
					for(Iterator j = root.elementIterator(); j.hasNext();)
					{
						testCaseMain = (Element) j.next();
						if(testCaseMain.attribute("name").getValue().equals(testName))
						{	
							testcaseExists = true; 
							for (Iterator i = testCaseMain.elementIterator(); i.hasNext();) 
							{
								Element scenario = (Element) i.next();
								if(scenario.getStringValue().equals(fileName))
									scenarioExists = true;  
							}
							break;
						}
					}
					//If tests, to assure not duplicating the tags
					if(testcaseExists == true)
					{
						if(scenarioExists == false)
						{
							Element scenario2 = testCaseMain.addElement("scenario");
							scenario2.addAttribute("name", "SIP");
							scenario2.addText(fileName);
						}
					}
					//If the file exists and the testcase tag doesn't exist, add the tag and the elements
					else
					{
						Element testcase2 = root.addElement("testcase"); 
						testcase2.addAttribute("name", testName);
						testcase2.addAttribute("description", "test sip"); 
						testcase2.addAttribute("state", "true");
						Element scenario = testcase2.addElement("scenario");
						scenario.addAttribute("name", "alice");
						scenario.addText(fileName);
						
					}						
					rightFinalResult(doc, testFileName);
				}
				//If the file doesn't exist, create it and add the elements
				else {
					Document doc = DocumentHelper.createDocument();
					Element rootElement = doc.addElement("test");
					rootElement.addAttribute("name", "importsipp");
					rootElement.addAttribute("description", "imported from sipp scenario");
					addGlobalNode(doc, rootElement, "testSuite");
					Element testCase = rootElement.addElement("testcase");
					testCase.addAttribute("name", testName);
					testCase.addAttribute("description", "test sip"); 
					testCase.addAttribute("state", "true");
					
					Element scenario = testCase.addElement("scenario");
					scenario.addAttribute("name", "alice");
					scenario.addText(fileName);
					rightFinalResult(doc, testFileName);
				}
			}
		catch (DocumentException e) {
			log(e, "error", "Document exception"); 
		}
		catch (IOException e) {
			log(e, "error", "IO Exception");
		}
	}
	
	/**
	 * Function that define usage for the user
	 * @param message
	 */
	static public void usage(String message) {
        System.out.println(message);
        System.out.println("Usage: importSipp <inputFileName>|<testFileName>|<testName>\n");
        System.exit(10);
    }
	
	/**
	 * Function that writes in the log file
	 * @param e
	 * @param level
	 * @param message
	 */
	static public void log(Exception e, String level, String message){
	    
		if(e!=null)
		{
			if (level.toUpperCase().equals("ERROR")) {
	        	e.printStackTrace();
	        	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "importSipp: "+message);
	        }
	        else if (level.toUpperCase().equals("WARN")) {
	        	e.printStackTrace();
	        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "importSipp: "+message);
	        }
		}
		else
		{
			if (level.toUpperCase().equals("DEBUG")) {
				GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "importSipp: "+message);
		    }
	        else if (level.toUpperCase().equals("INFO")) {
	        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "importSipp: "+message);
	        }
		}
	}
        	
}