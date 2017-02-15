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

package com.devoteam.srit.xmlloader.sctp.lksctp;

import com.devoteam.srit.xmlloader.sctp.*;

import dk.i1.sctp.*;

/**
 * @author emicpou
 *
 */
public class DataLksctp implements DataSctp {
	
	/**
	 * reference on the implementation object
	 */
    private SCTPData sctpData;
	
	public DataLksctp(){
		this.sctpData = new SCTPData( new byte[0] );
	}
	
	public DataLksctp( byte[] data ){
		assert(data!=null);
		this.sctpData = new SCTPData( data );
	}

    public DataLksctp( DataSctp chunk ){
		assert(chunk!=null);
		assert( chunk instanceof DataLksctp );
		DataLksctp chunkLksctp = (DataLksctp)chunk;
		SCTPData chunkScptData = chunkLksctp.sctpData;
		//a deep clone would be safer!
		this.sctpData = chunkScptData;
		assert(this.sctpData!=null);
	}
	
	public DataLksctp( SCTPData chunk ){
		//a deep clone would be safer!
		this.sctpData = chunk;
		assert(this.sctpData!=null);
	}	
	
	/**
	 * @return reference on the implementation object
	 */
	public SCTPData getSCTPData(){
		assert(this.sctpData!=null);
		return this.sctpData;
	}
	
	/**
	 * 
	 */
	@Override
	public byte[] getData(){
		return this.sctpData.getData();
	}
	
	/**
	 * 
	 */
	@Override
	public int getLength(){
		return this.sctpData.getLength();
	}
	
	/**
	 * 
	 */
	@Override
	public void setData(byte[] data) throws Exception{
		this.sctpData.setData(data);
	}
	
	/**
	* 
	*/
	@Override
	public InfoSctp getInfo(){
		if(this.sctpData.sndrcvinfo==null){
			return null;
		}
		return new InfoLksctp(this.sctpData.sndrcvinfo);
	}
	
	/**
	* 
	*/
	@Override
	public void setInfo( InfoSctp sndrcvinfo ) throws Exception{
		this.sctpData.sndrcvinfo = new sctp_sndrcvinfo();
		InfoLksctp dst = new InfoLksctp(this.sctpData.sndrcvinfo);
		dst.set(sndrcvinfo);
	}
	
	/**
	 * 
	 */
	@Override
	public void clear() throws Exception{
		this.sctpData = new SCTPData( new byte[0] );
	}

}

