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

package com.devoteam.srit.xmlloader.sctp;

import java.util.Collection;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.protocol.Msg;

public class MsgTransportInfosSctp implements Msg.TransportInfos {

	private final InfoSctp infoSctp;
	
	public MsgTransportInfosSctp(){
		this.infoSctp = new BasicInfoSctp();
	}

	public MsgTransportInfosSctp( InfoSctp infoSctp ){
		this.infoSctp = infoSctp;
	}
	
	public InfoSctp getInfoSctp(){
		return this.infoSctp;
	}

	@Override
	public void parseFromXml(Collection<Element> transportInfosElements) throws Exception {		
		this.infoSctp.setFromSctpStackConfig();
		for( Element transportInfosElement:transportInfosElements ){
	       	@SuppressWarnings("unchecked")
	    	Collection<Element> sctpElements = transportInfosElement.elements("sctp");
		    for( Element sctpElement:sctpElements ){
		    	this.infoSctp.setFromXml(sctpElement);
		    }	    
		}
	}

	@Override
	public Parameter getParameter(ParameterKey parameterKey) throws ParameterException {
		Parameter parameter = null;
		if( parameterKey.hasHeadSubkey("sctp") ){
			ParameterKey msgInfoKey = parameterKey.shift();
			parameter = this.infoSctp.getParameter( msgInfoKey );
		}
		else{
			Parameter.throwBadPathKeywordException(parameterKey);
		}
		assert parameter!=null;
		return parameter;
	}

}
