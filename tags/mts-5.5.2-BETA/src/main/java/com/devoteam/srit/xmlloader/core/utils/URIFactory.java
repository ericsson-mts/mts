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

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author gpasquiers
 */
public class URIFactory
{
    private static String doPath(String path)
    {
        path.replace('\\', '/');
        path.replace(" ", "%20");
        
        if(path.length()>1 && path.charAt(1) == ':') path = "/" + path;
        else if(path.startsWith("//")) while(!path.startsWith("////")) path = "/" + path;
        
        return path;
    }
    
    public static URI create(String path)
    {
        return URI.create(doPath(path));
    }

    public static URI newURI(String path) throws URISyntaxException
    {
        return new URI(doPath(path));
    }

    public static URI resolve(URI uri, String string)
    {
        return uri.resolve(doPath(string));
    }
}
