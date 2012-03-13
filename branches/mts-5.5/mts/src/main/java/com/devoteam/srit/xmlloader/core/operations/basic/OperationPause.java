package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementDefaultParser;


import org.dom4j.Element;

/**
 * OperationPause operation
 * 
 * 
 * @author JM. Auffret
 */
public class OperationPause extends Operation
{
    /**
     * Constructor
     * 
     * 
     * @param name Name of the operation
     * @param pause OperationPause value
     */
    public OperationPause(Element root)
    {
        super(root);
    }
    
    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     */
    public Operation execute(Runner runner) throws Exception
    {
        restore();

        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);                

        // Replace elements in XMLTree
        replace(runner, new XMLElementDefaultParser(runner.getParameterPool()), TextEvent.Topic.CORE);

        //
        // Read attribute
        //
        float pause ;

        if(null != getAttribute("seconds"))
        {
            pause = Float.parseFloat(getAttribute("seconds"));
            pause *= 1000;
            
        }
        else if(null != getAttribute("milliseconds"))
        {
            pause = Float.parseFloat(getAttribute("milliseconds"));           
        }
        else
        {
            throw new ExecutionException("Duration not specified for operation "  + this);
        }

        //
        // Do pause
        //
        if (pause >= 0) 
        {
        	Utils.pauseMilliseconds((long) pause);
        }
        else
        {
            GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Operation Pause : negative value. We ignore it.");        	
        }
        
        
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Pause of ", (float) pause / 1000, "sec");
        
        return null;
    }
}
