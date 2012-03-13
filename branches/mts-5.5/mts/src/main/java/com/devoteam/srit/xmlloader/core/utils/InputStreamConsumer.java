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
