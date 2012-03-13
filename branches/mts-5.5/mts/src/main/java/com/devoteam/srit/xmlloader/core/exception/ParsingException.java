/*
 * Created on Oct 22, 2004
 */
package com.devoteam.srit.xmlloader.core.exception;

/**
 * @author pn007888
 */
public class ParsingException extends Exception {
	
	/**
	 * 
	 */
	public ParsingException() {
		super();
	}

	/**
	 * @param message
	 */
	public ParsingException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ParsingException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
