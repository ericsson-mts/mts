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
*//*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devoteam.srit.xmlloader.master.masterutils;

import com.devoteam.srit.xmlloader.core.ParameterPool;
import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.RunnerState.State;
import com.devoteam.srit.xmlloader.core.Test;
import com.devoteam.srit.xmlloader.core.Tester;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerLoad;
import com.devoteam.srit.xmlloader.core.TestRunnerSequential;
import com.devoteam.srit.xmlloader.core.ThreadPool;
import com.devoteam.srit.xmlloader.core.newstats.StatPool;
import com.devoteam.srit.xmlloader.core.operations.Operation;
import com.devoteam.srit.xmlloader.core.operations.basic.OperationParameter;
import com.devoteam.srit.xmlloader.core.report.ReportGenerator;
import com.devoteam.srit.xmlloader.core.utils.Config;
import com.devoteam.srit.xmlloader.core.utils.URIRegistry;
import com.devoteam.srit.xmlloader.core.utils.exceptionhandler.ExceptionHandlerSingleton;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.DefaultHierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.hierarchy.HierarchyMember;
import com.devoteam.srit.xmlloader.core.utils.notifications.DefaultNotificationSender;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationSender;
import com.devoteam.srit.xmlloader.master.MasterImplementation;
import com.devoteam.srit.xmlloader.master.testmanager.RemoteTester;
import com.devoteam.srit.xmlloader.master.testmanager.RemoteTesterProvider;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.dom4j.Element;

/**
 *
 * @author gpasquiers
 */
public class RemoteTesterRunner implements HierarchyMember<MasterRunner, RemoteTester>, NotificationSender<Notification<String, RunnerState>>, NotificationListener<Notification<String, RunnerState>>
{
    // <editor-fold defaultstate="collapsed" desc="Hierarchy implementation">
    private DefaultHierarchyMember<MasterRunner, RemoteTester> defaultHierarchyMember;
    
    public MasterRunner getParent()
    {
        return this.defaultHierarchyMember.getParent();
    }

    public List<RemoteTester> getChildren()
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any child");
    }

    public void setParent(MasterRunner parent)
    {
        this.defaultHierarchyMember.setParent(parent);
    }

    public void addChild(RemoteTester child)
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any child");
    }

    public void removeChild(RemoteTester child)
    {
        throw new RuntimeException("This HierarchyMember CANNOT have any child");
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="NotificationSender Implementation" >
    private DefaultNotificationSender<Notification<String, RunnerState>> defaultNotificationSender;

    private Notification<String, RunnerState> lastNotification;
    private State lastState;
    private int lastProgression;

    public void addListener(NotificationListener<Notification<String, RunnerState>> listener)
    {
        this.defaultNotificationSender.addListener(listener);
        
        if(null != this.lastNotification) listener.notificationReceived(this.lastNotification);
    }

    public void removeListener(NotificationListener listener)
    {
        this.defaultNotificationSender.removeListener(listener);
    }

    public void notifyAll(Notification<String, RunnerState> notification)
    {
        this.defaultNotificationSender.notifyAll(notification);
    }

    public void notifyAll(RunnerState runnerState)
    {
        this.defaultNotificationSender.notifyAll(new Notification<String, RunnerState>(this.testData.attributeValue("name"), runnerState));
    }
    // </editor-fold>

    // <editor-fold  defaultstate="collapsed" desc="NotificationListener Implementation">
    public void notificationReceived(Notification<String, RunnerState> notification)
    {
        notification.getData().setIndex(this.index);

        if((lastNotification == null)
          || (lastState != notification.getData().getState())
          || (lastProgression != notification.getData().getProgression()))
        {
            this.lastState = notification.getData().getState();
            this.lastProgression = notification.getData().getProgression();
            this.lastNotification = notification;
            this.notifyAll(notification);
        }
    }
    // </editor-fold>

    /*
     * Object representing a test inside of a master and containing informations
     * such as slave, path, and some parameter operations.
     */
    private TestData testData;
    
    /*
     * RemoteTester provider. This class will provide (when asked) a RemoteTester
     * to run the test. This classes choses if the remote tester is a RMI slave
     * (in this case it finds it using the slave attribute).
     * If there is no slave attribute it should return a local implementation
     * of the RemoteTester interface.
     */
    private RemoteTesterProvider remoteTesterProvider;
    
    /*
     * Interface to the RemoteTester that will run the test. That RemoteTester
     * could very well be a local implementation of the interface.
     */
    private RemoteTester remoteTester;
    
    /*
     * Test object as opened on the RemoteTester.
     */
    private Test test;
    
    /*
     * Map of parameters as simple strings (name, value) that are computed from
     * the TestData object and are used to configure the Test on the RemoteTester
     * (replaces the values of "configurable" parameters).
     */
    private HashMap<String, String> initialParametersValues;
    
    /*
     * StatPool saved when the test ended. Used to generate report.
     */
    private StatPool statPool;
    
    /*
     * Timestamp of the last start. This is used with the reporting because
     * all StatPool values are relative to their start date.
     */
    private long startTimestamp;
    
    /*
     * Index that will be set on the notifications coming from the RemoteTester.
     */
    private int index;
    
    public RemoteTesterRunner(TestData testData, int index) throws Exception
    {
        this.test = null;
        this.index = index;
        this.statPool = null;
        this.testData = testData;
        this.remoteTester = null;
        this.defaultHierarchyMember = new DefaultHierarchyMember();
        this.remoteTesterProvider = new RemoteTesterProvider();
        this.defaultNotificationSender = new DefaultNotificationSender();
        this.initParameterPool();
    }
    
    /**
     * Run the "parameter" operations of the TestData objet and fill the
     * initialParametersValues HashMap.
     * @throws java.lang.Exception
     */
    private void initParameterPool() throws Exception
    {
        final Element root = this.testData.getRoot();
                
        TestRunner runnerTest = new TestRunner("temp_test", null) {
            @Override
            public void start()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void init() throws Exception
            {
                LinkedList<Operation> operations = new LinkedList<Operation>();
                List<Element> parameterElements = (List<Element>) root.elements("parameter");
                for (Element parameterElement : parameterElements)
                {
                    operations.add(new OperationParameter(parameterElement));
                }

                for (Operation operation : operations)
                {
                    operation.executeAndStat(this);
                }
            }

            @Override
            public void stop()
            {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        runnerTest.setParameterPool(new ParameterPool(null, ParameterPool.Level.standalone, null));
        runnerTest.init();
        
        initialParametersValues = new HashMap<String, String>();
        
        for(String parameterName:runnerTest.getParameterPool().getParametersNameLocal())
        {
            
            if(runnerTest.getParameterPool().get(parameterName).length() != 1)
            {
                initialParametersValues.put(parameterName, null);
            }
            else
            {
                initialParametersValues.put(parameterName, runnerTest.getParameterPool().get(parameterName).get(0).toString());
            }
        }
    }
    
    public void init(final boolean force) throws Exception
    {
        /*
         * Find a RemoteTester (it might be a local "RemoteTester") if the slave
         * attribute is null.
         */
        try
        {
            this.remoteTester = this.remoteTesterProvider.get(testData.attributeValue("slave"));
        }
        catch(Exception e)
        {
            RunnerState runnerState = new RunnerState();
            runnerState.setIndex(this.index);
            runnerState.setProgression(0);
            Notification<String, RunnerState> notification = new Notification(this.testData.attributeValue("name"), runnerState);
            notification.getData().setState(State.OPEN_FAILED);
            this.notificationReceived(notification);
            ExceptionHandlerSingleton.instance().display(e, null);
            return;
        }

        /*
         * Deploy the test (make the RemoteTester open it).
         */
        final RemoteTesterRunner _this = this;
        
        ThreadPool.reserve().start(new Runnable(){
            public void run(){
                RunnerState runnerState = new RunnerState();
                runnerState.setIndex(_this.index);
                runnerState.setProgression(0);
                String testName = _this.testData.attributeValue("name");
                Notification<String, RunnerState> notification = new Notification(testName, runnerState);
                try
                {
                    notification.getData().setState(State.OPENING);
                    _this.notificationReceived(notification);

                    _this.test = _this.remoteTester.openTest(_this.testData.getPath(), URIRegistry.IMSLOADER_BIN, testName, _this.testData.attributeValue("home"), _this.initialParametersValues, force);

                    notification.getData().setState(State.OPEN_SUCCEEDED);
                    _this.notificationReceived(notification);
                }
                catch (Exception e)
                {
                    notification.getData().setState(State.OPEN_FAILED);
                    _this.notificationReceived(notification);
                    ExceptionHandlerSingleton.instance().display(e, null);
                }
            }
        });
    }
    
    public RemoteTester getRemoteTester()
    {
        if(null == this.remoteTester)
        {
            throw new RuntimeException("called too soon! remoteTester not set!");
        }
        
        return this.remoteTester;
    }
    
    public Test getTest()
    {
        return this.test;
    }
    
    public long getStartTimestamp()
    {
        return this.startTimestamp;
    }
    
    /**
     * This method returns the statpool of the remote slave or an exception if
     * there is no slave yet since it means the test was never deployed.
     * If the test stopped then the statpool is cached in this object for further
     * calls to this method.
     * If this method is called during the test execution, the statpool is returned
     * "as is" from the slave through RMI.
     * @return The statpool of the remote slave.
     * @throws java.rmi.RemoteException
     */
    public StatPool getStatPool() throws RemoteException
    {
        if(null == this.statPool)
        {
            StatPool pool = this.getRemoteTester().getStatPool();
            pool.setUpdateLastTimestamp(false);
            return pool;
        }
        else
        {
            return this.statPool;
        }
    }
    
    /**
     * Do all the reporting in a separate thread.
     * This method is asynchronous and returns the ReportGenerator for the caller
     * to register it's own listeners and get updates on the progression.
     * This method already adds a listener that calls the MasterFrame for it to 
     * refresh it's state (open/close menus allowed or not).
     * @return the report generator
     * @throws java.lang.Exception
     */
    public ReportGenerator report() throws Exception
    {
        final StatPool statpool = this.getStatPool();
        final long zeroTimestamp = this.startTimestamp;
        // generate stat report
    	String dirName = Config.getConfigByName("tester.properties").getString("stats.REPORT_DIRECTORY","../reports/");
    	String fileName = dirName +  "/MASTER_TEST_" + this.testData.attributeValue("name"); 
        final ReportGenerator reportGenerator = new ReportGenerator(fileName);
        
        final Test test = this.test;
        ThreadPool.reserve().start(new Runnable(){
            public void run(){
                try
                {
                    reportGenerator.generateReport(statpool, zeroTimestamp);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        return reportGenerator;
    }
    
    public void resetStats(long timestamp) throws Exception
    {
        this.getRemoteTester().resetStatPool();
        this.startTimestamp = timestamp;
    }
    
    public void start()throws Exception
    {

        final RemoteTesterRunner _this = this;
        this.statPool = null;

        ThreadPool.reserve().start(new Runnable(){
            public void run(){
                try
                {
                    if (_this.testData.attributeValue("runner").equalsIgnoreCase("sequential"))
                    {
                        _this.getRemoteTester().startTest(TestRunnerSequential.class);
                    }
                    else if (_this.testData.attributeValue("runner").equalsIgnoreCase("load"))
                    {
                        _this.getRemoteTester().startTest(TestRunnerLoad.class);
                    }
                    else
                    {
                        throw new RemoteException("Invalid runner name: " + _this.testData.attributeValue("runner"));
                    }

                    /*
                     * Register a listener to the test in order to get future updates.
                     */
                    MasterImplementation.instance().addMultiplexedListener(_this, _this.getRemoteTester(), _this.test.attributeValue("name"), null, null);
                    
                    _this.addListener(new NotificationListener<Notification<String, RunnerState>>(){
                        public void notificationReceived(Notification<String, RunnerState> notification){
                            switch(notification.getData().getState())
                            {
                                case SUCCEEDED:
                                case FAILED:
                                case INTERRUPTED:
                                    try
                                    {
                                        _this.statPool = _this.getRemoteTester().getStatPool();
                                        _this.statPool.setUpdateLastTimestamp(false);
                                    }
                                    catch(Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    _this.removeListener(this);
                                    try
                                    {
                                        MasterImplementation.instance().removeMultiplexedListener(_this);
                                    }
                                    catch(Exception e)
                                    {
                                        System.err.println("TODO " + e);
                                    }
                                    break;
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    RunnerState runnerState = new RunnerState();
                    runnerState.setState(State.FAILED);
                    runnerState.setIndex(_this.index);
                    runnerState.setProgression(100);
                    System.out.println("test exception");
                    _this.notificationReceived(new Notification<String, RunnerState>(_this.testData.attributeValue("name"), runnerState));
                    ExceptionHandlerSingleton.instance().display(e, null);
                }
            }
        });
    }
    
    /**
     * Stops the test. It only calls the TestRunner.stop() method. The consequencies
     * of this call should come by the notifications having a State @ INTERRUTING
     * and/or INTERRUPTED.
     * @throws java.lang.Exception
     */
    public void stop() throws RemoteException
    {
        this.remoteTester.stopTest();
    }

    public TestData getTestData()
    {
        return this.testData;
    }
    
    private boolean disposed = false;
    public void dispose() throws Exception
    {
        if(disposed) return;
        
        try
        {
            MasterImplementation.instance().removeMultiplexedListener(this);
        }
        catch(Exception e)
        {
            ExceptionHandlerSingleton.instance().display(e, null);
        }
        
        this.remoteTesterProvider.free();
        disposed = true;
    }
}
