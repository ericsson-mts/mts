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

package com.devoteam.srit.xmlloader.master.master.utils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class DataTest {

    private URI _path;
    private Element _root;
    private ControlerTest _controlerTest;
    
    public DataTest(Element root, URI path) throws Exception {
        _root = root;
        _path = URIFactory.resolve(path, root.attributeValue("path"));
        if (null != root.attributeValue("home") && !root.attributeValue("home").endsWith("/")) {
            root.attribute("home").setValue(root.attributeValue("home") + "/");
        }
        _controlerTest = new ControlerTest(this);
    }

    public HashMap<String, String> getInitialValues() throws Exception{
        Runner runner = new Runner("tmp");
        runner.setParameterPool(new ParameterPool(runner, ParameterPool.Level.standalone, null));
        
        LinkedList<Operation> operations = new LinkedList<Operation>();
        List<Element> parameterElements = (List<Element>) _root.elements("parameter");
        for (Element parameterElement : parameterElements) {
            operations.add(new OperationParameter(parameterElement));
        }

        for (Operation operation : operations) {
            operation.executeAndStat(runner);
        }
        
        HashMap<String, String> ret = new HashMap<String, String>();
        for(String name: runner.getParameterPool().getParametersNameLocal()){
            ret.put(name, runner.getParameterPool().get(name).get(0).toString());
        }
        
        return ret;
    }
    
    public ControlerTest getControlerTest(){
        return _controlerTest;
    }
    
    public Element getRoot() {
        return _root;
    }

    public URI getPath() {
        return _path;
    }

    public void setRunner(String runner) {
        _root.attribute("runner").setValue(runner);
    }
    
    public String getRunner() {
        return _root.attributeValue("runner");
    }

    public void setSlave(String slave) {
        _root.attribute("slave").setValue(slave);
    }
    
    public String getSlave() {
        return _root.attributeValue("slave");
    }
    
    public String getName() {
        return _root.attributeValue("name");
    }
    
    public String getHome() {
        return _root.attributeValue("home");
    }
}
