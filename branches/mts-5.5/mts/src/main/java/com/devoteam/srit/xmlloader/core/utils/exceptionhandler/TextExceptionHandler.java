/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
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
