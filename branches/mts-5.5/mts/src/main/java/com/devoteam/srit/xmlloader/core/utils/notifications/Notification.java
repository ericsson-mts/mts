/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.notifications;

import java.io.Serializable;

/**
 *
 * @author gpasquiers
 */
public class Notification<S, D> implements Serializable
{
    private S source;
    private D data;
    
    public Notification(S source, D data)
    {
        this.source = source;
        this.data = data;
    }
    
    public S getSource()
    {
        return this.source;
    }
    
    public D getData()
    {
        return this.data;
    }
}
