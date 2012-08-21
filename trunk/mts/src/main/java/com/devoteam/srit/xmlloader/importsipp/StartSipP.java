package com.devoteam.srit.xmlloader.importsipp;

import java.io.InputStream;
import java.util.List;

public class StartSipP {
	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String... args) {
		 
		String allOptions = null; 
		String fileName = null; 
		List<String> options = null ; 
		
		/*
		 * Handle arguments
		 */
        if (args.length <= 0) {
            usage("At least one argument is required : the scenario file path");
        }
        for(int i=0; i<args.length; i++)
        {
        	/*
        	 * It's an option ! 
        	 */
        	if(args[i].equals("-sn"))
        	{
        		fileName = args[i+1]+".xml"; 
        	}
        	if(args[i].equals("-sf"))
        	{
        		fileName = args[i+1]; 
        	}
        	if(args[i].equals("-users"))
        	{
        		options.add("-instances"); 
        	}
        }
       
        for(int j=0; j<options.size(); j++)
        {
        	allOptions = allOptions+" "+options.get(j); 
        }
        String commandTest = "cmd /c startCmd.bat" + allOptions; 
        
		String command = "cmd /c startTest.bat";
        Process p;
		try 
		{
			p = Runtime.getRuntime().exec(command);
			InputStream input = p.getInputStream();
			InputStream errInput= p.getErrorStream();
			
			int data = input.read();
			while(data != -1) 
			{
				System.out.print((char)data);
				data = input.read();
			}
			
			data = errInput.read();
			while(data != -1) 
			{
				System.out.print((char)data);
				data = input.read();
			}
			
			p.waitFor();
			int exitValue = p.exitValue();
			System.exit(exitValue);
		}	
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public void usage(String message) {
        System.out.println(message);
        System.exit(10);
    }
}
