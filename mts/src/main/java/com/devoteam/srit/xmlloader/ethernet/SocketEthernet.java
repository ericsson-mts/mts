package com.devoteam.srit.xmlloader.ethernet;

import java.io.IOException;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;

class myPacketReceiver implements PacketReceiver {
	private StackEthernet stack = null;
	
	public myPacketReceiver(StackEthernet stack) {
		super();
		this.stack = stack;
	}
	
	@Override
	public void receivePacket(Packet arg0) {
		if (arg0 instanceof IPPacket)
		{
			IPPacket ipp = (IPPacket) arg0;
			Msg msg = null;
			//cut off 14 first byte of ethernet frame
			int length = ipp.header.length + ipp.data.length - 14;
			byte[] datas = new byte[length];
			int j = 0;
			for (int i = 14; i < ipp.header.length; i++) {
				datas[j++] = ipp.header[i];
			}
			for (int i = 0; i < ipp.data.length; i++) {
				datas[j++] = ipp.data[i];
			}
			
			try {
				msg = stack.readFromDatas(datas, length);
				msg.setListenpoint(stack.getSock().getListenpointEthernet());
				stack.receiveMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

public class SocketEthernet extends Thread {
	
	private ListenpointEthernet listenpointEthernet;
	
	public ListenpointEthernet getListenpointEthernet() {
		return listenpointEthernet;
	}

	public SocketEthernet()
	{}
	
	public void run()
	{
		StackEthernet stack = null;
		try {
			stack = (StackEthernet) StackFactory.getStack(this.listenpointEthernet.getProtocol());
		} catch (ExecutionException e) {
			GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "Exception in SocketUdp : unable to instatiate the stack");
		}
		NetworkInterface[] devices = JpcapCaptor.getDeviceList();
		JpcapCaptor jpcap;
		try {
			jpcap = JpcapCaptor.openDevice(devices[0], 65535, false, 5000); // Set timeout to 5 s ... doesn't seem to work on windows plateform
			jpcap.setFilter(stack.getCaptureFilter(), true);
			jpcap.setNonBlockingMode(true);
			myPacketReceiver pr = new myPacketReceiver(stack);
			jpcap.loopPacket(1, pr);
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}

	/**
	 * @param listenpointEthernet the listenpointEthernet to set
	 */
	public void setListenpointEthernet(ListenpointEthernet listenpointEthernet) {
		this.listenpointEthernet = listenpointEthernet;
	}

}
