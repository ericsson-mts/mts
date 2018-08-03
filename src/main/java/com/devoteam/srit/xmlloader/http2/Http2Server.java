package com.devoteam.srit.xmlloader.http2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import javax.net.ssl.SSLContext;

import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.H1Config;
import org.apache.hc.core5.http.impl.HttpProcessors;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.support.BasicAsyncServerExpectationDecorator;
import org.apache.hc.core5.http.nio.support.BasicServerExchangeHandler;
import org.apache.hc.core5.http.nio.support.DefaultAsyncResponseExchangeHandlerFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.impl.Http2Processors;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.ListenerEndpoint;


import com.devoteam.srit.xmlloader.http2.util.InternalServerHttp2EventHandlerFactory;

public class Http2Server extends AsyncServer {

    private final SSLContext sslContext;
    private final RequestHandlerRegistry<Supplier<AsyncServerExchangeHandler>> registry;

    public Http2Server(final IOReactorConfig ioReactorConfig, final SSLContext sslContext) throws IOException {
        super(ioReactorConfig);
        this.sslContext = sslContext;
        this.registry = new RequestHandlerRegistry<>();
    }

    public Http2Server() throws IOException {
        this(IOReactorConfig.DEFAULT, null);
    }

    public void register(final String uriPattern, final Supplier<AsyncServerExchangeHandler> supplier) {
        registry.register(null, uriPattern, supplier);
    }

    public <T> void register(
            final String uriPattern,
            final AsyncServerRequestHandler<T> requestHandler) {
        register(uriPattern, new Supplier<AsyncServerExchangeHandler>() {

            @Override
            public AsyncServerExchangeHandler get() {
                return new BasicServerExchangeHandler<>(requestHandler);
            }

        });
    }

    public void start(final IOEventHandlerFactory handlerFactory) throws IOException {
        execute(handlerFactory);
    }

    public InetSocketAddress start(
            final HttpProcessor httpProcessor,
            final Decorator<AsyncServerExchangeHandler> exchangeHandlerDecorator,
            final H2Config h2Config) throws Exception {
        start(new InternalServerHttp2EventHandlerFactory(
                httpProcessor != null ? httpProcessor : Http2Processors.server(),
                new DefaultAsyncResponseExchangeHandlerFactory(
                        registry,
                        exchangeHandlerDecorator != null ? exchangeHandlerDecorator : new Decorator<AsyncServerExchangeHandler>() {

                            @Override
                            public AsyncServerExchangeHandler decorate(final AsyncServerExchangeHandler handler) {
                                return new BasicAsyncServerExpectationDecorator(handler);
                            }

                        }),
                HttpVersionPolicy.FORCE_HTTP_2,
                h2Config,
                H1Config.DEFAULT,
                CharCodingConfig.DEFAULT,
                sslContext));
        final Future<ListenerEndpoint> future = listen(new InetSocketAddress(0));
        final ListenerEndpoint listener = future.get();
        return (InetSocketAddress) listener.getAddress();
    }

    public InetSocketAddress start(
            final HttpProcessor httpProcessor,
            final Decorator<AsyncServerExchangeHandler> exchangeHandlerDecorator,
            final H1Config h1Config) throws Exception {
        start(new InternalServerHttp2EventHandlerFactory(
                httpProcessor != null ? httpProcessor : HttpProcessors.server(),
                new DefaultAsyncResponseExchangeHandlerFactory(
                        registry,
                        exchangeHandlerDecorator != null ? exchangeHandlerDecorator : new Decorator<AsyncServerExchangeHandler>() {

                    @Override
                    public AsyncServerExchangeHandler decorate(final AsyncServerExchangeHandler handler) {
                        return new BasicAsyncServerExpectationDecorator(handler);
                    }

                }),
                HttpVersionPolicy.FORCE_HTTP_1,
                H2Config.DEFAULT,
                h1Config,
                CharCodingConfig.DEFAULT,
                sslContext));
        final Future<ListenerEndpoint> future = listen(new InetSocketAddress(0));
        final ListenerEndpoint listener = future.get();
        return (InetSocketAddress) listener.getAddress();
    }

    public InetSocketAddress start(final H2Config h2Config) throws Exception {
        return start(null, null, h2Config);
    }

    public InetSocketAddress start(final H1Config h1Config) throws Exception {
        return start(null, null, h1Config);
    }

    public InetSocketAddress start() throws Exception {
        return start(H2Config.DEFAULT);
    }

    

}