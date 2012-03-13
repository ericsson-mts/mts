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
public interface ExceptionHandler
{
    public void display(Throwable t, Container container);
}
