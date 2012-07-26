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

package com.devoteam.srit.xmlloader.core.operations.basic.operators;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;
import com.devoteam.srit.xmlloader.core.utils.Utils;

import java.util.Map;
import java.util.Vector;

import javax.sdp.BandWidth;
import javax.sdp.Connection;
import javax.sdp.Key;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.RepeatTime;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sdp.TimeDescription;

import java.util.Date;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorSetFromSDP extends AbstractPluggableParameterOperator
{

	private static String OPERATION_TYPE = "setFromSDP";
	
    public PluggableParameterOperatorSetFromSDP()
    {
        this.addPluggableName(new PluggableName(OPERATION_TYPE));
        this.addPluggableName(new PluggableName("protocol." + OPERATION_TYPE));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws ParameterException
    {
        this.normalizeParameters(operands);
               
        Parameter contents = PluggableParameterOperatorList.assertAndGetParameter(operands, "value");
        Parameter pathes = PluggableParameterOperatorList.assertAndGetParameter(operands, "value2");
        Parameter result = new Parameter();
        
        try
        {
            for (int i = 0; i < contents.length(); i++)
            {
                String[] params = Utils.splitPath(pathes.get(i).toString());
            	addSDPParameter(result, params, contents.get(i).toString(), 0, pathes.get(i).toString());
            }
        }
        catch (Exception e)
        {
            throw new ParameterException("Error in setFromSDP operator", e);
        }

        return result;
    }

    public static void addSDPParameter(Parameter var, String[] params, String content, int index, String path) throws Exception
    {       
        content = content.trim();        
		content = Utils.replaceNoRegex(content, "\n", "\r\n");

	    //---------------------------------------------------------------------- origin -
	    if (params[index].equalsIgnoreCase("origin"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
            	if (sessionDesc.getOrigin() != null)
            	{
            		var.add(sessionDesc.getOrigin().toString());
            	}
            }
            else
            {
            	if (sessionDesc.getOrigin() != null)
            	{
            		addSDPOrigin(var, sessionDesc.getOrigin(), params, index + 1, path);
            	}
            }
	    }
	    //---------------------------------------------------------------------- [media]:bandWidth -
	    else if (params[index].equalsIgnoreCase("bandWidth"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
                var.addAll(sessionDesc.getBandwidths(true));
            }
            else
            {
                addSDPBandWidth(var, sessionDesc.getBandwidths(true), params, index + 1, path);
            }
	    }
	    //---------------------------------------------------------------------- [media]:connection -
	    else if (params[index].equalsIgnoreCase("connection"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
                var.add(sessionDesc.getConnection().toString());
            }
            else
            {
                addSDPConnection(var, sessionDesc.getConnection(), params, index + 1, path);
            }	
	    }
	    //---------------------------------------------------------------------- [media]:information -
	    else if (params[index].equalsIgnoreCase("information"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (sessionDesc.getInfo() != null)
            {
            	var.add(sessionDesc.getInfo().toString());
            }
	    }
	    //---------------------------------------------------------------------- [media]:key -
	    else if (params[index].equalsIgnoreCase("key"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
                var.add(sessionDesc.getKey().toString());
            }
            else
            {
                addSDPKey(var, sessionDesc.getKey(), params, index + 1, path);
            }
	    }
	    //---------------------------------------------------------------------- phone -
	    else if (params[index].equalsIgnoreCase("phone"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            var.addAll(sessionDesc.getPhones(true));
	    }
	    //---------------------------------------------------------------------- email -
	    else if (params[index].equalsIgnoreCase("email"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            var.addAll(sessionDesc.getEmails(true));
	    }
	    //---------------------------------------------------------------------- media -
	    else if (params[index].equalsIgnoreCase("media"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            Vector v = sessionDesc.getMediaDescriptions(true);
            if (params.length == index + 1)
            {
                var.addAll(v);
            }
            else
            {
                addSDPMedia(var, v, params, index + 1, path);
            }
	    }
	    //---------------------------------------------------------------------- sessionName -
	    else if (params[index].equalsIgnoreCase("sessionName"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (sessionDesc.getSessionName() != null)
            {
            	var.add(sessionDesc.getSessionName().toString());
            }
	    }
	    //---------------------------------------------------------------------- version -
	    else if (params[index].equalsIgnoreCase("version"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            var.add(Integer.toString(sessionDesc.getVersion().getVersion()));
	    }
	    //---------------------------------------------------------------------- uri -
	    else if (params[index].equalsIgnoreCase("uri"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (sessionDesc.getURI() != null)
            {
            	var.add(sessionDesc.getURI().toString());
            }
	    }
	    //---------------------------------------------------------------------- [media]:time -
	    else if (params[index].equalsIgnoreCase("time"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
                var.addAll(sessionDesc.getTimeDescriptions(true));
            }
            else
            {
                addSDPTimeDescription(var, sessionDesc.getTimeDescriptions(true), params, index + 1, path);
            }
	    }
	    //---------------------------------------------------------------------- zoneAdjustment -
	    else if (params[index].equalsIgnoreCase("zoneAdjustment"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            var.addAll(sessionDesc.getZoneAdjustments(true));
	    }
	    //---------------------------------------------------------------------- repeatTime -
	    else if (params[index].equalsIgnoreCase("repeatTime"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            Vector v = sessionDesc.getTimeDescriptions(true);
            for (int i = 0; i < v.size(); i++)
            {
                if (params.length == index + 1)
                {
                    var.addAll(((TimeDescription) v.get(i)).getRepeatTimes(true));
                }
                else
                {
                    Vector vect = ((TimeDescription) v.get(i)).getRepeatTimes(true);
                    addSDPRepeatTime(var, vect, params, index + 1, path);
                }
            }
	    }
	    //---------------------------------------------------------------------- [media]:attribut -
	    else if (params[index].equalsIgnoreCase("attribut"))
	    {
            SdpFactory factory = SdpFactory.getInstance();
            SessionDescription sessionDesc = factory.createSessionDescription(content);
            if (params.length == index + 1)
            {
                var.addAll(sessionDesc.getAttributes(true));
            //--------------------------------------------------------------- [media]:attribut:Xxxx -
            }
            else if (params.length == index + 2)
            {
                var.add(sessionDesc.getAttribute(params[index + 1]));
            }
	    }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }
    
    /* Get the elements Media sdp of this message */
    private static void addSDPMedia(Parameter var, Vector vect, String[] params, int index, String path) throws Exception
    {
        //---------------------------------------------------------------------- media:port -
        if (params[index].equalsIgnoreCase("port"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                Media m = ((MediaDescription) vect.get(i)).getMedia();
                var.add(Integer.toString(m.getMediaPort()));
            }
        }
        //---------------------------------------------------------------------- media:type -
        else if (params[index].equalsIgnoreCase("type"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                Media m = ((MediaDescription) vect.get(i)).getMedia();
                var.add(m.getMediaType());
            }
        }
        //---------------------------------------------------------------------- media:protocol -
        else if (params[index].equalsIgnoreCase("protocol"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                Media m = ((MediaDescription) vect.get(i)).getMedia();
                var.add(m.getProtocol());
            }
        }
        //---------------------------------------------------------------------- media:information -
        else if (params[index].equalsIgnoreCase("information"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
            	if ((vect.get(i) != null) && (((MediaDescription) vect.get(i)).getInfo() != null))
            	{
            		var.add(((MediaDescription) vect.get(i)).getInfo().getValue());
            	}
            }
        }
        //---------------------------------------------------------------------- media:key -
        else if (params[index].equalsIgnoreCase("key"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                if (params.length == index + 1)
                {
                    var.add(((MediaDescription) vect.get(i)).getKey().toString());
                }
                else
                {
                    addSDPKey(var, ((MediaDescription) vect.get(i)).getKey(), params, index + 1, path);
                }
            }
        }
        //---------------------------------------------------------------------- media:format -
        else if (params[index].equalsIgnoreCase("format"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
            	if (vect.get(i) != null)
            	{
            		Media m = ((MediaDescription) vect.get(i)).getMedia();
            		if (m != null)
            		{
            			var.addAll(m.getMediaFormats(true));
            		}
            	}
            }
        }
        //---------------------------------------------------------------------- media:portCount -
        else if (params[index].equalsIgnoreCase("portCount"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                Media m = ((MediaDescription) vect.get(i)).getMedia();
                var.add(Integer.toString(m.getPortCount()));
            }
        }
        //---------------------------------------------------------------------- media:connection -
        else if (params[index].equalsIgnoreCase("connection"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                if (params.length == 4)
                {
                    var.add(((MediaDescription) vect.get(i)).getConnection().toString());
                }
                else
                {
                    addSDPConnection(var, ((MediaDescription) vect.get(i)).getConnection(), params, index + 1, path);
                }
            }
        }
        //---------------------------------------------------------------------- media:bandWidth -
        else if (params[index].equalsIgnoreCase("bandWidth"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                if (params.length == index + 1)
                {
                    var.addAll(((MediaDescription) vect.get(i)).getBandwidths(true));
                }
                else
                {
                    addSDPBandWidth(var, ((MediaDescription) vect.get(i)).getBandwidths(true), params, index + 1, path);
                }
            }
        }
        //---------------------------------------------------------------------- media:mimeParameters
        else if (params[index].equalsIgnoreCase("mimeParameters"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                var.addAll((((MediaDescription) vect.get(i)).getMimeParameters()));
            }
        }
        //---------------------------------------------------------------------- media:mimeTypes
        else if (params[index].equalsIgnoreCase("mimeTypes"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                var.addAll((((MediaDescription) vect.get(i)).getMimeTypes()));
            }
        }
        //---------------------------------------------------------------------- media:attribut
        else if (params[index].equalsIgnoreCase("attribut"))
        {
            if (params.length == index + 1)
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    var.addAll((((MediaDescription) vect.get(i)).getAttributes(true)));
                }
            }
            else if (params.length == index + 2)
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    String value = ((MediaDescription) vect.get(i)).getAttribute(params[index + 1]);
                    if (null != value)
                    {
                        var.add(value);
                    }
                }
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /** Get the elements TimeDescription sdp of this message */
    private static void addSDPTimeDescription(Parameter var, Vector vect, String[] params, int index, String path) throws Exception
    {
        //---------------------------------------------------------------------- time:start -
        if (params[index].equalsIgnoreCase("start"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
            	TimeDescription td = (TimeDescription) vect.get(i);
            	Date d = (Date) td.getTime().getStart();
            	d = new Date();
                var.add(d.toString());
            }
        }
        //----------------------------------------------------------------------  time:stop -
        else if (params[index].equalsIgnoreCase("stop"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                var.add(((TimeDescription) vect.get(i)).getTime().getStop().toString());
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /**Get the elements Key sdp of this message */
    private static void addSDPKey(Parameter var, Key key, String params[], int index, String path) throws Exception
    {
        if (params.length == index + 1)
        {
            //---------------------------------------------------------------------- key:value -
            if (params[index].equalsIgnoreCase("value"))
            {
                var.add(key.getKey());
            }
            //----------------------------------------------------------------------  key:method -
            else if (params[index].equalsIgnoreCase("method"))
            {
                var.add(key.getMethod());
            }
        }
        else if (params.length == index + 2)
        {
            //---------------------------------------------------------------------- media:key:value -
            if (params[index + 1].equalsIgnoreCase("value"))
            {
                var.add(key.getKey());
            }
            //----------------------------------------------------------------------  media:key:method -
            else if (params[index + 1].equalsIgnoreCase("method"))
            {
                var.add(key.getMethod());
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /** Get the elements SessionName sdp of this message */
    private static void addSDPOrigin(Parameter var, Origin ori, String params[], int index, String path) throws Exception
    {
        //---------------------------------------------------------------------- origin:address -
        if (params[index].equalsIgnoreCase("address"))
        {

            var.add(ori.getAddress());
        }
        //----------------------------------------------------------------------  origin:addressType -
        else if (params[index].equalsIgnoreCase("addressType"))
        {
            var.add(ori.getAddressType());
        }
        //----------------------------------------------------------------------  origin:networkType -
        else if (params[index].equalsIgnoreCase("networkType"))
        {
            var.add(ori.getNetworkType());
        }
        //----------------------------------------------------------------------  origin:sessionId -
        else if (params[index].equalsIgnoreCase("sessionId"))
        {
            var.add(Long.toString(ori.getSessionId()));
        }
        //----------------------------------------------------------------------  origin:sessionVersion -
        else if (params[index].equalsIgnoreCase("sessionVersion"))
        {
            var.add(Long.toString(ori.getSessionVersion()));
        }
        //----------------------------------------------------------------------  origin:userName -
        else if (params[index].equalsIgnoreCase("userName"))
        {
            var.add(ori.getUsername());
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /** Get the elements bandwidth sdp of this message */
    private static void addSDPBandWidth(Parameter var, Vector vect, String[] params, int index, String path) throws Exception
    {
        if (params.length == index + 1)
        {
            //---------------------------------------------------------------------- bandWidth:type -
            if (params[index].equalsIgnoreCase("type"))
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    var.add(((BandWidth) vect.get(i)).getType());
                }
            }
            //---------------------------------------------------------------------- bandWidth:value -
            else if (params[index].equalsIgnoreCase("value"))
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    var.add(Integer.toString(((BandWidth) vect.get(i)).getValue()));
                }
            }
        }
        //----------------------------------------------------------------------  media:bandWidth:value -
        else if (params.length == index + 2)
        {
            if (params[index + 1].equalsIgnoreCase("value"))
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    var.add(Integer.toString(((BandWidth) vect.get(i)).getValue()));
                }
            }
            //---------------------------------------------------------------------- media:bandWidth:type -
            else if (params[index + 1].equalsIgnoreCase("type"))
            {
                for (int i = 0; i < vect.size(); i++)
                {
                    var.add(((BandWidth) vect.get(i)).getType());
                }
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /** Get the elements Connection sdp of this message */
    private static void addSDPConnection(Parameter var, Connection conn, String[] params, int index, String path) throws Exception
    {
        if (params.length == index + 1)
        {
            //---------------------------------------------------------------------- connection:address -
            if (params[index].equalsIgnoreCase("address"))
            {

                var.add(conn.getAddress());
            }
            //---------------------------------------------------------------------- connection:addressType-
            else if (params[index].equalsIgnoreCase("addressType"))
            {
                var.add(conn.getAddressType());
            }
            //---------------------------------------------------------------------- connection:networkType-
            else if (params[index].equalsIgnoreCase("networkType"))
            {
                var.add(conn.getNetworkType());
            }
        }
        else if (params.length == index + 2)
        {
            //---------------------------------------------------------------------- media:connection:address-
            if (params[index + 1].equalsIgnoreCase("address"))
            {

                var.add(conn.getAddress());
            }
            //---------------------------------------------------------------------- media:connection:addressType-
            else if (params[index + 1].equalsIgnoreCase("addressType"))
            {
                var.add(conn.getAddressType());
            }
            //---------------------------------------------------------------------- media:connection:networkType-
            else if (params[index + 1].equalsIgnoreCase("networkType"))
            {
                var.add(conn.getNetworkType());
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }

    /** Get the elements repeatTime sdp of this message */
    private static void addSDPRepeatTime(Parameter var, Vector vect, String[] params, int index, String path) throws Exception
    {
        //---------------------------------------------------------------------- repeatTime:duration -
        if (params[index].equalsIgnoreCase("duration"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                var.add(Integer.toString(((RepeatTime) vect.get(i)).getActiveDuration()));
            }
        }
        //---------------------------------------------------------------------- repeatTime:interval -
        else if (params[index].equalsIgnoreCase("interval"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                var.add(Integer.toString(((RepeatTime) vect.get(i)).getRepeatInterval()));
            }
        }
        //---------------------------------------------------------------------- repeatTime:offset -
        else if (params[index].equalsIgnoreCase("offset"))
        {
            for (int i = 0; i < vect.size(); i++)
            {
                int[] offset = ((RepeatTime) vect.get(i)).getOffsetArray();
                for (int j = 0; j < offset.length; j++)
                {
                    var.add(Integer.toString(offset[j]));
                }
            }
        }
        else
        {
        	Parameter.throwBadPathKeywordException(OPERATION_TYPE, path);
        }
    }
}
