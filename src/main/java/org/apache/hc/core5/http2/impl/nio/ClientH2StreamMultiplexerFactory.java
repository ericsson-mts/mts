package org.apache.hc.core5.http2.impl.nio;

import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.Internal;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.http2.frame.DefaultFrameFactory;
import org.apache.hc.core5.http2.frame.StreamIdGenerator;
import org.apache.hc.core5.reactor.ProtocolIOSession;
import org.apache.hc.core5.util.Args;

@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
@Internal
public final class ClientH2StreamMultiplexerFactory {

    private final HttpProcessor httpProcessor;
    private final HandlerFactory<AsyncPushConsumer> pushHandlerFactory;
    private final H2Config h2Config;
    private final CharCodingConfig charCodingConfig;
    private final H2StreamListener streamListener;
    private final StreamIdGenerator streamIdGenerator;

    public ClientH2StreamMultiplexerFactory(
            final HttpProcessor httpProcessor,
            final HandlerFactory<AsyncPushConsumer> pushHandlerFactory,
            final H2Config h2Config,
            final CharCodingConfig charCodingConfig,
            final H2StreamListener streamListener,
            final StreamIdGenerator streamIdGenerator) {
        this.httpProcessor = Args.notNull(httpProcessor, "HTTP processor");
        this.pushHandlerFactory = pushHandlerFactory;
        this.h2Config = h2Config != null ? h2Config : H2Config.DEFAULT;
        this.charCodingConfig = charCodingConfig != null ? charCodingConfig : CharCodingConfig.DEFAULT;
        this.streamListener = streamListener;
        this.streamIdGenerator = streamIdGenerator;
    }

    public ClientH2StreamMultiplexer create(final ProtocolIOSession ioSession) {
        return new ClientH2StreamMultiplexer(ioSession, DefaultFrameFactory.INSTANCE, httpProcessor,
                pushHandlerFactory, h2Config, charCodingConfig, streamListener, streamIdGenerator);
    }

}
