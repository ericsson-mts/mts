package com.devoteam.srit.xmlloader.http2.util;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.impl.nio.ClientHttp1StreamDuplexerFactory;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.Http2Processors;
import org.apache.hc.core5.http2.impl.nio.ClientHttp2StreamMultiplexerFactory;
import org.apache.hc.core5.http2.impl.nio.ClientHttpProtocolNegotiator;
import org.apache.hc.core5.reactor.IOEventHandler;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.TlsCapableIOSession;
import org.apache.hc.core5.util.Args;

public class InternalClientHttp2EventHandlerFactory implements IOEventHandlerFactory {

    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncPushConsumer> exchangeHandlerFactory;
    private final HttpVersionPolicy versionPolicy;
    private final H2Config h2Config;
    private final H1Config h1Config;
    private final CharCodingConfig charCodingConfig;
    private final SSLContext sslContext;

    public InternalClientHttp2EventHandlerFactory(
            final HttpProcessor httpProcessor,
            final HandlerFactory<AsyncPushConsumer> exchangeHandlerFactory,
            final HttpVersionPolicy versionPolicy,
            final H2Config h2Config,
            final H1Config h1Config,
            final CharCodingConfig charCodingConfig,
            final SSLContext sslContext) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.exchangeHandlerFactory = exchangeHandlerFactory;
        this.versionPolicy = versionPolicy != null ? versionPolicy : HttpVersionPolicy.NEGOTIATE;
        this.h2Config = h2Config != null ? h2Config : H2Config.DEFAULT;
        this.h1Config = h1Config != null ? h1Config : H1Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.sslContext = sslContext;
    }

    @Override
    public IOEventHandler createHandler(final TlsCapableIOSession ioSession, final Object attachment) {
        if (sslContext != null) {
            ioSession.startTls(sslContext, null ,null, null);
        }
        final ClientHttp1StreamDuplexerFactory http1StreamHandlerFactory = new ClientHttp1StreamDuplexerFactory(
                httpProcessor != null ? httpProcessor : HttpProcessors.client(),
                h1Config,
                charCodingConfig,
                LoggingHttp1StreamListener.INSTANCE_CLIENT);
        final ClientHttp2StreamMultiplexerFactory http2StreamHandlerFactory = new ClientHttp2StreamMultiplexerFactory(
                httpProcessor != null ? httpProcessor : Http2Processors.client(),
                exchangeHandlerFactory,
                h2Config,
                charCodingConfig,
                LoggingHttp2StreamListener.INSTANCE);
        return new ClientHttpProtocolNegotiator(
                        ioSession,
                        http1StreamHandlerFactory,
                        http2StreamHandlerFactory,
                        versionPolicy != null ? versionPolicy : HttpVersionPolicy.NEGOTIATE);
   }

}
