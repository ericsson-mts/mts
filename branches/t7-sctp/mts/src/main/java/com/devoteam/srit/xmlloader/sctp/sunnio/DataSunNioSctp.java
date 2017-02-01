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
    private byte[] payload;
    
	/**
	 * adaptee
	 */
    private MessageInfo info;
	
	/**
	 * constructor for outgoing datagrams
	 * 
	 * @param byteBuffer
	 */
	public DataSunNioSctp(){
		this.payload = new byte[0];
		this.info = MessageInfo.createOutgoing(null,0);
	}
	
	/**
	 * constructor for outgoing datagrams
	 * 
	 * @param byteBuffer
	 */
	public DataSunNioSctp( DataSunNioSctp dataSunNioSctp ){
		//a deep clone would be safer!
		this.payload = dataSunNioSctp.payload;
		this.info = dataSunNioSctp.info;
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
			this.payload = dataSunNioSctp.payload;
			this.info = dataSunNioSctp.info;
		}
		else{
			//a deep clone would be safer!
			this.payload = chunk.getData();
			
			//apply the copy operator on the wrapped messageInfo member
			this.info = MessageInfo.createOutgoing(null,0);
			InfoSunNioSctp infoSunNioSctp = new InfoSunNioSctp(this.info);
			infoSunNioSctp.trySet(chunk.getInfo());
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

		//copy the payload
		int length = byteBuffer.limit()-byteBuffer.position();
		assert(length>=0);
		this.payload = new byte[length];
		byteBuffer.get(this.payload);

		//a deep clone would be safer!
		this.info = messageInfo;
		assert(this.info!=null);
	}

	/**
	 * 
	 */
	public MessageInfo getMessageInfo(){
		assert(this.info!=null);
		return this.info;
	}
	    
	/**
	 * 
	 */
	@Override
	public byte[] getData(){
		return this.payload;
	}
	
	/**
	 * assumes the wrapped byteBuffer is in read mode
	 */
	@Override
	public int getLength(){
		return this.payload.length;
	}
	
	/**
	 * 
	 */
	@Override
	public void setData(byte[] data){
		assert(data!=null);
		//a copy would be safer!
		this.payload = data;
	}
	
	/**
	* 
	*/
	@Override
	public InfoSctp getInfo(){
		assert(this.info!=null);
		return new InfoSunNioSctp(this.info);
	}
	
	/**
	* 
	*/
	@Override
	public void setInfo( InfoSctp infoSctp ) throws Exception{
		if(infoSctp instanceof InfoSunNioSctp){
			//maybe we should clone instead of sharing instance?
			InfoSunNioSctp infoSunNioSctp = (InfoSunNioSctp)infoSctp;
			this.info = infoSunNioSctp.messageInfo;
			assert(this.info!=null);
		}
		else{
			//apply the copy operator on the wrapped messageInfo member
			InfoSunNioSctp infoSunNioSctp = new InfoSunNioSctp(this.info);
			infoSunNioSctp.trySet(infoSctp);
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void clear(){
		this.payload = new byte[0];
		this.info = MessageInfo.createOutgoing(null,0);
	}

}

