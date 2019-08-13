/* 
 * Copyright 2017 Orange http://www.orange.com
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
package com.devoteam.srit.xmlloader.core.operations.basic;

import java.io.File;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.groovy.MTSBinding;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.maps.HashMap;

import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;

/**
 * OperationGroovy executes the groovy script source code
 *
 *
 * @author mickael.jezequel@orange.com
 */
public class OperationGroovy extends Operation {

    /**
     *
     */
    private static final long serialVersionUID = 5682007474036872606L;

    /**
     * Map which contains every groovy class instance, and the name of the
     * property which is automaticaly added in the groovy operation
     */
    private Map<String, Script> injectedScripts = new HashMap<String, Script>();

    /**
     * Constructor
     *
     *
     * @param name Name of the operation
     * @param pause OperationGroovy value
     */
    public OperationGroovy(Element root) {
        super(root, null);
    }

    /**
     * Execute operation: load each imported groovy class file, instantiate each
     * groovy class, inject these instances in the groovy operation script, and
     * finally execute the groovy operation script
     *
     * @param runner
     * @return Next operation or null by default
     */
    @Override
    public Operation execute(Runner runner) throws Exception {
        if (runner instanceof ScenarioRunner) {
            GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);
        } else {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, this);
        }

        // retrieve the list of groovy files to load
        String groovyFiles = getRootElement().attributeValue("name");

        // retrieve the groovy operation script source
        String scriptSource = getRootElement().getText();

        try {
            // instantiate the binding which manage the exchange of variables
            // between groovy scripts and MTS runner:
            MTSBinding mtsBinding = new MTSBinding(runner);

            // instantiate the classloader in charge of groovy scripts
            CompilerConfiguration compilerConfig = new CompilerConfiguration();
            compilerConfig.setScriptBaseClass("MTSScript");

            ClassLoader mtsClassLoader = getClass().getClassLoader();
            GroovyClassLoader groovyClassLoader = new GroovyClassLoader(mtsClassLoader, compilerConfig);

            // load, instantiate and execute each groovy file,
            if (groovyFiles != null) {
                StringTokenizer st = new StringTokenizer(groovyFiles, ";");
                while (st.hasMoreTokens()) {
                    String scriptName = st.nextToken();
                    File file = new File(URIRegistry.MTS_TEST_HOME.resolve(scriptName));
                    if (file.exists() && file.getName().endsWith(".groovy")) {
                        Class groovyClass = groovyClassLoader.parseClass(file);
                        Object obj = groovyClass.newInstance();
                        if (obj instanceof Script) {
                            // add the MTS Binding and execute the run method
                            ((Script) obj).setBinding(mtsBinding);
                            ((Script) obj).invokeMethod("run", new Object[]{});
                            prepareScriptProperties(runner, scriptName, (Script) obj);
                        }
                    } else {
                        if (runner instanceof ScenarioRunner) {
                            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE,
                                    "invalid groovy file " + scriptName);
                        } else {
                            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE,
                                    "invalid groovy file " + scriptName);
                        }
                    }
                }
            }
            groovyClassLoader.close();

            // instantiate the groovy operation script
            Class scriptClass = groovyClassLoader.parseClass(scriptSource);
            Script script = (Script) scriptClass.newInstance();
            script.setBinding(mtsBinding);

            // inject each imported groovy class as a property of the groovy
            // operation script
            injectScriptProperties(script);

            // execute the groovy operation script
            Object result = script.run();

        } catch (AssertionError pae) {
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, pae.getMessage(), scriptSource);
            throw new ExecutionException("Error in groovy test", pae);

        } catch (Exception e) {
            GlobalLogger.instance().getSessionLogger().error(runner, TextEvent.Topic.CORE, scriptSource,
                    "Exception occured\n", e);
            throw new ExecutionException("Error executing groovy operation command", e);
        }

        return null;
    }

    /**
     * generate a property name based on the groovy script filename and store
     * the script instance in the map
     *
     * @param groovyFilename
     * @param script
     * @throws ExecutionException
     */
    private void prepareScriptProperties(Runner runner, String groovyFilename, Script script) throws ExecutionException {
        // the property name is prefixed by "groovy_" followed by the script filename
        // without suffix
        // ie if we load Toto.groovy, the script instance property will be groovy_Toto
        
        int first = groovyFilename.lastIndexOf('/');
        if (first == -1) {
            first = 0;
        } else if (first > 0) {
            //remove last '/'
            first += 1;
        }
        
        groovyFilename = groovyFilename.substring(first, groovyFilename.length());
        
        int last = groovyFilename.indexOf('.');
        if (last > 0) {
            String propertyName = MTSBinding.GROOVY_VAR_PREFIX + groovyFilename.substring(0, last);
            injectedScripts.put(propertyName, script);
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "injecting " + propertyName + " object for script " + groovyFilename);
        } else {
            throw new ExecutionException("cannot load groovy script " + groovyFilename + " : invalid file name");
        }
    }

    /**
     * inject each groovy class in the groovy operation script
     *
     * @param script : the groovy operation script
     */
    private void injectScriptProperties(Script script) {
        for (Object ScriptName : injectedScripts.keySet()) {
            script.setProperty((String) ScriptName, injectedScripts.get(ScriptName));
        }
    }

}
