package com.devoteam.srit.xmlloader.core.utils;

/**
 * Generic wrapper for any Object
 * @author efabhen
 *
 */
public class GenericWrapper 
{
	public Object obj;

	public GenericWrapper(Object object)
	{
		this.obj = object;
	}
	
	public Object getObject()
	{
		return obj;
	}
	
	public void setObject(Object object)
	{
		this.obj = object;
	}
	
}
