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
package com.devoteam.srit.xmlloader.core.groovy;

import java.util.ArrayList;
import java.util.List;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;

import groovy.lang.Binding;

/**
 * Binding class used by groovy scripts to manage the exchange of properties
 * between the groovy scripts and MTS. More precisely, the binding will look for
 * variables either from MTSBinding itself, from the groovy script or from MTS
 * Runner:
 * <ul>
 * <li>runner property is retrieved from MTSBinding.
 * <li>variables prefixed by "groovy_" are retrieved from the groovy script.
 * <li>other variables are retrieved from MTS ParameterPool.
 * </ul>
 *
 *
 * @author mickael.jezequel@orange.com
 *
 */
public class MTSBinding extends Binding {

    public static final String GROOVY_VAR_PREFIX = "groovy_";

    /**
     * list of properties which should be retrieved from the groovy script.
     * properties not declared here will be set/retrieved from MTS
     *
     */
    private static List<String> groovyProperties = new ArrayList<String>();

    static {
        groovyProperties.add("binding");
        groovyProperties.add("metaClass");
        groovyProperties.add("out");
    }

    /**
     * The MTS runner
     */
    private Runner runner;

    /**
     * the binding constructor
     *
     * @param runner
     */
    public MTSBinding(Runner runner) {
        this.runner = runner;
    }

    /**
     * retrieve a variable either from MTSBinding, from the groovy script or
     * from MTS.
     * <ul>
     * <li>runner is retrieved from MTSBinding.
     * <li>variables prefixed by "groovy_" are retrieved from the groovy script.
     * <li>other variables are retrieved from MTS ParameterPool.
     * </ul>
     *
     */
    public Object getVariable(String name) {
        // check if the variable is stored in MTSBinding
        if ("runner".equals(name)) {
            return runner;

            // check if the variable is a native property of the groovy script
        } else if (groovyProperties.contains(name)) {
            return super.getVariable(name);

            // check if the variable is a local variable
        } else if (name.startsWith(GROOVY_VAR_PREFIX)) {
            return super.getVariable(name);
        } else {

            // otherwise the variable should declared in MTS Parameter Pool
            try {
                Parameter param = runner.getParameterPool().get(ParameterPool.bracket(name));
                if (param != null && param.length() > 0) {
                    return param.get(0);
                }
            } catch (ParameterException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    /**
     * set a variable either into MTS or into the groovy script. variables
     * prefixed by "groovy_" are set into the groovy script. other variables are
     * set into MTS ParameterPool.
     *
     */
    public void setVariable(String name, Object value) {
        if (name.startsWith(GROOVY_VAR_PREFIX) || "binding".equals(name) || "out".equals(name)) {
            super.setVariable(name, value);
        } else {
            try {
                GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PARAM, "MyBinding.setVariable(", name, ")");
                Parameter groovyParameter = new Parameter();
                if (value instanceof TestParameter) {
                    if (((TestParameter) value).isEditable()) {
                        GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.PARAM, "MyBinding.setVariable(", name, ") is editable");
                    }
                    groovyParameter.add(((TestParameter) value).getValue());
                } else {
                    groovyParameter.add(value);
                }
                runner.getParameterPool().set(ParameterPool.bracket(name), groovyParameter);
            } catch (ParameterException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
