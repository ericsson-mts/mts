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

package com.devoteam.srit.xmlloader.snmp;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import java.util.Vector;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibValueSymbol;
import org.snmp4j.AbstractTarget;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

public class MsgSnmp extends Msg
{
    PDU pdu;
    AbstractTarget target;

    public MsgSnmp()
    {}
    
	public MsgSnmp(int version, String community, int requestType, int requestId, Integer errorStatus, Integer errorIndex, Integer nonRepeaters, Integer maxRepetitions) throws Exception {
		super();

        if(version == SnmpConstants.version3)//version 3 not supported actually
        {
            UserTarget userTarget = new UserTarget();
            userTarget.setSecurityName(new OctetString(community));

            // set encrypt level (auth? content ?)
            userTarget.setSecurityLevel(SecurityLevel.AUTH_PRIV);

            // set the user, must be known in the stack 
            userTarget.setSecurityName(new OctetString("MD5DES"));

            target = userTarget;
        }
        else
        {
            CommunityTarget communityTarget = new CommunityTarget();
            communityTarget.setCommunity(new OctetString(community));

            target = communityTarget;
        }
        
        target.setVersion(version);
        
        if(version == SnmpConstants.version1)
        {
            pdu = new PDUv1();
        }
        else if(version == SnmpConstants.version2c)
        {
            pdu = new PDU();
        }
        else if(version == SnmpConstants.version3)//version 3 not supported actualy
        {
            pdu = new ScopedPDU();
        }

        pdu.setType(requestType);
        pdu.setRequestID(new Integer32(requestId));
        
        if((errorIndex != null) || (errorStatus != null))
        {
            pdu.setErrorIndex(errorIndex);
            pdu.setErrorStatus(errorStatus);
        }
        if((nonRepeaters != null) || (maxRepetitions != null))
        {
            pdu.setNonRepeaters(nonRepeaters);
            pdu.setMaxRepetitions(maxRepetitions);
        }
	}

    public PDU getPdu() {
        return pdu;
    }

    public void setPdu(PDU pdu) {
        this.pdu = pdu;
    }

    public AbstractTarget getTarget() {
        return target;
    }

    public void setTarget(AbstractTarget target) {
        this.target = target;
    }

	/*
	 * Get parameters from the command/reply lines
	 */
    @Override
	public Parameter getParameter(String path) throws Exception 
	{	
		Parameter var = null;
        
        //specific case, because we have defined message:remoteHost/port under
        if(!path.equalsIgnoreCase("message:remoteHost") && !path.equalsIgnoreCase("message:remotePort")
           && !path.equalsIgnoreCase("message.remoteHost") && !path.equalsIgnoreCase("message.remotePort"))
            var = super.getParameter(path);

		if (null != var) 
		{
			return var;
		}

    	var = new Parameter();
        path = path.trim();
        String[] params = Utils.splitPath(path);

        if(params.length == 0)
        {
            Parameter.throwBadPathKeywordException(path);
        }
        
		if (params[0].equalsIgnoreCase("message"))
		{
            if(params.length > 1)
            {
                if (params[1].equalsIgnoreCase("remoteHost"))
                {
    				var.add(getRemoteHost());
                }
                else if (params[1].equalsIgnoreCase("remotePort"))
                {
    				var.add(getRemotePort());
                }
                else
                {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
            else
            {
                Parameter.throwBadPathKeywordException(path);
            }
		}
		else if (params[0].equalsIgnoreCase("header"))
		{
            if(params.length > 1)
            {
                if (params[1].equalsIgnoreCase("type"))
                {
    				var.add(PDU.getTypeString(getPdu().getType()));
                }
                else if (params[1].equalsIgnoreCase("requestId"))
                {
    				var.add(getPdu().getRequestID());
                }
                else if (params[1].equalsIgnoreCase("version"))
                {
                    if(target.getVersion() == SnmpConstants.version1)
                        var.add(1);
                    else if(target.getVersion() == SnmpConstants.version2c)
                        var.add(2);
                    else if(target.getVersion() == SnmpConstants.version3)
                        var.add(3);
                }
                else if (params[1].equalsIgnoreCase("community"))
                {
                    if(target instanceof CommunityTarget)
                        var.add(((CommunityTarget)target).getCommunity().toString());
                    else if(target instanceof UserTarget)
                        var.add(((UserTarget)target).getSecurityName().toString());
                }
                else if (params[1].equalsIgnoreCase("errorStatus"))
                {
    				var.add(getPdu().getErrorStatus());
                }
                else if (params[1].equalsIgnoreCase("errorIndex"))
                {
                    var.add(getPdu().getErrorIndex());
                }
                else if (params[1].equalsIgnoreCase("nonRepeaters"))
                {
                    var.add(getPdu().getNonRepeaters());
                }
                else if (params[1].equalsIgnoreCase("maxRepetitions"))
                {
                    var.add(getPdu().getMaxRepetitions());
                }
                else
                {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
            else
            {
                Parameter.throwBadPathKeywordException(path);
            }
		}
        else if (params[0].equalsIgnoreCase("variables"))
		{
            if(params.length > 1)
            {
                Vector<VariableBinding> variables = pdu.getVariableBindings();
                if (params[1].equalsIgnoreCase("name"))
                {
                    Mib[] mibTab = ((StackSnmp)StackFactory.getStack(StackFactory.PROTOCOL_SNMP)).getMibLoader().getAllMibs();
                    MibValueSymbol symbol = null;
                    for(int i = 0; i < variables.size(); i++)
                    {
                        for(int j = 0; j < mibTab.length; j++)
                        {
                        
                            symbol = mibTab[j].getSymbolByValue(variables.get(i).getOid().toString());
                            if(symbol != null)
                            {
                                var.add(symbol.getName());
                                break;
                            }
                        }
                    }
                }
                else if (params[1].equalsIgnoreCase("oid"))
                {
    				for(int i = 0; i < variables.size(); i++)
                    {
                        var.add(variables.get(i).getOid());
                    }
                }
                else if (params[1].equalsIgnoreCase("mib"))
                {
    				Mib[] mibTab = ((StackSnmp)StackFactory.getStack(StackFactory.PROTOCOL_SNMP)).getMibLoader().getAllMibs();
                    MibValueSymbol symbol = null;
                    for(int i = 0; i < variables.size(); i++)
                    {
                        for(int j = 0; j < mibTab.length; j++)
                        {
                            symbol = mibTab[j].getSymbolByValue(variables.get(i).getOid().toString());
                            if(symbol != null)
                            {
                                var.add(mibTab[j].getName());
                                break;
                            }
                        }
                    }
                }
                else if (params[1].equalsIgnoreCase("type"))
                {
                    for(int i = 0; i < variables.size(); i++)
                    {
                        var.add(variables.get(i).getVariable().getSyntaxString());
                    }
                }
                else if (params[1].equalsIgnoreCase("value"))
                {
    				for(int i = 0; i < variables.size(); i++)
                    {
                        var.add(variables.get(i).getVariable().toString());
                    }
                }
                else
                {
                    Parameter.throwBadPathKeywordException(path);
                }
            }
            else
            {
                Parameter.throwBadPathKeywordException(path);
            }
		}
		else 
		{
        	Parameter.throwBadPathKeywordException(path);
		}

		return var;
	}

	/** Get the protocol of this message */
	public String getProtocol() {
		return StackFactory.PROTOCOL_SNMP;
	}

	/** Return true if the message is a request else return false */
	public boolean isRequest() {
		return pdu.getType() != PDU.RESPONSE;
	}

	/** Get the command code of this message */
	public String getType() {
        return PDU.getTypeString(pdu.getType());
	}

	/*
	 * Get the response number
	 */
	public String getResult() {
        return pdu.getErrorStatusText();
	}

    @Override
    public boolean shallBeRetransmitted() throws Exception
    {
        return ((getPdu().getType() == PDU.TRAP) || (getPdu().getType() == PDU.V1TRAP)) ? false : true;
    }
	
	/** Return the transport of the message */
    @Override
	public String getTransport() {
		return StackFactory.PROTOCOL_UDP;
	}

    /** Get the data (as binary) of this message */
    @Override
    public byte[] getBytesData(){
        //TODO
        return new byte[3];
    }

    /** Returns a short description of the message. Used for logging as INFO level */
    /** This methods HAS TO be quick to execute for performance reason */
    @Override
	public String toShortString() throws Exception {
    	String ret = super.toShortString();
        ret += "type: " + PDU.getTypeString(pdu.getType()) + ", requestId: " + pdu.getRequestID();
        if(pdu.getType() != PDU.GETBULK)
            ret += ", errorStatus: " + pdu.getErrorStatusText() + ", errorIndex: " + pdu.getErrorIndex();
        else
            ret += ", nonRepeaters: " + pdu.getNonRepeaters() + ", maxRepetitions: " + pdu.getMaxRepetitions();
		return ret;
	}

    /** Get the XML representation of the message; for the genscript module. */
    @Override
    public String toXml() throws Exception {
    	String xml = "";
		try {
			xml += "type: " + PDU.getTypeString(pdu.getType()) + ", requestId: " + pdu.getRequestID();
            if(pdu instanceof PDUv1 && (pdu.getType() == PDU.V1TRAP))//specific for TRAP in SNMPV1
                xml += ", enterprise: " + ((PDUv1)pdu).getEnterprise() + ", agentAddress: " + ((PDUv1)pdu).getAgentAddress() + 
                       ", genericTrap: " + ((PDUv1)pdu).getGenericTrap() + ", specificTrap: " + ((PDUv1)pdu).getSpecificTrap() +
                       ", timestamp: " + ((PDUv1)pdu).getTimestamp();
            else if(pdu.getType() != PDU.GETBULK)
            xml += ", errorStatus: " + pdu.getErrorStatusText() + ", errorIndex: " + pdu.getErrorIndex();
        else
            xml += ", nonRepeaters: " + pdu.getNonRepeaters() + ", maxRepetitions: " + pdu.getMaxRepetitions();

            xml += "\r\n";
            if(pdu.getVariableBindings().size() > 0)
            {
                xml += "VariableBinding: " + pdu.getVariableBindings().toString();
            }
			
		} catch (Exception e) {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, e, "An error occured while logging the SMTP message : ", xml);
		}
		return xml;
    }
}
