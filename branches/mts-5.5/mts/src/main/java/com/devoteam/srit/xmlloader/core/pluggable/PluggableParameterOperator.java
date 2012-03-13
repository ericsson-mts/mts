/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.pluggable;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import java.util.List;
import java.util.Map;

/**
 * Interface that defines a parameters operator.<br/>
 * An operator carries itself the (legacy) names it will be registered with.<br/>
 * @author gpasquiers
 */
public interface PluggableParameterOperator extends PluggableComponent
{
    /**
     * Main method that does the work.
     * @param runner runner that is executing this operation (thus this operator)
     * @param operands map of operands given to this operator
     * @param operatorName the name of the called operator
     * @param resultantName the name of the resultant
     * @return the resultant Parameter.
     * @throws ExecutionException 
     */
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String operatorName, String resultantName) throws Exception;
}
