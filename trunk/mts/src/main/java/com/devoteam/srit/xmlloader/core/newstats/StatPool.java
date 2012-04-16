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

import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.protocol.StackFactory;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.DateUtils;
import com.devoteam.srit.xmlloader.core.utils.Helper;
import com.devoteam.srit.xmlloader.gui.model.ModelTreeRTStats;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import java.util.Map.Entry;
import java.util.Timer;

import org.dom4j.Element;

/**
 * The main Stat class. Holds the counter objects (that implement IStatCounter
 * interface) in th hash map indexed by objects of
 * {@link com.devoteam.srit.xmlloader.core.newstats.StatKey} type The object of
 * this class is a singletone for now (use getInstance method).<br/> Supported
 * counters:<br/> - "counter" - an ordinary event counter<br/> - "value" -
 * mean value counter (short period mean value, overal mean value)<br/> -
 * "flow" - flow of events per second counter<br/> - "percent" - percentage of events
 * counter<br/>
 * 
 * @author mjagodzinski
 */
public class StatPool implements Serializable, Cloneable
{
    // StatPool will be singleton for now
    private final static StatPool instance = new StatPool();
    public static final String PREFIX_REQUEST = "request";
    public static final String PREFIX_RESPONSE = "response";    
    public static final String PREFIX_TRANSACTION = "transaction";
    public static final String PREFIX_SESSION = "session";
    public static final String PREFIX_PROTOCOL = "protocol";
    public static final String PREFIX_TRANSPORT = "transport";
    public static final String PREFIX_RTPFLOW = "rtpflow";
    public static final String PREFIX_TEST = "test";
    public static final String PREFIX_TESTCASE = "testcase";
    public static final String PREFIX_SCENARIO = "scenario";
    public static final String PREFIX_OPERATION = "operation";
    public static final String PREFIX_PARAM = "parameter";
    public static final String PREFIX_USER = "user";

	public static final String LISTENPOINT_KEY = "listenpoint";    
	public static final String CHANNEL_KEY = "channel";
	public static final String PROBE_KEY = "probe";

	public static final String NIO_KEY = "_nio";    
	public static final String BIO_KEY = "_bio";
	
    private HashMap<StatKey, IStatCounter> statHash;
    private long zeroTimestamp;
    private long lastTimestamp;
    private boolean activate;
    private boolean updateLastTimestamp;
    
    private StatPool()
    {
        this.statHash = new HashMap<StatKey, IStatCounter>();
        this.zeroTimestamp = System.currentTimeMillis();
        this.lastTimestamp = 0;
        this.activate = Config.getConfigByName("tester.properties").getBoolean("stats.ACTIVATE_COUNTERS", true);
        this.updateLastTimestamp = true;
    }

    //prepare the timer to automatically generate periodically report
    public static void initialize(String aMode)
    {   
        try
        {
            Float statsPeriodFloat = null;
            String statsPeriod = Config.getConfigByName("tester.properties").getString("stats.GENERATE_AUTO_PERIOD");
            if((statsPeriod != null) && (statsPeriod.length() != 0))
                statsPeriodFloat = Float.parseFloat(statsPeriod);
            
            if(statsPeriodFloat != null)
            {
                String statsDate = Config.getConfigByName("tester.properties").getString("stats.GENERATE_AUTO_TIME");
            	long firstExecutionCalendar = DateUtils.parseDate(statsDate);
                Date firstExecutionDate = new Date(firstExecutionCalendar);
                Date currentDate = new Date();

                int period = (int) (statsPeriodFloat * 3600 * 1000);
                while(firstExecutionDate.before(currentDate))
                {
                    firstExecutionDate = new Date(firstExecutionDate.getTime() + period);
                }

                //lauch the timer now
                Timer autoStatsGenerationTimer = new Timer();
                StatTimerTask task = new StatTimerTask(aMode);
                autoStatsGenerationTimer.scheduleAtFixedRate(task, firstExecutionDate, period);
            }
        }
        catch(Exception e)
        {
            GlobalLogger.instance().getApplicationLogger().warn(TextEvent.Topic.CORE, "Error while configuring automatic generation report: ", e);
        }
    }

    public void setUpdateLastTimestamp(boolean updateLastTimestamp)
    {
        this.updateLastTimestamp = updateLastTimestamp;
    }
        
    /**
     * Returns THE instance of StatPool object (singletone object)
     */
    public static StatPool getInstance()
    {
        return instance;
    }

    public long relativeTimeMillis()
    {
        return System.currentTimeMillis() - this.zeroTimestamp;
    }
    
    public long getZeroTimestamp()
    {
        return this.zeroTimestamp;
    }

    public long getLastTimestamp()
    {
        return this.lastTimestamp;
    }
    
    /**
     * Empties the pool of stats (empties the hash map)
     */
    public void reset()
    {
    	synchronized (this.statHash)
    	{
			// reset the stat pool
		    this.statHash.clear();
    	}
        // reset the presentation counter manager
        StatCounterConfigManager.resetInstance();
        this.zeroTimestamp = System.currentTimeMillis();
        this.lastTimestamp = 0;
        this.activate = Config.getConfigByName("tester.properties").getBoolean("stats.ACTIVATE_COUNTERS", true);
        this.updateLastTimestamp = true;
        
        // reset the gui panel
        if(ModelTreeRTStats.instance()!=null)
        {
            ModelTreeRTStats.instance().resetStats();
        }
    }

    /**
     * Empties a counter into the pool of stats
     */
    public void resetPattern(StatKeyPattern statKeyPattern)
    {
        List<StatKey> findKeys = findStatKey(statKeyPattern);
        for (StatKey statKey : findKeys)
        {
        	synchronized (this.statHash)
        	{
    			// reset the stat pool
    		    this.statHash.remove(statKey);
        	}
        }
    }

    
    /**
     * Test whether a stat counter exists or not
     * 
     */
    public boolean exists(StatKey statKey)
    {
    	return this.statHash.get(statKey) != null;
    }

    
    /**
     * Add a value to a counter (IStatCounter object) given by a StatKey object
     * 
     */
    public void addValue(StatKey statKey, Object value)
    {
        if (activate)
        {
            get(statKey).addValue(value);
            
            // We mark it with a flag on the RTStatsTimer
            RTStatsTimer.shouldRefresh();
        }
    }

    /**
     * Add a value to a counter (IStatCounter object) given by a StatKey object
     *
     */
    public void addValue(StatKey statKey, Object value, long timestamp)
    {
        if (activate)
        {
            get(statKey).addValue(value, timestamp);

            // We mark it with a flag on the RTStatsTimer
            RTStatsTimer.shouldRefresh();
        }
    }
    /**
     * Get the counter (IStatCounter object) given by a StatKey object
     * 
     */
    public IStatCounter getValue(StatKey statKey)
    {
        if (activate)
        {
            return get(statKey);
        }
        return null;
    }

    /**
     * Returns the IStatCounter object from the hash map. If the counter indexed
     * by the StatKey having <code>attributes</code> doesn't exist it creates
     * one.<br/>
     * 
     * The last element in the <code>attributes</code> array indicates the
     * type of the counter, so it ALWAYS is obligatory.<br/> Supported
     * counters:<br/> - "counter" - an ordinary event counter<br/> - "value" -
     * mean value counter (short period mean value, overal mean value)</br> -
     * "flow" - flow of events per second counter<br/> - "percent" - percentage of
     * events per hour counter<br/>
     */
    private IStatCounter get(StatKey statKey)
    {
        return get(statKey, true);
    }

    private IStatCounter get(StatKey statKey, boolean shouldBeRefreshed)
    {
        if (statKey == null)
        {
            throw new RuntimeException("Do not search in StatPool with a null StatKey !");
        }

        if(this.updateLastTimestamp)
        {
            this.lastTimestamp = this.relativeTimeMillis();
        }
        
        IStatCounter counter = statHash.get(statKey);
        if (counter != null)
        {
            return counter;
        }

        // The counter was not found. It will be created and added to the
        // HashMap depending on the boolean shouldBeRefreshed.

        // Search again. If we were stuck on the synchronized(..), it is
        // possible that it was to get the counter that was just created.
        // Since we don't want to create it a second time, we search.
        counter = statHash.get(statKey);
        if (null != counter)
        {
            return counter;
        }

        // Create the counter.
        counter = new StatCounter(statKey, StatCounterConfigManager.getInstance().getCounterParameters(statKey));

        if (true == shouldBeRefreshed)
        {
            synchronized (this.statHash)
            {                	
            	statHash.put(statKey, counter);
            }
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "Created counter ", statKey.toString());
        }
        return counter;
    }

    /**
     * Returns a list of StatKeys that match a StatKeyPattern.
     * If a StatKey is longer thant the StatKeyPattern, only the firsts
     * parameters are tested. (LAX matching).
     * 
     * @param statKeyPattern StatKey object with regular expressions in its attributes
     * @return List of the matched StatKeys from the StatPool
     */
    public List<StatKey> findStatKey(StatKeyPattern statKeyPattern)
    {    	
        List<StatKey> keyResultList = new LinkedList<StatKey>();

        synchronized (this.statHash)
        {
        	Set<StatKey> setStats = this.statHash.keySet();
            for (StatKey statKey : setStats)
            {
                if (statKeyPattern.matchesLax(statKey))
                {
                    keyResultList.add(statKey);
                }
            }
        }
                
        return keyResultList;
    }

    /**
     * Returns a list of StatKeys with the attributes that match the
     * statKeyPattern. Duplicates are eliminated.
     * 
     * Result StatKeys are truncatured to the length of the statKeyPattern
     * 
     * This can be used for example to get a list of protocols by calling this
     * function with /protocol/.* <br/> Could return: <br/> /protocol/sip <br/>
     * /protocol/aaa <br/> /protocol/http <br/> etc<br/>
     * 
     * @param statKeyPattern
     * @return
     */
    public List<StatKey> findMatchingKeyStrict(StatKey statKeyPattern)
    {
        // find all matches
        List<StatKey> findList = this.findStatKey(new StatKeyPattern(statKeyPattern));

        Set<StatKey> findSet = new HashSet<StatKey>();

        String[] array;

        // eliminate the attributes that doesn't match
        for (Iterator<StatKey> iterator = findList.iterator(); iterator.hasNext();)
        {
            StatKey statKey = iterator.next();
            array = new String[statKeyPattern.getAllAttributes().length];
            System.arraycopy(statKey.getAllAttributes(), 0, array, 0, statKeyPattern.getAllAttributes().length);
            findSet.add(new StatKey(array));
        }
        return new LinkedList<StatKey>(findSet);
    }

    public List<IStatCounter> findCounter(StatKeyPattern statKeyPattern)
    {

        List<IStatCounter> result = new LinkedList<IStatCounter>();

        List<StatKey> statKeys = findStatKey(statKeyPattern);
        for (StatKey statKey : statKeys)
        {
            result.add(this.get(statKey));
        }

        return result;
    }

    /**
     * Returns a IStatCounter that is a sum of all IStatCounters selected by
     * <i>statKeyPattern</i> We suppose that all selected counters are of the
     * same type
     * 
     * @param statKeyPattern
     * @return
     */
    public IStatCounter sum(StatKey resultStat, StatKey statKeyPattern)
    {
        List<IStatCounter> findResult = findCounter(new StatKeyPattern(statKeyPattern));        
        if (findResult.size() == 0)
        {
            return null;
        }

        IStatCounter resStatCounter = this.get(resultStat, false);
        for (IStatCounter statCounter : findResult)
        {
            resStatCounter.sum(statCounter);
        }

        return resStatCounter;
    }

//-------------------------------------------------------------
// AJOUT POUR STATS REAL TIMES
    public IStatCounter sumReadOnly(StatKey resultStat, StatKey statKeyPattern)
    {
        List<IStatCounter> findResult = findCounterReadOnly(new StatKeyPattern(statKeyPattern));
        if (findResult.size() == 0)
        {
            return null;
        }

        IStatCounter resStatCounter = this.get(resultStat, false);
        for (IStatCounter statCounter : findResult)
        {
            resStatCounter.sum(statCounter);
        }

        return resStatCounter;
    }

//-------------------------------------------------------------
    public List<IStatCounter> findCounterReadOnly(StatKeyPattern statKeyPattern)
    {

        List<IStatCounter> result = new LinkedList<IStatCounter>();

        List<StatKey> statKeys = findStatKey(statKeyPattern);
        for (StatKey statKey : statKeys)
        {
            result.add(this.get(statKey, false));
        }

        return result;
    }
//-------------------------------------------------------------
//-------------------------------------------------------------
    @Override
    public StatPool clone()
    {
        StatPool clone = new StatPool();
        clone.activate = this.activate;
        clone.zeroTimestamp = this.zeroTimestamp;
        clone.lastTimestamp = this.lastTimestamp;
        clone.updateLastTimestamp = this.updateLastTimestamp;
        synchronized (this.statHash)
        {
            for(Entry<StatKey, IStatCounter> entry : this.statHash.entrySet())
            {
                clone.statHash.put(entry.getKey(), entry.getValue().clone());
            }
            clone.normalize();
        }
        
        return clone;
    }

    public void merge(StatPool other)
    {
        this.lastTimestamp = Math.max(this.lastTimestamp, other.lastTimestamp);
        
        synchronized (this.statHash)
        {
            synchronized (other.statHash)
            {
                for (Entry<StatKey, IStatCounter> entry : other.statHash.entrySet())
                {
                    IStatCounter statCounter = this.statHash.get(entry.getKey());

                    if (null == statCounter)
                    {
                        this.statHash.put(entry.getKey(), entry.getValue().clone());
                    }
                    else
                    {
                        statCounter.sum(entry.getValue());
                    }
                }
            }
        }
    }
    
    public void normalize()
    {
        for (Entry<StatKey, IStatCounter> entry : this.statHash.entrySet())
        {
            StatCounter statCounter = (StatCounter) entry.getValue();
            if(null != statCounter.graphDataset)
            {
                statCounter.graphDataset.compressGraphArray(this.lastTimestamp);
            }
        }
    }
    
    public long getTestStartTimestamp()
    {
        return zeroTimestamp;
    }

    public boolean isActivate()
    {
        return activate;
    }
    
	public static void beginStatisticProtocol(String object, String nio, String transport, String protocol)
	{
		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_PROTOCOL, object + nio, transport, protocol, "_startNumber"), 1);
		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_PROTOCOL, object + nio, transport, protocol, "_currentNumber"), 1);
	}
	
	public static void endStatisticProtocol(String object, String nio, String transport, String protocol, long startTimestamp)
	{
	    long endTimestamp = System.currentTimeMillis();
	    float duration_stats = ((float) (endTimestamp - startTimestamp) / 1000);        

		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_PROTOCOL, object + nio, transport, protocol, "_completeNumber"), 1);
		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_PROTOCOL, object + nio, transport, protocol, "_currentNumber"), -1);
		StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_PROTOCOL, object + nio, transport, protocol, "_durationTime"), duration_stats);
	}

	
    /**
     * add the static stat counters : specially for the editable parameters and test sections
     */
    public void addStatsStaticTestParameters(Test test)
    {
    	this.addValue(new StatKey(StatPool.PREFIX_TEST, test.getName(), "_name"), test.getName());
    	this.addValue(new StatKey(StatPool.PREFIX_TEST, test.getName(), "_description"), test.getDescription());
    	this.addValue(new StatKey(StatPool.PREFIX_TEST, test.getName(), "_filename"), test.getXMLDocument().getXMLFile().toASCIIString());
    	this.addValue(new StatKey(StatPool.PREFIX_TEST, test.getName(), "_version"), Tester.getRelease());
        
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        this.addValue(new StatKey(StatPool.PREFIX_TEST, "_currentTime"), dateFormat.format(this.getZeroTimestamp() + this.getLastTimestamp()));
        this.addValue(new StatKey(StatPool.PREFIX_TEST, "_currentDuration"), Helper.getElapsedTimeString(this.getLastTimestamp() / 1000));
        if (this.isActivate())
        {
            this.addValue(new StatKey(StatPool.PREFIX_TEST, "_currentInterval"), 0);
            float lInterval = (float) ((StatCounter) this.getValue(new StatKey(StatPool.PREFIX_TEST, "_currentInterval"))).graphDataset.graphParameters.graphPeriod / 1000;
            String interval = lInterval + " s";
            this.addValue(new StatKey(StatPool.PREFIX_TEST, "_currentInterval"), interval);
        }
        
    	// We add stats about configuration of the test -------------------------
    	List<Element> list = test.getEditableParameters();
    	for (Element elementEditable : list) {
	        String name = elementEditable.attributeValue("name");
	        name = name.substring(1, name.length() - 1);
	        String description = elementEditable.attributeValue("description");
	        String value = elementEditable.attributeValue("value");
	        StatKey statKey = new StatKey(StatPool.PREFIX_PARAM, "value", name, "_value");
	        addValue(statKey, value);
	        // create the corresponding template
	        CounterReportTemplate template = new CounterReportTemplate("<text>", statKey, null, name, name, description);
	        StatCounterConfigManager.getInstance().addTemplateList(new StatKey(StatPool.PREFIX_PARAM), template);
    	}
    }    

}
