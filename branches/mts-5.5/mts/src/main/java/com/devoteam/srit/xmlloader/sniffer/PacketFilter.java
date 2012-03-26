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
