package com.devoteam.srit.xmlloader.core.parameters;

import java.util.HashMap;

/**
 * interface to implement in order to provide values for editable parameters
 * when parsing test file
 */
public class EditableParameterProviderHashMap implements EditableParameterProvider {
    private HashMap<String, String> _map;

    public EditableParameterProviderHashMap(HashMap<String, String> map){
        _map = map;
    }

    @Override
    public String getParameterValue(String name){
        return _map.get(name);
    }
}
