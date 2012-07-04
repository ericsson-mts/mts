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

import com.devoteam.srit.xmlloader.core.log.GlobalLogger;
import com.devoteam.srit.xmlloader.core.log.TextEvent;
import com.devoteam.srit.xmlloader.core.utils.notifications.Notification;
import com.devoteam.srit.xmlloader.core.utils.notifications.NotificationListener;

/**
 *
 * @author gpasquiers
 */
public class TestRunnerSingle extends TestRunner implements NotificationListener<Notification<String, RunnerState>> {
    private TestcaseRunner _testcaseRunner;
    
    public TestRunnerSingle(Test test, Testcase testcase) throws Exception {
        super(test.attributeValue("name"), test);

        Cache.reset();

        _testcaseRunner = testcase.getTestcaseRunner();
        if(_testcaseRunner.attach(this)){
            _testcaseRunner.reset();
            _testcaseRunner.addListener(this);
            _testcaseRunner.init();
        }
        else{
            //TODO: some error
        }
    }

    @Override
    public void start() {
        GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSingle started");
        _testcaseRunner.startSingle();
    }

    @Override
    public void stop() {
        _testcaseRunner.stop();
    }

    @Override
    public void notificationReceived(Notification<String, RunnerState> notification) {
        if(notification.getData().isFinished() || notification.getData().couldNotStart()){
            _testcaseRunner.removeListener(this);
            _testcaseRunner.detach(this);
            GlobalLogger.instance().getApplicationLogger().debug(TextEvent.Topic.CORE, "TestRunnerSingle ended");
        }
        setState(notification.getData());
        doNotifyAll();
    }
}
