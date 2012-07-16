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

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.*;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLElementTextMsgParser;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.File;
import java.io.PrintStream;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibMacroSymbol;
import net.percederberg.mibble.MibTypeSymbol;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.log.Log4jLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Opaque;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UnsignedInteger32;
import org.snmp4j.smi.VariableBinding;
        
public class StackSnmp extends Stack
{
    private MibLoader mibLoader = null;
    
    public StackSnmp() throws Exception
    {
        super();
        loadAllMibs();

        //LogFactory.setLogFactory(new Log4jLogFactory());
        //PropertyConfigurator.configure("log4j.properties");
        
        // initiate a default listenpoint if port is not empty or null
        int port = getConfig().getInteger("listenpoint.LOCAL_PORT", 0);
        if (port > 0)
        {
            Listenpoint listenpoint = new ListenpointSnmp(this);
            createListenpoint(listenpoint, StackFactory.PROTOCOL_SNMP);
        }
    }

    private void loadAllMibs() throws Exception
    {
        mibLoader = new MibLoader();
        mibLoader.addAllDirs(new File("../conf/snmp/mibs"));//add all subDirectories to search for mib

        //travel through mibs repository to add all mibs
        loadDirectory(SingletonFSInterface.instance().list(new URI("../conf/snmp/mibs")), "../conf/snmp/mibs");
    }

    private void loadDirectory(String[] files, String path) throws Exception
    {
        StringBuilder currentPath = new StringBuilder();
        for(int i = 0; i < files.length; i++)
        {
            if(!path.endsWith("/"))
            {	
                currentPath.append(path).append("/");
            }
            currentPath.append(files[i]);
            
            if(SingletonFSInterface.instance().isFile(new URI(currentPath.toString())))//if it's a file
            {
                try
                {
                	if(!files[i].startsWith("."))
                	{
                		mibLoader.load(files[i]);
                	}
                }
                catch(Exception e)
                {
                	GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "ERROR : loading the MIBS files");
                	if (e instanceof MibLoaderException)
                	{
                		((MibLoaderException)e).getLog().printTo(new PrintStream("../logs/snmpStack.log"));
                	}
                }
            }
            else//if it's a directory, load it and all it's content
            {
            	if(!files[i].startsWith("."))
            	{
            		loadDirectory(SingletonFSInterface.instance().list(new URI(currentPath.toString())), currentPath.toString());
            	}
            }
            //reset currentPath
            currentPath.delete(0, currentPath.length());
        }
    }

    public MibLoader getMibLoader() {
        return mibLoader;
    }

    /** Creates a Listenpoint specific to each Stack */
    @Override
    public Listenpoint parseListenpointFromXml(Element root) throws Exception 
    { 
        Listenpoint listenpoint = new ListenpointSnmp(this, root);
        return listenpoint;        
    }

    @Override
    public Msg parseMsgFromXml(Boolean request, Element root, Runner runner) throws Exception
    {
        // header
        Element header = root.element("header");
        String version = header.attributeValue("version");
        String community = header.attributeValue("community");

        if(version == null)//get version given in config if not present in sendMessage
            version = getConfig().getString("protocol.version", "1");

        if(!Utils.isInteger(version))
            throw new Exception("attribute version must be integer");

        Integer versionInt = Integer.parseInt(version);
        if(versionInt == 1)
            versionInt = SnmpConstants.version1;
        else if(versionInt == 2)
            versionInt = SnmpConstants.version2c;
        else if(versionInt == 3)
            versionInt = SnmpConstants.version3;
        else
            throw new Exception("possible version for SNMP are 1, 2 or 3 exclusively");
        
        Element pdu = root.element("pdu");
        String name = pdu.attributeValue("name");
        String type = pdu.attributeValue("type");
        String requestId = pdu.attributeValue("requestId");
        String errorStatus = pdu.attributeValue("errorStatus");
        String errorIndex = pdu.attributeValue("errorIndex");
        String nonRepeaters = pdu.attributeValue("nonRepeaters");
        String maxRepetitions = pdu.attributeValue("maxRepetitions");

        if((type != null) && (name != null))
            throw new Exception("type and name of the message " + name + " must not be set both");

        if((type == null) && (name == null))
            throw new Exception("One of the parameter type or name of the message header must be set");

        if((type != null) && !Utils.isInteger(type))
            throw new Exception("attribute type must be integer");

        Integer typeInt = null;
        if(type == null)
        {
            if(name.equalsIgnoreCase("trap") && (versionInt == SnmpConstants.version1))
                typeInt = PDU.V1TRAP;
            else
            {
                typeInt = PDU.getTypeFromString(name.toUpperCase());
                if(typeInt < -100)
                    throw new Exception("the type " + name + " specified is unknown in the SNMP protocol");
            }
        }
        else
            typeInt = Integer.parseInt(type);

        if(((errorStatus != null) || (errorIndex != null)) && ((nonRepeaters != null) || (maxRepetitions != null)))
            throw new Exception("The attributes errorStatus or errorIndex must not be set with nonRepeaters or maxRepetitions");

        if((requestId == null) || !Utils.isInteger(requestId))
            throw new Exception("attribute requestId must be integer");

        if(((errorStatus != null) && !Utils.isInteger(errorStatus)) || ((errorIndex != null) && !Utils.isInteger(errorIndex)))
            throw new Exception("attribute errorStatus and errorIndex must be integer");

        if(((nonRepeaters != null) && !Utils.isInteger(nonRepeaters)) || ((maxRepetitions != null) && !Utils.isInteger(maxRepetitions)))
            throw new Exception("attribute nonRepeaters and maxRepetitions must be integer");
        
        Integer requestIdInt = (requestId != null) ? Integer.parseInt(requestId) : null;
        Integer errorStatusInt = (errorStatus != null) ? Integer.parseInt(errorStatus) : null;
        Integer errorIndexInt = (errorIndex != null) ? Integer.parseInt(errorIndex) : null;
        Integer nonRepeatersInt = (nonRepeaters != null) ? Integer.parseInt(nonRepeaters) : null;
        Integer maxRepetitionsInt = (maxRepetitions != null) ? Integer.parseInt(maxRepetitions) : null;

        MsgSnmp msgSmtp = new MsgSnmp(versionInt, community, typeInt, requestIdInt, errorStatusInt, errorIndexInt, nonRepeatersInt, maxRepetitionsInt);

        //specific parameter for snmpv1 Trap  to check and add to pdu
        if((msgSmtp.getPdu() instanceof PDUv1) && (typeInt == PDU.V1TRAP))
        {
            String enterprise = pdu.attributeValue("enterprise");
            String agentAddress = pdu.attributeValue("agentAddress");
            String genericTrap = pdu.attributeValue("genericTrap");
            String specificTrap = pdu.attributeValue("specificTrap");
            String timestamp = pdu.attributeValue("timestamp");

            if(genericTrap.equalsIgnoreCase("enterpriseSpecific") && !Utils.isInteger(specificTrap))
                throw new Exception("specificTrap attribute must be an integer when enterpriseSpecific if given for genereicTrap in SNMPV1 TRAP message");
            if(!Utils.isInteger(timestamp))
                throw new Exception("timestamp must be an integer");

            int genericTrapInt = 0;
            if(genericTrap.equalsIgnoreCase("coldStart"))
                genericTrapInt = PDUv1.COLDSTART;
            else if(genericTrap.equalsIgnoreCase("warmStart"))
                genericTrapInt = PDUv1.WARMSTART;
            else if(genericTrap.equalsIgnoreCase("linkDown"))
                genericTrapInt = PDUv1.LINKDOWN;
            else if(genericTrap.equalsIgnoreCase("linkUp"))
                genericTrapInt = PDUv1.LINKUP;
            else if(genericTrap.equalsIgnoreCase("authenticationFailure"))
                genericTrapInt = PDUv1.AUTHENTICATIONFAILURE;
            else if(genericTrap.equalsIgnoreCase("egpNeighborLoss"))
                genericTrapInt = 5;//specified in rfc 1157, but not present in snmp4j stack
            else if(genericTrap.equalsIgnoreCase("enterpriseSpecific"))
                genericTrapInt = PDUv1.ENTERPRISE_SPECIFIC;
            else
                throw new Exception("genericTrap attribute is unknown");

            ((PDUv1)msgSmtp.getPdu()).setEnterprise(new OID(enterprise));
            ((PDUv1)msgSmtp.getPdu()).setAgentAddress(new IpAddress(agentAddress));
            ((PDUv1)msgSmtp.getPdu()).setGenericTrap(genericTrapInt);
            ((PDUv1)msgSmtp.getPdu()).setSpecificTrap(Integer.parseInt(specificTrap));
            ((PDUv1)msgSmtp.getPdu()).setTimestamp(Integer.parseInt(timestamp));
        }

        List<Element> variables = pdu.elements("variableBinding");
        for(Element var:variables)
        {
            parseVariableBinding(var, msgSmtp.getPdu());
        }
        return msgSmtp;
    }
 
    private void parseVariableBinding(Element variable, PDU pdu) throws Exception
    {
        String name = variable.attributeValue("name");
        String value = variable.attributeValue("value");
        String type = variable.attributeValue("type");
        String mibName = null;

        if(name == null)
        {
            throw new Exception("ERROR : The variablebinding name is required to send the variable.");
        }

        VariableBinding varBind = null;
        boolean found = false;
        if(name.startsWith("1."))//if name begin with digit 1 follow by a dot, it is an oid, else we consider it's a variable mib
        {
            varBind = new VariableBinding(new OID(name));
            found = true;
        }
        else
        {
            if(name.contains("."))
            {
                mibName = name.substring(0, name.indexOf('.'));
                name = name.substring(name.indexOf('.') + 1);
            }

            Mib[] mibs = mibLoader.getAllMibs();
            for(int i = 0; (i < mibs.length) && !found; i++)
            {
                Collection symbols = mibs[i].getAllSymbols();
                for(Object symb:symbols)
                {
                    if(symb instanceof MibValueSymbol)
                    {
                        if(((MibValueSymbol)symb).getName().equalsIgnoreCase(name))
                        {
                            if((mibName == null) || ((MibValueSymbol)symb).getMib().getName().equalsIgnoreCase(mibName))
                            {
                                varBind = new VariableBinding(new OID(((MibValueSymbol)symb).getValue().toString()));

                                if(type == null)
                                {
                                    type = findType((MibValueSymbol)symb);
                                }
                                found = true;
                                break;
                            }
                        }
                    }
                    else if(symb instanceof MibTypeSymbol)
                    {
//                        System.out.println("mibTypeSymbol " + ((MibTypeSymbol)symb).getName() + " for mib name " + mibs[i].getName());
                    }
                    else if(symb instanceof MibMacroSymbol)
                    {
//                        System.out.println("mibMacroSymbol " + ((MibMacroSymbol)symb).getName() + " for mib name " + mibs[i].getName());
                    }
                    else
                    {
                    	throw new Exception("ERROR : Unknown class for variablebinding \"" + symb.getClass() + "\"");
                    }
                }
            }
        }
        if (!found)
        {
        	throw new Exception("ERROR : Unknown varbinding name \"" + name + "\" not found in the MIB files.");
        }       
        
        if(type == null)///if type not set, search in mib
        {
            Mib[] mibs = mibLoader.getAllMibs();
            for(int i = 0; (i < mibs.length); i++)
            {
                MibValueSymbol symbol = mibs[i].getSymbolByValue(varBind.getOid().toString());

                if(symbol != null)
                {
                    //set type in function of information in variable
                    type = findType(symbol);
                    break;
                }
            }
        }

        if((value != null) && (value.length() > 0))
        {
            if (type == null)
            {
                throw new Exception("ERROR : The variablebinding type is required : it is not defined nor in XML script nor in MIB files.");
            }
            if(type.equalsIgnoreCase("counter64"))
            {
                varBind.setVariable(new Counter64(Long.parseLong(value)));
            }
            else if(type.equalsIgnoreCase("integer32"))
            {
                varBind.setVariable(new Integer32(Integer.parseInt(value)));
            }
            else if(type.equalsIgnoreCase("octetString") || type.equalsIgnoreCase("octet string"))
            {
                varBind.setVariable(new OctetString(value));
            }
            else if(type.equalsIgnoreCase("opaque"))
            {
                varBind.setVariable(new Opaque(value.getBytes()));
            }
            else if(type.equalsIgnoreCase("oid"))
            {
                varBind.setVariable(new OID(value));
            }
            else if(type.equalsIgnoreCase("genericAddress"))
            {
                GenericAddress add = new GenericAddress();
                add.setValue(value);
                varBind.setVariable(add);
            }
            else if(type.equalsIgnoreCase("ipAddress"))
            {
                varBind.setVariable(new IpAddress(value));
            }
            else if(type.equalsIgnoreCase("unsignedInteger32"))
            {
                varBind.setVariable(new UnsignedInteger32(Integer.parseInt(value)));
            }
            else if(type.equalsIgnoreCase("counter32"))
            {
                varBind.setVariable(new Counter32(Long.parseLong(value)));
            }
            else if(type.equalsIgnoreCase("gauge32"))
            {
                varBind.setVariable(new Gauge32(Long.parseLong(value)));
            }
            else if(type.equalsIgnoreCase("timeTicks"))
            {
                varBind.setVariable(new TimeTicks(Long.parseLong(value)));
            }
            else
            {
                throw new Exception("Error : Unknown variablebinding type \"" + type + "\" : not understood by the application");
        	}
        }

        pdu.add(varBind);
    }

    private String findType(MibValueSymbol symb)
    {
        String type = null;
        if(((MibValueSymbol)symb).getType() instanceof SnmpObjectType)
        {
            SnmpObjectType obj = (SnmpObjectType) ((MibValueSymbol)symb).getType();
            if(obj.getSyntax().getName().equals("OCTET STRING") | obj.getSyntax().getName().equals("displayString"))
                type = "octetString";
            else if(obj.getSyntax().getName().equals("INTEGER") | obj.getSyntax().getName().equals("Integer32"))
                type = "integer32";
            else if(obj.getSyntax().getName().equals("Gauge") | obj.getSyntax().getName().equals("Gauge32"))
                type = "gauge32";
            else if(obj.getSyntax().getName().equals("Counter") | obj.getSyntax().getName().equals("Counter32"))
                type = "counter32";
            else if(obj.getSyntax().getName().equals("OBJECT IDENTIFIER"))
                type = "oid";
            else if(obj.getSyntax().getName().equals("Counter64"))
                type = "counter64";
            else if(obj.getSyntax().getName().equals("Opaque"))
                type = "opaque";
            else if(obj.getSyntax().getName().equals("IpAddress"))
                type = "ipAddress";
            else if(obj.getSyntax().getName().equals("TimeTicks"))
                type = "timeTicks";
            else if(obj.getSyntax().getName().equals("SEQUENCE OF"))
                type = "sequenceOf";
            else if(obj.getSyntax().getName().equals("Unsigned32"))
                type = "unsignedInteger32";
            else
                System.out.println("snmpObjectType " + obj.getSyntax().getName());
        }
//        else
//            System.out.println("class not found in type research " + ((MibValueSymbol)symb).getType().getClass() + " for value " + symb);
        return type;
    }
    
    public Config getConfig() throws Exception
    {
        return Config.getConfigByName("snmp.properties");
    }

    public XMLElementReplacer getElementReplacer()
    {
        return XMLElementTextMsgParser.instance();
    }
}
