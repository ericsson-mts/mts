/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.api;

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerProvider;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URI;

/**
 *
 * @author gpasquiers
 */
public class DeserializatingLogger
{
    private ObjectInputStream objectInputStream;
    private TextListenerProvider textListenerProvider;
    
    public DeserializatingLogger(URI uri, TextListenerProvider textListenerProvider) throws Exception
    {
        this.objectInputStream = new ObjectInputStream(new FileInputStream(new File(uri)));
        this.textListenerProvider = textListenerProvider;
    }

    public void work() throws Exception
    {
        try
        {
            Object object;
            while((object = objectInputStream.readObject()) != null)
            {
                textListenerProvider.provide(null).printText((TextEvent) object);
            }
        }
        catch(EOFException e)
        {
            textListenerProvider.provide(null).dispose();
            // expected exception: end of file
        }
    }
}
