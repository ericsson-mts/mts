package com.devoteam.srit.xmlloader.http2.util;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncEntityConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.support.AbstractServerExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * @since 5.0
 */
public abstract class MessageExchangeHandler<T> extends AbstractServerExchangeHandler<Message<HttpRequest, T>> {

    private final AsyncRequestConsumer<Message<HttpRequest, T>> requestConsumer;

    public MessageExchangeHandler(final AsyncRequestConsumer<Message<HttpRequest, T>> requestConsumer) {
        super();
        this.requestConsumer = requestConsumer;
    }

    public MessageExchangeHandler(final AsyncEntityConsumer<T> entityConsumer) {
        this(new BasicRequestConsumer<>(entityConsumer));
    }

    @Override
    protected AsyncRequestConsumer<Message<HttpRequest, T>> supplyConsumer(
            final HttpRequest request,
            final HttpContext context) throws HttpException {
        return requestConsumer;
    }

}
