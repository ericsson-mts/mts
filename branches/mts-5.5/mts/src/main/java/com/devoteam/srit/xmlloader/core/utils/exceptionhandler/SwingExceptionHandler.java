/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.exceptionhandler;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.awt.Container;

/**
 *
 * @author gpasquiers
 */
public class SwingExceptionHandler extends LogsExceptionHandler
{
    private Container defaultContainer;
    
    public SwingExceptionHandler(Container container)
    {
        this.defaultContainer = container;
    }

    @Override
    public void display(Throwable t, Container container)
    {
        super.display(t, container);

        if(null == container) container = defaultContainer;

        Utils.showError(container, t.getMessage(), t);
    }
}
