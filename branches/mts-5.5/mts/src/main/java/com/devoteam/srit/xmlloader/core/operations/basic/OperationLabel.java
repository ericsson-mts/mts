/*
 * OperationLabel.java
 *
 * Created on 15 mai 2007, 08:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;

import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class OperationLabel extends Operation
{

    private String name;

    /**
     * Creates a new instance of OperationLabel
     */
    public OperationLabel(Element root)
    {
        super(root);
        this.name = null;;
    }
    
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);
        
        this.name = this.getAttribute("name");

        return null;
    }

    public String getLabelName(Runner runner) throws Exception
    {
        if(null == this.name)
        {
            replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);
            return this.getAttribute("name");
        }
        return this.name;
    }
}
