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

package com.devoteam.srit.xmlloader.rtp.flow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

/**
 *
 * @author jbor
 */
public class WAVReader extends AudioFileReader{

    private byte[]    payload     = null;
    private int       payloadType = 0;
    private int       bitRate    = 0;

    public byte[] getPayload (){
        return this.payload;
    }

    public int getPayloadType (){
        return this.payloadType;
    }

    public int getBitRate (){
        return this.bitRate;
    }
    //lecture d'un int non signe en little-endian
    private long readUnsignedIntLE (DataInputStream is)throws IOException{
        byte[] buf = new byte[4];
        is.readFully(buf);
        return (buf[0] & 0xFF   | ((buf[1] & 0xFF) << 8)  | ((buf[2] & 0xFF) << 16)  | ((buf[3] & 0xFF) << 24));
    }

    //lecture d'un short non signe en little-endian
    private short readUnsignedShortLE (DataInputStream is) throws IOException{
        byte[] buf = new byte[2];
        is.readFully(buf);
        return (short) (buf[0] & 0xFF   | ((buf[1] & 0xFF) << 8));
    }

    //lecture des sample
    private byte[] readSample(DataInputStream is, int offset, int len) throws Exception{
        boolean eOFException = false;
        boolean iOException = false;
        byte[] buff = new byte[offset + len];
        while (!eOFException && !iOException){
            try{
                is.readFully(buff, offset, len);
            }
            catch(EOFException oefe){
                eOFException = true;
            }
            catch(IOException ioe){
                iOException = true;
            }
        }
        return buff;
    }

    @Override
    public AudioFileFormat getAudioFileFormat(InputStream in) throws UnsupportedAudioFileException, IOException {
        DataInputStream din;
        byte[] buf = new byte[4];

        if (in instanceof DataInputStream){
            din = (DataInputStream) in;
        }
        else{
            din = new DataInputStream(in);
        }

        din.readFully(buf);
        if (!new String(buf).equals("RIFF")){ // "RIFF"
            throw new UnsupportedAudioFileException("Invalid WAV chunk header.");
        }

        // Read the length of this RIFF thing.
        readUnsignedIntLE(din);

        din.readFully(buf);
        if (!new String(buf).equals("WAVE")){ // "WAVE"
            throw new UnsupportedAudioFileException("Invalid WAV chunk header.");
        }

        boolean foundFmt = false;
        boolean foundData = false;

        short compressionCode = 0, numberChannels = 0, blockAlign = 0, bitsPerSample = 0;
        long sampleRate = 0, bytesPerSecond = 0;
        long chunkLength = 0;
        int chunkId = 0;

        while (! foundData){
            chunkId = din.readInt();
            chunkLength = readUnsignedIntLE(din);
            
            switch (chunkId){
                case 0x666D7420: // "fmt "
                    foundFmt = true;
                    compressionCode = readUnsignedShortLE(din);
                    numberChannels = readUnsignedShortLE(din);
                    sampleRate = readUnsignedIntLE(din);
                    bytesPerSecond = readUnsignedIntLE(din);
                    blockAlign = readUnsignedShortLE(din);
                    bitsPerSample = readUnsignedShortLE(din);
                    din.skip(chunkLength - 16);
                    break;
                case 0x66616374: // "fact"
                    din.skip(chunkLength);
                    break;
                case 0x64617461: // "data"
                    if (! foundFmt){
                        throw new UnsupportedAudioFileException("This implementation requires WAV fmt chunks precede data chunks.");
                    }
                    else{
                        try{
                            this.payload = readSample(din, 0, (int) chunkLength);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    foundData = true;
                    break;
                default:
                    // Unrecognized chunk.  Skip it.
                    din.skip(chunkLength);
            }
        }

        AudioFormat.Encoding encoding;

        switch (compressionCode){
            case 1: // PCM/uncompressed
                //unite de base: kb/s
                this.bitRate = 88;//11025 (o/s) * 8 (b) / 1000
                this.payloadType = -1;//there is no payloadType for PCM
                if (bitsPerSample <= 8){
                    encoding = AudioFormat.Encoding.PCM_UNSIGNED;
                }
                else{
                    encoding = AudioFormat.Encoding.PCM_SIGNED;                    
                }
                break;
            case 6://G.711 a-law
                //unite de base: kb/s
               this.bitRate = 64;//8000 (o/s) * 8 (b) / 1000
               encoding = AudioFormat.Encoding.ALAW;
               this.payloadType = 8;
                break;
            case 7://G.711 mu-law
                //unite de base: kb/s
                this.bitRate = 64;//8000 (o/s) * 8 (b) / 1000
                encoding = AudioFormat.Encoding.ULAW;
                this.payloadType = 0;
                break;
            default:
                throw new UnsupportedAudioFileException("Unrecognized WAV compression code: 0x" + Integer.toHexString(compressionCode));
        }
        return new AudioFileFormat (AudioFileFormat.Type.WAVE, new AudioFormat(
          encoding, (float) sampleRate, bitsPerSample, numberChannels,
          ((bitsPerSample + 7) / 8) * numberChannels, (float) bytesPerSecond, false),
          (int) chunkLength);
    }

    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        InputStream is = url.openStream();
        try{
            return getAudioFileFormat(is);
        }
        finally{
            is.close();
        }
    }

    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        InputStream is = new FileInputStream(file);
        try{
            return getAudioFileFormat(is);
        }
        finally{
            is.close();
        }
    }

    @Override
    public AudioInputStream getAudioInputStream(InputStream stream) throws UnsupportedAudioFileException, IOException {
        AudioFileFormat aff = getAudioFileFormat(stream);
        return new AudioInputStream(stream, aff.getFormat(), (long) aff.getFrameLength());
    }

    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(url.openStream());
    }

    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        return getAudioInputStream(new FileInputStream(file));
    }
}