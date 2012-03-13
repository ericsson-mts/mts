/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.filesystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author gpasquiers
 */
public interface FSInterface extends Remote
{
    public InputStream getInputStream(URI path) throws RemoteException;

    public boolean exists(URI path) throws RemoteException;

    public boolean isFile(URI path) throws RemoteException;

    public boolean isDirectory(URI path) throws RemoteException;

    public byte[] getBytes(URI path) throws RemoteException;

    public String[] list(URI path) throws RemoteException;

    public OutputStream getOutputStream (URI path) throws RemoteException;
}
