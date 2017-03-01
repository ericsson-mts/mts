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

package com.devoteam.srit.xmlloader.mgcp;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.text.Header;
import com.devoteam.srit.xmlloader.core.coding.text.MsgParser;
import com.devoteam.srit.xmlloader.core.coding.text.TextMessage;

/**
 *
 * @author indiaye
 */
public class MsgMgcp extends Msg {

    private TextMessage message = null;

    /** Creates a new instance */
    public MsgMgcp(Stack stack) 
    {
        super(stack);       
    }
    
    /** 
     * Return true if the message is a request else return false
     */
	@Override
    public boolean isRequest() throws Exception 
	{
        return ((MGCPCommandLine) this.message.getGenericfirstline()).isIsRequest();
    }

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
    public String getType() throws Exception 
	{
        String method = "";
        if (isRequest()) {
            Parameter commandLineMethod = this.getParameter("MGCPCommandLine.MGCPVerb");
            if (commandLineMethod.length() > 0) {
                method = commandLineMethod.get(0).toString();
            }
        }
        return method;

    }

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult() throws Exception 
    {
        return getParameter("MGCPCommandLine.responseCode").get(0).toString();
    }
    
    
    //-------------------------------------------------
    // methods for the encoding / decoding of the message
    //-------------------------------------------------

    /** 
     * encode the message to binary data 
     */
    @Override
    public byte[] encode() throws Exception 
    {
        return message.getMessage().getBytes();
    }
    
    /** 
     * decode the message from binary data 
     */
    public void decode(byte[] data) throws Exception
    {
        this.message = new TextMessage("MGCP", true, 0, null);
        String text = new String(data);
        this.message.parse(text);
        this.message.setGenericfirstline(new MGCPCommandLine(this.message.getFirstLineString()));
    }
    
    
    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------
    
    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
    	String ret = super.toShortString();
    	ret += "\n";
        ret += ((MGCPCommandLine) (message.getGenericfirstline())).getLine();
		String transId = ((MGCPCommandLine) (message.getGenericfirstline())).getTransactionId().toString();
		ret += "\n";
        ret += "<MESSAGE transactionId=\"" + transId + "\">";
        return ret;
    }

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() throws Exception 
    {
        return this.message.getMessage().toString();
    }
    
    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    { 
    	super.parseFromXml(request,root,runner);

    	String text = root.getText();
    	decode(text.getBytes());
    }
    
    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message 
     */
    @Override
    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if (var != null) {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
        
        if (params.length >= 1 && params[0].equalsIgnoreCase("MGCPCommandLine")) {
            MGCPCommandLine mgcpline = ((MGCPCommandLine) (message.getGenericfirstline()));
            if (params.length == 1) {
                //---------------------------------------------------------------------- mgcpline -
                var.add(mgcpline.getLine());
                return var;
            }
            //---------------------------------------------------------------------- mgcpline:MGCPVerb -
            if (params.length == 2 && params[1].equalsIgnoreCase("MGCPVerb")) {
                var.add(mgcpline.getMGCPVerb());
                return var;
            }
            //---------------------------------------------------------------------- mgcpline:transactionId -
            if (params.length == 2 && params[1].equalsIgnoreCase("transactionId")) {
                var.add(mgcpline.getTransactionId());
                return var;
            }
            //---------------------------------------------------------------------- mgcpline:responseCode -
            if (params.length == 2 && params[1].equalsIgnoreCase("responseCode")) {
                var.add(mgcpline.getResponseCode());
                return var;
            }
            //---------------------------------------------------------------------- mgcpline:Version -
            if (params.length == 2 && params[1].equalsIgnoreCase("Version")) {

                var.add(mgcpline.getMGCPversion());
                return var;
            }
             if (params.length == 2 && params[1].equalsIgnoreCase("endpointName")) {

                var.add(mgcpline.getEndpointName());
                return var;
            }
        } else if ((params.length > 1 && params[0].equalsIgnoreCase("Parameter"))) {

            if (addMGCPHeaderGenericXXX(var, params, message.getHeader(params[1]))) {
                return var;
            }
        } else if ((params.length == 1 && params[0].equalsIgnoreCase("Parameter"))) {

            var.add(message.getHeaders());
                return var;
         
        }
        else if (params[0].toLowerCase().startsWith("content")) {
            message.addContentParameter(var, params, path);
            return var;
        } else {
            Parameter.throwBadPathKeywordException(path);
        }

        return var;


    }

    /** Get the elements of Via header of this message */
    private boolean addMGCPHeaderGenericXXX(Parameter var, String[] params, Header header) throws Exception {
      if(params.length == 2) {
            var.addHeader(header);
            return true;
        } else if (params[2].equalsIgnoreCase("Attribute")) {

            if (params.length == 3) {
                Header token = header.parseParameter(null, ",", ';', "()", "\"\"");
                var.addHeader(token);
                return true;
            } else {
                String value = header.getHeader(0);
                MsgParser parser = new MsgParser();
                if (params[1].equalsIgnoreCase("P"))
                     parser.parse(" ," + value, ",", '=', "()", "\"\"");
                else
                    parser.parse(" ," + value, ",", ':', "()", "\"\"");
                var.addHeader(parser.getHeader(params[3]));
                return true;
            }
        } else {
            return false;
        }
    }

}
