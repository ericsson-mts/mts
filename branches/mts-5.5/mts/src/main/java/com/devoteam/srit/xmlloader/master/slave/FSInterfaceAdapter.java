/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.slave;

import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.RemoteException;

/**
 *
 * @author Gwenhael
 */
public class FSInterfaceAdapter implements FSInterface{
    FSInterface _fsInterface;
    
    public FSInterfaceAdapter(FSInterface fsInterface){
        _fsInterface = fsInterface;
    }
    
    @Override
    public InputStream getInputStream(URI path) throws RemoteException {
        byte[] array = this.getBytes(path);
        return new ByteArrayInputStream(array);
    }

    @Override
    public boolean exists(URI path) throws RemoteException {
        return _fsInterface.exists(path);
    }

    @Override
    public boolean isFile(URI path) throws RemoteException {
        return _fsInterface.isFile(path);
    }

    @Override
    public boolean isDirectory(URI path) throws RemoteException {
        return _fsInterface.isDirectory(path);
    }

    @Override
    public byte[] getBytes(URI path) throws RemoteException {
        return _fsInterface.getBytes(path);
    }

    @Override
    public String[] list(URI path) throws RemoteException {
        return _fsInterface.list(path);
    }

    @Override
    public OutputStream getOutputStream(URI path) throws RemoteException {
        throw new UnsupportedOperationException("Not supported for remote interfaces.");
    }
    
}
