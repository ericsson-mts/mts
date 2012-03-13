/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.mgcp;

/**
 *
 * @author indiaye
 */
public class MgcpParameter {


private String name=null;
    private String value=null;
    public MgcpParameter(String name, String value) {
        this.name=name;
        this.value=value;
    }
    
 public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
