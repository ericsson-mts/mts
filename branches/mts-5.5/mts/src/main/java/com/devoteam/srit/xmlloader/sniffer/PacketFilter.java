package com.devoteam.srit.xmlloader.sniffer;

import java.util.Arrays;

import jpcap.PacketReceiver;
import jpcap.packet.Packet;

import com.devoteam.srit.xmlloader.core.protocol.Stack;

public class PacketFilter implements PacketReceiver {
		int i=0;
		boolean end = false;
		long timeBase;
		boolean fistPacket = true;
		Stack stack;

		Thread timeCalculation = new Thread(new Runnable() {
			long time = System.currentTimeMillis();
			public void run() {
				System.out.println("attend");
				while (time-timeBase<5*1000) {//5 secondes for 5000 milliseconds
					float woot = ((float)(time-timeBase))/1000;
					/*					if (woot%1==0 && woot!=0) {packet
						System.out.println(((float)(time-timeBase))/1000);
					}*/
					//System.out.println(i+" "+((float)(time-timeBase))/1000);
					//time = System.currentTimeMillis();
					Thread.yield();
				}
				System.out.println("fini");
				end = true;
			}
		});
		//this method is called every time Jpcap captures a packet
		public void receivePacket(Packet packet) {
			if (fistPacket) {
				miseAjour();
				lancementThread();
				fistPacket=false;
			}
			else {
				miseAjour();
			}
			// byte[] data = Arrays.copyOf(packet.header, packet.data.length+packet.header.length);
			// for (int i=0; i<packet.data.length; i++) {
			//  	data[packet.header.length+i]=packet.data[i];
			// }
			/*try {
				Msg msg = stack.readFromDatas(data, data.length);
				stack.receiveMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				new ExecutionException("j'ai fait de la merdeuh" + e);
			}*/
			//System.out.println(i++);
			//just print out a captured packet
			System.out.println(packet);
		}
		public void miseAjour() {
			timeBase =System.currentTimeMillis();
		}
		public void lancementThread() {
			
			/*try {
				Stack stack = StackFactory.getStack("PROTOCOL_UDP");
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			timeCalculation.setPriority(Thread.MIN_PRIORITY);
			timeCalculation.start();
		}
		public void woot() {
			timeCalculation.stop();
		}
		public boolean isEnd() {
			return end;
		}
}
