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

package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.Cache;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.functions.FunctionsRegistry;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.XMLDocument;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.net.URI;
import org.dom4j.Element;

/**
 * OperationPause operation
 * 
 * @author gpasquiers
 */
public class OperationFunction extends Operation {

    /**
     * Constructor
     */
    public OperationFunction(Element root) throws Exception {
        super(root, null);
        String file = root.attributeValue("file");
        String name = root.attributeValue("name");

        if (file == null && name != null) {
            FunctionsRegistry.instance().register(name, root);
        }
        else if (file != null && name == null) {

            if (!root.elements().isEmpty()) {
                // TODO : should be zero, throw some error if not
            }

            importFile(file, URIRegistry.MTS_TEST_HOME);
        }
        else {
            throw new ParsingException("<function> operation should have exactly one of the two attributes: file or name");
        }
    }

    public static void importFile(String file, URI relativeTo) throws Exception {
        // parse the xml file
        XMLDocument scenarioDocument = Cache.getXMLDocument(relativeTo.resolve(file), URIFactory.newURI("../conf/schemas/scenario.xsd"));

        // get all <function> element
        for (Object object : scenarioDocument.getDocument().selectNodes("//function")) {
            Element element = (Element) object;
            // create operation function for each (that will register them or go into files again)
            
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "adding function from file ", relativeTo.resolve(file));
            OperationFunction temp = new OperationFunction(element);
        }

        // TODO : /!\ warning cyclic files reference bug ahead ! should keep track of parsed files
    }

    public static void importDir(String dir, URI relativeTo) throws Exception {
        // try to add all files from a dir; should only contain xml files
        URI dirUri = relativeTo.resolve(dir);
        GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "adding function from dir ", dirUri);
        if (SingletonFSInterface.instance().exists(dirUri)) {
            String[] files = SingletonFSInterface.instance().list(dirUri);

            for (String file : files) {
                URI fileUri = dirUri.resolve(file);
                if (SingletonFSInterface.instance().isFile(fileUri)) {
                    if (file.toLowerCase().endsWith(".xml")) {
                        importFile(file, dirUri);
                    }
                }
                else {
                    importDir(file, dirUri);
                }
            }
        }
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     */
    public Operation execute(Runner runner) throws Exception {
        // nothing to do, register the function at parsing time only
        return null;
    }
}
