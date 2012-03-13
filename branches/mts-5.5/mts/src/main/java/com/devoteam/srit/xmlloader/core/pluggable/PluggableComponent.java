/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.pluggable;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public interface PluggableComponent extends Serializable
{
    /**
     * List of the names this pluggable component has.
     * Each name has it's own priority and deprecated status.
     * @return
     */
    public List<PluggableName> getPluggableNames();
}
