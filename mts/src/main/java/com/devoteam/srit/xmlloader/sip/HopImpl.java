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

package com.devoteam.srit.xmlloader.sip;

import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

/*
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 * Bug fix for correct handling of IPV6 Address added by
 * Daniel J. Martinez Manzano <dani@dif.um.es>
 */
/** 
 * Routing algorithms return a list of hops to which the request is
 * routed.
 *
 * @version 1.2 $Revision: 1.8 $ $Date: 2007/04/24 18:16:31 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 *
 
 *
 */
public final class HopImpl extends Object implements javax.sip.address.Hop {
	protected String host;
	protected int port;
	protected String transport;
	
	/**
	 * Debugging println.
	 */
	public String toString() {
		return host + ":" + port + "/" + transport;
	}

	/**
	 * Create new hop given host, port and transport.
	 * @param hostName hostname
	 * @param portNumber port
	 * @param trans transport
	 */
	public HopImpl(String hostName, int portNumber, String trans) {
		host = hostName;
		port = portNumber;
		transport = trans;
		
		// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
		// for correct management of IPv6 addresses.
		if(host != null && host.indexOf(":") >= 0)
			if(host.indexOf("[") < 0)
				host = "[" + host + "]";
		
		if (port <= 0)
		{
			port = transport.equalsIgnoreCase("TLS")?5061:5060;
		}
	}
	
	
	/**
	 * Creates new Hop
	 * @param hop is a hop string in the form of host:port/Transport
	 * @throws IllegalArgument exception if string is not properly formatted or null.
	 */
	HopImpl(String hop) throws Exception {
		
		if (hop == null)
			throw new IllegalArgumentException("Null arg!");
				
		int brack = hop.indexOf(']');
		int colon = hop.indexOf(':',brack);
		int slash = hop.indexOf('/',colon);
		
		if (colon>0) {
			this.host = hop.substring(0,colon);
			String portstr;
			if (slash>0) {				
				portstr = hop.substring(colon+1,slash);
				this.transport = hop.substring(slash+1);
			} else {
				portstr = hop.substring(colon+1);
				this.transport = "UDP";
			}
			try {
				port = Integer.parseInt(portstr);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Bad port spec");
			}
		} else {
			if (slash>0) {
				this.host = hop.substring(0,slash);
				this.transport = hop.substring(slash+1);
				this.port = transport.equalsIgnoreCase("TLS") ? 5061 : 5060;
			} else {
				this.host = hop;
				this.transport = "UDP";
				this.port = 5060;
			}
		}
		
		// Validate it
		if (host == null || host.length() == 0)
			throw new IllegalArgumentException("no host!");

		// normalize
		this.host = this.host.trim();
		this.transport = this.transport.trim();
				
		if ((brack>0) && host.charAt(0)!='[') {
			throw new IllegalArgumentException("Bad IPv6 reference spec");
		}
				
		if (transport.compareToIgnoreCase("UDP") != 0
			&& transport.compareToIgnoreCase("TLS") != 0 // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
			&& transport.compareToIgnoreCase("TCP") != 0) {
			throw new ExecutionException("Bad transport string " + transport);
		}
	}

	/**
	 * Retruns the host string.
	 * @return host String
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns the port.
	 * @return port integer.
	 */
	public int getPort() {
		return port;
	}

	/** returns the transport string.
	 */
	public String getTransport() {
		return transport;
	}
	
}
