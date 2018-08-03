package com.devoteam.srit.xmlloader.http2.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

public class EchoHandler implements AsyncServerExchangeHandler {

    private volatile ByteBuffer buffer;
    private volatile CapacityChannel inputCapacityChannel;
    private volatile DataStreamChannel outputDataChannel;
    private volatile boolean endStream;

    public EchoHandler(final int bufferSize) {
        this.buffer = ByteBuffer.allocate(bufferSize);
    }

    private void ensureCapacity(final int chunk) {
        if (buffer.remaining() < chunk) {
            final ByteBuffer oldBuffer = buffer;
            oldBuffer.flip();
            buffer = ByteBuffer.allocate(oldBuffer.remaining() + (chunk > 2048 ? chunk : 2048));
            buffer.put(oldBuffer);
        }
    }

    @Override
    public void handleRequest(
            final HttpRequest request,
            final EntityDetails entityDetails,
            final ResponseChannel responseChannel,
            final HttpContext context) throws HttpException, IOException {
        final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_OK);
        responseChannel.sendResponse(response, entityDetails);
    }

    @Override
    public int consume(final ByteBuffer src) throws IOException {
        if (buffer.position() == 0) {
            if (outputDataChannel != null) {
                outputDataChannel.write(src);
            }
        }
        if (src.hasRemaining()) {
            ensureCapacity(src.remaining());
            buffer.put(src);
            if (outputDataChannel != null) {
                outputDataChannel.requestOutput();
            }
        }
        return buffer.remaining();
    }

    @Override
    public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
        if (buffer.hasRemaining()) {
            capacityChannel.update(buffer.remaining());
            inputCapacityChannel = null;
        } else {
            inputCapacityChannel = capacityChannel;
        }
    }

    @Override
    public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
        endStream = true;
        if (buffer.position() == 0) {
            if (outputDataChannel != null) {
                outputDataChannel.endStream();
            }
        } else {
            if (outputDataChannel != null) {
                outputDataChannel.requestOutput();
            }
        }
    }

    @Override
    public int available() {
        return buffer.position();
    }

    @Override
    public void produce(final DataStreamChannel channel) throws IOException {
        outputDataChannel = channel;
        buffer.flip();
        if (buffer.hasRemaining()) {
            channel.write(buffer);
        }
        buffer.compact();
        if (buffer.position() == 0 && endStream) {
            channel.endStream();
        }
        final CapacityChannel capacityChannel = inputCapacityChannel;
        if (capacityChannel != null && buffer.hasRemaining()) {
            capacityChannel.update(buffer.remaining());
        }
    }

    @Override
    public void failed(final Exception cause) {
    }

    @Override
    public void releaseResources() {
    }

}
