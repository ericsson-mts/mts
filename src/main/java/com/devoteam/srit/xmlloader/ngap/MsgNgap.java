package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.s1ap.MsgS1ap;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.nas.BitInputStream;
import com.ericsson.mts.nas.message.AbstractMessage;
import com.ericsson.mts.nas.registry.Registry;

import com.ericsson.mts.nas.writer.XMLFormatWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.DatatypeConverter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MsgNgap extends MsgS1ap {

    public MsgNgap(Stack stack) throws IOException {
        super(stack);
    }

    @Override
    protected String getXmlRootNodeName() {
        return "NGAP-PDU";
    }

    protected ASN1Translator getASN1Translator() {
        return ((StackNgap) this.stack).getAsn1Translator();
    }

    protected AbstractMessage getNASTranslator() throws IOException { return ((StackNgap) this.stack).getNASTranslator(); }

    protected Registry getRegistryNas() throws IOException { return ((StackNgap) this.stack).getRegistry5GSNasTranslator(); }


    protected int getPpid() {
        return 60;
    }

    @Override
    public byte[] encode() throws Exception {
        return super.encode();
    }

    @Override
    public void decode(byte[] data) throws Exception {
        super.decode(data);

        //Decode the NAS part of the binary message
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression expr = xpath.compile("//NAS-PDU");
        NodeList nodes = (NodeList) expr.evaluate(element, XPathConstants.NODESET);

        if(nodes.getLength() > 0) {
            for(int i = 0; i < nodes.getLength(); i++){
                XMLFormatWriter formatWriter = new XMLFormatWriter();
                Element nasPDUElement = (Element) nodes.item(i);

                BitInputStream bitInputStream = new BitInputStream(new ByteArrayInputStream(DatatypeConverter.parseHexBinary(nasPDUElement.getTextContent())));
                getNASTranslator().decode(getRegistryNas(),bitInputStream, formatWriter);

                Element xml = formatWriter.getResultElement();
                //delete the existing child of the node
                while (nasPDUElement.hasChildNodes()){
                    nasPDUElement.removeChild(nasPDUElement.getFirstChild());
                }
                //replace the current node by the new Element
                nasPDUElement.appendChild(nasPDUElement.getOwnerDocument().adoptNode(xml));
            }
        }
    }

    @Override
    public void parseFromXml(ParseFromXmlContext context, org.dom4j.Element root, Runner runner) throws Exception {
        super.parseFromXml(context, root, runner);
    }
}
