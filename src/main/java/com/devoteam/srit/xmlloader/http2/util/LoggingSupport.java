package com.devoteam.srit.xmlloader.http2.util;

import org.apache.hc.core5.util.Identifiable;

public final class LoggingSupport {

    public static String getId(final Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Identifiable) {
            return ((Identifiable) object).getId();
        } else {
            return object.getClass().getSimpleName() + "-" + Integer.toHexString(System.identityHashCode(object));
        }
    }

}
