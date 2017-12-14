/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import static build.tools.Main.xpath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 *
 * @author egwepas
 */
public class AttributeInfo {

    String name;
    String typeName;
    String documentation;
    String appinfo;

    public AttributeInfo(Node node) throws XPathExpressionException {
        name = (String) xpath.evaluate(".//@*[local-name() = 'name']", node, XPathConstants.STRING);
        typeName = (String) xpath.evaluate(".//@*[local-name() = 'type']", node, XPathConstants.STRING);
        appinfo = (String) xpath.evaluate(".//*[local-name() = 'appinfo']", node, XPathConstants.STRING);
        documentation = (String) xpath.evaluate(".//*[local-name() = 'documentation']", node, XPathConstants.STRING);
    }
}
