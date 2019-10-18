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

import java.util.concurrent.TimeUnit;

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
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.Channel;
import com.devoteam.srit.xmlloader.core.protocol.Msg;
import com.devoteam.srit.xmlloader.core.protocol.Stack;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.utils.Config;
import java.util.Optional;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.protocol.HttpProcessorBuilder;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.util.TimeValue;

/**
 *
 * @author qqin
 */
public class ChannelHttp2 extends Channel {

    private HttpAsyncRequester requester = null;
    private boolean secure;
    private AsyncClientEndpoint clientEndpoint;
    private HttpHost target;

    /**
     * Creates a new instance of Channel
     */
    public ChannelHttp2(Stack stack) throws Exception {
        super(stack);
    }

    /**
     * Creates a new instance of Channel
     */
    public ChannelHttp2(String name, String localHost, String localPort, String remoteHost, String remotePort, String aProtocol, boolean secure) throws Exception {
        super(name, localHost, localPort, remoteHost, remotePort, aProtocol);
        this.secure = secure;
    }

    // ---------------------------------------------------------------------
    // methods for the transport
    // ---------------------------------------------------------------------
    @Override
    public boolean open() throws Exception {
        Config config = Config.getConfigByName("http2.properties");

        String hostname = this.getRemoteHost();
        int port = this.getRemotePort();
        int timeout = Integer.parseInt(config.getString("SOCKET_TIMEOUT", "30"));
        int initialWindowSize = Integer.parseInt(config.getString("client.INITIAL_WINDOW_SIZE", "1073741824"));
        int maxConcurrentStreams = Integer.parseInt(config.getString("client.MAX_CONCURRENT_STREAMS", "100"));

        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(timeout, TimeUnit.SECONDS)
                .build();

        H2Config h2Config = H2Config.custom()
                .setInitialWindowSize(initialWindowSize)
                .setPushEnabled(false)
                .setMaxConcurrentStreams(maxConcurrentStreams)
                .build();

        H2RequesterBootstrap bootstrap = H2RequesterBootstrap
                .bootstrap()
                .setIOReactorConfig(ioReactorConfig)
                .setVersionPolicy(HttpVersionPolicy.FORCE_HTTP_2)
                .setHttpProcessor(HttpProcessorBuilder.create().addAll(
                        (HttpRequestInterceptor) (request, entity, context) -> {
                            // transform legacy "Host" header (if present) to authority pseudo-header
                            Optional.ofNullable(request.getHeader(HttpHeaders.HOST)).ifPresent(header -> {
                                request.setAuthority(new URIAuthority(header.getValue()));
                                request.removeHeader(header);
                            });
                            
                            // fix Content-Length header if present and if there is any content
                            Optional.ofNullable(request.getHeader(HttpHeaders.CONTENT_LENGTH)).ifPresent(header -> {
                                request.removeHeader(header);
                                long contentLength = entity.getContentLength();
                                if(contentLength > 0 ){
                                    request.addHeader(new BasicHeader(HttpHeaders.CONTENT_LENGTH, contentLength));
                                }
                            });
                        }).build())
                .setExceptionCallback(e -> GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Error in HTTP channel " + ChannelHttp2.this.getName()))
                .setH2Config(h2Config);

        if (secure) {
            bootstrap.setTlsStrategy(new H2ClientTlsStrategy(StackHttp2.createClientSSLContext()));
        }

        requester = bootstrap.create();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                requester.close(CloseMode.IMMEDIATE);
            }
        });

        requester.start();

        if (secure) {
            target = new HttpHost("https", hostname, port);
        } else {
            target = new HttpHost(hostname, port);
        }

        clientEndpoint = requester.connect(target, Timeout.ofSeconds(timeout)).get();

        return true;
    }

    /**
     * Close a channel
     */
    @Override
    public boolean close() {
        try {
            clientEndpoint.releaseAndDiscard();
            requester.initiateShutdown();
            requester.awaitShutdown(TimeValue.MAX_VALUE);
            return super.close();
        } catch (Exception e) {
            throw new RuntimeException("Forward as RunTimeException", e);
        }
    }

    /**
     * Send a Msg to Channel
     */
    @Override
    public boolean sendMessage(Msg msg) throws Exception {
        MsgHttp2 msgHttp2 = (MsgHttp2) msg;
        HttpRequest msgRequest = (HttpRequest) msgHttp2.getMessage();

        if (msgRequest.getScheme() == null) {
            msgRequest.setScheme(Config.getConfigByName("http2.properties").getString("client.DEFAULT_PROTOCOL"));
        }

        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.PROTOCOL, "Channel: sendMessage() ", msg);

        BasicAsyncEntityProducer entityProducer;
        if (msgHttp2.getMessageContent() == null) {
            entityProducer = new BasicAsyncEntityProducer(new byte[0]);
        } else {
            entityProducer = new BasicAsyncEntityProducer(msgHttp2.getMessageContent().getBytes());
        }
        BasicRequestProducer requestProducer = new BasicRequestProducer(msgRequest, entityProducer);

        clientEndpoint.execute(requestProducer,
                new BasicResponseConsumer<>(new StringAsyncEntityConsumer()),
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
                    GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, e, "Channel: receiveMessage() completed Exception, ", e);
                }

                entityProducer.releaseResources();
                requestProducer.releaseResources();
            }

            @Override
            public void failed(final Exception ex) {
                GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.PROTOCOL, ex, "Channel: receiveMessage() failed, ");
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

    /**
     * Get the transport protocol
     */
    @Override
    public String getTransport() {
        if (secure) {
            return StackFactory.PROTOCOL_TLS;
        } else {
            return StackFactory.PROTOCOL_TCP;
        }
    }
}
