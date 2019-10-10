package com.devoteam.srit.xmlloader.s1ap;

import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.sctp.DataSctp;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.asn1.PERTranscoder;
import com.ericsson.mts.asn1.PERTranslatorFactory;
import gp.utils.arrays.DefaultArray;

import java.util.*;

public class StackS1ap extends Stack {

    private final ASN1Translator asn1Translator = new ASN1Translator(new PERTranslatorFactory(new PERTranscoder(true, true)),
            Collections.singletonList(StackS1ap.class.getResourceAsStream("/asn1/grammar/S1AP/S1AP.asn")));

    /**
     * Create a new StackS1AP instance
     *
     * @throws Exception File I/O exception
     */
    public StackS1ap() throws Exception {
        super();
    }

    @Override
    public Msg readFromSCTPData(DataSctp chunk) throws Exception {
        DefaultArray array = new DefaultArray(chunk.getData());
        if (18 != (chunk.getInfo().getPpid())) {
            throw new RuntimeException("Not a S1AP message");
        }
        MsgS1ap msg = new MsgS1ap(this);
        msg.decode(array.getBytes());
        return msg;
    }

    public XMLElementReplacer getElementReplacer() {
        return XMLElementAVPParser.instance();
    }

    /**
     * Return ASN.1 translator to decode and encode S1AP message
     *
     * @return asn1 translator
     */
    ASN1Translator getAsn1Translator() {
        return asn1Translator;
    }
}
