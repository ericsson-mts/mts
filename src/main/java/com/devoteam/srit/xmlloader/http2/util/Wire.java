package com.devoteam.srit.xmlloader.http2.util;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
public class Wire {

    private final Logger log;
    private final String id;

    public Wire(final Logger log, final String id) {
        super();
        this.log = log;
        this.id = id;
    }

    private void wire(final String header, final byte[] b, final int pos, final int off) {
        final StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < off; i++) {
            final int ch = b[pos + i];
            if (ch == 13) {
                buffer.append("[\\r]");
            } else if (ch == 10) {
                    buffer.append("[\\n]\"");
                    buffer.insert(0, "\"");
                    buffer.insert(0, header);
                    this.log.debug(this.id + " " + buffer.toString());
                    buffer.setLength(0);
            } else if ((ch < 32) || (ch > 127)) {
                buffer.append("[0x");
                buffer.append(Integer.toHexString(ch));
                buffer.append("]");
            } else {
                buffer.append((char) ch);
            }
        }
        if (buffer.length() > 0) {
            buffer.append('\"');
            buffer.insert(0, '\"');
            buffer.insert(0, header);
            this.log.debug(this.id + " " + buffer.toString());
        }
    }


    public boolean isEnabled() {
        return this.log.isDebugEnabled();
    }

    public void output(final byte[] b, final int pos, final int off) {
        wire(">> ", b, pos, off);
    }

    public void input(final byte[] b, final int pos, final int off) {
        wire("<< ", b, pos, off);
    }

    public void output(final byte[] b) {
        output(b, 0, b.length);
    }

    public void input(final byte[] b) {
        input(b, 0, b.length);
    }

    public void output(final int b) {
        output(new byte[] {(byte) b});
    }

    public void input(final int b) {
        input(new byte[] {(byte) b});
    }

    public void output(final ByteBuffer b) {
        if (b.hasArray()) {
            output(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            final byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            output(tmp);
        }
    }

    public void input(final ByteBuffer b) {
        if (b.hasArray()) {
            input(b.array(), b.arrayOffset() + b.position(), b.remaining());
        } else {
            final byte[] tmp = new byte[b.remaining()];
            b.get(tmp);
            input(tmp);
        }
    }

}
