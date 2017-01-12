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

package com.devoteam.srit.xmlloader.master.filesystem;

import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;

/**
 *
 * @author gpasquiers
 */
public class RemoteFSInterface implements FSInterface
{
    private FSInterface remoteFSInterface;
    
    public RemoteFSInterface(FSInterface remoteFSInterface)
    {
        this.remoteFSInterface = remoteFSInterface;
    }
    
    public InputStream getInputStream(URI path) throws RemoteException
    {
        byte[] array = this.getBytes(path);
        return new ByteArrayInputStream(array);
    }

    public boolean exists(URI path) throws RemoteException
    {
        return this.remoteFSInterface.exists(path);
    }

    public boolean isFile(URI path) throws RemoteException
    {
        return this.remoteFSInterface.isFile(path);
    }

    public boolean isDirectory(URI path) throws RemoteException
    {
        return this.remoteFSInterface.isDirectory(path);
    }

    public byte[] getBytes(URI path) throws RemoteException
    {
        return this.remoteFSInterface.getBytes(path);
    }

    public String[] list(URI path) throws RemoteException
    {
        return this.remoteFSInterface.list(path);
    }

    public OutputStream getOutputStream (URI path){
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
