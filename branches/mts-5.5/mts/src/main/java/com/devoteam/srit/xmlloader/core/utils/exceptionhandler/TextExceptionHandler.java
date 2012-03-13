/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.exceptionhandler;


import java.awt.Container;

/**
 *
 * @author gpasquiers
 */
public class TextExceptionHandler extends LogsExceptionHandler
{
    @Override
    public void display(Throwable t, Container container)
    {
        super.display(t, container);
        
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("- Exception stack trace --------------------------------\n");
        this.doDisplay(builder, t);
        builder.append("--------------------------------------------------------\n\n");
        System.out.println(builder.toString());
    }

    public void doDisplay(StringBuilder builder, Throwable t)
    {
        builder.append(t.getClass().getName() + ": " + t.getMessage() + "\n");
        
        for(StackTraceElement stackTraceElement:t.getStackTrace())
        {
            builder.append("        ").append(stackTraceElement + "\n");
        }
        
        if(null != t.getCause())
        {
            builder.append("nested ");
            doDisplay(builder, t.getCause());
        }
    }
}
