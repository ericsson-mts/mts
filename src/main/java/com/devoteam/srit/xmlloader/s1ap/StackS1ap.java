package com.devoteam.srit.xmlloader.s1ap;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.sctp.DataSctp;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.asn1.PERTranslatorFactory;
import dk.i1.diameter.node.Node;
import gp.utils.arrays.DefaultArray;

import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class StackS1ap extends Stack {
    private final ASN1Translator asn1Translator = new ASN1Translator(new PERTranslatorFactory(true),
            Collections.singletonList(StackS1ap.class.getResourceAsStream("/asn1/grammar/S1AP/S1AP.asn")));

    /**
     * Create a new StackS1AP instance
     *
     * @throws Exception File I/O exception
     */
    public StackS1ap() throws Exception {
        super();
        // configure stack trace parameters
        FileHandler fh = new FileHandler("../logs/s1apStack.log");
        // logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        Node.logger.addHandler(fh);
        String stringLevel = getConfig().getString("TRACE_LEVEL");
        Level traceLevel = Level.parse(stringLevel);
        Node.logger.setLevel(traceLevel);
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "traceLevel : ", traceLevel);
        Node.logger.warning("traceLevel = " + traceLevel);
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

    /**
     * Return ASN.1 translator to decode and encode S1AP message
     *
     * @return asn1 translator
     */
    ASN1Translator getAsn1Translator() {
        return asn1Translator;
    }
}
