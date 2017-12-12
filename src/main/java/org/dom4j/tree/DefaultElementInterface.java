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
*//*
 * DefaultElementInterface.java
 *
 * Created on 14 juin 2007, 12:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dom4j.tree;

import org.dom4j.Node;

/**
 *
 * @author gpasquiers
 */
public class DefaultElementInterface
{

    public static void insertNode(DefaultElement parent, Node reference, Node element)
    {
        int index = parent.indexOf(reference);
        
        parent.addNewNode(index, element);
    }
    
    public static void replaceNode(DefaultElement parent, Node reference, Node element)
    {
        int index = parent.indexOf(reference);
        
        if(null != element)
        {
            parent.addNewNode(index, element);
        }
        parent.removeNode(reference);
    }

    public static void addNode(DefaultElement parent, Node newNode, int index)
    {
        parent.addNode(index, newNode);
    }

}
