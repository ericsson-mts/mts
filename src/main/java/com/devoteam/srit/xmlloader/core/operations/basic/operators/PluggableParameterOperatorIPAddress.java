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

import java.util.Map;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;

/**
 *
 * @author gpasquiers
 */
public class PluggableParameterOperatorIPAddress extends AbstractPluggableParameterOperator {

    final private String S_IPLIST = "system.iplist";

    public PluggableParameterOperatorIPAddress() {
        this.addPluggableName(new PluggableName(S_IPLIST));
    }

    @Override
    public Parameter operate(Runner runner, Map<String, Parameter> operands, String name, String resultant) throws Exception {
        normalizeParameters(operands);
        resultant = ParameterPool.unbracket(resultant);

        Parameter result = new Parameter();

        Parameter value = operands.get("value");
        Parameter value2 = operands.get("value2");

        for(int i=0; i<value.length(); i++){

            String subnet = value.get(i).toString();

            String[] subnetSplit;
            String ip;
            String mask;

            if(subnet.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\/[0-9]{2}")){
                subnetSplit = subnet.split("/");
                ip = subnetSplit[0];
                mask = subnetSplit[1];
            }
            else if(subnet.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")){
                if(null != value2){
                    mask = value2.get(i).toString();
                    if(mask.matches("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}")){
                        ip = subnet;
                        String[] maskSplit = mask.split("\\.");
                        long maskLong = 0;
                        maskLong += Long.parseLong(maskSplit[3]);
                        maskLong += Long.parseLong(maskSplit[2]) * 256;
                        maskLong += Long.parseLong(maskSplit[1]) * 256 * 256;
                        maskLong += Long.parseLong(maskSplit[0]) * 256 * 256 * 256;

                        long oneCount = 0;
                        long zeroCount = 0;

                        for(int j=0; j<32; j++){
                            long bit = maskLong & 0x01;
                            if(bit == 0 && oneCount > 0){
                                throw new ExecutionException("invalid value for mask (value2) " + mask + ", should be AAA.BBB.CCC.DDD");
                            }
                            else if(bit == 0){
                                zeroCount ++;
                            }
                            else if(bit == 1){
                                oneCount ++;
                            }
                            maskLong /= 2;
                        }

                        mask = Long.toString(oneCount);
                    }
                    else{
                        throw new ExecutionException("invalid format for mask (value2)" + mask + ", should be AAA.BBB.CCC.DDD");
                    }
                }
                else{
                    throw new ExecutionException("missing value2 with mask value AAA.BBB.CCC.DDD");
                }
            }
            else{
                throw new ExecutionException("invalid format for subnet " + subnet + ", should be AAA.BBB.CCC.DDD/MM or AAA.BBB.CCC.DDD with value2 defined");
            }

            
            String[] ipSplit = ip.split("\\.");

            long maskLong = (long) Math.pow(2, 32 - Integer.parseInt(mask));

            long ipLong = 0;
            ipLong += Long.parseLong(ipSplit[3]);
            ipLong += Long.parseLong(ipSplit[2]) * 256;
            ipLong += Long.parseLong(ipSplit[1]) * 256 * 256;
            ipLong += Long.parseLong(ipSplit[0]) * 256 * 256 * 256;
            long netLong = (ipLong / maskLong) * maskLong;

            for(int j=1; j<maskLong - 1; j++){
                long currentIpLong = netLong + j;
                String currentIp = (currentIpLong/256/256/256 % 256) + "." + (currentIpLong/256/256 % 256) + "." + (currentIpLong/256 % 256) + "." + (currentIpLong % 256);
                result.add(currentIp);
            }
        }
        return result;
    }
}
