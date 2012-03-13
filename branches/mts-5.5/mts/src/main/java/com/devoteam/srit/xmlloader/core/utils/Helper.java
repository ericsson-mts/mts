/*
 * Created on Oct 6, 2004
 */
package com.devoteam.srit.xmlloader.core.utils;

import com.devoteam.srit.xmlloader.core.Runner;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;

/**
 * @author pn007888
 */
public class Helper {

    /**
     * Replace macro in the text
     * 
     * @param txt the text to work on
     * @param session the concerned session
     * @return the string with macro replaced
     * @throws ExecutionException
     */
     // TODO: update or delete method
    public static String replaceMacros(String txt, Runner session)
            throws ExecutionException {
       /* String msg = txt.replaceAll("[\r\n]", SipUtils.CRLF);

        // Preprocess the SIP message
        // 1- read the external parameters from data.xml
        // 2- load the dynamic parameters
		String result = "";
		int index = 0;
        int index1 = msg.indexOf("[");
        int index2 = msg.indexOf("]");
        while((index1 != -1) && (index2 != -1)) {
        	// Store the left part
            String value = msg.substring(index, index1);
            result = result + value;

			// Get the item to load
            String item = msg.substring(index1+1, index2);
            
            // Test if it's a counter or a method to call or a data or a dynamic parameter
//            if (Script.isScript(item)) {
            	// It's a method to call
//    			String methodToExecute = item.substring(1, item.length()-1);
            	//result = result + Script.executeCommand(session, "../callflows/script/script2.xml", methodToExecute);            	
//            } else
            if (Counter.isCounter(item)) {
            	// It's a counter
            	result = result + Counter.parseCommand(item);            	
            } else {
            	// It's a parameter
	            String data = session.getTestcase().getParentTestcases().getData().getData(item);
	            if (data != null) {
	                // It's a data parameter
	            	data = Helper.replaceMacros(data, session);
	                result = result + data;
	            } else {
	                // It's a dynamic parameter
	                result = result + session.getDynamicParameterResolver().getDynamicParam(item);
	            }
            }
            
            // Update indexes
            index = index2+1;
			index1 = msg.indexOf("[", index);
        	index2 = msg.indexOf("]", index);
        }
        result = result + msg.substring(index);
        return result;*/
        return null;
    }

    /**
     * convert the amount of ms in ellapsed time (hh:mm:ss)
     * @param timeInSeconds
     * @return a string 
     */
    public static String getElapsedTimeString(long timeInSeconds) {
        long hours, minutes, seconds;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return hours + " h " + minutes + " min " + seconds + " sec";
     }
}