/*
 * Created on Oct 22, 2004
 */
package com.devoteam.srit.xmlloader.core.exception;

/**
 * @author pn007888
 */
public class ExecutionException extends Exception {
	
	/**
	 * 
	 */
	public ExecutionException() {
		super();
	}
	
    /**
     * Constructor.
     * 
     * @param s exception description
     */
    public ExecutionException(String message) {
    	super(message);
    }

    /**
     * @param cause
     */
    public ExecutionException(Throwable cause) {
    	super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public ExecutionException(String message, Throwable cause) {
    	super(message, cause);
    }

}