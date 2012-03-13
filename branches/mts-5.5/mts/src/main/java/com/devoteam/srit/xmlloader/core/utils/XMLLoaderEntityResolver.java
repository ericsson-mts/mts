/*
 * XMLLoaderEntityResolver.java
 *
 * Created on 15 mars 2007, 12:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This class is used to set a correct path to the dtd in the xml files.
 * @author gpasquiers
 */
public class XMLLoaderEntityResolver implements EntityResolver
{

    private static String keyword_home = "[XML_LOADER_HOME]";

    private URI base;
    
    public XMLLoaderEntityResolver()
    {
        this(null);
    }

    public XMLLoaderEntityResolver(URI base)
    {
        this.base = base;
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
    {
        // replace keyword with path to a dummy , empy, dtd file
        if (systemId.indexOf(keyword_home) != -1)
        {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
        else if(null != base)
        {
            URI resource = base.resolve(systemId);
            try
            {
                InputStream in = SingletonFSInterface.instance().getInputStream(resource);
                if(null == in)
                {
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "Exception : Include the XML file : ", resource);
                }
                return new InputSource(in);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error reading resource " + systemId + " resolved as " + resource , e);
            }
        }
        else
        {
            return null;
        }
    }
}
