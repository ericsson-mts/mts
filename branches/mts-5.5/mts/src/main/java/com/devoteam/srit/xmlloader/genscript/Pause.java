/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.genscript;

/**
 * Represent a generated pause
 * @author bthou
 */
public class Pause {
    
    String name;
    Float time;
    
    // Getters et Setters
    public String getName() { return name; }
    public void setName(String n) { name = n; }
    
    public Float getTime() { return time; }
    public void setTime(Float t) { time = t; }
    // ------------------
    
    // Constructeur pour une pause
    public Pause(String n, Float f){
        name = n;
        time = f;        
    }
    
    // Methode pour générer le code xml de la pause
    public String toXml(){
        String pause = "";
        pause += "<pause ";
        if(getName() != null){
            pause += "name=\""+getName()+"\" ";
        }
        if(getTime() != null){
            pause += "seconds=\""+getTime().toString()+"\"";
        }
        pause += "/>"+System.getProperty("line.separator")+System.getProperty("line.separator");
        return pause;
    }
    
    public String toString() {
        return getTime().toString() + "sec";
    }

}
