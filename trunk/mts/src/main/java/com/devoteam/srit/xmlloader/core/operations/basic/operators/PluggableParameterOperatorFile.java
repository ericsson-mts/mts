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
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.CSVReader;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIFactory;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import com.devoteam.srit.xmlloader.rtp.flow.WAVReader;
import gp.utils.arrays.Array;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.SupArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary;
import com.devoteam.srit.xmlloader.rtp.flow.CodecDictionary.Protocol;

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
        this.addPluggableName(new PluggableName(NAME_READCSV));
        this.addPluggableName(new PluggableName(NAME_READMEDIA));
        this.addPluggableName(new PluggableName(NAME_READWAVE));
        this.addPluggableName(new PluggableName(NAME_WRITEWAVE));
    }
    
    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        normalizeParameters(operands);
        
        Parameter path = assertAndGetParameter(operands, "value" , "path");
        
        Parameter result = new Parameter();
        
        try
        {
            if(path.length() > 0)
            {
                URI filePath = URIRegistry.IMSLOADER_TEST_HOME.resolve(path.get(0).toString());

                if(name.equals(NAME_B_WRITE) || name.equals(NAME_S_WRITE))
                {
                    Parameter data = assertAndGetParameter(operands, "value2", "data");
                    if(data.length() != 0)
                    {
                        String fileData = data.get(0).toString();
                        File file = new File(filePath);
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
                    File file = new File(filePath);
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
                    File file = new File(filePath);
                    if(file.exists()) file.delete();
                    file.createNewFile();
                    result = null;
                }
                else if(name.equals(NAME_REMOVE))
                {
                    new File(filePath).delete();
                    result = null;
                }
                else if(name.equals(NAME_EXISTS))
                {
                    result.add(new File(filePath).exists());
                }
                else if(name.equals(NAME_READPROPERTY))
                {
                    normalizeParameters(operands);
                    Parameter config = path;
                    Parameter property = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

                    String configName = "";
                    String propertyName = "";
                    try
                    {
                        configName = config.get(0).toString();
                        propertyName = property.get(0).toString();
                        result.add(Config.getConfigByName(configName).getString(propertyName));
                    }
                    catch(Exception e)
                    {
                        throw new ParameterException("Error in setFromConfig operator reading " + propertyName + " in " + configName, e);
                    }
                }
                else if(name.equals(NAME_READPROPERTY))
                {
                    normalizeParameters(operands);
                    Parameter config = path;
                    Parameter property = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");

                    String configName = "";
                    String propertyName = "";
                    try
                    {
                        configName = config.get(0).toString();
                        propertyName = property.get(0).toString();
                        result.add(Config.getConfigByName(configName).getString(propertyName));
                    }
                    catch(Exception e)
                    {
                        throw new ParameterException("Error in setFromConfig operator reading " + propertyName + " in " + configName, e);
                    }
                }
                else if(name.equals(NAME_READCSV))
                {
                    Parameter csvPath = path;
                    Parameter csvCol = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                    CSVReader csvReader = null;
                    try
                    {
                        String var2 = csvCol.get(0).toString();
                        int column = -1;

                        if (Utils.isInteger(csvCol.get(0).toString()))
                        {
                            column = Integer.parseInt(var2);
                        }

                        csvReader = new CSVReader(csvPath.get(0).toString(), "#", "\"");
                        
                        List<String[]> list = csvReader.getData();

                        for (String[] line : list)
                        {
                            if (column == -1)
                            {
                                for (int j = 0; j < line.length; j++)
                                {
                                    if (line[j].trim().equals(var2.trim()))
                                    {
                                        column = j;
                                        break;
                                    }
                                }
                                if (column == -1)
                                {
                                    throw new ParameterException("invalid column index " + var2 + " (" + column + ")");
                                }
                                continue;
                            }
                            result.add(line[column].trim());
                        }
                    }
                    catch(ParameterException e)
                    {
                        throw e;
                    }
                    catch(Exception e)
                    {
                        throw new ParameterException("Error in setFromCSV operator", e);
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
                    SAXReader reader = new SAXReader();

                    URI uri = URIFactory.resolve(URIRegistry.IMSLOADER_RESOURCES_HOME, path.get(0).toString());
                    InputStream in = SingletonFSInterface.instance().getInputStream(uri);
                    Document document = reader.read(in);
                    in.close();

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

                    URI uri = URIFactory.resolve(URIRegistry.IMSLOADER_RESOURCES_HOME, path.get(0).toString());
                    WAVReader waveFileReader = new WAVReader();

                    if (SingletonFSInterface.instance().exists(uri))
                    {
                        InputStream in = SingletonFSInterface.instance().getInputStream(uri);
                        if(waveFileReader.getAudioFileFormat(in) != null)
                        {
                            if (waveFileReader.getPayload() != null){

                                //recuperation du nombre de paquets par ech a partir du fichier xml
                                Parameter paraDeltaTimeMilliSec = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
                                String deltaTimeMilliSec = paraDeltaTimeMilliSec.get(0).toString();
                                int nbEchPerPacket = (int) (Integer.parseInt(deltaTimeMilliSec)  * waveFileReader.getBitRate() / 8);

                                //recuperation du nombre totale de paquets
                                int nbPacket = waveFileReader.getPayload().length / nbEchPerPacket;
                                int nbFullPacket = nbPacket;
                                int nbEchInLastPacket = 0;
                                if (waveFileReader.getPayload().length % nbEchPerPacket != 0){
                                    //si la division a un reste, un rajoute un paquet qui ne sera pas plein
                                    nbEchInLastPacket = waveFileReader.getPayload().length - nbEchPerPacket*nbPacket;
                                    nbPacket++;
                                }

                                //recuperation et decoupage du payload
                                byte[] val;
                                for (int j = 0; j < waveFileReader.getPayload().length; j += nbEchPerPacket){
                                    DefaultArray temp;
                                    if (j < nbFullPacket*nbEchPerPacket){
                                        val = new byte[nbEchPerPacket];
                                        for (int k = j; k < j+nbEchPerPacket; k++){
                                            val[k-j] = waveFileReader.getPayload()[k];
                                        }
                                         temp = new DefaultArray(val);
                                        payloadList.add(Array.toHexString(temp));
                                    }
                                    else{
                                        val = new byte[nbEchInLastPacket];
                                        //on ajoute le dernier paquet qui ne sera pas plein
                                        for (int k = j; k < j+nbEchInLastPacket; k++){
                                            val[k-j] = waveFileReader.getPayload()[k];
                                        }
                                        temp = new DefaultArray(val);
                                        payloadList.add(Array.toHexString(temp));
                                    }
                                }
                            }

                            if (waveFileReader.getPayloadType() == 0 || waveFileReader.getPayloadType() == 8) {
                                payloadType.add(waveFileReader.getPayloadType());
                            }
                            else{
                                throw new ParameterException("The codec is not supported");
                            }
                            bitRate.add(waveFileReader.getBitRate());

                            runner.getParameterPool().set("["+ resultantUnbracketed + ".payload" + "]", payloadList);
                            runner.getParameterPool().set("["+ resultantUnbracketed + ".payloadType" + "]", payloadType);
                            runner.getParameterPool().set("["+ resultantUnbracketed + ".bitRate" + "]", bitRate);
                            result.add(resultantUnbracketed + ".payload");
                            result.add(resultantUnbracketed + ".payloadType");
                            result.add(resultantUnbracketed + ".bitRate");
                        }
                        in.close();
                    }
                    else{
                        throw new ParameterException("Error while reading wave file, file not found");
                    }
                }
                else if(name.equals(NAME_WRITEWAVE)){
                    URI uri = URIFactory.resolve(URIRegistry.IMSLOADER_RESOURCES_HOME, path.get(0).toString());
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
                    int payloadType = Integer.parseInt((String)paraPayloadType.get(0));
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
                    if (paraNbChannel != null) {
                        nbChannels = Integer.parseInt((String)paraNbChannel.get(0));
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
                    if (paraSampleRate != null) {
                        rateSample = Integer.parseInt((String)paraSampleRate.get(0));
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
                    if (paraBitsPerSample != null) {
                        bitsSample = Integer.parseInt((String) paraBitsPerSample.get(0));
                    }
                    else{
                        bitsSample = 8;//8 par defaut
                    }
                    bitsPerSample = Utils.convertFromIntegerToByte(bitsSample, 2);
                    bitsPerSample = Utils.convertToLittleEndian(bitsPerSample);

                    int alignBlock = Integer.parseInt((String)paraBitsPerSample.get(0)) * nbChannels / 8;
                    blockAlign = Utils.convertFromIntegerToByte(alignBlock, 2);
                    blockAlign = Utils.convertToLittleEndian(blockAlign);

                    int rateByte = Integer.parseInt((String)paraBitsPerSample.get(0)) * nbChannels * rateSample / 8;
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
                    File myFile= new File(uri);
                    if (myFile.exists()){
                        myFile.delete();
                    }
                    if (buf.length > 0 ){
                        try{
                            OutputStream out = SingletonFSInterface.instance().getOutputStream(uri);
                            out.write(buf);
                            out.close();
                        }
                        catch(Exception e){
                            GlobalLogger.instance().getApplicationLogger().error(TextEvent.Topic.CORE, e, "Cannot save file in writewave operation", uri);
                        }
                    }
                    result = null;
                }
                else
                {
                    throw new RuntimeException("unsupported operation " + name);
                }
            }
        }
        catch(RuntimeException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new ParameterException("Error in operation " + name, e);
        }
        return result;
    }
}