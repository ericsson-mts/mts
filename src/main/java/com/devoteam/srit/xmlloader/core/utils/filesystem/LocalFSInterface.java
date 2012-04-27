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

package com.devoteam.srit.xmlloader.core.utils.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.io.FileOutputStream;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

/**
 *
 * @author gpasquiers
 */
public class LocalFSInterface implements FSInterface
{
    private static URI userDir = new File(System.getProperty("user.dir")).toURI();
    
    private static URI desabolute(URI uri)
    {
        if(!uri.isAbsolute())
        {
            return userDir.resolve(uri);
        }
        else
        {
            return uri;
        }
    }
    
    public InputStream getInputStream(URI path)
    {
        path = desabolute(path);
        try
        {
            return new FileInputStream(new File(path));
        }
        catch(Exception e)
        {
        	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception : cannot find file ", path);
            return null;
        }
    }

    public boolean exists(URI path)
    {
        path = desabolute(path);
        return new File(path).exists();
    }

    public boolean isFile(URI path)
    {
        path = desabolute(path);
        return new File(path).isFile();
    }

    public boolean isDirectory(URI path)
    {
        path = desabolute(path);
        return new File(path).isDirectory();
    }

    public byte[] getBytes(URI path)
    {
        path = desabolute(path);
        try
        {
            File file = new File(path);
            byte[] array = new byte[(int) file.length()];
            FileInputStream fileInputStream = new FileInputStream(file);
            
            int currentLength = 0;
            while(currentLength != file.length())
            {
                int length = fileInputStream.read(array, currentLength, array.length - currentLength);
                if(length != -1)
                {
                    currentLength += length;
                }
                else
                {
                	Exception e = new Exception("Reached end of file before the end");
                	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Exception : Read the file : ", path);
                    break;
                }
            }
            
            fileInputStream.close();
            return array;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public String[] list(URI path)
    {
        path = desabolute(path);
        return new File(path).list();
    }

    public OutputStream getOutputStream (URI path){
        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(new File(path));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return fos;
    }
}
