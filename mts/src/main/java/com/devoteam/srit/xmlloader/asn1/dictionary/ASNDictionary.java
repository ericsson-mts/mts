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

package com.devoteam.srit.xmlloader.asn1.dictionary;

import java.util.HashMap;
import java.util.List;


/**
 *
 * @author fhenry
 */
public class ASNDictionary 
{

	private static ASNDictionary _instance;
    
	// list of embedded objects
	private static EmbeddedMap embeddedList;
	
    public ASNDictionary()
    {
    	// TCAP dico
		embeddedList = new EmbeddedMap();
		Embedded embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueOC", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.ExternalPDU",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.EmbeddedData", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.UniDialoguePDU",
				"oidString=0.0.17.773.1.2.1"); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.EmbeddedData", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialoguePDU",
				"oidString=0.0.17.773.1.1.1"); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.ObjectId", 
				"org.bn.types.ObjectIdentifier",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssResult", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_result",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssSourceDiagnostic", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_source_diagnostic",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceUser", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_user",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.AssResult", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Associate_result",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_provider",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_provider",
				null); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.DialogueServiceProvider", 
				"com.devoteam.srit.xmlloader.sigtran.ap.tcap.Dialogue_service_provider",
				null); 
		embeddedList.addEmbedded(embedded);
		// MAP dico
		embedded = new Embedded(
				"invokeparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.Mo_forwardSM_Arg",
				//"OperationLocalvalue=46");
				"value=46");
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"invokeparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.Mo_forwardSM_Arg",
				"OperationLocalvalue=46");
				//"localValue=46");
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"returnparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.Mo_forwardSM_Res",
				//"OperationLocalvalue=46");
				"localValue=46");
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"invokeparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.ProvideSubscriberLocation_Arg",
				"OperationLocalvalue=83"); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"returnparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.ProvideSubscriberLocation_Res",
				"OperationLocalvalue=83"); 
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"invokeparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.RoutingInfoForLCS_Arg",
				"OperationLocalvalue=85");
		embeddedList.addEmbedded(embedded);
		embedded = new Embedded(
				"returnparameter", 
				"com.devoteam.srit.xmlloader.sigtran.ap.map.RoutingInfoForLCS_Res",
				"OperationLocalvalue=85");
		embeddedList.addEmbedded(embedded);
    }
	
	public static ASNDictionary getInstance()
    {
    	if (_instance == null)
    	{
    		_instance = new ASNDictionary();
    	}
    	return _instance;
    }
	
    public Embedded getEmbeddedByInitial(String initial) 
	{
		return embeddedList.getEmbeddedByInitial(initial);
	}

    public List<Embedded> getEmbeddedByCondition(String condition) 
	{
    	return embeddedList.getEmbeddedByCondition(condition);
	}

}
