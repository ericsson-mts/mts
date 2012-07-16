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
package com.devoteam.srit.xmlloader.core.operations.basic;

import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.newstats.StatCounterConfigManager;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.report.CounterReportTemplate;

import com.devoteam.srit.xmlloader.core.utils.Utils;
import com.devoteam.srit.xmlloader.core.utils.XMLElementAVPParser;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

/**
 * enables your scenario to execute operations according to value of a boolean variable named condition
 *
 * @author ma007141
 *
 */
public class OperationStats extends Operation {

    /**
     * Constructor
     *
     * @param root XML root element of this operation.
     * @param scenario Scenario this operation belongs to.
     */
    public OperationStats(Element root) throws Exception {
        super(root, XMLElementAVPParser.instance());
    }

    /**
     * Execute operation
     *
     *
     * @param runner Current runner
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation execute(Runner runner) throws Exception {
        GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, this);

        try {
            lockAndReplace(runner);
            
            GlobalLogger.instance().getSessionLogger().debug(runner, TextEvent.Topic.CORE, "Operation after pre-parsing \n", this);

            List<Element> elements = (List<Element>) this.getRootElement().elements();
            for (Element counterElement : elements) {
                String counterElementName = counterElement.getName();

                if (counterElementName.equals("counter")) {
                    handleCounter(runner, counterElement);
                }
                else if (counterElementName.equals("flow")) {
                    handleFlow(runner, counterElement);
                }
                else if (counterElementName.equals("histogram")) {
                    GlobalLogger.instance().logDeprecatedMessage(
                            "stats> <histogram ...> ...</stats",
                            "stats> <value ...> ...</stats");
                    handleValue(runner, counterElement);
                }
                else if (counterElementName.equals("value")) {
                    handleValue(runner, counterElement);
                }
                else if (counterElementName.equals("percent")) {
                    handlePercent(runner, counterElement);
                }
                else if (counterElementName.equals("text")) {
                    handleText(runner, counterElement);
                }
                else if (counterElementName.equals("reset")) {
                    handleReset(runner, counterElement);
                }
                else {
                    throw new Exception("Feature not yet implemented");
                }
            }
        }
        finally {
            unlockAndRestore();
        }


        return null;
    }

    /**
     * Read the attributes and check the existing with different parameter case for the current counter
     *
     * @throws ExecutionException
     */
    private boolean checkStoreTemplate(StatKey statKey, CounterReportTemplate template) throws Exception {
        // check attributes		
        boolean exist = StatCounterConfigManager.getInstance().containsTemplateList(statKey, template);
        if (exist) {
            /*
             * if (!template.equals(oldTempl)) { throw new ExecutionException("Stats counter <" + name + "> already exists with different parameters"); }
             */
        }
        else {
            if (template.summary != null) {
                //store them in the StatCounterConfigManager
                StatCounterConfigManager.getInstance().addTemplateList(statKey, template);
            }
        }
        return true;
    }

    private void handleCounter(Runner runner, Element root) throws Exception {
        // read attributes
        String name = root.attributeValue("name");
        StatKey statKey = new StatKey(StatPool.PREFIX_USER, "value", name, "_count");

        // create the corresponding template
        CounterReportTemplate template = new CounterReportTemplate("<counter>", statKey, null, root);

        // read and check the attributes 
        checkStoreTemplate(new StatKey(StatPool.PREFIX_USER), template);

        // Now execute the action.
        List<Element> actionElements = (List<Element>) root.elements();
        for (Element actionElement : actionElements) {
            String actionElementName = actionElement.getName();
            if (actionElementName.equals("increase")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKey, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKey, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_count =+", value);
            }
            else if (actionElementName.equals("decrease")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKey, -value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKey, -value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_count =-", value);
            }
        }
    }

    private void handleFlow(Runner runner, Element root) throws Exception {
        // read attributes
        String name = root.attributeValue("name");
        StatKey statKey = new StatKey(StatPool.PREFIX_USER, "value", name, "_count");

        // create the corresponding template
        CounterReportTemplate template = new CounterReportTemplate("<flow>", statKey, null, root);

        // read and check the attributes 
        checkStoreTemplate(new StatKey(StatPool.PREFIX_USER), template);

        // Now execute the action.
        List<Element> actionElements = (List<Element>) root.elements();
        for (Element actionElement : actionElements) {
            String actionElementName = actionElement.getName();
            if (actionElementName.equals("increase")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKey, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKey, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_count =+", value);
            }
            else if (actionElementName.equals("decrease")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKey, -value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKey, -value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_count =-", value);
            }
        }
    }

    private void handleValue(Runner runner, Element root) throws Exception {
        // read attributes
        String name = root.attributeValue("name");

        // create the corresponding template 
        StatKey statKeyValue = new StatKey(StatPool.PREFIX_USER, "value", name, "_value");
        StatKey statKeyCount = new StatKey(StatPool.PREFIX_USER, "value", name, "_count");
        CounterReportTemplate template = new CounterReportTemplate("<value>", statKeyValue, statKeyCount, root);

        // read and check the attributes 
        checkStoreTemplate(new StatKey(StatPool.PREFIX_USER), template);

        // Now execute the action.
        List<Element> actionElements = (List<Element>) root.elements();
        for (Element actionElement : actionElements) {
            String actionElementName = actionElement.getName();
            if (actionElementName.equals("newValue")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKeyValue, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKeyValue, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_total +=", value);
                StatPool.getInstance().addValue(statKeyCount, 1);
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_count +=", 1);
            }
        }
    }

    private void handlePercent(Runner runner, Element root) throws Exception {
        // read attributes
        String name = root.attributeValue("name");
        StatKey statKeyValue = new StatKey(StatPool.PREFIX_USER, "value", name, "_value");
        StatKey statKeyTotal = new StatKey(StatPool.PREFIX_USER, "value", name, "_total");

        // create the corresponding template
        CounterReportTemplate template = new CounterReportTemplate("<percent>", statKeyValue, statKeyTotal, root);

        // read and check the attributes 
        checkStoreTemplate(new StatKey(StatPool.PREFIX_USER), template);

        // Now execute the action.
        List<Element> actionElements = (List<Element>) root.elements();
        for (Element actionElement : actionElements) {
            String actionElementName = actionElement.getName();
            if (actionElementName.equals("incValue")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKeyValue, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKeyValue, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_value =+", value);
            }
            else if (actionElementName.equals("incTotal")) {
                double value = Double.parseDouble((null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText()));
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKeyTotal, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKeyTotal, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_total =+", value);
            }
        }
    }

    private void handleText(Runner runner, Element root) throws Exception {
        // read attributes
        String name = root.attributeValue("name");

        // create the corresponding template 
        StatKey statKeyValue = new StatKey(StatPool.PREFIX_USER, "value", name, "_value");
        CounterReportTemplate template = new CounterReportTemplate("<text>", statKeyValue, null, root);

        // read and check the attributes 
        checkStoreTemplate(new StatKey(StatPool.PREFIX_USER), template);

        // Now execute the action.
        List<Element> actionElements = (List<Element>) root.elements();
        for (Element actionElement : actionElements) {
            String actionElementName = actionElement.getName();
            if (actionElementName.equals("newValue")) {
                String value = (null != actionElement.attributeValue("value") ? actionElement.attributeValue("value") : actionElement.getText());
                long timestamp = getTimestamp(actionElement);
                if (-1 != timestamp) {
                    StatPool.getInstance().addValue(statKeyValue, value, timestamp);
                }
                else {
                    StatPool.getInstance().addValue(statKeyValue, value);
                }
                GlobalLogger.instance().getSessionLogger().info(runner, TextEvent.Topic.CORE, "Statistic : ", name, "/_value +=", value);
            }
        }
    }

    private void handleReset(Runner runner, Element root) throws Exception {
        // read attributes
        String counterName = root.attributeValue("name");
        // reset all stat counters
        if (counterName == null) {
            StatPool.getInstance().reset();
            return;
        }

        counterName = counterName.trim();
        String path = root.attributeValue("path");
        if (path == null) {
            path = "user>" + counterName;
        }
        path = path.trim();
        if (path.charAt(0) == '>') {
            path = path.substring(1, path.length());
        }
        String[] key = Utils.splitNoRegex(path, ">");
        StatKey statKey = new StatKey(key);

        List<CounterReportTemplate> templates = StatCounterConfigManager.getInstance().getTemplateList(statKey);
        Iterator<CounterReportTemplate> iter = templates.iterator();
        while (iter.hasNext()) {
            CounterReportTemplate template = iter.next();
            // should replace LinkedList by LinkedHashMap  
            if (counterName.equalsIgnoreCase(template.name)) {
                template.resetCounter(StatPool.getInstance(), statKey);
            }
        }
    }

    private long getTimestamp(Element element) {
        long timestamp = -1;
        String millis = element.attributeValue("milliseconds");
        String seconds = element.attributeValue("seconds");

        if (null != millis) {
            timestamp = Long.parseLong(millis);
        }
        else if (null != seconds) {
            timestamp = (long) (Double.parseDouble(seconds) * 1000);
        }

        return timestamp;
    }
}
