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
