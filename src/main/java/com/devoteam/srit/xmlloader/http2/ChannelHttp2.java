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

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.impl.bootstrap.HttpAsyncRequester;
import org.apache.hc.core5.http.nio.AsyncClientEndpoint;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.nio.bootstrap.H2RequesterBootstrap;
import org.apache.hc.core5.http2.ssl.H2ClientTlsStrategy;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.ssl.SSLSessionVerifier;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.util.Timeout;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;

/**
 *
 * @author qqin
 */

public class ChannelHttp2 extends Channel {

	private HttpAsyncRequester requester = null;
	private boolean secure;
	private AsyncClientEndpoint clientEndpoint;
	private HttpHost target;
	
	/** Creates a new instance of Channel */
	public ChannelHttp2(Stack stack) throws Exception {
		super(stack);
	}

	/** Creates a new instance of Channel */
	public ChannelHttp2(String name, String localHost, String localPort, String remoteHost, String remotePort,
			String aProtocol, boolean secure) throws Exception {
		super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
		this.secure = secure;		
	}

	// ---------------------------------------------------------------------
	// methods for the transport
	// ---------------------------------------------------------------------

	@Override
	public boolean open() throws Exception {
		String hostname = this.getRemoteHost();
		int port = this.getRemotePort();

		int timeout = Integer.parseInt(Config.getConfigByName("http2.properties").getString("SOCKET_TIMEOUT", "30"));
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoTimeout(timeout, TimeUnit.SECONDS).build();

		H2Config h2Config = H2Config.custom().setPushEnabled(false).setMaxConcurrentStreams(1000).build();
		requester = H2RequesterBootstrap.bootstrap().setIOReactorConfig(ioReactorConfig)
				.setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2).setH2Config(h2Config)
				.setTlsStrategy(secure ? new H2ClientTlsStrategy(StackHttp2.createClientSSLContext(), new SSLSessionVerifier() {
                    @Override
                    public TlsDetails verify(final NamedEndpoint endpoint, final SSLEngine sslEngine) throws SSLException {
                    	System.out.println("TlsStrategy verify");
                        return null;
                    }
                }) : null)
				.create();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("HTTP2 requester shutting down");
				requester.close(CloseMode.GRACEFUL);
			}
		});

		requester.start();

		if(secure) {
			target = new HttpHost("https", hostname, port);
		}
		else {
			target = new HttpHost(hostname, port);
		}
		
		Future<AsyncClientEndpoint> future = requester.connect(target, Timeout.ofSeconds(timeout));
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

		if(msgRequest.getScheme() == null) {
			msgRequest.setScheme(Config.getConfigByName("http2.properties").getString("client.DEFAULT_PROTOCOL"));
		}
	
		GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Channel: sendMessage() ", msg);

		BasicAsyncEntityProducer entityProducer;
		if(msgHttp2.getMessageContent() == null){
			entityProducer = new BasicAsyncEntityProducer(new byte[0]);
		} else {
			entityProducer = new BasicAsyncEntityProducer(msgHttp2.getMessageContent().getBytes());
		}
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
							msgResponse.setTransaction(msg.getTransaction());
							msgResponse.setTransactionId(msg.getTransactionId());
							msgResponse.setChannel(msg.getChannel());

							GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() completed, ", msgResponse);
							receiveMessage(msgResponse);
						} catch (Exception e) {							
							GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() completed Exception, ", e);
						}

						entityProducer.releaseResources();
						requestProducer.releaseResources();
					}

					@Override
					public void failed(final Exception ex) {
						GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage() failed, ", ex);
						ex.printStackTrace();
						entityProducer.releaseResources();
						requestProducer.releaseResources();
						
					}

					@Override
					public void cancelled() {
						GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, "Channel: receiveMessage(): Canceled ");
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
}
