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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.core.licence;

import com.devoteam.srit.xmlloader.core.utils.*;

import gp.utils.arrays.Array;
import gp.utils.arrays.CipherArray;
import gp.utils.arrays.ConstantArray;
import gp.utils.arrays.DefaultArray;
import gp.utils.arrays.DigestArray;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * This class reads the MAC addresses from system commands ipconfig/ifconfig
 * (in order to work with java 1.5)
 * @author gpasquiers
 * This method reads also the MAC addresses with the Java 1.6 methods.
 */
public class UtilsLicence
{

    private static String defaultID = "0123456789abcdeffedcba9876543210";
    private static String licencePath = "../conf/licence/imsloader.licence";

    public static void main(String ... args){
    	System.out.println("Licence.instance().getUser()=" + Licence.instance().getUser());
    	System.out.println("Licence.instance().getCompany()=" + Licence.instance().getCompany());
    	System.out.println("Licence.instance().isComplete()=" + Licence.instance().isComplete());
    	System.out.println("Licence.instance().isTrial()=" + Licence.instance().isTrial());
    	System.out.println("Licence.instance().versionMatches()=" + Licence.instance().versionMatches());
    	System.out.println("Licence.instance().date()=" + Licence.instance().getDate());
    	System.out.println("Licence.instance().validity()=" + Licence.instance().getValidity());
    }

    public static Properties getLicence(){
        String text = null;
        String licence = null;
        Properties properties = new Properties();

        // read the licence file
        try{
            File licenceFile = new File(licencePath);
            int size = (int) licenceFile.length();
            byte[] data = new byte[size];
            FileInputStream fileInputStream = new FileInputStream(licenceFile);
            fileInputStream.read(data);
            text = new String(data);
            fileInputStream.close();
        }
        catch(Exception e){
        	UtilsLicence.logMessage("EXCEPTION getLicence() : open file licence file ", e);                                
        }

        try{
            List<String> ids = UtilsLicence.computeIDNew();
            for (String curId:ids) {
                BufferedReader strReader = new BufferedReader(new StringReader(text));
                if (licence != null)
                    break;
                String line = strReader.readLine();
                while (line != null) {
                    try {
                        licence = decrypt(line, curId);
                        if (licence != null) {
                            licence = new String(Array.fromHexString(licence).getBytes());
                            break;
                        }
                    } catch (Exception e) {}
                    line = strReader.readLine();
                }
            }
            if (licence == null) {
                String id = UtilsLicence.computeID();
                licence = decrypt(text, id);
                licence = new String(Array.fromHexString(licence).getBytes());
            }
        
        }
        catch(Exception e){
            licence = null;
            UtilsLicence.logMessage("EXCEPTION getLicence() : decrypt the licence file with the computerID ", e);                                
        }

        if(null == licence){
            try{
                licence = decrypt(text, defaultID);
                licence = new String(Array.fromHexString(licence).getBytes());
            }
            catch(Exception e){
                UtilsLicence.logMessage("EXCEPTION getLicence() : decrypt the licence file with the default computerID ", e);
                licence = null;
            }
        }

        if(null == licence){
            return null;
        }
        else{
            // parse license and populate properties
            try{
                ByteArrayInputStream bais = new ByteArrayInputStream(licence.getBytes());
                SAXReader reader = new SAXReader(false);
                Document document = reader.read(bais, licencePath);

                String mode = document.createXPath("//mode").selectSingleNode(document).getText();
                properties.setProperty("mode", mode);
                UtilsLicence.logMessage("mode = " + mode, null);                
                String version = document.createXPath("//version").selectSingleNode(document).getText();
                properties.setProperty("version", version);
                UtilsLicence.logMessage("version = " + version, null);
                String user = document.createXPath("//user").selectSingleNode(document).getText();
                properties.setProperty("user", user);
                UtilsLicence.logMessage("user = " + user, null);
                String company = document.createXPath("//company").selectSingleNode(document).getText();
                properties.setProperty("company", company);
                UtilsLicence.logMessage("company = " + company, null);
                String date = document.createXPath("//date").selectSingleNode(document).getText();
                properties.setProperty("date", date);
                UtilsLicence.logMessage("date = " + date, null);
                String validity = document.createXPath("//validity").selectSingleNode(document).getText();
                properties.setProperty("validity", validity);
                UtilsLicence.logMessage("validity = " + validity, null);               
                String computerID = document.createXPath("//computerID").selectSingleNode(document).getText();
                properties.setProperty("computerID",computerID);
                UtilsLicence.logMessage("computerID = " + computerID, null);
                String email = document.createXPath("//email").selectSingleNode(document).getText();
                properties.setProperty("email",email);
                UtilsLicence.logMessage("email = " + email, null);
            }
            catch(Exception e){
            	UtilsLicence.logMessage("EXCEPTION getLicence() : parse the XML licence file : ", e);                                
                return null;
            }
        }

        return properties;
    }

    public static String encrypt(String data, String secret) throws IOException{
        Array dataArray = Array.fromHexString(data);
        if (secret.length() == 12)
            secret += "0123456789abcdef0123";
        Array secretArray = Array.fromHexString(secret);
        Array saltArray = new ConstantArray((byte) 0, 16);
        Array cipher = new CipherArray(dataArray, secretArray, saltArray, "AES/CBC/PKCS5Padding", "AES", Cipher.ENCRYPT_MODE);
        return Array.toHexString(cipher);
    }

    public static String decrypt(String data, String secret) throws IOException{
        Array dataArray = Array.fromHexString(data);
        if (secret.length() == 12)
            secret += "0123456789abcdef0123";
        Array secretArray = Array.fromHexString(secret);
        Array saltArray = new ConstantArray((byte) 0, 16);
        Array cipher = new CipherArray(dataArray, secretArray, saltArray, "AES/CBC/PKCS5Padding", "AES", Cipher.DECRYPT_MODE);
        return Array.toHexString(cipher);
    }

    public static String computeID() throws Exception{
        List<String> addresses = getMacAddresses();

        Collections.sort(addresses);

        String concat = "";
        for(String mac:addresses){
            concat += mac;
        }

        DigestArray digest = new DigestArray(new DefaultArray(concat.getBytes()), "MD5");

        return Array.toHexString(digest);
    }

    public static List<String> computeIDNew() throws Exception{
        List<String> addresses = getMacAddressesNew();
        LinkedList<String> compIDs = new LinkedList<String>();

        if (!addresses.isEmpty()) {
            for(String mac:addresses){
                //DigestArray digest = new DigestArray(new DefaultArray(mac.getBytes()), "MD5");
                //compIDs.add(Array.toHexString(digest));
                compIDs.add(mac);
            }
        }
        else
            compIDs.add("abcdef012345");
        return compIDs;
    }

    public static String getNewComputeID() throws Exception{
        List<String> addresses = getMacAddressesNew();
        String compIDs = "";

        if (!addresses.isEmpty()) {
            for(String mac:addresses){
                //DigestArray digest = new DigestArray(new DefaultArray(mac.getBytes()), "MD5");
                //compIDs += Array.toHexString(digest);
                compIDs += mac;
            }
        }
        else
            compIDs += "abcdef012345";
        return compIDs;
    }

    public static List<String> getMacAddresses() throws Exception
    {
        List<String> result = new LinkedList();
        if(System.getProperty("os.name").contains("Windows")) // windows (ipconfig)
        {
            String std = execCommand("ipconfig /all");
            List<String> list = regexMatches(std, "[^\\-]([0-9A-F]{2}-){5}[0-9A-F]{2}[^\\-]");
            for(String addr:list) result.add(Utils.replaceNoRegex(addr, "-", ""));
        }
        else // other (ifconfig)
        {
            String std = execCommand("ifconfig");
            List<String> list = regexMatches(std, "[^:]([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}[^:]");
            for(String addr:list) result.add(Utils.replaceNoRegex(addr, ":", ""));
        }
        return result;
    }

    public static List<String> getMacAddressesNew() throws Exception
    {
        List<String> result = new LinkedList();
        Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
            if (ni.getHardwareAddress() != null && !ni.isVirtual() &&
                    Array.toHexString(new DefaultArray(ni.getHardwareAddress())).toString().length() == 12)
               result.add(Array.toHexString(new DefaultArray(ni.getHardwareAddress())));
        }
        return result;
    }

    private static List<String> regexMatches(String text, String regex){
        LinkedList<String> list = new LinkedList();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);

        while (m.find())
        {
            if ((m.group() != null) && (m.group().length() > 0))
            {
                list.add(m.group());
            }
        }

        return list;
    }

    private static String execCommand(String command) throws Exception{
        Process process = Runtime.getRuntime().exec(command);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line, std = "";

        while((line = bufferedReader.readLine()) != null) std += line;

        return std;
    }

    public static void logMessage(String message, Throwable exception)
    {
        boolean traceLicenceControl = Config.getConfigByName("tester.properties").getBoolean("core.TRACE_LICENCE_CONTROL", false); 
    	if (traceLicenceControl)
    	{
    		System.out.println(message);
    		if (exception != null)    		
    		{
    			exception.printStackTrace();
    		}
    	}
    }

    private static String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}
