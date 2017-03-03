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

import javax.annotation.Nonnull;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterKey;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.protocol.Channel;

public class ChannelTransportInfosSctp implements Channel.TransportInfos {

	private ChannelConfigSctp channelConfigSctp;
	
	public ChannelTransportInfosSctp(){
		this.channelConfigSctp = new ChannelConfigSctp();
	}
	
	public ChannelTransportInfosSctp( ChannelConfigSctp channelConfigSctp ){
		this.channelConfigSctp = channelConfigSctp;
	}
	
	public ChannelConfigSctp getChannelConfigSctp(){
		return this.channelConfigSctp;
	}

	@Override
	public void parseFromXml(Collection<Element> transportInfosElements) throws Exception {		
		this.channelConfigSctp.setFromSctpStackConfig();
		for( Element transportInfosElement:transportInfosElements ){
	       	@SuppressWarnings("unchecked")
	    	Collection<Element> sctpElements = transportInfosElement.elements("sctp");
		    for( Element sctpElement:sctpElements ){
		    	this.channelConfigSctp.setFromXml(sctpElement);
		    }	    
		}
	}

	@Override
	public Parameter getParameter(ParameterKey parameterKey) throws ParameterException {
		Parameter parameter = null;
		if( parameterKey.hasHeadSubkey("sctp") ){
			ParameterKey channelConfigKey = parameterKey.shift();
			parameter = this.channelConfigSctp.getParameter( channelConfigKey );
		}
		else{
			Parameter.throwBadPathKeywordException(parameterKey);
		}
		assert parameter!=null;
		return parameter;
	}

	@Override
	public boolean equals(Object object) {
		if( !(object instanceof ChannelTransportInfosSctp) ){
			return false;
		}
		ChannelTransportInfosSctp channelTransportInfosSctp = (ChannelTransportInfosSctp)object;
		if( !this.channelConfigSctp.equals(channelTransportInfosSctp.channelConfigSctp) ){
			return false;
		}
		return true;
	}

}
