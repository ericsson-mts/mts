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

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import java.util.List;

/**
 * Represent a generated message
 * @author bthou
 */
public class Message implements Comparable{
    
    private String PPP;
    
    private String srcIp;
    private String srcPort;
    
    private String dstIp;
    private String dstPort;
    
    private String name;
    private String listenPoint;
    
    private String type;
    private String result;
    private String request;
    
    private String msg;
    
    private Long timestamp;
    
    private Boolean envoi;
    
    private Msg msgSrc;    
    
    // Constructeur
    public Message(String PPP, String name){
        this.PPP = PPP;
        this.name = name;
    }
    
    // Methode pour cloner un message
    public Message clone(){
        Message clone = new Message(getPPP(), getName());
        clone.setSrcIp(srcIp);
        clone.setSrcPort(srcPort);
        clone.setDstIp(dstIp);
        clone.setDstPort(dstPort);
        clone.setListenPoint(listenPoint);
        clone.setType(type);
        clone.setMsg(msg);
        clone.setTimestamp(timestamp);
        clone.setEnvoi(envoi);
        clone.setMsgSrc(msgSrc);
        return clone;
    }

    // Getters et Setters
    public String getPPP() { return PPP; }
    public void setPPP(String PPP) { this.PPP = PPP; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getListenPoint() { return listenPoint; }
    public void setListenPoint(String listenPoint) { this.listenPoint = listenPoint; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    
    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    
    public Boolean getEnvoi() { return envoi; }
    public void setEnvoi(Boolean envoi) { this.envoi = envoi; }
    
    public String getSrcIp() { return srcIp; }
    public void setSrcIp(String srcIp) { this.srcIp = srcIp; }
    
    public String getSrcPort() { return srcPort; }
    public void setSrcPort(String srcPort) { this.srcPort = srcPort; }

    public String getDstIp() { return dstIp; }
    public void setDstIp(String dstIp) { this.dstIp = dstIp; }

    public String getDstPort() { return dstPort; }
    public void setDstPort(String dstPort) { this.dstPort = dstPort; }
    
    public Msg getMsgSrc() { return msgSrc; }
    public void setMsgSrc(Msg msgSrc) { this.msgSrc = msgSrc; }
    // --------------------------------------
    
    
    // Methode retournant la bonne balise (suivant s'il s'agit d'un evoi ou d'une reception)
    public String toXml() throws Exception {    	
        String xml = "";
        if(envoi){
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Generate <sendMessagePPP> " + msgSrc.toString());
            xml += sendToXml();
        }
        else{
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Generate <receiveMessagePPP> " + msgSrc.toString());
            xml += receiveToXml();
        }
        return xml;
    }
    
    // Methode retournant une balise send pour se message
    public String sendToXml(){
    	
        setName("Send "+getName().trim());
        
        String send = "";
        
        send += "  <sendMessage";
        send += getPPP();   
        send += " ";        
        if(getName() != null){
            send += "name=\""+getName()+"\" ";
        }                       
        if(getListenPoint() != null){
            send += "listenpoint=\""+getListenPoint()+"\" ";
        }
        if(getPPP().contains("RTP")){
            send += "remoteHost=\""+getDstIp()+"\" ";
            send += "remotePort=\""+getDstPort()+"\" ";
        }
        if(getPPP().contains("DIAMETER")){
            send += "remoteURL=\"diameter://"+getDstIp()+":"+getDstPort()+"\" ";
        }
        send += ">"+System.getProperty("line.separator");
        send += getMsg();
        send += System.getProperty("line.separator");
        send += "</sendMessage";
        send += getPPP()+">"+System.getProperty("line.separator")+System.getProperty("line.separator");
        
        return send;
    }
    
    // Methode retournant une balise receive pour ce message
    public String receiveToXml(){
        
        setName("Wait "+getName().trim());
        
        String rec = "";
        
        rec += "  <receiveMessage";
        rec += getPPP();   
        rec += " ";
        if(getName() != null){
            rec += "name=\""+getName()+"\" ";
        }
        if(getListenPoint() != null){
            rec += "listenpoint=\""+getListenPoint()+"\" ";
        }
        if(getType() != null){
            rec += "type=\""+getType()+"\" ";
        }
        if(getResult() != null){
            rec += "result=\""+getResult()+"\" ";
        }        
        if(getRequest() != null){
            rec += "request=\""+getRequest()+"\" ";
        } 
        rec += ">"+System.getProperty("line.separator");
        
        List<Param>listeParam = ConfigParam.getInstance().getParam(getPPP());
        if(listeParam != null){
            for(Param p : listeParam){
                try {                	
            		if ((Param.TARGET_RECSERVER.equalsIgnoreCase(p.getTarget()) && this.getMsgSrc().isRequest()) || 
            			(Param.TARGET_RECCLIENT.equalsIgnoreCase(p.getTarget()) && !this.getMsgSrc().isRequest())) {
            		String operation = p.getValue();
                        Parameter param = this.getMsgSrc().getParameter(operation);
                        if(param.length() > 0){
                                p.setDisplayed(true);
                                p.setRemplacedValue(param.get(0).toString());
                                rec += p.toXml();
                         }
            		}
                } catch (Exception ex) {
                     GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, ex, "Error while returning receive tag from xml");  }
            }
        }
        
        rec += "  </receiveMessage";
        rec += getPPP()+">"+System.getProperty("line.separator")+System.getProperty("line.separator");

        return rec;
    }

    public int compareTo(Object o) {
        return timestamp.compareTo(((Message)o).timestamp);
    }   

    public String toShortString() throws Exception {
        return msgSrc.toShortString();
    }

    public String toString() {
        return msgSrc.toString();
    }

}
