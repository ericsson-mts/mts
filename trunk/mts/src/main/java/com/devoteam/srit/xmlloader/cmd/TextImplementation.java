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

package com.devoteam.srit.xmlloader.cmd;

import com.devoteam.srit.xmlloader.core.PropertiesEnhanced;
import com.devoteam.srit.xmlloader.core.log.FileTextListenerProvider;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.log.TextListenerProviderRegistry;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.TextExceptionHandler;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.File;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author
 * gpasquiers
 */
public class TextImplementation {

    static public void main(String... args) {
        //
        // Handle arguments
        //
        if (args.length <= 0) {
            usage("At least one argument is required : the test file path");
        }

        ExceptionHandlerSingleton.setInstance(new TextExceptionHandler());

        /*
         * Set the FSInterface to LocalFS.
         */
        SingletonFSInterface.setInstance(new LocalFSInterface());

        String testFilename = args[0];
        String runnerName = "-seq";
        if (args.length >= 2) {
            runnerName = args[1];
        }

        List<String> parameterEditable = new LinkedList<String>();

        args[0] = null;
        if (args.length >= 2 && !args[1].startsWith("-")) {
            args[1] = null;
        }

        for (String arg : args) {
            if (null == arg) {
                continue;
            }
            String[] splitted = Utils.splitNoRegex(arg, ":");

            if (arg.startsWith("-seq") && "-sequential".startsWith(arg)) {
                runnerName = "-seq";
            }
            else if (arg.startsWith("-par") && "-parallel".startsWith(arg)) {
                runnerName = "-par";
            }
            else if (arg.startsWith("-load")) {     // deprecated
                GlobalLogger.instance().getApplicationLogger().warn(Topic.CORE, "DEPRECATED : please use -par[allel] option instead of -load");                
                runnerName = "-par";
            }
            else if (arg.startsWith("-testplan")) {

                URI uri = new File(testFilename).toURI();
                try {
                    TextTester textTester = new TextTester(uri, "-testplan", parameterEditable);
                }
                catch (Exception ex) {
                    GlobalLogger.instance().getApplicationLogger().error(Topic.CORE, ex, "error generate Test plan : path : ", uri.getPath());
                }
                System.exit(0);
            }
            else if (splitted.length == 2 && splitted[0].startsWith("-config") && "-configuration".startsWith(splitted[0])) {
                try {
                    String nameValue = splitted[1];
                    String[] splitted1 = Utils.splitNoRegex(nameValue, "+");
                    String name = splitted1[0];
                    String value = "";
                    if (splitted1.length > 1) {
                        value = splitted1[1];
                    }
                    PropertiesEnhanced properties = new PropertiesEnhanced();
                    properties.addPropertiesEnhancedComplete(name, value);
                    if (Config.getConfigByName("tester.properties").getString(name) == null) {
                        usage("The configuration parameter \"" + name + "\" does not exist in the tester.properties files. Please check that point.");
                    }
                    Config.overrideProperties("tester.properties", properties);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    usage("Unable to set the configuration parameters with the -config[uration] option.");
                }
            }
            else if (splitted.length == 2 && splitted[0].startsWith("-param") && "-parameter".startsWith(splitted[0])) {
                try {
                    String nameValue = splitted[1];
                    parameterEditable.add(nameValue);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    usage("Unable to set the editable parameter with the -param[eter] option.");
                }
            }
            else if (splitted[0].startsWith("-gen") && "-genReport".startsWith(splitted[0])) {
                PropertiesEnhanced properties = new PropertiesEnhanced();
                String nameValue = "true";
                if (splitted.length > 1) {
                    nameValue = splitted[1];
                }
                properties.addPropertiesEnhancedComplete("stats.AUTOMATIC_GENERATE", nameValue);
                Config.overrideProperties("tester.properties", properties);
            }
            else if (splitted[0].startsWith("-show") && "-showReport".startsWith(splitted[0])) {
                PropertiesEnhanced properties = new PropertiesEnhanced();
                String nameValue = "true";
                if (splitted.length > 1) {
                    nameValue = splitted[1];
                }
                properties.addPropertiesEnhancedComplete("stats.AUTOMATIC_SHOW", nameValue);
                Config.overrideProperties("tester.properties", properties);
            }
            else if (splitted[0].startsWith("-level") && "-levelLog".startsWith(splitted[0])) {
                PropertiesEnhanced properties = new PropertiesEnhanced();
                String nameValue = "true";
                if (splitted.length > 1) {
                    nameValue = splitted[1];
                }
                properties.addPropertiesEnhancedComplete("logs.MAXIMUM_LEVEL", nameValue);
                Config.overrideProperties("tester.properties", properties);
            }
            else if (splitted[0].startsWith("-stor") && "-storageLog".startsWith(splitted[0])) {
                PropertiesEnhanced properties = new PropertiesEnhanced();
                String nameValue = "true";
                if (splitted.length > 1) {
                    nameValue = splitted[1];
                }
                properties.addPropertiesEnhancedComplete("logs.STORAGE_LOCATION", nameValue);
                Config.overrideProperties("tester.properties", properties);
            }
            else {
                usage("Invalid option : " + arg);
            }
        }

        /*
         * Register the File logger provider
         */
        TextListenerProviderRegistry.instance().register(new FileTextListenerProvider());

        //
        // Initialize the Statistics to automatically generate periodically statistics report
        //
        StatPool.initialize("standalone");

        try {
            URI uri = new File(testFilename).toURI();
            TextTester textTester = new TextTester(uri, runnerName, parameterEditable);
            System.out.println("Run " + runnerName + " test \"" + testFilename);
            int logLevel = GlobalLogger.instance().getLogLevel();
            int logStorage = GlobalLogger.instance().getLogStorage();
            if ((runnerName.equals("-load")) && (logLevel < TextEvent.WARN) && (logStorage == GlobalLogger.LOG_STORAGE_FILE)) {
                String systemOut = "(logs.STORAGE_LOCATION=\"" + logStorage + "\",";
                systemOut += "logs.MAXIMUM_LEVEL=\"" + logLevel + "\")";
                System.out.println(systemOut);
            }

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "testFilename = ", testFilename);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "runnerName = ", runnerName);


            textTester.start();
            textTester.acquire();

            boolean automaticGenerate = Config.getConfigByName("tester.properties").getBoolean("stats.AUTOMATIC_GENERATE", false);
            if (automaticGenerate) {
                textTester.getRunner().getTest().report_generate();

            }
            //Generer un test plan en mode cmd
            textTester.getRunner().getTest().generateTestplan();
            // Return the status to the process

            if (textTester.getRunner().getState().isFailed() || textTester.getRunner().getState().isInterrupted()) {
                System.exit(1);
            }
            else {
                System.exit(0);
            }
        }
        catch (Exception e) {
            ExceptionHandlerSingleton.instance().display(e, null);
        }

        try {
            URI uri = new File(testFilename).toURI();
            MasterTextTester textTester = new MasterTextTester(uri, runnerName);
            System.out.println("Run master \"" + testFilename + "\"");

            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "testFilename = ", testFilename);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "runnerName = ", runnerName);

            textTester.run();

            // Shows report according to the AUTOMATIC_GENERATE boolean
            boolean automaticGenerate = Config.getConfigByName("tester.properties").getBoolean("stats.AUTOMATIC_GENERATE", false);
            if (automaticGenerate) {
                textTester.report();
            }

            // Return the status to the process
            if (textTester.getGlobalState() == null || textTester.getGlobalState().isFailed() || textTester.getGlobalState().isInterrupted()) {
                System.exit(1);
            }
            else {
                System.exit(0);
            }
        }
        catch (Exception e) {
            ExceptionHandlerSingleton.instance().display(e, null);
            System.exit(2);//exit with code 2 meaning error at parsing xml file
        }
    }

    static public void usage(String message) {
        System.out.println(message);
        System.out.println("Usage: startCmd <testFile>|<masterFile>\n"
                + "    -seq[uential]|-par[allel]|<testcaseName>\n"
                + "    -testplan\n"
                + "    [-param[eter]:<paramName>+<paramValue>]\n"
                + "    [-config[uration]:<configName>+<configValue>]\n"
                + "    [-level[Log]:ERROR=0|WARN=1|INFO=2|DEBUG=3]\n"
                + "    [-stor[ageLog]:disable=0|file=1]\n"
                + "    [-gen[Report]:false|true]\n"
                + "    [-show[Report]:false|true]\n");
        System.exit(10);
    }
}