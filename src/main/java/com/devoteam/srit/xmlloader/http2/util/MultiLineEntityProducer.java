package com.devoteam.srit.xmlloader.http2.util;


import java.io.IOException;
import java.nio.CharBuffer;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.StreamChannel;
import org.apache.hc.core5.http.nio.entity.AbstractCharAsyncEntityProducer;

public class MultiLineEntityProducer extends AbstractCharAsyncEntityProducer {

    private final String text;
    private final int total;
    private final CharBuffer charbuf;

    private int count;

    public MultiLineEntityProducer(final String text, final int total) {
        super(1024, -1, ContentType.TEXT_PLAIN);
        this.text = text;
        this.total = total;
        this.charbuf = CharBuffer.allocate(4096);
        this.count = 0;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void produceData(final StreamChannel<CharBuffer> channel) throws IOException {
        while (charbuf.remaining() > text.length() + 2 && count < total) {
            charbuf.put(text + "\r\n");
            count++;
        }
        if (charbuf.position() > 0) {
            charbuf.flip();
            channel.write(charbuf);
            charbuf.compact();
        }
        if (count >= total && charbuf.position() == 0) {
            channel.endStream();
        }
    }

    @Override
    public void failed(final Exception cause) {
    }

    @Override
    public void releaseResourcesInternal() {
        count = 0;
        charbuf.clear();
    }

}
