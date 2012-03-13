/*
 * TestRunnerSequential.java
 *
 * Created on 30 mai 2007, 11:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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

        XMLDocumentCache.reset();

        _testcaseRunner = testcase.getTestcaseRunner();
        if(_testcaseRunner.attach(this)){
            _testcaseRunner.reset();
            _testcaseRunner.addListener(this);
            _testcaseRunner.init(true);
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
