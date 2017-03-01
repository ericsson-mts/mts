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

package com.devoteam.srit.xmlloader.sigtran;

import java.util.Collection;
import java.util.List;

import org.dom4j.Element;

import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;

import com.devoteam.srit.xmlloader.asn1.ASNMessage;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.binary.q931.MessageQ931;
import com.devoteam.srit.xmlloader.rtp.flow.StackRtpFlow;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvDictionary;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvField;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvMessage;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvParameter;
import com.devoteam.srit.xmlloader.sigtran.ap.BN_APMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.BN_TCAPMessage;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoDictionary;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoParameter;

public class MsgSigtran extends Msg 
{

    // AP layer (Application part) (spec ITU Q.XXXX)= coding ASN1 => Use BinaryNotes library 
    private BN_APMessage _apMessage;
		
    // TCAP layer (Application part) (spec ITU Q.XXXX)= coding ASN1 => Use Mobicent library 
    //private MobicentTCAPMessage _tcapMessage;
    private BN_TCAPMessage _tcapMessage;
    
    // ISDN (Integrated Services Digital Network) layer (spec ITU Q.XXXX) = coding IE (Information element) 
    private MessageQ931 _ieMessage;

	// SS7 (Signaling System 7) layer (spec ITU Q.XXXX = coding FVO (Fixed Variable Optional)    
    private FvoMessage _fvoMessage;
    private int _fvoProtocol;
    
    // UA (User Part) layer (rfc IETF XXXX) = coding TLV (Tag Length Value) 
    private TlvMessage _tlvMessage;
    private int _tlvProtocol;

    private byte[] _encodedCache = null;

    /** Creates a new instance of MsgSigtran */
    public MsgSigtran(Stack  stack) throws Exception 
    {
    	super(stack);
    }

    /** Creates a new instance of MsgSigtran */
    public MsgSigtran(Stack  stack, int protocolIdentifier) throws Exception 
    {
    	this(stack);
    	this._tlvProtocol = protocolIdentifier;
    }

    /** 
     * Get the protocol acronym of the message 
     */
    @Override
    public String getProtocol() 
    {
        if (_apMessage != null) 
        {
            return _apMessage.getProtocol();
        }  
        if (_tcapMessage != null) 
        {
        	return _tcapMessage.getProtocol();
        }    	
        if (_fvoMessage != null) 
        {
        	return _fvoMessage.getProtocol();
        }    	
        if (_ieMessage != null) 
        {
        	return _ieMessage.getProtocol();
        }
        if (_tlvMessage != null) 
        {
        	return _tlvMessage.getProtocol();
        }
        return StackFactory.PROTOCOL_SIGTRAN;
    }

    /** 
     * Return true if the message is a request else return false
     */
    @Override
    public boolean isRequest() throws Exception
    {
        if (_apMessage != null) 
        {
            return _apMessage.isRequest();
        }    	
        if (_tcapMessage != null) 
        {
            return _tcapMessage.isRequest();
        }
        if (_ieMessage != null) 
        {
        	return _ieMessage.isRequest();
        }
        if (_fvoMessage != null) 
        {
            return _fvoMessage.isRequest();
        }
        if (_tlvMessage != null) 
        {
            return _tlvMessage.isRequest();
        }
        return true;
    }

    /** 
     * Get the type of the message
     * Used for message filtering with "type" attribute and for statistic counters 
     */
	@Override
    public String getType() throws Exception 
    {    	
    	String type = null;
        // for response message
        if (type == null && !isRequest())
        {
			Trans trans = getTransaction(); 
	    	if (type == null && trans != null)
	    	{
		    	Msg request = trans.getBeginMsg();
		    	if (request != null)
		    	{
		    		type = request.getType();
		    	}
	    	}
        }
    	
        if (type == null && _apMessage != null) 
        {
            type = _apMessage.getType();
        }
        else if (type == null && _tcapMessage != null) 
        {
        	type = _tcapMessage.getType();
        }
        else if (type == null && _ieMessage != null) 
        {
        	type = _ieMessage.getType();
        }
        else if (type == null && _fvoMessage != null) 
        {
        	type = _fvoMessage.getType();
        }    	
        else if (type == null && _tlvMessage != null) 
        {
        	type = _tlvMessage.getType();
        }
        
        return type;
	}

    /** 
     * Get the result of the message (null if request)
     * Used for message filtering with "result" attribute and for statistic counters 
     */
	@Override
    public String getResult() throws Exception 
    {
    	if (_apMessage != null) 
        {
            return _apMessage.getResult();
        } 
    	if (_tcapMessage != null) 
        {
            return _tcapMessage.getResult();
        }
    	if (_ieMessage != null) 
        {
        	return _ieMessage.getResult();
        }
    	if (_fvoMessage != null) 
        {
            return _fvoMessage.getResult();
        }
    	if (_tlvMessage != null) 
        {
            return _tlvMessage.getResult();
        }
        return null;
    }

    /** Get the transaction Identifier of this message */
    @Override
    public TransactionId getTransactionId() throws Exception
    {
    	if (!this.isTransactionIdSet)
        {
    		String transID = "";
	    	if (_apMessage != null) 
	        {
	    		transID += _apMessage.getTransactionId();
	        }
	    	if (_tcapMessage != null) 
	        {
	    		transID += _tcapMessage.getTransactionId();
	        }
	    	if (_ieMessage != null) 
	        {
	    		transID += _ieMessage.getTransactionId();
	        }
	    	if (_fvoMessage != null) 
	        {
	    		transID += _fvoMessage.getTransactionId();
	        }
	    	if (_tlvMessage != null) 
	        {
	    		transID += _tlvMessage.getTransactionId();
	        }
	    	if (channel != null)
	    	{
	    		// TODO bug when we don't open channel with <openChannel>
	    		// A investiguer dans la stack generique voir si la creation du channel
	    		// et l'envoi du message est fait avant ou après le traitement generique
	    		//transID = this.channel.getName() + "|" + transID;
	    	}
	    	this.transactionId = new TransactionId(transID);
	        this.isTransactionIdSet = true;	
        }
    	return this.transactionId;
    }

    public BN_TCAPMessage getTCAPMessage() {
		return _tcapMessage;
	}

	public void setTCAPMessage(BN_TCAPMessage tcapMessage) {
		this._tcapMessage = (BN_TCAPMessage) tcapMessage;
	}

	public BN_APMessage getAPMessage() 
    {
		return _apMessage;
	}

	public void setAPMessage(BN_APMessage apMessage) 
	{
		this._apMessage = (BN_APMessage) apMessage;
	}

    public MessageQ931 getIeMessage() 
    {
		return _ieMessage;
	}

	public void setIeMessage(MessageQ931 ieMessage) 
	{
		this._ieMessage = ieMessage;
	}

    public FvoMessage getFvoMessage() 
    {
        return _fvoMessage;
    }

    public void setFvoMessage(FvoMessage fvoMessage) 
    {
        _fvoMessage = fvoMessage;
    }

    public TlvMessage getTlvMessage() 
    {
        return _tlvMessage;
    }

    public void setTlvMessage(TlvMessage tlvMessage) 
    {
        _tlvMessage = tlvMessage;
    }
    

    public int getFvoProtocol() 
    {
        return _fvoProtocol;
    }

    public void setFvoProtocol(int fvoProtocol) 
    {
        _fvoProtocol = fvoProtocol;
    }

    public int getTlvProtocol() 
    {
        return _tlvProtocol;
    }

    public void setTlvProtocol(int tlvProtocol) 
    {
        _tlvProtocol = tlvProtocol;
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
        try 
        {
        	if (_apMessage != null)
        	{
        		// encode AP layers with BinaryNotes library
        		Array arrayAP = _apMessage.encode("BER");
        		
        		// set the data in TCAP layer
		    	_tcapMessage.setTCAPComponents(arrayAP);        		
        	}
        	
        	if (_tcapMessage != null)
        	{
	        	// encode TCAP layer with BN library
	    		Array arrayTCAP = _tcapMessage.encode("BER");
	    		
	    		if (arrayTCAP != null)
	    		{
		        	// get SS7 "Data" VParameter 
		        	FvoParameter paramFvo = _fvoMessage.getVparameter("Data");
		        	if (paramFvo ==  null)
		        	{
		        		// get SS7 "Long_Data" VParameter => BUG does not work ! why ? 
		        		paramFvo = _fvoMessage.getVparameter("Long_Data");
		        	}
		        	paramFvo.parseArray(arrayTCAP);
	    		}
        	}
        	
        	if (_ieMessage != null)
        	{
	        	Array encoded = _ieMessage.getValue();
	        	String val = Array.toHexString(encoded);
	        	TlvParameter paramTlv = _tlvMessage.getTlvParameter("Protocol_Data");
	        	TlvField field = paramTlv.getTlvField("Protocol_Data");
	        	field.setValue(val);
        	}
            if(null == _encodedCache)
            {
                _encodedCache = _tlvMessage.encode().getBytes();
            }
            return _encodedCache;
        }
        catch (Exception ex) 
        {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ex, "Error while encoding the SIGTRAN message: ");
        }
        return null;
    }
    
    /** 
     * decode the message from binary data 
     */
    @Override
    public void decode(byte[] data) throws Exception
    {
        _encodedCache = data;
        Array msgArray = new DefaultArray(data);
        _tlvMessage = new TlvMessage(this, msgArray, this._tlvProtocol);
        
        // Q931 layer 
    	TlvParameter paramTlv = _tlvMessage.getTlvParameter("Protocol_Data");
    	if (paramTlv != null)
    	{
    		TlvField field = paramTlv.getTlvField("Protocol_Data");
    		if (field != null)
    		{
        		String ieStr = field.getValue();
	    		Array ieArray = Array.fromHexString(ieStr);
	    		if (_tlvProtocol == 1)
	    		{
	    			_ieMessage = new MessageQ931(ieArray, "../conf/sigtran/q931.xml");
	    		} 
	    		else if (_tlvProtocol == 6)
	    		{
	    			_ieMessage = new MessageQ931(ieArray, "../conf/sigtran/v5x.xml");
	    		}
    		}
    	}
    	
        // TCAP/AP layers
    	if (_fvoMessage != null)
    	{
        	// get "Data" VParameter from 
	    	FvoParameter paramFvo = _fvoMessage.getVparameter("Data");
	    	if (paramFvo != null)
	    	{    		
	    		// decode TCAP layer with Mobicent library
	    		Array ieArray = paramFvo.encode();
	    		try
	    		{
	    			_tcapMessage = new BN_TCAPMessage("tcap/dictionary_TCAP.xml");
	    			_tcapMessage.decode(ieArray, "BER");
	    		}
	    		catch (Exception e)
	    		{
	    			// nothing to do : man not an AP layer (ASN1)
	    			_tcapMessage = null;
	    			//e.printStackTrace();
	    		}
		  
	    		if (_tcapMessage != null)
	    		{
					String ACN = null;
					Parameter param = this._tcapMessage.getParameter("tcap.application_context_name");
					if (param.length() > 0)
			        {
			            ACN = param.get(0).toString();
			        }
		            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL,"ACN=", ACN);
					_apMessage = new BN_APMessage();
					Array arrayAP = ((BN_TCAPMessage) _tcapMessage).getTCAPBinary();					
		    		try
		    		{
		    			if (ACN != null && ACN.startsWith("CAP-"))
		    			{
		    				_apMessage = new BN_APMessage("cap/dictionary_CAP.xml");
		    			}
		    			else 
		    			{
		    				_apMessage = new BN_APMessage("map/dictionary_MAP.xml");
		    			}
						_apMessage.decode(arrayAP, "BER");
		    		}
		    		catch (Exception e)
		    		{
		    			// nothing to do : man not an AP layer (ASN1)
		    			_apMessage = null;
		    			//e.printStackTrace();
		    		}
	    		}
	    	}
    	}
    }

    //---------------------------------------------------------------------
    // methods for the XML display / parsing of the message
    //---------------------------------------------------------------------

    /** 
     * Convert the message to XML document 
     */
    @Override
    public String toXml() 
    {
        String ret = "\n";
        if (_apMessage != null) 
        {
            ret += _apMessage.toXML() + "\n";
        }
        if (_tcapMessage != null) 
        {
            ret += _tcapMessage.toXML() + "\n";
        }
        
        if (_ieMessage != null) 
        {
            ret += _ieMessage.toString() + "\n";
        }
        
        if (_fvoMessage != null) 
        {
            ret += _fvoMessage.toString() + "\n";
        }

        if (_tlvMessage != null) 
        {
            ret += _tlvMessage.toString() + "\n";
        }

        return ret;
    }

    /** 
     * Parse the message from XML element 
     */
    @Override
    public void parseFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
    	super.parseFromXml(request,root,runner);

    	List<Element> listAps = root.elements("ASN");
        Object[] tabAps = listAps.toArray();
        
        ASNMessage tcapMessage = null;
        if (tabAps.length >= 1)
        {
        	Element elementTCAP = (Element) tabAps[tabAps.length - 1];
        	tcapMessage = new BN_TCAPMessage("tcap/dictionary_TCAP.xml");
        	tcapMessage.parseFromXML(elementTCAP);
            // TCAP layer (optional)
        	this.setTCAPMessage((BN_TCAPMessage) tcapMessage);
        }

        if (tabAps.length >= 2)
        {
        	Element elementAP = (Element) tabAps[0];
        	String dictionary = elementAP.attributeValue("dictionary"); 
        	ASNMessage apMessage = new BN_APMessage(dictionary);
        	apMessage.parseFromXML(elementAP);
            // AP layer (optional)
        	this.setAPMessage((BN_APMessage) apMessage);
        }

        // ISDN layer (optional)
        Element ie = root.element("ISDN");
        if (ie != null) {
        	MessageQ931 ieMessage = new MessageQ931(ie);
            this.setIeMessage(ieMessage);
        }
        
        // SS7 layer (optional)
        Element fvo = root.element("SS7");
        if (fvo != null) {
        	FvoDictionary fvoDictionnary = ((StackSigtran) this.stack).getFvoDictionnary(fvo.attributeValue("file"));
        	FvoMessage fvoMessage = new FvoMessage(this,fvoDictionnary);
            this.setFvoMessage(fvoMessage);
            fvoMessage.parseElement(fvo);
        }

        // UA layer (mandatory)
        Element tlv = root.element("UA");
        if (tlv != null) {
        	
            TlvDictionary tlvDictionnary = ((StackSigtran) this.stack).getTlvDictionnary(tlv.attributeValue("file"));
            TlvMessage tlvMessage = new TlvMessage(this, tlvDictionnary);
            tlvMessage.parseMsgFromXml(tlv);
            this.setTlvMessage(tlvMessage);
            this.setTlvProtocol(tlvDictionnary.getPpid());
        }
        else
        {
            // TODO throw some exception
        }
    }


    //------------------------------------------------------
    // method for the "setFromMessage" <parameter> operation
    //------------------------------------------------------

    /** 
     * Get a parameter from the message
     */
    @Override
    public Parameter getParameter(String path) throws Exception 
    {
        Parameter var = super.getParameter(path);
        if (null != var) 
        {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if (params.length > 0 && params[0].equalsIgnoreCase("asn")) 
        {
        	return this._apMessage.getParameter(path);
        }
        else if (params.length > 0 && params[0].equalsIgnoreCase("ap")) 
        {
        	return this._apMessage.getParameter(path);
        }
        else if (params.length > 0 && params[0].equalsIgnoreCase("tcap")) 
        {
        	return this._tcapMessage.getParameter(path);
        }
        else if (params.length > 0 && params[0].equalsIgnoreCase("isdn")) 
        {
        	this._ieMessage.getParameter(var, params, path);
        }
        else if (params.length > 0 && params[0].equalsIgnoreCase("ss7")) 
        {
            if (params.length > 1) {
                if (params[1].equalsIgnoreCase("content")) 
                {
                    var.add(_fvoMessage);
                }
                else 
                {
                    if (path.contains(":")) 
                    {
                        // var = _fvoMessage.getParameter(path.substring(path.indexOf(":") + 1));
                    }
                    else 
                    {
                        // var = _fvoMessage.getParameter(path.substring(path.indexOf(".") + 1));
                    }
                }
            }
        }
        else if(params.length > 0 && params[0].equalsIgnoreCase("ua")) 
        {
            if (params.length != 1) 
            {
                if (params[1].equalsIgnoreCase("ppid")) 
                {
                    var.add(getTlvProtocol());
                }
                else if (path.contains(":")) 
                {
                    var = _tlvMessage.getParameter(path.substring(path.indexOf(":") + 1));
                }
                else 
                {
                	var = _tlvMessage.getParameter(path.substring(path.indexOf(".") + 1));
                }
            }         
        }
        else 
        {
            Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }

}
