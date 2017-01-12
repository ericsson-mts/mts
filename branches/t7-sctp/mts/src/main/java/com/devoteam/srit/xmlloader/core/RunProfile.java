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

package com.devoteam.srit.xmlloader.core;

import com.devoteam.srit.xmlloader.core.exception.ParsingException;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.DateUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;
import org.dom4j.Element;

/**
 * This class tells a testcase when to start again, depending on the profile
 * that has beed defined.
 * @author gpasquiers
 */
public class RunProfile implements Cloneable, Serializable
{
    static public final int PROFILE_INF = -1;

    private long executions;

    // zero is now
    // positive is a date
    // degative is a delay from "now"
    private long startTime;
    private long endTime;

    private long profilePeriod;
    private int profileSize;
    private ArrayList<Point> points;

    private Element root;

    private boolean nothingToDo = false;

    /**
     * Parsing from an XML root.
     *   each "set" element must contain:
     *     attribute time (seconds)
     *     attribute frequence (hertz) XOR attribute period (seconds)
     * @param root
     */
    public RunProfile(Element root) throws Exception
    {
        this.root = root;
        this.parse(root);
    }

    public Element getRoot()
    {
        return this.root;
    }
    
    public synchronized void parse(Element root) throws Exception
    {
        long now = System.currentTimeMillis();
        
        this.executions = 0;
        this.points = new ArrayList();
        this.profilePeriod = PROFILE_INF;

        Element start = root.element("start");
        if(null != start)
        {
            if(null != start.attributeValue("time"))
            {
            	String date = start.attributeValue("time");
            	this.startTime = DateUtils.parseDate(date);
            }
            else if (null != start.attributeValue("delay"))
            {
                this.startTime = - (long) (Double.parseDouble(start.attributeValue("delay")) * 1000);
            }
            else
            {

            }

            if(null != start.attributeValue("time") && null != start.attributeValue("delay"))
            {
                throw new ParsingException("only specify time or delay attributes in <start>, not both");
            }

        }
        else
        {
            this.startTime = 0;
        }


        for(Object object : root.elements("step"))
        {
            Element element = (Element) object;

            long time = (long) Double.parseDouble(element.attributeValue("time")) * 1000;

            if(time == this.profilePeriod) time += 1;
            else if(time < this.profilePeriod) throw new ParsingException("time isn't greater than previous time");

            double frequence;

            String periodStr    = element.attributeValue("period");
            String frequencyStr = element.attributeValue("frequency");

            if(periodStr == null && frequencyStr == null) throw new ParsingException("missing period or frequence attribute");
            if(periodStr != null && frequencyStr != null) throw new ParsingException("please define only period or frequence attribute");


            if(null != periodStr && Double.parseDouble(periodStr) > 0)
            {
                frequence = (1 / Double.parseDouble(periodStr));
            }
            else
            {
                frequence = PROFILE_INF;
            }

            if(null != frequencyStr) frequence = Double.parseDouble(frequencyStr);
            if(frequence == 0) frequence = 0.000000001;
            this.points.add(new Point(time, frequence));
            this.profilePeriod = time;
        }

        // add steps if there is only one or no step at time 0
        if(this.points.size() == 0)
        {
            double period = Config.getConfigByName("tester.properties").getDouble("runprofile.PERIOD");
            if(0 == period) period = PROFILE_INF;
            this.points.add(new Point(0,period));
            // add point @ 0
        }

        // add steps if there is only one or no step at time 0
        if(this.points.get(0).date != 0)
        {
            this.points.add(0, new Point(0,0.000000001));
            // add point @ 0
        }

        // if there is only one step at zero
        if(this.points.size() == 1)
        {
            // add point to make a constant, ten second later
            this.points.add(this.points.get(0).clone());

            if(this.points.get(0).frequency == -1){
                this.points.get(1).date = 10000;
            }
            else{
                double delay = 1/this.points.get(0).frequency;

                while (delay < 1) delay *= 10;

                delay = Math.round(delay);

                this.points.get(1).date = delay * 10000;
            }
            
            this.profilePeriod = (long) Math.round(this.points.get(1).date);
        }


        Element end = root.element("end");
        if(null != end)
        {
            boolean hasTime = true;
            boolean hasExecutions = true;

            if(null != end.attributeValue("time"))
            {
            	String date = end.attributeValue("time");
            	this.endTime = DateUtils.parseDate(date);
            }
            else if (null != end.attributeValue("delay"))
            {
                long delay = (long) (Double.parseDouble(end.attributeValue("delay")) * 1000);                
                if(delay == 0) this.endTime = 0;
                else this.endTime = - delay;
            }
            else
            {
                hasTime = false;
            }

            if (null != end.attributeValue("iteration"))
            {
                this.executions = Long.parseLong(end.attributeValue("iteration"));
            }
            else
            {
                hasExecutions = false;
            }

            if(null != end.attributeValue("time") && null != end.attributeValue("delay"))
            {
                throw new ParsingException("only specify time or delay attributes in <end>, not both");
            }
            if(!hasTime && !hasExecutions)
            {
                // some error, must define end by time or iterations
            }
        }
        else
        {
            long delay = (long) Config.getConfigByName("tester.properties").getDouble("runprofile.DURATION", 0) * 1000;
            if(delay == 0) this.endTime = 0;
            else this.endTime = - delay;

            this.executions = Config.getConfigByName("tester.properties").getInteger("runprofile.NUMBER", 0);
        }

        long tempStartTime = 0;
        if(startTime<0) tempStartTime = now - startTime;
        if(startTime>0) tempStartTime = startTime;

        long tempEndTime = 0;
        if(endTime<0) tempEndTime = now - endTime;
        if(endTime>0) tempEndTime = endTime;

        if(0 != tempEndTime && tempEndTime <= tempStartTime) throw new ParsingException("start time cannot be greater than end time (check start and end dates or delays?)");

        // if the profile is a zero-only, set executions to zero and endTime to startTime
        nothingToDo = true;
        for(int i=0; i<points.size(); i++)
        {
            if(points.get(i).frequency != 0.000000001)
            {
                nothingToDo = false;
                break;
            }
        }

        this.profileSize = this.points.size();
    }

    public RunProfileContext createContext()
    {
        RunProfileContext context = new RunProfileContext();

        context.currentPointIndex = 0;

        long now = System.currentTimeMillis();

        context.startTime = now;
        if(startTime<0) context.startTime = now - startTime;
        if(startTime>0) context.startTime = startTime;

        context.endTime = 0;
        if(endTime<0) context.endTime = now - endTime;
        if(endTime>0) context.endTime = endTime;

        return context;
    }

    public ArrayList<Point> getPoints()
    {
        return this.points;
    }

    synchronized public double getNextDate(double date, RunProfileContext context)
    {
        Point currentPoint = points.get(context.currentPointIndex);
        Point nextPoint = points.get(context.currentPointIndex + 1);

        if(0 != this.getStartTime(context) && date < this.getStartTime(context)) return this.getStartTime(context) - date;

        date -= this.getStartTime(context);

        double computingDate = date % this.profilePeriod;

        while(!(currentPoint.date <= computingDate && nextPoint.date > computingDate))
        {
            // System.out.println(currentPoint.date + "<=" + computingDate + " && " + nextPoint.date + ">" + computingDate);
            context.currentPointIndex++;
            context.currentPointIndex %= (this.profileSize - 1);

            currentPoint = points.get(context.currentPointIndex);
            nextPoint = points.get(context.currentPointIndex + 1);
        }
        
        if(currentPoint.frequency == PROFILE_INF || nextPoint.frequency == PROFILE_INF) return PROFILE_INF;

        double completeDateDelta = nextPoint.date - currentPoint.date;
        double currentDateDelta = computingDate - currentPoint.date;
        double completeFrequenceDelta = nextPoint.frequency - currentPoint.frequency;

        double currentDelayDelta = 1000 / (currentPoint.frequency + completeFrequenceDelta * currentDateDelta / completeDateDelta);


        Point point = points.get((context.currentPointIndex + 1) % this.profileSize);

        double nextDate = (point.date > computingDate) ? point.date : point.date + this.profilePeriod;

        if(currentPoint.frequency < 1 && point.frequency > 1)
        {
            double nextDateCandidate = Math.round(currentPoint.date + (nextDate - currentPoint.date) / (point.frequency - currentPoint.frequency));
            if(nextDateCandidate < nextDate) nextDate = nextDateCandidate;
        }

        if(computingDate < nextDate && computingDate + currentDelayDelta > nextDate)
        {
            currentDelayDelta = nextDate - computingDate;
        }

        date += currentDelayDelta + this.getStartTime(context);
        return date;
    }

    synchronized public void add(double freq){

        for(Point point:this.points){
            if(point.frequency != 0.000000001 && point.frequency != PROFILE_INF){
                point.frequency += freq;
            }
            if(point.frequency <= 0.000000001 && point.frequency != PROFILE_INF){
                point.frequency = 0.000000001;
            }
        }
    }

    synchronized public void reset(){
        // this should not throw any exception because if we get there then the
        // parse method has been already called at least one time successfully
        try{
            parse(this.getRoot());
        }
        catch(Exception e){
            e.printStackTrace(); // should not happen
        }
    }



    public long getExecutions()
    {
        return this.executions;
    }

    /**
     * @return the startTime
     */
    public long getStartTime(RunProfileContext context)
    {
        return context.startTime;
    }

    /**
     * @return the endTime
     */
    public long getEndTime(RunProfileContext context)
    {
        return context.endTime;
    }

    public boolean nothingToDo()
    {
        return nothingToDo;
    }


    /**
     * One point of the profile
     */
    public class Point implements Cloneable, Serializable
    {
        public double date;
        public double frequency;

        public Point(double date, double frequence)
        {
            this.date = date;
            this.frequency = frequence;
        }

        @Override
        public Point clone()
        {
            return new Point(this.date, this.frequency);
        }

        @Override
        public String toString()
        {
            return "(date=" + date +", frequence= " + frequency + ")";
        }
    }
}
