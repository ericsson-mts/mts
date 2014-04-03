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

package com.devoteam.srit.xmlloader.core.protocol;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.protocol.probe.PIPReassembler;
import com.devoteam.srit.xmlloader.core.protocol.probe.PJpcapThread;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.protocol.probe.PTCPPacket;
import com.devoteam.srit.xmlloader.core.protocol.probe.PTCPSocket;
import com.devoteam.srit.xmlloader.core.protocol.probe.PUDPPacket;
import com.devoteam.srit.xmlloader.core.utils.expireshashmap.ExpireHashMap;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.ethernet.MsgEthernet;
import com.devoteam.srit.xmlloader.ethernet.StackEthernet;
import com.devoteam.srit.xmlloader.genscript.ScriptGenerator;

import gp.utils.arrays.Array;
import java.util.Map;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;

import org.dom4j.Element;

/**
 * Interface generique servant a identifier un probe : point de capture
 * @author fhenry
 */
public class Probe
{
    private static boolean DEFAULT_PROMISCUOUS_MODE = true;

    // name of the probe
    private String name;
    // name of the network interface to capture on
    private String networkInterface;
    // capture filter (the same syntax as tcpdump or Wireshark tool)
    private String captureFilter;
    // filename to capture
    private String filename;
    // Regular expression specific filter
    private String regexFilter;
    // True if you want to open the interface in promiscuous mode, and otherwise false.
    // In non-promiscuous mode, you can only capture packets send and received by your host.
    private String promiscuousMode;

    private Pattern pattern;
    private Matcher matcher;

    private String protocol;
    private Stack stack;

    private PJpcapThread probeJpcapThread;

    private ExpireHashMap<String, PTCPSocket> sockets;

    private long startTimestamp = 0;
    
    // ## flag for know if we are in capture or generation
    private boolean genScript = false;
    
    private ScriptGenerator generator;
    
    /** Creates a Probe specific from XML tree*/
    public Probe(Stack stack, Element root) throws Exception
    {
        this.stack = stack;
        this.name = root.attributeValue("name");
        this.networkInterface = root.attributeValue("networkInterface");
        String capFilter = root.attributeValue("captureFilter");
        if (capFilter != null)
        {
	        capFilter = Utils.replaceNoRegex(capFilter, "[", "");
	        capFilter = Utils.replaceNoRegex(capFilter, "]", "");
        }
        this.captureFilter = capFilter;
        this.filename = root.attributeValue("filename");
        this.regexFilter = root.attributeValue("regexFilter");        
        this.sockets = new ExpireHashMap<String, PTCPSocket>("captured TCP streams", 600000);
        if(regexFilter != null && !regexFilter.equals("")){
        	this.pattern =  Pattern.compile(regexFilter);
        }
        else{
            this.pattern = null;
        }
        this.promiscuousMode = root.attributeValue("promiscuousMode");
        this.probeJpcapThread = new PJpcapThread(this);
        PIPReassembler.start();
        
        // ETHERNET protocol only
        // To avoid using two jpcap capture at the same time
        // enable isEthernetProbeCreated in EthernetStack
        if (stack instanceof StackEthernet)
        	((StackEthernet) stack).setEthernetProbeCreated(true);
    }

    public Map<String, PTCPSocket> getPTCPSockets(){
        return sockets;
    }

    public String getName()
    {
        return this.name;
    }

    public String getNetworkInterface()
    {
        return this.networkInterface;
    }

    public String getFilename()
    {
        return this.filename;
    }

    public String getCaptureFilter()
    {
        return this.captureFilter;
    }
    
    public String getRegexFilter(){
    	return this.regexFilter;
    }

    public String getProtocol()
    {
        return protocol;
    }

    /** create a probe  */
    public boolean create(String protocol) throws Exception
    {
		StatPool.beginStatisticProtocol(StatPool.PROBE_KEY, "", StackFactory.PROTOCOL_IP, protocol);
		this.startTimestamp = System.currentTimeMillis();
    	
        this.protocol = protocol;
        probeJpcapThread.create();
        
        return true;
    }

    /** Remove a probe */
    public boolean remove()
    {
		StatPool.endStatisticProtocol(StatPool.PROBE_KEY, "", StackFactory.PROTOCOL_IP, this.protocol, startTimestamp);
    		
        this.probeJpcapThread.stop();
        return true;
    }
    
    public boolean sendETHMessage(Msg msg) throws ExecutionException
    {
    	return this.probeJpcapThread.sendETHMessage(msg);
    }
    
    /** display method */
    @Override
    public String toString()
    {
        String str = "";
        str += "name=\"" + this.name + "\"";
        str += ", networkInterface = \"" + this.networkInterface + "\"";
        str += ", captureFilter = \"" + this.captureFilter + "\"";
        if(null != filename){
        	str += ", filename = \"" + this.filename + "\"";
        }
        if(null != regexFilter && !regexFilter.equals("")){
            str += ", regexFilter = \"" + this.regexFilter + "\"";
        }
        return str;
    }

    /** equals method */
    public boolean equals(Probe probe)
    {
        if (probe == null)
        {
            return false;
        }

        String name = probe.getName();
        if (null != this.name)
        {
            if (!this.name.equals(name))
            {
                return false;
            }
        }

        String network = probe.getNetworkInterface();
        if (null != this.networkInterface)
        {
            if (!this.networkInterface.equals(network))
            {
                return false;
            }
        }

        String filter = probe.getCaptureFilter();
        if (null != this.captureFilter)
        {
            if (!this.captureFilter.equals(filter))
            {
                return false;
            }
        }
        
        String filename = probe.getFilename();
        if (null != this.filename)
        {
            if (!this.filename.equals(filename))
            {
                return false;
            }
        }

        return true;
    }

    public Parameter getParameter(String path) throws Exception
    {
        String[] params = Utils.splitPath(path);
        if (params.length < 2)
        {
            return null;
        }

    	Parameter parameter = new Parameter();

        if (params[1].equalsIgnoreCase("name"))
        {
            parameter.add(this.name);
        }
        else if (params[1].equalsIgnoreCase("networkInterface"))
        {
        	parameter.add(this.networkInterface);
        }
        else if (params[1].equalsIgnoreCase("captureFilter"))
        {
        	parameter.add(this.captureFilter);
        }
        else if (params[1].equalsIgnoreCase("filename"))
        {
        	parameter.add(this.filename);
        }
        else if (params[1].equalsIgnoreCase("regexFilter"))
        {
        	parameter.add(this.regexFilter);
        }
        else if(params[1].equalsIgnoreCase("promiscuousMode")){
        	parameter.add(this.promiscuousMode);
        }
        else
        {
        	Parameter.throwBadPathKeywordException(path);
        }
        return parameter;
    }

    public boolean getPromiscuousMode() {
            boolean promisc;

            if(null == promiscuousMode || "".equals(promiscuousMode)){
                    promisc = DEFAULT_PROMISCUOUS_MODE;
                    this.promiscuousMode = Boolean.toString(DEFAULT_PROMISCUOUS_MODE);
            }else{
                    promisc = new Boolean(this.promiscuousMode);
            }

            return promisc;
    }

    /**
     *
     * @return true if this data should be captured
     */
    private boolean isValidCapture(byte[] data){
            // the message is valid because part of an existing transaction or
            // session
            boolean result = stack.isValidCapture(data);
            if (result)
            {
                    return true;
            }
            // the message is valid because matching the regex filter given by
            // the user
            if(null != pattern){
                    matcher = pattern.matcher(new String(data));

                    result = matcher.find();
                    this.matcher.reset();
            }
            else
            {
                    result = true;
            }
            if (!result)
            {
                    return false;
            }

            return result;
    }
	
    synchronized public void capturedETHPacket(Packet packet) throws Exception {
    	
    	int length = packet.header.length + packet.data.length - 14; // 14 is the ethernet headder length we have to substract in order to get good length of ethernet frame payload
    	EthernetPacket eth = (EthernetPacket) packet.datalink;
    	int type = eth.frametype;
    	byte[] srcMac = eth.src_mac;
    	byte[] dstMac = eth.dst_mac;
    	byte[] data = new byte[length];
    	MsgEthernet msg = null;
    	int j = 0;
    	
		for (int i = 14; i < packet.header.length; i++) {
			data[j++] = packet.header[i];
		}
		for (int i = 0; i < packet.data.length; i++) {
			data[j++] = packet.data[i];
		}
		
		try {
			msg = (MsgEthernet) stack.readFromDatas(data, length);
			msg.setProbe(this);
			msg.setType(type);
			msg.setdstMac(dstMac);
			msg.setSrcMac(srcMac);
			stack.captureMessage(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
   
    
    synchronized public void capturedUDPPacket(PUDPPacket packet) throws Exception {
        byte[] data = packet.getData().getBytes();
        
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, Stack.CAPTURE, "the UDP message :\n", packet, "\n", new String(data));
        
        boolean result = stack.isValidCapture(data);
        if (!result){
            result = isValidCapture(data);
        }        
        if (result){
            Msg msg = stack.readFromDatas(data, data.length);            
            msg.setProbe(this);
            msg.setChannel(packet); 
            
            if(genScript){
                msg.setTimestampCaptureFile(packet.getIPHeader().getTimestamp());
                generator.generateMsg(msg);
            }
            else{
                stack.captureMessage(msg);
            }
        }
    }

    public void handlePTCPSocket(PTCPSocket socket) throws Exception {

        Msg msg = stack.readFromStream(socket.getInputStream(), socket);
        
        if (msg != null)
        {
	        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, Stack.CAPTURE, " TCP message :\n", msg);
	
	        msg.setChannel(socket);
	        msg.setProbe(this);
	
	        if(genScript){
	            msg.setTimestampCaptureFile(socket.getLastTimestamp());
	            generator.generateMsg(msg);
	        }
	        else{
	            stack.captureMessage(msg);
	        }
        }
    }

    synchronized public void capturedTCPPacket(PTCPPacket packet) throws Exception {
    	if (packet.getTCPHeader().ack())
    	{
    		    	
	        // compute capture socket ID
	        StringBuilder builder = new StringBuilder(12);
	        builder.append(Array.toHexString(packet.getIPHeader().getSrcIP()));
	        builder.append(Array.toHexString(packet.getTCPHeader().getSrcPort()));
	        builder.append(Array.toHexString(packet.getIPHeader().getDstIP()));
	        builder.append(Array.toHexString(packet.getTCPHeader().getDstPort()));
	
	        String socketID = builder.toString();
	
	        PTCPSocket socket = sockets.get(socketID);
	
	        boolean newData = true;
	        if(null == socket){
	            socket = new PTCPSocket(packet, this);
	        }
	        else{
	            newData = socket.addPacket(packet);
	        }
	        
	        if(newData){
	            sockets.remove(socketID);
	            sockets.put(socketID, socket);
	        }
    	}
    }
    
    // Methode pour activer le mode generation de script de Probe
    public void genScript(ScriptGenerator sg){
        genScript = true;
        generator = sg;        
    }
    
    public PJpcapThread getProbeJpcapThread(){
        return probeJpcapThread;
    }
}
