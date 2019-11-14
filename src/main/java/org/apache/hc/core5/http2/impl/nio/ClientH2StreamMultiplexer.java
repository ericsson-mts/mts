package org.apache.hc.core5.http2.impl.nio;

import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.impl.BasicHttpConnectionMetrics;
import org.apache.hc.core5.http.nio.AsyncClientExchangeHandler;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.nio.command.ExecutableCommand;
import org.apache.hc.core5.http.nio.command.RequestExecutionCommand;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http2.H2ConnectionException;
import org.apache.hc.core5.http2.H2Error;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.frame.DefaultFrameFactory;
import org.apache.hc.core5.http2.frame.FrameFactory;
import org.apache.hc.core5.http2.frame.StreamIdGenerator;
import org.apache.hc.core5.reactor.ProtocolIOSession;

import java.io.IOException;

@Internal
public class ClientH2StreamMultiplexer extends AbstractH2StreamMultiplexer {

    private final HandlerFactory<AsyncPushConsumer> pushHandlerFactory;

    public ClientH2StreamMultiplexer(
            final ProtocolIOSession ioSession,
            final FrameFactory frameFactory,
            final HttpProcessor httpProcessor,
            final HandlerFactory<AsyncPushConsumer> pushHandlerFactory,
            final H2Config h2Config,
            final CharCodingConfig charCodingConfig,
            final H2StreamListener streamListener,
            final StreamIdGenerator streamIdGenerator) {
        super(ioSession, frameFactory, streamIdGenerator, httpProcessor, charCodingConfig, h2Config, streamListener);
        this.pushHandlerFactory = pushHandlerFactory;
    }

    @Override
    void acceptHeaderFrame() throws H2ConnectionException {
        throw new H2ConnectionException(H2Error.PROTOCOL_ERROR, "Illegal HEADERS frame");
    }

    @Override
    void acceptPushFrame() throws H2ConnectionException {
    }

    @Override
    void acceptPushRequest() throws H2ConnectionException {
        throw new H2ConnectionException(H2Error.INTERNAL_ERROR, "Illegal attempt to push a response");
    }

    @Override
    H2StreamHandler createLocallyInitiatedStream(
            final ExecutableCommand command,
            final H2StreamChannel channel,
            final HttpProcessor httpProcessor,
            final BasicHttpConnectionMetrics connMetrics) throws IOException {
        if (command instanceof RequestExecutionCommand) {
            final RequestExecutionCommand executionCommand = (RequestExecutionCommand) command;
            final AsyncClientExchangeHandler exchangeHandler = executionCommand.getExchangeHandler();
            final HandlerFactory<AsyncPushConsumer> pushHandlerFactory = executionCommand.getPushHandlerFactory();
            final HttpCoreContext context = HttpCoreContext.adapt(executionCommand.getContext());
            context.setAttribute(HttpCoreContext.SSL_SESSION, getSSLSession());
            context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, getEndpointDetails());
            return new ClientH2StreamHandler(channel, httpProcessor, connMetrics, exchangeHandler,
                    pushHandlerFactory != null ? pushHandlerFactory : this.pushHandlerFactory,
                    context);
        }
        throw new H2ConnectionException(H2Error.INTERNAL_ERROR, "Unexpected executable command");
    }

    @Override
    H2StreamHandler createRemotelyInitiatedStream(
            final H2StreamChannel channel,
            final HttpProcessor httpProcessor,
            final BasicHttpConnectionMetrics connMetrics,
            final HandlerFactory<AsyncPushConsumer> pushHandlerFactory) throws IOException {
        final HttpCoreContext context = HttpCoreContext.create();
        context.setAttribute(HttpCoreContext.SSL_SESSION, getSSLSession());
        context.setAttribute(HttpCoreContext.CONNECTION_ENDPOINT, getEndpointDetails());
        return new ClientPushH2StreamHandler(channel, httpProcessor, connMetrics,
                pushHandlerFactory != null ? pushHandlerFactory : this.pushHandlerFactory,
                context);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("[");
        appendState(buf);
        buf.append("]");
        return buf.toString();
    }

}

