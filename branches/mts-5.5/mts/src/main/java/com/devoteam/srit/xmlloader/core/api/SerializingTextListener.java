/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.api;

import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URI;

/**
 *
 * @author gpasquiers
 */
public class SerializingTextListener implements TextListener
{
    private ObjectOutputStream objectOutputStream;

    public SerializingTextListener(URI uri) throws Exception
    {
        File file = new File(uri);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        this.objectOutputStream = new ObjectOutputStream(fileOutputStream);
    }

    public void printText(TextEvent e)
    {
        try
        {
            this.objectOutputStream.writeObject(e);
        }
        catch(Exception ee)
        {
            throw new RuntimeException(ee);
        }
    }

    public void dispose()
    {
        try
        {
            this.objectOutputStream.close();
        }
        catch(Exception ee)
        {
            throw new RuntimeException(ee);
        }    }

}
