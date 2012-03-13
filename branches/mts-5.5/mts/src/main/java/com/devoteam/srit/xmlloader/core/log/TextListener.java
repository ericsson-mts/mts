/*
 * Created on Oct 20, 2004
 */
package com.devoteam.srit.xmlloader.core.log;

/**
 * An interface to allow displaying of text with a level of severity
 * @author pn007888
 */
public interface TextListener
{
    /**
     * Print a text with a log level.
     * @param e a text event containing text + logLevel
     */
    public void printText(TextEvent e);
    
    /**
     * Free resources.
     */
    public void dispose() ;
}
