/* 
 * Copyright 2017 Ericsson http://www.ericsson.com
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

package com.devoteam.srit.xmlloader.sctp.sunnio;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.sctp.*;

import java.nio.*;
import com.sun.nio.sctp.*;

/**
 * @author emicpou
 *  *
 */
public class DataSunNioSctp implements DataSctp {
	
	/**
	 * own buffer
	 */
    private byte[] data;
    
	/**
	 * adaptee
	 * a MessageInfo is linked to a address or an Association that is unmutable
	 * this API doesn't allow to change it once instanciated
	 * the adpatee is provided for incoming messages from the Sctp*Chanels
	 * or 
	 */
    private MessageInfo messageInfo;
	
	/**
	 * alternative to MessageInfo adaptee
	 * required to store infos for outgoing messages
	 */
    private BasicInfoSctp alternativeInfo;

    /**
	 * constructor for outgoing datagrams
	 */
	public DataSunNioSctp(){
		this.data = new byte[0];
		this.messageInfo = null;
		this.alternativeInfo = new BasicInfoSctp();
	}
	
	/**
	 * copy constructor
	 * 
	 * @param byteBuffer
	 */
	public DataSunNioSctp( DataSunNioSctp dataSunNioSctp ){
		//a deep clone would be safer!
		this.data = dataSunNioSctp.data;
		this.messageInfo = dataSunNioSctp.messageInfo;
		this.alternativeInfo = dataSunNioSctp.alternativeInfo;
	}
		
	/**
	 * constructor for outgoing datagrams
	 * 
	 * @param byteBuffer
	 */
    public DataSunNioSctp( DataSctp chunk ){
		assert(chunk!=null);
		if( chunk instanceof DataSunNioSctp ){
			DataSunNioSctp dataSunNioSctp = (DataSunNioSctp)chunk;
			//a deep clone would be safer!
			this.data = dataSunNioSctp.data;
			this.messageInfo = dataSunNioSctp.messageInfo;
			this.alternativeInfo = dataSunNioSctp.alternativeInfo;
		}
		else{
			//a deep clone would be safer!
			this.data = chunk.getData();
			this.messageInfo = null;
			this.alternativeInfo = new BasicInfoSctp();
			this.alternativeInfo.trySet(chunk.getInfo());
		}
	}
	
	/**
	 * constructor for incoming or outgoing datagrams
	 * 
	 * @param byteBuffer a ByteBuffer in read mode. Its remaining data will be copied
	 * @param messageInfo
	 */
	public DataSunNioSctp( ByteBuffer byteBuffer,MessageInfo messageInfo ){
		//how to ensure the byteBuffer is in read mode?

		//copy the data
		int length = byteBuffer.limit()-byteBuffer.position();
		assert(length>=0);
		this.data = new byte[length];
		byteBuffer.get(this.data);

		//a deep clone would be safer!
		this.messageInfo = messageInfo;
		assert(this.messageInfo!=null);
		
		this.alternativeInfo = null;
	}

	/**
	 * 
	 */
	public MessageInfo getMessageInfo(){
		return this.messageInfo;
	}
	    
	/**
	 * 
	 */
	@Override
	public byte[] getData(){
		return this.data;
	}
	
	/**
	 * assumes the wrapped byteBuffer is in read mode
	 */
	@Override
	public int getLength(){
		return this.data.length;
	}
	
	/**
	 * 
	 */
	@Override
	public void setData(byte[] data) throws Exception{
		assert(data!=null);
		//a copy would be safer!
		this.data = data;
	}
	
	/**
	* 
	*/
	@Override
	public InfoSctp getInfo(){
		if( this.messageInfo!=null ){
			return new InfoSunNioSctp(this.messageInfo);
		}
		else {
			assert(this.alternativeInfo!=null);
			return this.alternativeInfo;
		}
	}
	
	/**
	* 
	*/
	@Override
	public void setInfo( InfoSctp infoSctp ) throws Exception{
		if(infoSctp instanceof InfoSunNioSctp){
			//maybe we should clone instead of sharing instance?
			InfoSunNioSctp infoSunNioSctp = (InfoSunNioSctp)infoSctp;
			this.messageInfo = infoSunNioSctp.messageInfo;
			this.alternativeInfo = null;
		}
		else if(infoSctp instanceof BasicInfoSctp){
			//maybe we should clone instead of sharing instance?
			BasicInfoSctp basicInfoSctp = (BasicInfoSctp)infoSctp;
			this.messageInfo = null;
			this.alternativeInfo = basicInfoSctp;
		}
		else{
			//apply the copy operator on the wrapped messageInfo member
			this.messageInfo = null;
			if( this.alternativeInfo==null ){
				this.alternativeInfo = new BasicInfoSctp();
			}
			this.alternativeInfo.trySet(infoSctp);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void clear() throws Exception{
		this.data = new byte[0];
		this.messageInfo = null;
		this.alternativeInfo = new BasicInfoSctp();
	}

}

