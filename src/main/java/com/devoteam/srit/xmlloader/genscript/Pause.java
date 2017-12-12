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
