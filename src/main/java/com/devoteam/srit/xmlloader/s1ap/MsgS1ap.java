package com.devoteam.srit.xmlloader.s1ap;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.ngap.StackNgap;
import com.devoteam.srit.xmlloader.sctp.MsgTransportInfosSctp;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.asn1.BitArray;
import com.ericsson.mts.asn1.XMLFormatReader;
import com.ericsson.mts.asn1.XMLFormatWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import com.ericsson.mts.nas.message.AbstractMessage;
import com.ericsson.mts.nas.registry.Registry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.io.DOMWriter;
import org.w3c.dom.NodeList;

import static com.ericsson.mts.nas.writer.XMLFormatWriter.bytesToHex;


public class MsgS1ap extends Msg {

    protected Element element;
    protected byte[] binaryData;
    private String pduType;

    /**
     * Creates a new instance
     */
    public MsgS1ap(Stack stack) throws IOException {
        super(stack);
    }

    protected String getXmlRootNodeName(){
        return "S1AP-PDU";
    }

    protected ASN1Translator getASN1Translator(){
        return ((StackS1ap) this.stack).getAsn1Translator();
    }

    protected AbstractMessage getNASTranslator() throws IOException { return ((StackNgap) this.stack).getNASTranslator(); }

    protected Registry getRegistryNas() throws IOException { return ((StackNgap) this.stack).getRegistry5GSNasTranslator(); }

    protected int getPpid(){
        return 18;
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
     * Get the type of the message Used for message filtering with "type"
     * attribute and for statistic counters
     */
    @Override
    public String getType() throws Exception {
        if (null == type) {
            type = (String) getParameter(getXmlRootNodeName() + "." + getPduType() + ".value.*[1]").get(0);
        }
        return type;
    }

    /**
     * Get the PDU Type of S1AP message
     *
     * @return (initiatingMessage | successfuloutcome | unsuccessfuloutcome)
     * @throws Exception exception
     */
    private String getPduType() throws Exception {
        if (null == pduType) {
            pduType = (String) getParameter(getXmlRootNodeName() + ".*[1]").get(0);
        }
        return pduType;
    }

    /**
     * Get the result of the message (null if request) Used for message
     * filtering with "result" attribute and for statistic counters
     */
    @Override
    public String getResult() throws Exception {
        if (getPduType().equalsIgnoreCase("successfulOutcome")) {
            return "SUCCESS";
        } else if (getPduType().equalsIgnoreCase("unsuccessfulOutcome")) {
            return "FAILURE";
        }
        return null;
    }

    /**
     * Add ppid for sctp protocol (18=S1AP)
     *
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
                throw new RuntimeException("Only SCTP is supported for " + protocol + " protocol");
            }
        } else {
            msgTransportInfosSctp = new MsgTransportInfosSctp();
        }
        msgTransportInfosSctp.getInfoSctp().setPpid(getPpid());
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
        return binaryData;
    }

    /**
     * decode the message from binary data
     */
    @Override
    public void decode(byte[] data) throws Exception {
        binaryData = data;
        XMLFormatWriter formatWriter = new XMLFormatWriter();
        InputStream binaryInputStream = new ByteArrayInputStream(data);
        getASN1Translator().decode(getXmlRootNodeName(), binaryInputStream, formatWriter);
        element = formatWriter.getResult();
    }

    @Override
    public int getLength() throws Exception {
        return binaryData.length;
    }

    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------
    /**
     * Convert the message to XML document
     */
    @Override
    public String toXml() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(element), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Parse the message from XML element
     */
    @Override
    public void parseFromXml(ParseFromXmlContext context, org.dom4j.Element root, Runner runner) throws Exception {
        super.parseFromXml(context, root, runner);
        this.element = (Element) new DOMWriter().write(DocumentHelper.createDocument(root.elementIterator().next().createCopy())).getDocumentElement();

        //Decode the NAS part of the binary message
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile("//NAS-PDU");
        NodeList nodes = (NodeList) expr.evaluate(element, XPathConstants.NODESET);

        if(nodes.getLength() > 0) {
            for (int i = 0; i < nodes.getLength(); i++) {

                Element nasPDUElement = (Element) nodes.item(i);
                NodeList childList = nasPDUElement.getChildNodes();

                for(int j = 0; j < childList.getLength(); j++){

                    if(childList.item(j).getNodeType() == Node.ELEMENT_NODE){

                        if(nasPDUElement.getFirstChild().getNextSibling() != null){
                            if(nasPDUElement.getFirstChild().getNextSibling().getNodeType() == Node.ELEMENT_NODE){
                                com.ericsson.mts.nas.reader.XMLFormatReader formatReader = new com.ericsson.mts.nas.reader.XMLFormatReader((Element) nasPDUElement.getFirstChild().getNextSibling(),"xml");
                                byte[] resultDecode = getNASTranslator().encode(getRegistryNas(),formatReader);

                                //delete the existing child of the node
                                while (nasPDUElement.hasChildNodes()){
                                    nasPDUElement.removeChild(nasPDUElement.getFirstChild());
                                }

                                //set the value of PDU to the NAS-PDU node
                                nasPDUElement.setTextContent(bytesToHex(resultDecode));
                            }
                        }
                    }
                }
            }
        }

        XMLFormatReader xmlFormatReader = new XMLFormatReader(element, getXmlRootNodeName());
        BitArray bitArray = new BitArray();
        getASN1Translator().encode(getXmlRootNodeName(), bitArray, xmlFormatReader);
        this.binaryData = bitArray.getBinaryArray();
    }

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

        if (params[0].equalsIgnoreCase(getXmlRootNodeName())) {
            path = "/" + path.replace('.', '/');
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath().evaluate(path, element, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parameter.add(node.getNodeName());
                } else if (node.getNodeType() == Node.TEXT_NODE) {
                    parameter.add(node.getTextContent());
                }
            }
            return parameter;
        }
        return null;
    }
}
