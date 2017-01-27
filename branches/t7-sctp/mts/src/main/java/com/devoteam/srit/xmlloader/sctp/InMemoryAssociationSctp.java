/**
 * 
 */
package com.devoteam.srit.xmlloader.sctp;

/**
 * @author emicpou
 *
 */
public class InMemoryAssociationSctp implements AssociationSctp{
	
	protected long id;
	
	public InMemoryAssociationSctp( long id ){
		this.id = id;
	}
	
	/**
	 * @return stringification value
	 */
	@Override
	public String toString(){
		return Long.toString(this.id);
	}
	
	/**
	 * @return the local handle to the SCTP association
	 */
	@Override
	public long getId(){
		return this.id;
	}
	
	/**
	 * @param id the local handle to the SCTP association
	 */
	public void setId( long id ){
		this.id = id;
	}

}
