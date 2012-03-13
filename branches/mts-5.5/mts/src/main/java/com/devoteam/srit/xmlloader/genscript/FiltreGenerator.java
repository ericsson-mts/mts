/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
