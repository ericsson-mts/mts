/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.exception;

/**
 *
 * @author jbor
 */
public class ParsingInputStreamException extends Exception {

    private String buffer;

	/**
	 *
	 */
	public ParsingInputStreamException() {
		super();
	}

	/**
	 * @param message
	 */
	public ParsingInputStreamException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ParsingInputStreamException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ParsingInputStreamException(String message, Throwable cause) {
		super(message, cause);
	}

    /**
     * @param message
     * @param buffer
     */
    public ParsingInputStreamException(String message, String buffer){
        super(message);
        this.buffer = buffer;
    }

    public String getBuffer(){
        return this.buffer;
    }
}
