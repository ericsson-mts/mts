package com.devoteam.srit.xmlloader.importsipp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StartSipP {
	/**
	 * @param args
	 */
	public static void main(String... args) {
		 
		String allOptions = ""; 
		String fileName = null; 
		List<String> options = new ArrayList<String>() ; 
		
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
        		fileName = "../conf/importsipp/test_"+args[i+1]+".xml"; 
        	}
        	if(args[i].equals("-sf"))
        	{	
        		int sippFileNamePosition1 = args[i+1].lastIndexOf("\\");
    			int sippFileNamePosition2 = args[i+1].lastIndexOf(".");
    			String sippFile = args[i+1].substring(sippFileNamePosition1+1, sippFileNamePosition2);
    			fileName = "../conf/importsipp/test_"+sippFile+".xml";  
        	}
        	if(args[i].equals("-base_cseq"))
        	{
        		options.add("-param:cseq+"+args[i+1]); 
        	}
        	if(args[i].equals("-bind_local") || args[i].equals("-ci") || args[i].equals("-i"))
        	{
        		options.add("-param:local_ip+"+args[i+1]); 
        	}
        	if(args[i].equals("-cp") || args[i].equals("-p"))
        	{
        		options.add("-param:localPort+"+args[i+1]); 
        	}
        	if(args[i].equals("-d"))
        	{
        		options.add("-param:_pauseDuration+"+args[i+1]); 
        	}
        	if(args[i].equals("-inf"))
        	{
        		options.add("-param:_fileName+"+args[i+1]); 
        	}
        	if(args[i].equals("-l") || args[i].equals("-users"))
        	{
        		options.add("-param:instances+"+args[i+1]); 
        	}
        	if(args[i].equals("-mi"))
        	{
        		options.add("-param:media_ip+"+args[i+1]); 
        	}
        	if(args[i].equals("-mp"))
        	{
        		options.add("-param:auto_media_port+"+args[i+1]); 
        	}
        	if(args[i].equals("-r"))
        	{
        		options.add("-param:frequency+"+args[i+1]); 
        	}
        	if(args[i].equals("-recv_timeout"))
        	{
        		options.add("-param:recv_timeout+"+args[i+1]); 
        	}
        	if(args[i].equals("-sleep"))
        	{
        		options.add("-param:startDelay+"+args[i+1]); 
        	}
        	if(args[i].equals("-rsa"))
        	{
        		int positionOfhost = args[i+1].lastIndexOf(":");
        		options.add("-param:remote_ip+"+args[i+1].substring(0, positionOfhost)+
        				" -param:remotePort+"+args[i+1].substring(positionOfhost+1)); 
        	}
        	if(args[i].equals("-s"))
        	{
        		options.add("-param:service+"+args[i+1]);
        	}
        	if(args[i].equals("-t"))
        	{
        		options.add("-param:transport+"+args[i+1]);
        	}
        	if(args[i].equals("-timeout"))
        	{
        		options.add("-param:endDelay+"+args[i+1]);
        	}
        	if(args[i].equals("-set"))
        	{
        		options.add("-param:instances+"+args[i+1]+args[i+2]);
        	}
        	
        	if(i>0 && !args[i].startsWith("-")&& !args[i-1].startsWith("-"))
        	{
        		int positionOfHost = args[i].lastIndexOf(":"); 
        		options.add("-param:remote_ip+"+args[i].substring(0, positionOfHost)+
        				" -param:remotePort+"+args[i].substring(positionOfHost+1));
        	}
        }
      
        for(int j=0; j<options.size(); j++)
        {
        	allOptions = allOptions+" "+options.get(j); 
        }
        String commandTest = "cmd /c startCmd.bat " + fileName +" -load"+ allOptions; 
        
		//String command = "cmd /c startTest.bat";
        Process p;
		try 
		{
			p = Runtime.getRuntime().exec(commandTest);
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
