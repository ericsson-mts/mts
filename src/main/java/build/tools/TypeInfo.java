/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import static build.tools.Main.xpath;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    public TypeInfo(Node node) throws XPathExpressionException {
        resolved = false;

        name = (String) xpath.evaluate("./@*[local-name() = 'name']", node, XPathConstants.STRING);

        extendedTypeName = (String) xpath.evaluate(".//*[local-name() = 'extension']/@*[local-name() = 'base']", node, XPathConstants.STRING);

        elementsNames = new LinkedList<>();
        NodeList elementsNodes = (NodeList) xpath.evaluate(".//*[local-name() = 'element']", node, XPathConstants.NODESET);
        for (int i = 0; i < elementsNodes.getLength(); i++) {
            elementsNames.add((String) xpath.evaluate("./@*[local-name() = 'name']", elementsNodes.item(i), XPathConstants.STRING));
        }

        attributes = new LinkedHashMap<>();
        NodeList attributesNodes = (NodeList) xpath.evaluate(".//*[local-name() = 'attribute']", node, XPathConstants.NODESET);
        for (int i = 0; i < attributesNodes.getLength(); i++) {
            AttributeInfo info = new AttributeInfo(attributesNodes.item(i));
            attributes.put(info.name, info);
        }
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
