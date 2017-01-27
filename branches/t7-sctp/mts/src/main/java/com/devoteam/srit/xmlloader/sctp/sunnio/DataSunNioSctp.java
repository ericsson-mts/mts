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
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.sctp.*;

import java.nio.*;
import java.nio.channels.*;
import com.sun.nio.sctp.*;

/**
 * @author emicpou
 * 
 * wraps a direect ByteBuffer without duplication
 *
 */
public class DataSunNioSctp implements DataSctp {
	
	/**
	 * reference on the implementation object
	 */
    protected ByteBuffer byteBuffer;
    protected MessageInfo messageInfo;
	
	public DataSunNioSctp(){
		this.byteBuffer = ByteBuffer.allocate(0);
	}
	
	public DataSunNioSctp( byte[] data ){
		assert(data!=null);
		this.byteBuffer = ByteBuffer.wrap(data);
	}

    public DataSunNioSctp( DataSctp chunk ){
		assert(chunk!=null);
		assert( chunk instanceof DataSunNioSctp );
		DataSunNioSctp dataSunNioSctp = (DataSunNioSctp)chunk;
		//a deep clone would be safer!
		this.byteBuffer = dataSunNioSctp.byteBuffer;
		this.messageInfo = dataSunNioSctp.messageInfo;
	}
	
	public DataSunNioSctp( ByteBuffer byteBuffer ){
		//a deep clone would be safer!
		this.byteBuffer = byteBuffer;
	}
	
	public DataSunNioSctp( ByteBuffer byteBuffer,MessageInfo messageInfo ){
		//a deep clone would be safer!
		this.byteBuffer = byteBuffer;
		this.messageInfo = messageInfo;
	}	
	
	/**
	 * 
	 */
	@Override
	public byte[] getData(){
		byte[] data = null;
		try{
			data = this.byteBuffer.array();
		}catch(Exception e){
			GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL,"byteBuffer backing array can't be accessed");
			data = new byte[0];
		}
		return data;
	}
	
	/**
	 * 
	 */
	@Override
	public int getLength(){
		//assumes the byteBuffer data are not consumed or inflated with the put/get API !
		int position = this.byteBuffer.position();
		int limit = this.byteBuffer.limit();//???
		int length = limit-position;
		assert(length>=0);
		return length;
	}
	
	/**
	 * 
	 */
	@Override
	public void setData(byte[] data){
		assert(data!=null);
		this.byteBuffer = ByteBuffer.wrap(data);
	}
	
	/**
	* 
	*/
	@Override
	public InfoSctp getInfo(){
		if( this.messageInfo==null ){
			return null;
		}
		//todo 
		return new InfoSunNioSctp(this.messageInfo);
	}
	
	/**
	* 
	*/
	@Override
	public void setInfo( InfoSctp sndrcvinfo ) throws Exception{
		//there should be only one stack activated : many implementations doesn't coexists at runtime
		assert(sndrcvinfo instanceof InfoSunNioSctp);
		//maybe we should clone instead of sharing instance?
		this.messageInfo = ((InfoSunNioSctp)sndrcvinfo).messageInfo;
	}
	
	/**
	 * 
	 */
	@Override
	public void clear(){
		this.byteBuffer = ByteBuffer.allocate(0);
		this.messageInfo = null;
	}

}

