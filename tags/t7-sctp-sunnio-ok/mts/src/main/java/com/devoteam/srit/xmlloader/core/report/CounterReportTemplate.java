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

package com.devoteam.srit.xmlloader.core.report;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import org.dom4j.Element;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.newstats.IStatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatCounter;
import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatKeyPattern;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.report.derived.DerivedCounter;
import com.devoteam.srit.xmlloader.core.report.derived.StatCount;
import com.devoteam.srit.xmlloader.core.report.derived.StatFlow;
import com.devoteam.srit.xmlloader.core.report.derived.StatPercent;
import com.devoteam.srit.xmlloader.core.report.derived.StatText;
import com.devoteam.srit.xmlloader.core.report.derived.StatValue;

public class CounterReportTemplate implements Serializable
{

    final public String type;
    public StatKey arg1;
    public StatKey arg2;
    public String summary;
    public String name;
    public String complete;

    public CounterReportTemplate(String type, StatKey arg1, StatKey arg2, String summary, String name, String complete)
    {
        super();
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.summary = summary.trim();
        this.name = name.trim();
        this.complete = complete;
    }

    public CounterReportTemplate(String type, StatKey arg1, StatKey arg2, Element root)
    {
        super();
        this.type = type;
        this.arg1 = arg1;
        this.arg2 = arg2;
        
	    this.summary = root.attributeValue("shortDesc");
	    if (this.summary == null)
	    {
	    	this.summary = root.attributeValue("summary");
	    }
	    else
	    {
            GlobalLogger.instance().logDeprecatedMessage(
        			"stats> <... shortDesc=\"XXXX\" ...> ...</stats", 
        			"stats> <... summary=\"XXXX\" ...> ...</stats"             		            		
            		);
	    }
	    
	    this.name = root.attributeValue("description");
	    if (name == null)
	    {
	    	this.name = root.attributeValue("name");
	    }
	    else
	    {
            GlobalLogger.instance().logDeprecatedMessage(
        			"stats> <... description=\"XXXX\" ...> ...</stats", 
        			"stats> <... name=\"XXXX\" ...> ...</stats"             		            		
            		);
	    }

	    this.complete = root.attributeValue("longDesc");
	    if (complete == null)
	    {
	    	this.complete = root.attributeValue("complete");
	    }
	    else
	    {	    	
            GlobalLogger.instance().logDeprecatedMessage(
        			"stats> <... longDesc=\"XXXX\" ...> ...</stats", 
        			"stats> <... complete=\"XXXX\" ...> ...</stats"             		            		
            		);
	    }
    }

    public boolean equals(Object obj)
    {
        if (!(obj instanceof CounterReportTemplate))
        {
            return false;
        }
        CounterReportTemplate counterTemplate = (CounterReportTemplate) obj;
        if ((summary != null) && (counterTemplate.summary != null) &&
                (!counterTemplate.summary.equals(summary)))
        {
            return false;
        }
        if ((name != null) && (counterTemplate.name != null) &&
                (!counterTemplate.name.equals(name)))
        {
            return false;
        }
        if ((complete != null) && (counterTemplate.complete != null) &&
                (!counterTemplate.complete.equals(complete)))
        {
            return false;
        }
        return true;
    }
    
    public DerivedCounter getDerivedCounter(StatPool pool, StatKey prefixKey) throws ParsingException
    {
        StatKey afterMatchStatKey = matchTemplate(arg1, prefixKey);
        StatKey afterMatchArg1 = matchTemplate(arg1, prefixKey);
        StatKey afterMatchArg2 = matchTemplate(arg2, prefixKey);

        if (type.equals("<flow>"))
        {

            if (afterMatchArg1 != null)
            {
                String[] sumKey = concat("report-sum", afterMatchStatKey.getAllAttributes());

                StatCounter arg1counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg1);

                if (arg1counter != null)
                {
                    StatFlow statFlow = new StatFlow(pool.getLastTimestamp(), pool.getZeroTimestamp(), afterMatchStatKey, arg1counter, this);
                    return statFlow;
                }
            }

        }

        if (this.type.equals("<value>"))
        {
            if (afterMatchArg1 != null && afterMatchArg2 != null)
            {
                String[] sumKey = concat("report-avg", afterMatchStatKey.getAllAttributes());

                StatCounter arg1counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg1);
                StatCounter arg2counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg2);

                if (arg1counter != null && arg2counter != null)
                {
                    StatValue average = new StatValue(pool.getLastTimestamp(), pool.getZeroTimestamp(), afterMatchStatKey, arg1counter, arg2counter, this);
                    return average;
                }

            }
        }

        if (this.type.equals("<counter>"))
        {
            if (afterMatchArg1 != null)
            {
                String[] sumKey = concat("report-acc", afterMatchStatKey.getAllAttributes());

                StatCounter arg1counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg1);

                if (arg1counter != null)
                {
                    StatCount statValue = new StatCount(pool.getLastTimestamp(), pool.getZeroTimestamp(), afterMatchStatKey, arg1counter, this);
                    return statValue;
                }
            }
        }

        if (this.type.equals("<percent>"))
        {
            if (afterMatchArg1 != null && afterMatchArg2 != null)
            {
                String[] sumKey = concat("report-perc", afterMatchStatKey.getAllAttributes());

                StatCounter arg1counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg1);

                StatCounter arg2counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg2);

                if (arg1counter != null && arg2counter != null)
                {
                    StatPercent statPercent = new StatPercent(pool.getLastTimestamp(), pool.getZeroTimestamp(), afterMatchStatKey, arg1counter, arg2counter, this);
                    return statPercent;
                }
            }
        }

        if (this.type.equals("<text>"))
        {
            if (afterMatchArg1 != null)
            {
                String[] sumKey = concat("report-text", afterMatchStatKey.getAllAttributes());

                StatCounter arg1counter = (StatCounter) pool.sum(new StatKey(sumKey), afterMatchArg1);

                if (arg1counter != null)
                {
                    StatText statText = new StatText(pool.getLastTimestamp(), pool.getZeroTimestamp(), afterMatchStatKey, arg1counter, this);
                    return statText;
                }
            }
        }

        return null;
    }

    public void resetCounter(StatPool pool, StatKey prefixKey) throws ParsingException
    {
        StatKey afterMatchArg = matchTemplate(arg1, prefixKey);                     
        if (afterMatchArg != null)
        {
            pool.resetPattern(new StatKeyPattern(afterMatchArg));
        }
        afterMatchArg = matchTemplate(arg2, prefixKey);
        if (afterMatchArg != null)
        {
            pool.resetPattern(new StatKeyPattern(afterMatchArg));
        }
    }

    static private StatKey matchTemplate(StatKey patternStatKey, StatKey examinedStatKey)
    {

        if (patternStatKey == null || examinedStatKey == null)
        {
            return null;
        }

        String[] patternAttributes = patternStatKey.getAllAttributes();
        String[] examinedAttributes = examinedStatKey.getAllAttributes();

        String[] resultAttributes = new String[patternAttributes.length];
        System.arraycopy(patternAttributes, 0, resultAttributes, 0, patternAttributes.length);

        for (int i = 0; i < examinedAttributes.length; i++)
        {
            if (matches(patternAttributes[i], examinedAttributes[i]))
            {
                resultAttributes[i] = examinedAttributes[i];
            }
            else
            {
                return null;
            }
        }
        return new StatKey(resultAttributes);

    }

    static public boolean matches(String patternString, String examinedString)
    {
        Pattern pattern = Pattern.compile(patternString);
        if (pattern.matcher(examinedString).matches())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // Array help functions
    static public String[] concat(String[] A, String B)
    {
        String[] C = new String[A.length + 1];
        System.arraycopy(A, 0, C, 0, A.length);
        C[C.length - 1] = B;
        return C;
    }

    static public String[] concat(String A, String[] B)
    {
        String[] C = new String[B.length + 1];
        C[0] = A;
        System.arraycopy(B, 0, C, 1, B.length);
        return C;
    }

    public String toString()
    {
        String str;
        str = "type = " + type + "\n";
        str += "arg1 = " + arg1 + "\n";
        str += "arg2 = " + arg2 + "\n";
        str += "name = " + name + "\n";
        str += "summary = " + summary + "\n";
        str += "complete = " + complete + "\n";

        return str;
    }
}
