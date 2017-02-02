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

package com.devoteam.srit.xmlloader.sctp;

import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 * @author emicpou
 *
 */
public abstract class InfoSctp {
	
	/**
	 * @return the stream id value
	 */
	public abstract short getStreamId();
	
	/**
	 * @param the stream id value
	 */
	public abstract void setStreamId( short streamId ) throws Exception;
	
	/**
	 * @return the ssn value
	 */
	public abstract short getSsn();
	
	/**
	 * @param the ssn value
	 */
	public abstract void setSsn( short ssn ) throws Exception;
	
	/**
	 * flags values
	 */
	static public final short FLAG_UNORDERED = 1;
	static public final short FLAG_ADDR_OVER = 2;
	static public final short FLAG_ABORT = 4;
	static public final short FLAG_EOF = 8;
	static public final short FLAG_SENDALL = 16;
	
	/**
	 * @return the flags value
	 */
	public abstract short getFlags();
	
	/**
	 * @param the flags value
	 */
	public abstract void setFlags( short flags ) throws Exception;
	
	/**
	 * @return the ppid value (little endian)
	 */
	public abstract int getPpid();
	
	/**
	 * @param the ppid value
	 */
	public abstract void setPpid( int ppid ) throws Exception;
	
	/**
	 * @return the context value
	 */
	public abstract int getContext();
	
	/**
	 * @param the context value
	 */
	public abstract void setContext( int context ) throws Exception;
	
	/**
	 * @return the ttl value
	 */
	public abstract int getTtl();
	
	/**
	 * @param the ttl value
	 */
	public abstract void setTtl( int ttl ) throws Exception;
	
	/**
	 * @return the tsn value
	 */
	public abstract int getTsn();
	
	/**
	 * @param the tsn value
	 */
	public abstract void setTsn( int tsn ) throws Exception;

	/**
	 * @return the tsn value
	 */
	public abstract int getCumtsn();
	
	/**
	 * @param the cumtsn value
	 */
	public abstract void setCumtsn( int cumtsn ) throws Exception;
	
	/**
	 * @return the AssociationSctp value (copy or immutable reference)
	 */
	public abstract AssociationSctp getAssociation();
	
	/**
	 * @param associationId the AssociationSctp value
	 */
	public abstract void setAssociationId( int associationId ) throws Exception;
	
	/**
	 * @param src the source object
	 */
	public void set( InfoSctp src ) throws Exception {
		copy( src,this );
	}

	/**
	 * @param src the source object
	 * @return status
	 */
	public boolean trySet( InfoSctp src ){
		return tryCopy( src,this );
	}
	
	/**
	 * @param dst the stream id value
	 */
	public void get( InfoSctp dst ) throws Exception {
		copy( this,dst );
	}
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	public boolean tryGet( InfoSctp dst ){
		return tryCopy( this,dst );
	}
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	protected static boolean tryCopy( InfoSctp src,InfoSctp dst ){
		boolean status = true;
		try{
			copy( src,dst );
		}catch(Exception e){
			status = false;
		}
		return status;
	}
	
	/**
	 * @param dst the stream id value
	 * @return status
	 */
	protected static void copy( InfoSctp src,InfoSctp dst )throws Exception{
		dst.setStreamId( src.getStreamId() );
		dst.setSsn( src.getSsn() );
		dst.setFlags( src.getFlags() );
		dst.setPpid( src.getPpid() );
		dst.setContext( src.getContext() );
		dst.setTtl( src.getTtl() );
		dst.setTsn( src.getTsn() );
		dst.setCumtsn( src.getCumtsn() );
		dst.setAssociationId( src.getAssociation().getId() );
	}
	
	/**
	 * 
	 */
	public String toString(){
		return this.toXml();
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public String toXml(){
		String xml = "";

		xml += "<InfoSctp>";
		xml += System.lineSeparator();
		
		xml += "<stream>" + StringPrinter.getStreamId( this ) + "</stream>";
		xml += System.lineSeparator();
		
		xml += "<ssn>" + StringPrinter.getSsn( this ) + "</ssn>";
		xml += System.lineSeparator();

		xml += "<ppid>" + StringPrinter.getPpid( this ) + "</ppid>";
		xml += System.lineSeparator();

		xml += "<flags>" + StringPrinter.getFlags( this ) + "</flags>";
		xml += System.lineSeparator();

		xml += "<context>" + StringPrinter.getContext( this ) + "</context>";
		xml += System.lineSeparator();

		xml += "<ttl>" + StringPrinter.getTtl( this ) + "</ttl>";
		xml += System.lineSeparator();

		xml += "<tsn>" + StringPrinter.getTsn( this ) + "</tsn>";
		xml += System.lineSeparator();

		xml += "<cumtsn>" + StringPrinter.getCumtsn( this ) + "</cumtsn>";
		xml += System.lineSeparator();

		xml += "<aid>" + StringPrinter.getAssociationId( this ) + "</aid>";
		xml += System.lineSeparator();
		
		xml += "</InfoSctp>";

		return xml;
	}
	
	/**
	 * helper class to convert unsigned values to strings
	 * @author emicpou
	 * TODO implementation based on generic
	 */
	public static class StringPrinter{
		
		static String getStreamId( InfoSctp infoSctp ){
			short usValue = infoSctp.getStreamId();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getSsn( InfoSctp infoSctp ){
			short usValue = infoSctp.getSsn();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getFlags( InfoSctp infoSctp ){
			short usValue = infoSctp.getFlags();
			long ulValue = Short.toUnsignedLong( usValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getPpid( InfoSctp infoSctp ){
			int uiValue = infoSctp.getPpid();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getContext( InfoSctp infoSctp ){
			int uiValue = infoSctp.getContext();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getTtl( InfoSctp infoSctp ){
			int uiValue = infoSctp.getTtl();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getTsn( InfoSctp infoSctp ){
			int uiValue = infoSctp.getTsn();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getCumtsn( InfoSctp infoSctp ){
			int uiValue = infoSctp.getCumtsn();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}
		static String getAssociationId( InfoSctp infoSctp ){
			int uiValue = infoSctp.getAssociation().getId();
			long ulValue = Integer.toUnsignedLong( uiValue );
			String strValue = Long.toUnsignedString( ulValue );
			return strValue;
		}

	}
}
