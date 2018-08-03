package com.devoteam.srit.xmlloader.http2;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.apache.hc.core5.function.Callback;
import org.apache.hc.core5.reactor.DefaultListeningIOReactor;
import org.apache.hc.core5.reactor.IOEventHandlerFactory;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.ListenerEndpoint;

import com.devoteam.srit.xmlloader.http2.util.LoggingIOSessionDecorator;
import com.devoteam.srit.xmlloader.http2.util.LoggingIOSessionListener;

public class AsyncServer extends IOReactorExecutor<DefaultListeningIOReactor> {

    public AsyncServer(final IOReactorConfig ioReactorConfig) {
        super(ioReactorConfig, null);
    }

    @Override
    DefaultListeningIOReactor createIOReactor(
            final IOEventHandlerFactory ioEventHandlerFactory,
            final IOReactorConfig ioReactorConfig,
            final ThreadFactory threadFactory,
            final Callback<IOSession> sessionShutdownCallback) throws IOException {
        return new DefaultListeningIOReactor(ioEventHandlerFactory, ioReactorConfig, threadFactory, threadFactory,
                LoggingIOSessionDecorator.INSTANCE, LoggingIOSessionListener.INSTANCE, sessionShutdownCallback);
    }

    public Future<ListenerEndpoint> listen(final InetSocketAddress address) {
        return reactor().listen(address, null);
    }

    public Set<ListenerEndpoint> getEndpoints() {
        return reactor().getEndpoints();
    }

}
