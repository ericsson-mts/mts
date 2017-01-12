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

package com.devoteam.srit.xmlloader.mgcp;

import com.devoteam.srit.xmlloader.core.coding.text.GenericFirstLine;



/**
 *
 * @author indiaye
 */
public class MGCPCommandLine extends GenericFirstLine{

    //--- attribut --- //
   
    private String MGCPVerb = null;
    private String transactionId = null;
    private String endpointName = null;
    private String MGCPversion = "";
    private boolean isRequest = false;
    private String responseCode=null;



    public MGCPCommandLine(String line){
     super(line);
        if (!isResponseCode(this.tabGeneric[0])) {
            isRequest=true;
            MGCPVerb = this.tabGeneric[0];
            transactionId = this.tabGeneric[1];
            endpointName = this.tabGeneric[3];
            MGCPversion = this.tabGeneric[3]+" "+this.tabGeneric[4];
        }
        else
        {
            isRequest=false;
            responseCode=this.tabGeneric[0];
            transactionId = this.tabGeneric[1];
        }


    }

   

    public String getMGCPVerb() {
        return MGCPVerb;
    }

    public void setMGCPVerb(String MGCPVerb) {
        this.MGCPVerb = MGCPVerb;
    }

    public String getMGCPversion() {
        return MGCPversion;
    }

    public void setMGCPversion(String MGCPversion) {
        this.MGCPversion = MGCPversion;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public boolean isIsRequest() {
        return isRequest;
    }

    public void setIsRequest(boolean isRequest) {
        this.isRequest = isRequest;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
      public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public static boolean isResponseCode(String chaine) {
        try {
            Integer.parseInt(chaine);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
