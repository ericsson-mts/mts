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

package com.devoteam.srit.xmlloader.core.newstats;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

import com.devoteam.srit.xmlloader.core.newstats.parameter.GraphParameters;
import com.devoteam.srit.xmlloader.core.newstats.parameter.HistogramParameters;
import com.devoteam.srit.xmlloader.core.newstats.parameter.StatCounterParameters;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.filesystem.SingletonFSInterface;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Provides necessary information required during the process of counter creation
 * and generation of the reports;
 * = counter presentation
 * 
 * @author mjagodzinski
 * 
 */
public class StatCounterConfigManager
{

    private static StatCounterConfigManager instance = null;
    
    private Map<String, List<CounterReportTemplate>> templateMapList = null;
    
    private static String FORMATTER_DIR = "../conf/stats/";
    
    private String CSV_SEPARATOR = ";";

    private StatCounterConfigManager()
    {
        templateMapList = new HashMap<String, List<CounterReportTemplate>>();
        List<CounterReportTemplate> templateList = new LinkedList<CounterReportTemplate>();
        templateMapList.put(StatPool.PREFIX_USER, templateList);
        templateList = new LinkedList<CounterReportTemplate>();
        templateMapList.put(StatPool.PREFIX_PARAM, templateList);
    }

    public synchronized static StatCounterConfigManager getInstance()
    {
    	if (instance == null)
    	{
    		instance = new StatCounterConfigManager();
    	}
        return instance;
    }

    public synchronized static void resetInstance()
    {
    	instance = null;
    }

    /**
     * Reads from configuration file counter parameters.
     * 
     * @param key
     *            pattern that identifies a counter
     * @return
     */
    public synchronized StatCounterParameters getCounterParameters(StatKey key)
    {

        // we use current timestamp to mark the test start; normally this value
        // must be overriden by StatPool object using setTestStartTimestamp
        // method
        int graphArrayLength = Config.getConfigByName("tester.properties").getInteger("stats.GRAPH_ARRAY_LENGTH", 256);
        // top be as nearest as possible to the request number of points 
        graphArrayLength = graphArrayLength * 4 / 3;
        GraphParameters graphParameters = new GraphParameters(graphArrayLength, 50);

        double[] x = {Double.MIN_VALUE, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2, 5, 10, 20, 50, 100, 200, 500, 1000, Double.MAX_VALUE};
        HistogramParameters histogramParameters = new HistogramParameters( x);

        StatCounterParameters statCounterParameters = new StatCounterParameters(key, graphParameters, histogramParameters, null, null);

        return statCounterParameters;
    }

    /**
     * Returns a template filename for a StatKey object by testing the 
     * file template existing 
     * 
     * @param filename
     */
    public synchronized String getTemplateFilename(StatKey level) throws Exception
    {
    	/** TODO */
    	String key = level.getAttribute(0);
    	int i;
    	for (i = level.getAttributesLength(); i > 0; i--)
    	{       		
    		if (SingletonFSInterface.instance().exists(new URI(FORMATTER_DIR + key + "_" + i + ".csv")))
    		{
    			key = key + "_" + i;
    			break;
    		}
    	}
    	if (i <= 0)
    	{
    		key = level.getAttribute(0);    		
    	}
        return key;
    }    
    
    /**
     * Returns a list of counters depending on the requested level
     * 
     * @param level
     */
    public synchronized List<CounterReportTemplate> getTemplateList(StatKey level) throws Exception
    {
        String key = getTemplateFilename(level);
        
        // search in the template map list
        List<CounterReportTemplate> templateList = templateMapList.get(key);
        if (templateList != null)
        {
            return templateList;
        }
        templateMapList.put(key, templateList);
        
        String fileName = FORMATTER_DIR + key + ".csv";
        CSVReader csvReader = new CSVReader(new InputStreamReader(SingletonFSInterface.instance().getInputStream(new URI(fileName))), CSV_SEPARATOR.charAt(0));
        List<String[]> list = csvReader.readAll();

        templateList = new LinkedList<CounterReportTemplate>();
        int index = 0;

        for (String[] line : list)
        {
            if (index > 0)
            {
                String type = line[0];
                StatKey arg1 = new StatKey(Utils.splitNoRegex(line[1].substring(1),"/"));

                StatKey arg2 = null;
                if (line[2].length() > 0)
                {
                    arg2 = new StatKey(Utils.splitNoRegex(line[2].substring(1),"/"));
                }

                String descrShort = line[3];
                String description = line[4];                
                String descrLong = line[5];
                CounterReportTemplate template = new CounterReportTemplate(type, arg1, arg2, descrShort, description, descrLong);
                templateList.add(template);
            }
            index++;
        }

        return templateList;
    }

    /**
     * Add a new template to the list of counters
     * 
     * @param level
     */
    public synchronized void addTemplateList(StatKey level, CounterReportTemplate template)
    {
        String key = level.getAttribute(0);
        List<CounterReportTemplate> templateList = templateMapList.get(key);
        if (!templateList.contains(template))
        {
            templateList.add(template);
        }
    }
    
    /**
     * Add a new template to the list of counters
     * 
     * @param level
     */
    public synchronized boolean containsTemplateList(StatKey level, CounterReportTemplate template)
    {
        String key = level.getAttribute(0);
        List<CounterReportTemplate> templateList = templateMapList.get(key);  
        if (templateList.contains(template))
        {
        	return true;
        }
        return false;
    }

}
