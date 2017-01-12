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
 * Represent a generated listenpoint
 * @author bthou
 */
public class ListenPoint {
    
    private String PPP;
    private String name;
    private String localHost;
    private String localPort;
    private String listenUDP;
    private String listenTCP;
    private String transport;
    
    // Constructeur
    public ListenPoint(String PPP, String name, String localHost, String localPort, String listenUDP, String listenTCP, String transport){
        this.PPP = PPP;
        this.name = name;
        this.localHost = localHost;
        this.localPort = localPort;
        this.listenUDP = listenUDP;
        this.listenTCP = listenTCP;
        this.transport = transport;
    }

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocalHost() { return localHost; }
    public void setLocalHost(String localHost) { this.localHost = localHost; }

    public String getLocalPort() { return localPort; }
    public void setLocalPort(String localPort) { this.localPort = localPort; }

    public String getListenUDP() { return listenUDP; }
    public void setListenUDP(String listenUDP) { this.listenUDP = listenUDP; }

    public String getListenTCP() { return listenTCP; }
    public void setListenTCP(String listenTCP) { this.listenTCP = listenTCP; }

    public String getTransport() { return transport; }
    public void setTransport(String transport) { this.transport = transport; }

    public String getPPP() { return PPP; }
    public void setPPP(String PPP) { this.PPP = PPP; }
    // -----------------------
    
    
    public String createToXml(){
        String listenPoint = "";
        listenPoint += "  <createListenpoint"+PPP+" ";
        if(getName() != null){
            listenPoint += "name=\""+getName()+"\" ";            
        }
        if(getLocalHost() != null){
            listenPoint += "localHost=\""+getLocalHost()+"\" ";            
        }
        if(getLocalPort() != null){
            listenPoint += "localPort=\""+getLocalPort()+"\" ";            
        }
        if(getListenUDP() != null){
            listenPoint += "listenUDP=\""+getListenUDP()+"\" ";            
        }
        if(getListenTCP() != null){
            listenPoint += "listenTCP=\""+getListenTCP()+"\" ";            
        }
        if(getTransport() != null){
            listenPoint += "transport=\""+getTransport()+"\" ";            
        }        
        listenPoint += "/>"+System.getProperty("line.separator");
        
        return listenPoint;
    }
    
    public String closeToXml(){
        String listenPoint = "";
        
        listenPoint += "  <removeListenpoint";
        listenPoint += getPPP();
        listenPoint += " name=\""+getName()+"\"";
        listenPoint += "/>"+System.getProperty("line.separator");
        
        return listenPoint;
    }

    public String toString(){
        String ret = "";        
        ret += PPP + ":" + localHost + ":" + localPort;
        return ret;
    }
}
