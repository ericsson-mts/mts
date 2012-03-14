/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.master.master;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.filesystem.FSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Re-using the already existing FSInterface
 * @author gpasquiers
 */
public class FSInterfaceImpl extends UnicastRemoteObject implements FSInterface {

    public FSInterfaceImpl() throws RemoteException {
    }

    @Override
    public InputStream getInputStream(URI path) throws RemoteException {
        throw new UnsupportedOperationException("Not supported in remote FS.");
    }

    @Override
    public boolean exists(URI path) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if file ", path, " exists");
        return SingletonFSInterface.instance().exists(path);
    }

    @Override
    public boolean isFile(URI path) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if ", path, " is a file");
        return SingletonFSInterface.instance().isFile(path);
    }

    @Override
    public boolean isDirectory(URI path) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked if ", path, " is a directory");
        return SingletonFSInterface.instance().isDirectory(path);
    }

    @Override
    public byte[] getBytes(URI path) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked byte array for file ", path);
        return SingletonFSInterface.instance().getBytes(path);
    }

    @Override
    public String[] list(URI path) throws RemoteException {
        GlobalLogger.instance().getApplicationLogger().debug(Topic.CORE, "FSInterface: slave asked list of files for ", path);
        return SingletonFSInterface.instance().list(path);
    }

    @Override
    public OutputStream getOutputStream(URI path) {
        throw new UnsupportedOperationException("Not supported in remote FS.");
    }
}
