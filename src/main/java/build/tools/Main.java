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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author egwepas
 */
public class Main {

    public static void main(String[] args) throws Exception {
        File source = new File(args[0]);
        File dest = new File(args[1]);
        dest.getParentFile().mkdirs();
        new Main(source, dest).work();
    }

    File source;
    File dest;
    String ns;
    Consumer<Node> dispatcher;
    BiConsumer<Node, ElementInfo> elementParser;
    BiConsumer<Node, AttributeInfo> attributeParser;
    BiConsumer<Node, TypeInfo> typeParser;

    public Main(File source, File dest) throws Exception {
        this.source = source;
        this.dest = dest;
    }

    public void work() throws Exception {
        Map<String, ElementInfo> elementInfoCache = new LinkedHashMap<>();
        Map<String, TypeInfo> complexTypeInfoCache = new LinkedHashMap<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(source);
            doc.getDocumentElement().normalize();

            dispatcher = new Consumer<Node>() {
                @Override
                public void accept(Node n) {
                    String name = n.getNodeName();
                    name = name.substring(name.indexOf(':') + 1);
                    switch (name) {
                        case "element":
                            ElementInfo ei = new ElementInfo();
                            ei.name = getAttributeValue(n, "name");
                            ei.typeName = getAttributeValue(n, "type");
                            forEachChildElement(n, c -> elementParser.accept(c, ei));
                            elementInfoCache.put(ei.name, ei);
                            break;
                        case "complexType":
                            TypeInfo ti = new TypeInfo();
                            ti.name = getAttributeValue(n, "name");
                            forEachChildElement(n, c -> typeParser.accept(c, ti));
                            complexTypeInfoCache.put(ti.name, ti);
                            break;
                        default:
                            forEachChildElement(n, this);
                    }
                }
            };

            elementParser = new BiConsumer<Node, ElementInfo>() {
                @Override
                public void accept(Node n, ElementInfo ei) {
                    String name = n.getNodeName();
                    name = name.substring(name.indexOf(':') + 1);
                    switch (name) {
                        case "element":
                            dispatcher.accept(n);
                            break;
                        case "attribute":
                            AttributeInfo ai = new AttributeInfo();
                            ai.name = getAttributeValue(n, "name");
                            forEachChildElement(n, c -> attributeParser.accept(c, ai));
                            ei.attributes.put(ai.name, ai);
                            break;
                        case "documentation":
                            ei.documentation = n.getTextContent();
                            break;
                        default:
                            forEachChildElement(n, c -> this.accept(c, ei));
                            break;
                    }
                }
            };

            attributeParser = new BiConsumer<Node, AttributeInfo>() {
                @Override
                public void accept(Node n, AttributeInfo ai) {
                    String name = n.getNodeName();
                    name = name.substring(name.indexOf(':') + 1);
                    switch (name) {
                        case "documentation":
                            ai.documentation = n.getTextContent();
                            break;
                        case "appinfo":
                            ai.appinfo = n.getTextContent();
                            break;
                        default:
                            forEachChildElement(n, c -> this.accept(c, ai));
                            break;
                    }
                }
            };

            typeParser = new BiConsumer<Node, TypeInfo>() {
                @Override
                public void accept(Node n, TypeInfo ti) {
                    String name = n.getNodeName();
                    name = name.substring(name.indexOf(':') + 1);
                    switch (name) {
                        case "attribute":
                            AttributeInfo ai = new AttributeInfo();
                            ai.name = n.getAttributes().getNamedItem("name").getNodeValue();
                            forEachChildElement(n, c -> attributeParser.accept(c, ai));
                            ti.attributes.put(ai.name, ai);
                            break;
                        case "element":
                            ti.elementsNames.add(getAttributeValue(n, "name"));
                            dispatcher.accept(n);
                            break;
                        case "extension":
                            ti.extendedTypeName = getAttributeValue(n, "base");
                        default:
                            forEachChildElement(n, c -> this.accept(c, ti));
                            break;
                    }
                }
            };

            forEachChildElement(doc, dispatcher);

            for (TypeInfo complexTypeInfo : complexTypeInfoCache.values()) {
                complexTypeInfo.resolve(elementInfoCache, complexTypeInfoCache);
            }

            for (ElementInfo elementInfo : elementInfoCache.values()) {
                elementInfo.resolveComplexType(complexTypeInfoCache);
            }

            try (FileWriter writer = new FileWriter(dest)) {
                Mustache.Compiler compiler = Mustache.compiler();
                compiler.computeNullValue("false");
                compiler.defaultValue(null);
                compiler.compile(new FileReader("src/main/resources/xsddoctemplate.html.jmustache")).execute(elementInfoCache, writer);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void forEachChildElement(Node node, Consumer<Node> consumer) {
        NodeList list = node.getChildNodes();
        int len = list.getLength();
        for (int i = 0; i < len; i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                consumer.accept(list.item(i));
            }
        }
    }

    public String getAttributeValue(Node node, String name) {
        Node attribute = node.getAttributes().getNamedItem(name);
        if (null != attribute) {
            return attribute.getNodeValue();
        } else {
            return null;
        }
    }
    
    public String stripPrefix(String name) {
        return name.substring(name.indexOf(':') + 1);
    }    
}
