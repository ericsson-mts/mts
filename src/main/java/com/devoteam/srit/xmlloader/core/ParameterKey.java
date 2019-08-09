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

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.Arrays;
import java.util.StringJoiner;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * @author emicpou
 * hierarchical parameter key
 */
@Immutable
public final class ParameterKey {

	/**
	 * immutable complete key
	 * could be shared between instances
	 */
	private final String path;

	/**
	 * immutable array of subkeys - decomposition of the path
	 * could be shared between instances
	 */
	private final String[] subkeys;

	/**
	 * immutable subkey offset pointing to the head (default to s0)
	 */
	private final int offset;
	
	/**
	 * constructor
	 * @param path splittable raw key
	 */
	public ParameterKey( String path ){
    	this.path = path.trim();
        this.subkeys = Utils.splitPath(this.path);
        this.offset = 0;
	}
	 
	/**
	 * constructor
	 * @param subkeys already split(-ted) keys
	 * @
	 * 
	 */
	public ParameterKey( String[] subkeys,String separator ){
		StringJoiner stringJoiner = new StringJoiner(separator);
		for( String subkey : subkeys ){
			stringJoiner.add(subkey);
		}		
		this.path = stringJoiner.toString();
		this.subkeys = subkeys.clone();
        this.offset = 0;
	}
	
	/**
	 * constructor
	 * @param path splittable raw key
	 * @param subkeys decomposition
	 */
	public ParameterKey( String path,String[] subkeys ){
    	this.path = path.trim();
        this.subkeys = subkeys.clone();
        this.offset = 0;
	}

	/**
	 * constructor
	 * @param params immutable key
	 * @param offset start offset
	 * 
	 */
	public ParameterKey( ParameterKey parameterKey,int offset ){
		this.path = parameterKey.path;
		this.subkeys = parameterKey.subkeys;
        this.offset = parameterKey.offset+offset;
	}
	
	/* 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.path;
	}
	
	/**
	 * @returns new instance with head key removed 
	 * 
	 */
	public ParameterKey shift(){
		return new ParameterKey( this,1 );
	}
	/**
	 * @returns new instance with head key removed 
	 * 
	 */
	public ParameterKey shift(int count){
		return new ParameterKey( this,count );
	}
	
	/**
	 * @returns if the key is empty
	 */
	public boolean isEmpty(){
		if( this.offset>=this.subkeys.length){
			return false;
		}
		return true;
	}
	
	/**
	 * @returns the subkeys count
	 */
	public int getSubkeysCount(){
		if(this.offset>=this.subkeys.length){
			return 0;
		}
		return this.subkeys.length-this.offset;
	}
	
	/**
	 * @param index
	 * @returns the subkey at position index
	 */
	public String getSubkey(int index){
		int subKeyIndex = this.offset+index;
		assert subKeyIndex<this.subkeys.length;
		return this.subkeys[subKeyIndex];
	}
	
	/**
	 * @param subkey candidate
	 * @returns status
	 */
	public boolean hasHeadSubkey( String subkey ){
		if(this.offset>=this.subkeys.length){
			return false;
		}
		String headSubkey = this.subkeys[this.offset];
		return headSubkey.equalsIgnoreCase(subkey);
	}
    
	/**
	 * @param subkey candidate
	 * @returns status
	 */
	@Nonnull
	public String getHeadSubkey() throws IndexOutOfBoundsException{
		if(this.offset>=this.subkeys.length){
			throw new IndexOutOfBoundsException();
		}
		return this.subkeys[this.offset];
	}

	/**
     * @param suffix
     * @returns
     */
    public boolean endsWith( String suffix ){
    	return this.path.endsWith(suffix);
    }
	
    /**
     * @param anotherString
     * @returns
     */
    public boolean equalsIgnoreCase( String anotherString ){
    	return this.path.equalsIgnoreCase(anotherString);
    }
	
    /**
     * @param s
     * @returns
     */
    public boolean contains( CharSequence s ){
    	return this.path.contains(s);
    }
    
    /**
     * @param ch
     * @return
     */
    public int indexOf(int ch  ){
    	return this.path.indexOf(ch);
    }
    
    /**
     * @param anotherString
     * @return
     */
    public int indexOf(String anotherString  ){
    	return this.path.indexOf(anotherString);
    }

    /**
     * 
     */
    public String substring(int beginIndex){
    	return this.path.substring(beginIndex);
    }

    /**
     * 
     */
    public String substring(int beginIndex,int endIndex ){
    	return this.path.substring(beginIndex,endIndex);
    }

    /**
     * 
     */
    public int length(){
    	return this.path.length();
    }
    
    
    
	/**
	 * @returns the subkeys (IMMUTABLE)
	 */
    public String[] getSubkeys()
    {
        if( this.offset==0 ){
        	return this.subkeys;
        }
        else{
    		assert this.offset<=this.subkeys.length;
        	return Arrays.copyOfRange(this.subkeys, this.offset, this.subkeys.length-this.offset );
        }
    }
}
