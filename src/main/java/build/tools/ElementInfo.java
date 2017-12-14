/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import static build.tools.Main.xpath;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 *
 * @author egwepas
 */
public class ElementInfo {

    String name;
    String documentation;
    String appinfo;
    String typeName;
    TypeInfo type;

    public ElementInfo(Node node) throws XPathExpressionException {
        name = (String) xpath.evaluate("./@*[local-name() = 'name']", node, XPathConstants.STRING);
        typeName = (String) xpath.evaluate("./@*[local-name() = 'type']", node, XPathConstants.STRING);
        appinfo = (String) xpath.evaluate(".//*[local-name() = 'appinfo']", node, XPathConstants.STRING);
        documentation = (String) xpath.evaluate(".//*[local-name() = 'documentation']", node, XPathConstants.STRING);
    }

    public void resolveComplexType(Map<String, TypeInfo> cache) {
        type = cache.get(typeName);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(name + " (" + typeName + ";" + appinfo + ";" + documentation + ")\n");
        if (null != type) {
            for (AttributeInfo ai : type.attributes.values()) {
                string.append("    @ " + ai.name + " (" + ai.typeName + ";" + ai.appinfo + ";" + ai.documentation + ")\n");
            }
            for (ElementInfo ei : type.elements.values()) {
                string.append("    - " + ei.name + "\n");
            }
        }

        return string.toString();
    }
}
