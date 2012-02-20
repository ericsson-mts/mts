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

import com.devoteam.srit.xmlloader.core.Tester;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author gpasquiers
 */
public class Licence {
	
    private static Licence instance;
    
    static synchronized public Licence instance(){
        if(null == instance) instance = new Licence();
        return instance;
    }

    private Properties properties;

    public Licence(){
        properties = UtilsLicence.getLicence();
        if(null == properties) {
            try{
                throw new RuntimeException("invalid licence, computer ID is " + UtilsLicence.getNewComputeID());
            }
            catch(Exception e){
                throw new RuntimeException("invalid licence, but could not compute computer ID", e);
            }
        }
    }

    public boolean isTrial(){
        return properties.getProperty("mode").equalsIgnoreCase("TRIAL");
    }

    public boolean isComplete(){
        return properties.getProperty("mode").equalsIgnoreCase("COMPLETE");
    }

    public boolean versionMatches(){
        String versionPattern = properties.getProperty("version");
        String version = Tester.getRelease();
        return version.matches(versionPattern);
    }

    public String getUser(){
        return properties.getProperty("user");
    }

    public String getCompany(){
        return properties.getProperty("company");
    }

    public String getDate(){
        return properties.getProperty("date");
    }

    public String getValidity(){
        return properties.getProperty("validity");
    }

    public String getComputerID(){
        return properties.getProperty("computerID");
    }

    public String getVersion(){
        return properties.getProperty("version");
    }
    
    public String getEmail(){
        return properties.getProperty("email");
    }




    public static String getProcedure(String computersID)
    {
    	String proc = "" + 
		"From this window, please follow the instruction below to request a license " + 
		"file from the Voxpilot company:\n" +
		"* Press the button to copy the content of the windows text area (including the computer ID).\n" +            		
		"* Then you should send the clipboard content by mail to imsloader@devoteam.com.\n" + 
		"* In the better delay, you will receive from Devoteam company a license file attached to our response.\n" +
		"* Then you just have to copy this file into the <InstallDirectory>/conf/licence directory.\n" +
		"---------------------------------------------------------------------------\n" +
		"Your computerID is : " + computersID + "\n";
        return proc;
    }
    
    public static String getTrialMessage()
    {
        try {
            String msg= "" +
            "This is an IMSloader trial version of IMSLoader product\n"+
            "To have more information about the product, please contact \n" +
            "M. Franck GUEGUEN:\n"+
            "86, rue Anatole France, 92300 Levallois-Perret, FRANCE\n"+
            "Phone: + 33 6 23 97 25 85\n"+
            "Mail: franck.gueguen@voxpilot.com"+"\n\n"+
            "IMSLoader "+Tester.getRelease()+"\n"+
            "ComputerID : "+UtilsLicence.getNewComputeID();
            return msg;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getExpiredLicenceMessage()
    {
        try {
            String msg= "" +
            "Your IMSloader licence as expired\n" +
            "To have more information about the product, please contact \n" +
            "M. Franck GUEGUEN:\n"+
            "86, rue Anatole France, 92300 Levallois-Perret, FRANCE\n"+
            "Phone: + 33 6 23 97 25 85\n"+
            "Mail: franck.gueguen@voxpilot.com"+"\n\n"+
            "IMSLoader "+Tester.getRelease()+"\n"+
            "ComputerID : "+UtilsLicence.getNewComputeID();
            return msg;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getBadVersionLicenceMessage()
    {
        try {
            String msg= "" +
            "Your IMSloader licence does not support this version of IMSLoader\n" +
            "To have more information about the product, please contact \n" +
            "M. Franck GUEGUEN:\n"+
            "86, rue Anatole France, 92300 Levallois-Perret, FRANCE\n"+
            "Phone: + 33 6 23 97 25 85\n"+
            "Mail: franck.gueguen@voxpilot.com"+"\n\n"+
            "IMSLoader "+Tester.getRelease()+"\n"+
            "ComputerID : "+UtilsLicence.getNewComputeID();
            return msg;
        }
        catch (Exception e) {
            return null;
        }
    }
}
