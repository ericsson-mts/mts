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

