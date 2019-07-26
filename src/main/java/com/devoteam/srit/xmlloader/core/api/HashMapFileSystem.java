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

package com.devoteam.srit.xmlloader.core.api;

import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author gpasquiers
 */
public class HashMapFileSystem implements FSInterface
{
    private HashMap<URI, byte[]> fileSystem;

    public HashMapFileSystem()
    {
        this.fileSystem = new HashMap();
    }

    public InputStream getInputStream(URI path) throws RemoteException
    {
        return new ByteArrayInputStream(this.getBytes(path));
    }

    public boolean exists(URI path) throws RemoteException
    {
        return this.fileSystem.containsKey(absolutise(path));
    }

    public boolean isFile(URI path) throws RemoteException
    {
        return !this.isDirectory(path);
    }

    public boolean isDirectory(URI path) throws RemoteException
    {
        return path.getPath().endsWith("/");
    }

    public byte[] getBytes(URI path) throws RemoteException
    {
        byte[] data = this.fileSystem.get(absolutise(path));
        if(null != data)
        {
            return data;
        }
        throw new RemoteException("unknown file " + absolutise(path));
    }

    public String[] list(URI path) throws RemoteException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public OutputStream getOutputStream (URI path){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void loadFileFromDisk(URI uri) throws Exception
    {
        File file = new File(uri);
        FileInputStream stream = new FileInputStream(file);
        if(file.length() > Integer.MAX_VALUE) throw new Exception("file is too big. Max is " + Integer.MAX_VALUE + " octets");
        byte[] data = new byte[(int) file.length()];
        stream.read(data);
        if( -1 != stream.read()) throw new Exception("Mismatch between byte array and read size");
        this.fileSystem.put(absolutise(uri), data);
    }

    public void loadDirFromdisk(URI uri) throws Exception
    {
        File dir = new File(uri);
        for(File child:dir.listFiles())
        {
            if(child.isDirectory())
            {
                loadDirFromdisk(uri.resolve(child.toURI()));
            }
            else
            {
                loadFileFromDisk(uri.resolve(child.toURI()));
            }
        }
    }

    public void addFile(URI uri, String file)
    {
        this.fileSystem.put(absolutise(uri), file.getBytes());
    }


    public void dumpToDisk(URI baseURI) throws Exception
    {
        if(!baseURI.isAbsolute()) throw new Exception("non-absolute URI in hashmap ! ! !");
        
        for(Entry<URI, byte[]> entry:this.fileSystem.entrySet())
        {
            URI targetURI = baseURI.resolve("." + entry.getKey().getRawPath().replace(":", ""));

            File file = new File(targetURI);
            if(!file.exists())
            {
                file.mkdirs();
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(entry.getValue());
        }
    }

    private URI absolutise(URI uri)
    {
        if(!uri.isAbsolute())
        {
            uri = URIRegistry.MTS_BIN_HOME.resolve(uri);
        }
        return uri;
    }
}
