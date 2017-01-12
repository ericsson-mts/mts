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

package com.devoteam.srit.xmlloader.core;

import java.util.Vector;

/**
 *
 * @author jbor
 */
public class PropertiesParametersStructure {

    //attribut
    private String name                     = null;
    private String description              = "";
    private String type                     = null;
    private String defaultValue             = null;
    private String localeValue              = null;
    private Vector<String> possibilities    = null;
    private boolean restart                 = false;

    //constructeurs
    public PropertiesParametersStructure(String name, String defaultValue){
        this.name            = name;
        this.defaultValue    = defaultValue;
    }
    public PropertiesParametersStructure(String name, String type, String defaultValue, String localeValue){
        this(name, defaultValue);
        this.localeValue     = localeValue;
        this.type            = type;
    }
    public PropertiesParametersStructure(String name, String description, String type, String defaultValue, String localeValue, boolean restart, Vector<String> possibilities){
        this(name, type, defaultValue, localeValue);
        this.description     = description;
        this.type            = type;
        this.restart         = restart;
        this.possibilities   = possibilities;
    }

    //methodes
    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }
    public String getType(){
        return this.type;
    }
    public String getDefaultValue(){
            return this.defaultValue;
    }
    public String getLocaleValue(){
            return this.localeValue;
    }
    public boolean getRestart(){
        return restart;
    }
    public Vector<String> getPossibilities(){
        return this.possibilities;
    }
    public void setLocaleValue(String localeValue){
        this.localeValue = localeValue;
    }
    public void setDefaultValue(String defaultValue){
        this.defaultValue = defaultValue;
    }
    
    @Override
    public PropertiesParametersStructure clone(){
        return new PropertiesParametersStructure(name, description, type, defaultValue, localeValue, restart, possibilities);
    }
}