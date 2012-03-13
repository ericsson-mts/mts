/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.pluggable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class AbstractPluggableComponent implements PluggableComponent
{
    private LinkedList<PluggableName> pluggableNames;
            
    public AbstractPluggableComponent()
    {
        this.pluggableNames = new LinkedList<PluggableName>();
    }
            
    public List<PluggableName> getPluggableNames()
    {
        return Collections.unmodifiableList(this.pluggableNames);
    }
    
    protected void addPluggableName(PluggableName pluggableName)
    {
        this.pluggableNames.add(pluggableName);
    }

}
