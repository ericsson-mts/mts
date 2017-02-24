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
