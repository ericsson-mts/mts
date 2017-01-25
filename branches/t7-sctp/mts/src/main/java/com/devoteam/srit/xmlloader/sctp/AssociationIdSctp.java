/**
 * 
 */
package com.devoteam.srit.xmlloader.sctp;

/**
 * @author emicpou
 *
 */
public interface AssociationIdSctp{
	
	/**
	 * @return a deep cloned instance
	 */
	public Object clone() throws CloneNotSupportedException;
	
	/**
	 * @return stringification value
	 */
	public String toString();
	
	/**
	 * @return the local handle to the SCTP association
	 */
	public long getValue();
	
	/**
	 * @param value the local handle to the SCTP association
	 */
	public void setValue( long value );

}
