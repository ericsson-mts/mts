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

import com.devoteam.srit.xmlloader.core.newstats.StatKey;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.utils.Helper;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;

import java.text.DateFormat;
import java.util.List;

/**
 *
 * @author gpasquiers
 */
public abstract class TestRunner extends Runner implements HierarchyMember<Object, TestcaseRunner>, NotificationSender<Notification<String, RunnerState>> {
    // <editor-fold defaultstate="collapsed" desc="Hierarchy implementation" >

    private DefaultHierarchyMember<Object, TestcaseRunner> defaultHierarchyMember;

    public Object getParent() {
        throw new RuntimeException("This HierarchyMember CANNOT have any parent");
    }

    public List<TestcaseRunner> getChildren() {
        return this.defaultHierarchyMember.getChildren();
    }

    public void setParent(Object parent) {
        throw new RuntimeException("This HierarchyMember CANNOT have any parent");
    }

    public void addChild(TestcaseRunner child) {
        this.defaultHierarchyMember.addChild(child);
    }

    public void removeChild(TestcaseRunner child) {
        this.defaultHierarchyMember.removeChild(child);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="NotificationSender Implementation">
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener) {
        this.defaultNotificationSender.addListener(listener);

        listener.notificationReceived(new Notification<String, RunnerState>(this.getName(), this.getState().clone()));
    }

    public void removeListener(NotificationListener listener) {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification) {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void doNotifyAll() {
        this.notifyAll(new Notification<String, RunnerState>(this.getName(), getState().clone()));
    }
    // </editor-fold>

    private Test test;

    public TestRunner(String name, Test test) {
        super(name);
        final TestRunner _this = this;
        this.test = test;
        this.defaultHierarchyMember = new DefaultHierarchyMember<Object, TestcaseRunner>();
        this.defaultNotificationSender = new DefaultNotificationSender<Notification<String, RunnerState>>();
        if(null != test){
            this.setParameterPool(test.getParameterPool());
        }
        else{
            this.setParameterPool(new ParameterPool(null, ParameterPool.Level.standalone, null));
        }

        this.addListener(new NotificationListener<Notification<String, RunnerState>>() {
            private boolean finishedOld = false;
            private boolean startedOld = false;

            public void notificationReceived(Notification<String, RunnerState> notification) {
                if (!startedOld && notification.getData().isStarted()) {
                    TestRunnerCounter.instance().runningTestsIncreased();
                    // add static stat counters for test and parameters sections            	    
                    StatPool.getInstance().addStatsStaticTestParameters(getTest());
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_startNumber"), 1);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_currentNumber"), 1);
                    long currentTime = System.currentTimeMillis();
            	    getTest().setBeginTime(currentTime);
            	    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
            	    String startTime = dateFormat.format(currentTime);
            	    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_beginTime"), startTime);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_endTime"), "?");
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_testDuration"), "?");            	    
                }

                // ending of test
                if (!finishedOld && notification.getData().isFinished()) {
                    TestRunnerCounter.instance().runningTestsDecreased();
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_completeNumber"), 1);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_currentNumber"), -1);
                    long currentTime = System.currentTimeMillis();
            	    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
                    String endTime = dateFormat.format(currentTime);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_endTime"), endTime);
                    long duration = (currentTime - getTest().getBeginTime()) / 1000;
                    String testDuration = Helper.getElapsedTimeString(duration);
                    StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_testDuration"), testDuration);                    
                }
                
                startedOld = notification.getData().isStarted();
                finishedOld = notification.getData().isFinished();
            }
        });

    }

    public Test getTest() {
        return this.test;
    }

    final protected void statAddValue(boolean sure) {
        this.statAddValue(0, sure);
    }

    public abstract void start();

    public abstract void stop();
    
    final protected void statAddValue(long duration, boolean sure) {
        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        long endTimestamp = getTest().getBeginTime() + duration;
        String endTime = "";
        String testTime = "";
        if (duration > 0) {
            endTime = df1.format(endTimestamp);
            testTime = Helper.getElapsedTimeString(duration / 1000);
        }
        if (!sure) {
            endTime += " ?";
            testTime += " ?";
        }        
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_endTime"), endTime);
        StatPool.getInstance().addValue(new StatKey(StatPool.PREFIX_TEST, getTest().getName(), "_testTime"), testTime);
    }
}
