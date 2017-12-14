/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package build.tools;

import com.samskivert.mustache.Mustache;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author egwepas
 */
public class Main {

    public static XPath xpath = XPathFactory.newInstance().newXPath();
    private static Map<String, ElementInfo> elementInfoCache = new LinkedHashMap<>();
    private static Map<String, TypeInfo> complexTypeInfoCache = new LinkedHashMap<>();

    public static void main(String[] args) throws Exception {
        String source = args[0];
        String dest = args[1];
        new File(dest).getParentFile().mkdirs();
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(source));
            doc.getDocumentElement().normalize();

            NodeList elements = (NodeList) xpath.evaluate("//*[local-name() = 'element']", doc, XPathConstants.NODESET);
            for (int i = 0; i < elements.getLength(); i++) {
                Node element = elements.item(i);
                ElementInfo ei = new ElementInfo(element);
                elementInfoCache.put(ei.name, ei);
            }

            NodeList complexTypes = (NodeList) xpath.evaluate("//*[local-name() = 'complexType']", doc, XPathConstants.NODESET);
            for (int i = 0; i < complexTypes.getLength(); i++) {
                Node complexType = complexTypes.item(i);
                TypeInfo cti = new TypeInfo(complexType);
                complexTypeInfoCache.put(cti.name, cti);
            }

            for (TypeInfo complexTypeInfo : complexTypeInfoCache.values()) {
                complexTypeInfo.resolve(elementInfoCache, complexTypeInfoCache);
            }

            for (ElementInfo elementInfo : elementInfoCache.values()) {
                elementInfo.resolveComplexType(complexTypeInfoCache);
            }

            try (FileWriter writer = new FileWriter(dest)) {
                Mustache.compiler().compile(new FileReader("src/main/resourceS/xsddoctemplate.html.jmustache")).execute(elementInfoCache, writer);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
