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
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.HttpVersion;
import org.apache.http.HttpException;
import java.io.*;

public class HttpLoaderClient extends Thread
{
    
    private static HttpLoaderServer http;
    
    //private Properties properties;
    
    private static HttpLoaderClient testhttp= new HttpLoaderClient(),responseThread;
    
    private static Long beginThreadRequest, endThreadRequest,timeThreadRequest;
    
    private static Long beginThreadResponse, endThreadResponse, timeThreadResponse;
    
    private HttpParams params = new BasicHttpParams();
    
    /** Creates a new instance of TestHttpLoader */
    public HttpLoaderClient()
    {}
    
    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.err.println("Please specify hostname and port number");
            System.exit(1);
        }
        http = new HttpLoaderServer(args[0],Integer.parseInt(args[1]));
        http.testConnection();
        responseThread = new HttpLoaderClient(); // thread de gestion des reponses coté client
        responseThread.setDaemon(false);
        responseThread.start(); // lancement du processus de gestion des msg
        beginThreadRequest = System.currentTimeMillis(); // top chono des requetes
        for (int i = 0; i < 5000000; i++)
            testhttp.createRequest(i); // creation et envoie des messages au serveur
        endThreadRequest = System.currentTimeMillis();
        timeThreadRequest = endThreadRequest - beginThreadRequest; // calcul de la duree de l'envoie des msg'
    }
    
    
    public void run()
    {
        
        Long beginThreadResponse = System.currentTimeMillis(); // top chrono des reponses
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setHttpElementCharset(params, "UTF-8");
        HttpProtocolParams.setContentCharset(params, "text");
        HttpProtocolParams.setUserAgent(params, "XmlLoader");
        HttpProtocolParams.setUseExpectContinue(params, true);
        
        try
        {
            http.receiveResponse("GET","D:/XMLloader/testPileHttp/src/test/HttpLoader/",params,5000000);
            Long endThreadResponse = System.currentTimeMillis();
            timeThreadResponse = endThreadResponse - beginThreadResponse; // calcul de la durée du processus de reception des msg
            Long totalTime = timeThreadRequest + timeThreadResponse; // calcul de la durée du programme
            System.out.println("transaction number : "+ 5000000*2);
            System.out.println("All transaction time : "+ (long)totalTime/1000+" s"
                    /* + "trans/ms: "+(float)totalTime/50*/);
            System.out.println("number transaction /s: "+ (float)(5000000*2)/(totalTime/1000));
        }
        catch(IOException e)
        {System.out.println("connection error");}
        catch(HttpException e)
        {}
    }
    
    protected void createRequest(int i) throws Exception
    {
        // Create a HTTP message object corresponding to the string message
        
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setHttpElementCharset(params, "UTF-8");
        HttpProtocolParams.setContentCharset(params, "text");
        HttpProtocolParams.setUserAgent(params, "XmlLoader");
        HttpProtocolParams.setUseExpectContinue(params, true);
        
        http.sendRequest("GET", "D:/XMLloader/testPileHttp/src/test/HttpLoader/"+i, params);
        
    }
}
