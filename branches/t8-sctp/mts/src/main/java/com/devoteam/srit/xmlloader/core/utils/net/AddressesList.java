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

package com.devoteam.srit.xmlloader.core.utils.net;

import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.net.InetAddress;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author emicpou
 *
 */

public class AddressesList{
	
	/**
	 * storage : an ordered collection
	 */
	protected List<InetAddress> addresses = new LinkedList<InetAddress>();
	
	/*
	 * @param addressesString the addresses string list (',' separator)
	 * @return status 
	 */
	public boolean setFromAddressesStringWithSeparator( String addressesStringWithSeparator ){
		this.addresses.clear();
		
		boolean status = true;
		String[] addressesStringArray = Utils.splitNoRegex(addressesStringWithSeparator, ",");
		for( String addressString:addressesStringArray){
			try{
				InetAddress address = InetAddress.getByName(addressString);
				assert(address!=null);
				this.addresses.add(address);
			}catch(Exception exception){
				status = false;
			}
		}
		return status;
	}
	
	public boolean setFromAddressString( String addressString ){
		this.addresses.clear();
		
		boolean status = true;
		try{
			InetAddress address = InetAddress.getByName(addressString);
			assert(address!=null);
			this.addresses.add(address);
		}catch(Exception exception){
			status = false;
		}
		return status;
	}
	
	public String toStringWithSeparator(){
		String addressesString = "";
		String separator = "";
		for( InetAddress address:this.addresses){
			addressesString += separator;
			addressesString += address.getHostAddress();			
			separator = ",";
		}
		return addressesString;
	}
	
    public boolean set( List<InetAddress> addresses ){
		this.addresses.clear();
		for( InetAddress address:addresses){
			InetAddress addressClone = AddressesList.clone(address);
			this.addresses.add(addressClone);
		}
    	return true;
    }
	
    public boolean set( AddressesList addressesList ){
		this.addresses.clear();
		for( InetAddress address:addressesList.addresses){
			InetAddress addressClone = AddressesList.clone(address);
			this.addresses.add(addressClone);
		}
    	return true;
    }
	
	/**
	 * @return the addresses
	 */
	public List<InetAddress> getAll(){
		return this.addresses;
	}
	
	/**
	 * TODO implement immutability
	 * @return the addresses
	 */
	//@Immutable
	public List<InetAddress> getAllImmutable(){
		return this.addresses;
	}
	
	/**
	 * TODO implement immutability
	 * @return the first address
	 */
	//@Nullable
	//@Immutable
	public InetAddress getHeadImmutable(){
		if( this.addresses.isEmpty()){
			return null;
		}
		Iterator<InetAddress> iterator = this.addresses.iterator();
		assert(iterator.hasNext());
		InetAddress head = iterator.next();
		return head;
	}
	
	/**
	 * equals method : same length, same addresses ip bytes in the same order
	 */
    public boolean equals(AddressesList comparedAddressesList)
    {
    	return AddressesList.equals( this,comparedAddressesList );
    }
        
	/**
	 * equals method : same length, same addresses ip bytes in the same order
	 */
    public static boolean equals(AddressesList al1,AddressesList al2){
    	//optimization
    	if( al1.addresses.size()!=al2.addresses.size()){
    		return false;
    	}
		Iterator<InetAddress> ai1 = al1.addresses.iterator();
		Iterator<InetAddress> ai2 = al2.addresses.iterator();
		while( ai1.hasNext()&&ai1.hasNext() ){
			InetAddress a1 = ai1.next();
			InetAddress a2 = ai2.next();
			if( !equals(a1,a2) ){
				return false;
			}
		}
		assert( !ai1.hasNext()&&!ai2.hasNext() );
		return true;
    }
	
    /**
	 * equals method : same addresses ip bytes
     */
	public static boolean equals( InetAddress a1,InetAddress a2 ){
		byte[] ab1 = a1.getAddress();
		byte[] ab2 = a2.getAddress();
		boolean status = Arrays.equals(ab1, ab2);
		return status;
	}
	
	/**
	 * 			
	 * @param referenceAddress the address to be cloned
	 * @return a clone of the reference object
	 * 
	 * // TODO improve be cloning the hostname too
	 * 
	 */
	public static InetAddress clone( InetAddress referenceAddress ){
		InetAddress clonedAddress = null;
		try{
			clonedAddress = InetAddress.getByAddress(referenceAddress.getAddress());
		}
		catch(Exception exception){
		}
		assert(clonedAddress!=null);
		assert( equals(referenceAddress,clonedAddress) );
		return clonedAddress;
	}

}
