/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
