package com.devoteam.srit.xmlloader.http2;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.hc.core5.function.Supplier;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.URIScheme;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.http.nio.AsyncResponseProducer;
import org.apache.hc.core5.http.nio.AsyncServerExchangeHandler;
import org.apache.hc.core5.http.nio.BasicRequestProducer;
import org.apache.hc.core5.http.nio.BasicResponseConsumer;
import org.apache.hc.core5.http.nio.BasicResponseProducer;
import org.apache.hc.core5.http.nio.CapacityChannel;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ResponseChannel;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;
import org.apache.hc.core5.http.nio.entity.StringAsyncEntityConsumer;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http2.config.H2Config;
import org.apache.hc.core5.reactor.ExceptionEvent;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.filesystem.LocalFSInterface;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.http2.util.ClientSessionEndpoint;


public class Http2Channel{

    protected Http2Client client;
    
    protected Http2Server server;
    
    private InetSocketAddress serverEndpoint;
    
    private  ClientSessionEndpoint streamEndpoint;
    
    public static SSLContext context;
    
    protected final URIScheme scheme;
    
    private static final TimeValue TIMEOUT = TimeValue.ofSeconds(30);
    
    final Queue<Future<Message<HttpResponse, String>>> queue = new LinkedList<>();
    
    public Http2Channel(final URIScheme scheme) throws IOException, Exception {
        this.scheme=scheme;
    }

    /**
     *  Create a server and a client and connect them.
     */
    public void open(H2Config h2config) throws Exception {
        //Starting up test client
        client = new Http2Client(IOReactorConfig.DEFAULT,
                scheme == URIScheme.HTTPS ? context : null);

        //Starting up test server
        server = new Http2Server(IOReactorConfig.DEFAULT,
                scheme == URIScheme.HTTPS ? context : null);
        
        
    	serverEndpoint = server.start(h2config);
        client.start(h2config);
        final Future<ClientSessionEndpoint> connectFuture = client.connect(
                "localhost", serverEndpoint.getPort(), TIMEOUT);
        streamEndpoint = connectFuture.get();
    }

    public void close() throws Exception {
        //Shutting down test client
        if (client != null) {
            client.shutdown(TimeValue.ofSeconds(5));
            final List<ExceptionEvent> exceptionLog = client.getExceptionLog();
            if (!exceptionLog.isEmpty()) {
                for (final ExceptionEvent event: exceptionLog) {
                    final Throwable cause = event.getCause();
                }
            }
        }
        //Shutting down test server
        if (server != null) {
            try {
                server.shutdown(TimeValue.ofSeconds(5));
                final List<ExceptionEvent> exceptionLog = server.getExceptionLog();
                server = null;
                if (!exceptionLog.isEmpty()) {
                    for (final ExceptionEvent event: exceptionLog) {
                        final Throwable cause = event.getCause();
                    }
                }
            } catch (final Exception ignore) {
            }
        }
    }
   
    public void initSSL() throws Exception {
    	
    	TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager(){

                    public java.security.cert.X509Certificate[] getAcceptedIssuers(){
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType){
                        //No need to implement.
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType){
                        //No need to implement.
                    }
                }
            };

        	SingletonFSInterface.setInstance(new LocalFSInterface());
        	
        	
            String certificatePath = Config.getConfigByName("tls.properties").getString("cert.SERVER.DIRECTORY");
            String certificateAlgorithm = Config.getConfigByName("tls.properties").getString("cert.ALGORITHM");
            String certificatePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEY_PASSWORD");
            String keystorePassword = Config.getConfigByName("tls.properties").getString("cert.SERVER.KEYSTORE_PASSWORD");
            String certificateSSLVersion = Config.getConfigByName("tls.properties").getString("cert.SSL_VERSION");

            char[] certificatePasswordArray;
            char[] keystorePasswordArray;
            //
            // If password is an empty string (allowed) or not defined (allowed), do not use a password
            //
            if (null == certificatePassword || certificatePassword.length() == 0)
            {
                certificatePasswordArray = null;
            }
            else
            {
                certificatePasswordArray = certificatePassword.toCharArray();
            }
            if (null == keystorePassword || keystorePassword.length() == 0)
            {
                keystorePasswordArray = null;
            }
            else
            {
                keystorePasswordArray = keystorePassword.toCharArray();
            }

            KeyStore keyStore = KeyStore.getInstance(certificateAlgorithm);
            keyStore.load(new FileInputStream(certificatePath), keystorePasswordArray);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, certificatePasswordArray);

            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            
            Http2Channel.context = SSLContext.getInstance(certificateSSLVersion);
            Http2Channel.context.init(keyManagers, trustAllCerts, null);
    }
    
    private URI createRequestURI(final InetSocketAddress serverEndpoint, final String path) {
        try {
            return new URI(scheme.toString(), null, "localhost", serverEndpoint.getPort(), path, null, null);
        } catch (final URISyntaxException e) {
            throw new IllegalStateException();
        }
    }
    
    public String getTransport() 
    {   
    	return scheme.toString();
    }

    /**
     *  The channel will send
     */
    public void register(String path, HttpResponse response) {
    	server.register(path, new Supplier<AsyncServerExchangeHandler>() {

            @Override
            public AsyncServerExchangeHandler get() {
                return new AsyncServerExchangeHandler() {

                    private final AtomicReference<AsyncResponseProducer> responseProducer = new AtomicReference<>(null);
                    
                    @Override
                    public void updateCapacity(final CapacityChannel capacityChannel) throws IOException {
                        capacityChannel.update(Integer.MAX_VALUE);
                    }

                    @Override
                    public int consume(final ByteBuffer src) throws IOException {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    public void streamEnd(final List<? extends Header> trailers) throws HttpException, IOException {
                    }

                    @Override
                    /**
                     * Method used to send the response, verify if there is an header in order to send different responses.
                     */
                    public void handleRequest(
                            final HttpRequest request,
                            final EntityDetails entityDetails,
                            final ResponseChannel responseChannel,
                            final HttpContext context) throws HttpException, IOException 
                    {
                    	System.out.println("Requete reçue   : " + request.toString());
                    	final AsyncResponseProducer producer;
                        final Header h = request.getFirstHeader("password");
                        if (h != null && "secret".equals(h.getValue())) 
                        {
                            producer = new BasicResponseProducer(response, response.getReasonPhrase());
                            System.out.println("Reponse envoyée : " + response.getCode()+ " OK " + response.getReasonPhrase());
                        } 
                        else 
                        {
                            producer = new BasicResponseProducer(HttpStatus.SC_UNAUTHORIZED, "You shall not pass");
                            System.out.println("Reponse envoyée : " + String.valueOf(HttpStatus.SC_UNAUTHORIZED) + " UNAUTHORIZED " + "You shall not pass");
                        }
                        responseProducer.set(producer);

                        
                        producer.sendResponse(responseChannel);
                    }

                    @Override
                    public int available() 
                    {
                        final AsyncResponseProducer producer = this.responseProducer.get();
                        return producer.available();
                    }

                    /**
                     * Allow us to know on which stream is send the message, this info is needed to replace the transaction ID
                     */
                    @Override
                    public void produce(final DataStreamChannel channel) throws IOException 
                    {
                    	if(channel.toString().startsWith("[id")) {
                    		String[] strings = channel.toString().split(",");
                    		System.out.println("Messages echangés sur le stream " + strings[0].substring(1));
                    	}
                        final AsyncResponseProducer producer = this.responseProducer.get();
                        producer.produce(channel);
                    }
                    
                    @Override
                    public void failed(final Exception cause) {
                    }

                    @Override
                    public void releaseResources() {
                    }
                };
            }

        });
    }

    /**
     *  Add the request to the LinkedList
     */   
    public void sendRequest(HttpRequest requete) throws Exception 
    {
        //TODO : Need to find a way to get beck this entity
        final Future<Message<HttpResponse, String>> future = streamEndpoint.execute(
                new BasicRequestProducer(requete, new BasicAsyncEntityProducer("Hello")),
                new BasicResponseConsumer<>(new StringAsyncEntityConsumer()), null);
        queue.add(future);
    	System.out.println("Requete envoyée : " + requete.toString());
    }

    /**
     *  This method send all messages stored in the LinkedList
     */
    public void send() throws InterruptedException, ExecutionException, TimeoutException 
    {
    	while (!queue.isEmpty()) 
    	{
            final Future<Message<HttpResponse, String>> future = queue.remove();
            final Message<HttpResponse, String> result = future.get(TIMEOUT.getDuration(), TIMEOUT.getTimeUnit());
            System.out.println("Reponse reçue   : " + result.getHead().getCode() + " " + result.getHead().getReasonPhrase() + " " + result.getBody());
    	}
    }
 
    //Need to create a new method sending push message. 
    
    public static void main(String [ ] args) throws Exception
    {
    	Http2Channel h2C = new Http2Channel(URIScheme.HTTPS);
    	h2C.initSSL();
    	H2Config h2Config = H2Config.DEFAULT;
    	h2C.open(h2Config);
    	final HttpRequest  requete1 = new BasicHttpRequest("GET" , h2C.createRequestURI(h2C.serverEndpoint, "/essai1"));
    	final HttpRequest  requete2 = new BasicHttpRequest("POST", h2C.createRequestURI(h2C.serverEndpoint, "/essai2"));
    	final HttpResponse response1 = new BasicHttpResponse(HttpStatus.SC_OK, "essai1");
    	final HttpResponse response2 = new BasicHttpResponse(HttpStatus.SC_OK, "essai2");
    	h2C.register(requete1.getPath(), response1);
    	h2C.register(requete2.getPath(), response2);
        requete1.addHeader("password", "secret");
        requete2.addHeader("password", "secrt");
    	h2C.sendRequest(requete1);
    	h2C.send();
    	h2C.sendRequest(requete2);
    	h2C.send();
    	h2C.close();
    }    
}
