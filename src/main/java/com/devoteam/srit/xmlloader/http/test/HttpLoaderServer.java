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
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.impl.io.DefaultClassicHttpResponseFactory;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.DefaultBHttpClientConnection;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.io.HttpRequestHandler;
import org.apache.hc.core5.http.io.HttpServerConnection;
import org.apache.hc.core5.http.impl.io.DefaultBHttpServerConnection;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.protocol.DefaultHttpProcessor;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpCoreContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.impl.io.HttpService;
import org.apache.hc.core5.http.protocol.RequestConnControl;
import org.apache.hc.core5.http.protocol.RequestContent;
import org.apache.hc.core5.http.protocol.RequestHandlerRegistry;
import org.apache.hc.core5.http.protocol.RequestTargetHost;
import org.apache.hc.core5.http.protocol.RequestUserAgent;
import org.apache.hc.core5.http.protocol.ResponseDate;
import org.apache.hc.core5.http.protocol.ResponseServer;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.http.protocol.ResponseContent;
import org.apache.hc.core5.http.protocol.ResponseConnControl;


public class HttpLoaderServer extends Thread
{
	
	
	private Http1Config h1c;
    private  HttpContext context = new BasicHttpContext(null);
    private  DefaultBHttpClientConnection Clientconn = new DefaultBHttpClientConnection(h1c);
    private DefaultBHttpServerConnection Serverconn = new DefaultBHttpServerConnection("H1Config",h1c);
    private  ConnectionReuseStrategy connStrategy = new DefaultConnectionReuseStrategy();
    private int port;
    private String hostname;
    //private MyClass myclass;
    
    public HttpLoaderServer(String hostname,int port)
    {
        this.port=port;
        this.hostname=hostname;
        this.h1c= Http1Config.custom()
    			.build();
        try
        {
            Thread server = new RequestListenerServerThread(port);
            server.setDaemon(false);
            server.start();
        }
        catch(Exception e)
        {/*e.printStackTrace();*/}
    }
    
    
    public void sendRequest(String method, String uri)throws IOException
    {
        // Required protocol interceptors & recommended protocol interceptors
    	HttpRequestInterceptor[] requestInterceptors = {new RequestContent(), new RequestTargetHost(), new RequestConnControl(),new RequestUserAgent()};
        
        context.setProtocolVersion(new ProtocolVersion("HTTP",1,1));
        context.setAttribute("http.connection", Clientconn);
        context.setAttribute("http.target_host", hostname);
        try
        {
            testConnection();
            BasicHttpRequest br = new BasicHttpRequest(method, uri);
            br.setVersion(new ProtocolVersion("HTTP",1,1));
            context.setAttribute(HttpCoreContext.HTTP_REQUEST, br);
            //myclass.doSendRequest(br, Clientconn, context);
        }
        catch(Exception e)
        {}
        
    }
    
    
    public void testConnection()throws IOException
    {
        if (!Clientconn.isOpen())
        {
            HttpHost host = new HttpHost(hostname, port);
            Socket socket = new Socket(host.getHostName(), host.getPort());
            Clientconn.bind(socket);
        }
    }
    
    
    public void receiveResponse(String method, String uri, int numberRequest)throws HttpException, IOException
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
        public RequestListenerServerThread(int port) throws IOException
        {
            this.serversocket = new ServerSocket(port);
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
                    Serverconn.bind(socket);
                    System.out.println("Incoming connection from " + socket.getInetAddress());
                    // Set up the HTTP protocol processor
                    HttpResponseInterceptor[] responseInterceptors = {new ResponseDate(),new ResponseServer(),new ResponseContent(),new ResponseConnControl()};
                    DefaultHttpProcessor httpproc = new DefaultHttpProcessor(responseInterceptors);
                    
                    // Set up request handlers
                    RequestHandlerRegistry<HttpRequestHandler> reqistry = new RequestHandlerRegistry();
                    reqistry.register(hostname,"*", new HttpFileHandler());
                    
                    // Set up the HTTP service
                    HttpService httpService = new HttpService(
                            httpproc,
                            reqistry, new DefaultConnectionReuseStrategy(),
                            new DefaultClassicHttpResponseFactory());
                    
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
                final ClassicHttpResponse response,
                final HttpContext context)throws HttpException, IOException
        {
            receiveRequest(request,context,response);
        }
        
        
        public void receiveRequest( HttpRequest request,
                HttpContext context, ClassicHttpResponse response
                )throws HttpException, IOException
        {
            String method = request.getMethod().toUpperCase();
            String target = request.getPath();
            sendResponse(response,context,request);
        }
        
        
        public void sendResponse( ClassicHttpResponse response,HttpContext context, HttpRequest request)throws HttpException, IOException
        {

            response.setCode(HttpStatus.SC_OK);
            File file = new File("D:/XMLloader/testPileHttp/test/fileTestHttp.txt");
            //FileEntity body = new FileEntity(file, "text/html");
            HttpEntity body = new StringEntity("");
            response.setEntity(body);
        }

		@Override
		public void handle(ClassicHttpRequest request, ClassicHttpResponse response, HttpContext context)
				throws HttpException, IOException {
			receiveRequest(request,context,response);
			
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
                this.conn.close(CloseMode.GRACEFUL);
            }
        }
        
    }
}
