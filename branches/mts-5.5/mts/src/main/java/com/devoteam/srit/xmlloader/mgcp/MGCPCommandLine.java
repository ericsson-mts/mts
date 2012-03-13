/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
