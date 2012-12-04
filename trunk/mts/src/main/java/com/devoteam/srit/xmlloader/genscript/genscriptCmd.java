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

package com.devoteam.srit.xmlloader.genscript;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.TextExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;

/**
 * Command Line User Interface for genscript module
 * @author bthou
 */
public class genscriptCmd {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {    	

        // Initialisation de MTS core
        ExceptionHandlerSingleton.setInstance(new TextExceptionHandler());
        SingletonFSInterface.setInstance(new LocalFSInterface());
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());
        // set the storage location to FILE for logging
        PropertiesEnhanced properties = new PropertiesEnhanced();
        properties.addPropertiesEnhancedComplete("logs.STORAGE_LOCATION", "FILE");
        Config.overrideProperties("tester.properties", properties);
    	
        // S'il n'y a pas d'arguments, on affiche la syntaxe à utiliser
        if (args.length < 3) {
            System.out.println("ERROR => At least two arguments are required : filtre and capture file");
            System.out.println("Usage : genscript <protocol>:<host>:<port> <capturefile> <generatedfile>");
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, "ERROR => At least two arguments are required : filtre and capture file");
            System.exit(-1);
        }
        // Si le nombre d'argument est bon, on lance la convertion
        System.out.println("-------- GENSCRIPT -------------------------------------------------------");
        System.out.println("START => capture file: " + args[args.length - 2]);
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "START => capture file: " + args[args.length - 2]);

        List<FiltreGenerator> listeFiltre = new ArrayList<FiltreGenerator>();
        List<Probe> listeProbe = new ArrayList<Probe>();
        
        String src = "";
        URI srcURI = null;
        String out = "";
        URI outURI = null;
        String nomPcap = "";

        // Pour chaque élément passé en paramètre
        for (int i = 0; i < args.length; i++) {

            // Si l'argument est un filtre
            if (args[i].matches("[a-zA-Z0-9]+:[a-zA-Z0-9.]+:[0-9]+")) {
                String[] filtre = args[i].split(":");
                listeFiltre.add(new FiltreGenerator(filtre[0], filtre[1], Integer.parseInt(filtre[2])));
            }
        }

        // Création des URI à partir des paramètres
        // URI du fichier pcap source
        src = args[args.length - 2];
        srcURI = new File(src).toURI();

        // URI du fichier de test à générer
        out = args[args.length - 1];
        outURI = new File(out).toURI();

        // On récupère le nom du fichier pcap
        String[] nomPcapPath = args[args.length - 2].split("/");
        nomPcap = nomPcapPath[nomPcapPath.length - 1].replaceAll("[.]([0-9A-Za-z])+", "");

        // Creation du generateur de script
        ScriptGenerator generator = new ScriptGenerator(outURI);
        generator.setTestcaseName(nomPcap);
            
       try {
            // Chaque filtre est enregistré dans le générateur de script de test
            for (FiltreGenerator fg : listeFiltre) {
                generator.addFiltre(fg);
            }

            // Generation des fichiers de test
            //generator.generateTestFile();
            generator.generateTest();

            // Pour chaque filtre on créer l'objet Probe
            for (FiltreGenerator fg : listeFiltre) {
                System.out.println("CAPTURE filter: => " + fg);
                GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "CAPTURE filter: => " + fg);
                // Création de la stack du protocole que l'on souhaite filtrer
                Stack stack = StackFactory.getStack(fg.getProtocole());
                DOMDocument docProbe = new DOMDocument();
                Element root = docProbe.addElement("root");
                root.addAttribute("filename", src);
                // String captureFilter = "host " + fg.getHostName() + " and port " + fg.getHostPort().toString();
                String captureFilter = "host " + fg.getHostName() + " and (port " + fg.getHostPort().toString() + " or not (ip[6:2] & 0x1FFF = 0)) and ip";
                root.addAttribute("captureFilter", captureFilter);

                // Création de l'objet Probe
                Probe probe = new Probe(stack, root);
                listeProbe.add(probe);

                // On configure le probe pour la génération de script
                probe.genScript(generator);

                stack.createProbe(probe, fg.getProtocole());
            }

            // Attente de la fin de la capture à partir du fichier
            while (!listeProbe.get(0).getProbeJpcapThread().getStopPossible()) {
            }

            generator.closeTest();

            System.out.println("END => test file: " + args[args.length - 1] + "(See application.log)");
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "END => test file: " + args[args.length - 1] + "(See application.log)");
            
            // On ferme proprement le programme                                      
            System.exit(0);
        }
        catch (Exception e) {
            System.out.println("EXCEPTION => " + e);
            e.printStackTrace();
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "EXCEPTION => ");            System.exit(-10);
        }
    }
}
