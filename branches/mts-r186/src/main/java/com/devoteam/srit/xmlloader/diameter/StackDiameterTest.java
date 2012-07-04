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

package com.devoteam.srit.xmlloader.diameter;

import java.io.File;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.devoteam.srit.xmlloader.core.ScenarioReference;
import com.devoteam.srit.xmlloader.core.ScenarioRunner;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLLoaderEntityResolver;

import dk.i1.diameter.AVP;
import dk.i1.diameter.Message;
import dk.i1.diameter.ProtocolConstants;
import dk.i1.diameter.AVP_Integer32;
import dk.i1.diameter.Utils;

public class StackDiameterTest extends TestCase {
                    
    static private StackDiameterTest test = new StackDiameterTest();
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        test.testXMLParsedMessage();
    }       
    
    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void setUp() throws Exception {
        super.setUp();
               
    }

    /**
     * junit setup method.
     * @throws Exception if any problem occurs.
     */
    protected void tearDown() throws Exception {
        super.tearDown();                 
    }
    
    /**
     * testXMLParsedMessage() method.
     *
     */
    public void testXMLParsedMessage() throws Exception{
        Tester.buildInstance();
              
        Element scElem = readMessageFromXml("../conf/diameter/diameterMessage.xml");
        
        MsgDiameter request = (MsgDiameter) StackFactory.getStack(StackFactory.PROTOCOL_DIAMETER).parseMsgFromXml(true, scElem, null);
        MsgDiameter response = new MsgDiameter(prepareResponse(request.getMessage(), ProtocolConstants.DIAMETER_RESULT_SUCCESS));
        
        ScenarioReference src = new ScenarioReference("srcScenario");
        ScenarioRunner srcRunner = new ScenarioRunner(null, src);
        ScenarioReference dest = new ScenarioReference("destScenario");
        ScenarioRunner destRunner = new ScenarioRunner(null, dest);
        
        int maxIter = Config.getConfigByName("diameter.properties").getInteger("NB_ITERATION");
        for (int i = 0; i < maxIter; i++) {
            // scenario level
            // request = new MsgDiameter(null);
            // request.parseXml(scElem, new Variables());        
            
            // System.out.println(i);
            request.getMessage().hdr.hop_by_hop_identifier = 0;
            request.getMessage().hdr.end_to_end_identifier = 0;
            
            StackFactory.getStack(StackFactory.PROTOCOL_DIAMETER).sendMessage(request, srcRunner, destRunner, srcRunner);
            int hopByHop = request.getMessage().hdr.hop_by_hop_identifier;
            int endToEnd = request.getMessage().hdr.end_to_end_identifier;
            // wait the requestn to be received            
            MsgDiameter result = (MsgDiameter) destRunner.getBufferMsg().readMessageFromStack(30000);
            destRunner.getBufferMsg().removeMsgFromStack(result);
            
            /*
            while(!dest.getMsgStack().keySet().iterator().hasNext()) {            
            }
            */
            //destSession.doWait(30000);
            /*
            if (dest.getMsgStack().keySet().iterator().hasNext()) {
                request = (MsgDiameter) dest.getMsgStack().keySet().iterator().next();
            }                       
            */
            response = new MsgDiameter(prepareResponse(result.getMessage(), ProtocolConstants.DIAMETER_RESULT_SUCCESS));
            // response.getMessage().hdr.hop_by_hop_identifier = hopByHop;
            // response.getMessage().hdr.end_to_end_identifier = endToEnd;
            StackFactory.getStack(StackFactory.PROTOCOL_DIAMETER).sendMessage(response, srcRunner, destRunner, srcRunner);
            // wait the requestn to be received
            response = (MsgDiameter) srcRunner.getBufferMsg().readMessageFromStack(30000);
            srcRunner.getBufferMsg().removeMsgFromStack(response);
            /*
            while(!src.getMsgStack().keySet().iterator().hasNext()) {            
            }
            response = (MsgDiameter) src.getMsgStack().keySet().iterator().next();
            */
            /*
            if (src.getMsgStack().keySet().iterator().hasNext()) {
                response = (MsgDiameter) src.getMsgStack().keySet().iterator().next();
            }*/                       

        }
               
        assertNotNull(response);
        AVP avp = response.getMessage().find(ProtocolConstants.DI_RESULT_CODE);
        assertNotNull(avp);       
        
        assertEquals(((AVP_Integer32)new AVP_Integer32(avp)).queryValue(), ProtocolConstants.DIAMETER_RESULT_SUCCESS);

    }

    /**
     * create the diameter Header.
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    private Message prepareResponse(Message request, int resultCode) {
        Message message = new Message();
        
        // set the header
        message.prepareAnswer(request);        
        /*
        message.hdr.setRequest(false);
        message.hdr.setProxiable(request.hdr.isProxiable());
        message.hdr.application_id = request.hdr.application_id;        
        message.hdr.command_code = request.hdr.command_code;
        message.hdr.end_to_end_identifier = request.hdr.end_to_end_identifier;
        message.hdr.hop_by_hop_identifier = request.hdr.hop_by_hop_identifier;
        */        
        
        message.add(new AVP_Integer32(ProtocolConstants.DI_RESULT_CODE, resultCode));
        
        Utils.setMandatory_RFC3588(message);
        
        return message;
    }


    /**
     * read the message from XML scenario
     * @param    request The request to send
     * @return The answer to the request. Null if there is no answer (all peers down, or other error)
     */
    private Element readMessageFromXml(String fileName) throws Exception {
        String filename = "../conf/diameter/diameterMessage.xml" ;

        // Instanciate the XML parser
        SAXReader reader = new SAXReader(false);
        reader.setEntityResolver(new XMLLoaderEntityResolver());
        
        File file = new File(filename);
        org.dom4j.Document doc = reader.read(file);
    
        // scenario level
        return (Element) doc.getRootElement().elements().get(0);
    }

}
