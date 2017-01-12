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

package com.devoteam.srit.xmlloader.rtp.flow;

import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.CSVReader;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rbarbot
 */
public class MosParameters {
    static private ParamsConfig instance = null;

    static public ParamsConfig instance()
    {
    	if (instance == null)
    	{
    		try {
    			instance = new ParamsConfig();
    		}
    		catch (Exception e)
    		{
    			
    		}
    	}
        return instance;
    }

    public static class ParamsConfig {
        public final static int   name     = 0;
        public final static int   PT       = 0;
        public final static int   SLR      = 1;
        public final static int   RLR      = 2;
        public final static int   STMR     = 3;
        public final static int   LSTR     = 4;
        public final static int   DS       = 5;
        public final static int   DR       = 6;
        public final static int   TELR     = 7;
        public final static int   WEPL     = 8;
        public final static int   QDU      = 9;
        public final static int   IE       = 10;
        public final static int   BPL      = 11;
        public final static int   BurstR   = 12;
        public final static int   NC       = 13;
        public final static int   NFOR     = 14;
        public final static int   PS       = 15;
        public final static int   PR       = 16;
        public final static int   A        = 17;

        private CSVReader csvFile;
        private HashMap<String, String[]> params;
        private HashMap<String, String[]> paramsPT; // PayloadType as key

        public ParamsConfig() throws Exception {
            params = new HashMap<String, String[]>();
            paramsPT = new HashMap<String, String[]>();
            URI uri = URIRegistry.MTS_BIN_HOME.resolve("../conf/rtpflow/mos_parameters.csv");

            csvFile = new CSVReader("#", ";", "''");

            for (String[] data:csvFile.loadAllData(uri)) {
                String[] codec = new String[data.length-1];
                String[] codecPT = new String[data.length-1];
                for (int i=1, max=data.length; i<max; i++) {
                    codec[i-1] = data[i];
                    codecPT[i-1] = data[i];
                }
                codecPT[0] = data[0];
                params.put(data[0], codec);
                paramsPT.put(data[1], codecPT);
            }
        }

        public HashMap<String, String[]> getParams() {
            return params;
        }

        public String[] getCodecParamsbyName(String name) {
            if (params.containsKey(name)) {
                return params.get(name);
            }
            return null;
        }

        public String[] getCodecParamsbyPT(int payloadType) {
            String pt = payloadType+"";
            if (paramsPT.containsKey(pt)) {
                return paramsPT.get(pt);
            }
            return null;
        }
    }
}

