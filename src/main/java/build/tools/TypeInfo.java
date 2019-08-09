/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author egwepas
 */
public class TypeInfo {

    String name;
    String extendedTypeName;
    List<String> elementsNames;
    Map<String, AttributeInfo> attributes;
    Map<String, ElementInfo> elements;
    boolean resolved;

    public TypeInfo() {
        resolved = false;
        attributes = new LinkedHashMap<>();
        elements = new LinkedHashMap<>();
        elementsNames = new LinkedList<>();
    }

    public void resolve(Map<String, ElementInfo> elementsCache, Map<String, TypeInfo> typesCache) {
        if (!resolved) {
            resolved = true;

            elements = new LinkedHashMap<>();
            for (String name : elementsNames) {
                elements.put(name, elementsCache.get(name));
            }

            TypeInfo type = typesCache.get(extendedTypeName);
            if (null != type) {
                type.resolve(elementsCache, typesCache);
                for (ElementInfo extendedTypeElement : type.elements.values()) {
                    if (!elements.containsKey(extendedTypeElement.name)) {
                        elements.put(extendedTypeElement.name, extendedTypeElement);
                    }
                }

                for (AttributeInfo extendedAttribute : type.attributes.values()) {
                    if (!attributes.containsKey(extendedAttribute.name)) {
                        attributes.put(extendedAttribute.name, extendedAttribute);
                    }
                }
            }
        }
    }
}
