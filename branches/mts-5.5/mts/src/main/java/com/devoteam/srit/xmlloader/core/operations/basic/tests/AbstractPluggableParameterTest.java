/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.core.operations.basic.tests;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.AbstractPluggableComponent;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableParameterTest;
import java.util.Map;
import java.util.Map.Entry;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
abstract public class AbstractPluggableParameterTest extends AbstractPluggableComponent implements PluggableParameterTest
{
    public AbstractPluggableParameterTest()
    {
        super();
    }
    abstract public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception;
}
