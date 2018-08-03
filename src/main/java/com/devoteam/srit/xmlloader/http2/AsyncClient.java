package com.devoteam.srit.xmlloader.http2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.reactor.ConnectionInitiator;
import org.apache.hc.core5.reactor.DefaultConnectingIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

import com.devoteam.srit.xmlloader.http2.util.LoggingIOSessionDecorator;
import com.devoteam.srit.xmlloader.http2.util.LoggingIOSessionListener;

import java.util.concurrent.ThreadFactory;

import org.apache.hc.core5.http.URIScheme;

public class AsyncClient extends IOReactorExecutor<DefaultConnectingIOReactor> implements ConnectionInitiator {

    public AsyncClient(final IOReactorConfig ioReactorConfig) {
        super(ioReactorConfig, null);
    }

    @Override
    DefaultConnectingIOReactor createIOReactor(
            final IOEventHandlerFactory ioEventHandlerFactory,
            final IOReactorConfig ioReactorConfig,
            final ThreadFactory threadFactory,
            final Callback<IOSession> sessionShutdownCallback) throws IOException {
        return new DefaultConnectingIOReactor(ioEventHandlerFactory, ioReactorConfig, threadFactory,
                LoggingIOSessionDecorator.INSTANCE, LoggingIOSessionListener.INSTANCE, sessionShutdownCallback);
    }

    private InetSocketAddress toSocketAddress(final HttpHost host) {
        int port = host.getPort();
        if (port < 0) {
            final String scheme = host.getSchemeName();
            if (URIScheme.HTTP.same(scheme)) {
                port = 80;
            } else if (URIScheme.HTTPS.same(scheme)) {
                port = 443;
            }
        }
        final String hostName = host.getHostName();
        return new InetSocketAddress(hostName, port);
    }

    public Future<IOSession> requestSession(final HttpHost host, final TimeValue timeout, final FutureCallback<IOSession> callback) {
        Args.notNull(host, "Host");
        return reactor().connect(host, toSocketAddress(host), null, timeout, null, callback);
    }

    @Override
    public Future<IOSession> connect(
            final NamedEndpoint remoteEndpoint,
            final SocketAddress remoteAddress,
            final SocketAddress localAddress,
            final TimeValue timeout,
            final Object attachment,
            final FutureCallback<IOSession> callback) {
        return reactor().connect(remoteEndpoint, remoteAddress, localAddress, timeout, attachment, callback);
    }

}
