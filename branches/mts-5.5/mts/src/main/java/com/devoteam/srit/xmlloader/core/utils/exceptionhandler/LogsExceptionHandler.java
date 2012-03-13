/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.exceptionhandler;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import java.awt.Container;

/**
 *
 * @author gpasquiers
 */
public class LogsExceptionHandler implements ExceptionHandler
{
    public void display(Throwable t, Container container)
    {
        GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, t, "Exception occured: ", t);
    }
}
