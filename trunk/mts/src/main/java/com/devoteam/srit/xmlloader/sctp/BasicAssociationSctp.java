/**
 * 
 */
package com.devoteam.srit.xmlloader.sctp;

/**
 * An implementation of AssociationSctp which stores informations in memory
 *
 * @author emicpou
 */
public class BasicAssociationSctp implements AssociationSctp{
	
	protected int id;
	
	public BasicAssociationSctp( int id ){
		this.id = id;
	}
	
	/**
	 * @return stringification value
	 */
	@Override
	public String toString(){
		return Integer.toUnsignedString(this.id);
	}
	
	/**
	 * @return the local handle to the SCTP association
	 */
	@Override
	public int getId(){
		return this.id;
	}
	
	/**
	 * @param id the local handle to the SCTP association
	 */
	public void setId( int id ){
		this.id = id;
	}

}
