/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
