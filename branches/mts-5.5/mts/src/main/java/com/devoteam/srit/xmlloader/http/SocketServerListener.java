/*
 * SocketServerHttpListener.java
 *
 * Created on 26 juin 2007, 09:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.http;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

/**
 *
 * @author sngom
 */
public abstract class SocketServerListener
{
    protected boolean secure;

    /** Creates a new instance of SocketServerHttpListener */
    public SocketServerListener(boolean secure) throws ExecutionException
    {
        this.secure = secure;
    }

    public abstract void shutdown();
}
