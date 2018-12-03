package com.devoteam.srit.xmlloader.http2;
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.io.IOException;
import java.net.URI;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.RequestChannel;
import org.apache.hc.core5.http.protocol.HttpContext;

import com.devoteam.srit.xmlloader.core.protocol.Msg;

/**
*
* @author qqin
*/
public class Http2RequestProducer implements AsyncRequestProducer {

    private final HttpRequest request;
    private final AsyncEntityProducer dataProducer;
    private final MsgHttp2 msgHttp2;

    public Http2RequestProducer(final HttpRequest request, final AsyncEntityProducer dataProducer, final Msg msgHttp2) {
        this.request = request;
        this.dataProducer = dataProducer;
        this.msgHttp2 = (MsgHttp2) msgHttp2;
    }

    public Http2RequestProducer(final String method, final HttpHost host, final String path, final AsyncEntityProducer dataProducer, final Msg msgHttp2) {
        this(new BasicHttpRequest(method, host, path), dataProducer, msgHttp2);
    }

    public Http2RequestProducer(final String method, final HttpHost host, final String path, final Msg msgHttp2) {
        this(method, host, path, null, msgHttp2);
    }

    public Http2RequestProducer(final String method, final URI requestUri, final AsyncEntityProducer dataProducer, final Msg msgHttp2) {
        this(new BasicHttpRequest(method, requestUri), dataProducer, msgHttp2);
    }

    public Http2RequestProducer(final String method, final URI requestUri, final Msg msgHttp2) {
        this(method, requestUri, null, msgHttp2);
    }

    @Override
    public void sendRequest(final RequestChannel requestChannel, final HttpContext httpContext) throws HttpException, IOException {
        requestChannel.sendRequest(request, dataProducer, httpContext);
    }

    @Override
    public int available() {
        return dataProducer != null ? dataProducer.available() : 0;
    }

    @Override
    public void produce(final DataStreamChannel channel) throws IOException {
        if (dataProducer != null) {
            dataProducer.produce(channel);
        }
    }

    @Override
    public boolean isRepeatable() {
        return dataProducer == null || dataProducer.isRepeatable();
    }

    @Override
    public void failed(final Exception cause) {
        try {
            if (dataProducer != null) {
                dataProducer.failed(cause);
            }
        } finally {
            releaseResources();
        }
    }

    @Override
    public void releaseResources() {
        if (dataProducer != null) {
            dataProducer.releaseResources();
        }
    }
}
