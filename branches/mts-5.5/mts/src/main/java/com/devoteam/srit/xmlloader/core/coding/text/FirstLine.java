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

package com.devoteam.srit.xmlloader.core.coding.text;

public class FirstLine extends GenericFirstLine {
    //--- attribut --- //

    private String method ="";
    private String uri = null;
    private String version ="";
    private String statusCode =null;
    private String reasonPhrase = null;
    private boolean isRequest = false;

    public FirstLine(String line, String protocol) {
        super(line);
        if (this.tabGeneric != null) {


            if (!this.tabGeneric[0].startsWith(protocol)) {
                this.isRequest = true;
                if (this.tabGeneric.length > 0) {
                    this.method = this.tabGeneric[0].trim();
                }
                if (this.tabGeneric.length > 1) {
                    this.uri = this.tabGeneric[1].trim();
                }
                if (this.tabGeneric.length > 2) {
                    this.version = this.tabGeneric[2].trim();
                }
            } else {
                this.isRequest = false;
                if (this.tabGeneric.length > 0) {
                    this.version = this.tabGeneric[0].trim();
                }
                if (this.tabGeneric.length > 1) {
                    this.statusCode = this.tabGeneric[1].trim();
                }
                if (this.tabGeneric.length > 2) {
                    this.reasonPhrase = this.tabGeneric[2].trim();
                }

            }
        }

    }

    public boolean isRequest() {
        return isRequest;
    }

    public String getMethod() {
        if (method.equals("")) return null;
        else
        return method;
    }

    public String getReasonPhrase() {
         
        return reasonPhrase;
    }

    public String getStatusCode() {
       
        return statusCode;
        
    }

    public String getUri() {
        return uri;
    }

    public String getVersion() {
        return version;
    }
}
