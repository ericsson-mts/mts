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

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.*;

import javax.sip.*;

import javax.sip.message.*;
import javax.sip.address.*;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

/*
 * Bug reported by Will Scullin -- maddr was being ignored when routing
 * requests. Bug reported by Antonis Karydas - the RequestURI can be a non-sip
 * URI Jiang He - use address in route header. Significant changes to conform to
 * RFC 3261 made by Jeroen van Bemmel. Hagai Sela contributed a bug fix to the
 * strict route post processing code.
 * 
 */

/**
 * This is the default router. When the implementation wants to forward a
 * request and had run out of othe options, then it calls this method to figure
 * out where to send the request. The default router implements a simple
 * "default routing algorithm" which just forwards to the configured proxy
 * address.
 * 
 * <p>
 * When <code>javax.sip.USE_ROUTER_FOR_ALL_URIS</code> is set to
 * <code>false</code>, the next hop is determined according to the following
 * algorithm:
 * <ul>
 * <li> If the request contains one or more Route headers, use the URI of the
 * topmost Route header as next hop, possibly modifying the request in the
 * process if the topmost Route header contains no lr parameter(*)
 * <li> Else, if the property <code>javax.sip.OUTBOUND_PROXY</code> is set,
 * use its value as the next hop
 * <li> Otherwise, use the request URI as next hop. If the request URI is not a
 * SIP URI, call {@link javax.sip.address.Router#getNextHop(Request)} provided
 * by the application.
 * </ul>
 * 
 * <p>
 * (*)Note that in case the topmost Route header contains no 'lr' parameter
 * (which means the next hop is a strict router), the implementation will
 * perform 'Route Information Postprocessing' as described in RFC3261 section
 * 16.6 step 6 (also known as "Route header popping"). That is, the following
 * modifications will be made to the request:
 * <ol>
 * <li>The implementation places the Request-URI into the Route header field as
 * the last value.
 * <li>The implementation then places the first Route header field value into
 * the Request-URI and removes that value from the Route header field.
 * </ol>
 * Subsequently, the request URI will be used as next hop target
 * 
 * 
 * @version 1.2 $Revision: 1.11 $ $Date: 2007/02/22 21:00:57 $
 * 
 * @author M. Ranganathan <br/>
 * 
 */
public class DefaultRouter {

	private static DefaultRouter defaultRouter = null;
	
	private static AddressFactory addressFactory;
	
	private static Hop defaultRoute;


	/**
	 * Constructor 
	 */
	private DefaultRouter() {
		try {	       
            addressFactory = SipFactory.getInstance().createAddressFactory();
            String outboundProxy = Config.getConfigByName("sip.properties").getString("javax.sip.OUTBOUND_PROXY");
            defaultRoute = null;
	        if ((outboundProxy != null) && (outboundProxy.length() > 0))
	        {
	        	defaultRoute = new HopImpl(outboundProxy);
	        }
		} catch (Exception ex) {
			// The outbound proxy is optional. If specified it should be host:port/transport.
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.PROTOCOL, "Invalid default route specification - need host:port/transport");            
		}
	}

	/**
	 * Singleton : getInstance() 
	 */
	public static DefaultRouter getInstance() {
		if (defaultRouter == null)
		{
		    defaultRouter = new DefaultRouter();
		}
		return defaultRouter;
	}

	/**
	 * Singleton : getInstance() 
	 */
	public static void resetInstance() {
	    defaultRouter = null;
	}

	/**
	 * Return addresses for default proxy to forward the request to.
	 **/
	public Hop getNextHop(Msg msg) throws Exception {
		if (msg.isRequest())
		{
			return getNextHopRequest(msg);			
		} 
		else 
		{
			return getNextHopResponse(msg);
		}
	}
	
	/**
	 * Return addresses for default proxy to forward the request to. The list is
	 * organized in the following priority. If the requestURI refers directly to
	 * a host, the host and port information are extracted from it and made the
	 * next hop on the list. If the default route has been specified, then it is
	 * used to construct the next element of the list. <code>
	 * RouteHeader firstRoute = (RouteHeader) req.getHeader( RouteHeader.NAME );
	 * if (firstRoute!=null) {
	 *   URI uri = firstRoute.getAddress().getURI();
	 *    if (uri.isSIPUri()) {
	 *       SipURI nextHop = (SipURI) uri;
	 *       if ( nextHop.hasLrParam() ) {
	 *           // OK, use it
	 *       } else {
	 *           nextHop = fixStrictRouting( req );        <--- Here, make the modifications as per RFC3261
	 *       }
	 *   } else {
	 *       // error: non-SIP URI not allowed in Route headers
	 *       throw new SipException( "Request has Route header with non-SIP URI" );
	 *   }
	 * } else if (outboundProxy!=null) {
	 *   // use outbound proxy for nextHop
	 * } else if ( req.getRequestURI().isSipURI() ) {
	 *   // use request URI for nextHop
	 * } 
	 *
	 * </code>
	 * 
	 * @param request
	 *            is the sip request to route.
	 * 
	 */
	private Hop getNextHopRequest(Msg msg) throws Exception {
		Parameter requestURIParam = msg.getParameter("firstline.URI");
		if (requestURIParam == null) {
			return defaultRoute;
		}
		
		URI requestURI = addressFactory.createURI(requestURIParam.get(0).toString());
		if (requestURI == null)
			throw new IllegalArgumentException("Bad message: Null requestURI");

		Parameter routesParam = msg.getParameter("header.Route");

		/*
		 * In case the topmost Route header contains no 'lr' parameter (which
		 * means the next hop is a strict router), the implementation will
		 * perform 'Route Information Postprocessing' as described in RFC3261
		 * section 16.6 step 6 (also known as "Route header popping"). That is,
		 * the following modifications will be made to the request:
		 * 
		 * The implementation places the Request-URI into the Route header field
		 * as the last value.
		 * 
		 * The implementation then places the first Route header field value
		 * into the Request-URI and removes that value from the Route header
		 * field.
		 * 
		 * Subsequently, the request URI will be used as next hop target
		 */

		if ((routesParam != null) && (routesParam.length() > 0)) {

			// to send the request through a specified hop the application is
			// supposed to prepend the appropriate Route header which.
			Address routeAddress = addressFactory.createAddress(routesParam.get(0).toString());
			URI uri = routeAddress.getURI();
			if (uri.isSipURI()) {
				SipURI sipUri = (SipURI) uri;
				if (!sipUri.hasLrParam()) {

					// fixStrictRouting(sipRequest);
					GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Fixed strict routing like rfc2543 is not supported !");
				}
				
				Hop hop = createHop(sipUri);
				GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "NextHop based on Route:" + hop);
				return hop;
			} else {
				throw new SipException("First Route not a SIP URI");
			}

		} else if (requestURI.isSipURI()
				&& ((SipURI) requestURI).getMAddrParam() != null) {
			Hop hop = createHop((SipURI) requestURI);
			GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Using request URI maddr to route the request =", hop);
			return hop;

		} else if (defaultRoute != null) {
			GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Using outbound proxy to route the request = ", defaultRoute);
			return defaultRoute;
		} else if (requestURI.isSipURI()) {
			Hop hop = createHop((SipURI) requestURI);
			if (hop != null)
				GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Used request-URI for nextHop = ", hop);
			else {
				GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "returning null hop - loop detected");
			}
			return hop;

		} else {
			// The internal router should never be consulted for non-sip URIs.
			GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "Unexpected non-sip URI");
			return null;
		}

	}

	/**
	 * Performs strict router fix according to RFC3261 section 16.6 step 6
	 * NOT SUPPORTED
	 * 
	 * pre: top route header in request has no 'lr' parameter in URI post:
	 * request-URI added as last route header, new req-URI = top-route-URI
	 */
	private void fixStrictRouting(SIPRequest req) {

		RouteList routes = req.getRouteHeaders();
		Route first = (Route) routes.getFirst();
		SipUri firstUri = (SipUri) first.getAddress().getURI();
		routes.removeFirst();

		// Add request-URI as last Route entry
		AddressImpl addr = new AddressImpl();
		addr.setAddess(req.getRequestURI()); // don't clone it
		Route route = new Route(addr);

		routes.add(route); // as last one
		req.setRequestURI(firstUri);
		GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.PROTOCOL, "post: fixStrictRouting", req);
	}

	/**
	 * Utility method to create a hop from a SIP URI
	 * 
	 * @param sipUri
	 * @return
	 */

	private final Hop createHop(SipURI sipUri) {
		// always use TLS when secure
		String transport = sipUri.isSecure() ? SIPConstants.TLS : sipUri
				.getTransportParam();
		if (transport == null)
			transport = SIPConstants.UDP;

		int port;
		if (sipUri.getPort() != -1) {
			port = sipUri.getPort();
		} else {
			if (transport.equalsIgnoreCase(SIPConstants.TLS))
				port = 5061;
			else
				port = 5060; // TCP or UDP
		}
		String host = sipUri.getMAddrParam() != null ? sipUri.getMAddrParam()
				: sipUri.getHost();
		return new HopImpl(host, port, transport);

	}

	/**
	 * Return addresses for default proxy to forward the request to. The list is
	 * organized in the following priority. If the requestURI refers directly to
	 * a host, the host and port information are extracted from it and made the
	 * next hop on the list. If the default route has been specified, then it is
	 * used to construct the next element of the list. <code>
	 * RouteHeader firstRoute = (RouteHeader) req.getHeader( RouteHeader.NAME );
	 * if (firstRoute!=null) {
	 *   URI uri = firstRoute.getAddress().getURI();
	 *    if (uri.isSIPUri()) {
	 *       SipURI nextHop = (SipURI) uri;
	 *       if ( nextHop.hasLrParam() ) {
	 *           // OK, use it
	 *       } else {
	 *           nextHop = fixStrictRouting( req );        <--- Here, make the modifications as per RFC3261
	 *       }
	 *   } else {
	 *       // error: non-SIP URI not allowed in Route headers
	 *       throw new SipException( "Request has Route header with non-SIP URI" );
	 *   }
	 * } else if (outboundProxy!=null) {
	 *   // use outbound proxy for nextHop
	 * } else if ( req.getRequestURI().isSipURI() ) {
	 *   // use request URI for nextHop
	 * } 
	 *
	 * </code>
	 * 
	 * @param request
	 *            is the sip request to route.
	 * 
	 */
	private Hop getNextHopResponse(Msg msg) throws Exception {		
		
		Parameter viaParam = msg.getParameter("header.TopmostVia");
		
		// case Via header is not present
		if ((viaParam == null) || (viaParam.length() <= 0))
			throw new SipException("No via header in response!");
		
		// get the transport 
		Parameter viaTransParam = msg.getParameter("header.TopmostVia.Transport");
		String transport = StackFactory.PROTOCOL_UDP;
		if ((viaTransParam != null) && (viaTransParam.length() > 0))
		{
			transport = viaTransParam.get(0).toString();
		}
		
		// check to see if Via has "received parameter". If so
		// set the host to the via parameter. Else set it to the
		// Via host.
		Parameter viaRecParam = msg.getParameter("header.TopmostVia.Parameter.Received");
		String host = null; 
		if ((viaRecParam != null) && (viaRecParam.length() > 0) && (viaRecParam.get(0).toString().length() > 0))
		{		
			host = viaRecParam.get(0).toString();			
		} 
		else
		{
			Parameter viaHostParam = msg.getParameter("header.TopmostVia.Host");
			if ((viaHostParam != null) && (viaHostParam.length() > 0))
			{		
				host = viaHostParam.get(0).toString();
			}
			else
			{
				GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Invalid Via header");
			}
		}
	
		// Symmetric nat support
		// check to see if Via has "RPort" parameter". If so
		// set the host to the via parameter. Else set it to the
		// Via port.
		Parameter viaRPortParam = msg.getParameter("header.TopmostVia.Parameter.RPort");
		int port = -1; 
		if ((viaRPortParam != null) && (viaRPortParam.length() > 0) && (viaRPortParam.get(0).toString().length() > 0))
		{		
			port = Integer.parseInt(viaRPortParam.get(0).toString()); 					
		} 
		else
		{
			Parameter viaPortParam = msg.getParameter("header.TopmostVia.Port");
			if ((viaPortParam != null) && (viaPortParam.length() > 0))
			{		
				port = Integer.parseInt(viaPortParam.get(0).toString());
			}
		}
	
		Hop hop = new HopImpl(host, port, transport);

		return hop;
	}
}