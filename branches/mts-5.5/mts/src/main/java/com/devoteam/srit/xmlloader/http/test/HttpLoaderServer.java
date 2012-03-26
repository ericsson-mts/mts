/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.http.test;
import java.net.Socket;
import java.io.*;
import java.net.*;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.HttpRequest;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpRequestHandler;


public class HttpLoaderServer extends Thread
{
    
    private HttpParams params = new BasicHttpParams();
    
    private  HttpContext context = new BasicHttpContext(null);
    private  BasicHttpProcessor httpproc = new BasicHttpProcessor();
    private  DefaultHttpClientConnection Clientconn = new DefaultHttpClientConnection();
    private DefaultHttpServerConnection Serverconn = new DefaultHttpServerConnection();
    private  ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();
    private int port;
    private String hostname;
    //private MyClass myclass;
    
    public HttpLoaderServer(String hostname,int port)
    {
        this.port=port;
        this.hostname=hostname;
        try
        {
            Thread server = new RequestListenerServerThread(port);
            server.setDaemon(false);
            server.start();
        }
        catch(Exception e)
        {/*e.printStackTrace();*/}
    }
    
    
    public void sendRequest(String method, String uri,HttpParams params)throws IOException
    {
        // Required protocol interceptors
        httpproc.addInterceptor(new RequestContent());
        httpproc.addInterceptor(new RequestTargetHost());
        // Recommended protocol interceptors
        httpproc.addInterceptor(new RequestConnControl());
        httpproc.addInterceptor(new RequestUserAgent());
        
        context.setAttribute(ExecutionContext.HTTP_CONNECTION, Clientconn);
        context.setAttribute(ExecutionContext.HTTP_TARGET_HOST, hostname);
        try
        {
            testConnection();
            BasicHttpRequest br = new BasicHttpRequest(method, uri);
            context.setAttribute(ExecutionContext.HTTP_REQUEST, br);
            br.setParams(params);
            //myclass.doSendRequest(br, Clientconn, context);
        }
        catch(Exception e)
        {}
        
    }
    
    
    public void testConnection()throws IOException
    {
        HttpParams params = new BasicHttpParams();
        if (!Clientconn.isOpen())
        {
            HttpHost host = new HttpHost(hostname, port);
            Socket socket = new Socket(host.getHostName(), host.getPort());
            Clientconn.bind(socket, params);
        }
    }
    
    
    public void receiveResponse(String method,String uri, HttpParams params,int numberRequest)throws HttpException, IOException
    {
        HttpResponse response;
        try
        {
            for(int i=0; i < numberRequest; i++)
            {
                BasicHttpRequest br = new BasicHttpRequest(method, uri+i);
                //response=myclass.doReceiveResponse(br,Clientconn,context);
                //System.out.println(EntityUtils.toString(response.getEntity()));
               // response.getEntity().consumeContent();
                //System.out.println(response.getStatusLine());
            }
        }
        catch(Exception e)
        {}
    }
    
    public class RequestListenerServerThread extends Thread
    {
        
        private final ServerSocket serversocket;
        private final HttpParams params;
        public RequestListenerServerThread(int port) throws IOException
        {
            this.serversocket = new ServerSocket(port);
            this.params = new BasicHttpParams();
        }
        
        public void run()
        {
            System.out.println("Listening on port " + serversocket.getLocalPort());
            while (!Thread.interrupted()&& !Serverconn.isOpen())
            {
                try
                {
                    // Set up HTTP connection
                    Socket socket = this.serversocket.accept();
                    Serverconn.bind(socket, this.params);
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    // Set up the HTTP protocol processor
                    BasicHttpProcessor httpproc = new BasicHttpProcessor();
                    httpproc.addInterceptor(new ResponseDate());
                    httpproc.addInterceptor(new ResponseServer());
                    httpproc.addInterceptor(new ResponseContent());
                    httpproc.addInterceptor(new ResponseConnControl());
                    
                    // Set up request handlers
                    HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
                    reqistry.register("*", new HttpFileHandler());
                    
                    // Set up the HTTP service
                    HttpService httpService = new HttpService(
                            httpproc,
                            new DefaultConnectionReuseStrategy(),
                            new DefaultHttpResponseFactory());
                    httpService.setParams(this.params);
                    httpService.setHandlerResolver(reqistry);
                    
                    // Start Server thread
                    Thread t = new ServerThread(httpService, Serverconn);
                    t.setDaemon(true);
                    t.start();
                }
                catch (InterruptedIOException ex)
                {
                    break;
                }
                catch (IOException e)
                {
                    System.err.println("I/O error initialising connection thread: "
                            + e.getMessage());
                    break;
                }
            }
        }
    }
    
    public class HttpFileHandler implements HttpRequestHandler
    {
        
        public HttpFileHandler()
        {
            super();
        }
        
        public void handle(final HttpRequest request,
                final HttpResponse response,
                final HttpContext context)throws HttpException, IOException
        {
            receiveRequest(request,context,response);
        }
        
        
        public void receiveRequest( HttpRequest request,
                HttpContext context, HttpResponse response
                )throws HttpException, IOException
        {
            String method = request.getRequestLine().getMethod().toUpperCase();
            String target = request.getRequestLine().getUri();
            sendResponse(response,context,request);
        }
        
        
        public void sendResponse( HttpResponse response,HttpContext context, HttpRequest request)throws HttpException, IOException
        {
            
            response.setStatusCode(HttpStatus.SC_OK);
            File file = new File("D:/XMLloader/testPileHttp/test/fileTestHttp.txt");
            //FileEntity body = new FileEntity(file, "text/html");
            HttpEntity body = new StringEntity("");
            response.setEntity(body);
        }
    }
    
    
    public class ServerThread extends Thread
    {
        
        private final HttpService httpservice;
        private final HttpServerConnection conn;
        public ServerThread(
                final HttpService httpservice,
                final HttpServerConnection conn)
        {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }
        
        
        public void run()
        {
            HttpContext context = new BasicHttpContext(null);
            try
            {
                while (!Thread.interrupted() && conn.isOpen())
                {
                    this.httpservice.handleRequest(conn, context);
                }
            }
            catch (ConnectionClosedException ex)
            {
            }
            catch (IOException ex)
            {
            }
            catch (HttpException ex)
            {
            }
            finally
            {
                try
                {
                    this.conn.shutdown();
                }
                catch (IOException ignore)
                {}
            }
        }
        
    }
}
