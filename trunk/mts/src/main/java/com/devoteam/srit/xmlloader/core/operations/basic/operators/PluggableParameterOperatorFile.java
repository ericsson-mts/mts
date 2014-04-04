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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.CSVReader;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.rtp.flow.WAVReader;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary.Protocol;
import java.util.Vector;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorFile extends AbstractPluggableParameterOperator
{
    final private String NAME_B_WRITE       = "file.writebinary";
    final private String NAME_B_READ        = "file.readbinary";
    final private String NAME_S_WRITE       = "file.writestring";
    final private String NAME_S_READ        = "file.readstring";
    final private String NAME_READPROPERTY  = "file.readproperty";
    final private String NAME_LISTPROPERTYKEYS = "file.listpropertykeys";
    final private String NAME_READCSV       = "file.readcsv";
    final private String NAME_REMOVE        = "file.remove";
    final private String NAME_EXISTS        = "file.exists";
    final private String NAME_CREATE        = "file.create";
    final private String NAME_READMEDIA     = "file.readmedia";
    final private String NAME_READWAVE      = "file.readwave";
    final private String NAME_WRITEWAVE     = "file.writewave";

    public PluggableParameterOperatorFile()
    {
        this.addPluggableName(new PluggableName(NAME_B_WRITE));
        this.addPluggableName(new PluggableName(NAME_B_READ));
        this.addPluggableName(new PluggableName(NAME_S_WRITE));
        this.addPluggableName(new PluggableName(NAME_S_READ));
        this.addPluggableName(new PluggableName(NAME_REMOVE));
        this.addPluggableName(new PluggableName(NAME_EXISTS));
        this.addPluggableName(new PluggableName(NAME_CREATE));
        this.addPluggableName(new PluggableName(NAME_READPROPERTY));
        this.addPluggableName(new PluggableName(NAME_LISTPROPERTYKEYS));
        this.addPluggableName(new PluggableName(NAME_READCSV));
        this.addPluggableName(new PluggableName(NAME_READMEDIA));
        this.addPluggableName(new PluggableName(NAME_READWAVE));
        this.addPluggableName(new PluggableName(NAME_WRITEWAVE));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception
    {
        normalizeParameters(operands);
        
        Parameter path = assertAndGetParameter(operands, "value" , "path");
        
        Parameter result = new Parameter();
        
        if(path.length() > 0)
        {
            URI filePathURI = URIRegistry.MTS_TEST_HOME.resolve(path.get(0).toString());

            if(name.equals(NAME_B_WRITE) || name.equals(NAME_S_WRITE))
            {
                Parameter data = assertAndGetParameter(operands, "value2", "data");
                if(data.length() != 0)
                {
                    String fileData = data.get(0).toString();
                    File file = new File(filePathURI);
                    if(!file.exists()) file.createNewFile();
                    OutputStream out = new FileOutputStream(file, true);

                    Array array;

                    if(name.equals(NAME_B_WRITE))
                    {
                        array = Array.fromHexString(fileData);
                    }
                    else
                    {
                        array = new DefaultArray(fileData.getBytes());
                    }

                    out.write(array.getBytes());
                    out.close();
                }
                result = null;
            }
            else if(name.equals(NAME_B_READ) || name.equals(NAME_S_READ))
            {
                File file = new File(filePathURI);
                byte[] bytes = new byte[(int) file.length()];
                InputStream in = new FileInputStream(file);
                in.read(bytes);
                in.close();

                if(name.equals(NAME_B_READ))
                {
                    result.add(Array.toHexString(new DefaultArray(bytes)));
                }
                else
                {
                    result.add(new String(bytes));
                }
            }
            else if(name.equals(NAME_CREATE))
            {
                File file = new File(filePathURI);
                if(file.exists()) file.delete();
                file.createNewFile();
                result = null;
            }
            else if(name.equals(NAME_REMOVE))
            {
                new File(filePathURI).delete();
                result = null;
            }
            else if(name.equals(NAME_EXISTS))
            {
                result.add(new File(filePathURI).exists());
            }
            else if(name.equals(NAME_READPROPERTY))
            {
                normalizeParameters(operands);
                Parameter config = path;
                Parameter property = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

                String configName = "";
                String propertyName = "";
                configName = config.get(0).toString();
                propertyName = property.get(0).toString();
                result.add(Config.getConfigByName(configName).getString(propertyName));
            }
            else if(name.equals(NAME_LISTPROPERTYKEYS))
            {
                normalizeParameters(operands);
                Parameter config = path;

                String configName = "";
                configName = config.get(0).toString();
                
                Vector<String> names = Config.getConfigByName(configName).getPropertiesEnhanced().getNameOfAllParameters();
                for(String key:names){
                    result.add(key);
                }
            }            
            else if(name.equals(NAME_READCSV))
            {
                Parameter csvCol = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

                String comment = Config.getConfigByName("tester.properties").getString("operations.CSV_COMMENT_CHAR", "#");
                String separator = Config.getConfigByName("tester.properties").getString("operations.CSV_SEPARATOR_CHAR", ";");
                String escape = Config.getConfigByName("tester.properties").getString("operations.CSV_ESCAPE_CHAR", "\"");
                CSVReader csvReader = new CSVReader(comment, separator, escape + escape);
                
                String var2 = csvCol.get(0).toString();
                int column = -1;

                List<String> listData = null;
                if (Utils.isInteger(var2))
                {
                    column = Integer.parseInt(var2);
                    listData = csvReader.loadData(filePathURI, column, false);
                }
                else
                {
	                // get the header line to retrieve the column number
	                String[] listHeader = csvReader.loadHeader(filePathURI);
	                for (int j = 0; j < listHeader.length; j++)
	                {
	                    if (listHeader[j].equals(var2.trim()))
	                    {
	                        column = j;
	                        break;
	                    }
	                }
	                if (column >= 0)
	                {
	                	listData = csvReader.loadData(filePathURI, column, true);
	                }
                }
                if (listData != null)
                {
                	result.addAll(listData);
                }
            }
            else if(name.equals(NAME_READMEDIA))
            {
                String resultantUnbracketed = ParameterPool.unbracket(resultant);
                Parameter payloadList = new Parameter(resultantUnbracketed + ".payload");
                Parameter timestampList = new Parameter(resultantUnbracketed + ".timestampList");
                Parameter seqList = new Parameter(resultantUnbracketed + ".seqList");
                Parameter payloadType = new Parameter(resultantUnbracketed + ".payloadType");
                Parameter deltaTime = new Parameter(resultantUnbracketed + ".deltaTime");
                Parameter markList = new Parameter(resultantUnbracketed + ".markList");
                boolean isPayloadTypeSet = false;
                long timestamp = 0;
                long oldTimestamp = 0;
                long diffTimestamp = 0;

                String value = null;
                Element node = null;
                
                InputStream in = null;
                Document document = null;
                try {
	                SAXReader reader = new SAXReader();
	                in = SingletonFSInterface.instance().getInputStream(filePathURI);
	                document = reader.read(in);
	                in.close();
                }
                catch(Exception e){
                	if (in != null) in.close();
                    throw e;
                }
                //parsing du fichier
                List listNode = document.selectNodes("//proto[@name='geninfo' or @name='rtp']/field[@name='timestamp' or @name='rtp.payload' or @name='rtp.seq' or @name='rtp.timestamp' or @name='rtp.p_type' or @name='rtp.marker']");//5s for 50000

                for(int j = 0; j < listNode.size(); j++)//parse all rtp message
                {
                    node = (Element)listNode.get(j);
                    value = node.attributeValue("name");

                    if(value.equalsIgnoreCase("rtp.payload"))
                    {
                        value = node.attributeValue("value");//get payload in hexa string
                        payloadList.add(value);
                    }
                    else if(value.equalsIgnoreCase("rtp.timestamp"))
                    {
                        value = node.attributeValue("show");//get timestamp
                        timestampList.add(Long.parseLong(value));
                    }
                    else if(value.equalsIgnoreCase("rtp.seq"))
                    {
                        value = node.attributeValue("show");//get seqnum
                        seqList.add(Long.parseLong(value));
                    }
                    else if(value.equalsIgnoreCase("rtp.marker"))
                    {
                        value = node.attributeValue("show");//get mark header
                        markList.add((Integer)Integer.parseInt(value));
                    }
                    else if(!isPayloadTypeSet && value.equalsIgnoreCase("rtp.p_type"))
                    {
                        value = node.attributeValue("show");//get payloadType
                        payloadType.add((Integer)Integer.parseInt(value));
                        isPayloadTypeSet = true;
                    }
                    else if(value.equalsIgnoreCase("timestamp"))//get arrival time, useful to calculate deltaTime between each packet
                    {
                        value = node.attributeValue("value");//get capture timestamp
                        timestamp = (long) (Double.parseDouble(value) * 1000);//convert in ms
                        if(oldTimestamp != 0)//for all messages except the first received
                        {
                            diffTimestamp = timestamp - oldTimestamp;
                        }
                        oldTimestamp = timestamp;
                        deltaTime.add(diffTimestamp);
                    }

                }
                runner.getParameterPool().set("["+ resultantUnbracketed + ".payload" +"]", payloadList);
                runner.getParameterPool().set("["+ resultantUnbracketed + ".timestamp" +"]", timestampList);
                runner.getParameterPool().set("["+ resultantUnbracketed + ".seq" +"]", seqList);
                runner.getParameterPool().set("["+ resultantUnbracketed + ".payloadType" +"]", payloadType);
                runner.getParameterPool().set("["+ resultantUnbracketed + ".deltaTime" +"]", deltaTime);
                runner.getParameterPool().set("["+ resultantUnbracketed + ".markList" +"]", markList);
                result.add(resultantUnbracketed + ".payload");
                result.add(resultantUnbracketed + ".timestamp");
                result.add(resultantUnbracketed + ".seq");
                result.add(resultantUnbracketed + ".payloadType");
                result.add(resultantUnbracketed + ".deltaTime");
                result.add(resultantUnbracketed + ".markList");
            }
            else if (name.equals(NAME_READWAVE)){
                String resultantUnbracketed = ParameterPool.unbracket(resultant);
                Parameter payloadList = new Parameter(resultantUnbracketed + ".payload");
                Parameter payloadType = new Parameter(resultantUnbracketed + ".payloadType");
                Parameter bitRate = new Parameter(resultantUnbracketed + ".bitRate");

                InputStream in = null;
                WAVReader waveFileReader = null;
                AudioFileFormat format = null;
                try {
	                in = SingletonFSInterface.instance().getInputStream(filePathURI);
	                if (in == null)
	                {
	                	Exception e = new FileNotFoundException(filePathURI.toString() + " file is not found.");
	                	throw e;
	                }
	                waveFileReader = new WAVReader();
	                format = waveFileReader.getAudioFileFormat(in);
	                in.close();
                }
                catch(Exception e){
                	if (in != null) in.close();
                    throw e;
                }	            
                
                if (format != null){
                	byte[] payload = waveFileReader.getPayload();

                    //recuperation du nombre de paquets par ech a partir du fichier xml
                    Parameter paraDeltaTimeMilliSec = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                    String strDeltaTimeMilliSec = paraDeltaTimeMilliSec.get(0).toString();
                    int deltaTimeMilliSec = Integer.parseInt(strDeltaTimeMilliSec);
                    int nbEchPerPacket = deltaTimeMilliSec  * waveFileReader.getBitRate() / 8;

                    //recuperation du nombre totale de paquets
                    int nbPacket = payload.length / nbEchPerPacket;
                    int nbFullPacket = nbPacket;
                    int nbEchInLastPacket = 0;
                    if (payload.length % nbEchPerPacket != 0){
                        //si la division a un reste, un rajoute un paquet qui ne sera pas plein
                        nbEchInLastPacket = payload.length - nbEchPerPacket*nbPacket;
                        nbPacket++;
                    }

                    //recuperation et decoupage du payload
                    byte[] val;
                    for (int j = 0; j < payload.length; j += nbEchPerPacket){
                        DefaultArray temp;
                        if (j < nbFullPacket*nbEchPerPacket){
                            val = new byte[nbEchPerPacket];
                            for (int k = j; k < j+nbEchPerPacket; k++){
                                val[k-j] = payload[k];
                            }
                             temp = new DefaultArray(val);
                            payloadList.add(Array.toHexString(temp));
                        }
                        else{
                            val = new byte[nbEchInLastPacket];
                            //on ajoute le dernier paquet qui ne sera pas plein
                            for (int k = j; k < j+nbEchInLastPacket; k++){
                                val[k-j] = payload[k];
                            }
                            temp = new DefaultArray(val);
                            payloadList.add(Array.toHexString(temp));
                        }
                    }
                    int payloadTypeInt = waveFileReader.getPayloadType();
                    if (!(payloadTypeInt != 0) && !(payloadTypeInt != 8)) {
                    	throw new ParameterException("The codec is not supported : " + payloadTypeInt);
                    }
                    payloadType.add(payloadTypeInt);
                    
                    bitRate.add(waveFileReader.getBitRate());

                    runner.getParameterPool().set("["+ resultantUnbracketed + ".payload" + "]", payloadList);
                    runner.getParameterPool().set("["+ resultantUnbracketed + ".payloadType" + "]", payloadType);
                    runner.getParameterPool().set("["+ resultantUnbracketed + ".bitRate" + "]", bitRate);
                    result.add(resultantUnbracketed + ".payload");
                    result.add(resultantUnbracketed + ".payloadType");
                    result.add(resultantUnbracketed + ".bitRate");
                }
            }
            else if(name.equals(NAME_WRITEWAVE)){
                byte[] chunkID = "RIFF".getBytes();
                byte[] chunkSize;
                byte[] format = "WAVE".getBytes();
                byte[] subChunk1ID = "fmt ".getBytes();
                byte[] subChunk1Size;
                byte[] audioFormat;
                byte[] numberChannels;
                byte[] sampleRate;
                byte[] byteRate;
                byte[] blockAlign;
                byte[] bitsPerSample;
                byte[] subChunkFactID = "fact".getBytes();
                byte[] subChunkFactSize;
                byte[] fact;
                byte[] subChunk2ID = "data".getBytes();
                byte[] subChunk2Size;
                byte[] payload;

                CodecDictionary dico = new CodecDictionary();
                Parameter paraPayloadType = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                String strPayloadType = paraPayloadType.get(0).toString();
                int payloadType = Integer.parseInt(strPayloadType);
                if (payloadType != 0 && payloadType != 8) {
                    throw new Exception("Payload Type not supported yet");
                }
                Protocol protocol = dico.getProtocol(payloadType);

                int formatAudio;
                if (protocol == null) {
                    formatAudio = 6;//default value for PCMA
                }
                else {
                    formatAudio = protocol.getCompressionCode();
                }
                if (formatAudio < 0) {
                    formatAudio = 6;
                }
                audioFormat = Utils.convertFromIntegerToByte(formatAudio, 2);
                audioFormat = Utils.convertToLittleEndian(audioFormat);

                Parameter paraNbChannel = PluggableParameterOperatorList.getParameter(operands, "value6");
                int nbChannels;
                if (paraNbChannel != null && paraNbChannel.length() > 0) {
                	String strNbChannel = paraNbChannel.get(0).toString();
                    nbChannels = Integer.parseInt(strNbChannel);
                }
                else{
                   if (protocol == null) {
                    nbChannels = 1;
                    }
                    else {
                        nbChannels = protocol.getNbChannel();
                    }
                    if (nbChannels <= 0) {
                        nbChannels = 1;
                    }
                }
                numberChannels = Utils.convertFromIntegerToByte(nbChannels, 2);
                numberChannels = Utils.convertToLittleEndian(numberChannels);

                Parameter paraSampleRate = PluggableParameterOperatorList.getParameter(operands, "value5");
                int rateSample;
                if (paraSampleRate != null && paraSampleRate.length() > 0) {
                	String strSampleRate = paraSampleRate.get(0).toString();
                    rateSample = Integer.parseInt(strSampleRate);
                }
                else {
                    if(protocol == null) {
                        rateSample = 8000;//8000 default value for PCMA/PCMU
                    }
                    else {
                        rateSample = protocol.getClockRate();
                    }
                    if (rateSample <= 0) {
                        rateSample = 8000;
                    }
                }
                sampleRate = Utils.convertFromIntegerToByte(rateSample, 4);
                sampleRate = Utils.convertToLittleEndian(sampleRate);

                Parameter paraBitsPerSample = PluggableParameterOperatorList.getParameter(operands, "value4");
                int bitsSample;
                if (paraBitsPerSample != null && paraBitsPerSample.length() > 0) {
                	String strBitsPerSample = paraBitsPerSample.get(0).toString();
                    bitsSample = Integer.parseInt(strBitsPerSample);
                }
                else{
                    bitsSample = 8;//8 par defaut
                }
                bitsPerSample = Utils.convertFromIntegerToByte(bitsSample, 2);
                bitsPerSample = Utils.convertToLittleEndian(bitsPerSample);
                
                int alignBlock = bitsSample * nbChannels / 8;
                blockAlign = Utils.convertFromIntegerToByte(alignBlock, 2);
                blockAlign = Utils.convertToLittleEndian(blockAlign);

                int rateByte = bitsSample * nbChannels * rateSample / 8;
                byteRate = Utils.convertFromIntegerToByte(rateByte, 4);
                byteRate = Utils.convertToLittleEndian(byteRate);

                Parameter paraPayloadBinary = PluggableParameterOperatorList.assertAndGetParameter(operands, "value3");
                SupArray concat = new SupArray();
                String temp;
                for(int k = 0; k < paraPayloadBinary.length(); k++){
                    temp = (String) paraPayloadBinary.get(k);
                    concat.addLast(Array.fromHexString(temp));
                }
                payload = concat.getBytes();

                subChunk2Size = Utils.convertFromIntegerToByte(payload.length, 4);
                subChunk2Size = Utils.convertToLittleEndian(subChunk2Size);

                int sub1 = 28;//16 (same for all) + 12 (for PCMA: "fact" field)
                subChunk1Size = Utils.convertFromIntegerToByte(sub1, 4);
                subChunk1Size = Utils.convertToLittleEndian(subChunk1Size);

                int sub0 = 4 + (8 + sub1) + (8 + payload.length);
                chunkSize = Utils.convertFromIntegerToByte(sub0, 4);
                chunkSize = Utils.convertToLittleEndian(chunkSize);

                int subFact = 4;
                subChunkFactSize = Utils.convertFromIntegerToByte(subFact, 4);
                subChunkFactSize = Utils.convertToLittleEndian(subChunkFactSize);

                fact = subChunk2Size;
                int totalLength = chunkID.length+chunkSize.length+format.length+subChunk1ID.length+subChunk1Size.length+
                        audioFormat.length+numberChannels.length+sampleRate.length+byteRate.length+blockAlign.length+
                        bitsPerSample.length+subChunkFactID.length+subChunkFactSize.length+fact.length+subChunk2ID.length+
                        subChunk2Size.length+payload.length;
                byte[] buf = new byte[totalLength];
                System.arraycopy(chunkID, 0, buf, 0, 4);
                System.arraycopy(chunkSize, 0, buf, 4, 4);
                System.arraycopy(format, 0, buf, 8, 4);
                System.arraycopy(subChunk1ID, 0, buf, 12, 4);
                System.arraycopy(subChunk1Size, 0, buf, 16, 4);
                System.arraycopy(audioFormat, 0, buf, 20, 2);
                System.arraycopy(numberChannels, 0, buf, 22, 2);
                System.arraycopy(sampleRate, 0, buf, 24, 4);
                System.arraycopy(byteRate, 0, buf, 28, 4);
                System.arraycopy(blockAlign, 0, buf, 32, 2);
                System.arraycopy(bitsPerSample, 0, buf, 34, 2);
                System.arraycopy(subChunkFactID, 0, buf, 36, 4);
                System.arraycopy(subChunkFactSize, 0, buf, 40, 4);
                System.arraycopy(fact, 0, buf, 44, 4);
                System.arraycopy(subChunk2ID, 0, buf, 48, 4);
                System.arraycopy(subChunk2Size, 0, buf, 52, 4);
                System.arraycopy(payload, 0, buf, 56, payload.length);
                File myFile= new File(filePathURI);
                if (myFile.exists()){
                    myFile.delete();
                }
                if (buf.length > 0 ){
                	OutputStream out = null;
                    try{
                        out = SingletonFSInterface.instance().getOutputStream(filePathURI);
                        out.write(buf);
                        out.close();
                    }
                    catch(Exception e){
                    	if (out != null) out.close();
                        throw e;
                    }
                }
                result = null;
            }
            else
            {
                throw new RuntimeException("unsupported operation " + name);
            }
        }
        return result;
    }
}