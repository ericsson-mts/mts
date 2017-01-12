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
 * Reprensent a filtre of generation
 * @author bthou
 */
public class FiltreGenerator {
    
    String protocole;
    String hostName;
    Integer hostPort;

    public FiltreGenerator(String p, String hn, Integer hp) {
        protocole = p;
        hostName = hn;
        hostPort = hp;
    }
    
    public String getProtocole(){
        return protocole;
    }
    
    public String getHostName(){
        return hostName;
    }
    
    public Integer getHostPort(){
        return hostPort;
    }
    
    public boolean equals(FiltreGenerator f){
        return f.getProtocole().equals(protocole) && f.getHostName().equals(hostName) && f.getHostPort().equals(hostPort);
    }

    @Override
    public String toString(){
        return protocole.toUpperCase()+":"+hostName+":"+hostPort;
    }
    
}
