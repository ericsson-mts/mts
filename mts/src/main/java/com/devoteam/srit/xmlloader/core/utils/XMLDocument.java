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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.LinkedList;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 *
 * @author Gege
 */
public class XMLDocument implements Serializable
{
    private URI schemaFile;
    private URI xmlFile;
    
    private Document document;
    
    /**
     * Constructor
     */
    public XMLDocument()
    {
        schemaFile = null;
        xmlFile = null;
        document = null;
    }
    
    /**
     * Set and retrieve the XML Schema for parsing
     * @param path
     * @throws ParsingException
     */
    public void setXMLSchema(URI path) throws Exception
    {
        path = URIRegistry.MTS_BIN_HOME.resolve(path);
        if(SingletonFSInterface.instance().exists(path))
        {
            this.schemaFile = path;
        }
        else
        {
            throw new ParsingException("Schema file does not exists: " + path);
        }
    }
    
    
    public URI getXMLSchema()
    {
        return schemaFile;
    }
    
    /**
     * Set and retrieve the XML File to parse
     * @param path
     * @throws ParsingException
     */
    public void setXMLFile(URI path) throws Exception
    {
        path = URIRegistry.MTS_BIN_HOME.resolve(path);
        if(SingletonFSInterface.instance().exists(path))
        {
            this.xmlFile = path;
        }
        else
        {
            throw new ParsingException("XML file does not exists: " + path);
        }
    }
    
    public URI getXMLFile()
    {
        return xmlFile;
    }

    /**
     * Open an inputStream and adds it to a list.
     * This list will be used to ensure all opened inputStreams are close
     * @param list
     * @param path
     * @return
     * @throws RemoteException
     */
    private InputStream openInputStream(LinkedList<InputStream> list, URI path) throws RemoteException
    {
        InputStream inputStream = SingletonFSInterface.instance().getInputStream(path);
        list.add(inputStream);
        return inputStream;
    }

    /**
     * Parses the XMLFile against the XMLSchema
     * @throws java.lang.ParsingException
     */
    public void parse() throws ParsingException
    {
        if(null == this.schemaFile)
        {
            throw new ParsingException("Schema file not setted");
        }
        
        if(null == this.xmlFile)
        {
            throw new ParsingException("XML file not setted");
        }

        final LinkedList<InputStream> streamsList = new LinkedList();

        try
        {
            //
            // create a SchemaFactory capable of understanding WXS schemas
            //
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            
            //
            // Load a XSD schema, represented by a Schema instance
            //
            Source schemaSource = new StreamSource(openInputStream(streamsList, this.schemaFile));

            final XMLDocument _this = this;
            
            factory.setResourceResolver(new LSResourceResolver()
            {
                public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
                {
                    if(!systemId.toLowerCase().endsWith(".xsd"))
                    {
                        return null;
                    }

                    final URI finalPath = URIFactory.resolve(_this.schemaFile,systemId);//"./" + schemaFile).substring(0, ("./" + schemaFile).lastIndexOf("/") + 1) + systemId;
                    return new LSInput() {
                        public InputStream getByteStream()
                        {
                            try
                            {
                                InputStream in = openInputStream(streamsList, finalPath);

                                if(null == in)
                                {
                                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Exception : Include the XML file : ", finalPath);
                                }
                                return in;
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                                return null;
                            }
                        }
                        
                        public Reader getCharacterStream()
                        {
                            return null;
                        }

                        public void setCharacterStream(Reader characterStream)
                        {
                        }

                        public void setByteStream(InputStream byteStream)
                        {
                        }

                        public String getStringData()
                        {
                            return "";
                        }

                        public void setStringData(String stringData)
                        {
                        
                        }

                        public String getSystemId()
                        {
                            return "";
                        }

                        public void setSystemId(String systemId)
                        {
                           
                        }

                        public String getPublicId()
                        {
                            return "";
                        }

                        public void setPublicId(String publicId)
                        {
                            
                        }

                        public String getBaseURI()
                        {
                            return "";
                        }

                        public void setBaseURI(String baseURI)
                        {
                        }

                        public String getEncoding()
                        {
                            return "UTF-8";
                        }

                        public void setEncoding(String encoding)
                        {
                        }

                        public boolean getCertifiedText()
                        {
                            return false;
                        }

                        public void setCertifiedText(boolean certifiedText)
                        {

                        }
                    };
                }
            });
            
            Schema schema = factory.newSchema(schemaSource);
            
            //
            // Create and configure the DocumentBuilderFactory
            //
            DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
            parserFactory.setSchema(schema);
            parserFactory.setValidating(false);
            parserFactory.setNamespaceAware(true);

            parserFactory.setCoalescing(true);
            
            //
            // Get the parser
            //
            DocumentBuilder parser = parserFactory.newDocumentBuilder();



            //
            // Parse to check document agains XSD
            //
            parser.setEntityResolver(new XMLLoaderEntityResolver(this.xmlFile));
            parser.setErrorHandler(new ErrorHandler(){

                public void warning(SAXParseException exception) throws SAXException
                {
                }

                public void error(SAXParseException exception) throws SAXException
                {
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException
                {
                    throw exception;
                }
            });
            
            parser.parse(openInputStream(streamsList, this.xmlFile), this.getXMLFile().toString());
            
            //
            // Instanciate the XML parser (no valididy check with dtd)
            //
            SAXReader reader = new SAXReader(false);
            reader.setEntityResolver(new XMLLoaderEntityResolver(this.xmlFile));
            document = reader.read(openInputStream(streamsList, this.xmlFile), this.getXMLFile().toString());
        }
        catch (SAXParseException e)
        {
            throw new ParsingException("In file : " + xmlFile + "\n" + "Parsing error at line " + e.getLineNumber() + ", column " + e.getColumnNumber() + "\n" + e.getMessage() ,e);
        }
        catch (Exception e)
        {
            throw new ParsingException(e);
        }
        finally
        {
            for(InputStream stream:streamsList)
            {
                try
                {
                    stream.close();
                }
                catch(Exception e)
                {
                    // ignore
                }
            }
        }
    }
    
    /**
     * Returns the document representing this parsed XMLDocument
     * @return Document
     * @throws java.lang.ParsingException
     */
    public Document getDocument() throws ParsingException
    {
        if(null == document)
        {
            throw new ParsingException("Call parse() before getDocument()");
        }
        
        return document;
    }
    
    /**
     * Remove recursively all "whitespaces" text nodes.
     * @throws java.lang.ParsingException
     */
    public void clean() throws ParsingException
    {
        //    removeWhistespacesTextNodes(getDocument());
    }
    
    /**
     * Recursively remove text nodes containing white spaces
     */
    private void removeWhistespacesTextNodes(Node node)
    {
        if(null == node)
        {
            return;
        }
        
        NodeList list = node.getChildNodes();
        
        for(int i=0; i<list.getLength(); i++)
        {
            Node child = list.item(i);
            if(Node.TEXT_NODE == child.getNodeType())
            {
                Text text = (Text) child;
                
                // if(text.isElementContentWhitespace())
                {
                    node.removeChild(child);
                    i--;
                }
            }
            else if(Node.ELEMENT_NODE == child.getNodeType())
            {
                removeWhistespacesTextNodes(child);
            }
        }
    }

    public XMLDocument duplicate() throws Exception
    {
        XMLDocument xmlDocument = new XMLDocument();
        xmlDocument.setXMLFile(this.getXMLFile());
        xmlDocument.setXMLSchema(this.getXMLSchema());
        xmlDocument.document = new DefaultDocument(this.document.getRootElement().createCopy());
        return xmlDocument;
    }
}
