package com.devoteam.srit.xmlloader.importsipp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class StartSipP {
	/**
	 * @param args
	 */
	public static void main(String... args) {
		
		String fileName = null; 
		List<String> options = null ; 
		
		//
        // Handle arguments
        //
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
	
	public static void copy (String SourceFile, String NewDestFile) throws IOException
	//ouvre le fichier et copie le contenu du fichier dans un nouveau fichier 
	/*
	pre: SourceFile est initialisé et correspond au nom d'un fichier externe qui existe et est fermé,
	NewDestFile est initialisé et correspond au nom d'un fichier externe qui n'existe pas (s'il existe déjà le contenu du précédent fichier sera écrasé)
	post: SourceFile est inchangée et NewDestFile est le contenu de SourceFile et est fermé
	*/
	{	
		// je met SourceFile dans nomFichier
		File nomFichier = new File(SourceFile);
		//je met nomFichier dans inputfile
		Scanner inputFile = new Scanner(nomFichier);
		
		/*inputfile est initialisé,il est lié au fichier externe SourceFile et est ouvert en lecture*/
		PrintWriter outputFile = new PrintWriter(NewDestFile);
		
		//ouvre le fichier NewDestFile
		//écrit dans le fichier nexDestFile le contenu du fichier source
		while (inputFile.hasNext())//regarde si la ligne suivante existe
		{
			//inputfile.nextline() voir scanner et file au debut de la méthode
			outputFile.println(inputFile.nextLine());
		}
		
		//ferme le fichier en écriture
		outputFile.close(); 
		//inputfile est fermé
		inputFile.close();
	}


}
