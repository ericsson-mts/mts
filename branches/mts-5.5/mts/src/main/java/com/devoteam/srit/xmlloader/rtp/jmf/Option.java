/*
 * Created on 26 sept. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.devoteam.srit.xmlloader.rtp.jmf;

/**
 * @author ma007141
 *
*/
class Option {
	
	private int id;
	
	private byte[] value;
	
	public Option(int i, byte[] v) {
		id = i;
		value = new byte[v.length];
		System.arraycopy(v, 0,value,0,v.length);
	}
	
	public int getId() {
		return id;
	}
	
	public byte[] getValue() {
		return value;
	}
	
	public int getLength() {
		return value.length+2;
	}
	
}
