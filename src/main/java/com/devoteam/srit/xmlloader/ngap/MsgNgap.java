package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.sctp.MsgTransportInfosSctp;
import com.ericsson.mts.asn1.BitArray;
import com.ericsson.mts.asn1.XMLFormatReader;
import com.ericsson.mts.asn1.XMLFormatWriter;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.List;

public class MsgNgap extends Msg {
    private Element dom4jNode;
    private byte[] binaryData;
    private final String XMLRootNodeName = "NGAP-PDU";
    private String NGAPPDUType;
    private String NGAPType;


    /**
     * Creates a new instance
     */
    public MsgNgap(Stack stack) throws IOException {
        super(stack);
    }

    public MsgNgap() {
        super();
    }

    //-----------------------------------------------------------------------------------------
    // generic methods for protocol request type result retransmission, transaction and session
    //-----------------------------------------------------------------------------------------

    /**
     * @return null if it's a request without answer
     * @throws Exception
     */
    @Override
    public TransactionId getTransactionId() throws Exception {
        return null;
    }

    @Override
    public MessageId getMessageId() throws Exception {
        return null;
    }

    @Override
    public SessionId getSessionId() throws Exception {
        return null;
    }


    /**
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() throws Exception {
        return true;
    }

    /**
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters
     */
    @Override
    public String getType() throws Exception {
        if (null == NGAPType) {
            NGAPType = (String) getParameter(XMLRootNodeName + "." + getNGAPPDUType() + ".value.*[1]").get(0);
        }
        return NGAPType;
    }

    /**
     * Get the PDU Type of NGAP message
     *
     * @return (initiatingMessage | successfuloutcome | unsuccessfuloutcome)
     * @throws Exception exception
     */
    private String getNGAPPDUType() throws Exception {
        if (null == NGAPPDUType) {
            NGAPPDUType = (String) getParameter(XMLRootNodeName + ".*[1]").get(0);
        }
        return NGAPPDUType;
    }

    /**
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters
     */
    @Override
    public String getResult() throws Exception {
        if (getNGAPPDUType().equalsIgnoreCase("successfuloutcome")) {
            return "NGAP_SUCCESS";
        } else if(getNGAPPDUType().equalsIgnoreCase("unsuccessfulOutcome")){
            return "NGAP_FAILURE";
        }
        return null;
    }

    /**
     * Add ppid for sctp protocol (60=NGAP)
     * @return sctptransportinfos
     * @throws Exception throw an exception if sctp protocol isn't set
     */
    @Override
    public TransportInfos getTransportInfos() throws Exception {
        MsgTransportInfosSctp msgTransportInfosSctp;
        if (transportInfos != null) {
            if (transportInfos instanceof MsgTransportInfosSctp) {
                msgTransportInfosSctp = (MsgTransportInfosSctp) transportInfos;
            } else {
                throw new RuntimeException("Only SCTP is supported for NGAP protocol");
            }
        } else {
            msgTransportInfosSctp = new MsgTransportInfosSctp();
        }
        msgTransportInfosSctp.getInfoSctp().setPpid(60);
        return msgTransportInfosSctp;
    }

    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /**
     * encode the message to binary data
     */
    @Override
    public byte[] encode() throws Exception {
        if (binaryData == null) {
            XMLFormatReader xmlFormatReader = new XMLFormatReader(convertToW3CElement(dom4jNode), XMLRootNodeName);
            BitArray bitArray = new BitArray();
            ((StackNgap) StackFactory.getStack(StackFactory.PROTOCOL_NGAP)).getAsn1Translator().encode(XMLRootNodeName, bitArray, xmlFormatReader);
            binaryData = bitArray.getBinaryArray();
            return binaryData;
        } else {
            return binaryData;
        }
    }


    /**
     * decode the message from binary data
     */
    @Override
    public void decode(byte[] data) throws Exception {
        if (dom4jNode == null) {
            XMLFormatWriter formatWriter = new XMLFormatWriter();
            InputStream binaryInputStream = new ByteArrayInputStream(data);
            ((StackNgap) StackFactory.getStack(StackFactory.PROTOCOL_NGAP)).getAsn1Translator().decode(XMLRootNodeName, binaryInputStream, formatWriter);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(formatWriter.getResult()), new StreamResult(writer));
            dom4jNode = DocumentHelper.parseText(writer.toString()).getRootElement();
        }
    }

    @Override
    public int getLength() throws Exception {
        return this.toXml().length();
    }

    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /**
     * Convert the message to XML document
     */
    @Override
    public String toXml() throws Exception {
        return dom4jNode.asXML();
    }

    /**
     * Parse the message from XML element
     */
    @Override
    public void parseFromXml(ParseFromXmlContext context, Element root, Runner runner) throws Exception {
        super.parseFromXml(context, root, runner);
        dom4jNode = root.element(XMLRootNodeName);
    }

    private org.w3c.dom.Element convertToW3CElement(Element element) throws IOException, SAXException, ParserConfigurationException {
        org.w3c.dom.Element element1 = stringToDom(element.asXML()).getDocumentElement();
        if (element1.getNodeType() == Node.ELEMENT_NODE) {
            if (!XMLRootNodeName.equals(element1.getNodeName())) {
                throw new RuntimeException("Error during XML parsing: no " + XMLRootNodeName + " found");
            }
            return element1;
        }
        throw new RuntimeException("Error during XML parsing : no " + XMLRootNodeName + " found");
    }

    private static Document stringToDom(String xmlSource)
            throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlSource)));
    }

    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------


    /**
     * Get a parameter from the message
     *
     * @param path path
     * @return parameter for a given path
     * @throws Exception exception
     */
    @Override
    public Parameter getParameter(String path) throws Exception {
        Parameter parameter = super.getParameter(path);
        if (parameter != null) {
            return parameter;
        }
        parameter = new Parameter();
        String[] params = new ParameterKey(path).getSubkeys();

        if (params.length < 1) {
            return null;
        }

        if (params[0].equalsIgnoreCase(XMLRootNodeName)) {
            path = String.format("/%s", path.replaceAll("\\.", "/"));
            org.dom4j.Document document = DocumentHelper.parseText(dom4jNode.asXML());
            XPath xpath = DocumentHelper.createXPath(path);
            List<org.dom4j.Node> nodeList = xpath.selectNodes(document);

            for (int i = 0; i < nodeList.size(); i++) {
                org.dom4j.Node node = nodeList.get(i);
                if (node.getNodeType() == org.dom4j.Node.ELEMENT_NODE) {
                    parameter.add(node.getName());
                } else if (node.getNodeType() == org.dom4j.Node.TEXT_NODE) {
                    parameter.add(node.getStringValue());
                }
            }
            return parameter;
        }
        return null;
    }
}
