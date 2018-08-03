package com.devoteam.srit.xmlloader.http2.util;

import org.apache.hc.core5.function.Decorator;
import org.apache.hc.core5.reactor.IOSession;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LoggingIOSessionDecorator implements Decorator<IOSession> {

    public final static LoggingIOSessionDecorator INSTANCE = new LoggingIOSessionDecorator();

    private final Logger wireLog = LoggerFactory.getLogger("org.apache.hc.core5.http.wire");

    private LoggingIOSessionDecorator() {
    }

    @Override
    public IOSession decorate(final IOSession ioSession) {
        final Logger sessionLog = LoggerFactory.getLogger(ioSession.getClass());
        return new LoggingIOSession(ioSession, sessionLog, wireLog);
    }
}
