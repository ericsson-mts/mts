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

package com.devoteam.srit.xmlloader.diameter.test;

public final class IMSDiameterConstants {

	public static final int DIAMETER_3GPP_VENDOR_ID = 10415;
	public static final int DIAMETER_APPLICATION_CXDX = 16777216;
	// command codes
	public static final int RAR = 358;
	
	
	// IMS command codes
	public static final int UAR = 300;
	public static final int SAR = 301;
	public static final int LIR = 302;
	// 3GPP public static final int MAR = 303;
    public static final int MAR = 506;                                  // Ericsson
	public static final int RTR = 304;
	public static final int PPR = 305;
	
	
	public static final int UAA = UAR;
	public static final int SAA = SAR;
	public static final int LIA = LIR;
	public static final int MAA = MAR;
	public static final int RTA = RTR;
	public static final int PPA = PPR;
	
	// IMS AVPs codes
	public static final int VISITED_NETWORK_IDENTIFIER = 600;
    // 3GPP public static final int PUBLIC_IDENTITY = 601;
    public static final int PUBLIC_IDENTITY = 2;                        // Ericsson
	public static final int SERVER_NAME = 602;
	public static final int SERVER_CAPABILITIES = 603;
	public static final int MANDATORY_CAPABILITY = 604;
	public static final int OPTIONAL_CAPABILITY = 605;
	public static final int USER_DATA = 606;
	// 3GPP public static final int SIP_NUMBER_AUTH_ITEMS = 607;
    public static final int SIP_NUMBER_AUTH_ITEMS = 1026;                // Ericsson
	// 3GPP public static final int SIP_AUTHENTICATION_SCHEME = 608;
    public static final int SIP_AUTHENTICATION_SCHEME = 1020;            // Ericsson
	// public static final int SIP_AUTHENTICATE = 609;
    public static final int SIP_AUTHENTICATE = 1023;
	// 3GPP public static final int SIP_AUTHORIZATION = 610;
    public static final int SIP_AUTHORIZATION = 1022;                   // Ercisson    
	// 3GPP public static final int SIP_AUTHENTICATION_CONTEXT = 611;
    public static final int SIP_AUTHENTICATION_CONTEXT = 1024;          // Ericsson
    // 3GPP public static final int SIP_AUTH_DATA_ITEM = 612;
    public static final int SIP_AUTH_DATA_ITEM = 1018;                  // Ericsson
    // 3GPP public static final int SIP_ITEM_NUMBER = 613;
    public static final int SIP_ITEM_NUMBER = 1021;                     // Ericsson
	public static final int SERVER_ASSIGNMENT_TYPE = 614;
	public static final int DEREGISTRATION_REASON = 615;
	public static final int REASON_CODE = 616;
	public static final int REASON_INFO = 617;
	public static final int CHARGING_INFORMATION = 618;
	public static final int PRIMARY_EVENT_CHARGING_FUNCTION_NAME = 619;
	public static final int SECONDARY_EVENT_CHARGING_FUNCTION_NAME = 620;
	public static final int PRIMARY_EVENT_CHARGING_COLLECTION_NAME = 621;
	public static final int SECONDARY_EVENT_CHARGING_COLLECTION_NAME = 622;
	public static final int USER_AUTHORIZATION_TYPE = 623;
	public static final int USER_DATA_ALREADY_AVAILABLE = 624;
	public static final int CONFIDENTIALITY_KEY = 625;
	public static final int INTEGRITY_KEY = 626;
	public static final int SUPPORTED_FEATURES = 628;
	public static final int FEATURE_LIST_ID = 629;
	public static final int FEATURE_LIST = 630;
	public static final int SUPPORTED_APPLICATIONS = 631;
	public static final int ASSOCIATED_IDENTITIES = 632;

	// IMS status codes
    public static final int DIAMETER_FIRST_REGISTRATION = 2001;
    public static final int DIAMETER_SUBSEQUENT_REGISTRATION = 2002;
    public static final int DIAMETER_UNREGISTERED_SERVICE = 2003;
    public static final int DIAMETER_SUCCESS_SERVER_NAME_NOT_STORED = 2004;
    public static final int DIAMETER_ERROR_USER_UNKNOWN = 5001;
    public static final int DIAMETER_ERROR_IDENTITIES_DONT_MATCH = 5002;
    public static final int DIAMETER_ERROR_IDENTITY_NOT_REGISTERED = 5003;
    public static final int DIAMETER_ERROR_ROAMING_NOT_ALLOWED = 5004;
    public static final int DIAMETER_ERROR_IDENTITY_ALREADY_REGISTERED = 5005;
    public static final int DIAMETER_ERROR_AUTH_SCHEME_NOT_SUPPORTED = 5006;
    public static final int DIAMETER_ERROR_IN_ASSIGNMENT_TYPE = 5007;
    public static final int DIAMETER_ERROR_TOO_MUCH_DATA = 5008; 
    public static final int DIAMETER_ERROR_NOT_SUPPORTED_USER_DATA = 5009;
    public static final int DIAMETER_ERROR_FEATURE_UNSUPPORTED = 5011;

    
	// IMS Server Assignment Type codes
    public enum ServerAssignmentTypeEnum { 
        _NO_ASSIGNMENT, 
        _REGISTRATION,
        _RE_REGISTRATION, 
        _UNREGISTERED_USER,
        _TIMEOUT_DEREGISTRATION,
        _USER_DEREGISTRATION,
        _TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME,
        _USER_DEREGISTRATION_STORE_SERVER_NAME,
        _ADMINISTRATIVE_DEREGISTRATION, 
        _AUTHENTICATION_FAILURE,
        _AUTHENTICATION_TIMEOUT,
        _DEREGISTRATION_TOO_MUCH_DATA,        
    }    
    public static final int _NO_ASSIGNMENT = 0;
    public static final int _REGISTRATION = 1;
    public static final int _RE_REGISTRATION = 2;
    public static final int _UNREGISTERED_USER = 3;
    public static final int _TIMEOUT_DEREGISTRATION = 4;
    public static final int _USER_DEREGISTRATION = 5;
    public static final int _TIMEOUT_DEREGISTRATION_STORE_SERVER_NAME = 6;
    public static final int _USER_DEREGISTRATION_STORE_SERVER_NAME = 7;
    public static final int _ADMINISTRATIVE_DEREGISTRATION = 8; 
    public static final int _AUTHENTICATION_FAILURE = 9;
    public static final int _AUTHENTICATION_TIMEOUT = 10;
    public static final int _DEREGISTRATION_TOO_MUCH_DATA = 11;
    
    // IMS User Data Already Available codes
    public enum UserDataAlreadyAvailableEnum { 
        _USER_DATA_NOT_AVAILABLE, 
        _USER_DATA_ALREADY_AVAILABLE,
    }    
    public static final int _USER_DATA_NOT_AVAILABLE = 0; 
    public static final int _USER_DATA_ALREADY_AVAILABLE = 1; 

    // IMS Type Of Authorization codes
    public enum TypeOfAuthorizationEnum { 
        _REGISTRATION_, 
        _DE_REGISTRATION_,
        _REGISTRATION_AND_CAPABILITIES_
    }    
    public static final int _REGISTRATION_ = 0; 
    public static final int _DE_REGISTRATION_ = 1; 
    public static final int _REGISTRATION_AND_CAPABILITIES_ = 2;    

}
