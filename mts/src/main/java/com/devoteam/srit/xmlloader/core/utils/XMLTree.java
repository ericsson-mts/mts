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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.tree.AbstractElement;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;


/**
 *
 * @author Gege
 */
public class XMLTree implements Serializable
{
    private Element root;
    
    private HashMap<Element, List<Element>> elementsMap;
    
    private LinkedList<Element> elementsOrder;
    
    private Lock lock;
    
    public XMLTree(Element root)
    {
        this(root, true);
    }
    
    public XMLTree(Element root, boolean duplicate)
    {
        elementsOrder = new LinkedList<Element>();
        elementsMap = new HashMap<Element, List<Element>>();
        lock = new ReentrantLock();
        if(duplicate)
        {
            this.root = root.createCopy();
        }
        else
        {
            this.root = root;
        }
    }

    public XMLTree(Element root, boolean duplicate, String regex)
    {
        this(root, duplicate);
        this.compute(regex, true);
    }

    public XMLTree(Element root, boolean duplicate, String regex, boolean recurse)
    {
        this(root, duplicate);
        this.compute(regex, recurse);
    }
    
    public Element getTreeRoot()
    {
        return root;
    }
    
  
    /**
     * Identify all the Elements containing an attribute or text in which some
     * parts match the regex.
     * @param regex Regular expression used to find attributes.
     */
    public void compute(String regex, boolean recurse)
    {
        listMatchingElements(root, regex, recurse);
    }

    public void lock(){
        lock.lock();
    }
    
    public void unlock(){
        lock.unlock();
    }
    
    /**
     * Replace nodes previously identified by the method compute()
     * accordingly to the XMLElementReplacer.
     * @throws java.lang.Exception
     */
    public void replace(XMLElementReplacer replacer, ParameterPool parameterPool) throws Exception
    {
        if(null == replacer)
        {
            throw new ExecutionException("XMLElementReplacer must not be null");
        }

        for(Element e:elementsOrder)
        {
            List<Element> newNodesList = replacer.replace(e, parameterPool);
            
            if(newNodesList.isEmpty())
            {
                Element element = new DefaultElement("removedElement");
                newNodesList.add(element);
            }
            
            elementsMap.put(e, newNodesList);
            
            AbstractElement parent = (AbstractElement) e.getParent();
            if(null != parent)
            {
                for(Element newChild:newNodesList)
                {
                    DefaultElementInterface.insertNode((DefaultElement) parent, e, newChild);
                }
                e.detach();
                parent.remove(e);

                if(1 == newNodesList.size() && e == root){
                    root = newNodesList.get(0);
                }
            }
            else if(1 == newNodesList.size())
            {
                newNodesList.get(0);
                root = newNodesList.get(0);
                e.detach();
            }
            else
            {
                // some error
            }
        }
    }
    
    /**
     * Restore this XMLTree to his original state
     */
    public void restore()
    {
        for(int i=elementsOrder.size()-1 ; i>=0; i--)
        {
            Element e = elementsOrder.get(i);
            List<Element> list = elementsMap.get(e);
            if(null != list && !list.isEmpty()){
                // all Elements in this list should have the same parent
                Element parent = list.get(0).getParent();
                if(null != parent)
                {
                    DefaultElementInterface.insertNode((DefaultElement) parent, list.get(0), e);
                    for(Node oldChild:list)
                    {
                        parent.remove(oldChild);
                    }

                    if(1 == list.size() && e == root){
                        root = list.get(0);
                    }

                }
                else
                {
                    root = list.get(0);
                }
            }
        }
    }
    
    private void listMatchingElements(Element element, String regex, boolean recurse)
    {
        //
        // First check attributes
        //
        List<Attribute> namedNodeMap = element.attributes();
        if(null != namedNodeMap)
        {
            for(Attribute attribute:namedNodeMap)
            {
                String value = attribute.getValue();
                if(Utils.containsRegex(value, regex))
                {
                    if(false == elementsMap.containsKey(element))
                    {
                        elementsMap.put(element, null);
                        elementsOrder.addFirst(element);
                    }
                }
            }
        }
        
        //
        // Then check text
        //
        if(Utils.containsRegex(element.getText(), regex))
        {
            if(false == elementsMap.containsKey(element))
            {
                elementsMap.put(element, null);
                elementsOrder.addFirst(element);
            }
        }
        
        //
        // Finally elements
        //
        if(recurse)
        {
            List<Element> childrens = element.elements();
            for(Element child:childrens)
            {
                listMatchingElements(child, regex, recurse);
            }
        }
    }
    
    /**
     * Prints the actual XML tree. Method for debug/checking purposes
     */
    public String toString()
    {
        return Utils.unescapeEntities(root.asXML());
    }

    public XMLTree clone()
    {
        return new XMLTree((Element) root.clone());
    }
}
