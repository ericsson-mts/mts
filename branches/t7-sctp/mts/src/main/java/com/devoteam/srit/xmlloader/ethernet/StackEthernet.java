package com.devoteam.srit.xmlloader.ethernet;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Probe;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.udp.MsgUdp;

public class StackEthernet extends Stack
{
	private String captureFilter = null;
	private boolean isEthernetProbeCreated = false;

	public StackEthernet() throws Exception {
		super();
	}

	public boolean isEthernetProbeCreated() {
		return isEthernetProbeCreated;
	}

	public void setEthernetProbeCreated(boolean isEthernetProbeCreated) {
		this.isEthernetProbeCreated = isEthernetProbeCreated;
	}

	public String getCaptureFilter() {
		return captureFilter;
	}

	public void setCaptureFilter(String captureFilter) {
		this.captureFilter = captureFilter;
	}
		
	@Override
	public synchronized boolean sendMessage(Msg msg) throws Exception
    {
		boolean ret;
		Probe p = msg.getProbe();
        if (p != null)
        {
            ret = p.sendETHMessage(msg);
        }
        else
        {
            throw new ExecutionException("No Probe to transport the message : \r\n" + msg.toString());
        }

        // increment counters in the transport section
        incrStatTransport(msg, StackFactory.PREFIX_OUTGOING, StackFactory.PREFIX_INCOMING);
        return ret;
    }
}
