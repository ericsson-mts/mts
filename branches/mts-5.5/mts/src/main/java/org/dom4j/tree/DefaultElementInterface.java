/*
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
