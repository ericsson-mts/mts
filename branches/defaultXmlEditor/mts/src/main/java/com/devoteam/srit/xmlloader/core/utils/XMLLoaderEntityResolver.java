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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * This class is used to set a correct path to the dtd in the xml files.
 * @author gpasquiers
 */
public class XMLLoaderEntityResolver implements EntityResolver, Serializable
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
