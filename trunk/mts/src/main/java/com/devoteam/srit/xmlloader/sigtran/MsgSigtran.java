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

import java.util.Iterator;
import java.util.LinkedList;

import gp.utils.arrays.Array;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.coding.binary.q931.MessageQ931;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvField;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvMessage;
import com.devoteam.srit.xmlloader.sigtran.tlv.TlvParameter;
import com.devoteam.srit.xmlloader.sigtran.ap.APMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.BinaryNotesAPMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.MobicentMAPMessage;
import com.devoteam.srit.xmlloader.sigtran.ap.MobicentTCAPMessage;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoField;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoMessage;
import com.devoteam.srit.xmlloader.sigtran.fvo.FvoParameter;

public class MsgSigtran extends Msg {

    // AP layer (Application part) (spec ITU Q.XXXX)= coding ASN1 
    private APMessage _apMessage;
	
    // ISDN (Integrated Services Digital Network) layer (spec ITU Q.XXXX) = coding IE (Information element) 
    private MessageQ931 _ieMessage;

	// SS7 (Signaling System 7) layer (spec ITU Q.XXXX = coding FVO (Fixed Variable Optional)    
    private FvoMessage _fvoMessage;
    private int _fvoProtocol;
    
    // UA (User Part) layer (rfc IETF XXXX) = coding TLV (Tag Length Value) 
    private TlvMessage _tlvMessage;
    private int _tlvProtocol;

    private byte[] _encodedCache = null;

    /**
     * Creates a new instance of MsgSigtran
     */
    public MsgSigtran() throws Exception {
    	//_apMessage = new MobicentTCAPMessage();
    	_apMessage = new MobicentMAPMessage();
    	//_apMessage = new BinaryNotesAPMessage();
    }

    public MsgSigtran(Array msgArray, int protocolIdentifier) throws Exception 
    {
    	this();
        _tlvProtocol = protocolIdentifier;
        _encodedCache = msgArray.getBytes();
        _tlvMessage = new TlvMessage(this, msgArray, protocolIdentifier);
        // ie layer 
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
        // ie layer 
    	// get Data V Parameter 
    	// TODO Select by name instead of third one (name=Data)
    	FvoParameter paramFvo = _fvoMessage.getVparameters().get(2);
    	if (paramFvo != null)
    	{    		
    		Array ieArray = paramFvo.encode();
    		//_apMessage = new BinaryNotesAPMessage();
	    	_apMessage.decode(ieArray);
    	}	
    }

    /** Get a parameter from the message */
    @Override
    public Parameter getParameter(String path) throws Exception {
        Parameter var = super.getParameter(path);
        if (null != var) {
            return var;
        }

        var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);
                
        if (params.length > 0 && params[0].equalsIgnoreCase("isdn")) {
        	this._ieMessage.getParameter(var, params, path);
        }
        else if (params.length > 0 && params[0].equalsIgnoreCase("ss7")) {
            if (params.length > 1) {
                if (params[1].equalsIgnoreCase("content")) {
                    var.add(_fvoMessage);
                }
                else {
                    if (path.contains(":")) {
                        // var = _fvoMessage.getParameter(path.substring(path.indexOf(":") + 1));
                    }
                    else {
                        // var = _fvoMessage.getParameter(path.substring(path.indexOf(".") + 1));
                    }
                }
            }
        }
        else if(params.length > 0 && params[0].equalsIgnoreCase("ua")) {
            if (params.length != 1) {
                if (params[1].equalsIgnoreCase("ppid")) {
                    var.add(getTlvProtocol());
                }
                else if (path.contains(":")) {
                    var = _tlvMessage.getParameter(path.substring(path.indexOf(":") + 1));
                }
                else {
                        var = _tlvMessage.getParameter(path.substring(path.indexOf(".") + 1));
                }
            }         
        }
        else {
            Parameter.throwBadPathKeywordException(path);
        }

        return var;
    }

    /** Get the protocol of this message */
    @Override
    public String getProtocol() {
        return StackFactory.PROTOCOL_SIGTRAN;
    }

    @Override
    public String getType() throws Exception {
        if (_fvoMessage != null) {
            return "" + _fvoMessage.getMessageType();
        }    	
        if (_ieMessage != null) {
            return "" + _ieMessage.getType();
        }
        if (_tlvMessage != null) {
            return _tlvMessage.getName();
        }
        else {
            return null;
        }
    }

    @Override
    public String getResult() throws Exception {
        // TODO !!!!!!!!!!!!!!!!!!!!!
        return null;
    }

    @Override
    public int getLength() {
        return _tlvMessage.getMessageLength();
    }

    @Override
    public boolean isRequest() {
        // TODO !!!!!!!!!!!!!!!!!!!!
        return false;
    }

    public MessageQ931 getIeMessage() {
		return _ieMessage;
	}

	public void setIeMessage(MessageQ931 ieMessage) {
		this._ieMessage = ieMessage;
	}

    public FvoMessage getFvoMessage() {
        return _fvoMessage;
    }

    public void setFvoMessage(FvoMessage fvoMessage) {
        _fvoMessage = fvoMessage;
    }

    public TlvMessage getTlvMessage() {
        return _tlvMessage;
    }

    public void setTlvMessage(TlvMessage tlvMessage) {
        _tlvMessage = tlvMessage;
    }
    

    public int getFvoProtocol() {
        return _fvoProtocol;
    }

    public void setFvoProtocol(int fvoProtocol) {
        _fvoProtocol = fvoProtocol;
    }

    public int getTlvProtocol() {
        return _tlvProtocol;
    }

    public void setTlvProtocol(int tlvProtocol) {
        _tlvProtocol = tlvProtocol;
    }

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData() {
        try {
        	if (_apMessage != null)
        	{
        		Array encoded = _apMessage.encode();
	        	String val = Array.toHexString(encoded);
	        	LinkedList<FvoParameter> params = _fvoMessage.getVparameters();
	        	Iterator iterParam = params.iterator();
	        	FvoParameter param = null;
	        	while (iterParam.hasNext())
	        	{
	        		param = (FvoParameter) iterParam.next();
	        		if ("Data".equalsIgnoreCase(param.getName()))
	        		{
	        			break;
	        		}
	        	}
	        	LinkedList<FvoField> fields = param.getFields();
	        	Iterator iterField = fields.iterator();
	        	FvoField field = null;
	        	if (iterField.hasNext())
	        	{
	        		field = (FvoField) iterField.next();
	        	}
	        	field.setValue(val);
        	}
        	if (_ieMessage != null)
        	{
	        	Array encoded = _ieMessage.getValue();
	        	String val = Array.toHexString(encoded);
	        	TlvParameter param = _tlvMessage.getTlvParameter("Protocol_Data");
	        	TlvField field = param.getTlvField("Protocol_Data");
	        	field.setValue(val);
        	}
            if(null == _encodedCache){
                _encodedCache = _tlvMessage.encode().getBytes();
            }
            return _encodedCache;
        }
        catch (Exception ex) {
            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ex, "Error while encoding the SIGTRAN message: ");
        }
        return null;
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
    public String toShortString() throws Exception {
        String str = super.toShortString();

        if (_ieMessage != null) {
            // str += _ieMessage.toShortString("ISDN") + "\n";
            return str;
        }

        if (_fvoMessage != null) {
            str += _fvoMessage.toShortString() + "\n";
            return str;
        }

        if (_tlvMessage != null) {
            str += _tlvMessage.toShortString("UA") + "\n";
            return str;
        }

        return str;
    }
    
    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() {
        String ret = "";

        if (_ieMessage != null) {
            ret += _ieMessage.toString() + "\n";
        }
        
        if (_fvoMessage != null) {
            ret += _fvoMessage.toString() + "\n";
        }

        if (_tlvMessage != null) {
            ret += _tlvMessage.toString() + "\n";
        }

        return ret;
    }
}
