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

package com.devoteam.srit.xmlloader.http2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncServer;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.ssl.BasicServerTlsStrategy;
import org.apache.hc.core5.http.nio.support.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.support.BasicResponseProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.frame.RawFrame;
import org.apache.hc.core5.http2.impl.nio.H2StreamListener;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.reactor.IOReactorConfig;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.Trans;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;
import com.devoteam.srit.xmlloader.core.utils.Config;

public class ListenpointHttp2 extends Listenpoint {

	private HttpAsyncServer server = null;
	private URIAuthority authority = null;
	private boolean secure = false;
	private ListenpointHttp2 listenpoint = this;
	
	/** Creates a new instance of Listenpoint */
	public ListenpointHttp2(Stack stack) throws Exception {
		super(stack);
	}

	// ---------------------------------------------------------------------
	// methods for the XML display / parsing
	// ---------------------------------------------------------------------

	@Override
	public boolean create(String protocol) throws Exception {
		int portToUse = 0;
		if(this.getPort() == 0 && this.getPortTLS() != 0) {
			portToUse = this.getPortTLS();
			secure = true;
		}
		else {
			portToUse = this.getPort();
		}
		
		int timeout = Integer.parseInt(Config.getConfigByName("http2.properties").getString("SOCKET_TIMEOUT", "30"));
		IOReactorConfig config = IOReactorConfig.custom().setSoTimeout(timeout, TimeUnit.SECONDS).setTcpNoDelay(true)
				.build();

		server = H2ServerBootstrap.bootstrap().setIOReactorConfig(config)
				.setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
				.setCanonicalHostName("localhost")
				.setTlsStrategy(secure ? new BasicServerTlsStrategy(StackHttp2.createServerSSLContext(),SecureAllPortsStrategy.INSTANCE) : null)
				.setStreamListener(new H2StreamListener() {
					@Override
					public void onHeaderInput(final HttpConnection connection, final int streamId,
							final List<? extends Header> headers) {
						Iterator<? extends Header> iterator = headers.iterator();
						while (iterator.hasNext()) {
							Header header = iterator.next();
							if (header.getName().equals(":authority")) {
								String[] parts = header.getValue().split(":");
								if(parts.length == 1) 
									authority = new URIAuthority(parts[0]);
								else if(parts.length > 1) 
									authority = new URIAuthority(parts[0], Integer.parseInt(parts[1]));
								iterator.remove();								
							}
						}					
					}

					@Override
					public void onHeaderOutput(final HttpConnection connection, final int streamId,
							final List<? extends Header> headers) {
					}

					@Override
					public void onInputFlowControl(final HttpConnection connection, final int streamId, final int delta,
							final int actualSize) {
					}

					@Override
					public void onOutputFlowControl(final HttpConnection connection, final int streamId,
							final int delta, final int actualSize) {
					}

					@Override
					public void onFrameInput(HttpConnection connection, int streamId, RawFrame frame) {
					}

					@Override
					public void onFrameOutput(HttpConnection connection, int streamId, RawFrame frame) {
					}

				})
	
				.register("*", serverRequestHandler).create();
				
				
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("HTTP2 server shutting down");
				remove();
			}
		});
		server.start();
		server.listen(new InetSocketAddress(portToUse));

		return true;
	}

	@Override
	public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
		MsgHttp2 beginMsg = (MsgHttp2) msg.getTransaction().getBeginMsg();		

		MsgHttp2 msgHttp2 = (MsgHttp2) msg;
		msgHttp2.setType(beginMsg.getType());
		msgHttp2.setListenpoint(beginMsg.getListenpoint());
		HttpResponse msgResponse = (HttpResponse) msgHttp2.getMessage();
		if(msgHttp2.getMessageContent() == null){
			beginMsg.getResponseTrigger().submitResponse(new BasicResponseProducer(msgResponse,
					new BasicAsyncEntityProducer(new byte[0])), beginMsg.getContext());
		} else {
			beginMsg.getResponseTrigger().submitResponse(new BasicResponseProducer(msgResponse,
					new BasicAsyncEntityProducer(msgHttp2.getMessageContent().getBytes())), beginMsg.getContext());
		}
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Listenpoint: sendMessage() ",
				msgHttp2);
		return true;
	}

	/**
	 * Returns the string description of the message. Used for logging as DEBUG
	 * level
	 */
	public String toString() {
		String str = "";
		if (name != null) {
			str += "name=\"" + name + "\"";
		} else {
			str += "name=\"\"";
		}

		str += " localHost=\"" + this.getAddressesString() + "\"";
		str += " localPort=\"" + super.getPort() + "\"";
		str += " localPortTLS=\"" + super.getPortTLS() + "\"";
		return str;
	}

	public boolean remove() {
		System.out.println("ListenpointHttp2.close() ");
		server.close(CloseMode.GRACEFUL);
		
		return true;
	}

	// ------------------------------------------------------
	// method for the "setFromMessage" <parameter> operation
	// ------------------------------------------------------

	/** equals method */
	public boolean equals(Listenpoint listenpoint) {
		if (listenpoint == null) {
			return false;
		}

		String name = listenpoint.getName();
		if (null != this.name) {
			if (!this.name.equals(name)) {
				return false;
			}
		}

		if (!this.getAddressesString().equals(listenpoint.getAddressesString())) {
			return false;
		}

		if (super.getPort() != listenpoint.getPort()) {
			if (super.getPort() != 0) {
				return false;
			}
		}
		return true;
	}

	AsyncServerRequestHandler<Message<HttpRequest, Void>> serverRequestHandler= new AsyncServerRequestHandler<Message<HttpRequest, Void>>() {
		Map<HttpRequest,EntityDetails> myEntityDetailsMap = new HashMap<HttpRequest,EntityDetails>(); 
		Map<HttpRequest,AsyncEntityConsumer> dataConsumerMap = new HashMap<HttpRequest,AsyncEntityConsumer>();
		
		@Override
		public AsyncRequestConsumer<Message<HttpRequest, Void>> prepare(final HttpRequest request,
				final EntityDetails entityDetails, final HttpContext context) throws HttpException {	
			AsyncEntityConsumer dataConsumer = new BasicAsyncEntityConsumer();
			dataConsumerMap.put(request,dataConsumer);
			
			return new BasicRequestConsumer<>(entityDetails != null ? dataConsumer : null);
		}

		@Override
		public void handle(final Message<HttpRequest, Void> requestMessage,
				final ResponseTrigger responseTrigger, final HttpContext context)
						throws HttpException, IOException {
			AsyncEntityConsumer dataConsumer = dataConsumerMap.get(requestMessage.getHead());
			
			try {
				byte[] body = (byte[]) dataConsumer.getContent();
				if(authority != null) {
					requestMessage.getHead().setAuthority(authority);
				}
			
				MsgHttp2 msg = new MsgHttp2(stack, requestMessage.getHead());		
				
				TransactionId transactionId = new TransactionId(UUID.randomUUID().toString());
				Trans transaction = new Trans(stack, msg);
				msg.setTransaction(transaction);
				msg.setTransactionId(transactionId);
				msg.setResponseTrigger(responseTrigger);
				msg.setContext(context);
				msg.setListenpoint(listenpoint);
				
				if(body != null) {
					msg.setMessageContent(new String(body));
				}				
	
				stack.receiveMessage(msg);

				GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Listenpoint: receiveMessage() ", msg);
			} catch (Exception e) {
				GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e);
			}
			finally {
				dataConsumerMap.remove(requestMessage.getHead());
			}

		}
	};

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

}

