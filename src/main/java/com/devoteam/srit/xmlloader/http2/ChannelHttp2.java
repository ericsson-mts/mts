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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.BasicRequestProducer;
import org.apache.hc.core5.http.nio.BasicResponseConsumer;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.DefaultH2RequestConverter;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;

/**
 *
 * @author qqin
 */

public class ChannelHttp2 extends Channel {

	private HttpAsyncRequester requester = null;
	protected boolean secure;
	private AsyncClientEndpoint clientEndpoint;
	private HttpHost target;
	private FutureCallback<Message<HttpResponse, String>> callback;
	final DefaultH2RequestConverter converter = new DefaultH2RequestConverter();
	
	/** Creates a new instance of Channel */
	public ChannelHttp2(Stack stack) throws Exception {
		super(stack);
	}

	/** Creates a new instance of Channel */
	public ChannelHttp2(String name, String localHost, String localPort, String remoteHost, String remotePort,
			String aProtocol, boolean secure) throws Exception {
		super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
	}

	// ---------------------------------------------------------------------
	// methods for the transport
	// ---------------------------------------------------------------------

	@Override
	public boolean open() throws Exception {
		String hostname = this.getRemoteHost();
		int port = this.getRemotePort();

		IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoTimeout(15, TimeUnit.SECONDS).build();

		H2Config h2Config = H2Config.custom().setPushEnabled(false).setMaxConcurrentStreams(1000).build();

		requester = H2RequesterBootstrap.bootstrap().setIOReactorConfig(ioReactorConfig)
				.setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2).setH2Config(h2Config)
				.create();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("HTTP2 requester shutting down");
				requester.close(CloseMode.GRACEFUL);
			}
		});

		requester.start();

		target = new HttpHost(hostname, port);
		Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(50));
		clientEndpoint = future.get();

		return true;
	}

	/** Close a channel */
	@Override
	public boolean close() {
		System.out.println("ChannelHttp2.close()");
		clientEndpoint.releaseAndDiscard();
		requester.initiateShutdown();
		return super.close();
	}

	/** Send a Msg to Channel */
	@Override
	public boolean sendMessage(Msg msg) throws Exception {		
		MsgHttp2 msgHttp2 = (MsgHttp2) msg;
		HttpRequest msgRequest = (HttpRequest) msgHttp2.getMessage();
		//String type = msgHttp2.getType();
		
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Channel: sendMessage() ", msg);
		
		BasicAsyncEntityProducer entityProducer = new BasicAsyncEntityProducer(msgHttp2.getMessageContent().getBytes());
		BasicRequestProducer requestProducer = new BasicRequestProducer(msgRequest, entityProducer);

		clientEndpoint.execute(requestProducer,		
				new BasicResponseConsumer<>(new StringAsyncEntityConsumer() ),
				new FutureCallback<Message<HttpResponse, String>>() {
					@Override
					public void completed(final Message<HttpResponse, String> message) {
						HttpResponse response = message.getHead();
						String body = message.getBody();

						try {
							MsgHttp2 msgResponse = new MsgHttp2(stack, response);
							msgResponse.setMessageContent(body);
							// Set transactionId in message for a request
							msgResponse.setTransactionId(msg.getTransactionId());
							msgResponse.setChannel(msg.getChannel());

							GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() ", msgResponse);
							receiveMessage(msgResponse);
						} catch (Exception e) {							
							GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() ", e);
						}

						entityProducer.releaseResources();
						requestProducer.releaseResources();
					}

					@Override
					public void failed(final Exception ex) {
						GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() ", ex);
						entityProducer.releaseResources();
						requestProducer.releaseResources();
						
					}

					@Override
					public void cancelled() {
						GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage(): Canceled");
						entityProducer.releaseResources();
						requestProducer.releaseResources();
					}

				});

		return true;
	}

	/** Get the transport protocol */
	@Override
	public String getTransport() {
		if (secure) {
			return StackFactory.PROTOCOL_TLS;
		} else {
			return StackFactory.PROTOCOL_TCP;
		}
	}

	public FutureCallback<Message<HttpResponse, String>> getCallback() {
		return callback;
	}

	public void setCallback(FutureCallback<Message<HttpResponse, String>> callback) {
		this.callback = callback;
	}

}
