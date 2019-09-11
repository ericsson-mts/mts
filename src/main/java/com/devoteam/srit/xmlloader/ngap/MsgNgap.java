package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.s1ap.MsgS1ap;
import com.ericsson.mts.asn1.ASN1Translator;
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

    protected int getPpid() {
        return 60;
    }
}
