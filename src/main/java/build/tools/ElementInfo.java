/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author egwepas
 */
public class ElementInfo {

    String name;
    String documentation;
    String appinfo;
    String typeName;

    Map<String, AttributeInfo> attributes;
    TypeInfo type;

    public ElementInfo() {
        attributes = new LinkedHashMap<>();
    }

    public void resolveComplexType(Map<String, TypeInfo> cache) {
        if (null != typeName) {
            type = cache.get(typeName);
            if (null != type) {
                for (Map.Entry<String, AttributeInfo> entry : type.attributes.entrySet()) {
                    if (!attributes.containsKey(entry.getKey())) {
                        attributes.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }    
    
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(name + " (" + typeName + ";" + appinfo + ";" + documentation + ")\n");
        for (AttributeInfo ai : attributes.values()) {
            string.append("    @ " + ai.name + " (" + ai.typeName + ";" + ai.appinfo + ";" + ai.documentation + ")\n");
        }
        if (null != type) {
            for (ElementInfo ei : type.elements.values()) {
                string.append("    - " + ei.name + "\n");
            }
        }

        return string.toString();
    }
}
