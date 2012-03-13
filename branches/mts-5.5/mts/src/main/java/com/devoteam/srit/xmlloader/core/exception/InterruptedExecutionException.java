/*
 * Created on Oct 22, 2004
 */
package com.devoteam.srit.xmlloader.core.exception;

/**
 * @author pn007888
 */
public class InterruptedExecutionException extends Exception {
	
	/**
	 * 
	 */
	public InterruptedExecutionException() {
		super();
	}
	
    /**
     * Constructor.
     * 
     * @param s exception description
     */
    public InterruptedExecutionException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public InterruptedExecutionException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public InterruptedExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}