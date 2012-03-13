/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.exceptionhandler;

/**
 *
 * @author gpasquiers
 */
public class ExceptionHandlerSingleton
{
    static private ExceptionHandler instance = new LogsExceptionHandler();
    
    static public void setInstance(ExceptionHandler newInstance)
    {
        instance = newInstance;
    }
    
    static public ExceptionHandler instance()
    {
        return instance;
    }
}
