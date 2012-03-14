/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.devoteam.srit.xmlloader.gui.better;

import com.devoteam.srit.xmlloader.core.RunnerState;
import com.devoteam.srit.xmlloader.core.TestRunner;
import com.devoteam.srit.xmlloader.core.TestRunnerSingle;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;
import com.devoteam.srit.xmlloader.gui.TesterGui;
import java.util.ArrayList;

/**
 *
 * @author Gwenhael
 */
public class RegistryTestRunner {
    private static RegistryTestRunner _instance = null;
    
    public synchronized static RegistryTestRunner getInstance(){
        if(null == _instance){
            _instance = new RegistryTestRunner();
        }
        return _instance;
    }
    
    private final ArrayList<TestRunner> _activeTestRunners;
    
    private RegistryTestRunner(){
        _activeTestRunners = new ArrayList();
    }
    
    public void registerTestRunner(final TestRunner testRunner){
        synchronized(_activeTestRunners){
            _activeTestRunners.add(testRunner);
            doUpdateForMenus();
        }
        
        testRunner.addListener(new NotificationListener<Notification<String, RunnerState>>() {
            @Override
            public void notificationReceived(Notification<String, RunnerState> notification) {
                RunnerState s = notification.getData();
                if(s.isFinished() || s.couldNotStart()){
                    testRunner.removeListener(this);
                    unregisterTestRunner(testRunner);
                }
            }
        });
    }
    
    private void unregisterTestRunner(TestRunner testRunner){
        synchronized(_activeTestRunners){
            _activeTestRunners.remove(testRunner);
            doUpdateForMenus();
        }
    }
    
    
    public ArrayList<TestRunner> getActiveTestRunners(){
        synchronized(_activeTestRunners){
            return (ArrayList<TestRunner>) _activeTestRunners.clone();
        }
    }
    
    private void doUpdateForMenus(){
        boolean testcase = false;
        boolean test = false;
        for(TestRunner runner : _activeTestRunners){
            if(runner instanceof TestRunnerSingle){
                testcase = true;
            }
            else{
                test = true;
            }
        }
        TesterGui.instance().getGUIMenuHelper().updateMenuStatesTestcase(testcase);
        TesterGui.instance().getGUIMenuHelper().updateMenuStatesTest(test);
    }
}

