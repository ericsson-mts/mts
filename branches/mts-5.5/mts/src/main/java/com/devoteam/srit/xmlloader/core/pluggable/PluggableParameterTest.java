/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.pluggable;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import java.util.Map;
import org.dom4j.Element;

/**
 * Interface that defines a parameters operator.<br/>
 * An operator carries itself the (legacy) names it will be registered with.<br/>
 * @author gpasquiers
 */
public interface PluggableParameterTest extends PluggableComponent
{
    /**
     * Main method that does the work. Throws an assert Exception if the test
     * failed. Does nothing else.
     * @param runner runner that is executing this operation (thus this operator)
     * @param operands map of operands given to this operator
     * @param testName the name of the called test
     * @param parameterName the name of the tested parameter
     */
    public void test(Runner runner, Map<String, Parameter> operands, String testName, String parameterName) throws Exception;
}
