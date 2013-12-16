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
package com.devoteam.srit.xmlloader.core.operations;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ExecutionException;
import com.devoteam.srit.xmlloader.core.exception.ExitExecutionException;
import com.devoteam.srit.xmlloader.core.exception.GotoExecutionException;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.XMLElementReplacer;
import com.devoteam.srit.xmlloader.core.utils.XMLTree;
import java.io.Serializable;
import org.dom4j.Element;

public abstract class Operation implements Serializable {

    /**
     * Maximum number of characters to write into the log
     */
    private static int MAX_STRING_LENGTH = Config.getConfigByName("tester.properties").getInteger("logs.MAX_STRING_LENGTH", 1000);
    /**
     * Name of the operation
     */
    protected String _name;
    /**
     * Key of the operation (for stats feature)
     */
    protected String[] _key = new String[2];
    /**
     * Object representing the XML definition of this operation
     */
    private XMLTree _xmlTree;
    /**
     * Replacer that will operation will use
     */
    private XMLElementReplacer _replacer;
    /**
     * Constructor
     *
     * @param name Name of the operation
     */
    public Operation(Element root, XMLElementReplacer replacer) {
        this(root, replacer, true);
    }

    public Operation(Element root, XMLElementReplacer replacer, boolean recurse) {
        _name = root.getName();
        _replacer = replacer;
        _key[0] = _name;
        _key[1] = "";
        _xmlTree = new XMLTree(root);
        _xmlTree.compute(Parameter.EXPRESSION, recurse);
    }

    public Operation(String aName) {
        this._name = aName;
    }

    public void setReplacer(XMLElementReplacer replacer){
        _replacer = replacer;
    }
    
    @Override
    public String toString() {
        String string = _xmlTree.toString();
        if (string.length() > MAX_STRING_LENGTH) 
        {
            string = "{" + MAX_STRING_LENGTH + " of " + string.length() + "} " + string.substring(0, MAX_STRING_LENGTH);
        }
        return string;
    }

    public String getName() {
        return _name;
    }

    public void lockAndReplace(Runner runner) throws Exception{
        _xmlTree.lock();
        _xmlTree.replace(_replacer, runner.getParameterPool());
    }
    
    public void lockAndReplace(ParameterPool parameterPool) throws Exception{
        _xmlTree.lock();
        _xmlTree.replace(_replacer, parameterPool);
    }
    
    public void unlockAndRestore(){
        try{
            _xmlTree.restore();
        }
        finally{
            _xmlTree.unlock();
        }
    }
    
    public Element getRootElement() {
        return _xmlTree.getTreeRoot();
    }

    /**
     * Returns an attribute of the root element of the XMLTree.
     *
     * @return String
     */
    public String getAttribute(String attributeName) {
        return _xmlTree.getTreeRoot().attributeValue(attributeName);
    }

    /**
     * Add the increments of statistic current counter
     */
    private void addStatCurrent1(Object value) throws Exception {
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_OPERATION, this._key[0], this._key[1], "_currentNumber"), value);
    }

    /**
     * Add the increments of statistic end counter
     */
    private void addStatEnd1(long startTimestamp) throws Exception {
        addStatCurrent1(-1);
        long endTimestamp = System.currentTimeMillis();
        float duration_stats = ((float) (endTimestamp - startTimestamp) / 1000);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_OPERATION, this._key[0], this._key[1], "_durationTime"), duration_stats);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_OPERATION, this._key[0], this._key[1], "_completeNumber"), 1);
    }

    /**
     * Add the increments of statistic KO counter
     */
    private void addStatKO1() throws Exception {
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_OPERATION, this._key[0], this._key[1], "_failedNumber"), 1);
    }
    
    /**
     * Add the increments of statistic Start counter
     */
    private void addStatStart() throws Exception {
    	StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_OPERATION, this._key[0], this._key[1], "_startNumber"), 1);
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public Operation executeAndStat(Runner runner) throws Exception {
        long startTimestamp = System.currentTimeMillis();
        addStatStart();
        addStatCurrent1(1);

        Operation nextOperation = null;
        try {
            // restore the XMLTree before executing the operation.
            // this is to have correct logs before the preparsing.

            // Execute operation
            nextOperation = execute(runner);
        }
        catch (AssertException e) {
            throw e;
        }
        catch (ExitExecutionException e) {
            if (e.getFailed()) {
                addStatKO1();
            }
            throw e;
        }
        catch (GotoExecutionException e) {
            throw e;
        }
        catch (Exception e) {
            addStatKO1();
            throw e;
        }
        finally {
            addStatEnd1(startTimestamp);
        }
        return nextOperation;
    }

    /**
     * Execute operation
     *
     * @param session Current session
     * @return Next operation or null by default
     * @throws ExecutionException
     */
    public abstract Operation execute(Runner runner) throws Exception;
}
