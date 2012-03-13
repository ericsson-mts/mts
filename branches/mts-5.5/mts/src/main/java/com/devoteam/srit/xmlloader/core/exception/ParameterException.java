/*
 * ParameterException.java
 *
 * Created on 8 octobre 2007, 10:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.exception;

/**
 *
 * @author gpasquiers
 */
public class ParameterException extends Exception
{
    public ParameterException(String text)
    {
        super(text);
    }
    
    public ParameterException(String text, Exception e)
    {
        super(text, e);
    }
}
