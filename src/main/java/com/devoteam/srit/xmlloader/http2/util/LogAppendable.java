package com.devoteam.srit.xmlloader.http2.util;

import java.io.IOException;

import org.slf4j.Logger;

class LogAppendable implements Appendable {

    private final Logger log;
    private final String prefix;
    private final StringBuilder buffer;

    LogAppendable(final Logger log, final String prefix) {
        this.log = log;
        this.prefix = prefix;
        this.buffer = new StringBuilder();
    }


    @Override
    public Appendable append(final CharSequence text) throws IOException {
        return append(text, 0, text.length());
    }

    @Override
    public Appendable append(final CharSequence text, final int start, final int end) throws IOException {
        for (int i = start; i < end; i++) {
            append(text.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(final char ch) throws IOException {
        if (ch == '\n') {
            log.debug(prefix + " " + buffer.toString());
            buffer.setLength(0);
        } else if (ch != '\r') {
            buffer.append(ch);
        }
        return this;
    }

    public void flush() {
        if (buffer.length() > 0) {
            log.debug(prefix + " " + buffer.toString());
            buffer.setLength(0);
        }
    }

}
