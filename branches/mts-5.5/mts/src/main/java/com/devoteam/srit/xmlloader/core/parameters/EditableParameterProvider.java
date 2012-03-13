package com.devoteam.srit.xmlloader.core.parameters;

import java.io.Serializable;

/**
 * interface to implement in order to provide values for editable parameters
 * when parsing test file
 */
public interface EditableParameterProvider extends Serializable {

    /**
     * returns the value for the parameter named name
     *  - null value means that there is no value
     */
    public String getParameterValue(String name);
}
