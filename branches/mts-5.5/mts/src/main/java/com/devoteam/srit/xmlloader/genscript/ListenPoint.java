/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
