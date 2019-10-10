package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.sctp.DataSctp;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.asn1.PERTranscoder;
import com.ericsson.mts.asn1.PERTranslatorFactory;
import gp.utils.arrays.DefaultArray;

import java.util.Arrays;

public class StackNgap extends Stack {

    private final ASN1Translator asn1Translator = new ASN1Translator(new PERTranslatorFactory(new PERTranscoder(true, true)), Arrays.asList(
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-Common-Definitions.asn"),
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-Constant-Definitions.asn"),
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-Container-Definitions.asn"),
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-Elementary-Procedure-Definitions.asn"),
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-Information-Element-Definitions.asn"),
            StackNgap.class.getResourceAsStream("/asn1/grammar/NGAP/NGAP-PDU-Definitions.asn"))
    );

    /**
     * Create a new StackNgap instance
     *
     * @throws Exception File I/O exception
     */
    public StackNgap() throws Exception {
        super();
    }

    @Override
    public Msg readFromSCTPData(DataSctp chunk) throws Exception {

        DefaultArray array = new DefaultArray(chunk.getData());
        if (60 != (chunk.getInfo().getPpid())) {
            throw new RuntimeException("Not a NGAP message");
        }
        MsgNgap msg = new MsgNgap(this);
        msg.decode(array.getBytes());
        return msg;
    }

    public XMLElementReplacer getElementReplacer() {
        return XMLElementAVPParser.instance();
    }

    /**
     * Return ASN.1 translator to decode and encode NGAP message
     *
     * @return asn1 translator
     */
    ASN1Translator getAsn1Translator() {
        return asn1Translator;
    }
}
