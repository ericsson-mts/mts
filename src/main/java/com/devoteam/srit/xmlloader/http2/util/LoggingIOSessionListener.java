package com.devoteam.srit.xmlloader.http2.util;

import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.reactor.IOSession;
import org.apache.hc.core5.reactor.IOSessionListener;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class LoggingIOSessionListener implements IOSessionListener {

    public final static LoggingIOSessionListener INSTANCE = new LoggingIOSessionListener();

    private final Logger connLog = LoggerFactory.getLogger("org.apache.hc.core5.http.connection");

    private LoggingIOSessionListener() {
    }

    @Override
    public void tlsStarted(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " TLS session started: " + session);
        }
    }

    @Override
    public void tlsInbound(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " TLS inbound: " + session);
        }
    }

    @Override
    public void tlsOutbound(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " TLS outbound: " + session);
        }
    }

    @Override
    public void connected(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " connected: " + session);
        }
    }

    @Override
    public void inputReady(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " input ready: " + session);
        }
    }

    @Override
    public void outputReady(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " output ready: " + session);
        }
    }

    @Override
    public void timeout(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " timeout: " + session);
        }
    }

    @Override
    public void exception(final IOSession session, final Exception ex) {
        if (ex instanceof ConnectionClosedException) {
            return;
        }
        connLog.error(LoggingSupport.getId(session) + " " + ex.getMessage(), ex);
    }

    @Override
    public void disconnected(final IOSession session) {
        if (connLog.isDebugEnabled()) {
            connLog.debug(LoggingSupport.getId(session) + " disconnected: " + session);
        }
    }

}
