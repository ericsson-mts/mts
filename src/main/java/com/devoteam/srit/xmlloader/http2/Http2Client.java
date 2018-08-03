package com.devoteam.srit.xmlloader.http2;


import java.io.IOException;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.concurrent.BasicFuture;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.Http2Processors;
import org.apache.hc.core5.http2.nio.support.DefaultAsyncPushConsumerFactory;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

import com.devoteam.srit.xmlloader.http2.util.ClientSessionEndpoint;
import com.devoteam.srit.xmlloader.http2.util.InternalClientHttp2EventHandlerFactory;

public class Http2Client extends AsyncClient {

    private final SSLContext sslContext;
    private final RequestHandlerRegistry<Supplier<AsyncPushConsumer>> registry;

    public Http2Client(final IOReactorConfig ioReactorConfig, final SSLContext sslContext) throws IOException {
        super(ioReactorConfig);
        this.sslContext = sslContext;
        this.registry = new RequestHandlerRegistry<>();
    }

    public Http2Client() throws IOException {
        this(IOReactorConfig.DEFAULT, null);
    }

    public void register(final String uriPattern, final Supplier<AsyncPushConsumer> supplier) {
        Args.notNull(uriPattern, "URI pattern");
        Args.notNull(supplier, "Supplier");
        registry.register(null, uriPattern, supplier);
    }

    public void start(final IOEventHandlerFactory handlerFactory) throws IOException {
        super.execute(handlerFactory);
    }

    public void start(final HttpProcessor httpProcessor, final H2Config h2Config) throws IOException {
        start(new InternalClientHttp2EventHandlerFactory(
                httpProcessor,
                new DefaultAsyncPushConsumerFactory(registry),
                HttpVersionPolicy.FORCE_HTTP_2,
                h2Config,
                H1Config.DEFAULT,
                CharCodingConfig.DEFAULT,
                sslContext));
    }

    public void start(final HttpProcessor httpProcessor, final H1Config h1Config) throws IOException {
        start(new InternalClientHttp2EventHandlerFactory(
                httpProcessor,
                new DefaultAsyncPushConsumerFactory(registry),
                HttpVersionPolicy.FORCE_HTTP_1,
                H2Config.DEFAULT,
                h1Config,
                CharCodingConfig.DEFAULT,
                sslContext));
    }

    public void start(final H2Config h2Config) throws IOException {
        start(Http2Processors.client(), h2Config);
    }

    public void start(final H1Config h1Config) throws IOException {
        start(Http2Processors.client(), h1Config);
    }

    public void start() throws Exception {
        start(H2Config.DEFAULT);
    }

    public Future<ClientSessionEndpoint> connect(
            final HttpHost host,
            final TimeValue timeout,
            final FutureCallback<ClientSessionEndpoint> callback) throws InterruptedException {
        final BasicFuture<ClientSessionEndpoint> future = new BasicFuture<>(callback);
        requestSession(host, timeout, new FutureCallback<IOSession>() {

            @Override
            public void completed(final IOSession session) {
                future.completed(new ClientSessionEndpoint(session));
            }

            @Override
            public void failed(final Exception cause) {
                future.failed(cause);
            }

            @Override
            public void cancelled() {
                future.cancel();
            }
        });
        return future;
    }

    public Future<ClientSessionEndpoint> connect(final HttpHost host,final TimeValue timeout) throws InterruptedException {
        return connect(host, timeout, null);
    }

    public Future<ClientSessionEndpoint> connect(final String hostname, final int port, final TimeValue timeout) throws InterruptedException {
        return connect(new HttpHost(hostname, port), timeout, null);
    }

}
