package com.devoteam.srit.xmlloader.ngap;

import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.sctp.DataSctp;
import com.ericsson.mts.asn1.ASN1Translator;
import com.ericsson.mts.asn1.PERTranscoder;
import com.ericsson.mts.asn1.PERTranslatorFactory;
import com.ericsson.mts.nas.message.AbstractMessage;
import com.ericsson.mts.nas.registry.Registry;
import dk.i1.diameter.node.Node;
import gp.utils.arrays.DefaultArray;

//import java.rmi.registry.Registry;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    private  Registry commonRegistryNasTranslator = new Registry();
    private  Registry registry5GSNasTranslator = new Registry();
    private  Registry registry5gsSessionManagementMessages = new Registry();
    private  AbstractMessage nasTraslator = createNASTranslator();

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
//        if (60 != (chunk.getInfo().getPpid())) {
//            throw new RuntimeException("Not a NGAP message");
//        }
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

    private AbstractMessage createNASTranslator() throws Exception {

        Config config = Config.getConfigByName("ngap.properties");
        String directory = "../conf/nas/grammar/5gs/NAS-5GS-"+config.getString("nas.VERSION","152");



        commonRegistryNasTranslator.loadMessages(new FileInputStream(directory+"/dictionary5GSCommonMessages.yaml"));
        commonRegistryNasTranslator.loadInformationElements(new FileInputStream(directory+"/dictionary5GSCommonInformationElements.yaml"));

        registry5GSNasTranslator.loadMessages(new FileInputStream(directory+"/dictionnary5GSMobilityManagementMessages.yaml"));
        registry5GSNasTranslator.loadInformationElements(new FileInputStream(directory+"/dictionnary5GSMobilityManagementInformationElements.yaml"));

        registry5gsSessionManagementMessages.loadMessages(new FileInputStream(directory+"/dictionnary5GSSesionManagementMessages.yaml"));
        registry5gsSessionManagementMessages.loadInformationElements(new FileInputStream(directory+"/dictionnary5GSSesionManagementInformationElements.yaml"));


        registry5GSNasTranslator.mergeRegistry(commonRegistryNasTranslator);
        registry5GSNasTranslator.mergeRegistry(registry5gsSessionManagementMessages);

        registry5GSNasTranslator.init();

        return registry5GSNasTranslator.getMessage("L3MessageWrapper");
    }

    public AbstractMessage getNASTranslator() {
        return nasTraslator;
    }

    public Registry getRegistry5GSNasTranslator() {
        return registry5GSNasTranslator;
    }
}
