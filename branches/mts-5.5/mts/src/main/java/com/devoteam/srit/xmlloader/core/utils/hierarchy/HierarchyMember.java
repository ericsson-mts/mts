/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.hierarchy;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public interface HierarchyMember<P, C> extends Serializable
{
    public P getParent();
    
    public List<C> getChildren();
    
    public void setParent(P parent);
    
    public void addChild(C child);

    public void removeChild(C child);
}
