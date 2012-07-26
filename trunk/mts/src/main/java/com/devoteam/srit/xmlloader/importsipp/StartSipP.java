package com.devoteam.srit.xmlloader.importsipp;

import java.io.InputStream;

public class StartSipP {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String command = "cmd /c startCmd.bat"; 
        Process p;
		try 
		{
			p = Runtime.getRuntime().exec(command);
			InputStream input = p.getInputStream();
			InputStream errInput= p.getErrorStream();
        
			p.waitFor();
			int exitValue = p.exitValue();
			System.exit(exitValue);
		}	
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
