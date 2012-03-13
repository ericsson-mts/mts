/*
 * XMLElementReplacer.java
 *
 * Created on 30 mai 2007, 22:45:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.util.List;
import org.dom4j.Element;


/**
 *
 * @author Gege
 */
public interface XMLElementReplacer
{
    
    /**
     * Implement replacement behaviour in a class implementing this interface
     *
     * @param element oldElement
     * @return newElement
     */
    public List<Element> replace(Element element) throws Exception;
}
