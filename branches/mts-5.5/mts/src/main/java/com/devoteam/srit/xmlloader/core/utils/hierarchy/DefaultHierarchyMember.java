/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils.hierarchy;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public class DefaultHierarchyMember<P, C> implements HierarchyMember<P, C>
{
    private P parent;
    
    private LinkedList<C> children = new LinkedList<C>();

    public P getParent()
    {
        return this.parent;
    }

    public List<C> getChildren()
    {
        return this.children;
    }

    public void setParent(P parent)
    {
        this.parent = parent;
    }

    public void addChild(C child)
    {
        if (!children.contains(child))
        {
            children.add(child);
        }
        else
        {
            throw new RuntimeException("toto");
        }
    }

    public void removeChild(C child)
    {
        if (children.contains(child))
        {
            children.remove(child);
        }
    }
}
