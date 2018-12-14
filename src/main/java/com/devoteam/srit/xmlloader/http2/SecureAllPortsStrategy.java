package com.devoteam.srit.xmlloader.http2;

import java.net.SocketAddress;

import org.apache.hc.core5.http.nio.ssl.SecurePortStrategy;

public class SecureAllPortsStrategy implements SecurePortStrategy {

    public static final SecureAllPortsStrategy INSTANCE = new SecureAllPortsStrategy();

    @Override
    public boolean isSecure(final SocketAddress localAddress) {
        return true;
    }

}