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

package com.devoteam.srit.xmlloader.core.protocol.probe;

import gp.utils.arrays.Array;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author gpasquiers
 */
public class PInputStream extends InputStream
{

    private long currentTimestamp;
    private Array currentArray;
    private int currentIndex;
    private LinkedBlockingQueue<Element> arrays;
    private int bytesLeft;
    private final Object bytesLeftLock;


    public PInputStream()
    {
        arrays = new LinkedBlockingQueue();
        currentArray = null;
        currentTimestamp = -1;
        currentIndex = 0;
        bytesLeft = 0;
        bytesLeftLock = new Object();
    }

    @Override
    public int available(){
        synchronized(bytesLeftLock){
            //System.out.println("    bytesLeft = "+bytesLeft);
            return bytesLeft;
        }
    }

    private Array currentArray(boolean blocking) throws InterruptedException
    {
        if (null == currentArray || currentIndex >= currentArray.length)
        {
            currentIndex = 0;

            if (blocking)
            {
                Element elmt = arrays.take();
                currentArray = elmt.getArray();
                currentTimestamp = elmt.getTimestamp();
                //System.out.println("Nouveau currentTimestamp dans PInputStream : "+currentTimestamp);
                
                //currentArray = arrays.poll(500, TimeUnit.MILLISECONDS);
                //while(null == currentArray){
                //    currentArray = arrays.poll(500, TimeUnit.MILLISECONDS);
                //    if(null == currentArray){
                //        System.out.println("                                currentArray::take timeout with bytesLeft=" + bytesLeft);
                //    }
                //}
            }
            else
            {
                Element elmt = arrays.poll();
                if(elmt != null){
                    currentArray = elmt.getArray();
                    currentTimestamp = elmt.getTimestamp();
                    //System.out.println("Nouveau currentTimestamp dans PInputStream : "+currentTimestamp);
                }
                else{
                    currentArray = null;
                }
            }
        }

        return currentArray;
    }

    public void feed(Array array, Long timestamp)
    {
        if(-1 == currentTimestamp){
            currentTimestamp = timestamp;
        }
        Element elmt = new Element(array, timestamp);
        this.arrays.add(elmt);
        //System.out.println("timestamp enregistre : "+timestamp);
        synchronized(bytesLeftLock){
            bytesLeft += array.length;
        }
    }

    @Override
    public int read() throws IOException
    {
        try
        {
            int value = 0xFF & currentArray(true).get(currentIndex++);
            synchronized(bytesLeftLock){
                bytesLeft--;
            }
            return value;
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Try to fill the byte[] with data from the Arrays.
     *
     * Sockets's InputStream have a special behaviour. This method is supposed to
     * block until either the byte[] buffer is full or the stream ended.
     *
     * However in Sockets this method will only block if there is no data available.
     * When there is one or more bytes available it will fill the byte[] buffer as
     * much as possible and than return.
     *
     * @param b
     * @param off
     * @param length
     * @return
     * @throws IOException
     */
    @Override
    public int read(byte b[], int off, int length) throws IOException
    {
        int done = 0;
        int todo = length;

        try
        {
            b[off] = currentArray(true).get(currentIndex++);
            synchronized(bytesLeftLock){
                bytesLeft--;
            }
            done++;
            todo--;
            
            Array array;
            while(null != (array = currentArray(false)) && todo > 0)
            {
                int copyLength = Math.min(todo, array.length - currentIndex);
                array.getBytes(currentIndex, b, off + done, copyLength);
                currentIndex += copyLength;
                done += copyLength;
                todo -= copyLength;
                synchronized(bytesLeftLock){
                    bytesLeft -= copyLength;
                }
            }


        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());
        }
        return done;
    }
    
    public long getTimestamp(){
        return currentTimestamp;              
    }
    
    class Element{
        private Array array;
        private Long timestamp;

        public Element(Array a, Long t){
            array = a;
            timestamp = t;            
        }

        public Array getArray(){
            return array;
        }

        public Long getTimestamp(){
            return timestamp;
        }
    }
}
