/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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