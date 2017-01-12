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
