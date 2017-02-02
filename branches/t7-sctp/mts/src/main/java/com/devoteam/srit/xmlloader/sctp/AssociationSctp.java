/**
 * 
 */
package com.devoteam.srit.xmlloader.sctp;

/**
 * @author emicpou
 *
 */
public interface AssociationSctp{
		
	/**
	 * @return stringification value
	 */
	public String toString();
	
	/**
	 * @return the local handle to the SCTP association
	 */
	public int getId();
	
}
