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
public class AssertException extends Exception
{
    public AssertException(String text)
    {
        super(text);
    }
    
    public AssertException(String text, Exception e)
    {
        super(text, e);
    }
}
