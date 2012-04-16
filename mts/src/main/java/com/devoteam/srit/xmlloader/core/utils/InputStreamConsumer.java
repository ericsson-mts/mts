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

package com.devoteam.srit.xmlloader.core.utils;

import java.io.InputStream;
import java.util.concurrent.Semaphore;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

public class InputStreamConsumer extends Thread{
	
	private InputStream inputStream;
    
    private Semaphore semaphore;
        
    private String contents;
    
    public InputStreamConsumer(InputStream anInputStream)
    {
        semaphore = new Semaphore(0);
        inputStream = anInputStream;
        contents = "";
        start();
    }
    
    @Override
    public void run()
    {
    	try
        {
        	byte[] buffer = new byte[256];
            int len;
            while(-1 != (len = inputStream.read(buffer)))
            {
            	String bufferStr = new String(buffer,0, len);
                contents += bufferStr;
            }
        }
        catch(Exception e)
        {
        	System.out.println("error developpement" + e);
        }
        semaphore.release();
    }
    
    public String getContents()
    {
    	return contents;
    }
    
    public void acquire() throws Exception
    {
        semaphore.acquire();
    }
}
