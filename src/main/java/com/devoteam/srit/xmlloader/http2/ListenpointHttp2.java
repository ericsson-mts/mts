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
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.BasicResponseProducer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.frame.RawFrame;
import org.apache.hc.core5.http2.impl.nio.Http2StreamListener;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Listenpoint;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.TransactionId;

public class ListenpointHttp2 extends Listenpoint {

	private HttpAsyncServer server = null;

	/** Creates a new instance of Listenpoint */
	public ListenpointHttp2(Stack stack) throws Exception {
		super(stack);
	}

	// ---------------------------------------------------------------------
	// methods for the XML display / parsing
	// ---------------------------------------------------------------------

	@Override
	public boolean create(String protocol) throws Exception {
		IOReactorConfig config = IOReactorConfig.custom().setSoTimeout(50, TimeUnit.SECONDS).setTcpNoDelay(true)
				.build();

		server = H2ServerBootstrap.bootstrap().setIOReactorConfig(config)
				.setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2).setStreamListener(new Http2StreamListener() {

					@Override
					public void onHeaderInput(final HttpConnection connection, final int streamId,
							final List<? extends Header> headers) {
						Iterator<? extends Header> iterator = headers.iterator();
						while (iterator.hasNext()) {
							if (iterator.next().getName().equals(":authority")) {
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

				}).register("*", serverRequestHandler).create();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("HTTP2 server shutting down");
				shutdown();
			}
		});

		server.start();
		server.listen(new InetSocketAddress(super.getPort()));

		return true;
	}

	@Override
	public boolean sendMessage(Msg msg, String remoteHost, int remotePort, String transport) throws Exception {
		MsgHttp2 beginMsg = (MsgHttp2) msg.getTransaction().getBeginMsg();
		msg.setListenpoint(beginMsg.getListenpoint());

		MsgHttp2 msgHttp2 = (MsgHttp2) msg;
		HttpResponse msgResponse = (HttpResponse) msgHttp2.getMessage();

		beginMsg.getResponseTrigger().submitResponse(new BasicResponseProducer(msgResponse,
				new BasicAsyncEntityProducer(msgHttp2.getMessageContent().getBytes())), beginMsg.getContext());

		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Listenpoint: sendMessage() ",
				msg);
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
		return str;
	}

	public void shutdown() {
		server.close(CloseMode.GRACEFUL);
	}

	// ------------------------------------------------------
	// method for the "setFromMessage" <parameter> operation
	// ------------------------------------------------------

	/** equals method */
	public boolean equals(Listenpoint listenpoint) {
		System.out.println("ListenpointHttp2.equals()");
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
			
			myEntityDetailsMap.put(request,entityDetails);
		
			AsyncEntityConsumer dataConsumer = new BasicAsyncEntityConsumer();
			dataConsumerMap.put(request,dataConsumer);

			return new BasicRequestConsumer<>(entityDetails != null ? dataConsumer : null);
		}

		@Override
		public void handle(final Message<HttpRequest, Void> requestMessage,
				final ResponseTrigger responseTrigger, final HttpContext context)
						throws HttpException, IOException {
			EntityDetails myEntityDetails = myEntityDetailsMap.get(requestMessage.getHead());
			AsyncEntityConsumer dataConsumer = dataConsumerMap.get(requestMessage.getHead());
			
			try {
				byte[] body = (byte[]) dataConsumer.getContent();

				MsgHttp2 msg = new MsgHttp2(stack, requestMessage.getHead());
				TransactionId transactionId = new TransactionId(UUID.randomUUID().toString());
				msg.setTransactionId(transactionId);
				msg.setResponseTrigger(responseTrigger);
				msg.setContext(context);
				msg.setMessageContent(new String(body));
	
				System.out.println("protocole version: " + context.getProtocolVersion());
				stack.receiveMessage(msg);

				GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Listenpoint: receiveMessage() ", msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};
}
