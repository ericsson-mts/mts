package com.devoteam.srit.xmlloader.http2.util;

import java.io.IOException;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.BasicResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler.ResponseTrigger;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicServerExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;


import java.io.IOException;

import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncRequestConsumer;
import org.apache.hc.core5.http.nio.AsyncServerRequestHandler;
import org.apache.hc.core5.http.nio.BasicRequestConsumer;
import org.apache.hc.core5.http.nio.BasicResponseProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.nio.support.BasicServerExchangeHandler;
import org.apache.hc.core5.http.protocol.HttpContext;

public class MultiLineResponseHandler extends BasicServerExchangeHandler<Message<HttpRequest, String>> {

    public MultiLineResponseHandler(final String message, final int count) {
        super(new AsyncServerRequestHandler<Message<HttpRequest, String>>() {

                  @Override
                  public AsyncRequestConsumer<Message<HttpRequest, String>> prepare(
                          final HttpRequest request,
                          final HttpContext context) throws HttpException {
                      return new BasicRequestConsumer<>(new StringAsyncEntityConsumer());
                  }

                  @Override
                  public void handle(
                          final Message<HttpRequest, String> requestMessage,
                          final ResponseTrigger responseTrigger,
                          final HttpContext context) throws HttpException, IOException {
                      final HttpResponse response = new BasicHttpResponse(HttpStatus.SC_OK);
                      responseTrigger.submitResponse(new BasicResponseProducer(
                              response,
                              new MultiLineEntityProducer(message, count)));
                  }
              }
        );
    }

}

