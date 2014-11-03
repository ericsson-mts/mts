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

package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ParsingInputStreamException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.log.TextEvent.Topic;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;

import gp.utils.arrays.DefaultArray;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Utility functions
 *
 */
public class Utils
{

    public static InetAddress localAddress;
    private static int UIDIndex = 0;
    private static DecimalFormat df = new DecimalFormat("##,###,###,###,###,##0.####");    
    private static DecimalFormat dfMicro = new DecimalFormat("0.####E0");    

    static
    {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(decimalFormatSymbols);
        dfMicro.setDecimalFormatSymbols(decimalFormatSymbols);
    }

    /**
     * Private constructor
     */
    private Utils()
    {
        // Nothing
    }

    public static int readFromSocketStream(InputStream inputStream, byte[] tab) throws IOException{
        // GPASQUIERS: FIX
        // Sur des InputStream de sockets TCP la méthode "read" peut s'arreter
        // n'importe ou, meme si le nombre d'octets qu'on a demande n'a pas ete
        // lut. Il faut donc s'assurer de relancer la lecture sur ce stream
        // tant qu'on a pas obtenu le nombre d'octets voulu.
        // Sauf si une des lectures retourne -1 (socket ferme/en erreur)
        int todo = tab.length;
        int done = 0;
        do{
            int res = inputStream.read(tab, done, todo);
            if(res == -1){
                todo = 0;
            }
            else{
                done += res;
                todo -= res;
            }
        }
        while(todo != 0);
        
        return done;
    }


    public static String newString(byte[] bytes)
    {
        char[] chars = new char[bytes.length];
        for(int i=0; i<bytes.length; i++) chars[i] = (char) (bytes[i]&0xff);
        return new String(chars);
    }

    public static byte[] getStringBytes(String string)
    {
        char[] chars = new char[string.length()];
        byte[] bytes = new byte[string.length()];
        string.getChars(0, string.length(), chars,0);
        for(int i=0; i<chars.length; i++) bytes[i] = (byte) (chars[i]&0xff);
        return bytes;
    }

    public static String[] splitPath(String path)
    {
    	int pos2Dots = path.indexOf(':');
        if(pos2Dots > 0 && pos2Dots < path.length() - 1){
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE,
    			"Deprecated separator \":\" in path " + path,
    			" please use \".\" instead.");

            return Utils.splitNoRegex(path, ":");
        }
        else
        {
            return Utils.splitNoRegex(path, ".");
        }
    }

    public static String replaceFileName(String filename)
    {
    	filename = filename.trim();
    	filename = filename.replace("/", "");
    	filename = filename.replace("\\", "");    	
    	filename = filename.replace(":", "");
    	filename = filename.replace("*", "");
    	filename = filename.replace("?", "");
    	filename = filename.replace("\"", "");    	
    	filename = filename.replace("<", "");
    	filename = filename.replace(">", "");
    	filename = filename.replace("|", "");
    	filename = filename.replace("&gt;", "");
    	filename = filename.replace("&lt;", "");
    	return filename;
    }

    public static String unescapeEntities(String text)
    {
        text = text.replace("&lt;", "<");
        text = text.replace("&gt;", ">");
        text = text.replace("&quot;", "\"");
        text = text.replace("&apos", "\'");
        text = text.replace("&amp;", "&");
        return text;
    }

    /**
     * Return the exception's stack trace
     *
     * @param e Exception
     * @return String
     */
    public static String printStackTrace(Throwable e)
    {
        StringBuilder res = new StringBuilder();

        //res.append("Reasons :\n";

        //
        // Print a resume
        //
        //res += "Exception: " + e.getMessage() + "\n";

        Throwable cause = e;
        while (cause != null)
        {
            String message = cause.getMessage();
            if(null == message) message = cause.getClass().getSimpleName();
            res.append("CAUSE: ").append(message).append('\n');
            cause = cause.getCause();
        }

        res.append("\nComplete stack :\n").append(Utils.printStackTrace(e, 1));

        return res.toString();
    }

    /**
     * Return the exception's stack trace
     *
     * @param e Exception
     * @return String
     */
    public static String printStackTrace(Throwable e, int indentLevel)
    {
        StringBuilder indent = new StringBuilder();

        for (int i = 0; i < indentLevel; i++)
        {
            indent.append("    ");
        }

        StringBuilder ret = new StringBuilder().append(indent).append(e.toString()).append('\n');

        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements)
        {
            ret.append(indent).append(stackTraceElement.toString()).append('\n');

        }

        if (null != e.getCause())
        {
            ret.append("Nested exception :\n").append(Utils.printStackTrace(e.getCause(), indentLevel + 1));
        }
        return ret.toString();
    }

    public static String padInteger(String value, int length)
    {
        StringBuilder string = new StringBuilder();
        
        if (value.charAt(0) == '-')
        {
            string.append('-');
            value = value.substring(1);
        }

        while (string.length() < (length - value.length()))
        {
            string.append('0');
        }
        string.append(value);

        return string.toString();
    }

    /**
     * Make a pause
     *
     * @param duration Duration in seconds
     */
    public static void pause(int duration) throws ExecutionException
    {
        if (Thread.currentThread().isInterrupted())
        {
            throw new ExecutionException("Pause interrupted");
        }

        try
        {
            Thread.sleep(duration * 1000);
        }
        catch (Exception e)
        {
            throw new ExecutionException("Pause interrupted", e);
        }
    }

    /**
     * Make a pause
     *
     * @param duration Duration in milliseconds
     */
    public static void pauseMilliseconds(long duration) throws ExecutionException
    {
        if (Thread.currentThread().isInterrupted())
        {
            throw new ExecutionException("Pause interrupted");
        }

        try
        {
            Thread.sleep(duration);
        }
        catch (Exception e)
        {
            throw new ExecutionException("Pause interrupted", e);
        }
    }

    /**
     * Compile a regular expression given by a string 
     * and returns a Pattern object
     * Replace posx and non-standard constant by their value
     * See http://en.wikipedia.org/wiki/Regular_expression
     */
    public static Pattern compilesRegex(String regexp)
    {
    	if ((regexp.indexOf("[:") > 0) || (regexp.indexOf(":]") > 0))
    	{
    		regexp = Utils.replaceNoRegex(regexp, "[:alnum:]", "[A-Za-z0-9]");
    		regexp = Utils.replaceNoRegex(regexp, "[:word:]", "[A-Za-z0-9_]");
    		regexp = Utils.replaceNoRegex(regexp, "[:alpha:]", "[A-Za-z]");
    		regexp = Utils.replaceNoRegex(regexp, "[:blank:]", "[ \\t]");
    		regexp = Utils.replaceNoRegex(regexp, "[:cntrl:]", "[\\x00-\\x1F\\x7F]");
    		regexp = Utils.replaceNoRegex(regexp, "[:digit:]", "[0-9]");
    		regexp = Utils.replaceNoRegex(regexp, "[:graph:]", "[\\x21-\\x7E]");
    		regexp = Utils.replaceNoRegex(regexp, "[:lower:]", "[a-z]");
    		regexp = Utils.replaceNoRegex(regexp, "[:print:]", "[\\x20-\\x7E]");
    		regexp = Utils.replaceNoRegex(regexp, "[:punct:]", "[\\]\\[!\"#$%&\'()*+,./:;<=>?@\\^_`{|}~-]");
    		regexp = Utils.replaceNoRegex(regexp, "[:space:]", "[ \\t\\r\\n\\v\\f]");
    		regexp = Utils.replaceNoRegex(regexp, "[:upper:]", "[A-Z]");
    		regexp = Utils.replaceNoRegex(regexp, "[:xdigit:]", "[A-Fa-f0-9]");
    	}
    	
        Pattern p = Pattern.compile(regexp);
        return p;
    }

    
    /**
     * Returns true if the regex can be found in the value
     */
    public static boolean containsRegex(String value, String regex)
    {
        if (null == value)
        {
            return false;
        }

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value);
        return m.find();
    }

    public static void deleteRecursively(String path)
    {
        File file = new File(path);
        if (file.isDirectory())
        {
            String names[] = file.list();
            for (String name : names)
            {
                deleteRecursively(path + "/" + name);
            }
        }
        file.delete();
    }

    /**
     * Tests if the string is an integer.
     */
    public static boolean isInteger(String string)
    {
        if (null == string || string.length() == 0)
        {
            return false;
        }
        for (int i = 0; i < string.length(); i++)
        {
            if (!Character.isDigit(string.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if the string is an integer.
     */
    public static boolean parseBoolean(String text, String data)
    {
    	text = text.trim();
    	int iPos = text.indexOf(":");
    	String label = text;
    	String value = text;
    	if (iPos >= 0)
    	{
    		label = text.substring(0, iPos);
    		value = text.substring(iPos + 1);
    	}
    	label = label.trim();
    	value= value.trim();
    	
    	try
    	{
    		int i = Integer.parseInt(value);
			if (i == 1)
			{
				if (!label.equalsIgnoreCase("true") && !label.equals(text))
				{
					GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "The boolean value \"" + text + "\"  is not valid for the boolean data \"" + data + "\".");
				}
				return true;
			}
			else if (i == 0)
			{
				if (!label.equalsIgnoreCase("false") && !label.equals(text))
				{
					GlobalLogger.instance().getApplicationLogger().warn(Topic.PROTOCOL, "The boolean value \"" + text + "\"  is not valid for the boolean data \"" + data + "\".");
				}
				return false;
			}
			else
			{
				throw new RuntimeException("The boolean value \"" + text + "\"  is not valid for the boolean data \"" + data + "\"."); 
		    }

    	}
    	catch (NumberFormatException e)
    	{
	    	if ("true".equalsIgnoreCase(label))
	    	{
	    		return true;
	    	}
	    	else if ("false".equalsIgnoreCase(label))
	    	{
	    		return false;
	    	}
	    	else
	    	{
	    		throw new RuntimeException("Bad value value for the boolean data : \"" + text + "\""); 
	    	}
    	}
	}
    
    /**
     * generates a string of nb*"    " (four spaces nb times), used for intentation in printAvp
     */
    public static String indent(int nb)
    {
        String str = "";
        for (int i = 0; i < nb; i++)
        {
            str += "    ";
        }
        return str;
    }

    public static byte[] parseBinary(String contents) throws Exception
    {
        int length = contents.length();

        if (length % 2 != 0)
        {
            throw new Exception("odd number of characters in binary string");
        }

        length = length / 2;

        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++)
        {
            bytes[i] = (byte) Integer.parseInt(contents.substring(2 * i, 2 * i + 2), 16);
        }

        return bytes;
    }

    public static byte[] parseBinaryString(String contents)
    {

        String[] splitted = Utils.splitNoRegex(contents, " ");

        byte[][] result = new byte[splitted.length][];

        for (int i = 0; i < splitted.length; i++)
        {
            String split = splitted[i];
            if (split.length() > 0)
            {
                // read length
                int length = 0;
                int index = 0;
                byte[] value = null;

                if ((index = split.indexOf(':')) != -1)
                {
                    if (split.substring(index + 1).length() > 0)
                    {
                        length = Integer.parseInt(split.substring(index + 1));
                    }
                    split = split.substring(0, index);
                }

                if (split.toLowerCase().startsWith("h"))
                {
                    //Hexa
                    split = split.substring(1);

                    if (split.length() % 2 != 0)
                    {
                        split = "0" + split;
                    }

                    if (length == 0)
                    {
                        length = split.length() / 2;
                    }
                    value = new byte[length];


                    for (int j = value.length - 1; j >= 0; j--)
                    {
                        int splitLength = split.length();

                        long octet;

                        if (splitLength > 0)
                        {
                            octet = Long.parseLong(split.substring(splitLength - 2, splitLength), 16);
                            split = split.substring(0, splitLength - 2);
                        }
                        else
                        {
                            octet = 0;
                        }

                        value[j] = (byte) (octet & 255);
                    }
                }
                else if (split.toLowerCase().startsWith("b"))
                {
                    //Binary
                    split = split.substring(1);

                    if (length == 0)
                    {
                        length = (split.length() / 8);
                        if(split.length() % 8 > 0){
                            length ++;
                        }
                    }
                    value = new byte[length];

                    long val = Long.parseLong(split, 2);

                    for (int j = value.length - 1; j >= 0; j--)
                    {
                        value[j] = (byte) (val & 255);
                        val = val >> 8;
                    }
                }
                else if (split.toLowerCase().startsWith("s"))
                {
                    //String
                    split = split.substring(1);

                    if (length == 0)
                    {
                        length = split.length();
                    }
                    value = new byte[length];

                    byte[] bytes = split.getBytes();

                    for (int j = 1; j <= value.length; j++)
                    {
                        if (bytes.length - j >= 0)
                        {
                            value[value.length - j] = bytes[bytes.length - j];
                        }
                        else
                        {
                            value[value.length - j] = 0;
                        }
                    }
                }
                else
                {
                    //Decimal
                    if (split.toLowerCase().startsWith("d"))
                    {
                        split = split.substring(1);
                    }
                    long val = Long.parseLong(split);
                    // if no length specified, we choose the minimum length to represent to integer 
                    if (length == 0)                    	
                    {
                    	if (Math.abs(val) <= 255)
                    	{
                    		length = 1;
                    	}
                    	else if (Math.abs(val) <= 65535)
                    	{
                    		length = 2;
                    	}
                    	else if (Math.abs(val) <= 4294967295L)
                    	{
                    		length = 4;
                    	}             
                    	else
                    	{
                    		length = 8;
                    	}
                    }

                    value = new byte[length];
                                       

                    for (int j = value.length - 1; j >= 0; j--)
                    {
                        value[j] = (byte) (val & 255);
                        val = val >> 8;
                    }
                }

                result[i] = value;
            }
            else
            {
                result[i] = new byte[0];
            }
        }

        //
        // Compile results
        //
        int totalSize = 0;
        for (byte[] array : result)
        {
            totalSize += array.length;
        }

        byte[] finalResult = new byte[totalSize];

        int index = 0;
        for (int i = 0; i < result.length; i++)
        {
            for (int j = 0; j < result[i].length; j++)
            {
                finalResult[index++] = result[i][j];
            }
        }

        return finalResult;
    }

    public static String toStringBinary(byte[] data, int length)
    {
        String ret = "";
        try
        {
            ret += new String(data, 0, length, "UTF8");
        }
        catch (Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "An error occured while logging the SIP message : ", ret);
            e.printStackTrace();
        }
        ret = ret.replace('\r', '\0');
        ret = ret.replace('\n', '\0');
        ret = ret.replace('\t', '\0');
        return ret;
    }

    public static String toBinaryString(byte[] data)
    {
        return toBinaryString(data, 0, -1, 0, true);
    }

    public static String toBinaryString(byte[] data, boolean format)
    {
        return toBinaryString(data, 0, -1, 0, format);
    }

    public static String toBinaryString(byte[] data, int offset, int length, int indent)
    {
        return toBinaryString(data, offset, length, indent, true);
    }

    public static String byteTabToString(byte[] data)
    {
        String ret = "";
        if (data.length > 0)
        {
		    ret += "<data format=\"text\">\n";
		    try
		    {
		        ret += new String(data, "UTF8") + "\n";
		    }
		    catch (Exception e)
		    {
		        GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "An error occured while logging the message : ", ret);
		        e.printStackTrace();
		    }
		    ret += "</data>\n";
		    ret += "<data format=\"binary\">\n";
		    ret += Utils.toBinaryString(data, 0, data.length, 0) + "\n";
		    ret += "</data>";
        }
        return ret;
    }

    public static String toBinaryString(byte[] data, int offset, int length, int indent, boolean format)
    {
        String indentStr = indent(indent);
        String res = "";

        String string;
        string = "";

        if (length == -1)
        {
            length = data.length - offset;
        }



        for (int i = offset; i < offset + length; i++)
        {
            int value = data[i];

            if (value < 0)
            {
                value = 256 + value;
            }


            String octet = Integer.toHexString(value);
            if (octet.length() < 2)
            {
                octet = "0" + octet;
            }

            string += "h" + octet + " ";

            if (format && string.length() % (4 * 16) == 0)
            {
                res += indentStr + string + "\n";
                string = "";
            }
        }
        if (string.length() > 0)
        {
            res += indentStr + string;
        }

        return res;
    }

    public static String toHexaString(byte[] data)    
    {
    	return toHexaString(data, 0, -1);
    }
    public static String toHexaString(byte[] data, String sep)    
    {
    	return toHexaString(data, 0, -1, sep);
    }
    public static String toHexaString(byte[] data, int offset, int length)
    {
    	return toHexaString(data, offset, length, ".");
    }
    public static String toHexaString(byte[] data, int offset, int length, String sep)
    {
        StringBuffer buffer = new StringBuffer();
        if (length == -1)
        {
            length = data.length - offset;
        }

        for (int i = offset; i < offset + length; i++)
        {
            int value = (data[i] & 0xff) + 0x100;
            buffer.append(Integer.toString(value, 16).substring(1));
            if (sep != null)
            {
            	buffer.append(sep);
            }
        }
        String res = buffer.toString(); 
        return res;
    }

    public static InetAddress getLocalAddress() throws UnknownHostException
    {

        if (!(Utils.localAddress == null))
        {
            return Utils.localAddress;
        }
        InetAddress returnAddr = null;
        Enumeration<NetworkInterface> nets;
        try
        {
            nets = NetworkInterface.getNetworkInterfaces();
            interfacesLooplabel:
            for (NetworkInterface netint : Collections.list(nets))
            {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses))
                {
                    if (inetAddress instanceof Inet4Address)
                    {
                        if (!inetAddress.isLoopbackAddress())
                        {
                            Utils.localAddress = inetAddress;
                            return inetAddress;
                        }
                        else
                        {
                            returnAddr = inetAddress;
                        }
                    }
                }
            }
        }
        catch (SocketException e)
        {
        }

        if (returnAddr == null)
        {
            returnAddr = InetAddress.getLocalHost();
        }
        Utils.localAddress = returnAddr;
        return returnAddr;
    }

    /**
     * Utility method that delegates to the methods of
     * NetworkInterface to
     * determine addresses for this machine.
     * 
     * This method was added to solve the problem which occured when we tried to
     * use an "artificial" IP address added to a NIC under Linux to send RTP packets
     * Apparently, InetAddress.getLocalHost() function doesn't see those addresses as local
     * (even when we added them in /etc/hosts and set hostname correctly)
     * 
     * The solution is to use a method that returns an array of all addresses 
     * (NetworkInterface.getNetworkInterfaces() method added in Java1.4)
     * when JMF attempts to check if a given address belong to the local pool of addresses..
     * 
     * See also http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4665037
     * 
     *
     * @return InetAddress[] - all addresses found from
     * the NetworkInterfaces
     * @throws UnknownHostException - if there is a
     * problem determining addresses
     */
    public static InetAddress[] getAllLocalUsingNetworkInterface() throws UnknownHostException
    {
        ArrayList addresses = new ArrayList();
        Enumeration e = null;
        try
        {
            e = NetworkInterface.getNetworkInterfaces();
        }
        catch (SocketException ex)
        {
            throw new UnknownHostException("127.0.0.1");
        }
        while (e.hasMoreElements())
        {
            NetworkInterface ni = (NetworkInterface) e.nextElement();
            for (Enumeration e2 = ni.getInetAddresses(); e2.hasMoreElements();)
            {
                addresses.add(e2.nextElement());
            }
        }
        InetAddress[] iAddresses = new InetAddress[addresses.size()];
        for (int i = 0; i < iAddresses.length; i++)
        {
            iAddresses[i] = (InetAddress) addresses.get(i);
        }
        return iAddresses;
    }
    
    /*
     * Format the IP address : for IPV6 only add brackets characters; for IPV4 do nothing
     */
    public static String formatIPAddress(String addr)
    {
    	// for IPV6 address then add '[' and ']' character around;
    	if (addr == null)
    	{
    		return addr;
    	}
    	// test IPV6 address
    	if (addr.indexOf(":") >= 0)
    	{
    		if (addr.charAt(0) != '[')
    		{
    			addr = "[" + addr;
    		}
    		int len = addr.length() - 1;
    		if (addr.charAt(len) != ']')
    		{
    			addr = addr + "]";
    		}
    	}
    	return addr;
    }
    
    static public String formatdouble(double number)
    {
        if (Math.abs(number) > 1e-4 || number == 0)
        {
	        return df.format(number);
        }
        else 
        { 
        	return dfMicro.format(number);
        }         
    }

    static public String[] splitNoRegex(String string, String splitter)
    {
        int index;
        int splitterLength = splitter.length();
        int size = 1;

        index = 0;
        while (-1 != (index = string.indexOf(splitter, index)))
        {
            index += splitterLength;
            size++;
        }

        String[] result = new String[size];

        int lastIndex = 0;
        index = 0;
        int resultIndex = 0;
        while (-1 != (index = string.indexOf(splitter, index)))
        {
            result[resultIndex++] = string.substring(lastIndex, index);
            index += splitterLength;
            lastIndex = index;
        }

        result[resultIndex] = string.substring(lastIndex);
        return result;
    }

    static public String replaceNoRegex(String string, String oldStr, String newStr)
    {
        StringBuilder buff = null;
        int index = 0;
        while (index != -1)
        {
            int newIndex = string.indexOf(oldStr, index);
            if (newIndex != -1)
            {
                if(null == buff) buff = new StringBuilder();

                buff.append(string.substring(index, newIndex));
                buff.append(newStr);

                index = newIndex + oldStr.length();
            }
            else
            {
                if(index == 0 && newIndex == -1) return string;

                if(null == buff) buff = new StringBuilder();

                buff.append(string.substring(index));
                index = newIndex;
            }
        }
        return buff.toString();
    }

    public static void copyFile(URI in, URI out) throws Exception
    {
        InputStream fis = SingletonFSInterface.instance().getInputStream(in);
        OutputStream fos = new FileOutputStream(new File(out));
        try
        {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, i);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    public static void showError(Container container, Throwable e)
    {
        showError(container, e.getMessage(), e);
    }

    public static void showError(Container container, String message)
    {
        showError(container, message, new Exception());
    }

    public static void showError(Container container, String message, Throwable e)
    {
        Window window = getNearestDialogOrFrame(container);

        JDialogError jDialogError;
        if (window instanceof Dialog)
        {
            jDialogError = new JDialogError((Dialog) window, true);
        }
        else
        {
            jDialogError = new JDialogError((Frame) window, true);
        }

        jDialogError.setTitle("An error occured.");
        jDialogError.setMessage(message);
        jDialogError.setDetails(e);
        jDialogError.setVisible(true);
    }

    public static Window getNearestDialogOrFrame(Container container)
    {
        while (!(container instanceof Frame) && !(container instanceof Dialog))
        {
            container = container.getParent();
        }

        return (Window) container;
    }

    public static String escapeHTML(String s)
    {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++)
        {
            char c = s.charAt(i);
            switch (c)
            {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case 'à':
                    sb.append("&agrave;");
                    break;
                case 'À':
                    sb.append("&Agrave;");
                    break;
                case 'â':
                    sb.append("&acirc;");
                    break;
                case 'Â':
                    sb.append("&Acirc;");
                    break;
                case 'ä':
                    sb.append("&auml;");
                    break;
                case 'Ä':
                    sb.append("&Auml;");
                    break;
                case 'å':
                    sb.append("&aring;");
                    break;
                case 'Å':
                    sb.append("&Aring;");
                    break;
                case 'æ':
                    sb.append("&aelig;");
                    break;
                case 'Æ':
                    sb.append("&AElig;");
                    break;
                case 'ç':
                    sb.append("&ccedil;");
                    break;
                case 'Ç':
                    sb.append("&Ccedil;");
                    break;
                case 'é':
                    sb.append("&eacute;");
                    break;
                case 'É':
                    sb.append("&Eacute;");
                    break;
                case 'è':
                    sb.append("&egrave;");
                    break;
                case 'È':
                    sb.append("&Egrave;");
                    break;
                case 'ê':
                    sb.append("&ecirc;");
                    break;
                case 'Ê':
                    sb.append("&Ecirc;");
                    break;
                case 'ë':
                    sb.append("&euml;");
                    break;
                case 'Ë':
                    sb.append("&Euml;");
                    break;
                case 'ï':
                    sb.append("&iuml;");
                    break;
                case 'Ï':
                    sb.append("&Iuml;");
                    break;
                case 'ô':
                    sb.append("&ocirc;");
                    break;
                case 'Ô':
                    sb.append("&Ocirc;");
                    break;
                case 'ö':
                    sb.append("&ouml;");
                    break;
                case 'Ö':
                    sb.append("&Ouml;");
                    break;
                case 'ø':
                    sb.append("&oslash;");
                    break;
                case 'Ø':
                    sb.append("&Oslash;");
                    break;
                case 'ß':
                    sb.append("&szlig;");
                    break;
                case 'ù':
                    sb.append("&ugrave;");
                    break;
                case 'Ù':
                    sb.append("&Ugrave;");
                    break;
                case 'û':
                    sb.append("&ucirc;");
                    break;
                case 'Û':
                    sb.append("&Ucirc;");
                    break;
                case 'ü':
                    sb.append("&uuml;");
                    break;
                case 'Ü':
                    sb.append("&Uuml;");
                    break;
                case '®':
                    sb.append("&reg;");
                    break;
                case '©':
                    sb.append("&copy;");
                    break;
                case '€':
                    sb.append("&euro;");
                    break;
                // be carefull with this one (non-breaking whitee space)
                case ' ':
                    sb.append("&nbsp;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    synchronized public static String newUID()
    {
        UIDIndex++;
        return "UID{" + UIDIndex + "}";
    }

    public static String getParentDir(String path)
    {
        String root = "";
        boolean isAbsolute = false;
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {
            File[] roots = File.listRoots();
            for (File aRoot : roots)
            {
                if (path.startsWith(aRoot.toString().replace("\\", "")))
                {
                    root = aRoot.toString().replace("\\", "/");
                    isAbsolute = true;
                    path = path.substring(aRoot.toString().length());
                    break;
                }
            }
            path = path.replace('\\', '/');
        }
        else
        {
            if (path.startsWith("/"))
            {
                isAbsolute = true;
            }
        }

        String[] elements = Utils.splitNoRegex(path, "/");

        ArrayList<String> vector = new ArrayList<String>(elements.length + 1);

        for (String element : elements)
        {
            vector.add(element);
        }

        /*
         * First, remove all ".".
         * Remove the empty elements from the path as well:
         *   This makes this method ignore the ending "/" or the "//" in path
         *   since those generate empty elements.
         */
        while (vector.contains("."))
        {
            vector.remove(".");
        }
        while (vector.contains(""))
        {
            vector.remove("");
        }

        /**
         * Now either remove the last element or add ".." at the end.
         */
        if (vector.size() > 0 && !vector.get(vector.size() - 1).equals(".."))
        {
            vector.remove(vector.size() - 1);
        }
        else
        {
            vector.add("..");
        }


        /*
         * Then try to remove as much ".." elements as possible
         */
        for (int i = 0; i < vector.size(); i++)
        {
            if (vector.get(i).equals(".."))
            {
                if (i > 0 && !vector.get(i - 1).equals(".."))
                {
                    vector.remove(i);
                    vector.remove(i - 1);
                    i = Math.max(0, i - 2);
                }
            }
        }

        /*
         * If it is an absolute path, remove the firsts ".." elements
         */
        if (isAbsolute && vector.size() > 0)
        {
            while (vector.size() > 0 && vector.get(0).equals(".."))
            {
                vector.remove(0);
            }
        }

        /*
         * Finaly build the path
         */
        StringBuilder builder = new StringBuilder();
        if (isAbsolute)
        {
            builder.append(root);
        }
        int size = vector.size();
        for (int i = 0; i < size; i++)
        {
            builder.append(vector.get(i));

            if (i < size - 1)
            {
                builder.append("/");
            }
        }

        return builder.toString();
    }

    public static String readLineFromInputStream(InputStream inputStream) throws Exception
    {
        StringBuilder buf = new StringBuilder();
        int value = 0;

        for (;;)
        {
            value = inputStream.read();
            if (value != -1)
            {
                buf.append((char) value);
                if ((value == '\n'))
                {
                    return buf.toString();
                }
            }
            else
            {
                throw new ParsingInputStreamException("End of stream detected", buf.toString());
            }
        }
    }
    
    public static String normalizePath(String path)
    {
		if (path != null)
		{
		    //, windows case
		    if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
		    {
		        if (!path.startsWith("\""))
		        {
		        	path = "\"" + path;
		        }
		        if (!path.endsWith("\""))
		        {
		        	path = path + "\"";
		        }
		    }
		    // linux case
		    else
		    {
		        if (path.startsWith("\""))
		        {
		        	path = path.substring(1);
		        }
		        if (path.endsWith("\""))
		        {
		        	path = path.substring(0, path.length() - 1);
		        }
		    }
        }
        return path;
    }

    
    public static void openEditor(final URI file)
    {
        ThreadPool.reserve().start(new Runnable()
        {
            public void run()
            {
                String editor = "";
                String command = "";
                try
                {
                    editor = Config.getConfigByName("tester.properties").getString("gui.EDITOR_PATH");
                    editor = Utils.normalizePath(editor);
                    if (editor != null)
                    {
	                    String fileAbsPath = new File(file).getAbsolutePath();
	                    // command = editor + " \"" + fileAbsPath + "\"";
	                    command = editor + " " + fileAbsPath;
	                    GlobalLogger.instance().getApplicationLogger().info(TextEvent.Topic.CORE, "Opening editor with the command :", command);
	                    Runtime.getRuntime().exec(command);
                    }
                }
                catch (Exception e)
                {
                	GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, e, "Unable to start editor with the command : ", command);
                }
            }
        });
    }

    public static byte[] convertToLittleEndian (byte[] data){
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++){
            result[i] = data[data.length-i-1];
        }
        return result;
    }

    public static byte[] convertFromIntegerToByte (int i, int nbByte) throws Exception{
        byte[] tab;
        String hexa;
        if (nbByte == 2) {
            hexa = String.format("%04X", i);
        }
        else if (nbByte == 4) {
            hexa = String.format("%08X", i);
        }
        else{
            throw new Exception("Methode not supported yet");
        }
        tab = DefaultArray.fromHexString(hexa).getBytes();
        return tab;
        
    }

    public static int convertLittleBigIndian(int little) throws Exception
    {
    	return((little&0xff)<<24)+((little&0xff00)<<8)+((little&0xff0000)>>8)+((little>>24)&0xff);
    }

    public static String trimLeft(String str) throws Exception{
    	int i = 0;
    	while ((i < str.length()) && (Character.isWhitespace(str.charAt(i))))
    	{
    		i++;
    	}
    	return str.substring(i);
    }

	public static String randomString(int numChar)
	{
	    StringBuilder s = new StringBuilder();
	    for (int j = 0; j < numChar; j++)
	    {
	        int nextChar = (int) (Math.random() * 62);
	        if (nextChar < 10) //0-9
	        {
	            s.append(nextChar);
	        }
	        else if (nextChar < 36) //a-z
	        {
	            s.append((char) (nextChar - 10 + 'a'));
	        }
	        else //A-Z
	        {
	            s.append((char) (nextChar - 36 + 'A'));
	        }
	    }
	    return s.toString();
	}
    
	public static long randomLong(long min, long max)
	{	
		
		double d = Math.random() * (max - min) + min;
		return Math.round(d);
	}

	public static boolean randomBoolean()
	{	
		long l = Utils.randomLong(0, 1L);
		boolean b; 
		if (l == 1)
		{
			b = true;
		}
		else
		{
			b = false;
		}
		return b;
	}

	public static Document stringParseXML(String xml, boolean deleteNS) throws Exception
	{
		// remove beginning to '<' character
		int iPosBegin = xml.indexOf('<');
		if (iPosBegin > 0)
		{
			xml = xml.substring(iPosBegin);
		}
		// remove from '>' character to the end
		int iPosEnd = xml.lastIndexOf('>');
		if ((iPosEnd > 0) && (iPosEnd < xml.length() - 1))
		{
			xml = xml.substring(0, iPosEnd + 1);
		}
		
		int iPosXMLLine = xml.indexOf("<?xml");
		if (iPosXMLLine < 0)
		{
			xml = "<?xml version='1.0'?>" + xml;
		}
		
		// remove the namespace because the parser does not support them if there are not declare in the root node
		if (deleteNS)
		{
			xml = xml.replaceAll("<[a-zA-Z\\.0-9_]+:", "<");
			xml = xml.replaceAll("</[a-zA-Z\\.0-9_]+:", "</");
		}
		// remove doctype information (dtd files for the XML syntax)
		xml = xml.replaceAll("<!DOCTYPE\\s+\\w+\\s+\\w+\\s+[^>]+>", "");
		
		InputStream input = new ByteArrayInputStream(xml.getBytes());
	    SAXReader reader = new SAXReader(false);
	    reader.setEntityResolver(new XMLLoaderEntityResolver());
	    Document document = reader.read(input);
	    return document;
	}
	
}
