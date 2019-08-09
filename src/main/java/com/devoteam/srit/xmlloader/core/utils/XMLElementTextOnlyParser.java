/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultElement;
import org.dom4j.tree.DefaultElementInterface;
import org.dom4j.tree.DefaultText;

/**
 * Generic replacer to replace only the content of the XML tag
 *
 * @author gpasquiers
 */
public class XMLElementTextOnlyParser implements XMLElementReplacer, Serializable 
{

    static private XMLElementReplacer instance = null;

    static public XMLElementReplacer instance() {
        if (null == instance) {
            instance = new XMLElementTextOnlyParser();
        }
        return instance;
    }

    protected XMLElementTextOnlyParser() {
    }

    public List<Element> replace(Element element, ParameterPool variables) throws Exception {
        List<Element> result = new LinkedList();
        result.add(element.createCopy());
        element = result.get(0);

        Iterator nodesIterator = element.nodeIterator();
        HashMap<Node, Node> nodesToReplace = new HashMap<Node, Node>();

        boolean alreadyNext = false;

        while (nodesIterator.hasNext()) {
            Node node = null;
            if (!alreadyNext) {
                node = (Node) nodesIterator.next();
            }

            Node lastTextNode = null;
            String lastTextNodeText = "";
            alreadyNext = false;

            //
            // We put all successive TEXT Nodes into one node ( there is some fragmentation i don't understand )
            //
            while (null != node && node.getNodeType() == Node.TEXT_NODE) {
                alreadyNext = true;
                lastTextNode = (Text) node;
                lastTextNodeText += lastTextNode.getText();

                // this node will be deleted later ... if not overwritten in this hashmap
                nodesToReplace.put(lastTextNode, null);
                if (nodesIterator.hasNext()) {
                    node = (Node) nodesIterator.next();
                }
                else {
                    node = null;
                }
            }

            //
            // We process normally the CDATA Nodes
            // 
            if (null != node && node.getNodeType() == Node.CDATA_SECTION_NODE) {
                lastTextNode = (Node) node;
                lastTextNodeText = lastTextNode.getText();
            }

            //
            // We do nothing for the other type Nodes
            // 
            if (null == lastTextNode) {
                continue;
            }

            lastTextNode.setText(lastTextNodeText);

            //
            // Now that we have only one, complete, TEXT node or one CDATA node to proceed
            //
            Node textNode = (Node) lastTextNode;

            String text = textNode.getText();
            String out = "";

            int endOfLine;
            String line;

            //
            // Transform all \r\n, in \n
            //
            //text = Utils.replaceNoRegex(text, "\r", "");

            while (text.length() > 0) {
                //
                // Read a line
                //
                endOfLine = text.indexOf("\n");

                if (endOfLine == -1) {
                    line = text;
                    text = "";
                }
                else {
                    line = text.substring(0, endOfLine + 1);
                    text = text.substring(endOfLine + 1);
                }

                //
                // Replace line if it contains at least a variable
                //
                if (Parameter.containsParameter(line)) {
                    List<String> results = variables.parse(line);

                    for (String s : results) {
                        out += s;
                    }
                }
                else {
                    out += line;
                }
            }

            //
            // Set new text into new AVP
            //
            Text newTextNode = new DefaultText(out);
            nodesToReplace.put(textNode, newTextNode);
        }

        for (Node key : nodesToReplace.keySet()) {
            DefaultElementInterface.replaceNode((DefaultElement) element, key, nodesToReplace.get(key));
        }

        if (result.size() != 1) {
            throw new ExecutionException("Size of result for XMLElementTextOnlyParser should be 1");
        }

        return result;
    }
}
