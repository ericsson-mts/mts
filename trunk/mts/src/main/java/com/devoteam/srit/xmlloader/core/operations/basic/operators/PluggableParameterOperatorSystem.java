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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.newstats.StatCounterConfigManager;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.report.SectionReportGenerator;
import com.devoteam.srit.xmlloader.core.report.derived.DerivedCounter;
import com.devoteam.srit.xmlloader.core.utils.InputStreamConsumer;
import com.devoteam.srit.xmlloader.core.utils.Utils;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSystem extends AbstractPluggableParameterOperator
{

    final private String S_READPROPERTY = "system.readproperty";
    final private String S_TIMESTAMP = "system.timestamp";
    final private String S_IPADDRESS = "system.ipaddress";
    final private String S_MACADDRESS = "system.macaddress";
    final private String S_QUERYSQL = "system.querySQL";
    final private String S_COMMAND = "system.command";
    final private String S_STATCOUNTER = "system.statCounter";
    

    public PluggableParameterOperatorSystem()
    {
        this.addPluggableName(new PluggableName(S_READPROPERTY));
        this.addPluggableName(new PluggableName(S_TIMESTAMP));
        this.addPluggableName(new PluggableName(S_IPADDRESS));
        this.addPluggableName(new PluggableName(S_MACADDRESS));
        this.addPluggableName(new PluggableName(S_QUERYSQL));
        this.addPluggableName(new PluggableName(S_COMMAND));
        this.addPluggableName(new PluggableName(S_STATCOUNTER));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception
    {
        normalizeParameters(operands);
        resultant = ParameterPool.unbracket(resultant);
        
        Parameter param_1;
        Parameter param_2;

        if (name.equalsIgnoreCase(S_TIMESTAMP) || name.equalsIgnoreCase(S_IPADDRESS) || name.equalsIgnoreCase(S_MACADDRESS))
        {
            param_1 = operands.get("value");
        }
        else
        {
            param_1 = assertAndGetParameter(operands, "value");
        }

        Parameter result = new Parameter();

        int size = 0;
        if (null != param_1)
        {
            size = param_1.length();
        }
        if (name.equalsIgnoreCase(S_READPROPERTY))
        {
            for (int i = 0; i < size; i++)
            {
                result.add(System.getProperty(param_1.get(i).toString(), "UNDEFINED"));
            }
        }
        else if (name.equalsIgnoreCase(S_TIMESTAMP)) {
        	if (null != param_1 && param_1.length() != 1) {
                    throw new ParameterException("value attribute should have a size of 1 for operation system.ipaddress");
        	}

        	String var2 = "1970";
        	if(null != param_1) var2 = param_1.get(0).toString();

        	if (var2.equals("1970")) {
        		result.add(String.valueOf((System.currentTimeMillis())));
        		return result;
        	} 
        	else if (var2.equals("1900")) {
        		Calendar cal = Calendar.getInstance(Locale.US);
        		cal.set(Integer.parseInt(var2), 00, 01, 00, 00, 00); // current time in millisecond since 1st january 1900
        		Long timeMillis1900 = cal.getTimeInMillis(); // obtain the time in milliseconds
        		result.add(String.valueOf((System.currentTimeMillis()) - timeMillis1900));
        		return result;
        	}
        }
        else if (name.equalsIgnoreCase(S_MACADDRESS)) {
        	if (null != param_1 && param_1.length() != 1) {
        		throw new ParameterException("value attribute should have a size of 1 for operation system.ipaddress");
        	}
        	try {
        		// add loopback address
        		if ((null == param_1) || (param_1.get(0).toString().equals("lo"))) {
        			InetAddress ip = InetAddress.getLocalHost();
        			NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        			byte[] mac = network.getHardwareAddress();
        			String res = "";
        			for (int j = 0; j < mac.length; j++)
    					res += String.format("%02x%s", mac[j], (j < mac.length - 1) ? ":" : "");
    				result.add(res);
    			}
        		else
        		{
	        		InetAddress address = null;
	        		int i = 0;
	        		ArrayList<String> array = new ArrayList<String>();
	        		for (Enumeration e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
	        			NetworkInterface eth = (NetworkInterface) e.nextElement();
	        			if (null == param_1 || param_1.get(0).toString().equals(eth.getName())) {
	        				byte[] mac = eth.getHardwareAddress();
	        				String res = "";
	        				for (int j = 0; j < mac.length; j++)
	        					res += String.format("%02x%s", mac[j], (j < mac.length - 1) ? ":" : "");
	        				result.add(res);
	        			}
	        		}
        		}
        	}
        	catch (Exception e) {
        		throw new ParameterException("error in " + name, e);
        	}
        }
        else if (name.equalsIgnoreCase(S_IPADDRESS)) {
        	if (null != param_1 && param_1.length() != 1) {
        		throw new ParameterException("value attribute should have a size of 1 for operation system.ipaddress");
        	}
        	String version = "4";
        	if (operands.get("value2") != null)
        	{
        		Parameter v = assertAndGetParameter(operands, "value2");
        		version = v.get(0).toString();
        	}
        	try {
        		InetAddress address = null;
        		int i = 0;
        		ArrayList<String> array = new ArrayList<String>();
        		for (Enumeration e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
        			NetworkInterface eth = (NetworkInterface) e.nextElement();
        			if (null == param_1 || param_1.get(0).toString().equals(eth.getName())) {
        				for (Enumeration addr = eth.getInetAddresses(); addr.hasMoreElements();) {
        					address = (InetAddress) addr.nextElement();
        					if (version.contains("6") && address instanceof Inet6Address)
        					{
        						// remove loopback address
	                            if (!address.isLoopbackAddress())
	                            {
	        						Inet6Address a = (Inet6Address) address;
	        						result.add(new String("[" + a.getHostAddress().split("%")[0] + "]"));
	                            }
        					}
        					if (version.contains("4") && address instanceof Inet4Address)
                            {
                            	// remove loopback address
	                            if (!address.isLoopbackAddress())
	                            {
	                            	result.add(address.getHostAddress()); // obtain the ip value from the specified interface
	                            }
                            }
        				}
        			}
        		}
        		// add loopback address
        		if ((null == param_1) || (param_1.get(0).toString().equals("lo"))) {
        			if (version.contains("4"))
        				result.add("127.0.0.1");
        			if (version.contains("6"))
        				result.add("[::1]");
    			}
        	}
        	catch (Exception e) {
        		throw new ParameterException("error in " + name, e);
        	}
        }
        else if (name.equalsIgnoreCase(S_QUERYSQL)) {
        	String url = param_1.get(0).toString();
        	String query = assertAndGetParameter(operands, "value2").get(0).toString();

        	String[] split = decoupe(url,":");
        	if (split[0].length()==0) {
        		split[0]="localhost";
        	}
        	if (split[1].length()==0) {
        		split[1]="3306";
        	}
        	if (split[2].length()==0) {
        		split[2]="root";
        	}
        	if (split[4].length()==0) {
        		split[4]="test";
        	}
        	
        	String serveur = "jdbc:mysql://"+split[0]+":"+split[1];
        	String login = split[2];
        	String password = split[3];
        	String database = "use "+split[4]+";";
        	Connection con = null;
        	Statement stmt = null;
        	ResultSet rs = null;
	
        	try {
        		Class.forName("com.mysql.jdbc.Driver").newInstance();
        	} catch (Exception e) {
        		System.out.println("Error driver " + e);
        	}
        	try {
        		con = (Connection) DriverManager.getConnection(serveur,login, password);
        	} catch (Exception e) {
        		throw new RuntimeException("Error connection (url, login or password) "+e);
        	}
        	try {
        		stmt = (Statement) con.createStatement();
        	} catch (Exception e1) {
        		throw new RuntimeException("error while creating statement "+e1);
        	}
        	try {
        		stmt.executeQuery(database);
        	} catch (Exception e1) {
        		throw new RuntimeException(e1);
        	}
        	try {
        		stmt.execute(query);
        	}catch (Exception e) {
        		throw new RuntimeException("query error "+e);
        	}
        	try {
        		rs = stmt.getResultSet();
        		if (rs!=null) {
        			int len = rs.getMetaData().getColumnCount(); 			
        			for (int i=0; i<len; i++) {
        				String nom = resultant+"."+rs.getMetaData().getColumnLabel(i+1);
        				Parameter nouveauP = new Parameter(nom);      
        				result.add(nom);
        				if (rs.first()) {
        					do  {
        						nouveauP.add(rs.getString(i+1));
        					} while (rs.next());
        				}
        				runner.getParameterPool().set("["+nom+"]", nouveauP);
        			}
        		}
        	}
        	catch(Exception e) {
        		throw new RuntimeException("error developpement " +e );
        	}
        }else if (name.equalsIgnoreCase(S_COMMAND)) {
        	String command = param_1.get(0).toString();
        	
            try
            {
                Process p = Runtime.getRuntime().exec(command);
                
                InputStreamConsumer stdInputStreamConsumer= new InputStreamConsumer(p.getInputStream());
                InputStreamConsumer errInputStreamConsumer= new InputStreamConsumer(p.getErrorStream());

                stdInputStreamConsumer.acquire();
                String nomInput = resultant+".standardOut";
                Parameter  inputStream = new Parameter(nomInput);
                inputStream.add(stdInputStreamConsumer.getContents());
                result.add(nomInput);
				runner.getParameterPool().set("["+nomInput+"]", inputStream);
                
                errInputStreamConsumer.acquire();
                String nomErr = resultant+".errorOut";
                Parameter  errStream = new Parameter(nomErr);
                errStream.add(errInputStreamConsumer.getContents());
                result.add(nomErr);
				runner.getParameterPool().set("["+nomErr+"]", errStream);
                
				p.waitFor();
                String nomSortie = resultant+".returnCode";
                Parameter sortieCode = new Parameter(nomSortie);
                sortieCode.add(String.valueOf(p.exitValue()));
                result.add(nomSortie);
				runner.getParameterPool().set("["+nomSortie+"]", sortieCode);
            }
            catch (Exception e)
            {
                throw new ExecutionException("Exception occured\n Error executing system operation command", e);
            }
        }
        else if (name.equalsIgnoreCase(S_STATCOUNTER)) {
        	String path = param_1.get(0).toString().trim();
        	if (path.charAt(0) == '>')
        	{
        		path = path.substring(1, path.length());
        	}
        	String[] key = Utils.splitNoRegex(path, ">");
        	StatKey statKey = new StatKey(key);
        	        
        	String counterName = ((String)assertAndGetParameter(operands, "value2").get(0)).trim();
        	
        	List<CounterReportTemplate> templates = StatCounterConfigManager.getInstance().getTemplateList(statKey);
        	Iterator<CounterReportTemplate> iter = templates.iterator();
        	while (iter.hasNext())
        	{
        		CounterReportTemplate template = iter.next();
        		// should replace LinkedList by LinkedHashMap  
        		if (counterName.equalsIgnoreCase(template.name))
        		{
                	DerivedCounter counter = template.getDerivedCounter(StatPool.getInstance(), statKey);
                	if (counter != null)
                	{
                		counter.addParameterStats(runner, result, resultant);
                		break;
                	}
        		}
        	}	
        	
        }

        else {
        	throw new RuntimeException("unsupported operation " + name);
        }
	
        return result;
    }
    public static String[] decoupe(String ligneEntree,String separateur) {
		if (ligneEntree == null) {
			return null;
		}
		int index = 0;
		ArrayList lig = new ArrayList();
		String temporaire = ligneEntree;
		if (temporaire != null) {
			index = temporaire.indexOf(separateur);
			while (index >= 0) {
				lig.add(temporaire.substring(0, index));
				temporaire = temporaire.substring(index + separateur.length(), temporaire.length());
				index = temporaire.indexOf(separateur);
			}
			lig.add(temporaire);
		}
		String[] plip = new String[lig.size()] ;
		for(int j=0; j<lig.size(); j++) {
			plip[j] = (String) lig.get(j);
		}
		return plip;
	}
}

