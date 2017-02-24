package com.devoteam.srit.xmlloader.importsipp;
	
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultCDATA;
import org.xml.sax.SAXException;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.TextExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;

@SuppressWarnings("rawtypes")
public class ImportSipP {
	
	private static final Exception Exception = null;
	public static void main(String... args) throws DocumentException, ParserConfigurationException, SAXException, IOException {
		
		// Initialization of MTS core
        ExceptionHandlerSingleton.setInstance(new TextExceptionHandler());
        SingletonFSInterface.setInstance(new LocalFSInterface());
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());
        
        //Set the storage location to FILE for logging
        PropertiesEnhanced properties = new PropertiesEnhanced();
        properties.addPropertiesEnhancedComplete("logs.STORAGE_LOCATION", "FILE");
        Config.overrideProperties("tester.properties", properties);
        
        //Remind the user of the usage in case of lack in arguments
		 if (args.length < 2) {
	            usage("All arguments are required !");
	     }
		 
		String sippfile = null, result = null, testfileName = null, testfile = null , testcase = null; 
		// Get all the arguments values
		for(int i=0; i<args.length; i++)
		{	
			if(args[i].equals("-sippfile"))
			{
				sippfile = args[i+1]; 
			}
			if(args[i].equals("-result"))
			{
				result = args[i+1]; 
			}
			if(args[i].equals("-testfileName"))
			{
				testfileName = args[i+1]; 
			}
			if(args[i].equals("-testcase"))
			{
				testcase = args[i+1]; 
			}
		}
		// If the optional arguments are empty, we specify a default value
		if(result == null)
		{	
			int sippFileNamePosition1 = sippfile.lastIndexOf("\\");
			int sippFileNamePosition2 = sippfile.lastIndexOf(".");
			new File(sippfile.substring(0, sippFileNamePosition1)+"/"+testcase).mkdirs();
			result = sippfile.substring(0, sippFileNamePosition1)+"\\"+ testcase + sippfile.substring(sippFileNamePosition1, sippFileNamePosition2)+"_mts.xml"; 
			
		}
		if(testfileName == null)
		{	
			int sippFileNamePosition = sippfile.lastIndexOf("\\"); 
			testfileName = "test.xml"; 
			testfile = sippfile.substring(0, sippFileNamePosition)+"\\"+testfileName; 
		}
		else if(testfileName!=null)
		{
			testfile = testfileName; 
		}
		if(testcase == null)
		{	
			int sippFileNamePosition1 = sippfile.lastIndexOf("\\");
			int sippFileNamePosition2 = sippfile.lastIndexOf("."); 
			testcase = sippfile.substring(sippFileNamePosition1+1, sippFileNamePosition2); 
		}

		 /*-------------------------------------------------------------------------
		  *	Logging and Writing INFO 
		 */
			File absolutSippFile = new File(sippfile); 
			File absolutTestFile = new File(testfile); 
			Exception e = new Exception(); 
			String message = "--------------------------------------\n" +
					"SIPP file = "+ absolutSippFile.getCanonicalPath() +"\n" +
							"Testcase Name = "+ testcase + "\n" +
									"Test Filename = "+ absolutTestFile.getCanonicalPath() + "\n" +
											"--------------------------------------"; 
			log(e, "INFO", message); 
		 /* -------------------------------------------------------------------------*/
		 
		/*
		 * Create the corresponding TEST file
		 * createTestFile(String testFileName,String testName, String outputFileName)
		 */
		 int scenarioNum = createTestFile(testfile, testcase, result, testcase); 
		 
		 /*-------------------------------------------------------------------------
		  *	Logging and Writing INFO 
		 */
			message = "Creating Test File....\nDone!\n" +
					"--------------------------------------"; 
			log(e, "INFO", message); 
		 /* -------------------------------------------------------------------------*/
			
		 ArrayList<Element> testList = new ArrayList<Element>(); 
		 ArrayList<Element> nodes = new ArrayList<Element>(); 
		 try {
			 	//Get the source XML file, and parse it using SAX parser */
			 	String filepath = sippfile; 
			 	SAXReader reader = new SAXReader();
			 	reader.setEntityResolver(new XMLLoaderEntityResolver());		 	
			 	Document sourceDocument = reader.read(filepath);
			 		
			 	//Create the resulting XML file, with the root element 'scenario'
			 	Document resultDocument = DocumentHelper.createDocument();
			 	Element rootElement = resultDocument.addElement("scenario");
			 	
			 	//Get the root of the source XML file */
			 	Element root = sourceDocument.getRootElement(); 

				//Add global template nodes after checking if they exist in any of the source file, tags
				for (Iterator i = root.elementIterator(); i.hasNext();) 
				{	
					Element element = (Element) i.next();
					testList.add(element);
				}
				/*-------------------------------------------------------------------------
				  *	Logging and Writing INFO 
				 */
				File absolutResult = new File(result); 
					message = "Creating resulting file: "+ absolutResult.getCanonicalPath() +"\n"+"Adding Global parameters:"; 											
					log(e, "INFO", message); 
				 /* -------------------------------------------------------------------------*/
					
				addNodeCheckParameters(testList, rootElement, resultDocument, "scenario", scenarioNum);
				
				/*-------------------------------------------------------------------------
				  *	Logging and Writing INFO 
				 */
					message = "DONE!\n"+"--------------------------------------"; 											
					log(e, "INFO", message); 
				 /* -------------------------------------------------------------------------*/
					
				//Run through the elements (nodes) of the source XML file 
				for (Iterator i = root.elementIterator(); i.hasNext();) 
				{
		            Element element = (Element) i.next();
					String nodename = element.getName();
					/*-------------------------------------------------------------------------
					  *	Logging and Writing INFO 
					 */
						message = "Current SIPp node: <"+ nodename +">"; 											
						log(e, "INFO", message); 
					 /* -------------------------------------------------------------------------*/
					addNodeWithParameters(nodes, rootElement, resultDocument,"operation", scenarioNum);
					//If the current node is a 'recv' node
					if(nodename.equals("recv"))
					{
						//If it contains the 'next' & 'optional' attributes together
						if(element.attributes().toString().contains("optional")&& element.attributes().toString().contains("next")) 
						{	
							//We add the current node to the list of nodes we already created
							nodes.add(element);
						}
						/*
						 * If the recv node does not contain the 'optional' attribute and the saved nodes
						 * list is not empty, then it's the last recv node after a sequence of recv nodes
						 */
						else if(!element.attributes().toString().contains("optional")&& !nodes.isEmpty())
						{
							//We add this node to the list of nodes
							nodes.add(element);
							//We apply the normal template of the recv
							addNodeCheckParameters(nodes, rootElement, resultDocument,nodename, scenarioNum);
							//We apply the if_recv template to work with the 'goto'
							addNodeWithParameters(nodes, rootElement, resultDocument,"if_"+nodename, scenarioNum);
							//We clear the saved nodes list
							nodes.clear();
						}
						//If it doesn't contain optional at all
						else if(!element.attributes().toString().contains("optional"))
						{
							//We add this node to the list of nodes
							nodes.add(element);
							//We apply the normal template of the recv
							addNodeCheckParameters(nodes, rootElement, resultDocument,nodename,scenarioNum);
							//We clear the saved nodes list
							nodes.clear();
						}
					}
					//If the current node is NOT a '<recv>' node
					else
					{
						//We add the current node to the nodes list
						nodes.add(element);
						//We apply the corresponding template file normally
						addNodeCheckParameters(nodes, rootElement, resultDocument,nodename, scenarioNum);
						//We clear the saved nodes list
						nodes.clear();
					}
					/* Check the children of the current SIPP element, 
					 * go through them and replace with the corresponding tags 
					 */
					for(Iterator j = element.elementIterator(); j.hasNext();)
					{
						ArrayList<Element> childNodes = new ArrayList<Element>(); 
						Element childNodeElement = (Element) j.next();
						String childName = childNodeElement.getName(); 
						if(childName.equals("action"))
						{	//Go through the children of the action tag
							for(Iterator k = childNodeElement.elementIterator(); k.hasNext();)
							{	
								Element childOfAction = (Element) k.next();
								childNodes.add(childOfAction);
								addNodeWithParameters(childNodes, rootElement, resultDocument, childOfAction.getName(), scenarioNum); 
								childNodes.clear();
							}
						}
						else
						{
							childNodes.add(childNodeElement); 
							addNodeWithParameters(childNodes, rootElement, resultDocument,childName, scenarioNum);
							childNodes.clear();
						}
					}
				}
				//Function to write the result, to the resulting XML file
				rightFinalResult(resultDocument, result);
				
				//Function to replace strings in a file
				replaceInFile(result, "&lt;", "<");
				replaceInFile(result, "&gt;", ">");
				replaceInFile(result, "&#13;", "");
				
				/*
				 * Get the file's location path, and copy all .csv files to the same directory of the 
				 * resulting MTS XML file. 
				 * If the file in argument does not contain a path, we get the current directory path.
				 */
				if(sippfile.contains("\\"))
				{
					int indexOfLast = sippfile.lastIndexOf("\\"); 
					checkIfFileUsed(sippfile.substring(0, indexOfLast), testfile.substring(0,testfile.lastIndexOf("\\")), filepath);
				}
				else
				{
					String currentDirectory = System.getProperty("user.dir");
					checkIfFileUsed(currentDirectory, testfile.substring(0,testfile.lastIndexOf("\\")), filepath);
				}
				
				//Write 'DONE' on the system out when finished
				System.out.println("============================================\nALL DONE!");
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
			log(ioe, "ERROR", "IO Exception");           
		}
	 }
	
	/**
	 * Same as 'addNode' functionality but for the 'if_receive' templates
	 * @param nodes
	 * @param main_root
	 * @param doc2
	 * @param templateFile
	 * @throws SAXException
	 * @throws IOException
	 * @throws DocumentException
	 */
	public static void addNodeWithParameters(ArrayList<Element> sippNode, Element resultDocRoot, Document resultDoc, String templateFile, int scenarioNum) throws SAXException, IOException, DocumentException{	
		SAXReader reader = new SAXReader();
		reader.setEntityResolver(new XMLLoaderEntityResolver());		
		Document template = reader.read("../conf/importsipp/Templates/"+templateFile+".xml");
		Element templateRoot = template.getRootElement();
		if(sippNode.size()>0)
			resultDocRoot.addComment(sippNode.get(0).getName());
		
		for(int n = 0; n<sippNode.size(); n++)
		{
            Element sippNoeud = sippNode.get(n);
            ArrayList<Element> sippNodeList = new ArrayList<Element>(); 
            sippNodeList.add(sippNoeud);
			for (Iterator i = templateRoot.elementIterator();i.hasNext();) 
			{
				Element templateElement = (Element) i.next();
				writeNodes(resultDoc,templateElement,sippNodeList,resultDocRoot,scenarioNum);
			}
		}
		//For global parameters
		if(sippNode.isEmpty())
		{
			for (Iterator i = templateRoot.elementIterator();i.hasNext();) 
			{
				Element templateElement = (Element) i.next();
				writeNodes(resultDoc,templateElement,sippNode,resultDocRoot,scenarioNum);
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
	public static void addNodeCheckParameters(ArrayList<Element> sippNode, Element resultDocRoot, Document resultDoc, String templateFile, int scenarioNum) throws SAXException, IOException, DocumentException{		
		//Parsing the corresponding template file
	 	SAXReader reader = new SAXReader();
	 	reader.setEntityResolver(new XMLLoaderEntityResolver()); 	
	 	Document template = reader.read("../conf/importsipp/Templates/"+templateFile+".xml");
		Element templateRoot = template.getRootElement(); 
		if(sippNode.size()>0)//if scenario
			resultDocRoot.addComment(sippNode.get(0).getName());
		
		//Run through all template elements
		for (Iterator i = templateRoot.elementIterator();i.hasNext();) 
		{
			Element templateElement = (Element) i.next();
			if(templateElement.getName().equals("parameter") && !sippNode.get(0).getName().equals("recvCmd")
					&& !sippNode.get(0).getName().equals("sendCmd"))
			{
				if(checkParameter(sippNode, templateElement))
					writeNodes(resultDoc,templateElement,sippNode,resultDocRoot,scenarioNum);
			}
        	else
        		writeNodes(resultDoc,templateElement,sippNode,resultDocRoot,scenarioNum);
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Check if the parameter already exists in later parts of the file
	 * @param sippNode
	 * @param templateElement
	 * @return
	 */
	public static boolean checkParameter(ArrayList<Element> sippNode, Element templateElement)
	{	
		List<Attribute> elementAttributes = templateElement.attributes();
		for(int i=0; i<elementAttributes.size();i++)
		{
			if(elementAttributes.get(i).getValue().contains("regex") || 
					elementAttributes.get(i).getValue().contains("xpath") || 
					elementAttributes.get(i).getValue().contains("integer"))
			{
				return true; 
			}
		}
		for(int k = 0; k < sippNode.size(); k++ )
		{
			if(sippNode.get(k).getStringValue().contains(templateElement.attribute("name").getValue()))
			{
				return true; 
			}
		}
		for(int j=0; j<sippNode.get(sippNode.size()-1).attributes().size();j++)
		{
			Attribute att =  (Attribute) sippNode.get(sippNode.size()-1).attributes().get(j); 
			if(att.getValue().contains(templateElement.attribute("name").getValue()))
			{
				return true; 
			}
		}
		
		return false; 
	}
	
	/**
	 * Get all the special expressions (XPATH, REGEX, INTEGER,...)in the XML file 
	 * @param attribute
	 * @param sippNode
	 * @param resultDocument
	 * @param att
	 * @param scenarioNum
	 */
	public static void getSpecialExpression(Attribute attribute, ArrayList<Element> sippNode, Document resultDocument, Map<String,ArrayList<String>> att, int scenarioNum)
	{	
		//Get each attribute value & name
		String attributeValue = attribute.getValue(); 
		String attributeName = attribute.getName(); 
		
		//If the value contains a special expression
		int getRegexBeginning = 0; 
		
		while(getRegexBeginning >= 0)
		{	
			getRegexBeginning = attributeValue.indexOf("{",getRegexBeginning);
			if(getRegexBeginning <0 )
				break; 
			int getRegexEnd = attributeValue.indexOf("}", getRegexBeginning);
			if(getRegexEnd < 0)
			{
				log(new Exception(), "ERROR", "Template syntax error: '}' is missing");
				System.exit(-1); 
			}
			//Save the expression value to replace it with the apply result later
			String expressionValue = attributeValue.substring(getRegexBeginning, getRegexEnd+1);
			//If it's an XPATH
			if(expressionValue.startsWith("{xpath:"))
				
			{	//Go through the nodes in the nodes list
				for(int j=0; j<sippNode.size(); j++)
				{
					//Get the XPATH expression
			    	XPath xpath = resultDocument.createXPath(expressionValue.substring(7, expressionValue.length()-1));
			    	//Get the XPath returning value
			    	Object obj = xpath.evaluate(sippNode.get(j));
			    	
			    	//if the OBJ is an attribute
			    	  if(obj instanceof Attribute)
			    	  {
			    		  /*If the XPath is an instance of an Attribute we create a list for all XPATH
			    		   * values, and we add every XPATH attribute found, to it. 
			    		   */
			    		  ArrayList<String> list = att.get(attributeName+getRegexBeginning); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeName+getRegexBeginning,list); 
			    		  }
			    		  list.add(((Attribute) obj).getValue()); 
			    	  }
			    	  //If the OBJ returns an Element
			    	  else if(obj instanceof Element)
			    	  {
			    		  ArrayList<String> list = att.get(attributeName+getRegexBeginning); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeName+getRegexBeginning,list); 
			    		  }
			    		  list.add(((Element) obj).getName()); 
			    	  }
			    	  //If the OBJ returns double
			    	  else if(obj instanceof Double)
			    	  {
			    		  ArrayList<String> list = att.get(attributeName+getRegexBeginning); 
			    		  if(list == null)
			    		  {
			    			  list = new ArrayList<String>() ; 
			    			  att.put(attributeName+getRegexBeginning,list); 
			    		  }
		    			  list.add(((Double) obj).toString()); 
			    	  }
				}
			}
			//If it's a REGEX
			else if (expressionValue.startsWith("{regex:"))
			{	
				for(int j=0; j<sippNode.size(); j++)
				{
					Pattern last = Pattern.compile(expressionValue.substring(7,expressionValue.length()-1));
					Matcher variableMatcher = last.matcher(sippNode.get(j).getText());
					while (variableMatcher.find())  
					{ 	
		    		  ArrayList<String> list = att.get(attributeName+getRegexBeginning); 
		    		  if(list == null)
		    		  {
		    			  list = new ArrayList<String>() ; 
		    			  att.put(attributeName+getRegexBeginning,list); 
		    		  }
		    		  if(variableMatcher.groupCount()>=1)
		    			  list.add(variableMatcher.group(1)); 
					}
				}
			}
			//If it's an integer
			else if(expressionValue.startsWith("{integer"))
			{
				//xpathExists= true;
				ArrayList<String> list = att.get(attributeName+getRegexBeginning);
				 if(list == null)
	    		 {
	    			  list = new ArrayList<String>() ;
	    			  att.put(attributeName+getRegexBeginning,list); 
	    		 }
				 list.add(new Integer(scenarioNum).toString());
			}
			
			getRegexBeginning = getRegexEnd + 1; 
		}
	}
	/**
	 * xPath function, permits to add a node, manipulate attributes containing XPATH expressions or not, 
	 * manipulate children and adding them to the resulting XML file
	 * @param resultDocument
	 * @param currentTemplateNode
	 * @param sippNode
	 * @param resultDocumentRoot
	 */
	@SuppressWarnings("unchecked")
	public static void writeNodes(Document resultDocument, Element currentTemplateNode, ArrayList<Element> sippNode, Element resultDocumentRoot, int scenarioNum)
	{			
		Map<String,ArrayList<String>> att = new HashMap<String, ArrayList<String>>();
		Element newelement = null;
		boolean specialExpressionExists = false; 
		
		//Run through all attributes of the current template element
		for (Iterator i = currentTemplateNode.attributeIterator();i.hasNext();) 
		{
			Attribute attribute = (Attribute) i.next();
			//Get the corresponding results for every special expression
			getSpecialExpression(attribute, sippNode, resultDocument, att, scenarioNum); 
		}
	  	/*
	  	 * We go through the results of the getXpath function and we get the maximum size of all arrays
	  	 * (these arrays show how many values the special expression has returned) 
	  	 */
		int max = 0 ;
		for (ArrayList<String> value : att.values()) 
		{
			if(value.size()>max)
			{
				max = value.size();
			}
		}
		
		for(int k =0; k<max; k++)
		{	
			/*
			 * Create a new element in the result document with the same name as the current template
			 * node's name 
			 */
			newelement = resultDocumentRoot.addElement(currentTemplateNode.getName());
			/*-------------------------------------------------------------------------
			  *	Logging and Writing INFO 
			 */
				Exception e = new Exception(); 
				String message = "------> Adding element : <"+ newelement.getName()+">"; 											
				log(e, "INFO", message); 
			 /* -------------------------------------------------------------------------*/
			for (Iterator j = currentTemplateNode.attributeIterator();j.hasNext();) 
			{
				/*We get the current TEMPLATE node attributes, and we create an equivalent attribute
				 * for the new element created, with the same attribute name and value. 
				 */
				Attribute currentTemplateAttribute = (Attribute) j.next();
				String attributeValue = currentTemplateAttribute.getValue();
				String attributeName = currentTemplateAttribute.getName(); 
				ArrayList <String> attributeToCreate = null; 
				String replaceResult = attributeValue; 
				int getRegexBeginning = 0;
				/*
				 * Search for and get the expression that we want to replace.
				 */
				while(getRegexBeginning >= 0)
				{	
					getRegexBeginning = attributeValue.indexOf("{",getRegexBeginning);
					if(getRegexBeginning <0 )
						break; 
					int getRegexEnd = attributeValue.indexOf("}", getRegexBeginning);
					//The special expression (regex, xpath...) we want to replace
					String regexValue = attributeValue.substring(getRegexBeginning, getRegexEnd+1);
					attributeToCreate = att.get(attributeName+getRegexBeginning);
					/*
					 * If the attribute is found in the list, we replace the attribute with the expression
					 * result
					 */
					if(attributeToCreate != null && attributeToCreate.size() > k) 
						replaceResult = replaceResult.replace(regexValue, attributeToCreate.get(k));
					//Go to the next special expression
					getRegexBeginning = getRegexEnd + 1 ;
				}
				if(attributeToCreate != null)
				{	//Adding the attribute to the element after the element being created
					if(attributeToCreate.size() > k) 
						newelement.addAttribute(currentTemplateAttribute.getName(),replaceResult); 
				}
				else
				{	/*
					* We add all other attributes that do not contain a special expression, to the new
					* element
					*/
					if(!currentTemplateAttribute.getValue().contains("xpath") 
							&& !currentTemplateAttribute.getValue().contains("regex")
							&& !currentTemplateAttribute.getValue().contains("integer"))
						newelement.addAttribute(currentTemplateAttribute.getName(),attributeValue);
				}
			}
		}
		/*
		 * For all the elements that contain no attributes with special expressions, we create the
		 * element again with the same attributes and values
		 */
		List<Attribute> templateAttributes = currentTemplateNode.attributes();
		for(int i=0; i<templateAttributes.size();i++)
		{
			if(templateAttributes.get(i).getValue().contains("regex") ||
					templateAttributes.get(i).getValue().contains("xpath") || 
					templateAttributes.get(i).getValue().contains("integer"))
			{
				specialExpressionExists = true; 
			}
		}
		if(!specialExpressionExists)
		{
			newelement = resultDocumentRoot.addElement(currentTemplateNode.getName());	
			newelement.setAttributes(currentTemplateNode.attributes());
		}
		/*
		 * If the TEMPLATE node contains an 'XPATH' in the CONTENT, we have to apply this XPATH 
		 * and add it to the new element's content
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
				if(newelement != null)
					newelement.addText("\n"+resultat.getValue());
	    	 }

			else if(obj instanceof ArrayList)
	    	  {
				ArrayList cdataResult = null ; 
				cdataResult = (ArrayList) obj ; 
				DefaultCDATA result = (DefaultCDATA) cdataResult.get(1); 
				if(newelement != null)
					newelement.addCDATA(result.getText());
	    	  }
		}
		//If not, we add the same content as the original sippNode
		else
		{
			if(newelement != null && !currentTemplateNode.getText().isEmpty())
				newelement.addText(currentTemplateNode.getText());
		}
		//Do all the work again, for the template element child
		if(newelement != null)
			for (Iterator k = currentTemplateNode.elementIterator(); k.hasNext();)
		    {	
				Element childTemplate = (Element) k.next();
				writeNodes(resultDocument, childTemplate, sippNode,newelement, scenarioNum); 
		    }
	}
		

	/**
	 * This function creates the Test file to be used by MTS.  
	 * @param testFileName
	 * @param testName
	 * @param inputFileName
	 */
	public static int createTestFile(String testFileName,String testName, String outputFileName, String testcase){	
		boolean scenarioExists = false ;
		boolean testcaseExists = false; 
		int numScenario = 0; 
		//Get the fileName from the path passed in arguments
		String fileName = outputFileName.substring(outputFileName.lastIndexOf("\\")+1);
		try {	
				boolean fileExists = (new File(testFileName)).exists();
				//If the file exists, add the element to it's corresponding place in the file
				if (fileExists) 
				{	
					//Read the test file
					SAXReader reader = new SAXReader();
					reader.setEntityResolver(new XMLLoaderEntityResolver());					
					Document doc = reader.read(testFileName);
					Element root = doc.getRootElement();
					Element testCaseMain = null ; 
					//Go through the test file elements
					for(Iterator j = root.elementIterator(); j.hasNext();)
					{
						testCaseMain = (Element) j.next();
						/*
						 * If the testName passed in arguments already exists in the test file we add the
						 * scenario in this test tag 
						 */
						
						if(testCaseMain.attribute("name").getValue().equals(testName))
						{	
							testcaseExists = true; 
							for (Iterator i = testCaseMain.elementIterator(); i.hasNext();) 
							{
								/*
								 * If the scenario (with the fileName) already exists in the test file
								 * we don't add the scenario to the test tag
								 */
								Element scenario = (Element) i.next();
								if(scenario.getStringValue().equals(testcase+"/"+fileName))
									scenarioExists = true;  
							}
							break;
						}
					}
					//If test tag already exists , to assure not duplicating the tags
					if(testcaseExists == true)
					{
						if(scenarioExists == false)
						{	
							//Add the scenario
							numScenario = testCaseMain.elements("scenario").size();
							Element scenario2 = testCaseMain.addElement("scenario");
							scenario2.addAttribute("name", "[localPort("+numScenario+")]");
							scenario2.addText(testcase+"/"+fileName);
						}
						else
							//Scenario already existing, so we get it's position in the test tag
							for (int i = 0;  i<testCaseMain.elements().size(); i++) 
							{	
								Element scenarioExisting = (Element) (Element)testCaseMain.elements().get(i);  
								if(scenarioExisting.getText().equals(testcase+"/"+fileName))
									numScenario = i ;
							}
					}
					/*
					 * If the file exists and the test case tag doesn't exist, add the tag, the elements
					 * 
					 */
					else
					{
						Element testcase2 = root.addElement("testcase"); 
						testcase2.addAttribute("name", testName);
						testcase2.addAttribute("description", "test sip"); 
						testcase2.addAttribute("state", "true");
						ArrayList<Element> nodes = new ArrayList<Element>(); 
						addNodeWithParameters(nodes, testcase2, doc, "runprofile", 0);
						Element scenario = testcase2.addElement("scenario");
						scenario.addAttribute("name", "[localPort("+numScenario+")]");
						scenario.addText(testcase+"/"+fileName);
					}						
					rightFinalResult(doc, testFileName);
				}
				//If the file doesn't exist, create it and add all the elements & the scenario 
				else 
				{
					Document doc = DocumentHelper.createDocument();
					Element rootElement = doc.addElement("test");
					rootElement.addAttribute("name", "importsipp");
					rootElement.addAttribute("description", "imported from sipp scenario");
					ArrayList<Element> nodes = new ArrayList<Element>(); 
					addNodeWithParameters(nodes, rootElement, doc, "testSuite", 0);
					Element testCase = rootElement.addElement("testcase");
					testCase.addAttribute("name", testName);
					testCase.addAttribute("description", "test sip"); 
					testCase.addAttribute("state", "true");
					
					ArrayList<Element> nodes2 = new ArrayList<Element>(); 
					addNodeWithParameters(nodes2, testCase, doc, "runprofile", 0);
					
					Element scenario = testCase.addElement("scenario");
					scenario.addAttribute("name", "[localPort("+numScenario+")]");
					scenario.addText(testcase+"/"+fileName);
					rightFinalResult(doc, testFileName);
				}
			}
		catch (Exception e) {
			log(e, "error", "Document exception"); 
		}
		return numScenario; 
	}
	
	/**
	 * Function that defines the usage for the user, in case of lack in arguments
	 * @param message
	 */
	static public void usage(String message) {
        System.out.println(message);
        System.out.println("Usage: importSipp -sippfile <inputFileName> (Mandatory)| " +
        		"-result <MTSFileName> (Optional)| " +
        		"-testfile <TestFileName> (Optional)| " +
        		"-testcase <testCaseName> (Optional)\n");
        System.exit(10);
    }
	
	/**
	 * Function that writes in the log file
	 * @param e
	 * @param level
	 * @param message
	 */
	static public void log(Exception e, String level, String message){
	    
			if (level.toUpperCase().equals("ERROR")) {
	        	e.printStackTrace();
	        	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "importSipp: "+message);
	        	System.out.println(message);
	        }
	        else if (level.toUpperCase().equals("WARN")) {
	        	e.printStackTrace();
	        	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "importSipp: "+message);
	        	System.out.println(message);
	        }

	        else if (level.toUpperCase().equals("DEBUG")) {
				GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "importSipp: "+message);
		    }
	        else if (level.toUpperCase().equals("INFO")) {
	        	GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "importSipp: "+message);
	        	System.out.println(message);
	        }
	}
	
	/**
	 * Get all the .csv files existing in the same directory of the SIPP xml file,
	 * checks the ones used in the SIPP scenario, and call the copyFile function to copy them to the 
	 * same directory of the resulting MTS XML.  
	 * @param sippFileLocation
	 * @param TestFileLocation
	 * @throws IOException
	 */
	public static void checkIfFileUsed(String sippFileLocation, String TestFileLocation,String sippFile) throws IOException
	{
		//Get the location folder of the SIPP file
		File folder = new File(sippFileLocation);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			  if (listOfFiles[i].isFile() && listOfFiles[i].toString().endsWith(".csv") ||
					  listOfFiles[i].isFile() && listOfFiles[i].toString().endsWith(".pcap")) {
				  String originFile = sippFileLocation+"\\"+listOfFiles[i].getName();
				  String destFile = TestFileLocation+"\\"+listOfFiles[i].getName(); 
				  /*
				   * If the .csv file is used in the SIPP XML file we copy it, if not we don't need to 
				   * copy it.
				   */
				  Scanner in = null;
				  boolean result = false;
			        try {
			            in = new Scanner(new FileReader(sippFile));
			            while(in.hasNextLine() && !result) {
			                result = in.nextLine().indexOf(listOfFiles[i].getName()) >-1;
			            }
			        }
			        catch(IOException e) {
			            e.printStackTrace();      
			        }
			        if(result)
			        {
			        	copyFile(originFile,destFile);
			        }
			  } 
		}
	}
	/**
	 * Copy a file from a source path to a destination path
	 * @param SourceFile
	 * @param NewDestFile
	 * @throws IOException
	 */
	public static void copyFile(String SourceFile, String NewDestFile) throws IOException
	{	
		File nomFichier = new File(SourceFile);
		Scanner inputFile = new Scanner(nomFichier);
		
		PrintWriter outputFile = new PrintWriter(NewDestFile);
		while (inputFile.hasNext())
		{
			outputFile.println(inputFile.nextLine());
		}
		outputFile.close(); 
		inputFile.close();
	}
        	
}